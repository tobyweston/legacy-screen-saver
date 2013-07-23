/**
 * 
 */
package com.touchclarity.buildstatus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

class HistoricalTestStatisticsFileManager extends TimerTask {

    private Logger logger = Logger.getLogger("org.jdesktop.jdic.screensaver");
    
    private static int instance;
    
    private Timer timer;
    private long interval;
    private String folder;
    private String output; 
    private List<String> revisions;
    
    /* it doesn't make much sense to schedule this very often, it may take some time and the
     * data its watching doesn't change frequently. */
    public HistoricalTestStatisticsFileManager(String folder, String output, long interval) {
        this.interval = interval;
        this.folder = folder;
        this.output = output;
        this.revisions = new ArrayList<String>();
        loadRevisionList();
        timer = new Timer("HistoricalTestStatisticRecorder-" + instance, true);
        instance++;
    }


    public void start() {
        timer.schedule(this, 0, interval);
    }
    
    @Override
    public void run() {
        boolean found = false;
        File root = new File(folder);
        if (!root.isDirectory()) {
            logger.severe("Path configured in " + getClass().getName() + " is not a valid folder, " + root);
            return;
        }
        File[] folders = root.listFiles(new RevisionFolderFilter());
        
        List<TestStatistics> statistics = loadXMLFile();
        for (File revision : folders) {
            if (!revisions.contains(revision.getName())) {
                UnitTestInfoCollector collector = new UnitTestInfoCollector(revision.getAbsolutePath());
                TestStatistics results = collector.getTestStatistics();
                results.setName(revision.getName());
                statistics.add(results);
                revisions.add(revision.getName());
                logger.finest("Adding new archive " + results.getName());
                found = true;
            }
        }
        if (found) {
            try {
                outputFile(statistics);
            } catch (IOException e) {
                logger.severe("Failed to output XML, " + output + ", " + e.getMessage());
            }
        }
    }

    private void loadRevisionList() {
        for (TestStatistics result : loadXMLFile())
            revisions.add(result.getName());
    }


    @SuppressWarnings("unchecked")
    private List<TestStatistics> loadXMLFile() {
        XStream xstream = new XStream(new DomDriver());
        xstream.alias("TestStatistics", TestStatistics.class);
        File input = new File(output);
        FileReader reader;
        try {
            reader = new FileReader(input);
            List<TestStatistics> results = (List<TestStatistics>) xstream.fromXML(reader);
            return results;
        } catch (FileNotFoundException e) {
            logger.warning(output + " file not found, this may be ok if the this is the first time it has run");
        }
        return new ArrayList<TestStatistics>();
    }
    
    private void outputFile(List<TestStatistics> statistics) throws IOException {
        XStream xstream = new XStream(new DomDriver());
        xstream.alias("TestStatistics", TestStatistics.class);
        BufferedWriter writer = new BufferedWriter(new FileWriter(output));
        xstream.toXML(statistics, writer);
    }

    private final class RevisionFolderFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            return pathname.isDirectory() && isRevision(pathname);
        }

        private boolean isRevision(File pathname) {
            String tail = pathname.getName().substring(1);
            try {
                if (pathname.getName().startsWith("r")) {
                    Integer.parseInt(tail);
                    return true;
                }
            } catch (NumberFormatException e) {
            }
            return false;
        }
    }

    public int getRevisionCount() {
        return revisions.size();
    }


    public List<String> getRevisionList() {
        return new ArrayList<String>(revisions); // is this a clone?
    }
    
}