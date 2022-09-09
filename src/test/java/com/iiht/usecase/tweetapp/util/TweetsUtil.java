package com.iiht.usecase.tweetapp.util;

import com.iiht.usecase.tweetapp.entity.Reply;
import com.iiht.usecase.tweetapp.entity.Tweet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TweetsUtil {

    private TweetsUtil() {
    }

    public static Tweet returnTweetOne() {
        Map<String, String> likeMap = new HashMap<>() {
            {
                put("user1", "Tweet-1");
                put("user2", "Tweet-1");
            }
        };

        Reply reply1 = Reply.builder().username("user1").tweetId("tweet1").replyContent("reply1").build();

        Reply reply2 = Reply.builder().username("user2").tweetId("tweet1").replyContent("reply2").build();

        List<Reply> replies = List.of(reply1, reply2);

        return Tweet.builder().id("Tweet-1").username("Test User").content("This is a test tweet")
                .created("2022/07/28 15:09:48").likes(likeMap).replies(replies).tag("tag2").build();
    }

    public static Tweet returnTweetTwo() {
        Map<String, String> likeMap = new HashMap<>() {
            {
                put("user3", "Tweet-2");
                put("user4", "Tweet-2");
            }
        };

        Reply reply1 = Reply.builder().username("user3").tweetId("tweet2").replyContent("reply1").build();

        Reply reply2 = Reply.builder().username("user4").tweetId("tweet2").replyContent("reply2").build();

        List<Reply> replies = List.of(reply1, reply2);

        return Tweet.builder().id("Tweet-2").username("Test User 2").content("This is another test tweet")
                .created("2022/07/28 15:09:48").likes(likeMap).replies(replies).tag("tag2").build();
    }
}
