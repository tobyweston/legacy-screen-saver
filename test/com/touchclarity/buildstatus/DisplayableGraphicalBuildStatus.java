package com.touchclarity.buildstatus;

import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.UIManager;

public class DisplayableGraphicalBuildStatus extends JFrame {

    private static final long serialVersionUID = 7034937508655146290L;

    public static final SortedMap<String, FailureMode> statuses = new TreeMap<String, FailureMode>();
    
    static { 
        statuses.put("web", FailureMode.TEST);
        statuses.put("dataextractor", FailureMode.PASS);
        statuses.put("tools", FailureMode.PASS);
        statuses.put("reasonforserve", FailureMode.PASS);
        statuses.put("cms", FailureMode.COMPILE);
        statuses.put("digitalenvoy", FailureMode.PASS);
        statuses.put("rubarb", FailureMode.COMPILE);
        statuses.put("offlinedataimporter", FailureMode.COMPILE);
        statuses.put("warehouse", FailureMode.COMPILE);
        statuses.put("web", FailureMode.PASS);
        statuses.put("shared", FailureMode.PASS);
        statuses.put("databases", FailureMode.PASS);
        statuses.put("filecharger", FailureMode.PASS);
        statuses.put("mole", FailureMode.TEST);
        statuses.put("datalogger", FailureMode.PASS);
        statuses.put("loggerscorer", FailureMode.COMPILE);
        statuses.put("modeller", FailureMode.TEST);
        statuses.put("extranet", FailureMode.PASS);
        statuses.put("simulateddata", FailureMode.PASS);
        statuses.put("lookup", FailureMode.SYSTEM);
        statuses.put("loggingdata", FailureMode.COMPILE);
        statuses.put("console", FailureMode.SYSTEM);
        statuses.put("cat", FailureMode.PASS);
        statuses.put("requestplayer", FailureMode.PASS);
        statuses.put("functionaltests", FailureMode.PASS);
        statuses.put("acceptancetests", FailureMode.PASS);
    }
    
    public static void main(String[] args) {
        DisplayableGraphicalBuildStatus bar = new DisplayableGraphicalBuildStatus(statuses);        
        bar.setVisible(true);
    }
    
    public DisplayableGraphicalBuildStatus(SortedMap<String, FailureMode> results) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        final GraphicalBuildStatus statusBar = new GraphicalBuildStatus("build/1", "http://dev1/tcs/trunk/build/output/build.summary");
        statusBar.setStatues(results);
        add(statusBar);
        setTitle("Test Results - " + results.size());
        setSize(600, 400);
    }
    
}
