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

import java.awt.Point;

/**
 *
 * @author jordan
 */
public class RealPoint implements Cloneable{
    
    double x;
    double y;
    
    public RealPoint (double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public RealPoint (int x, int y) {
        this((double)x, (double)y);
    }
    
    public RealPoint (Point p) {
        this(p.x, p.y);
    }
    
    public RealPoint scaleBy(double scalar) {
        x *= scalar;
        y *= scalar;
        return this;
    }
    
    public RealPoint inverseScaleBy(double scalar) {
        x /= scalar;
        y /= scalar;
        return this;
    }
    
    public void offsetFrom(RealPoint origin) {
        x -= origin.x;
        y -= origin.y;
    }
    
    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public void translate(double dx, double dy) {
        x += dx;
        y += dy;
    }
    
    public RealPoint subtract(RealPoint other) {
        x -= other.x;
        y -= other.y;
        return this;
    }
    
    public RealPoint add(RealPoint other) {
        x += other.x;
        y += other.y;
        return this;
    }
    
    public Point getPoint() {
        return new Point((int)x, (int)y);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new RealPoint(x,y);
    }
}
