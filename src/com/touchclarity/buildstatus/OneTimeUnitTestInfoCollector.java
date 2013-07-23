package com.touchclarity.buildstatus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class OneTimeUnitTestInfoCollector {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            printUsage();
            System.exit(-1);
        }
        String file = args[0];
        String folder = args[1];

        backupOrginalFile(file);
        HistoricalTestStatisticsFileManager manager = new HistoricalTestStatisticsFileManager(folder, file, Long.MAX_VALUE);
        printOringalFileStatus(manager);
        manager.run();
        printUpdatedFileStatus(manager);
    }

    private static void printOringalFileStatus(HistoricalTestStatisticsFileManager manager) {
        System.out.print("Orginal ");
        printStatus(manager);
    }
    
    private static void printUpdatedFileStatus(HistoricalTestStatisticsFileManager manager) {
        System.out.print("Updated ");
        printStatus(manager);
        System.out.println("Done.");
    }
    
    private static void printStatus(HistoricalTestStatisticsFileManager manager) {
        System.out.println("file contains information for " + manager.getRevisionCount() + " revisions");
        if (System.getProperty("debug") != null)
            System.out.println("Revisions\n" + manager.getRevisionList());
    }

    private static void backupOrginalFile(String file) throws IOException {
        File backup = new File(file + ".bak");
        if (backup.exists())
            backup.delete();
        backup.createNewFile();
        
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(backup));
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.flush();
        in.close();
        out.close();
    }

    private static void printUsage() {
        System.out.println("Usage : " + OneTimeUnitTestInfoCollector.class.getName() + " outputFile archiveFolder");
    }
    
}
