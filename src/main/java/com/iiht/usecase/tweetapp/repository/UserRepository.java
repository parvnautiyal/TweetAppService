package com.iiht.usecase.tweetapp.repository;

import com.iiht.usecase.tweetapp.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUserNameAndPassword(String email, String password);
    Optional<User> findByUserName(String username);

    Optional<User> findByUserNameOrEmail(String username, String email);
    List<User> findUserByUserNameContaining(String matcher);
}
