package com.touchclarity.buildstatus;

import static com.touchclarity.buildstatus.FailureMode.PASS;
import static com.touchclarity.buildstatus.FailureMode.SYSTEM;
import static com.touchclarity.buildstatus.FailureMode.TEST;

import java.net.URL;
import java.util.Map;

import junit.framework.TestCase;

public class BuildSummaryReaderTest extends TestCase {

    public void testLoadNormalBuild() {
        Map<String, FailureMode> results = getResults("/build.summary");
        verifyResultSize(results, 24);
        printResults(results);
        verifyPassingModules(results, "tools", "dataextractor", "cms", "warehouse");
        verifyInPorgressModules(results, "digitalenvoy");
        verifyMissingModules(results, "web", "cat", "loggingdata", "filecharger", "modeller", "datalogger", "loggerscorer", "extranet", "console", "simulateddata", "lookup", "mole", "acceptancetests", "reasonforserve", "requestplayer", "databases", "functionaltests", "offlinedataimporter");
    }
    
    public void testLoadBuildingBuild() {
        Map<String, FailureMode> results = getResults("/building-sample.summary");
        verifyResultSize(results, 24);
        verifyNotYetTestedModules(results, "tools", "shared", "dataextractor", "cms", "warehouse", "digitalenvoy", "web", "cat", "loggingdata", "filecharger");
        verifyInPorgressModules(results);
        verifyMissingModules(results, "modeller", "datalogger", "loggerscorer", "extranet", "console", "simulateddata", "lookup", "mole", "acceptancetests", "reasonforserve", "requestplayer", "databases", "functionaltests", "offlinedataimporter");
    }
    
    public void testLoadFailingBuild() {
        Map<String, FailureMode> results = getResults("/failing-build.summary");
        verifyResultSize(results, 7);  
        for (String module : results.keySet()) {
            System.out.println(module + " " + results.get(module));
        }
    }
    
    private Map<String, FailureMode> getResults(String file) {
        URL url = getClass().getResource(file);
        assertNotNull("file not found", url);
        BuildSummaryReader reader = new BuildSummaryReader("file://" + url.getFile());
        Map<String, FailureMode> results = reader.getSummary();
        return results;
    }
    
    private void verifyResultSize(Map<String, FailureMode> results, int expectedSize) {
        assertNotNull(results);
        assertEquals(expectedSize, results.size());
    }
    
    private void printResults(Map<String, FailureMode> results) {
        for (String module : results.keySet()) {
            System.out.println(module + " : " + results.get(module));
        }
    }

    // these are missing from the build.summary file
    private void verifyNotYetTestedModules(Map<String, FailureMode> results, String... modules) {
        verifyModules(results, SYSTEM, modules);
    }

    private void verifyInPorgressModules(Map<String, FailureMode> results, String... modules) {
        verifyModules(results, TEST, modules);        
    }

    private void verifyMissingModules(Map<String, FailureMode> results, String... modules) {
        verifyModules(results, SYSTEM, modules);
    }

    private void verifyPassingModules(Map<String, FailureMode> results, String... modules) {
        verifyModules(results, PASS, modules);        
    }
    
    private void verifyModules(Map<String, FailureMode> results, FailureMode expected, String... modules) {
        for (String module : modules)
            assertEquals(module, expected, results.get(module));   
    }
    
}
