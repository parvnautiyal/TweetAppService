package com.iiht.usecase.tweetapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iiht.usecase.tweetapp.domain.ReplyEvent;
import com.iiht.usecase.tweetapp.entity.Reply;
import com.iiht.usecase.tweetapp.entity.Tweet;
import com.iiht.usecase.tweetapp.producer.KafkaEventProducer;
import com.iiht.usecase.tweetapp.service.TweetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

import static com.iiht.usecase.tweetapp.util.Constants.*;

@CrossOrigin
@RestController
@RequestMapping(BASE_URI)
@Slf4j
public class TweetController {

    @Autowired
    private TweetService tweetService;

    @Autowired
    private KafkaEventProducer kafkaEventProducer;

    @GetMapping("/all")
    public ResponseEntity<List<Tweet>> showAllTweets() {
        log.info(IN_REQUEST_LOG, "showAllTweet", "display all the tweets");
        List<Tweet> tweets = tweetService.getAllTweets();
        log.debug(EXITING_RESPONSE_LOG, "showAllTweet", tweets);
        log.info(SUCCESS);
        return new ResponseEntity<>(tweets, HttpStatus.OK);
    }

    @GetMapping("/{username}")
    public ResponseEntity<List<Tweet>> showTweetsOfUser(@PathVariable("username") String username) {
        log.info(IN_REQUEST_LOG, "showTweetsOfUser", "display all the tweets by " + username);
        List<Tweet> tweets = tweetService.getTweetsOfUser(username);
        log.debug(EXITING_RESPONSE_LOG, "showTweetsOfUser", tweets);
        log.info(SUCCESS);
        return new ResponseEntity<>(tweets, HttpStatus.OK);
    }

    @DeleteMapping("/{username}/delete/{id}")
    public ResponseEntity<String> deleteTweet(@PathVariable("username") String username,
            @PathVariable("id") String tweetId) {
        log.info(IN_REQUEST_LOG, "deleteTweet", "delete tweet of " + username + " with id " + tweetId);
        log.debug(EXITING_RESPONSE_LOG, "deleteTweet", "Tweet with id " + tweetId + " deleted");
        log.info(SUCCESS);
        return new ResponseEntity<>(tweetService.deleteTweet(tweetId), HttpStatus.OK);
    }

    @PutMapping("/{username}/like/{id}")
    public ResponseEntity<String> likeTweet(@PathVariable("username") String username,
            @PathVariable("id") String tweetId) {
        log.info(IN_REQUEST_LOG, "likeTweet", "tweet " + tweetId + " liked by user " + username);
        String likeResult = tweetService.likeTweet(username, tweetId);
        log.info(SUCCESS);
        return new ResponseEntity<>(likeResult, HttpStatus.OK);
    }

    @PutMapping("/{username}/dislike/{id}")
    public ResponseEntity<String> dislikeTweet(@PathVariable("username") String username,
            @PathVariable("id") String tweetId) {
        log.info(IN_REQUEST_LOG, "dislikeTweet", "tweet " + tweetId + " disliked by user " + username);
        String dislikeResult = tweetService.dislikeTweet(username, tweetId);
        log.info(SUCCESS);
        return new ResponseEntity<>(dislikeResult, HttpStatus.OK);
    }

    @PostMapping("/{username}/reply/{id}")
    public ResponseEntity<String> replyTweet(@PathVariable("username") String username,
            @PathVariable("id") String tweetId, @RequestBody @NotBlank @Size(max = 50) String replyContent)
            throws JsonProcessingException {
        log.info(IN_REQUEST_LOG, "replyTweet", "user " + username + " replied to tweed " + tweetId);
        ReplyEvent replyEvent = new ReplyEvent();
        Reply reply = Reply.builder().tweetId(tweetId).username(username).replyContent(replyContent).build();
        replyEvent.setReply(reply);
        kafkaEventProducer.replyHandler(replyEvent);
        return new ResponseEntity<>("Replied by " + username, HttpStatus.OK);
    }
}
