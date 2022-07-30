package com.iiht.usecase.tweetapp;

import com.iiht.usecase.tweetapp.entity.Tweet;
import com.iiht.usecase.tweetapp.entity.User;
import com.iiht.usecase.tweetapp.repository.TweetRepository;
import com.iiht.usecase.tweetapp.repository.UserRepository;
import com.iiht.usecase.tweetapp.util.TweetsUtil;
import com.iiht.usecase.tweetapp.util.UserUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static com.iiht.usecase.tweetapp.util.Constants.BASE_URI;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class TweetControllerIntegrationTest {
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

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

    private Tweet tweet1, tweet2;

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
    }

    @Test
    void showAllTest() throws Exception {

        //given
        tweetRepository.saveAll(List.of(tweet1, tweet2));

        //when
        ResultActions response = mockMvc.perform(get(BASE_URI + "/all"));

        //then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))
                .andDo(print());
    }

    @Test
    void showAll4xxTest() throws Exception {

        //when
        ResultActions response = mockMvc.perform(get(BASE_URI + "/all"));

        //then
        response.andExpect(status().is4xxClientError())
                .andExpect(content().string("No tweets found"))
                .andDo(print());
    }

    @Test
    void showTweetsOfUserTest() throws Exception {

        //given
        tweetRepository.saveAll(List.of(tweet1, tweet2));

        //when
        ResultActions response = mockMvc.perform(get(BASE_URI + "/Test User"));

        //then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andDo(print());
    }

    @Test
    void showTweetsOfUser4xxTest() throws Exception {

        //when
        ResultActions response = mockMvc.perform(get(BASE_URI + "/Test User"));

        //then
        response.andExpect(status().is4xxClientError())
                .andExpect(content().string("No tweets by user"))
                .andDo(print());
    }

    @Test
    void deleteTweetTest() throws Exception {

        //given
        tweetRepository.saveAll(List.of(tweet1, tweet2));

        //when
        ResultActions response = mockMvc.perform(delete(BASE_URI+"/"+tweet1.getUsername()+"/delete/"+tweet1.getId()));
        //then
        response.andExpect(status().isOk())
                .andExpect(content().string("Tweet with id "+ tweet1.getId() +" deleted"))
                .andDo(print());
    }

    @Test
    void deleteTweet4xxTest() throws Exception {

        //when
        ResultActions response = mockMvc.perform(delete(BASE_URI+"/"+tweet1.getUsername()+"/delete/"+tweet1.getId()));
        //then
        response.andExpect(status().is4xxClientError())
                .andExpect(content().string("Tweet not found"))
                .andDo(print());
    }

    @Test
    void likeTweetTest() throws Exception {

        //given
        tweetRepository.saveAll(List.of(tweet1, tweet2));
        userRepository.save(user);

        //when
        ResultActions response = mockMvc.perform(put(BASE_URI + "/Test User/like/Tweet-1"));

        //then
        response.andExpect(status().isOk())
                .andExpect(content().string("Post liked by user Test User"))
                .andDo(print());
    }

    @Test
    void likeTweet4xxTest() throws Exception {

        //when
        ResultActions response = mockMvc.perform(put(BASE_URI + "/Test User/like/Tweet-1"));

        //then
        response.andExpect(status().is4xxClientError())
                .andExpect(content().string("Invalid parameters"))
                .andDo(print());
    }

    @Test
    void replyTweetTest() throws Exception{

        //given
        tweetRepository.saveAll(List.of(tweet1, tweet2));
        userRepository.save(user);

        //when
        ResultActions response = mockMvc.perform(post(BASE_URI+"/Test User/reply/Tweet-1")
                .contentType(MediaType.TEXT_HTML)
                .content("Test Reply"));

        //then
        response.andExpect(status().isOk())
                .andExpect(content().string("Post replied by user Test User"))
                .andDo(print());
    }

    @Test
    void replyTweet4xxTest() throws Exception{

        //when
        ResultActions response = mockMvc.perform(post(BASE_URI+"/Test User/reply/Tweet-1")
                .contentType(MediaType.TEXT_HTML)
                .content("Test Reply"));

        //then
        response.andExpect(status().is4xxClientError())
                .andExpect(content().string("Invalid parameters"))
                .andDo(print());
    }
}
