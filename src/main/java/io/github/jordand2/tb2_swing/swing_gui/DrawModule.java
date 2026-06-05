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

import io.github.jordand2.tb2_swing.core.ConfigAssign;
import io.github.jordand2.tb2_swing.core.ConfigField;
import static io.github.jordand2.tb2_swing.swing_gui.DrawPort.PORT_INSET;
import static io.github.jordand2.tb2_swing.swing_gui.DrawPort.PORT_SIZE;
import static io.github.jordand2.tb2_swing.swing_gui.DrawPort.PORT_STRIDE;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import io.github.jordand2.tb2_swing.core.Module;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jordan
 */
public class DrawModule implements Cloneable {
    
    final static double ARROW_SIZE = 0.7;
    final static double ARROW_WIDTH = 1.2;
    
    String name = "";
    String type = "";
    
    String template = "uvm_component.jte";
    
    DrawModule parent = null;
    ViewportPanel canvas = null;
    
    RealPoint location;
    RealPoint size;
    
    boolean hidePortLabels = false;
    boolean hideInternals = false;
    
    final ArrayList<DrawPort> inputPorts  = new ArrayList<>();
    final ArrayList<DrawPort> outputPorts = new ArrayList<>();
    
    final Deque<DrawModule> submodules = new ArrayDeque<>();
    
    final ArrayList<DrawConnection> connections = new ArrayList<>();
    
    final ArrayList<ConfigField> configFields = new ArrayList<>();
    final ArrayList<ConfigAssign> configAssigns = new ArrayList<>();
    
    public DrawModule(ViewportPanel canvas, String name) {
        this.canvas = canvas;
        this.name = name;
    }
    
    public DrawModule(ViewportPanel canvas, String name, RealPoint location, RealPoint size) {
        this(canvas, name);
        this.location = location;
        this.size = size;
        this.type = "";
    }
    
        
    public DrawModule(ViewportPanel canvas, String name, String type, RealPoint location) {
        this(canvas, name, location);
        this.type = type;
    }
    
    public DrawModule(ViewportPanel canvas, String name, RealPoint location) {
        this(canvas, name, location, new RealPoint(0, 0));
    }
    
    public DrawModule(ViewportPanel canvas, DrawModule parent, Module module) {
        this.name = module.getName();
        if (this.name == null || this.name.isEmpty()) {
            this.name = module.getType();
        }
        this.type = module.getType();
        this.parent = parent;
        this.canvas = canvas;
        this.template = module.template;
        this.location = new RealPoint(0,0);
        this.size = new RealPoint(0,0);
        this.configFields.addAll(module.configFields);
        this.configAssigns.addAll(module.configAssigns);
        this.inputPorts .addAll(module.inputPorts .stream().map((port) -> new DrawPort      (canvas, this, DrawPort.INPUT_PORT , port.name, port.getType())).toList());
        this.outputPorts.addAll(module.outputPorts.stream().map((port) -> new DrawPort      (canvas, this, DrawPort.OUTPUT_PORT, port.name, port.getType())).toList());
        this.submodules .addAll(module.submodules .stream().map((sub ) -> new DrawModule    (canvas, this, sub                                            )).toList());
        this.connections.addAll(module.connections.stream().map((conn) -> new DrawConnection(this, getPort(conn.srcPath), getPort(conn.dstPath)   )).toList());
    }
    
    /**
     * Reassign the index field for each port in this module. The index field is used to determine
     * the vertical offset of each port when drawn on the Canvas.
     * 
     * The caller may want to call repaint(), so that the updated port positions will be displayed
     * 
     * @param includeSubmodules Iff true, then this will also recursively reindex all submodules too
     */
    public void reindexPorts(boolean includeSubmodules) {
        for (int i = 0; i < inputPorts.size(); i++) {
            inputPorts.get(i).setIdx(i);
        }
        for (int i = 0; i < outputPorts.size(); i++) {
            outputPorts.get(i).setIdx(i);
        }
        
        if (includeSubmodules) {
            for (DrawModule sub : submodules) {
                sub.reindexPorts(includeSubmodules);
            }
        }
        canvas.modified = true;
    }
    
