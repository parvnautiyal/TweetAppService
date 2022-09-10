package com.iiht.usecase.tweetapp;

import com.iiht.usecase.tweetapp.domain.TweetEvent;
import com.iiht.usecase.tweetapp.entity.Tweet;
import com.iiht.usecase.tweetapp.entity.User;
import com.iiht.usecase.tweetapp.entity.dto.TweetDto;
import com.iiht.usecase.tweetapp.entity.dto.UserDto;
import com.iiht.usecase.tweetapp.repository.TweetRepository;
import com.iiht.usecase.tweetapp.repository.UserRepository;
import com.iiht.usecase.tweetapp.util.UserUtil;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.iiht.usecase.tweetapp.util.Constants.BASE_URI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(topics = "tweet-event", partitions = 4)
@TestPropertySource(properties = { "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}" })
@Testcontainers
class UserControllerIntegrationTest {

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(
            DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TweetRepository tweetRepository;
    private User user1, user2;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Consumer<Integer, String> consumer;

    @BeforeAll
    static void initAll() {
        mongoDBContainer.start();
    }

    @BeforeEach
    void setup() {

        user1 = UserUtil.returnUserOne();
        user1.setUserName("user123");
        user2 = UserUtil.returnUserTwo();
        user2.setUserName("user456");
        userRepository.deleteAll();

        Map<String, Object> configs = new HashMap<>(
                KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer())
                .createConsumer();
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @Test
    void registerTest() throws Exception {

        // when
        ResultActions response = mockMvc.perform(post(BASE_URI + "/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString((modelMapper.map(user1, UserDto.class)))));

        // then
        response.andExpect(status().isCreated()).andDo(print())
                .andExpect(jsonPath("$.userName", is(user1.getUserName())));
    }

    @Test
    void registerTest4xx() throws Exception {

        // given
        userRepository.save(user1);

        // when
        ResultActions response = mockMvc.perform(post(BASE_URI + "/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString((modelMapper.map(user1, UserDto.class)))));

        // then
        response.andExpect(status().is4xxClientError()).andExpect(content().string("User already exists!"))
                .andDo(print());
    }

    @Test
    void loginTest() throws Exception {

        // given
        userRepository.save(user1);

        // when
        ResultActions response = mockMvc.perform(
                get(BASE_URI + "/login").param("username", user1.getUserName()).param("password", user1.getPassword()));

        // then
        response.andExpect(status().isOk())
                .andExpect(content().string("Login successful for user " + user1.getUserName())).andDo(print());
    }

    @Test
    void login4xxTest() throws Exception {

        // when
        ResultActions response = mockMvc.perform(
                get(BASE_URI + "/login").param("username", user1.getUserName()).param("password", user1.getPassword()));

        // then
        response.andExpect(status().is4xxClientError()).andExpect(content().string("Login Failed!")).andDo(print());
    }

    @Test
    void forgotPasswordTest() throws Exception {

        // given
        userRepository.save(user1);

        // when
        ResultActions response = mockMvc.perform(
                get(BASE_URI + "/forgot").param("email", user1.getEmail()).param("newPassword", "newPassword"));

        // then
        response.andExpect(status().isOk())
                .andExpect(content().string("Password successfully changed for user " + user1.getUserName()))
                .andDo(print());
    }

    @Test
    void forgotPassword4xxTest() throws Exception {

        // when
        ResultActions response = mockMvc.perform(
                get(BASE_URI + "/forgot").param("email", user1.getEmail()).param("newPassword", "newPassword"));

        // then
        response.andExpect(status().is4xxClientError()).andExpect(content().string("User does not exist"))
                .andDo(print());
    }

    @Test
    void showAllUsersTest() throws Exception {

        // given
        userRepository.saveAll(List.of(user1, user2));

        // when
        ResultActions response = mockMvc.perform(get(BASE_URI + "/users/all"));

        // then
        response.andExpect(status().isOk()).andExpect(jsonPath("$.size()", is(2))).andDo(print());
    }

    @Test
    void showAllUsers4xxTest() throws Exception {

        // when
        ResultActions response = mockMvc.perform(get(BASE_URI + "/users/all"));

        // then
        response.andExpect(status().is4xxClientError()).andExpect(content().string("No users exist")).andDo(print());
    }

    @Test
    void showUsersContainingUsernameTest() throws Exception {

        // given
        userRepository.saveAll(List.of(user1, user2));

        // when
        ResultActions response = mockMvc.perform(get(BASE_URI + "/users/search").param("username", "user"));

        // then
        response.andExpect(status().isOk()).andExpect(jsonPath("$.size()", is(2))).andDo(print());
    }

    @Test
    void showUsersContainingUsername4xxTest() throws Exception {

        // when
        ResultActions response = mockMvc.perform(get(BASE_URI + "/users/search").param("username", "user"));

        // then
        response.andExpect(status().is4xxClientError()).andExpect(content().string("No Users")).andDo(print());
    }

    @Test
    void showUserTest() throws Exception {

        // given
        user1.setUserName("user1");
        userRepository.save(user1);

        // when
        ResultActions response = mockMvc.perform(get(BASE_URI + "/user/user1"));

        // then
        response.andExpect(status().isOk()).andExpect(jsonPath("$.userName", is(user1.getUserName())));
    }

    @Test
    void showUser4xxTest() throws Exception {

        // when
        ResultActions response = mockMvc.perform(get(BASE_URI + "/user/user1"));

        // then
        response.andExpect(status().is4xxClientError()).andExpect(content().string("No Users")).andDo(print());
    }

    @Test
    @Timeout(5)
    void postTweetEventTest() {

        Tweet tweet = Tweet.builder().content("New Tweet").tag("tag").build();

        TweetEvent tweetEvent = TweetEvent.builder().id(null).tweet(modelMapper.map(tweet, TweetDto.class)).build();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("content-type", String.valueOf(MediaType.APPLICATION_JSON));

        HttpEntity<TweetEvent> request = new HttpEntity<>(tweetEvent, httpHeaders);

        ResponseEntity<TweetEvent> response = restTemplate.exchange(BASE_URI + "/user1/add", HttpMethod.POST, request,
                TweetEvent.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        ConsumerRecords<Integer, String> consumerRecords = KafkaTestUtils.getRecords(consumer);

        assertThat(consumerRecords.count()).isEqualTo(1);
    }

    @Test
    @Timeout(5)
    void putTweetEventTest() {

        Tweet tweet1 = Tweet.builder().id("Tweet-1").username("user1").tag("tag").content("Test Content")
                .created("2022/07/28 15:09:48").likes(new HashMap<>()).replies(new ArrayList<>()).build();

        tweetRepository.save(tweet1);

        TweetDto tweet = TweetDto.builder().content("Updated Tweet").tag("tag").build();

        TweetEvent tweetEvent = TweetEvent.builder().id(null).tweet(tweet).build();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("content-type", MediaType.APPLICATION_JSON.toString());
        HttpEntity<TweetEvent> request = new HttpEntity<>(tweetEvent, httpHeaders);

        ResponseEntity<TweetEvent> response = restTemplate.exchange(BASE_URI + "/user1/edit/Tweet-1", HttpMethod.PUT,
                request, TweetEvent.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        ConsumerRecords<Integer, String> consumerRecords = KafkaTestUtils.getRecords(consumer);

        assertThat(consumerRecords.count()).isEqualTo(2);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }
}
