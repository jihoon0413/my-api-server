package com.example.my_api_server.lock;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class SyncCounter {

    private int count = 0;

    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<>();
        int threadCount = 3;
        SyncCounter counter = new SyncCounter();

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

    private synchronized void increaseCount() {
        State state = Thread.currentThread().getState();
        log.info("state1 = {}", state.toString());
//        synchronized (this) {
//            log.info("state2 = {}", state.toString());
//
//            count++;
//        }

        log.info("state3 = {}", state.toString());
    }
}