    public DrawPort getPort(String relPath) {
        String[] parts = relPath.split("\\.", 2);
        if (parts.length == 2) {
            return submodules.stream().filter((sub) -> sub.name.matches(parts[0])).findAny().get().getPort(parts[1]);
        } else {
            ArrayList<DrawPort> allPorts = new ArrayList<>();
            allPorts.addAll(inputPorts);
            allPorts.addAll(outputPorts);
            return allPorts.stream().filter((port) -> port.name.matches(parts[0])).findAny().get();
        }
    }
    
    public boolean isTopModule() {
        return parent == null;
    }
    
    public Module getAsModule() {
        Module module = new Module();
        module.pkg = "unknown_pkg";
        module.name = name;
        module.type = type;
        module.superType = "NONE";
        module.longComment = "no comment";
        module.template = template;
        module.configFields.addAll(configFields);
        module.configAssigns.addAll(configAssigns);
        module.inputPorts.addAll(inputPorts.stream().map((port) -> port.getAsPort()).toList());
        module.outputPorts.addAll(outputPorts.stream().map((port) -> port.getAsPort()).toList());
        module.submodules.addAll(submodules.stream().map((sub) -> sub.getAsModule()).toList());
        module.connections.addAll(connections.stream().map((conn) -> conn.getAsConnection()).toList());
        return module;
    }
    
    public void getSubmodules(List<DrawModule> output, boolean recursive) {
        output.addAll(submodules);
        if (recursive) {
            for (DrawModule sub : submodules) {
                sub.getSubmodules(output, recursive);
            }
        }
    }
    
    public void addSubmodule(DrawModule module) {
        synchronized (submodules) {
            submodules.add(module);
        }
        module.parent = this;
        canvas.modified = true;
    }
    
    public void deleteSubmodule(DrawModule module) {
        synchronized (submodules) {
            submodules.remove(module);
            connections.removeIf((conn) -> (conn.sourcePort.parent == module || conn.destPort.parent == module));
        }
        canvas.modified = true;
    }
    
    /**
     * Adds a new Port to this module
     * 
     * @param name The name of the new Port
     * @param dataType The type of data this Port handles
     * @param type INPUT_PORT or OUTPUT_PORT
     */
    public void addPort(String name, String dataType, int type) {
        if (type == DrawPort.INPUT_PORT) {
            addPort(name, dataType, type, inputPorts.size());
        } else {
            addPort(name, dataType, type, outputPorts.size());
        }
        canvas.modified = true;
    }
    
    /**
     * Adds a new Port to this module
     * 
     * @param name The name of the new Port
     * @param dataType The type of data this Port handles
     * @param type INPUT_PORT or OUTPUT_PORT
     * @param portIndex Specify the port index to be assigned to this port
     */
    public void addPort(String name, String dataType, int type, int portIndex) {
        if (type == DrawPort.INPUT_PORT) {
            inputPorts.add(new DrawPort(canvas, this, type, name, dataType));
            inputPorts.getLast().idx = portIndex;
        } else {
            outputPorts.add(new DrawPort(canvas, this, type, name, dataType));
            outputPorts.getLast().idx = portIndex;
        }
        canvas.modified = true;
    }
    
    /**
     * Removes a port from this module, along with any connections associated
     * with that port.
     * 
     * @param port The port to be removed
     */
    public void deletePort(DrawPort port) {
        inputPorts.remove(port);
        outputPorts.remove(port);
        
        connections.removeIf((conn) -> conn.sourcePort == port || conn.destPort == port);
        if (this.parent != null) {
            parent.connections.removeIf((conn) -> conn.sourcePort == port || conn.destPort == port);
        }
        canvas.modified = true;
    }
    
