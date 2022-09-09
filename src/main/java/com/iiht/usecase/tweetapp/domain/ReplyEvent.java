package com.iiht.usecase.tweetapp.domain;

import com.iiht.usecase.tweetapp.entity.Reply;
import lombok.*;

import javax.validation.Valid;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class ReplyEvent {
    private Integer id;
    @Valid
    private Reply reply;
}
