//   Copyright 2026 JordanD2.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package io.github.jordand2.tb2_swing.swing_gui;

import io.github.jordand2.tb2_swing.core.Port;
import java.awt.Point;
import java.awt.Rectangle;

/**
 *
 * @author jordan
 */
public class DrawPort implements Cloneable {
    
    final static int PORT_INSET = 5;
    final static int PORT_SIZE = 10;
    final static int PORT_STRIDE = PORT_INSET + PORT_SIZE;
    
    final static int INPUT_PORT = 0;
    final static int OUTPUT_PORT = 1;
    
    public Canvas canvas;
    public DrawModule parent;

    public int type;

    public String name;
    public String dataType = "int";
    
    int idx;
    
    public DrawPort (Canvas canvas, DrawModule parent, int type, String name, String dataType) {
        this.canvas = canvas;
        this.parent = parent;
        this.name = name;
        this.type = type;
        this.dataType = dataType;
    }
    
    public DrawPort setIdx(int idx) {
        this.idx = idx;
        return this;
    }
    
    public int getLabelWidth() {
        return canvas.getStringDrawWidth(name);
    }
    
    public double getXPos() {
        if (type == INPUT_PORT) {
            return (parent.getXOffset() - PORT_SIZE);
        } else {
            return (parent.getXOffset() + parent.size.x);
        }
    }
    
    public double getYPos() {
        if (type == INPUT_PORT) {
            return (parent.getYOffset() + parent.size.y/2 + PORT_INSET/2.0 - PORT_STRIDE * parent.inputPorts.size()/2.0 + PORT_STRIDE * idx);
        } else {
            return (parent.getYOffset() + parent.size.y/2 + PORT_INSET/2.0 - PORT_STRIDE * parent.outputPorts.size()/2.0 + PORT_STRIDE * idx);
        }
    }
    
    public RealPoint getCenter() {
        return new RealPoint(
                (getXPos() - canvas.offset.x + PORT_SIZE/2)*canvas.scaleFactor,
                (getYPos() - canvas.offset.y + PORT_SIZE/2)*canvas.scaleFactor
        );
    }
    
    public RealPoint getClosestPoint(RealPoint other) {
        final int portSize = (int)(PORT_SIZE * canvas.scaleFactor);
        final RealPoint mCenter = getCenter();
        
        if (type == INPUT_PORT) {
            // FIXME : disconnected arrows
            // pointing at a square
            double dx = other.x - mCenter.x;
            double dy = other.y - mCenter.y;
            
            double dxNorm;
            double dyNorm;
            
            if (Math.abs(dx) > Math.abs(dy)) {
                dxNorm = 0.5*portSize*Math.signum(dx);
                dyNorm = -0.5*portSize*dy/dx;
            } else {
                dxNorm = 0.5*portSize*dx/dy*Math.signum(dy);
                dyNorm = 0.5*portSize*Math.signum(dy);
            }
            mCenter.translate(dxNorm, dyNorm);
        } else if (type == OUTPUT_PORT) {
            // pointing at a circle
            double dx = other.x - mCenter.x;
            double dy = other.y - mCenter.y;
            double dist = Math.sqrt(dx*dx+dy*dy);
                    
            // TODO : zero handling
            double dxNorm = dx*0.5*portSize/dist;
            double dyNorm = dy*0.5*portSize/dist;
            
            mCenter.translate(dxNorm, dyNorm);
        }
        return mCenter;
    }
    
    public Rectangle getScreenRect() {
        final int screenXPos = (int)((getXPos() - canvas.offset.x)*canvas.scaleFactor);
        final int screenYPos = (int)((getYPos() - canvas.offset.y)*canvas.scaleFactor);
        final int screenSize = (int)(PORT_SIZE * canvas.scaleFactor);
        
        return new Rectangle(
                screenXPos,
                screenYPos,
                screenSize,
                screenSize
        );
    }
    
    public Point getScreenCenter() {
        Rectangle r = getScreenRect();
        return new Point(r.x + r.width/2, r.y + r.height/2);
    }
    
    public Port getAsPort() {
        return new Port(name, dataType);
    }

    @Override
    protected Object clone(){
        DrawPort clone = new DrawPort(canvas, parent, type, name, dataType);
        clone.parent = null;
        return clone;
    }
    

}
