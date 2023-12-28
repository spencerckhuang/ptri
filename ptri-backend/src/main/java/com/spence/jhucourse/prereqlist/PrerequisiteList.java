package com.spence.jhucourse.prereqlist;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PrerequisiteList {
    private String operator;
    private List<PrerequisiteList> operands;

    public PrerequisiteList() {
        operator = "Single Course";
        operands = new ArrayList<>(); 
    }

    public PrerequisiteList(String prereqString) {
        operands = new ArrayList<>(); 

        String pattern = "[A-Za-z]{2}\\.[0-9]{3}\\.[0-9]{3}";
        Pattern regex = Pattern.compile(pattern);

        // * Find first term: It either has parentheses, or doesn't. Represent using indices
        int firstTermEndIndex;

        if (prereqString.charAt(0) == '(') {
            firstTermEndIndex = prereqString.indexOf(")", 1);
        } else {
            firstTermEndIndex = 9;
        }

        // * Find first operand -- this will dictate operand of *this* prereqList
        int firstAND = prereqString.indexOf("AND", firstTermEndIndex);
        int firstOR = prereqString.indexOf("OR", firstTermEndIndex);

        if (firstAND == -1 && firstOR == -1) {
            operator = "Single Course";
        } else if (firstAND < firstOR) {
            operator = "AND";
        } else {    // i.e. if firstOR < firstAND
            operator = "OR";
        }


        // * Get rest of terms and add. Goal is to set value of "operands"
        // * --> Either a term is a "single term" or it is a group, surrounded by parentheses
        // TODO: Use getTerms here


        // * Traverse through getTerms and add all to operands.
        // * --> If a term is a "group", need to recursively call PrerequisiteList on it. 
        // * --> If a term is a single course, create a new PrerequisiteList with "Single Course" operator (i.e. recursive base case)
        // TODO: ^that lol


        // * Done :)
    }


    
    private List<String> getTerms(String prereqString) {
        List<String> terms = new ArrayList<>();
        // * If you find an opening parentheses, find the next one. Else, just add the next 8 characters (i.e. single course code)

        int index = 0;

        while (index < prereqString.length()) {
            // TODO: Traverse string using index and put all terms into list
        }

        return terms;
    }

    public String getOperator() {
        return this.operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public List<PrerequisiteList> getOperand() {
        return this.operands;
    }

    public void setOperand(List<PrerequisiteList> operands) {
        this.operands = operands;
    }
   

   
    
}
