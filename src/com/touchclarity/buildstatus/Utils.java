package com.touchclarity.buildstatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class Utils {

    /** Get the content of the page */
    protected static StringBuilder getContent(String url) throws IOException {
        StringBuilder content = new StringBuilder();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                content.append(inputLine);
        } catch (IOException e) {
            content = null;
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return content;
    }

}
