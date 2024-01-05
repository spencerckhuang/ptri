package com.spence.jhucourse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.spence.jhucourse.course.CourseService;

@SpringBootApplication
public class CourseAPI {
    @Autowired
    private CourseService courseService;

    public static void main(String[] args) {
        SpringApplication.run(CourseAPI.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        courseService.getAllCourses();
    }
}
