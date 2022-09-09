package com.iiht.usecase.tweetapp.entity;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Reply {
    private String username;
    private String tweetId;
    private String replyContent;
}
