package com.iiht.usecase.tweetapp.controller;

import com.iiht.usecase.tweetapp.entity.Tweet;
import com.iiht.usecase.tweetapp.service.TweetService;
import com.iiht.usecase.tweetapp.util.TweetsUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(TweetController.class)
@AutoConfigureMockMvc
class TweetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TweetService tweetService;

    private Tweet tweet1,tweet2;

    @BeforeEach
    void setup(){

        tweet1 = TweetsUtil.returnTweetOne();
        tweet2 = TweetsUtil.returnTweetTwo();
    }

    @Test
    void showAllTweetsTest() throws Exception {

        //given
        given(tweetService.getAllTweets()).willReturn(List.of(tweet1,tweet2));

        //when
        ResultActions response = mockMvc.perform(get(BASE_URI+"/all"));

        //then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))
                .andDo(print());
    }

    @Test
    void showTweetsOfUserTest() throws Exception {

        //given
        given(tweetService.getTweetsOfUser(any())).willReturn(List.of(tweet1));

        //when
        ResultActions response = mockMvc.perform(get(BASE_URI+"/Test User"));

        //then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.size()",is(1)))
                .andDo(print());
    }

    @Test
    void deleteTweet() throws Exception{

        //given
        given(tweetService.deleteTweet(any())).willReturn("Tweet with id Tweet-1 deleted");

        //when
        ResultActions response = mockMvc.perform(delete(BASE_URI+"/Test User/delete/Tweet-1"));

        //then
        response.andExpect(status().isOk())
                .andExpect(content().string("Tweet with id Tweet-1 deleted"))
                .andDo(print());
    }

    @Test
    void likeTweet() throws Exception{

        //given
        given(tweetService.likeTweet(any(),any())).willReturn("Post liked by user Test User");

        //when
        ResultActions response = mockMvc.perform(put(BASE_URI+"/Test User/like/Tweet-1"));

        //then
        response.andExpect(status().isOk())
                .andExpect(content().string("Post liked by user Test User"))
                .andDo(print());
    }

    @Test
    void replyTweetTest() throws Exception{

        //given
        given(tweetService.replyTweet(any(),any(),any())).willReturn("Post replied by user Test User");

        //when
        ResultActions response = mockMvc.perform(post(BASE_URI+"/Test User/reply/Tweet-1")
                .contentType(MediaType.TEXT_HTML)
                .content("Test Reply"));

        //then
        response.andExpect(status().isOk())
                .andExpect(content().string("Post replied by user Test User"))
                .andDo(print());
    }
}
