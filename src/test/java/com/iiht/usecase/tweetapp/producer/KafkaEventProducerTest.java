package com.iiht.usecase.tweetapp.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iiht.usecase.tweetapp.domain.ReplyEvent;
import com.iiht.usecase.tweetapp.domain.TweetEvent;
import com.iiht.usecase.tweetapp.entity.Reply;
import com.iiht.usecase.tweetapp.entity.Tweet;
import com.iiht.usecase.tweetapp.entity.dto.TweetDto;
import com.iiht.usecase.tweetapp.util.TweetsUtil;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaEventProducerTest {

    @Mock
    private KafkaTemplate<Integer, String> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper;

    @Spy
    private ModelMapper modelMapper;

    @InjectMocks
    private KafkaEventProducer kafkaEventProducer;

    private TweetEvent tweetEvent;

    private ReplyEvent replyEvent;

    @BeforeEach
    void setup() {
        Tweet tweet1 = TweetsUtil.returnTweetOne();
        tweetEvent = TweetEvent.builder().id(null).tweet(modelMapper.map(tweet1, TweetDto.class)).build();
        Reply reply = Reply.builder().username("user1").tweetId("tweet1").replyContent("reply").build();
        replyEvent = ReplyEvent.builder().id(null).reply(reply).build();
    }

    @Test
    void sendTweetEventFailTest() throws JsonProcessingException {

        // given
        SettableListenableFuture future = new SettableListenableFuture();
        future.setException(new RuntimeException("Exception calling kafka"));

        // when
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        // then
        ListenableFuture<SendResult<Integer, String>> messsgeSent = kafkaEventProducer.tweetHandler(tweetEvent);
        assertThrows(Exception.class, messsgeSent::get);
    }

    @Test
    void sendReplyEventFailTest() throws JsonProcessingException {

        // given
        SettableListenableFuture future = new SettableListenableFuture();
        future.setException(new RuntimeException("Exception calling kafka"));

        // when
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        // then
        ListenableFuture<SendResult<Integer, String>> messgerSent = kafkaEventProducer.replyHandler(replyEvent);
        assertThrows(Exception.class, messgerSent::get);
    }

    @Test
    void sendTweetEventSuccessTest() throws JsonProcessingException, ExecutionException, InterruptedException {

        // given
        SettableListenableFuture future = new SettableListenableFuture();
        String record = objectMapper.writeValueAsString(tweetEvent);
        ProducerRecord<Integer, String> producerRecord = new ProducerRecord("tweet-event", tweetEvent.getId(), record);
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("tweet-event", 1), 1, 1,
                System.currentTimeMillis(), 1, 2);
        SendResult<Integer, String> sendResult = new SendResult<>(producerRecord, recordMetadata);
        future.set(sendResult);

        // when
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);
        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaEventProducer.tweetHandler(tweetEvent);
        SendResult<Integer, String> sendResult1 = listenableFuture.get();

        // then
        assertThat(sendResult1.getRecordMetadata().partition()).isEqualTo(1);
    }

    @Test
    void sendReplyEventSuccessTest() throws JsonProcessingException, ExecutionException, InterruptedException {

        // given
        SettableListenableFuture future = new SettableListenableFuture();
        String record = objectMapper.writeValueAsString(replyEvent);
        ProducerRecord<Integer, String> producerRecord = new ProducerRecord("reply-event", replyEvent.getId(), record);
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("reply-event", 1), 1, 1,
                System.currentTimeMillis(), 1, 2);
        SendResult<Integer, String> sendResult = new SendResult<>(producerRecord, recordMetadata);
        future.set(sendResult);

        // when
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);
        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaEventProducer.replyHandler(replyEvent);
        SendResult<Integer, String> sendResult1 = listenableFuture.get();

        // then
        assertThat(sendResult1.getRecordMetadata().partition()).isEqualTo(1);
    }
}
