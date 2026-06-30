package com.kether.storyteller;

import org.springframework.boot.SpringApplication;

public class TestStoryTellerApplication {

    public static void main(String[] args) {
        SpringApplication.from(StoryTellerApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
