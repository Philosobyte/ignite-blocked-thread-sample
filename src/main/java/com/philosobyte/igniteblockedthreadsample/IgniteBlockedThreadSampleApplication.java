package com.philosobyte.igniteblockedthreadsample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class IgniteBlockedThreadSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(IgniteBlockedThreadSampleApplication.class, args);
    }

}
