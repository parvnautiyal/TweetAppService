package com.iiht.usecase.tweetapp.service;

import com.iiht.usecase.tweetapp.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    User registerUser(User user);

    String login(String username, String password);

    String forgotPassword(String email, String password);

    List<User> getAllUsers();

    List<User> getUserByUsername(String matcher);

    User getUserByEmail(String email);
}
