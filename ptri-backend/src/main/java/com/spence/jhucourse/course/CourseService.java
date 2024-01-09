package com.spence.jhucourse.course;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private PrerequisiteListRepository prerequisiteListRepository;

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

            // ! This is not ideal, only here since some courses have INCORRECTLY FORMATTED PREREQUISISITE STRINGS :(
                // rip processing time, TOD 12/29/2023, 12:28pm
            manualAdjustments(course);

            String prereqString = course.getPrerequisiteString();

            // * Remove unnecessary phrases from end
            List<String> badPhrases = new ArrayList<>(Arrays.asList("or permission of the instructor.", "or permission of instructor.", "or permission", "or permission.", "or equivalent.", "(Computer System Fundamentals)"));

            for (String badPhrase : badPhrases) {
                if (prereqString.endsWith(badPhrase)) {
                    prereqString = prereqString.substring(0, prereqString.length() - badPhrase.length()).trim();
                }
            }

            PrerequisiteList prereqs = constructPrereqsFromString(prereqString);
            prerequisiteListRepository.save(prereqs);
            course.setPrerequisiteFor(prereqs);
            courseRepository.save(course);

        }

        System.out.println("DONE MATCHING");

        setCourseLevels(courses);

        System.out.println("DONE SETTING LAYERS");

    }

    private void setCourseLevels(List<Course> courses) {
        Map<String, Boolean> courseTakenMap = new HashMap<>();
        for (Course course : courses) {
            courseTakenMap.put(course.getOfferingName(), false);
        }
        
        int currentLayer = 0;
        List<Course> coursesTakenThisLevel = new ArrayList<>();
        
        while (courses.size() != 0) {
            for (int i = 0; i < courses.size(); i++) {
                Course currentCourse = courses.get(i);
                boolean validCourse = courseCanBeTaken(currentCourse.getPrerequisiteFor(), courseTakenMap);

                if (validCourse) {
                    currentCourse.setLevel(currentLayer);
                    courseRepository.save(currentCourse);

                    coursesTakenThisLevel.add(currentCourse);
                    removeFromList(i, courses);
                    i--;
                }
            }

            for (Course course : coursesTakenThisLevel) {
                courseTakenMap.put(course.getOfferingName(), true);
            }   

            coursesTakenThisLevel.clear();
            currentLayer++;
        }
    }

    // Constant time method to remove from Course list since order doesn't matter
    private void removeFromList(int index, List<Course> list) {
        list.set(index, list.get(list.size() - 1));
        list.remove(list.size() - 1);
    }

    // Determine if a course can be taken
    private boolean courseCanBeTaken(PrerequisiteList prereqList, Map<String, Boolean> courseTakenMap) {
        switch (prereqList.getOperator()) {
            case "NULL":
                return true;
            case "UNIT":
                return courseTakenMap.getOrDefault(prereqList.getUnitString(), false);
            case "AND":
                for (PrerequisiteList list : prereqList.getOperands()) {
                    if (!courseCanBeTaken(list, courseTakenMap)) {
                        return false;
                    }
                }
                return true;
            case "OR":
                for (PrerequisiteList list : prereqList.getOperands()) {
                    if (courseCanBeTaken(list, courseTakenMap)) {
                        return true;
                    }
                }
                return false;   
        }

        // * This line should never be hit
        return false;

    }


    // Create a Mono for a Course object from a String such as the following: XX.YYY.ZZZ
    private Mono<Course> getCourseInfo(String codeAndSectionFull) {
        Course course = new Course();

        String codeAndSection = codeAndSectionFull.replace(".", "") + "01";

        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString("https://sis.jhu.edu/api/classes/{code}/{term}")
                .queryParam("key", API_KEY)
                .buildAndExpand(codeAndSection, "Fall 2023");

        List<String> badKeyphrases = new ArrayList<>(
                Arrays.asList("Students", "Credit may only be earned"));

        // List<String> badKeyphrases = new ArrayList<>(
        //         Arrays.asList("Students can", "Students may", "Students can take",
        //                 "Students must have completed Lab Safety", "Credit may only be earned"));

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


    private void handleExternalCourse(Course newCourse) {
        courseRepository.save(newCourse);        
    }

    private PrerequisiteList constructPrereqsFromString (String prereqString) {
        if (prereqString == "") {
            PrerequisiteList ret = createNullPrerequisiteList();
            prerequisiteListRepository.save(ret);
            return createNullPrerequisiteList();
        }

        // * If it has surrounding parentheses, get rid of them
        prereqString = prereqString.trim();
        if (prereqString.charAt(0) == '(' && prereqString.indexOf(')') == prereqString.length() - 1) {
            prereqString = prereqString.substring(1, prereqString.length() - 1).trim();
        }

        prereqString = prereqString.trim();
        PrerequisiteList ret = new PrerequisiteList();

        // * Get operator: Get first term, then find operator after that

        // * Shave off potential outer parentheses
        // ! I think this is unnecessary bc of above lines? double check later
        if (prereqString.charAt(0) == '(' && findMatchingClosingParentheses(prereqString, 0) == prereqString.length() - 1) {
            prereqString = prereqString.substring(1, prereqString.length() - 1);
        }

        // Get first term:
        int firstTermIndex = 0;

        if (prereqString.charAt(0) == '(') {
            firstTermIndex = findMatchingClosingParentheses(prereqString, firstTermIndex);
        }

        int firstAND = prereqString.toUpperCase().indexOf(" AND", firstTermIndex);
        int firstOR = prereqString.toUpperCase().indexOf(" OR", firstTermIndex);

        if (firstAND == -1 && firstOR == -1) {
            //* First check that prereq string is a course code */
            try {
                Mono<Course> courseMono = getCourseInfo(prereqString);
                handleExternalCourse(courseMono.block());
            } catch (Exception e) {
                System.out.println("AAAAAAAAAAAAAAAA");
            }

            return new PrerequisiteList(prereqString);
            
        } else if (firstAND == -1) {
            ret.setOperator("OR");
        } else if (firstOR == -1) {
            ret.setOperator("AND");
        } else {
            ret.setOperator(firstAND < firstOR ? "AND" : "OR");
        }

        prerequisiteListRepository.save(ret);

        // * Get individual terms
        List<String> terms = splitStringIntoTerms(prereqString, ret.getOperator());  

        // Iterate over terms. To "operands", add constructPrereqsFromString(term) for all
        for (String term : terms) {
            ret.getOperands().add(constructPrereqsFromString(term));
        }

        prerequisiteListRepository.save(ret);

        return ret;

    }

    private List<String> splitStringIntoTerms(String prereqString, String operator) {
        assert(operator == "AND" || operator == "OR");
        List<String> ret = new ArrayList<>();

        int index = 0;
        while (index < prereqString.length()) {
            // * Get term, and find next character *after* next operator
            int nextOperator;

            if (prereqString.charAt(index) == '(') {
                int nextParentheses = findMatchingClosingParentheses(prereqString, index);
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

    // This should only be called if the char at startIndex is an opening parentheses
    private int findMatchingClosingParentheses(String str, int startIndex) {
        assert (str.charAt(startIndex) == '(');

        int stack = 0;

        for (int i = startIndex + 1; i < str.length(); i++) {
            switch (str.charAt(i)) {
                case '(':
                    stack++;
                    break;
                case ')':
                    if (stack == 0) {
                        return i;
                    }
                    stack--;
            }
        }

        // This should never happen. Every opening parentheses should have a matching closing one.
        return -1;
    }

    private PrerequisiteList createNullPrerequisiteList() {
        PrerequisiteList ret = new PrerequisiteList();
        ret.setOperator("NULL");
        return ret;
    }

    private void manualAdjustments(Course course) {
        if (course.getTitle().equals("Machine Translation")) {
            course.setPrerequisiteString("EN.601.226 AND (EN.553.211 OR EN.553.310 OR EN.553.311 OR ((EN.553.420 OR EN.553.421) AND (EN.553.430 OR EN.553.431)))");
        } else if (course.getTitle().equals("Machine Learning")) {
            course.setPrerequisiteString("AS.110.202 AND (EN.553.211 OR EN.553.310 OR EN.553.311 OR ((EN.553.420 or EN.553.421) AND (EN.553.430 OR EN.553.431))) AND (AS.110.201 OR AS.110.212 OR EN.553.291 OR EN.553.295) AND (EN.500.112 OR EN.500.113 OR EN.500.114 OR (EN.601.220 OR EN.600.120) OR AS.250.205 OR EN.580.200 OR (EN.600.107 OR EN.601.107)))");
        } else if (course.getTitle().equals("Computer Integrated Surgery I")) {
            course.setPrerequisiteString("EN.601.226 AND (AS.110.201 OR AS.110.212 OR EN.553.291)");
        } else if (course.getTitle().equals("Computer Vision")) {
            course.setPrerequisiteString("(EN.553.310 OR EN.553.311 OR ((EN.553.420 OR EN.553.421) AND (EN.553.430 OR EN.553.431)) AND (AS.110.201 OR AS.110.212 OR EN.553.291 OR EN.553.295) AND (EN.500.112 OR EN.500.113 OR EN.500.114 OR EN.601.220 OR AS.250.205)");
        }
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

