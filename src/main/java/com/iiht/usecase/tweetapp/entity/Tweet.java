package com.iiht.usecase.tweetapp.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "tweet")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Tweet {

    @Id
    private String id;
    private String username;
    private String content;
    private String created;
    private Map<String, String> likes;
    private Map<String, List<String>> replies;
}
