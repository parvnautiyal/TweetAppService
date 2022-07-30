package com.iiht.usecase.tweetapp.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user" )
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class User {

    @Id
    private String userName;
    private String firstName;
    private String lastName;
    private String gender;
    private String dob;
    private String email;
    private String password;
}
