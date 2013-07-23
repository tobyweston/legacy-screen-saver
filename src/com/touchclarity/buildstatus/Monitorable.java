package com.touchclarity.buildstatus;

import java.awt.Graphics;
import java.awt.Rectangle;

public interface Monitorable {
    
    public void display(Graphics g, Rectangle r);
    
    public void updateStatus(); 
}
