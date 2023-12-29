package com.spence.jhucourse.course;

import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

        System.out.println("STARTING MATCHING...");

        for (Course course : courses) {
            System.out.println("Matching " + course.getTitle() + "...");

            String prereqString = course.getPrerequisiteString();

            // * Remove unnecessary phrases from end
            List<String> badPhrases = new ArrayList<>(Arrays.asList("or permission of the instructor."));

            for (String badPhrase : badPhrases) {
                if (prereqString.endsWith(badPhrase)) {
                    prereqString = prereqString.substring(0, prereqString.length() - badPhrase.length()).trim();
                }
            }

            System.out.println("Prerequisite string after modification: " + prereqString);

            System.out.println("-- constructing prereq object debugging --");
            course.setPrerequisiteFor(constructPrereqsFromString(prereqString));
            System.out.println("-- debugging statements done --");

            System.out.println("Prerequisite object:\n" + course.getPrerequisiteFor().toString());
            System.out.println("Matching done! Saving...");

            courseRepository.save(course);

            System.out.println("Save successful!\n\n");

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
                            course.setPrerequisiteString("");
                            courseRepository.save(course);
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

    public PrerequisiteList constructPrereqsFromString (String prereqString) {
        // * If it has surrounding parentheses, get rid of them
        prereqString = prereqString.trim();
        if (prereqString.charAt(0) == '(' && prereqString.indexOf(')') == prereqString.length() - 1) {
            prereqString = prereqString.substring(1, prereqString.length() - 1).trim();
        }

        System.out.println("cur prereq string: " + prereqString);

        PrerequisiteList ret = new PrerequisiteList();

        // * Get operator
        int firstAND = prereqString.toUpperCase().indexOf(" AND");
        int firstOR = prereqString.toUpperCase().indexOf(" OR");

        if (firstAND == -1 && firstOR == -1) {
            return new PrerequisiteList(prereqString);
        } else if (firstAND == -1) {
            ret.setOperator("OR");
        } else if (firstOR == -1) {
            ret.setOperator("AND");
        } else {
            ret.setOperator(firstAND < firstOR ? "AND" : "OR");
        }

        System.out.println("cur operator: " + ret.getOperator());

        // * Get individual terms
        List<String> terms = splitStringIntoTerms(prereqString, ret.getOperator());  
        
        System.out.println("individual terms: " + terms.toString());

        // Iterate over terms. To "operands", add constructPrereqsFromString(term) for all
        for (String term : terms) {
            ret.getOperands().add(constructPrereqsFromString(term));
        }

        return ret;

    }

    private List<String> splitStringIntoTerms(String prereqString, String operator) {
        assert(operator == "AND" || operator == "OR");

        System.out.println("current operator: " + operator);

        List<String> ret = new ArrayList<>();

        int index = 0;
        while (index < prereqString.length()) {
            // * Get term, and find next character *after* next operator
            int nextOperator;

            if (prereqString.charAt(index) == '(') {
                int nextParentheses = prereqString.indexOf(")", index);
                assert(nextParentheses != -1);
                ret.add(prereqString.substring(index, nextParentheses + 1));

                nextOperator = prereqString.toUpperCase().indexOf(" " + operator, nextParentheses);
            } else {
                // * Find next operator and add everything inbetween
                nextOperator = prereqString.toUpperCase().indexOf(" " + operator, index);

                if (nextOperator == -1) { // If no next operator, then can return early
                    ret.add(prereqString.substring(index, prereqString.length()));
                    return ret;
                }

                ret.add(prereqString.substring(index, nextOperator));
            }  
            
            // * Find first character of next term
            if (nextOperator == -1) {
                return ret;
            }

            index = nextOperator + (operator == "AND" ? 5 : 4);

            // * Repeat (happens at top of while loop)
        }

        return ret;
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
