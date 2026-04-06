package com.example.my_api_server.repo;

import com.example.my_api_server.entity.Member;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.springframework.stereotype.Component;

//DAO : db와 통신하는 객체
@Component
public class MemberRepo {

    Map<Long, Member> members = new HashMap<>();

    public Long saveMember(String email, String password) {
        Random random = new Random();
        long id = random.nextLong();
        Member member = Member.builder()
                .id(id)
                .email(email)
                .password(password)
                .build();
        members.put(id, member);

        return id;
    }

    public Member findMember(Long id) {
        return members.get(id);

    }

}
