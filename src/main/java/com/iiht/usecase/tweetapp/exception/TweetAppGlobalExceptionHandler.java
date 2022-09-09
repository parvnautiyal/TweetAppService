package com.iiht.usecase.tweetapp.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.iiht.usecase.tweetapp.util.Constants.ERROR_LOG;

@ControllerAdvice
@Slf4j
public class TweetAppGlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .map(fieldError -> fieldError.getField() + "-" + fieldError.getDefaultMessage()).sorted()
                .collect(Collectors.joining(","));
        log.info(ERROR_LOG, errorMessage);
        log.error(Arrays.toString(ex.getStackTrace()));
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> tweetAppException(TweetAppException exception) {
        log.info(ERROR_LOG, exception.getData());
        log.error(Arrays.toString(exception.getStackTrace()));
        return new ResponseEntity<>(exception.getData(), exception.getStatus());
    }
}
