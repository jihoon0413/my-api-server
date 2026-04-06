package com.example.my_api_server.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class ReentrantCounter {

    private final ReentrantLock lock = new ReentrantLock();

    private int count = 0;

    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<>();
        int threadCount = 1000;
        ReentrantCounter counter = new ReentrantCounter();

        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(counter::increaseCount);
            thread.start();
            threads.add(thread);

        }

        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        log.info("기대값이 : {}", threadCount);
        log.info("실제값이 : {}", counter.getCount());
    }

    private void increaseCount() {
        this.lock.lock();

        try {
            if (this.lock.tryLock(3, TimeUnit.SECONDS)) {
                try {
                    log.info("락 획득 후 연산 작업 시작!");
                    this.count++;
                    Thread.sleep(4000);
                } finally {
                    this.lock.unlock();
                }
            } else {
                log.info("3초 안에 락 획득을 못함");
            }
        } catch (InterruptedException e) {
            log.info("작업 중단!");
            throw new RuntimeException(e);
        }


    }
}
