package com.sarefin.miniddd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// Bootstrap — the composition root's entry point; the only class allowed to know that Spring wires the whole
// application together.
@SpringBootApplication
@EnableScheduling
public class MiniDddApplication {
    public static void main(String[] args) {
        SpringApplication.run(MiniDddApplication.class, args);
    }
}
