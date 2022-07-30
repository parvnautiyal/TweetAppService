package com.iiht.usecase.tweetapp.domain;

import com.iiht.usecase.tweetapp.entity.dto.TweetDto;
import com.iiht.usecase.tweetapp.util.TweetEventType;
import lombok.*;

import javax.validation.Valid;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class TweetEvent {
    private Integer id;
    private TweetEventType tweetEventType;
    @Valid
    TweetDto tweet;
}
