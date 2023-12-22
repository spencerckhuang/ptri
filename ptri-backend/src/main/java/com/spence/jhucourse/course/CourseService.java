package com.spence.jhucourse.course;

import java.util.List;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spence.jhucourse.apicourse.JHUApiCourse;
import com.spence.jhucourse.apicourse.JHUApiService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private JHUApiService jhuApiService;

    private static final String API_KEY = "Z33DyKCS864qRJnVAsC5FgdAtgV92NhF";

    @Autowired
    private final WebClient webClient;

    public CourseService(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<Course> getAllCourses() { // Maybe connect this to JHUApiService instead
        // List<Course> courses = new ArrayList<>();
        // courseRepository.findAll().forEach(courses::add); // findAll() returns an
        // iterator
        // return courses

        Flux<JHUApiCourse> apiCourseFlux = jhuApiService.makeApiCall(API_KEY);

        Flux<Course> courseFlux = apiCourseFlux.flatMap(this::convertToCourse);

        List<Course> courses = courseFlux.collectList().block();

        return courses;
    }

    private Mono<Course> convertToCourse(JHUApiCourse apiCourse) {
        Course course = new Course();

        course.setOfferingName(apiCourse.getOfferingName());

        // * Set course title
        course.setTitle(apiCourse.getTitle());

        // * Make another API call to:
        String codeAndSection = apiCourse.getOfferingName().replace(".", "") + "01";

        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString("https://sis.jhu.edu/api/classes/{code}/{term}")
                .queryParam("key", API_KEY)
                .buildAndExpand(codeAndSection, "Fall 2023");

        return webClient.get()
                .uri(uriComponents.toUriString(), codeAndSection, "Fall 2023")
                .header("key", API_KEY)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(responseBody -> {
                    try {
                        // System.out.println("RESPONSE: " + responseBody);
                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode jsonNode = objectMapper.readTree(responseBody);

                        try {
                            String description = jsonNode.get(0).get("SectionDetails").get(0).get("Description")
                                    .asText("");
                            String prerequisites = jsonNode.get(0).get("SectionDetails").get(0).get("Prerequisites")
                                    .get(0).get("Description").asText("");

                            course.setDescription(description);
                            course.setPrerequisiteString(prerequisites);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return Mono.just(course);
                });

    }

    public Course getCourse(String id) {
        Optional<Course> optionalCourse = courseRepository.findById(id);
        return optionalCourse.orElse(null);

    }

    public void addCourse(Course course) {
        courseRepository.save(course);
    }

    public void updateCourse(Course course) {
        courseRepository.save(course);
    }

    public void deleteCourse(String id) {
        courseRepository.deleteById(id);
    }

    public void addJHUCourses() {

    }
}
