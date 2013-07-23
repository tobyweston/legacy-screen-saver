package com.touchclarity.buildstatus;

import static java.awt.Color.BLACK;
import static java.awt.Color.DARK_GRAY;
import static java.awt.Color.GRAY;
import static java.awt.Color.ORANGE;
import static java.awt.Color.RED;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.Map;
import java.util.SortedMap;
import java.util.logging.Logger;


public class GraphicalBuildStatus extends Canvas implements Monitorable {

    Logger logger = Logger.getLogger("org.jdesktop.jdic.screensaver");
    
    private static final int PADDING = 20;
    private static final int SPACER = 5;
    
    private static final Color DARK_ORANGE = new Color(220, 150, 0);
    private static final Color DARK_RED    = new Color(200, 0, 0);
    private static final Color DARK_GREEN  = new Color(0, 200, 0);
    
    private static final long serialVersionUID = -5961684766139495162L;
    
    private Map<String, FailureMode> results;
    private String title;
    private String url;

    private boolean drawModuleNames;

    public GraphicalBuildStatus(String title, String url) {
        this(title, url, true);
    }
    
    public GraphicalBuildStatus(String title, String url, boolean drawModuleNames) {
        this.title = title;       
        this.url = url;
        this.drawModuleNames = drawModuleNames;
        this.results = new Hashtable<String, FailureMode>();
        this.results.put("Loading...", FailureMode.SYSTEM);
    }

    @Override
    public void paint(Graphics g) {
        setBackground(BLACK);
        final int width = getWidth() - (PADDING * 2);
        final int height = getHeight() - (PADDING * 2);
        final int textWidth = width / 3;
        final int moduleWidth = (textWidth * 2) / results.size();
        int x = textWidth + PADDING;
        int y = 0 + PADDING;

        drawTitle(g, getTextRegion(y, textWidth, height));
        drawModules(g, height, moduleWidth, x, y);
    }

    private Rectangle getTextRegion(int y, int width, int height) {
        final Rectangle rectangle = new Rectangle(0, y, width, height);
        final int SHRINK_PERCENT = 10;
        rectangle.grow(-(width / SHRINK_PERCENT), -(height / SHRINK_PERCENT));
        return rectangle;
    }

    private void drawTitle(Graphics graphics, Rectangle region) {
        Graphics2D g = (Graphics2D) graphics.create();
        final Font font = new Font("Serif", Font.BOLD, 24);
        final FontRenderContext context = g.getFontRenderContext();
        g.setFont(font);
        Rectangle2D bounds = g.getFont().getStringBounds(title, context);
        
        final LineMetrics metrics = font.getLineMetrics(title, context);
        float xScale = (float) (region.width / bounds.getWidth());
        float yScale = (region.height / (metrics.getAscent() + metrics.getDescent()));

        double x = region.x;
        double y = region.y + region.height - (yScale * metrics.getDescent());
        AffineTransform transformation = AffineTransform.getTranslateInstance(x, y);
        
        if (xScale > yScale)
            transformation.scale(yScale, yScale);
        else
            transformation.scale(xScale, xScale);

        g.setFont(font.deriveFont(transformation));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.GRAY);
        g.drawString(title, 0, 0);
        g.setColor(Color.LIGHT_GRAY);
        g.drawString(title, -1, -1);
        g.dispose();
    }

    private void drawModules(Graphics g, final int height, final int moduleWidth, int x, int y) {
//        synchronized (results) {
            g.setFont(new Font("Arial", Font.BOLD, 12));
            for (String module : results.keySet()) {
                final FailureMode failureMode = getFailureMode(module);
                drawModuleBlock(g, x, y, moduleWidth, height, failureMode);
                drawModuleName(g, module, failureMode, x, PADDING);   
                x = x + moduleWidth; 
            }
//        }
    }

    private void drawModuleBlock(Graphics graphics, int x, int y, final int width, final int height, final FailureMode failureMode) {
        Graphics2D g = (Graphics2D) graphics;
        Color colour = getColorFromFailureMode(failureMode);
        g.setPaint(new GradientPaint(x, y, colour, x + width, y + height, colour.brighter()));
        g.fill(new Rectangle2D.Double(x, y, width, height));

        // to give it a border
        graphics.setColor(colour);
        graphics.draw3DRect(x, y, width, height, true); 
    }

    private void drawModuleName(Graphics graphics, String module, FailureMode failureMode, int x, int y) {
        if (drawModuleNames) {
            Graphics2D g = (Graphics2D) graphics.create();        
            g.setColor(getFontColor(failureMode));
            g.rotate(Math.toRadians(90));
            g.drawString(module, y + SPACER, -(x + SPACER)); 
            g.dispose();
        }
    }

    private Color getFontColor(FailureMode failureMode) {
        switch (failureMode) {
        case SYSTEM:
            return GRAY;
        case TEST:
            return DARK_ORANGE.darker();
        case COMPILE:
            return DARK_RED.darker();
        case PASS:
            return DARK_GREEN.darker();
        default:
            return GRAY;
        }
    }

    private Color getColorFromFailureMode(FailureMode failureMode) {
        switch (failureMode) {
        case COMPILE:
            return RED.darker();
        case TEST:
            return ORANGE.darker();
        case SYSTEM:
            return DARK_GRAY;
        case PASS:
            return DARK_GREEN;
        default:
            return DARK_GRAY;
        }
    }

    private FailureMode getFailureMode(String module) {
        return results.get(module);
    }

    @Override
    public void display(Graphics graphics, Rectangle r) {
        Graphics g = graphics.create(r.x, r.y, r.width, r.height);
        setBounds(r);        
        paint(g);
        g.dispose();
    }

    @Override
    public void updateStatus() {
        BuildSummaryReader reader = new BuildSummaryReader(url);
        results = reader.getSummary();
    }

    void setStatues(SortedMap<String, FailureMode> results) {
        this.results = results;
    }

}
