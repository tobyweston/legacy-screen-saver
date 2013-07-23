package com.touchclarity.buildstatus;

import java.net.URL;

import javax.swing.JFrame;
import javax.swing.UIManager;

@SuppressWarnings("serial")
public class DisplayableHistoricalTestStatistics extends JFrame {

    public static void main(String[] args) {
        DisplayableHistoricalTestStatistics thing = new DisplayableHistoricalTestStatistics();
        thing.setVisible(true);
    }

    public DisplayableHistoricalTestStatistics() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        URL resource = DisplayableHistoricalTestStatistics.class.getResource("/junit-stats.xml");
        if (resource == null)
            System.err.println("failing to find resource");        
        HistoricalTestStatistics graph = new HistoricalTestStatistics("file://" + resource.getFile());
        add(graph);
        setTitle("Historical Results");
        setSize(300, 200);    }
    
}
