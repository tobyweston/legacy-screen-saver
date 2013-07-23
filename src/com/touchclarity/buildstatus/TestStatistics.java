package com.touchclarity.buildstatus;

import java.util.Date;

public class TestStatistics {

    private int errors;
    private int failures;
    private int tests;
    private float time;
    private String name;
    private Date date;

    public Date getDate() {
        return date;        
    }

    void setErrors(int errors) {
        this.errors = errors;
    }

    void setFailures(int failures) {
        this.failures = failures;
    }

    void setTests(int tests) {
        this.tests = tests;
    }

    void setTime(float time) {
        this.time = time;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public int getErrors() {
        return errors;
    }

    public int getFailures() {
        return failures;
    }

    public int getTests() {
        return tests;
    }

    public float getTime() {
        return time;
    }

    public String getName() {
        return name;
    }

    void setDate(Date date) {
        this.date = date;
    }

    
}
