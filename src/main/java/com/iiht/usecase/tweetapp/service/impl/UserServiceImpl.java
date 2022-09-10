package com.iiht.usecase.tweetapp.service.impl;

import com.iiht.usecase.tweetapp.entity.User;
import com.iiht.usecase.tweetapp.exception.TweetAppException;
import com.iiht.usecase.tweetapp.repository.UserRepository;
import com.iiht.usecase.tweetapp.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.iiht.usecase.tweetapp.util.Constants.*;

@Component
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User registerUser(User user) {
        log.info(IN_REQUEST_LOG, "registerUser", "Service method to register user");
        log.info(VALIDATION);
        if (userRepository.findByUserNameOrEmail(user.getUserName(), user.getEmail()).isPresent())
            throw new TweetAppException(HttpStatus.BAD_REQUEST, "User already exists!");
        else {
            User registeredUser = userRepository.save(user);
            log.debug(EXITING_RESPONSE_LOG, "register", registeredUser);
            log.info(SUCCESS);
            return registeredUser;
        }
    }

    @Override
    public String login(String username, String password) {
        log.info(IN_REQUEST_LOG, "login", "Service method to login");
        log.info(VALIDATION);
        Optional<User> foundUser = userRepository.findByUserNameAndPassword(username, password);
        if (foundUser.isPresent()) {
            log.info(SUCCESS);
            return "Login successful for user " + foundUser.get().getUserName();
        } else
            throw new TweetAppException(HttpStatus.UNAUTHORIZED, "Login Failed!");
    }

    @Override
    public String forgotPassword(String email, String password) {
        log.info(IN_REQUEST_LOG, "forgotPassword", "Service method to reset password");
        log.info(VALIDATION);
        Optional<User> user = userRepository.findByUserNameOrEmail(null, email);
        if (user.isPresent()) {
            user.get().setPassword(password);
            userRepository.save(user.get());
            log.info(SUCCESS);
            return "Password successfully changed for user " + user.get().getUserName();
        } else
            throw new TweetAppException(HttpStatus.BAD_REQUEST, "User does not exist");
    }

    @Override
    public List<User> getAllUsers() {
        log.info(IN_REQUEST_LOG, "getAllUsers", "Service method to get all users");
        log.info(VALIDATION);
        List<User> users = userRepository.findAll();
        if (users.isEmpty())
            throw new TweetAppException(HttpStatus.BAD_REQUEST, "No users exist");
        else {
            log.debug(EXITING_RESPONSE_LOG, "getAllUsers", users);
            log.info(SUCCESS);
            return users;
        }
    }

    @Override
    public List<User> getUserByUsername(String matcher) {
        log.info(IN_REQUEST_LOG, "getUserByUsername", "Service method to get all users matching the given string");
        log.info(VALIDATION);
        List<User> users = userRepository.findUserByUserNameContaining(matcher);
        if (users.isEmpty())
            throw new TweetAppException(HttpStatus.NOT_FOUND, "No Users");
        else {
            log.debug(EXITING_RESPONSE_LOG, "getUserByUsername", users);
            log.info(SUCCESS);
            return users;
        }
    }

    @Override
    public User getUser(String username) {
        log.info(IN_REQUEST_LOG, "getUser", "Service method to get user by username");
        log.info(VALIDATION);
        Optional<User> user = userRepository.findByUserName(username);
        if (user.isEmpty())
            throw new TweetAppException(HttpStatus.NOT_FOUND, "No Users");
        else {
            log.debug(EXITING_RESPONSE_LOG, "getUser", user);
            log.info(SUCCESS);
            return user.get();
        }
    }

    @Override
    public User getUserByEmail(String email) {
        log.info(IN_REQUEST_LOG, "getUserByEmail", "Service method to get a user by email");
        log.info(VALIDATION);
        Optional<User> userOptional = userRepository.findByUserNameOrEmail(null, email);
        User foundUser;
        if (userOptional.isPresent()) {
            foundUser = userOptional.get();
            log.debug(EXITING_RESPONSE_LOG, "getUserByEmail", foundUser);
            log.info(SUCCESS);
            return foundUser;
        } else
            throw new TweetAppException(HttpStatus.BAD_REQUEST, "User does not exist");
    }
}
