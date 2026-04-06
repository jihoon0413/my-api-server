package com.example.my_api_server.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberSignUpEvent {
    private final Long id;
    private final String email;

}

