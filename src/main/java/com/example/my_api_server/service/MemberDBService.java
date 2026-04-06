package com.example.my_api_server.service;

import com.example.my_api_server.entity.Member;
import com.example.my_api_server.event.MemberSignUpEvent;
import com.example.my_api_server.repo.MemberDBRepo;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberDBService {

    private final MemberDBRepo memberDBRepo;
    private final MemberPointService memberPointService;
    private final ApplicationEventPublisher publisher;

    //    @Transactional(rollbackFor = IOException.class) // checkedException은 에러가 발생해도 롤백 되지 않는데 특정 에러만 롤백 되도록 하는 설정
    @Transactional
    public Long signUp(String email, String password) throws IOException {
        Member member = Member.builder()
                .email(email)
                .password(password)
                .build();

        //저장
        Member savedMember = memberDBRepo.save(member);
        publisher.publishEvent(new MemberSignUpEvent(savedMember.getId(), savedMember.getEmail()));
//        sendNotification();
//        changeAllUserData(); // commit되기 전에 저장한 데이터를 조회하는거 확인하기
//        memberPointService.changeAllUserData();
//        throw new IOException("외부 API 호출하다가 I/O 예외가 터짐");
        //DB에 저장하다가 뭔가 오류가 발생해서 예외가 터짐(Runtime 예외)
//        throw new RuntimeException("DB에 저장하다가 뭔가 오류가 발생해서 예외가 터짐");

        return savedMember.getId();
    }

    //    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Transactional
    public void changeAllUserData() {
        List<Member> members = memberDBRepo.findAll();
    }

    public void sendNotification() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("알림 전송 완료!");
    }

    @Transactional(propagation = Propagation.REQUIRED, timeout = 2)
    public void tx1() {
        List<Member> members = memberDBRepo.findAll();

        members.forEach((m) -> {
            log.info("member id = {}", m.getId());
            log.info("member email = {}", m.getEmail());
        });

        memberPointService.changeAllUserData();
        memberPointService.timeout();
    }
}
