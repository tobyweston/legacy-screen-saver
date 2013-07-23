package com.touchclarity.buildstatus;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdesktop.jdic.screensaver.ScreensaverContext;
import org.jdesktop.jdic.screensaver.ScreensaverSettings;
import org.jdesktop.jdic.screensaver.SimpleScreensaver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

/**
 * Screen saver that displays build / test status for all build environments
 *
 * @author Jason Crane
 */
public class ScreenSaver extends SimpleScreensaver implements ImageObserver {
    
    private static final int DEFAULT_REFRESH = 10;
    private static final String DEFAULT_CONFIG_LOCATION = "\\\\dev4\\c$\\screensaver\\builds.xml";

    Logger logger = Logger.getLogger("org.jdesktop.jdic.screensaver");
        
    private List<Monitorable> displayableRegions = Collections.synchronizedList(new ArrayList<Monitorable>());
    
    private boolean initDone=false;
    private int refreshInterval;
    private boolean singleScreen;
    
    private DOMParser parser;
    private Image backBuffer;
    private Graphics g;
    boolean redraw;

    
    public ScreenSaver() {
        parser = new DOMParser();
    }
    
    /**
     * Initialise this screen saver
     */
    @Override
    public void init() {
        logger.finest("Touch Clarity Screen Save init() called");
		ScreensaverSettings settings = getContext().getSettings();
		
		refreshInterval = getRefreshInterval(settings);
		singleScreen = getSingleScreen(settings);
		boolean drawModuleNames = getDrawModuleNames(settings);
		String configFile = getConfigFile(settings);
        
		displayableRegions.clear();

		try {
		    parser.parse(new InputSource(new FileInputStream(configFile)));
		    Document document = parser.getDocument();

		    NodeList builds = document.getElementsByTagName("builds").item(0).getChildNodes();
		    for (int i = 0; i < builds.getLength(); i++) {
		        Node buildNode = builds.item(i);

		        String name = null;
		        String url = null;
		        for (int x = 0; x < buildNode.getChildNodes().getLength(); x++) {
		            Node node = buildNode.getChildNodes().item(x);
		            if (node.getNodeName().equals("name")){
		                name = node.getTextContent();
		            }

		            if (node.getNodeName().equals("url")){
		                url = node.getTextContent();
		            }

		            if (name != null && url != null) {
		                if (buildNode.getNodeName().equalsIgnoreCase("build")) {
		                    displayableRegions.add(new GraphicalBuildStatus(name, url, drawModuleNames));
		                } else if (buildNode.getNodeName().equalsIgnoreCase("test-statistics")) {
		                    displayableRegions.add(new HistoricalTestStatistics(url));
		                }
		                break;
		            }
		        }
		    }
		} catch (SAXException e) {
		    logger.log(Level.SEVERE, "Error parsing XML", e);
		} catch (IOException e) {
		    logger.log(Level.SEVERE, "Error reading XML", e);
		}
		
        if (!initDone) {
			Thread update = new Thread(new UpdateStatusThread(refreshInterval));
			update.setName("ScreenSaver-Update-" + update.getName());
			update.start();
			initDone=true;
        }
    }

    private int getRefreshInterval(ScreensaverSettings settings) {
        try {
			return Integer.parseInt(settings.getProperty("refresh"));
		} catch (NumberFormatException e) {
		} finally {
		    logger.finest("refreshInterval set to " + refreshInterval);
		}
		return DEFAULT_REFRESH;
    }

    private boolean getSingleScreen(ScreensaverSettings settings) {
        boolean singleScreen = (settings.getProperty("singleScreen") != null);
		logger.finest("singleScreen set to " + singleScreen);
		return singleScreen;
    }

    private boolean getDrawModuleNames(ScreensaverSettings settings) {
        return (settings.getProperty("dontDrawModuleNames") == null);
    }
    
    private String getConfigFile(ScreensaverSettings settings) {
        String configFile = settings.getProperty("configFile");
        if (configFile == null) {
            configFile = DEFAULT_CONFIG_LOCATION;
        }
		logger.finest("configFile set to " + configFile);
        return configFile;
    }

	protected List<Monitorable> getDisplayableRegions(){
		 return displayableRegions;
	}
    
	@Override
    public void paint(Graphics graphics) {
        if (!redraw)
            return;
        if (displayableRegions.size() == 0)
            return;

        Component c = getContext().getComponent();
        c.setVisible(true);
        
        int width = c.getWidth();
        int height = c.getHeight();

        if (backBuffer == null || imageHasBeenResized(backBuffer, width, height)) {
            backBuffer = getContext().getComponent().createImage(width, height);
            g = backBuffer.getGraphics();
        }

        int rowHeight = height / displayableRegions.size();

        g.setColor(Color.BLACK);
        g.clearRect(0, 0, width, height);
        g.fillRect(0, 0, width, height);

        for (int row = 0; row < displayableRegions.size(); row++) {
            if (singleScreen) {
                final Rectangle bounds = new Rectangle(0, rowHeight * row, width, rowHeight);
                displayableRegions.get(row).display(g, bounds);
                outlineBorder(g, bounds);
            } else {
                Rectangle bounds = new Rectangle(0, rowHeight * row, width / 2, rowHeight);
                displayableRegions.get(row).display(g, bounds);
                outlineBorder(g, bounds);
                
                bounds = new Rectangle(width / 2, rowHeight * row, width / 2, rowHeight);
                displayableRegions.get(row).display(g, bounds);
                outlineBorder(g, bounds);
            }
        }
        drawVersion(g);
        graphics.drawImage(backBuffer, 0, 0, this);
        redraw = false;
    }

    private boolean imageHasBeenResized(Image image, int width, int height) {
        if (image == null)
            return true;
        return image.getWidth(this) != width || image.getHeight(this) != height;
    }

    private void drawVersion(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(8, 0, 34, 10);
        Font font = new Font("Arial", Font.BOLD, 12);
        g.setFont(font);
        g.setColor(Color.DARK_GRAY);
        g.drawString(Version.getVersion(), 10, 10);
        g.setColor(Color.GRAY);
        g.drawString(Version.getVersion(), 9, 9);
    }

    private void outlineBorder(Graphics g, Rectangle bounds) {
        g.setColor(Color.GRAY);
        Rectangle r = new Rectangle(bounds);
        r.grow(-5, -5);
        g.draw3DRect(r.x, r.y, r.width, r.height, true);
    }
    
    /** Thread to ask the status lines to refresh their status every 10 sec */
    private class UpdateStatusThread implements Runnable {
    	private int refreshInterval;
    	
    	private UpdateStatusThread(int refreshInterval){    	    
    		this.refreshInterval = refreshInterval;
    	}
    	
		public void run() {
			try {
				for (;;){
				    synchronized (displayableRegions) {
				        for (Monitorable status : displayableRegions){
				            status.updateStatus();
				        }
                    }
				    redraw = true;
					Thread.sleep(refreshInterval * 1000);
				}
			} catch (InterruptedException e) {
			}
		}
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        return true;
    }

    public void setContext(ScreensaverContext context) {
        this.context = context;
    }
    
}
