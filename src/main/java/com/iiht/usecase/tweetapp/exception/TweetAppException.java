package com.iiht.usecase.tweetapp.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
public class TweetAppException extends RuntimeException {
    private final HttpStatus status;
    private final String data;
}
