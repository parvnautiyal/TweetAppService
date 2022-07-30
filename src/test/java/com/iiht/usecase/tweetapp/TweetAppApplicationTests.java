package com.iiht.usecase.tweetapp;

import com.iiht.usecase.tweetapp.controller.TweetController;
import com.iiht.usecase.tweetapp.controller.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableMongoRepositories
class TweetAppApplicationTests {
    @Autowired
    private TweetController tweetController;
    @Autowired
    private UserController userController;

    @Test
    void contextLoads() {
        assertThat(tweetController).isNotNull();
        assertThat(userController).isNotNull();
    }

}
