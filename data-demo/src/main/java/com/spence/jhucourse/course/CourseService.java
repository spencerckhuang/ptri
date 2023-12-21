package com.spence.jhucourse.course;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.stereotype.Service;

@Service
public class CourseService {
    private List<Course> courseList = new ArrayList<>(Arrays.asList(
            new Course(1, "553.481", "Numerical Analysis"),
            new Course(2, "553.420", "Probability"),
            new Course(3, "553.436", "Introduction to Data Science")));

    public List<Course> getAllCourses() {
        return courseList;
    }

    public Course getCourse(int id) {
        for (int i = 0; i < courseList.size(); i++) {
            Course currentCourse = courseList.get(i);
            if (currentCourse.getId() == id) {
                return currentCourse;
            }
        }

        return null;
    }

    public void addCourse(Course course) {
        courseList.add(course);
    }

    public void updateCourse(int id, Course course) {
        for (int i = 0; i < courseList.size(); i++) {
            Course currentCourse = courseList.get(i);
            if (currentCourse.getId() == id) {
                courseList.set(i, course);
                return;
            }
        }
    }

    public void deleteCourse(int id) {
        for (int i = 0; i < courseList.size(); i++) {
            Course currentCourse = courseList.get(i);
            if (currentCourse.getId() == id) {
                courseList.remove(i);
                return;
            }
        }
    }
}
