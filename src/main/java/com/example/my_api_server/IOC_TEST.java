package com.example.my_api_server;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class IOC_TEST {

    private final IOC ioc;

    @GetMapping
    public void iocTest() {
        ioc.func1();
    }
}
