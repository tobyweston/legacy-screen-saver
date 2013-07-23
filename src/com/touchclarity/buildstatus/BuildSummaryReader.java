package com.touchclarity.buildstatus;

import static com.touchclarity.buildstatus.FailureMode.COMPILE;
import static com.touchclarity.buildstatus.FailureMode.PASS;
import static com.touchclarity.buildstatus.FailureMode.SYSTEM;
import static com.touchclarity.buildstatus.FailureMode.TEST;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class BuildSummaryReader {

    Logger logger = Logger.getLogger("org.jdesktop.jdic.screensaver");

    private String url;

    public BuildSummaryReader(String url) {
        this.url = url;        
    }

    public Map<String, FailureMode> getSummary() {
        URLConnection connection;
        BufferedInputStream in = null; 
        Map<String, FailureMode> results = new LinkedHashMap<String, FailureMode>(24);
        try {
            connection = new URL(url).openConnection();
            Properties properties = new Properties();
            in = new BufferedInputStream(connection.getInputStream());
            properties.load(in);
            if (properties.containsKey("modules")) {
                String modules = properties.getProperty("modules");
                StringTokenizer tizer = new StringTokenizer(modules, ",");
                if (tizer.countTokens() == 0) {
                    results.put("No modules found", SYSTEM);
                    return results;
                }
                while (tizer.hasMoreElements()) {
                    final String module = tizer.nextToken();
                    Properties moduleProperties = getProperties(module, properties);
                    results.put(module, getFailureMode(module, moduleProperties));
                }
            } else {
                results.put("No modules found", SYSTEM);
            }
        } catch (MalformedURLException e) {
            results.put("Bad URL", SYSTEM);
            logger.warning("Bad URL " + url + ", " + e.getMessage());
        } catch (IOException e) {
            results.put("Problem connecting", SYSTEM);
            logger.warning("Problem connecting to URL " + url + ", " + e.getMessage() + " from " + e.getClass().getName());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return results;
    }

    private FailureMode getFailureMode(String module, Properties moduleProperties) {        
        final String unittests = module + ".unit.test";
        final String functests = module + ".functional.test";

        if (moduleNotYetReached(moduleProperties))
            return SYSTEM;
        
        if (!codeCompiles(module, moduleProperties))
            return COMPILE;
        
        if (!testsCompile(module, moduleProperties))
            return COMPILE;
        
        if (moduleProperties.containsKey(functests))
            return getFailureMode(moduleProperties.getProperty(functests));

        if (moduleProperties.containsKey(unittests))
            return getFailureMode(moduleProperties.getProperty(unittests));
        
        return SYSTEM;
    }

    private boolean moduleNotYetReached(Properties moduleProperties) {
        return moduleProperties.isEmpty();
    }

    private boolean codeCompiles(String module, Properties moduleProperties) {
        final String property = module + ".compile";
        if (moduleProperties.containsKey(property)) 
            return getFailureMode(moduleProperties.getProperty(property)) != COMPILE;
        return false;
    }

    private boolean testsCompile(String module, Properties moduleProperties) {
        final String property = module + ".test.compile";
        if (moduleProperties.containsKey(property)) 
            return getFailureMode(moduleProperties.getProperty(property)) != COMPILE;
        return false;
    }
    
    private FailureMode getFailureMode(String string) {
        if (string.equals("inprogress"))            
            return TEST;
        if (string.equals("fail"))
            return COMPILE;
        if (string.equals("success"))
            return PASS;
        return SYSTEM;
    }

    private Properties getProperties(String prefix, Properties properties) {
        Properties result = new Properties();
        Enumeration<Object> keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            if (key.startsWith(prefix))
                result.put(key, properties.get(key));
        }
        return result;
    }

}