    public String getPathTo(DrawPort port) {
        System.out.println(String.format("Looking for port %s under module %s...", port.name, name));
        if (inputPorts.contains(port) || outputPorts.contains(port)) {
            return port.name;
        } else {
            for (DrawModule sub : submodules) {
                String subPath = sub.getPathTo(port);
                if (subPath != null && !subPath.isEmpty()) {
                    return String.format("%s.%s", sub.name, subPath);
                }
            }
            System.err.println(String.format("Failed to get path to Port %s!", port.name));
            return null;
        }
    }
    
    public RealPoint getModuleOffset() {
        if (isTopModule()) {
            return new RealPoint(location.x, location.y);
        } else {
            return parent.getModuleOffset().add(this.location);
        }
    }
    
    public double getXOffset() {
        if (isTopModule()) {
            return location.x;
        } else {
            return parent.getXOffset() + location.x;
        }
    }
    
    public double getYOffset() {
        if (isTopModule()) {
            return location.y;
        } else {
            return parent.getYOffset() + location.y;
        }
    }
    
     public Rectangle getScreenRect() {
        return new Rectangle(
                (int)((getXOffset() - canvas.offset.x) * canvas.scaleFactor),
                (int)((getYOffset() - canvas.offset.y) * canvas.scaleFactor),
                (int)(size.x  * canvas.scaleFactor),
                (int)(size.y * canvas.scaleFactor)
        );
     }
    
    public DrawModule getClickedModule(MouseEvent evt) {
        if (this.getScreenRect().contains(evt.getPoint())) {
            if (submodules.isEmpty() || hideInternals) {
                return this; // no children - must have been this one
            } else {
                synchronized (submodules) {
                    for (Iterator<DrawModule> iterator = submodules.descendingIterator(); iterator.hasNext();) {
                        DrawModule sub = iterator.next();
                        if (canvas.hoverModule != null && sub == canvas.selected) {
                            continue;
                        }
                        if (sub.getScreenRect().contains(evt.getPoint())) {
                            return sub.getClickedModule(evt); // clicked on subcomponent - continue search there
                        }
                    }
                }
                return this; // didn't click on any subcomponent - return this
            }
        } else {
            return null; // clicked somewhere else - return null
        }
    }
    
    public DrawPort getClickedPort(MouseEvent evt) {
        // 1st, check my own ports
        for (DrawPort port : inputPorts) {
            Rectangle portScreenRect = port.getScreenRect();
            if (portScreenRect.contains(evt.getPoint())) {
                return port;
            }
        }
        for (DrawPort port : outputPorts) {
            Rectangle portScreenRect = port.getScreenRect();
            if (portScreenRect.contains(evt.getPoint())) {
                return port;
            }
        }
        // 2nd, if we're inside my screenBox, then check my submodule's ports
        if (!hideInternals && getScreenRect().contains(evt.getPoint())) {
            synchronized (submodules) {
                for (Iterator<DrawModule> iterator = submodules.descendingIterator(); iterator.hasNext();) {
                    DrawModule submodule = iterator.next();
                    DrawPort clicked = submodule.getClickedPort(evt);
                    if (clicked != null) {
                        return clicked;
                    }
                }
            }
        }
        return null;
    }
    
    public boolean validate() {
        boolean valid = true;
        
        // Check 1: Check that all variable names are unique
        List<String> varNames = new ArrayList<>();
        varNames.addAll(inputPorts  .stream().map((port)  -> port .name).toList());
        varNames.addAll(outputPorts .stream().map((port)  -> port .name).toList());
        varNames.addAll(submodules  .stream().map((sub)   -> sub  .name).toList());
        varNames.addAll(configFields.stream().map((field) -> field.name).toList());
        // TODO include config fields here too
        // Also keywords???
        Set<String> unique = new HashSet(varNames);
        if (varNames.size() != unique.size()) {
            System.err.println("Duplicate var name!");
            valid = false;
        }
        
        // Check 1.5: Check that all input ports have at least 1 provider
        // Confirm this requirment from UVM?
        
        // Check 2: Check that all port connections have matching types
        for (DrawConnection conn : connections) {
            valid &= conn.validate();
        }
        
        // Check 3: Validate all subcomponents
        for(DrawModule sub : submodules) {
            valid &= sub.validate();
        }
        
        return valid;
    }
    
