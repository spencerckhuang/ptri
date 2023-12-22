package com.spence.jhucourse.course;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Course {

    @Id
    private int id;
    private String code;
    private String name;

    public Course() {
    }

    public Course(int id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public Course(String code, String name) {
        this.id = -1;
        this.code = code;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Course id(int id) {
        setId(id);
        return this;
    }

    public Course code(String code) {
        setCode(code);
        return this;
    }

    public Course name(String name) {
        setName(name);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Course)) {
            return false;
        }
        Course course = (Course) o;
        return id == course.id && Objects.equals(code, course.code) && Objects.equals(name, course.name);
    }

    @Override
    public String toString() {
        return "{" +
                " id='" + getId() + "'" +
                ", code='" + getCode() + "'" +
                ", name='" + getName() + "'" +
                "}";
    }

}
