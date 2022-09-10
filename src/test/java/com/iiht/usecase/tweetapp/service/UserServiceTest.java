package com.iiht.usecase.tweetapp.service;

import com.iiht.usecase.tweetapp.entity.User;
import com.iiht.usecase.tweetapp.exception.TweetAppException;
import com.iiht.usecase.tweetapp.repository.UserRepository;
import com.iiht.usecase.tweetapp.service.impl.UserServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user1, user2;

    @BeforeEach
    void setup() {
        user1 = UserUtil.returnUserOne();
        user2 = UserUtil.returnUserTwo();
    }

    @Test
    void registerTest() {

        // given
        given(userRepository.findByUserNameOrEmail(user1.getUserName(), user1.getEmail())).willReturn(Optional.empty());
        given(userRepository.save(user1)).willReturn(user1);

        // when
        User savedUser = userService.registerUser(user1);

        // then
        assertThat(savedUser).isNotNull().hasToString(user1.toString());
    }

    @Test
    void registerExceptionTest() {

        // given
        given(userRepository.findByUserNameOrEmail(user1.getUserName(), user1.getEmail()))
                .willReturn(Optional.of(user1));

        // when
        assertThrows(TweetAppException.class, () -> userService.registerUser(user1));

        // then
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginAndForgotPasswordTest() {

        // given
        given(userRepository.findByUserNameAndPassword(user1.getUserName(), user1.getPassword()))
                .willReturn(Optional.of(user1));
        given(userRepository.findByUserNameOrEmail(null, user1.getEmail())).willReturn(Optional.of(user1));

        // when
        String actualLogin = userService.login(user1.getUserName(), user1.getPassword());
        String actualForgotPassword = userService.forgotPassword(user1.getEmail(), "New Password");

        // then
        assertThat(actualLogin).isEqualTo("Login successful for user " + user1.getUserName());
        assertThat(actualForgotPassword).isEqualTo("Password successfully changed for user " + user1.getUserName());
        assertThat(user1.getPassword()).isEqualTo("New Password");
    }

    @Test
    void loginAndForgotPasswordExceptionTest() {

        // given
        given(userRepository.findByUserNameAndPassword(user1.getUserName(), user1.getPassword()))
                .willReturn(Optional.empty());
        given(userRepository.findByUserNameOrEmail(null, user1.getEmail())).willReturn(Optional.empty());

        // when
        assertThrows(TweetAppException.class, () -> userService.login("Test User", "password"));
        assertThrows(TweetAppException.class, () -> userService.forgotPassword("email@test.com", "New Password"));

        // then
    }

    @Test
    void getAllUsersTest() {

        // given
        given(userRepository.findAll()).willReturn(List.of(user1, user2));

        // when
        List<User> users = userService.getAllUsers();

        // then
        assertThat(users).isNotNull().hasSize(2).containsExactlyInAnyOrderElementsOf(List.of(user1, user2));
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsersExceptionTest() {

        // given
        given(userRepository.findAll()).willReturn(Collections.emptyList());

        // when
        assertThrows(TweetAppException.class, () -> userService.getAllUsers());

        // then
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserByUsernameTest() {

        // given
        given(userRepository.findUserByUserNameContaining("Test")).willReturn(List.of(user1, user2));

        // then
        List<User> users = userService.getUserByUsername("Test");

        // then
        assertThat(users).isNotNull().hasSize(2).containsExactlyInAnyOrderElementsOf(List.of(user1, user2));
        verify(userRepository, times(1)).findUserByUserNameContaining(any());
    }

    @Test
    void getUserByUsernameExceptionTest() {

        // given
        given(userRepository.findUserByUserNameContaining("Test")).willReturn(Collections.emptyList());

        // then
        assertThrows(TweetAppException.class, () -> userService.getUserByUsername("Test"));

        // then
        verify(userRepository, times(1)).findUserByUserNameContaining(any());
    }

    @Test
    void getUserTest() {

        // given
        given(userRepository.findByUserName("Test")).willReturn(Optional.of(user1));

        // then
        User user = userService.getUser("Test");

        // then
        assertThat(user).isNotNull();
        assertThat(user).isEqualTo(user1);
        verify(userRepository, times(1)).findByUserName(any());
    }

    @Test
    void getUserExceptionTest() {

        // given
        given(userRepository.findByUserName("Test")).willReturn(Optional.empty());

        // then
        assertThrows(TweetAppException.class, () -> userService.getUser("Test"));

        // then
        verify(userRepository, times(1)).findByUserName(any());
    }

    @Test
    void getUserByEmailTest() {

        // given
        given(userRepository.findByUserNameOrEmail(null, user1.getEmail())).willReturn(Optional.of(user1));

        // when
        User user = userService.getUserByEmail(user1.getEmail());

        // then
        assertThat(user).isNotNull().isEqualTo(user1);
    }

    @Test
    void getUserByEmailExceptionTest() {

        // given
        given(userRepository.findByUserNameOrEmail(null, user1.getEmail())).willReturn(Optional.empty());

        // when
        assertThrows(TweetAppException.class, () -> userService.getUserByEmail("email@test.com"));
    }
}
