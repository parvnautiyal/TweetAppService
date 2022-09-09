package com.iiht.usecase.tweetapp.exception;

public class KafkaRuntimeException extends Exception {

    public KafkaRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
