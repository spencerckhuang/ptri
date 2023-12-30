package com.spence.jhucourse.course;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class Course {

    @Id
    private String offeringName;
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String prerequisiteString; // get this first from raw api call

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "prerequisite_id")
    private PrerequisiteList prerequisiteFor;

    public Course() {
        this.offeringName = "";
        this.title = "";
        this.description = "";
        this.prerequisiteString = "";
        this.prerequisiteFor = new PrerequisiteList();
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

    public PrerequisiteList getPrerequisiteFor() {
        return this.prerequisiteFor;
    }

    public void setPrerequisiteFor(PrerequisiteList prerequisiteFor) {
        this.prerequisiteFor = prerequisiteFor;
    }

}
