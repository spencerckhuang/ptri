package com.spence.jhucourse.apicourse;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@AllArgsConstructor
@Getter
@Setter
public class JHUApiCourse {
    private String TermStartDate;
    private String SchoolName;
    private String CoursePrefix;
    private String Term;
    private String TermIdr;

    @Id
    private String OfferingName;

    private String SectionName;
    private String Title;
    private String Credits;
    private String Department;
    private String Level;
    private String Status;
    private String Dow;
    private String DowSort;
    private String TimeOfDay;
    private String SubDepartment;
    private String SectionRegRestrictions;
    private String SeatsAvailable;
    private String MaxSeats;
    private String OpenSeats;
    private String Waitlisted;
    private String IsWritingIntensive;
    private String AllDepartments;
    private String Instructors;
    private String InstructorsFullName;
    private String Location;
    private String Building;
    private String HasBio;
    private String Meetings;
    private String Areas;
    private String InstructionMethod;
    private String SectionCoRequisites;
    private String SectionCoReqNotes;
    private String SssSectionsID;
    private String TermJSS;
    private String Repeatable;
    private String SectionDetails;

    public String getTermStartDate() {
        return this.TermStartDate;
    }

    public void setTermStartDate(String TermStartDate) {
        this.TermStartDate = TermStartDate;
    }

    public String getSchoolName() {
        return this.SchoolName;
    }

    public void setSchoolName(String SchoolName) {
        this.SchoolName = SchoolName;
    }

    public String getCoursePrefix() {
        return this.CoursePrefix;
    }

    public void setCoursePrefix(String CoursePrefix) {
        this.CoursePrefix = CoursePrefix;
    }

    public String getTerm() {
        return this.Term;
    }

    public void setTerm(String Term) {
        this.Term = Term;
    }

    public String getTermIdr() {
        return this.TermIdr;
    }

    public void setTermIdr(String TermIdr) {
        this.TermIdr = TermIdr;
    }

    public String getOfferingName() {
        return this.OfferingName;
    }

    public void setOfferingName(String OfferingName) {
        this.OfferingName = OfferingName;
    }

    public String getSectionName() {
        return this.SectionName;
    }

    public void setSectionName(String SectionName) {
        this.SectionName = SectionName;
    }

    public String getTitle() {
        return this.Title;
    }

    public void setTitle(String Title) {
        this.Title = Title;
    }

    public String getCredits() {
        return this.Credits;
    }

    public void setCredits(String Credits) {
        this.Credits = Credits;
    }

    public String getDepartment() {
        return this.Department;
    }

    public void setDepartment(String Department) {
        this.Department = Department;
    }

    public String getLevel() {
        return this.Level;
    }

    public void setLevel(String Level) {
        this.Level = Level;
    }

    public String getStatus() {
        return this.Status;
    }

    public void setStatus(String Status) {
        this.Status = Status;
    }

    public String getDow() {
        return this.Dow;
    }

    public void setDow(String Dow) {
        this.Dow = Dow;
    }

    public String getDowSort() {
        return this.DowSort;
    }

    public void setDowSort(String DowSort) {
        this.DowSort = DowSort;
    }

    public String getTimeOfDay() {
        return this.TimeOfDay;
    }

    public void setTimeOfDay(String TimeOfDay) {
        this.TimeOfDay = TimeOfDay;
    }

    public String getSubDepartment() {
        return this.SubDepartment;
    }

    public void setSubDepartment(String SubDepartment) {
        this.SubDepartment = SubDepartment;
    }

    public String getSectionRegRestrictions() {
        return this.SectionRegRestrictions;
    }

    public void setSectionRegRestrictions(String SectionRegRestrictions) {
        this.SectionRegRestrictions = SectionRegRestrictions;
    }

    public String getSeatsAvailable() {
        return this.SeatsAvailable;
    }

    public void setSeatsAvailable(String SeatsAvailable) {
        this.SeatsAvailable = SeatsAvailable;
    }

    public String getMaxSeats() {
        return this.MaxSeats;
    }

    public void setMaxSeats(String MaxSeats) {
        this.MaxSeats = MaxSeats;
    }

    public String getOpenSeats() {
        return this.OpenSeats;
    }

    public void setOpenSeats(String OpenSeats) {
        this.OpenSeats = OpenSeats;
    }

    public String getWaitlisted() {
        return this.Waitlisted;
    }

    public void setWaitlisted(String Waitlisted) {
        this.Waitlisted = Waitlisted;
    }

    public String getIsWritingIntensive() {
        return this.IsWritingIntensive;
    }

    public void setIsWritingIntensive(String IsWritingIntensive) {
        this.IsWritingIntensive = IsWritingIntensive;
    }

    public String getAllDepartments() {
        return this.AllDepartments;
    }

    public void setAllDepartments(String AllDepartments) {
        this.AllDepartments = AllDepartments;
    }

    public String getInstructors() {
        return this.Instructors;
    }

    public void setInstructors(String Instructors) {
        this.Instructors = Instructors;
    }

    public String getInstructorsFullName() {
        return this.InstructorsFullName;
    }

    public void setInstructorsFullName(String InstructorsFullName) {
        this.InstructorsFullName = InstructorsFullName;
    }

    public String getLocation() {
        return this.Location;
    }

    public void setLocation(String Location) {
        this.Location = Location;
    }

    public String getBuilding() {
        return this.Building;
    }

    public void setBuilding(String Building) {
        this.Building = Building;
    }

    public String getHasBio() {
        return this.HasBio;
    }

    public void setHasBio(String HasBio) {
        this.HasBio = HasBio;
    }

    public String getMeetings() {
        return this.Meetings;
    }

    public void setMeetings(String Meetings) {
        this.Meetings = Meetings;
    }

    public String getAreas() {
        return this.Areas;
    }

    public void setAreas(String Areas) {
        this.Areas = Areas;
    }

    public String getInstructionMethod() {
        return this.InstructionMethod;
    }

    public void setInstructionMethod(String InstructionMethod) {
        this.InstructionMethod = InstructionMethod;
    }

    public String getSectionCoRequisites() {
        return this.SectionCoRequisites;
    }

    public void setSectionCoRequisites(String SectionCoRequisites) {
        this.SectionCoRequisites = SectionCoRequisites;
    }

    public String getSectionCoReqNotes() {
        return this.SectionCoReqNotes;
    }

    public void setSectionCoReqNotes(String SectionCoReqNotes) {
        this.SectionCoReqNotes = SectionCoReqNotes;
    }

    public String getSssSectionsID() {
        return this.SssSectionsID;
    }

    public void setSssSectionsID(String SssSectionsID) {
        this.SssSectionsID = SssSectionsID;
    }

    public String getTermJSS() {
        return this.TermJSS;
    }

    public void setTermJSS(String TermJSS) {
        this.TermJSS = TermJSS;
    }

    public String getRepeatable() {
        return this.Repeatable;
    }

    public void setRepeatable(String Repeatable) {
        this.Repeatable = Repeatable;
    }

    public String getSectionDetails() {
        return this.SectionDetails;
    }

    public void setSectionDetails(String SectionDetails) {
        this.SectionDetails = SectionDetails;
    }

}
