package com.iiht.usecase.tweetapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iiht.usecase.tweetapp.entity.Tweet;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TweetService {

    List<Tweet> getAllTweets();

    List<Tweet> getTweetsOfUser(String username);

    String deleteTweet(String id);

    String likeTweet(String username, String tweetId);

    void processTweetEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException;

    void processReplyEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException;

    String dislikeTweet(String username, String tweetId);
}
