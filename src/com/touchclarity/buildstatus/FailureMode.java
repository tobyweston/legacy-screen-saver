package com.touchclarity.buildstatus;

import java.awt.Color;

public enum FailureMode {

    SYSTEM(Color.DARK_GRAY, "Can't connect"), 
    PASS(Color.GREEN, "Pass"), 
    COMPILE(Color.RED, "Compilation failure"), 
    TEST(Color.ORANGE, "Test failures");
    
    
    private Color colour;

    String text;

    FailureMode(Color colour, String text) {
        this.colour = colour;
        this.text = text;
    }

    public Color getColour() {
        return this.colour;
    }

}
