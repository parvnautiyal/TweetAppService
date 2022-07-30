package com.iiht.usecase.tweetapp.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iiht.usecase.tweetapp.service.TweetService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.iiht.usecase.tweetapp.util.Constants.IN_REQUEST_LOG;

@Component
@Slf4j
public class TweetEventConsumer {

    @Autowired
    private TweetService tweetService;

    @KafkaListener(topics = {"tweet-event"})
    public void onMessage(ConsumerRecord<Integer,String> consumerRecord) throws JsonProcessingException {
        log.info(IN_REQUEST_LOG,"onMessage","Consuming message");
        tweetService.processTweetEvent(consumerRecord);
        log.info("Message consumed Successfully");
    }
}
