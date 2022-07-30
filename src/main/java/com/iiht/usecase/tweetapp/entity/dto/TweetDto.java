package com.iiht.usecase.tweetapp.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
@JsonPropertyOrder({"tweetId", "username", "content", "created", "likes", "replies"})
public class TweetDto {

    @JsonProperty("tweetId")
    private String id;
    private String username;
    @NotBlank(message = "tweet cannot be empty")
    @Size(max = 144, message = "tweet should not go beyond 144 characters")
    private String content;
    private String created;
    private Map<String, String> likes;
    private Map<String, List<String>> replies;
}
