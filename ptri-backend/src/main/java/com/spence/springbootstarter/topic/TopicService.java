package com.spence.springbootstarter.topic;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class TopicService {
    private List<Topic> topics = Arrays.asList(
            new Topic("spring", "Spring Framework", "learn about spring framework and springboot"),
            new Topic("algo", "Intro Algorithms", "basic algorithms"),
            new Topic("lade", "Linear Algebra and Differential Equations", "exactly what it sounds like"));

    public List<Topic> getAllTopics() {
        return topics;
    }

    // Iterate over list of topics and find first Topic that matches Id
    public Topic getTopic(String id) {
        return topics.stream().filter(t -> t.getId().equals(id)).findFirst().get();
    }
}
