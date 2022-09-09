package com.iiht.usecase.tweetapp.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.iiht.usecase.tweetapp.entity.Reply;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
@JsonPropertyOrder({ "tweetId", "username", "content", "created", "likes", "replies" })
public class TweetDto {

    @JsonProperty("tweetId")
    private String id;
    private String username;
    @NotBlank(message = "tweet cannot be empty")
    @Size(max = 144, message = "tweet should not go beyond 144 characters")
    private String content;
    @Size(max = 50, message = "tag should not go beyond 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9 ]+$", message = "invalid tag")
    private String tag;
    private String created;
    private Map<String, String> likes;
    private List<Reply> replies;
}
