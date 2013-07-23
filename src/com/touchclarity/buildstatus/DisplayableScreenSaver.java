package com.touchclarity.buildstatus;

import org.jdesktop.jdic.screensaver.ScreensaverBase;
import org.jdesktop.jdic.screensaver.ScreensaverFrame;
import org.jdesktop.jdic.screensaver.SimpleScreensaver;

@SuppressWarnings("serial")
public class DisplayableScreenSaver extends ScreensaverFrame {

    public DisplayableScreenSaver(ScreensaverBase screensaver, String params) {
        super(screensaver, params);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1152, 864);
        setTitle("Touch Clarity - Build Status");
    }

    @SuppressWarnings({ "unchecked", "cast" })
    public static void main(String args[]) {
        try {
            if (args.length == 0)
                usage();

            String screensaverClassName = args[0];
            Class<SimpleScreensaver> screensaverClass = (Class<SimpleScreensaver>) Class.forName(screensaverClassName);
            if (!ScreensaverBase.class.isAssignableFrom(screensaverClass)) {
                System.err.println("Error: Class " + screensaverClassName + " is not a subclass of ScreensaverBase");
                System.exit(-1);
            }
            ScreensaverBase screensaver = (ScreensaverBase) screensaverClass.newInstance();

            String params = "";
            for (int i = 1; i < args.length; i++) {
                params += args[i] + " ";
            }
            new DisplayableScreenSaver(screensaver, params).setVisible(true);
        } catch (ClassNotFoundException e) {
            System.err.println("Can't find screensaver " + e.getMessage());
        } catch (InstantiationException e) {
            System.err.println("Can't instantiate screensaver: " + e.getMessage());
        } catch (IllegalAccessException e) {
            System.err.println("Can't instantiate screensaver: " + e.getMessage());
        }
    }

    private static void usage() {
        System.err.println("Usage: java " + ScreensaverFrame.class.getName() + " <screensaverclass> [<params>]");
        System.exit(-1);
    }

}
