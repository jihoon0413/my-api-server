package com.example.my_api_server.service;

import com.example.my_api_server.entity.Member;
import com.example.my_api_server.repo.MemberRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service //bean 등록
@RequiredArgsConstructor // 생성자 주입 di
@Slf4j
public class MemberService {
    private final MemberRepo memberRepo;

    public Long signUp(String email, String password) {
        Long memberId = memberRepo.saveMember(email, password);
        log.info("회원가입한 member ID = {}", memberId);
        sendNotification();
        return memberId;
    }

    public void sendNotification() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.info("알림 전송 완료!");
    }

    public Member findMember(Long id) {
        Member member = memberRepo.findMember(id);
        return member;
    }
}