    /**
     * Automatically resizes this module and all its submodules. Resizing is performed starting
     * from the deepest submodules and working up towards this module.
     */
    public void resize() {
        int longestOutput = 0;
        int longestInput = 0;
        
        int nameWidth = Math.max(
                canvas.getStringDrawWidth(name, 1.0f),
                canvas.getStringDrawWidth(type, 1.0f)
        );
        int nameHeight = canvas.getStringDrawHeight(name, 1.0f);
        
        if (!outputPorts.isEmpty() && !hidePortLabels) {
            longestOutput = outputPorts.stream().mapToInt((port) -> port.getLabelWidth(1.0f)).max().getAsInt() + (PORT_INSET*2);
        }
        if (!inputPorts.isEmpty() && !hidePortLabels) {
            longestInput = inputPorts.stream().mapToInt((port) -> port.getLabelWidth(1.0f)).max().getAsInt() + (PORT_INSET*2);
        }
        
        double farthestSubX = 0;
        double farthestSubY = 0; 
        
        if (!submodules.isEmpty() && !hideInternals) {
            synchronized (submodules) {
            
                for (DrawModule sub : submodules) {
                    sub.resize();
                }

                double leftmostSubX = submodules.stream().mapToDouble( (sub) -> sub.location.x).min().getAsDouble() - longestInput - (PORT_SIZE*2);
                double topmostSubY  = submodules.stream().mapToDouble( (sub) -> sub.location.y).min().getAsDouble() - nameHeight*2;
                
                this.location.translate(leftmostSubX, topmostSubY);
                for (DrawModule sub : submodules) {
                    sub.location.translate(-leftmostSubX, -topmostSubY);
                }

                farthestSubX = submodules.stream().mapToDouble( (sub) -> sub.location.x + sub.size.x).max().getAsDouble();
                farthestSubY = submodules.stream().mapToDouble( (sub) -> sub.location.y + sub.size.y).max().getAsDouble();
            }
        }
        
        int minWidth = (int)Math.max(
                Math.max(
                        farthestSubX,
                        longestInput
                ) + longestOutput,
                nameWidth
        ) + PORT_SIZE*2;
        
        int minHeight = (int)Math.max(
                (farthestSubY + nameHeight*2),
                (Math.max(
                        outputPorts.size(),
                        inputPorts.size()
                ) * PORT_STRIDE) + (nameHeight*3)
        );
            
        size.x = minWidth;
        size.y = minHeight;
    }
    
