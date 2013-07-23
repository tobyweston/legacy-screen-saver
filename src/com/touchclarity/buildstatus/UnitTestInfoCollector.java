package com.touchclarity.buildstatus;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class UnitTestInfoCollector {

    private Logger logger = Logger.getLogger("org.jdesktop.jdic.screensaver");
    private String path;

    
    public UnitTestInfoCollector(String path) {
        this.path = path;
    }
    
    public TestStatistics getTestStatistics() {
        ArrayList<TestStatistics> statistics = new ArrayList<TestStatistics>();
        ArrayList<File> files = getFileList();
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        final JunitReportStatisticsHandler handler = new JunitReportStatisticsHandler();
        factory.setValidating(false);
        logger.finest("Processing files...");
        for (File file : files) {
            logger.finest(file.getName());
            try {
                SAXParser parser = factory.newSAXParser();
                parser.parse(file, handler);
                final TestStatistics result = handler.getStatistics();
                if (result == null)
                    logger.warning("Didn't find expected XML element(s) in " + file.getAbsolutePath());
                else 
                    statistics.add(result);
            } catch (IOException e) {
                logger.severe("IO Error with file, " + file.getName());
            } catch (ParserConfigurationException e) {
                logger.severe("Error configuring parser");
            } catch (SAXException e) {
                logger.severe("Error parsing " + file.getName() + ", " + e.getMessage());
            }
        }
        return getStatisticTotals(statistics);
    }
    
    private TestStatistics getStatisticTotals(ArrayList<TestStatistics> statistics) {
        TestStatistics total = new TestStatistics();
        total.setName("All Tests for " + path);
        for (TestStatistics results : statistics) {
            if (total.getDate() == null) 
                total.setDate(results.getDate());
            total.setErrors(total.getErrors() + results.getErrors());
            total.setFailures(total.getFailures() + results.getFailures());
            total.setTests(total.getTests() + results.getTests());
            total.setTime(total.getTime() + results.getTime());
        }
        return total;
    }

    ArrayList<File> getFileList() {
        File root = new File(path);
        if (root == null || !root.isDirectory()) {
            logger.severe(path + " is an invalid root path for the unit test folder");
            return new ArrayList<File>();
        }
        return listTestResults(root.listFiles());
    }

    private ArrayList<File> listTestResults(File[] files) {
        ArrayList<File> results = new ArrayList<File>();
        for (File file : files) {
            if (file.isDirectory()) {
                final ArrayList<File> tests = listTestResults(file.listFiles());
                Collections.addAll(results, tests.toArray(new File[] {}));
            } else if (file.getName().endsWith("Test.xml") && !file.getName().startsWith("FAILED-TEST-")) {
                results.add(file);
            }
        }
        return results;
    }


    private class JunitReportStatisticsHandler extends DefaultHandler {

        private boolean found;
        private boolean dateFound;
        private TestStatistics statistics;
        
        @Override
        public void startDocument() {
            found = false;
            dateFound = false;
            statistics = null;
        }
        
        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) {
            if (!found && name.equalsIgnoreCase("testsuite")) { 
                statistics = new TestStatistics();
                final String errors = attributes.getValue("errors");
                final String failures = attributes.getValue("failures");
                final String tests = attributes.getValue("tests");
                final String time = attributes.getValue("time");
                final String testName = attributes.getValue("name");
                if (errors != null)
                    statistics.setErrors(Integer.parseInt(errors));
                if (failures != null) 
                    statistics.setFailures(Integer.parseInt(failures));
                if (tests != null)
                    statistics.setTests(Integer.parseInt(tests));
                if (time != null)
                    statistics.setTime(Float.parseFloat(time));
                if (testName != null)
                    statistics.setName(testName);
                found = true;
            } 
            if (!dateFound && name.equalsIgnoreCase("property")) { 
                final String attr = attributes.getValue("name");                
                if ("build.time".equalsIgnoreCase(attr)) {
                    try {
                        String date = attributes.getValue("value");
                        statistics.setDate(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).parse(date));
                    } catch (ParseException e) {
                        logger.warning("Problem parsing date, " + attributes.getValue("value") + ", " + e.getMessage());
                    }
                    dateFound = true;
                }
            }
        }

        public TestStatistics getStatistics() {
            return statistics;
        }

    }
}
