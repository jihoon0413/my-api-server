package com.example.my_api_server;

import org.springframework.stereotype.Component;

@Component
public class IOC {

    public void func1() {
        System.out.println("func1 실행");
    }

    static void main(String[] args) {
        IOC ioc = new IOC();
        ioc.func1();
    }
}
