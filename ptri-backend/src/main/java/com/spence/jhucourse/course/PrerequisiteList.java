package com.spence.jhucourse.course;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

@Entity
public class PrerequisiteList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String operator;
    private String unitString;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "prerequisite_id")
    private List<PrerequisiteList> operands;

    

    
    public PrerequisiteList(String unitString) {
        operator = "UNIT";
        this.unitString = unitString; 
        operands = null; 
    }

    public PrerequisiteList() {
        operands = new ArrayList<>();
    }

    public String getOperator() {
        return this.operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getUnitString() {
        return this.unitString;
    }

    public void setUnitString(String unitString) {
        this.unitString = unitString;
    }

    public List<PrerequisiteList> getOperands() {
        return this.operands;
    }

    public void setOperands(List<PrerequisiteList> operands) {
        this.operands = operands;
    }

    public String toString() {
        switch (operator) {
            case "NULL":
                return "Null List";
            case "UNIT":
                return "Unit: " + unitString;
            default:
                String ret = operator + ": {\n";
                for (PrerequisiteList p : operands) {
                    ret += p.toString() + "\n";
                }
                ret += "\n}";
                return ret;
        }
        
    }

    

   
    
}
