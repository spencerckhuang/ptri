package com.spence.springbootstarter.topic;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TopicController {

    @RequestMapping("/topics")
    public List<Topic> getAllTopics() {
        return Arrays.asList(
                new Topic("spring", "Spring Framework", "learn about spring framework and springboot"),
                new Topic("algo", "Intro Algorithms", "basic algorithms"),
                new Topic("lade", "Linear Algebra and Differential Equations", "exactly what it sounds like"));
    }
}
