package com.iiht.usecase.tweetapp.util;

import com.iiht.usecase.tweetapp.entity.User;

public final class UserUtil {

    private UserUtil(){}

    public static User returnUserOne(){

        return User.builder()
                .userName("Test User")
                .firstName("Test")
                .lastName("User")
                .dob("2000-12-31")
                .email("email@test.com")
                .password("password")
                .gender("male")
                .build();
    }

    public static User returnUserTwo(){

        return User.builder()
                .userName("Test User 2")
                .firstName("Test 2")
                .lastName("User 2")
                .dob("2000-02-22")
                .email("email2@test2.com")
                .password("password2")
                .gender("female")
                .build();
    }
}
