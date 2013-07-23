package com.touchclarity.buildstatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class StatisticsXStreamTest extends TestCase{

    public void testWrite() {
        List<TestStatistics> stats = new ArrayList<TestStatistics>();
        stats.add(createStatistic(23, 4, 1309));
        stats.add(createStatistic(0, 0, 19));
        stats.add(createStatistic(18, 4, 200));
        stats.add(createStatistic(0, 23, 23));
        
        XStream xstream = new XStream(new DomDriver());
        System.out.println(xstream.toXML(stats));
    }

    private TestStatistics createStatistic(int errors, int failures, int tests) {
        TestStatistics stats = new TestStatistics();
        stats.setDate(new Date());
        stats.setErrors(errors);
        stats.setFailures(failures);
        stats.setName("Sample statistics");
        stats.setTests(tests);
        stats.setTime(12.23F);
        return stats;
    }
    
}
