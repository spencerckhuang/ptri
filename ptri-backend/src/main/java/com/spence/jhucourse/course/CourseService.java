package com.spence.jhucourse.course;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        // TODO: Set course title
        course.setTitle(apiCourse.getTitle());

        // TODO: Make another API call to:
        // TODO: Get course description
        // TODO: Get course prerequisites

        // courseRepository.save(course);
        return Mono.just(course);
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
