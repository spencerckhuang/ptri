package com.spence.jhucourse.course;

import java.util.ArrayList;
import java.util.List;

public class PrerequisiteList {
    private String operator;
    private String unitString;
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