    /**
     * Automatically lays out this component and all of its submodules. Layout is performed
     * starting with the deepest subcomponent and working up toards this module.
     */
    public void layout () {
        if (submodules.isEmpty() || hideInternals) {
            return;  // early exit
        }
        
        // Recursive layout : First layout all submodules, then resize them, then proceed to layout this module.
        //                    Ensures that layouts are done bottom-up
        for (DrawModule sub : submodules) {
            sub.layout();
            sub.resize();
        }
        
        // Topological Sort structures
        final Map<DrawModule, Integer> depth = new LinkedHashMap<>(/* no data */);
        final ArrayList<DrawModule> unvisited = new ArrayList<>(submodules);
        
        while(!unvisited.isEmpty()) {
            // Step 1: Pick a node and assign 0 to it
            DrawModule next = unvisited.removeFirst();
            depth.put(next, 0);
            boolean madeProgress;
            do {
                // Step 2: Try to assign values to other nodes based on the connections. Do this repeatedly until we get stuck.
                madeProgress = false;
                for (DrawConnection conn : connections) {
                    final DrawModule src = conn.sourcePort.parent;
                    final DrawModule dst = conn.destPort.parent;
                    
                    final boolean visitedSrc = depth.containsKey(src);
                    final boolean visitedDst = depth.containsKey(dst);
                    
                    if (visitedSrc && !visitedDst && dst != this) {
                        final int srcDepth = depth.get(src);
                        unvisited.remove(dst);
                        depth.put(dst, srcDepth+1);
                        madeProgress = true;
                    }
                    
                    if (visitedDst && !visitedSrc && src != this) {
                        final int dstDepth = depth.get(dst);
                        unvisited.remove(src);
                        depth.put(src, dstDepth-1);
                        madeProgress = true;
                    }
                }
            } while (madeProgress);
        }
        
        // Normalize the depth values, so that depth 0 is always column 0
        final int minDepth = depth.values().stream().mapToInt((x) -> x).min().getAsInt();
        depth.keySet().forEach((k) -> {
            final int oldValue = depth.get(k);
            final int newValue = oldValue - minDepth;
            depth.put(k, newValue);
        });
        
        final int numCols = depth.values().stream().mapToInt((x) -> x).max().getAsInt() + 1;
        
        ArrayList<DrawModule>[] drawCols = new ArrayList[numCols];
        for (DrawModule sub : submodules) {
            final int col = depth.get(sub);
            if (drawCols[col] == null) {
                drawCols[col] = new ArrayList<>();
            }
            drawCols[col].add(sub);
        }
        
        double xOffset = PORT_SIZE*2;
        for (int i = 0; i < numCols; i++) {
            double yOffset = PORT_STRIDE + canvas.getStringDrawHeight(name, 1.0f);
            
            for (DrawModule mod : drawCols[i]) {
                mod.location.setLocation(xOffset, yOffset);
                yOffset += mod.size.y + PORT_SIZE*2;
            }
            
            xOffset += drawCols[i].stream().mapToDouble((mod) -> mod.size.x).max().orElse(0) + 4.0*PORT_SIZE;
        }
        
    }
    
