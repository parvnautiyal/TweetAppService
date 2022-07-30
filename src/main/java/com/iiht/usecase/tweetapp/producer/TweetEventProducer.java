package com.iiht.usecase.tweetapp.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iiht.usecase.tweetapp.domain.TweetEvent;
import com.iiht.usecase.tweetapp.exception.KafkaRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;

@Component
@Slf4j
public class TweetEventProducer {

    @Autowired
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    private static final String TOPIC = "tweet-event";

    public ListenableFuture<SendResult<Integer, String>> sendTweetEvent(TweetEvent tweetEvent) throws JsonProcessingException {
        Integer key = tweetEvent.getId();
        String value = objectMapper.writeValueAsString(tweetEvent);

        log.info("Message prepared -> {}",value);

        ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value);

        log.info("Invoking send method to stream data to kafka");

        ListenableFuture<SendResult<Integer, String>> listenableFuture =
                kafkaTemplate.send(producerRecord);
        listenableFuture.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, value, result);
            }
        });
        return listenableFuture;
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Message sent successfully for the key: {} and the value is {}, and partition is {}", key, value, result.getRecordMetadata().partition());
    }

    private void handleFailure(Throwable throwable) {
        try {
            throw new KafkaRuntimeException("Exception occured",throwable);
        } catch (KafkaRuntimeException exception) {
            log.error("Error in OnFailure: {}", throwable.getMessage());
        }
    }

    private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value) {
        log.info("Creating a producer record");
        List<Header> recordHeaders = List.of(new RecordHeader("event-source","tweet".getBytes()));
        return new ProducerRecord<>(TOPIC, null, key, value, recordHeaders);
    }
}
