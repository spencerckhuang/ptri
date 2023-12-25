package com.spence.jhucourse.course;

import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public void getAllCourses() { // Maybe connect this to JHUApiService instead

        Flux<JHUApiCourse> apiCourseFlux = jhuApiService.makeApiCall(API_KEY);
        List<JHUApiCourse> apiCourses = apiCourseFlux.collectList().block();

        Map<String, Course> uniqueCourses = new HashMap<>();

        for (JHUApiCourse apiCourse : apiCourses) {
            if (apiCourse.getLevel().indexOf("Graduate") == -1 && apiCourse.getLevel().indexOf("Independent") == -1) {
                if (!uniqueCourses.containsKey(apiCourse.getOfferingName())) {
                    System.out.println("ACCEPTED: " + apiCourse.getLevel());
                    uniqueCourses.put(apiCourse.getOfferingName(), convertToCourse(apiCourse).block());
                } else {
                    System.out.println("DENIED REPEAT: " + apiCourse.getLevel());
                }

            } else {
                System.out.println("DENIED GRAD/INDPT: " + apiCourse.getLevel());
            }

        }

        List<Course> courses = new ArrayList<>(uniqueCourses.values());

        String pattern = "[A-Za-z]{2}\\.[0-9]{3}\\.[0-9]{3}";
        Pattern regex = Pattern.compile(pattern);

        System.out.println("STARTING MATCHING...");

        for (Course course : courses) {

            // * Now use regex to find actual prerequisites
            Matcher matcher = regex.matcher(course.getPrerequisiteString());

            while (matcher.find()) {
                String match = matcher.group();
                System.out.println(course.getTitle() + " (" + course.getOfferingName() + "): Found " + match);
                // * Find course with matching code and add to course prerequisiteFor List<>
                Optional<Course> optionalCourse = courseRepository.findById(match);
                if (optionalCourse.isPresent()) {
                    course.getPrerequisiteFor().add(optionalCourse.get().getOfferingName());
                    System.out.println("Added " + optionalCourse.get().getTitle() + " as a PR to " + course.getTitle());
                } else {
                    // * Try to find other course, might not be in CS department
                    Course newCourse = getCourseInfo(match).block();
                    if (newCourse.getTitle() != "") {
                        course.getPrerequisiteFor().add(newCourse.getOfferingName());
                    }
                }
            }

            try {
                courseRepository.save(course);
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getClass().getSimpleName() + ". Duplicate key (?): "
                        + course.getOfferingName() + ", " + course.getTitle());
            }

        }

        System.out.println("DONE MATCHING");

    }

    private Mono<Course> getCourseInfo(String codeAndSectionFull) {
        Course course = new Course();

        String codeAndSection = codeAndSectionFull.replace(".", "") + "01";

        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString("https://sis.jhu.edu/api/classes/{code}/{term}")
                .queryParam("key", API_KEY)
                .buildAndExpand(codeAndSection, "Fall 2023");

        List<String> badKeyphrases = new ArrayList<>(
                Arrays.asList("Students can only", "Students may", "Students can take",
                        "Students must have completed Lab Safety", "Credit may only be earned"));

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
                            course.setTitle(jsonNode.get(0).get("Title").asText(""));
                        } catch (NullPointerException e) {
                            System.out.println("ERROR: COURSE DOES NOT HAVE A TITLE: " + codeAndSectionFull);
                        }

                        course.setOfferingName(codeAndSectionFull);

                        try {
                            String description = jsonNode.get(0).get("SectionDetails").get(0).get("Description")
                                    .asText("");
                            course.setDescription(description);
                        } catch (NullPointerException e) {
                            // System.out.println("LOLXD");
                        }

                        if (course.getTitle() != "") {
                            courseRepository.save(course);
                        }

                        try {
                            int prereqDescIndex = -1;
                            String prerequisites;
                            do {
                                prereqDescIndex++;
                                prerequisites = jsonNode.get(0).get("SectionDetails").get(0).get("Prerequisites")
                                        .get(prereqDescIndex).get("Description").asText("");
                            } while (startsWithAny(prerequisites, badKeyphrases));
                            course.setPrerequisiteString(prerequisites);
                            courseRepository.save(course);
                        } catch (NullPointerException e) {
                            // System.out.println("LOLXDXD");
                        }

                        if (course.getTitle() != "") {
                            courseRepository.save(course);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return Mono.just(course);
                });
    }

    private Mono<Course> convertToCourse(JHUApiCourse apiCourse) {
        return getCourseInfo(apiCourse.getOfferingName());
    }

    // * Helper method that returns true if str starts with any string in prefixes.
    // False otherwise.
    private boolean startsWithAny(String str, List<String> prefixes) {
        for (String pre : prefixes) {
            if (str.startsWith(pre)) {
                return true;
            }
        }
        return false;
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
