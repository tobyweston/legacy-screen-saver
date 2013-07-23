package com.touchclarity.buildstatus;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import junit.framework.TestCase;

public class UnitTestInfoCollectorTest extends TestCase {

    public void testEmptyFileList() {
        String path = getFilenameFromClasspath("/archive/junit-report");
        UnitTestInfoCollector collector = new UnitTestInfoCollector(path);
        ArrayList<File> results = collector.getFileList();
        verifyNumberOfTestFiles(results, 0);
    }
    
    public void testFileList() {
        String path = getFilenameFromClasspath("/archive/junit-xml");
        UnitTestInfoCollector collector = new UnitTestInfoCollector(path);
        ArrayList<File> results = collector.getFileList();
        verifyNumberOfTestFiles(results, 58);
    }
    
    public void testExtractTestStatistics() {
        String path = getFilenameFromClasspath("/archive/junit-xml");
        UnitTestInfoCollector collector = new UnitTestInfoCollector(path);
        TestStatistics statistics = collector.getTestStatistics();
        assertNotNull(statistics);
        assertEquals(2, statistics.getErrors());
        assertEquals(3, statistics.getFailures());
        assertEquals(931, statistics.getTests());
        assertEquals(440.153F, statistics.getTime());
        assertEquals("Thu Nov 01 10:35:00 GMT 2007", statistics.getDate().toString());
    }
    
    public void XtestArchive() {
        UnitTestInfoCollector collector = new UnitTestInfoCollector("\\\\dev1\\c$\\tcs_build\\archive\\r22260\\junit-xml");
        TestStatistics statistics = collector.getTestStatistics();
        outputResults(statistics);
    }

    private void outputResults(TestStatistics statistics) {
        System.out.println(statistics.getName());
        System.out.println("tests    : " + statistics.getTests());
        System.out.println("errors   : " + statistics.getErrors());
        System.out.println("failures : " + statistics.getFailures());
        System.out.println("time     : " + statistics.getTime());
    }

    private String getFilenameFromClasspath(String filename) {
        URL resource = getClass().getResource(filename);
        assertNotNull("resource not found " + filename, resource);
        return resource.getPath();
    }

    private void verifyNumberOfTestFiles(ArrayList<File> results, int expected) {
        assertEquals("incorrect number of tests found", expected, results.size());
    }

}
