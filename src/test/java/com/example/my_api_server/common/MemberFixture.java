package com.example.my_api_server.common;

import com.example.my_api_server.entity.Member;

// 공통으로 사용하는 멤버를 생성해주는 클래스
public class MemberFixture {

    // 정적 팩토리 메서드 패턴
    public static Member.MemberBuilder defaultMember() {
        return Member.builder()
                .email("test1@gmail.com");
    }

}