    public void draw (Graphics g) {
        final int labelInset = (int)(PORT_INSET * canvas.scaleFactor);
        final int portSize   = (int)(PORT_SIZE  * canvas.scaleFactor);
        
        final double moduleBaseX = getXOffset() - canvas.offset.x;
        final double moduleBaseY = getYOffset() - canvas.offset.y;
        Color fillColor = Color.WHITE;
        Color borderColor = Color.BLACK;
        
        int nameWidth  = canvas.getStringDrawWidth(name);
        int nameHeight = canvas.getStringDrawHeight(name);

        if (this == canvas.selected) {
            borderColor = Color.RED;
        } else if (this == canvas.hoverModule) {
            borderColor = Color.YELLOW;
        }

        // Draw Box
        g.setColor(fillColor);
        g.fillRect(
                (int)(moduleBaseX * canvas.scaleFactor),
                (int)(moduleBaseY * canvas.scaleFactor),
                (int)(size.x * canvas.scaleFactor),
                (int)(size.y * canvas.scaleFactor)
        );
        g.setColor(borderColor);
        g.drawRect(
                (int)(moduleBaseX * canvas.scaleFactor),
                (int)(moduleBaseY * canvas.scaleFactor),
                (int)(size.x * canvas.scaleFactor),
                (int)(size.y * canvas.scaleFactor)
        );
        
        // Draw connections       
        if (!hideInternals){
            for (DrawConnection conn : connections) {
                final Point src = conn.sourcePort.getScreenCenter();
                final Point dst = conn.destPort.getScreenCenter();
                g.drawLine(src.x, src.y, dst.x, dst.y);
                
                RealPoint closest = conn.destPort.getClosestPoint(conn.sourcePort.getCenter());
                canvas.fillLineArrow(g, src.x, src.y, closest.x, closest.y);
            }
        }

        // Draw Input Ports
        for (DrawPort port : inputPorts) {
            Rectangle r = port.getScreenRect();

            if (canvas.selected == null && canvas.selectedPort != null) {
                if (port == canvas.selectedPort) {
                    borderColor = Color.RED;
                } else if (port == canvas.hoverPort) {
                    borderColor = Color.YELLOW;
                } else {
                    borderColor = Color.BLACK;
                }
            }

            g.setColor(fillColor);
            g.fillRect(r.x, r.y, r.width, r.height);

            g.setColor(borderColor);
            g.drawRect(r.x, r.y, r.width, r.height);

            if (!hidePortLabels) {
                g.drawString(port.name, r.x + portSize + labelInset, r.y + portSize/2 + labelInset);
            }
        }

        // Draw Output Ports
        for (DrawPort port : outputPorts) {
            Rectangle r = port.getScreenRect();

            if (canvas.selected == null && canvas.selectedPort != null) {
                if (port == canvas.selectedPort) {
                    borderColor = Color.RED;
                } else if (port == canvas.hoverPort) {
                    borderColor = Color.YELLOW;
                } else {
                    borderColor = Color.BLACK;
                }
            }

            g.setColor(fillColor);
            g.fillOval(r.x, r.y, r.width, r.height);

            g.setColor(borderColor);
            g.drawOval(r.x, r.y, r.width, r.height);

            if (!hidePortLabels) {
                g.drawString(port.name, r.x - labelInset - port.getLabelWidth(), r.y + portSize/2 + labelInset);
            }
        }

        if (canvas.selected == null && canvas.selectedPort != null) {
            borderColor = Color.BLACK;
        }

        // Draw Name
        g.setColor(borderColor);
        g.drawString(
                name,
                (int)((moduleBaseX + size.x/2) * canvas.scaleFactor) - nameWidth/2,
                (int)((moduleBaseY) * canvas.scaleFactor) + nameHeight
        );
        
        // Draw Type
        g.drawString(
                type,
                (int)((moduleBaseX + size.x/2) * canvas.scaleFactor - canvas.getStringDrawWidth(type)/2),
                (int)((moduleBaseY + size.y) * canvas.scaleFactor - canvas.getStringDrawHeight(type)/2)
        );
        
        if (!hideInternals) {
            synchronized (submodules) {
                for (DrawModule sub : submodules) {
                    sub.draw(g);
                }
            }
        }
        
    }
    


    @Override
    public Object clone() throws CloneNotSupportedException {
        Map<DrawPort, DrawPort> connPortMap = new HashMap<>();
        return cloneWithPortMap(connPortMap);
    }
    
    protected DrawModule cloneWithPortMap(Map<DrawPort, DrawPort> connPortMap) throws CloneNotSupportedException {
        DrawModule clone = new DrawModule(canvas, name, (RealPoint)location.clone(), (RealPoint)size.clone());
        
        clone.parent = parent;
        clone.type = type;
        clone.template = template;
        clone.hideInternals = hideInternals;
        clone.hidePortLabels = hidePortLabels;
        for (DrawPort port : inputPorts) {
            clone.addPort(port.name, port.dataType, port.type);
            connPortMap.put(port, clone.inputPorts.getLast());
        }
        for (DrawPort port : outputPorts) {
            clone.addPort(port.name, port.dataType, port.type);
            connPortMap.put(port, clone.outputPorts.getLast());
        }
        
        for (DrawModule sub : submodules) {
            clone.addSubmodule(sub.cloneWithPortMap(connPortMap));
        }
        
        for (DrawConnection conn : connections) {
            clone.connections.add(new DrawConnection(clone, connPortMap.get(conn.sourcePort), connPortMap.get(conn.destPort)));
        }
        
        return clone;
    }
}
