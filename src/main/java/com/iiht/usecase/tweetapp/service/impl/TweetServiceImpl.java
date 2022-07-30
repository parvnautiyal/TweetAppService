package com.iiht.usecase.tweetapp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iiht.usecase.tweetapp.domain.TweetEvent;
import com.iiht.usecase.tweetapp.entity.Tweet;
import com.iiht.usecase.tweetapp.entity.User;
import com.iiht.usecase.tweetapp.entity.dto.TweetDto;
import com.iiht.usecase.tweetapp.exception.TweetAppException;
import com.iiht.usecase.tweetapp.repository.TweetRepository;
import com.iiht.usecase.tweetapp.repository.UserRepository;
import com.iiht.usecase.tweetapp.service.TweetService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.iiht.usecase.tweetapp.util.Constants.*;

@Component
@Slf4j
public class TweetServiceImpl implements TweetService {

    @Autowired
    private TweetRepository tweetRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private ObjectMapper objectMapper;
    @Override
    public List<Tweet> getAllTweets() {
        log.info(IN_REQUEST_LOG, "getAllTweets", "Service method to get all tweets");
        log.info(VALIDATION);
        List<Tweet> foundTweets = tweetRepository.findAll();
        if (foundTweets.isEmpty())
            throw new TweetAppException(HttpStatus.NOT_FOUND, "No tweets found");
        else {
            log.debug(EXITING_RESPONSE_LOG, "getAllTweets", foundTweets);
            log.info(SUCCESS);
            return foundTweets;
        }
    }
    @Override
    public List<Tweet> getTweetsOfUser(String username) {
        log.info(IN_REQUEST_LOG, "getTweetsOfUser", "Service method to get all tweets of a user");
        log.info(VALIDATION);
        List<Tweet> foundTweets = tweetRepository.findTweetByUsername(username);
        if (foundTweets.isEmpty())
            throw new TweetAppException(HttpStatus.NOT_FOUND, "No tweets by user");
        else {
            log.debug(EXITING_RESPONSE_LOG, "getTweetsOfUser", foundTweets);
            log.info(SUCCESS);
            return foundTweets;
        }
    }
    @Override
    public String deleteTweet(String id) {
        log.info(IN_REQUEST_LOG, "deleteTweet", "Service method to delete a tweet by id");
        log.info(VALIDATION);
        Optional<Tweet> tweet = tweetRepository.findById(id);
        if (tweet.isPresent()) {
            tweetRepository.deleteById(id);
            log.debug(EXITING_RESPONSE_LOG, "deleteTweet", SUCCESS);
            return "Tweet with id " + id + " deleted";
        } else throw new TweetAppException(HttpStatus.NOT_FOUND, "Tweet not found");
    }
    @Override
    public String likeTweet(String username, String tweetId) {
        log.info(IN_REQUEST_LOG, "likeTweet", "Service method to like a tweet");
        log.info(VALIDATION);
        Optional<Tweet> tweetOptional = tweetRepository.findById(tweetId);
        Optional<User> userOptional = userRepository.findByUserName(username);
        if (tweetOptional.isEmpty() || userOptional.isEmpty())
            throw new TweetAppException(HttpStatus.BAD_REQUEST, "Invalid parameters");
        else {
            tweetOptional.ifPresent(tweet -> {
                Map<String, String> likeMap;
                if (tweet.getLikes() == null) {
                    likeMap = new HashMap<>();
                } else {
                    likeMap = new HashMap<>(tweet.getLikes());
                }
                likeMap.put(username, tweetId);
                tweet.setLikes(likeMap);
                tweetRepository.save(tweet);
            });
            log.debug(EXITING_RESPONSE_LOG, "likeTweet", "liked by " + username);
            log.info(SUCCESS);
            return "Post liked by user " + username;
        }
    }
    @Override
    public String replyTweet(String username, String tweetId, String reply) {
        log.info(IN_REQUEST_LOG, "replyTweet", "Service method to reply a tweet");
        log.info(VALIDATION);
        Optional<Tweet> tweetOptional = tweetRepository.findById(tweetId);
        Optional<User> userOptional = userRepository.findByUserName(username);
        if (tweetOptional.isEmpty() || userOptional.isEmpty())
            throw new TweetAppException(HttpStatus.BAD_REQUEST, "Invalid parameters");
        else {
            tweetOptional.ifPresent(tweet -> {
                Map<String, List<String>> replyMap;
                List<String> replyList;
                if (tweet.getReplies() == null) {
                    replyMap = new HashMap<>();
                    replyList = new ArrayList<>();
                } else {
                    replyMap = new HashMap<>(tweet.getReplies());
                    if(!replyMap.containsKey(username))
                        replyList = new ArrayList<>();
                    else replyList = replyMap.get(username);
                }
                replyList.add(reply);
                replyMap.put(username, replyList);
                tweet.setReplies(replyMap);
                tweetRepository.save(tweet);
            });
            log.debug(EXITING_RESPONSE_LOG, "replyTweet", "replied by " + username);
            log.info(SUCCESS);
            return "Post replied by user " + username;
        }
    }
    @Override
    public void processTweetEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        TweetEvent tweetEvent = objectMapper.readValue(consumerRecord.value(), TweetEvent.class);
        switch (tweetEvent.getTweetEventType()) {
            case POST -> save(tweetEvent);
            case UPDATE -> update(tweetEvent);
            default -> log.info("invalid library event type");
        }
    }
    private void update(TweetEvent tweetEvent) {
        Optional<Tweet> tweet = tweetRepository.findById(tweetEvent.getTweet().getId());
        if (tweet.isPresent()) {
            tweet.get().setContent(tweetEvent.getTweet().getContent());
            log.info("Record updating");
            tweetRepository.save(tweet.get());
            log.info(SUCCESS);
        } else throw new IllegalArgumentException("not a valid tweet event");
    }
    private void save(TweetEvent tweetEvent) {
        TweetDto tweet = TweetDto.builder()
                .content(tweetEvent.getTweet().getContent())
                .created(tweetEvent.getTweet().getCreated())
                .username(tweetEvent.getTweet().getUsername())
                .build();
        log.info("Record saving");
        tweetRepository.save(modelMapper.map(tweet, Tweet.class));
        log.info(SUCCESS);
    }
}