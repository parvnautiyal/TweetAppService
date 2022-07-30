package com.iiht.usecase.tweetapp.util;

import com.iiht.usecase.tweetapp.entity.Tweet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TweetsUtil {

    private TweetsUtil() {
    }

    public static Tweet returnTweetOne(){
        Map<String,String> likeMap = new HashMap<>() {{
            put("user1", "Tweet-1");
            put("user2", "Tweet-1");
        }};

        List<String> reply1 = Arrays.asList("Test Reply 1","Test Reply 2");
        List<String> reply2 = Arrays.asList("Test Reply 3","Test Reply 4");

        Map<String,List<String>> replyMap = new HashMap<>() {{
            put("user1",reply1) ;
            put("user2", reply2);
        }};

        return Tweet.builder()
                .id("Tweet-1")
                .username("Test User")
                .content("This is a test tweet")
                .created("2022/07/28 15:09:48")
                .likes(likeMap)
                .replies(replyMap)
                .build();
    }

    public static Tweet returnTweetTwo(){
        Map<String,String> likeMap = new HashMap<>() {{
            put("user3", "Tweet-2");
            put("user4", "Tweet-2");
        }};

        List<String> reply1 = Arrays.asList("Test Reply 5","Test Reply 6");
        List<String> reply2 = Arrays.asList("Test Reply 7","Test Reply 8");

        Map<String,List<String>> replyMap = new HashMap<>() {{
            put("user3",reply1) ;
            put("user4", reply2);
        }};

        return Tweet.builder()
                .id("Tweet-2")
                .username("Test User 2")
                .content("This is another test tweet")
                .created("2022/07/28 15:09:48")
                .likes(likeMap)
                .replies(replyMap)
                .build();
    }
}
