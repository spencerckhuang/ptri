package com.spence.jhucourse.course;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Course {

    @Id
    private String offeringName;
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 1000)
    private String prerequisiteString; // get this first from raw api call

    @OneToMany
    private List<Course> prerequisiteFor; // later we process the above string and fill out this list

    public Course() {
        this.offeringName = "";
        this.title = "";
        this.description = "";
        this.prerequisiteString = "";
        this.prerequisiteFor = new ArrayList<>();
    }

    public String getOfferingName() {
        return this.offeringName;
    }

    public void setOfferingName(String offeringName) {
        this.offeringName = offeringName;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrerequisiteString() {
        return this.prerequisiteString;
    }

    public void setPrerequisiteString(String prerequisiteString) {
        this.prerequisiteString = prerequisiteString;
    }

    public List<Course> getPrerequisiteFor() {
        return this.prerequisiteFor;
    }

    public void setPrerequisiteFor(List<Course> prerequisiteFor) {
        this.prerequisiteFor = prerequisiteFor;
    }

}
