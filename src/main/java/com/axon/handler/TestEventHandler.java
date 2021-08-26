package com.axon.handler;

import com.axon.event.TestEvent;
import com.axon.handler.exception.TestTransientException;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@ProcessingGroup("RetryProcessor")
public class TestEventHandler {
public static final Map<String , AtomicInteger> map = new ConcurrentHashMap<>();
    @EventHandler
    public void on(TestEvent evt) {
        log.info("TestEvent Start {} : map :{}", evt, map);
        map.putIfAbsent(evt.getId(), new AtomicInteger(0));
        int count = map.get(evt.getId()).incrementAndGet();
        if(count < 3 ){
            throw new TestTransientException("hikori dicori dock..!!");

        }
        map.remove(evt.getId());
        log.info("TestEvent End map: {}", map);
    }

}
