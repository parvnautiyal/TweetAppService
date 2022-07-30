package com.iiht.usecase.tweetapp.config.producer;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class AutoCreateConfig {

    @Bean
    public NewTopic tweetEvent(){
        return TopicBuilder.name("tweet-event")
                .partitions(4)
                .replicas(1)
                .build();
    }
}
