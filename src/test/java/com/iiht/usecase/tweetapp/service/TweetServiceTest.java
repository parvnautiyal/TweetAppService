package com.iiht.usecase.tweetapp.service;

import com.iiht.usecase.tweetapp.entity.Tweet;
import com.iiht.usecase.tweetapp.entity.User;
import com.iiht.usecase.tweetapp.exception.TweetAppException;
import com.iiht.usecase.tweetapp.repository.TweetRepository;
import com.iiht.usecase.tweetapp.repository.UserRepository;
import com.iiht.usecase.tweetapp.service.impl.TweetServiceImpl;
import com.iiht.usecase.tweetapp.util.TweetsUtil;
import com.iiht.usecase.tweetapp.util.UserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TweetServiceTest {

    @Mock
    private TweetRepository tweetRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TweetServiceImpl tweetService;

    private Tweet tweet1, tweet2;
    private User user;

    @BeforeEach
    void setup() {
        tweet1 = TweetsUtil.returnTweetOne();
        tweet2 = TweetsUtil.returnTweetTwo();
        user = UserUtil.returnUserOne();
    }

    @Test
    void getAllTweetsTest() {

        // given
        given(tweetRepository.findAll()).willReturn(List.of(tweet1, tweet2));

        // when
        List<Tweet> tweets = tweetService.getAllTweets();

        // then
        assertThat(tweets).isNotNull().hasSize(2).containsExactlyInAnyOrderElementsOf(List.of(tweet1, tweet2));
        verify(tweetRepository, times(1)).findAll();
    }

    @Test
    void getAllTweetsExceptionTest() {

        // given
        given(tweetRepository.findAll()).willReturn(Collections.emptyList());

        // when
        assertThrows(TweetAppException.class, () -> tweetService.getAllTweets());

        // then
        verify(tweetRepository, times(1)).findAll();
    }

    @Test
    void getTweetsOfUserTest() {

        // given
        given(tweetRepository.findTweetByUsername(any(String.class))).willReturn(List.of(tweet1));

        // when
        List<Tweet> tweets = tweetService.getTweetsOfUser("Test User");

        // then
        assertThat(tweets).isNotNull().hasSize(1).containsExactlyInAnyOrderElementsOf(List.of(tweet1));
        verify(tweetRepository, times(1)).findTweetByUsername(any(String.class));
    }

    @Test
    void getTweetsOfUserExceptionTest() {

        // given
        given(tweetRepository.findTweetByUsername("Test User")).willReturn(Collections.emptyList());

        // when
        assertThrows(TweetAppException.class, () -> tweetService.getTweetsOfUser("Test User"));

        // then
        verify(tweetRepository, times(1)).findTweetByUsername(any(String.class));
    }

    @Test
    void deleteTweetTest() {

        // given
        given(tweetRepository.findById("Tweet-1")).willReturn(Optional.of(tweet1));
        willDoNothing().given(tweetRepository).deleteById("Tweet-1");

        // when
        tweetService.deleteTweet("Tweet-1");

        // then
        verify(tweetRepository, times(1)).deleteById("Tweet-1");
    }

    @Test
    void deleteTweetExceptionTest() {

        // given
        given(tweetRepository.findById("Tweet-1")).willReturn(Optional.empty());

        // when
        assertThrows(TweetAppException.class, () -> tweetService.deleteTweet("Tweet-1"));

        // then
        verify(tweetRepository, times(0)).deleteById("Tweet-1");
    }

    @Test
    void likeTweetTest() {

        // given
        given(tweetRepository.findById("Tweet-1")).willReturn(Optional.of(tweet1));
        given(userRepository.findByUserName("Test User")).willReturn(Optional.of(user));

        // when
        String actual = tweetService.likeTweet("Test User", "Tweet-1");

        // then
        assertThat(actual).isEqualTo("Post liked by user Test User");
        assertThat(tweet1.getLikes()).containsEntry("Test User", "Tweet-1");
    }

    @Test
    void likeTweetException() {

        // given
        given(tweetRepository.findById("Tweet-1")).willReturn(Optional.empty());

        // when
        assertThrows(TweetAppException.class, () -> tweetService.likeTweet("Test User", "Tweet-1"));

        // then
        verify(tweetRepository, times(0)).save(any());
    }
}
