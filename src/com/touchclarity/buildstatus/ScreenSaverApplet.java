package com.touchclarity.buildstatus;

import java.awt.Graphics;
import java.util.Date;

import javax.swing.JApplet;

import org.jdesktop.jdic.screensaver.ScreensaverContext;

@SuppressWarnings("serial")
public class ScreenSaverApplet extends JApplet implements Runnable {

    private ScreenSaver saver;

    private boolean running = true;
    private int refresh = 60;
    
    @Override
    public void init() {        
        saver = new ScreenSaver();
        saver.setContext(createContext());
        saver.init();
        Thread thread = new Thread(this);
        thread.setName("Applet-Update-" + thread.getName());
        thread.start();
    }

    @Override
    public void paint(Graphics g) {
        if (saver != null) {
            saver.redraw = true;
            saver.paint(g);
            showStatus("Last repaint " + new Date().toString());
        }
    }
    
    
    private ScreensaverContext createContext() {
        ScreensaverContext context = new ScreensaverContext();
        context.setComponent(getContentPane());
        configure(context);
        return context;
    }

    private void configure(ScreensaverContext context) {
        String configFile = getParameter("configFile");
        String statusRefresh = getParameter("statusRefresh");
        String appletRefresh = getParameter("appletRefresh");
        if (getParameter("singleScreen") != null)
            context.getSettings().setProperty("singleScreen", "true");
        if (configFile != null)
            context.getSettings().setProperty("configFile", configFile);
        if (statusRefresh != null)
            context.getSettings().setProperty("refresh", statusRefresh);
        if (appletRefresh != null)
            setAppletRefresh(appletRefresh);
    }
    
    private void setAppletRefresh(String appletRefresh) {
        try {
            refresh = Integer.parseInt(appletRefresh);
        } catch (NumberFormatException e) {
        }
    }

    @Override
    public String getAppletInfo() {
        return "Touch Clarity - Build Status";
    }
    
    @Override
    public String[][] getParameterInfo() {
        return new String[][] { 
                {"singleScreen", "boolean", "if set, single screen mode is used"},
                {"configFile", "String", "optional, alternative config file, defaults to \\\\dev4\\c$\\screensaver\\builds.xml"},
                {"statusRefresh", "int", "number of seconds between status checks"},
                {"appletRefresh", "int", "number of seconds between applet repaints"}
        };
    }

    @Override
    public void stop() {
        if (saver != null) {
            running = false;
            // ideally, we should really need to stop the thread of the screensaver too
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(refresh * 1000);
                validate();
                repaint();
            } catch (InterruptedException e) {
            }
        }
    }

}
