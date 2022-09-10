package com.iiht.usecase.tweetapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iiht.usecase.tweetapp.domain.TweetEvent;
import com.iiht.usecase.tweetapp.entity.Tweet;
import com.iiht.usecase.tweetapp.entity.User;
import com.iiht.usecase.tweetapp.entity.dto.TweetDto;
import com.iiht.usecase.tweetapp.entity.dto.UserDto;
import com.iiht.usecase.tweetapp.producer.KafkaEventProducer;
import com.iiht.usecase.tweetapp.service.UserService;
import com.iiht.usecase.tweetapp.util.TweetsUtil;
import com.iiht.usecase.tweetapp.util.UserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.iiht.usecase.tweetapp.util.Constants.BASE_URI;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelMapper modelMapper;

    @MockBean
    private KafkaEventProducer kafkaEventProducer;

    @MockBean
    private UserService userService;

    private User user1, user2;

    private Tweet tweet1;

    @BeforeEach
    void setup() {

        user1 = UserUtil.returnUserOne();
        user1.setUserName("user123");
        user2 = UserUtil.returnUserTwo();
        user2.setUserName("user456");
        tweet1 = TweetsUtil.returnTweetOne();
    }

    @Test
    void registerTest() throws Exception {

        // given
        given(userService.registerUser(any(User.class))).willReturn(user1);

        // when
        ResultActions response = mockMvc.perform(post(BASE_URI + "/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString((modelMapper.map(user1, UserDto.class)))));

        // then
        response.andExpect(status().isCreated()).andDo(print())
                .andExpect(jsonPath("$.userName", is(user1.getUserName())));
    }

    @Test
    void loginTest() throws Exception {

        // given
        given(userService.login(any(String.class), any(String.class))).willReturn("Login successful for user user123");

        // when
        ResultActions response = mockMvc
                .perform(get(BASE_URI + "/login").param("username", "user123").param("password", "password"));

        // then
        response.andExpect(status().isOk()).andExpect(content().string("Login successful for user user123"))
                .andDo(print());
    }

    @Test
    void forgotPasswordTest() throws Exception {

        // given
        given(userService.forgotPassword(any(String.class), any(String.class)))
                .willReturn("Password successfully changed for user user123");

        // when
        ResultActions response = mockMvc.perform(
                get(BASE_URI + "/forgot").param("email", "email@test.com").param("newPassword", "newPassword"));

        // then
        response.andExpect(status().isOk())
                .andExpect(content().string("Password successfully changed for user user123")).andDo(print());
    }

    @Test
    void showAllUsersTest() throws Exception {

        // given
        given(userService.getAllUsers()).willReturn(List.of(user1, user2));

        // when
        ResultActions response = mockMvc.perform(get(BASE_URI + "/users/all"));

        // then
        response.andExpect(status().isOk()).andExpect(jsonPath("$.size()", is(2))).andDo(print());
    }

    @Test
    void showUsersContainingUsernameTest() throws Exception {

        // given
        given(userService.getUserByUsername("user")).willReturn(List.of(user1, user2));

        // when
        ResultActions response = mockMvc.perform(get(BASE_URI + "/users/search").param("username", "user"));

        // then
        response.andExpect(status().isOk()).andExpect(jsonPath("$.size()", is(2))).andDo(print());
    }

    @Test
    void showUserTest() throws Exception {

        // given
        given(userService.getUser("user")).willReturn(user1);

        // when
        ResultActions response = mockMvc.perform(get(BASE_URI + "/user/user"));

        // then
        response.andExpect(status().isOk()).andExpect(jsonPath("$.userName", is(user1.getUserName())));
    }

    @Test
    void postTweetEventTest() throws Exception {

        // given
        TweetEvent tweetEvent = TweetEvent.builder().id(null).tweet(modelMapper.map(tweet1, TweetDto.class)).build();

        given(kafkaEventProducer.tweetHandler(isA(TweetEvent.class))).willReturn(null);

        // when
        ResultActions response = mockMvc.perform(post(BASE_URI + "/user123/add").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tweetEvent)));

        // then
        response.andExpect(status().isCreated()).andDo(print());
    }

    @Test
    void postTweetEvent4xxTest() throws Exception {

        // given
        TweetDto tweet = TweetDto.builder().content(null).tag(null).build();

        TweetEvent tweetEvent = TweetEvent.builder().id(null).tweet(tweet).build();

        given(kafkaEventProducer.tweetHandler(isA(TweetEvent.class))).willReturn(null);

        // when
        String expectedErrorMessage = "tweet.content-tweet cannot be empty";

        ResultActions response = mockMvc.perform(post(BASE_URI + "/user123/add").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tweetEvent)));

        // then
        response.andExpect(status().is4xxClientError()).andExpect(content().string(expectedErrorMessage))
                .andDo(print());
    }

    @Test
    void putTweetEventTest() throws Exception {

        // given
        TweetEvent tweetEvent = TweetEvent.builder().id(null).tweet(modelMapper.map(tweet1, TweetDto.class)).build();

        given(kafkaEventProducer.tweetHandler(isA(TweetEvent.class))).willReturn(null);

        // when
        ResultActions response = mockMvc.perform(put(BASE_URI + "/user123/edit/Tweet-1")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(tweetEvent)));

        // then
        response.andExpect(status().isOk()).andDo(print());
    }

    @Test
    void putTweetEvent4xxTest() throws Exception {

        // given
        TweetDto tweet = TweetDto.builder().content(null).tag(null).build();

        TweetEvent tweetEvent = TweetEvent.builder().id(null).tweet(tweet).build();

        given(kafkaEventProducer.tweetHandler(isA(TweetEvent.class))).willReturn(null);

        // when
        String expectedErrorMessage = "tweet.content-tweet cannot be empty";

        ResultActions response = mockMvc.perform(put(BASE_URI + "/user123/edit/Tweet-1")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(tweetEvent)));

        // then
        response.andExpect(status().is4xxClientError()).andExpect(content().string(expectedErrorMessage))
                .andDo(print());
    }
}
