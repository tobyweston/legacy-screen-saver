package com.touchclarity.buildstatus;

import org.jdesktop.jdic.screensaver.ScreensaverContext;
import org.jdesktop.jdic.screensaver.ScreensaverSettings;

public class ConfigurableBuildStatus extends ScreenSaver {
	
	public void addProperty(String name, String value) {
		ScreensaverContext saverContext = getContext();
		if (saverContext==null){
			context = new ScreensaverContext();
		}
		ScreensaverSettings settings = getContext().getSettings();
		settings.setProperty(name, value);
	}

}
