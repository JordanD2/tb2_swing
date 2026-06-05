/*
 * Copyright 2026 JordanD2.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.jordand2.tb2_swing.swing_gui;

import java.awt.Graphics;
import java.awt.Point;
import javax.swing.JPanel;
import io.github.jordand2.tb2_swing.core.Module;

/**
 *
 * @author JordanD2
 */
public abstract class ViewportPanel extends JPanel {
    
    final static float DEFAULT_FONT_SIZE = 12.0f;
    final static double DEFAULT_SCALE_FACTOR = 1.0;
    
    final static double SCALE_STEP = 1.2;
    final static double MAX_SCALE_FACTOR = 20.0;
    final static double MIN_SCALE_FACTOR = 1.0/MAX_SCALE_FACTOR;
    final static int KEYBOARD_ZOOM_STEP = 3;
    
    final RealPoint offset = new RealPoint(0, 0);
    
    // Use to turn canvas points into screen points
    double scaleFactor = DEFAULT_SCALE_FACTOR;
    
    boolean modified = false;
    
    DrawModule selected = null;
    DrawModule hoverModule = null;
    DrawModule pasteBin = null;
    DrawPort selectedPort = null;
    DrawPort hoverPort = null;
    
    public void resetViewport() {
        offset.setLocation(0, 0);
        scaleFactor = DEFAULT_SCALE_FACTOR;
        repaint();
    }
    
    /**
     * @param s The display string
     * @return The number of horizontal screen pixels required to display the given string
     */
    public int getStringDrawWidth(String s) {
        return getStringDrawWidth(s, (float)this.scaleFactor);
    }
    
    /**
     * @param s The display string
     * @param scaleFactor The font scaling as a percentage. 1.00 corresponds to default 12pt font.
     * @return The number of horizontal screen pixels required to display the given string
     */
    public int getStringDrawWidth(String s, float scaleFactor) {
        return getGraphics().getFontMetrics(getFont().deriveFont(DEFAULT_FONT_SIZE * scaleFactor)).stringWidth(s);
    }
    
    /**
     * @param s The display string
     * @return The number of vertical screen pixels required to display the given string
     */
    public int getStringDrawHeight(String s) {
        return getStringDrawHeight(s, (float)this.scaleFactor);
    }
    
    /**
     * @param s The display string
     * @param scaleFactor The font scaling as a percentage. 1.00 corresponds to default 12pt font.
     * @return The number of vertical screen pixels required to display the given string
     */
    public int getStringDrawHeight(String s, float scaleFactor) {
        return getGraphics().getFontMetrics(getFont().deriveFont(DEFAULT_FONT_SIZE * scaleFactor)).getHeight();
    }
    
    public abstract void loadModule(Module module);
    
    public void fillLineArrow(Graphics g, double x1, double y1, double x2, double y2) {
        final double arrowSize = (DrawModule.ARROW_SIZE);
        final double arrowWidth = (DrawModule.ARROW_WIDTH);
        final double portSize = (DrawPort.PORT_SIZE * scaleFactor);
        
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dist = Point.distance(x1, y1, x2, y2);
        
        // TODO : zero handling?
        double dxNorm = dx*0.5*portSize/dist;
        double dyNorm = dy*0.5*portSize/dist;
                    
        g.fillPolygon(new int[]{
            (int)x2,
            (int)(x2 - arrowSize*dxNorm + arrowWidth*arrowSize*dyNorm),
            (int)(x2 - arrowSize*dxNorm - arrowWidth*arrowSize*dyNorm)
        }, new int[]{
            (int)y2,
            (int)(y2 - arrowSize*dyNorm - arrowWidth*arrowSize*dxNorm),
            (int)(y2 - arrowSize*dyNorm + arrowWidth*arrowSize*dxNorm)
        }, 3);
    }
    
}
