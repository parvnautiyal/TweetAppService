package com.iiht.usecase.tweetapp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iiht.usecase.tweetapp.domain.TweetEvent;
import com.iiht.usecase.tweetapp.entity.User;
import com.iiht.usecase.tweetapp.entity.dto.UserDto;
import com.iiht.usecase.tweetapp.producer.KafkaEventProducer;
import com.iiht.usecase.tweetapp.service.UserService;
import com.iiht.usecase.tweetapp.util.TweetEventType;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.iiht.usecase.tweetapp.util.Constants.*;

@CrossOrigin
@RestController
@RequestMapping(BASE_URI)
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private KafkaEventProducer kafkaEventProducer;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody @Valid UserDto user) {
        log.info(IN_REQUEST_LOG, "register", "Registering new user");
        User newUser = userService.registerUser(modelMapper.map(user, User.class));
        log.debug(EXITING_RESPONSE_LOG, "register", "registered user: " + newUser);
        log.info(SUCCESS);
        return new ResponseEntity<>(modelMapper.map(newUser, UserDto.class), HttpStatus.CREATED);
    }

    @GetMapping("/login")
    public ResponseEntity<String> login(@RequestParam("username") String username,
            @RequestParam("password") String password) {
        log.info(IN_REQUEST_LOG, "login", "Attempting to login");
        String loginResult = userService.login(username, password);
        log.info(SUCCESS);
        return new ResponseEntity<>(loginResult, HttpStatus.OK);
    }

    @GetMapping("/forgot")
    public ResponseEntity<String> forgotPassword(@RequestParam("newPassword") String password,
            @RequestParam("email") String email) {
        log.info(IN_REQUEST_LOG, "forgotPassword", "User requesting a password change");
        String forgotPasswordResult = userService.forgotPassword(email, password);
        log.info(SUCCESS);
        return new ResponseEntity<>(forgotPasswordResult, HttpStatus.OK);
    }

    @GetMapping("/users/all")
    public ResponseEntity<List<User>> showAllUsers() {
        log.info(IN_REQUEST_LOG, "showAllUsers", "show all the users");
        List<User> users = userService.getAllUsers();
        log.debug(EXITING_RESPONSE_LOG, "showAllUsers", users);
        log.info(SUCCESS);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<User>> showUsersContainingUsername(@RequestParam("username") String username) {
        log.info(IN_REQUEST_LOG, "showUsersContainingUsername", "Getting all users which contain the string");
        List<User> users = userService.getUserByUsername(username);
        log.debug(EXITING_RESPONSE_LOG, "showUsersContainingUsername", users);
        log.info(SUCCESS);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<User> showUser(@PathVariable("username") String username) {
        log.info(IN_REQUEST_LOG, "showUser", "Getting the user with username");
        User user = userService.getUser(username);
        log.debug(EXITING_RESPONSE_LOG, "showUser", user);
        log.info(SUCCESS);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/{username}/add")
    public ResponseEntity<TweetEvent> postTweetEvent(@PathVariable("username") String username,
            @RequestBody @Valid TweetEvent tweetEvent) throws JsonProcessingException {
        log.info(IN_REQUEST_LOG, "postTweetEvent", "Building a new tweet to post into event streaming platform");
        tweetEvent.setTweetEventType(TweetEventType.POST);
        tweetEvent.getTweet().setUsername(username);
        tweetEvent.getTweet()
                .setCreated(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()));
        tweetEvent.getTweet().setLikes(new HashMap<>());
        tweetEvent.getTweet().setReplies(new ArrayList<>());
        log.debug(IN_REQUEST_LOG, "postTweetEvent", "tweet build completed, now posting, tweet " + tweetEvent);
        kafkaEventProducer.tweetHandler(tweetEvent);
        log.info(SUCCESS);
        return ResponseEntity.status(HttpStatus.CREATED).body(tweetEvent);
    }

    @PutMapping("/{username}/edit/{tweetId}")
    public ResponseEntity<TweetEvent> putTweetEvent(@PathVariable("username") String username,
            @PathVariable("tweetId") String tweetId, @RequestBody @Valid TweetEvent tweetEvent)
            throws JsonProcessingException {
        log.info(IN_REQUEST_LOG, "putTweetEvent", "Sending tweetEvent to update a tweet");
        tweetEvent.getTweet().setId(tweetId);
        tweetEvent.setTweetEventType(TweetEventType.UPDATE);
        tweetEvent.getTweet()
                .setCreated(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()));
        log.debug(IN_REQUEST_LOG, "putTweetEvent", "tweet build completed, now posting, tweet " + tweetEvent);
        kafkaEventProducer.tweetHandler(tweetEvent);
        log.info(SUCCESS);
        return ResponseEntity.status(HttpStatus.OK).body(tweetEvent);
    }
}
