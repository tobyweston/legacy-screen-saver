package com.touchclarity.buildstatus;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.util.List;
import java.util.logging.Logger;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

@SuppressWarnings("serial")
public class HistoricalTestStatistics extends Canvas implements Monitorable {

    private static final int NUMBER_REVISIONS_TO_SHOW = 200;
    private static final boolean FAILING = false;
    private static final boolean PASSING = true;
    private static final Paint TEXT_COLOR = Color.LIGHT_GRAY;

    Logger logger = Logger.getLogger("org.jdesktop.jdic.screensaver"); 
    
    private String source;

    private JFreeChart failuresChart; 
    private JFreeChart passingChart;
    
    public HistoricalTestStatistics(String url) {
        this.source = url;
        load();
    }
    
    @Override
    public synchronized void paint(Graphics g) {
        setBackground(Color.BLACK);
        Rectangle bounds = getBounds();
        final int half = bounds.width / 2;
        final Rectangle leftRegion = new Rectangle(0, 0, half, bounds.height);
        final Rectangle rightRegion = new Rectangle(half, 0, half, bounds.height);
        leftRegion.grow(-10, -10);
        rightRegion.grow(-10, -10);
        setChartGradients(failuresChart, leftRegion, Color.RED);
        setChartGradients(passingChart, rightRegion, Color.GREEN);
        failuresChart.draw((Graphics2D) g, leftRegion);
        passingChart.draw((Graphics2D) g, rightRegion);
    }

    private void setChartRange(JFreeChart passingChart) {
        XYPlot plot = passingChart.getXYPlot();
        DefaultTableXYDataset dataset = (DefaultTableXYDataset) plot.getDataset();
        XYSeries series = dataset.getSeries(0);
        double y = getMaxY(series);
        double twoPercent = (y * 0.02);
        plot.getRangeAxis().setRange(new Range(y - twoPercent, y + twoPercent)); 
    }

    private void setChartGradients(JFreeChart chart, Rectangle region, Color seriesColour) {
        final XYPlot plot = chart.getXYPlot();
        GradientPaint backgroundGradient = new GradientPaint(region.x + (region.width / 2), region.y, Color.BLACK, region.x + region.width, region.y + region.height, Color.LIGHT_GRAY);
        plot.setBackgroundPaint(backgroundGradient);
    }
    
    private void setChartColours(JFreeChart chart, final Color fillColour) {
        chart.setBackgroundPaint(Color.BLACK);
        chart.getTitle().setPaint(TEXT_COLOR);
        chart.setBorderPaint(Color.LIGHT_GRAY);
        final XYPlot plot = chart.getXYPlot();
        plot.setRenderer(new StackedXYAreaRenderer2(null, null) {
            @Override
            public Paint getItemPaint(int row, int item) {
                return fillColour;
            }
        });

        // x
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.getDomainAxis().setLabelPaint(TEXT_COLOR);
        plot.getDomainAxis().setTickLabelPaint(TEXT_COLOR);
        plot.getDomainAxis().setTickMarkPaint(TEXT_COLOR);
        final NumberFormat formatter = NumberFormat.getIntegerInstance();
        formatter.setGroupingUsed(false);
        ((NumberAxis)plot.getDomainAxis()).setNumberFormatOverride(formatter);
        
        // y
        plot.setRangeGridlinePaint(Color.GRAY); 
        plot.getRangeAxis().setLabelPaint(TEXT_COLOR);
        plot.getRangeAxis().setTickLabelPaint(TEXT_COLOR);
    }
    
    @Override
    public void display(Graphics graphics, Rectangle r) {
        Graphics g = graphics.create(r.x, r.y, r.width, r.height);
        setBounds(r);        
        paint(g);
        g.dispose();
    }

    private void populateGraphs(TableXYDataset failing, TableXYDataset passing) {
        failuresChart = ChartFactory.createStackedXYAreaChart("Failing Unit Tests", "Revision", "Tests", failing, PlotOrientation.VERTICAL, false, false, false);
        passingChart = ChartFactory.createStackedXYAreaChart("Passing Unit Tests", "Revision", "Tests", passing, PlotOrientation.VERTICAL, false, false, false);
        setChartRange(passingChart);
        setChartColours(passingChart, Color.GREEN);
        setChartColours(failuresChart, Color.RED);
    }

    
    @SuppressWarnings("unchecked")
    private synchronized void load() {
        URLConnection connection;
        BufferedInputStream in = null;
        try {
            connection = new URL(source).openConnection();
            in = new BufferedInputStream(connection.getInputStream());
            XStream xstream = new XStream(new DomDriver());
            xstream.alias("TestStatistics", TestStatistics.class);
            List<TestStatistics> results = (List<TestStatistics>) xstream.fromXML(in);
            TableXYDataset failing = toDataSet(results, FAILING);
            TableXYDataset passing = toDataSet(results, PASSING);
            logger.finest("data for " + results.size() + " revisions loaded");
            populateGraphs(failing, passing);
        } catch (MalformedURLException e) {
            logger.severe("Bad URL from " + source + ", " + e.getMessage());
        } catch (IOException e) {
            logger.severe("Unable to load source data from " + source + ", " + e.getMessage());
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                }
        }
    }
    
    private TableXYDataset toDataSet(List<TestStatistics> results, boolean passing) {
        DefaultTableXYDataset data = new DefaultTableXYDataset();
        XYSeries series = new XYSeries(passing ? "passing" : "failing", true, false);
        series.setMaximumItemCount(NUMBER_REVISIONS_TO_SHOW);
        for (TestStatistics result : results) {
            final int revision = Integer.parseInt(result.getName().substring(1));
            final int failures = result.getFailures() + result.getErrors();
            series.add(revision, passing ? result.getTests() - failures : failures);
        }
        data.addSeries(series);
        return data;
    }

    private double getMaxY(XYSeries series) {
        double y = 0;
        for (int i = 0; i < series.getItemCount(); i++) {
            XYDataItem item = series.getDataItem(i);
            if (item.getY().doubleValue() > y)
                y = item.getY().doubleValue();
        }
        return y;
    }
    
    @Override
    public void updateStatus() {
    }
    
}
