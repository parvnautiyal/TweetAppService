package com.iiht.usecase.tweetapp;

import com.iiht.usecase.tweetapp.entity.Tweet;
import com.iiht.usecase.tweetapp.entity.User;
import com.iiht.usecase.tweetapp.repository.TweetRepository;
import com.iiht.usecase.tweetapp.repository.UserRepository;
import com.iiht.usecase.tweetapp.util.TweetsUtil;
import com.iiht.usecase.tweetapp.util.UserUtil;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.iiht.usecase.tweetapp.util.Constants.BASE_URI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(topics = "reply-event", partitions = 4)
@TestPropertySource(properties = { "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}" })
@Testcontainers
class TweetControllerIntegrationTest {
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(
            DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    private Tweet tweet1, tweet2;

    private Consumer<Integer, String> consumer;

    private User user;

    @BeforeAll
    static void initAll() {
        mongoDBContainer.start();
    }

    @BeforeEach
    void setup() {
        tweet1 = TweetsUtil.returnTweetOne();
        tweet2 = TweetsUtil.returnTweetTwo();
        user = UserUtil.returnUserOne();
        tweetRepository.deleteAll();
        userRepository.deleteAll();

        Map<String, Object> configs = new HashMap<>(
                KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer())
                .createConsumer();
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @Test
    void showAllTest() throws Exception {

        // given
        tweetRepository.saveAll(List.of(tweet1, tweet2));

        // when
        ResultActions response = mockMvc.perform(get(BASE_URI + "/all"));

        // then
        response.andExpect(status().isOk()).andExpect(jsonPath("$.size()", is(2))).andDo(print());
    }

    @Test
    void showAll4xxTest() throws Exception {

        // when
        ResultActions response = mockMvc.perform(get(BASE_URI + "/all"));

        // then
        response.andExpect(status().is4xxClientError()).andExpect(content().string("No tweets found")).andDo(print());
    }

    @Test
    void showTweetsOfUserTest() throws Exception {

        // given
        tweetRepository.saveAll(List.of(tweet1, tweet2));

        // when
        ResultActions response = mockMvc.perform(get(BASE_URI + "/Test User"));

        // then
        response.andExpect(status().isOk()).andExpect(jsonPath("$.size()", is(1))).andDo(print());
    }

    @Test
    void showTweetsOfUser4xxTest() throws Exception {

        // when
        ResultActions response = mockMvc.perform(get(BASE_URI + "/Test User"));

        // then
        response.andExpect(status().is4xxClientError()).andExpect(content().string("No tweets by user")).andDo(print());
    }

    @Test
    void deleteTweetTest() throws Exception {

        // given
        tweetRepository.saveAll(List.of(tweet1, tweet2));

        // when
        ResultActions response = mockMvc
                .perform(delete(BASE_URI + "/" + tweet1.getUsername() + "/delete/" + tweet1.getId()));
        // then
        response.andExpect(status().isOk()).andExpect(content().string("Tweet with id " + tweet1.getId() + " deleted"))
                .andDo(print());
    }

    @Test
    void deleteTweet4xxTest() throws Exception {

        // when
        ResultActions response = mockMvc
                .perform(delete(BASE_URI + "/" + tweet1.getUsername() + "/delete/" + tweet1.getId()));
        // then
        response.andExpect(status().is4xxClientError()).andExpect(content().string("Tweet not found")).andDo(print());
    }

    @Test
    void likeTweetTest() throws Exception {

        // given
        tweetRepository.saveAll(List.of(tweet1, tweet2));
        userRepository.save(user);

        // when
        ResultActions response = mockMvc.perform(put(BASE_URI + "/Test User/like/Tweet-1"));

        // then
        response.andExpect(status().isOk()).andExpect(content().string("Post liked by user Test User")).andDo(print());
    }

    @Test
    void likeTweet4xxTest() throws Exception {

        // when
        ResultActions response = mockMvc.perform(put(BASE_URI + "/Test User/like/Tweet-1"));

        // then
        response.andExpect(status().is4xxClientError()).andExpect(content().string("Invalid parameters"))
                .andDo(print());
    }

    @Test
    void postReplyEventTest() {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("content-type", String.valueOf(MediaType.TEXT_PLAIN));

        HttpEntity<String> request = new HttpEntity<>("reply", httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(BASE_URI + "user1/reply/Tweet-1", HttpMethod.POST,
                request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertEquals("Replied by user1", response.getBody());

        ConsumerRecords<Integer, String> consumerRecords = KafkaTestUtils.getRecords(consumer);

        assertThat(consumerRecords.count()).isEqualTo(1);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }
}
