package com.touchclarity.buildstatus;

import java.io.File;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

public class HistoricalTestStatisticsFileManagerTest extends TestCase {

    final String output = "HistoricalTestStatisticsFileManagerTest-results.xml";

    public void testRun() {
        URL folder = getResource("/archive/revisions");
        HistoricalTestStatisticsFileManager manager = new HistoricalTestStatisticsFileManager(folder.getFile(), output, Long.MAX_VALUE);
        manager.run();
        verifyRevisionsLoaded(manager, 2);
        verifyRevisionListSize(manager, 2);
        verifyFileExists(output);
    }
    
    /*
     * Need to clean the project's bin folder between runs
     */
    @SuppressWarnings("unused")    
    public void testBigFile() {
        URL resource = getResource("/junit-stats.xml"); // a big file
        String folder = "\\\\dev1\\c$\\tcs_build\\archive";
        long originalLastModified = getOrginalFileLastModified(resource);
        HistoricalTestStatisticsFileManager manager = new HistoricalTestStatisticsFileManager(folder, resource.getFile(), Long.MAX_VALUE);
        verifyRevisionsLoaded(manager, 65);
        verifyRevisionListSize(manager, 65);
        List<String> orginalRevisionList = manager.getRevisionList();
        manager.run();
        assertEquals("you didn't clone the list correctly", 65, orginalRevisionList.size());
        List<String> updatedRevisionList = manager.getRevisionList();
        File updatedFile = new File(resource.getFile());
        assertTrue("failed to update the file", updatedFile.lastModified() != originalLastModified);              
    }

    private long getOrginalFileLastModified(URL resource) {
        File file = new File(resource.getFile());
        long lastModified = file.lastModified();
        return lastModified;
    }

    private void verifyRevisionsLoaded(HistoricalTestStatisticsFileManager manager, int expected) {
        assertEquals("Failed to initialise the list of revisions loaded", expected, manager.getRevisionCount());
    }

    private void verifyRevisionListSize(HistoricalTestStatisticsFileManager manager, int expected) {
        assertEquals(expected, manager.getRevisionList().size());
    }
    
    private void verifyFileExists(String output) {
        File file = new File(output);
        assertNotNull(file);
        assertTrue(file.isFile());
    }

    private URL getResource(String output) {
        URL resource = getClass().getResource(output);
        assertNotNull("failed to get resource " + resource, resource);
        return resource;
    }
    
}
