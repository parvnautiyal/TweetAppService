package com.iiht.usecase.tweetapp.repository;

import com.iiht.usecase.tweetapp.entity.Tweet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TweetRepository extends MongoRepository<Tweet, String> {

    List<Tweet> findTweetByUsername(String username);
}
