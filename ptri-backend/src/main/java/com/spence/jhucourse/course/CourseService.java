package com.spence.jhucourse.course;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    private List<Course> courses;
    private List<Course> nonMajorCourses;
    Map<String, Course> uniqueCourses;
    private Set<String> coursesTaken;

    public CourseService(WebClient webClient) {
        this.webClient = webClient;
        courses = new ArrayList<>();
        nonMajorCourses = new ArrayList<>();
        coursesTaken = new HashSet<>();
        uniqueCourses = new HashMap<>();
    }

    public void getAllCourses() { // Maybe connect this to JHUApiService instead

        Flux<JHUApiCourse> apiCourseFlux = jhuApiService.makeApiCall(API_KEY);
        List<JHUApiCourse> apiCourses = apiCourseFlux.collectList().block();

        System.out.println("Accepting/Denying courses...");

        for (JHUApiCourse apiCourse : apiCourses) {
            if (apiCourse.getLevel().indexOf("Graduate") == -1 && apiCourse.getLevel().indexOf("Independent") == -1) {
                if (!uniqueCourses.containsKey(apiCourse.getOfferingName())) {
                    // System.out.println("ACCEPTED: " + apiCourse.getLevel());
                    uniqueCourses.put(apiCourse.getOfferingName(), convertToCourse(apiCourse).block());
                } else {
                    // System.out.println("DENIED REPEAT: " + apiCourse.getLevel());
                }

            } else {
                // System.out.println("DENIED GRAD/INDPT: " + apiCourse.getLevel());
            }

        }

        System.out.println("Accepting/Denying completed!");

        courses = new ArrayList<>(uniqueCourses.values());
        System.out.println("List of current CS courses:");
        for (Course course : courses) {
            System.out.println("\t" + course.getTitle() + " (" + course.getOfferingName() + ")");
        }
        

        System.out.println("Starting course matching...");

        for (int i = 0; i < courses.size(); i++) {
            // System.out.println("Matching " + courses.get(i).getTitle() + "...");

            // ! This is not ideal, only here since some courses have INCORRECTLY FORMATTED PREREQUISISITE STRINGS :(
                // rip processing time, TOD 12/29/2023, 12:28pm
            manualAdjustments(courses.get(i));

            String prereqString = courses.get(i).getPrerequisiteString();

            // * Remove unnecessary phrases from end
            List<String> badPhrases = new ArrayList<>(Arrays.asList("or permission of the instructor.", "or permission of instructor.", "or permission", "or permission.", "or equivalent.", "(Computer System Fundamentals)"));

            for (String badPhrase : badPhrases) {
                if (prereqString.endsWith(badPhrase)) {
                    prereqString = prereqString.substring(0, prereqString.length() - badPhrase.length()).trim();
                }
            }

            PrerequisiteList prereqs = constructPrereqsFromString(prereqString);
            prerequisiteListRepository.save(prereqs);
            courses.get(i).setPrerequisiteFor(prereqs);
            courseRepository.save(courses.get(i));
        }

        System.out.println("Done matching major courses!");

        System.out.println("List of non-major courses collected: ");
        for (Course course : nonMajorCourses) {
            System.out.println("\t" + course.getTitle() + " (" + course.getOfferingName() + ")");
        }
        
        System.out.println("Processing non-major courses...");
        processNonMajorCourses();
        System.out.println("Done matching non-major courses!");

        courses.addAll(nonMajorCourses);
        
        System.out.println("Setting levels...");
        setCourseLevels();

        System.out.println("Done setting levels!");

        System.out.println("Process complete -- API ready to use :)");

    }

    private void processNonMajorCourses() {
        int index = 0;
        while (index < nonMajorCourses.size()) {
            Course currentCourse = nonMajorCourses.get(index);
            if (currentCourse.getTitle().equals("")) {
                removeCourseFromList(index, nonMajorCourses);
            } else {
                courseRepository.save(currentCourse);
                index++;
            }
        }

        // * Match all of the non-major courses. But do *not* look for any new courses. Just match ones that are already registered
        for (int i = 0; i < nonMajorCourses.size(); i++) {
            Course currentCourse = nonMajorCourses.get(i);
            // System.out.println("Matching for course: " + currentCourse.getTitle());
            String prereqString = currentCourse.getPrerequisiteString();

            List<String> badPhrases = new ArrayList<>(Arrays.asList("or permission of the instructor.", "or permission of instructor.", "or permission", "or permission.", "or equivalent.", "(Computer System Fundamentals)"));

            for (String badPhrase : badPhrases) {
                if (prereqString.endsWith(badPhrase)) {
                    prereqString = prereqString.substring(0, prereqString.length() - badPhrase.length()).trim();
                }
            }

            PrerequisiteList prereqs = constructPrereqsFromString(prereqString);
            prerequisiteListRepository.save(prereqs);
           currentCourse.setPrerequisiteFor(prereqs);
            courseRepository.save(currentCourse);
        }
    }

    private void removeCourseFromList(int index, List<Course> list) {
        // * Order doesn't matter so swap with last and remove for constant-time operation
        list.set(index, list.get(list.size() - 1));
        list.remove(list.size() - 1);
    }

    // TODO: Incorporate information regarding nonMajor courses as well
    private void setCourseLevels() {
        Set<String> coursesTakenTemp = new HashSet<>(); // Temporary, only used for getting topological order of courses. NOT THE SAME AS coursesTaken

        // * Loooooooop
        int currentLevel = 0;
        while (coursesTakenTemp.size() < courses.size()) {
            List<String> coursesTakenTempBuffer = new ArrayList<>();
            boolean coursesAddedThisLevel = false;

            // * Find all courses that can be taken at this level, and 'set' their levels
            for (int i = 0; i < courses.size(); i++) {
                Course currentCourse = courses.get(i);

                // * Ignore courses that have already been accounted for in the topo. ordering
                if (coursesTakenTemp.contains(currentCourse.getOfferingName())) {
                    continue;
                }

                // System.out.println("Examining " + currentCourse.getTitle() + "...");

                if (courseCanBeTaken(courses.get(i).getPrerequisiteFor(), coursesTakenTemp)) {
                    currentCourse.setLevel(currentLevel);
                    courseRepository.save(currentCourse);
                    System.out.println(currentCourse.getTitle() + " set to level " + currentLevel);
                    coursesTakenTempBuffer.add(currentCourse.getOfferingName());
                    coursesAddedThisLevel = true;
                } else {
                    System.out.println(currentCourse.getTitle() + " level could not be set at this time.");
                }
            }

            // * Mark all courses in the buffer as "taken" by adding them to coursesTakenTemp
            for (String courseInBuffer : coursesTakenTempBuffer) {
                coursesTakenTemp.add(courseInBuffer);
            }

            // If a course can't be matched, set it to level 0 (i.e. just break because courses are level 0 by default)
            if (coursesAddedThisLevel == false) {
                break;
            }

            // * Increase level
            currentLevel++;
        }
        
    }

    // TODO
    private boolean courseCanBeTaken(PrerequisiteList prereqList, Set<String> coursesTakenSet) {
        switch (prereqList.getOperator()) {
            case "NULL":
                return true;
            case "UNIT": 
                return processUnitList(prereqList, coursesTakenSet);
            case "AND":
                for (PrerequisiteList list : prereqList.getOperands()) {
                    if (!courseCanBeTaken(list, coursesTakenSet)) {
                        return false;
                    }
                }    
                return true;
            case "OR":
                for (PrerequisiteList list : prereqList.getOperands()) {
                    if (courseCanBeTaken(list, coursesTakenSet)) {
                        return true;
                    }
                }
                return false;
            default: // ! This should never happen
                System.out.println("SOMETHING IS VERY WRONG!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                return false;
        }
    }

    private boolean processUnitList(PrerequisiteList list, Set<String> coursesTakenSet) {
        // * If the unit string matches a course code, need to check if it exists and/or it has been taken. 
        // * If it does not, just return true

        String unitString = list.getUnitString();
        if (unitString.matches("^[A-Za-z]{2}\\.[0-9]{3}\\.[0-9]{3}$")) {
            // return coursesTakenSet.contains(unitString);
            Mono<Course> currentCourseMono = getCourseInfo(unitString);
            return (currentCourseMono == null ? true : coursesTakenSet.contains(unitString));
            // ! May want to change getCourseInfo to throw an exception if the course can't be fetched from the API
        } else {
            return false;
        }
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
                Arrays.asList("Student", "Credit may only be earned"));

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
                            // System.out.println("ERROR: COURSE DOES NOT HAVE A TITLE: " + codeAndSectionFull);
                        }

                        // If a course is graduate-level, ignore it and return NULL
                        String levelString = jsonNode.get(0).get("Level").asText("");
                        if (levelString.indexOf("Graduate") != -1 || levelString.indexOf("Independent") != -1) {
                            return null;
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
                        } else {
                            return null;
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


    private void handleExternalCourse(String prereqString) {   
        try {
            Mono<Course> courseMono = getCourseInfo(prereqString);
            Course newCourse = courseMono.block();
            nonMajorCourses.add(newCourse);
            uniqueCourses.put(prereqString, newCourse);
        } catch (Exception e) {
            // System.out.println("lolxd");
        }
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
            if (!uniqueCourses.containsKey(prereqString)) {
                handleExternalCourse(prereqString);
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
        // System.out.println("Splitting string: " + prereqString);
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

        // This should never happen. Every opening parentheses should have a matching closing one. If this happens, a manual adjustment is probably needed
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
            course.setPrerequisiteString("(EN.553.310 OR EN.553.311 OR ((EN.553.420 OR EN.553.421) AND (EN.553.430 OR EN.553.431)) AND (AS.110.201 OR AS.110.212 OR EN.553.291 OR EN.553.295)) AND (EN.500.112 OR EN.500.113 OR EN.500.114 OR EN.601.220 OR AS.250.205)");
        } else if (course.getOfferingName().equals("EN.601.415")) {
            course.setTitle("(Advanced) Databases");
        } else if (course.getTitle().equals("Artificial Intelligence")) {
            course.setPrerequisiteString("EN.601.226");
        } else if (course.getTitle().equals("Natural Language Processing")) {
            course.setPrerequisiteString("EN.601.226");
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

    // ! What is this lol
    public void addJHUCourses() {

    }
}

