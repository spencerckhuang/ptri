package com.spence.jhucourse.apicourse;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apicourse")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true", allowedHeaders = "*")
public class JHUApiCourseController {
    private final JHUApiService jhuApiService;
    private static final String API_KEY = "Z33DyKCS864qRJnVAsC5FgdAtgV92NhF";

    @Autowired
    public JHUApiCourseController(JHUApiService j) {
        this.jhuApiService = j;
    }

    @RequestMapping("")
    public String test() {
        return "Hello World";
    }

    // need method to get data
    @RequestMapping("fetch-and-store-data")
    public List<JHUApiCourse> makeApiCall() {
        // return jhuApiService.makeApiCall(API_KEY);
        return null;
    }

}
