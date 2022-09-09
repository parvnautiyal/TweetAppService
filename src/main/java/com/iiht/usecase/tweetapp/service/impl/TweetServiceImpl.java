package com.iiht.usecase.tweetapp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iiht.usecase.tweetapp.domain.ReplyEvent;
import com.iiht.usecase.tweetapp.domain.TweetEvent;
import com.iiht.usecase.tweetapp.entity.Reply;
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
        } else
            throw new TweetAppException(HttpStatus.NOT_FOUND, "Tweet not found");
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
    public String dislikeTweet(String username, String tweetId) {
        log.info(IN_REQUEST_LOG, "dislikeTweet", "Service method to dislike a tweet");
        log.info(VALIDATION);
        Optional<Tweet> tweetOptional = tweetRepository.findById(tweetId);
        Optional<User> userOptional = userRepository.findByUserName(username);
        if (tweetOptional.isEmpty() || userOptional.isEmpty())
            throw new TweetAppException(HttpStatus.BAD_REQUEST, "Invalid parameters");
        else {
            tweetOptional.ifPresent(tweet -> {
                Map<String, String> likeMap = tweet.getLikes();
                likeMap.remove(username);
                tweet.setLikes(likeMap);
                tweetRepository.save(tweet);
            });
            log.debug(EXITING_RESPONSE_LOG, "dislikeTweet", "disliked by " + username);
            log.info(SUCCESS);
            return "Post disliked by user " + username;
        }
    }

    @Override
    public void processTweetEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        TweetEvent tweetEvent = objectMapper.readValue(consumerRecord.value(), TweetEvent.class);
        switch (tweetEvent.getTweetEventType()) {
        case POST -> save(tweetEvent);
        case UPDATE -> update(tweetEvent);
        default -> log.info("invalid tweet event type");
        }
    }

    @Override
    public void processReplyEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        ReplyEvent replyEvent = objectMapper.readValue(consumerRecord.value(), ReplyEvent.class);
        reply(replyEvent);
    }

    private void reply(ReplyEvent replyEvent) {
        tweetRepository.findById(replyEvent.getReply().getTweetId()).ifPresentOrElse(tweet -> {
            List<Reply> replies;
            if (tweet.getReplies() == null) {
                replies = new ArrayList<>();
            } else {
                replies = new ArrayList<>(tweet.getReplies());
            }
            replies.add(replyEvent.getReply());
            tweet.setReplies(replies);
            tweetRepository.save(tweet);
        }, () -> {
            throw new IllegalArgumentException("not a valid reply event");
        });
    }

    private void update(TweetEvent tweetEvent) {
        Optional<Tweet> tweet = tweetRepository.findById(tweetEvent.getTweet().getId());
        if (tweet.isPresent()) {
            tweet.get().setContent(tweetEvent.getTweet().getContent());
            log.info("Record updating");
            tweetRepository.save(tweet.get());
            log.info(SUCCESS);
        } else
            throw new IllegalArgumentException("not a valid tweet event");
    }

    private void save(TweetEvent tweetEvent) {
        TweetDto tweet = TweetDto.builder().content(tweetEvent.getTweet().getContent())
                .created(tweetEvent.getTweet().getCreated()).username(tweetEvent.getTweet().getUsername())
                .tag(tweetEvent.getTweet().getTag().replaceAll("\\s", "")).build();
        log.info("Record saving");
        tweetRepository.save(modelMapper.map(tweet, Tweet.class));
        log.info(SUCCESS);
    }
}