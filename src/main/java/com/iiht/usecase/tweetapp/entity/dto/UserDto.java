package com.iiht.usecase.tweetapp.entity.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@JsonPropertyOrder({ "userName", "firstName", "lastName", "gender", "dob", "email", "password" })
public class UserDto {

    @NotBlank(message = "username cannot be null")
    @Pattern(regexp = "^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){3,18}[a-zA-Z0-9]$", message = "Invalid userName")
    private String userName;

    @NotBlank(message = "firstName cannot be null")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Invalid firstName")
    private String firstName;

    @NotBlank(message = "lastName cannot be null")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Invalid lastName")
    private String lastName;

    @Pattern(regexp = "male|female|other", message = "Invalid Gender")
    @NotBlank(message = "gender cannot be null")
    private String gender;

    @Pattern(regexp = "^\\d{4}\\-(0[1-9]|1[012])\\-(0[1-9]|[12]\\d|3[01])$", message = "Date of birth Should be in YYYY-MM-DD format")
    private String dob;

    @Pattern(regexp = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$", message = "Invalid EmailId")
    @NotBlank(message = "Enter Valid Email Id")
    private String email;

    @NotBlank(message = "password cannot be null")
    private String password;
}
