package com.axon.controller;

import com.axon.event.TestEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
public class TestController {

    @Autowired
    private EventGateway eventGateway;


    @GetMapping(path = "/publish/test/event")
    public String getHello() {
        String id = UUID.randomUUID().toString();
        eventGateway.publish(TestEvent.builder().id(id).message("ba ba black sheep..!!!").build());
        return id;
    }

}
