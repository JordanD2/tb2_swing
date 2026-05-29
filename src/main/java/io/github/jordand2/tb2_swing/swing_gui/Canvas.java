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

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import io.github.jordand2.tb2_swing.core.Module;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author jordan
 */
public class Canvas extends JPanel{
    
    final static int MOUSE_SELECT_BUTTON = MouseEvent.BUTTON1;
    final static int MOUSE_NAV_BUTTON    = MouseEvent.BUTTON2;
    final static int MOUSE_OPTION_BUTTON = MouseEvent.BUTTON3;
    
    final static float DEFAULT_FONT_SIZE = 12.0f;
    final static double DEFAULT_SCALE_FACTOR = 1.0;
    final static double SCALE_STEP = 1.2;
    final static double MAX_SCALE_FACTOR = 20.0;
    final static double MIN_SCALE_FACTOR = 1.0/MAX_SCALE_FACTOR;
    final static int KEYBOARD_ZOOM_STEP = 3;
    
    final static int NAV_STEP = 8;
    final static int NAV_UPDATE_PERIOD_MS = 16;  // 16ms -> 60fps (Now we're gaming!)
    
    final static int EDIT_MODIFIER_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Tb2Swing.class.getName());
    
    boolean modified = false;
    
    final Deque<DrawModule> modules = new ArrayDeque<>();
    
    DrawModule selected = null;
    DrawModule hoverModule = null;
    DrawModule pasteBin = null;
    DrawPort selectedPort = null;
    DrawPort hoverPort = null;
    
    final RealPoint offset = new RealPoint(0, 0);
        
    // Use to turn canvas points into screen points
    double scaleFactor = DEFAULT_SCALE_FACTOR;
    
    final ModulePopup modulePopup;
    final CanvasPopup canvasPopup;
    final PortPopup portPopup;
    
    boolean navUp;
    boolean navDown;
    boolean navLeft;
    boolean navRight;
    
    public Canvas() {
        super();
        InputMap im = this.getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getActionMap();
        
        im.put(KeyStroke.getKeyStroke("pressed UP"), "upStart");
        am.put("upStart", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navUp = true;
            }
        });
        
        im.put(KeyStroke.getKeyStroke("released UP"), "upStop");
        am.put("upStop", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navUp = false;
            }
        });
        
        im.put(KeyStroke.getKeyStroke("pressed DOWN"), "downStart");
        am.put("downStart", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navDown = true;
            }
        });
        
        im.put(KeyStroke.getKeyStroke("released DOWN"), "downStop");
        am.put("downStop", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navDown = false;
            }
        });
        
        im.put(KeyStroke.getKeyStroke("pressed LEFT"), "leftStart");
        am.put("leftStart", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navLeft = true;
            }
        });
        
        im.put(KeyStroke.getKeyStroke("released LEFT"), "leftStop");
        am.put("leftStop", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navLeft = false;
            }
        });
        
        im.put(KeyStroke.getKeyStroke("pressed RIGHT"), "rightStart");
        am.put("rightStart", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navRight = true;
            }
        });
        
        im.put(KeyStroke.getKeyStroke("released RIGHT"), "rightStop");
        am.put("rightStop", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navRight = false;
            }
        });
        
        im.put(KeyStroke.getKeyStroke("O"), "out");
        am.put("out", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleZoom(KEYBOARD_ZOOM_STEP, getWidth()/2, getHeight()/2);
                repaint();
            }
        });
        
        im.put(KeyStroke.getKeyStroke("I"), "in");
        am.put("in", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleZoom(-KEYBOARD_ZOOM_STEP, getWidth()/2, getHeight()/2);
                repaint();
            }
        });
        
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        am.put("escape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selected = null;
                selectedPort = null;
                repaint();
            }
        });
        
        
        im.put(KeyStroke.getKeyStroke("DELETE"), "delete");
        am.put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selected != null) {
                    if (selected.isTopModule()) {
                        modules.remove(selected);
                        selected = null;
                    } else {
                        selected.parent.deleteSubmodule(selected);
                    }
                    modules.remove(selected);
                    selected = null;
                    modified = true;
                    repaint();
                }
            }
        });
        
        im.put(KeyStroke.getKeyStroke('X', EDIT_MODIFIER_MASK), "cut");
        am.put("cut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selected != null) {
                    pasteBin = selected;
                    
                    if (selected.isTopModule()) {
                        modules.remove(selected);
                    } else {
                        selected.parent.deleteSubmodule(selected);
                    }
                    selected = null;
                    modified = true;
                    repaint();
                }
            }
        });
        
        im.put(KeyStroke.getKeyStroke('C', EDIT_MODIFIER_MASK), "copy");
        am.put("copy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selected != null) {
                    pasteBin = selected;
                }
            }
        });
        
        im.put(KeyStroke.getKeyStroke('V', EDIT_MODIFIER_MASK), "paste");
        am.put("paste", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pasteBin != null) {
                    try {
                        DrawModule pasted = (DrawModule) pasteBin.clone();
                        pasted.location.translate(ModulePopup.DUPLICATE_TRANSLATION, ModulePopup.DUPLICATE_TRANSLATION);
                        if (pasteBin.isTopModule()) {
                            modules.add(pasted);
                        } else {
                            pasteBin.parent.addSubmodule(pasted);
                        }
                        pasteBin = pasted;
                        modified = true;
                    } catch (CloneNotSupportedException ex) {
                        System.getLogger(ModulePopup.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                    repaint();
                }
            }
        });
        
        im.put(KeyStroke.getKeyStroke('D', EDIT_MODIFIER_MASK), "duplicate");
        am.put("duplicate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selected != null) {
                    try {
                        DrawModule dupe = (DrawModule) selected.clone();
                        dupe.location.translate(ModulePopup.DUPLICATE_TRANSLATION, ModulePopup.DUPLICATE_TRANSLATION);
                        if (selected.isTopModule()) {
                            modules.add(dupe);
                        } else {
                            selected.parent.addSubmodule(dupe);
                        }
                        selected = dupe;
                        modified = true;
                    } catch (CloneNotSupportedException ex) {
                        System.getLogger(ModulePopup.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }
                repaint();
            }
        });
        
        modulePopup = new ModulePopup();
        this.add(modulePopup);
        
        canvasPopup = new CanvasPopup();
        this.add(canvasPopup);
        
        portPopup = new PortPopup();
        this.add(portPopup);
        
        // Setup NavThread for smooth keyboard navigation
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            boolean needsUpdate = false;
            if (navUp && !navDown) {
                offset.y -= NAV_STEP/scaleFactor;
                needsUpdate = true;
            } else if (navDown && !navUp) {
                offset.y += NAV_STEP/scaleFactor;
                needsUpdate = true;
            }

            if (navLeft && !navRight) {
                offset.x -= NAV_STEP/scaleFactor;
                needsUpdate = true;
            } else if (navRight && !navLeft) {
                offset.x += NAV_STEP/scaleFactor;
                needsUpdate = true;
            }

            if (needsUpdate) {
                repaint();
            }
        }, 0, NAV_UPDATE_PERIOD_MS, TimeUnit.MILLISECONDS);
    }
    
    public void loadModule(Module module) {
        DrawModule mod = new DrawModule(this, (DrawModule)(null), module);
        modules.add(mod);
        mod.reindexPorts(true);
        if (modules.size() == 1) {
            modified = false; // No modification if we've just loaded a module
        }
        mod.layout();
        mod.resize();
        mod.location.x = getWidth ()*scaleFactor/2 - mod.size.x/2;
        mod.location.y = getHeight()*scaleFactor/2 - mod.size.y/2;
        repaint();
    }
    
    public Module getTopAsModule() {
        if (modules.isEmpty()) {
            return null;
        }
        
        if (modules.size() > 1) {
            DrawModule top = new DrawModule(this, "top", new RealPoint(0, 0));
            for (DrawModule mod : modules) {
                top.addSubmodule(mod);
            }
            modules.clear();
            modules.add(top);
        }
        
        return modules.getFirst().getAsModule();
    }
    
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
    
    public void handleMouseWheelEvent(MouseWheelEvent evt){

        logger.log(Level.INFO, "Scroll {0} @ ({1}, {2})", new Object[]{evt.getUnitsToScroll(), evt.getX(), evt.getY()});
        
        handleZoom(evt.getUnitsToScroll(), evt.getX(), evt.getY());

        repaint();
            
        logger.log(Level.INFO, "{0} @ ({1},{2})", new Object[]{String.valueOf(evt.getUnitsToScroll()), evt.getX(), evt.getY()});
    }
    
    public void handleZoom(int unitsToScroll, Point center) {
        RealPoint evtLoc = new RealPoint(center).inverseScaleBy(scaleFactor);
        
        for (int i = 0; i < Math.abs(unitsToScroll); i++) {
            if (unitsToScroll > 0){
                logger.log(Level.INFO, "Zoom Out ({0})!", new Object[]{scaleFactor});
                scaleFactor = Math.max(MIN_SCALE_FACTOR, scaleFactor / SCALE_STEP);
            } else {
                logger.log(Level.INFO, "Zoom In ({0})!", new Object[]{scaleFactor});
                scaleFactor = Math.min(MAX_SCALE_FACTOR, scaleFactor * SCALE_STEP);
            }
        }
        
        RealPoint postZoomLoc = new RealPoint(center).inverseScaleBy(scaleFactor);
        
        offset.add(evtLoc.subtract(postZoomLoc));
    }

    public void handleZoom(int unitsToScroll, int x, int y) {
        RealPoint evtLoc = new RealPoint(x,y).inverseScaleBy(scaleFactor);
        
        for (int i = 0; i < Math.abs(unitsToScroll); i++) {
            if (unitsToScroll > 0){
                logger.log(Level.INFO, "Zoom Out ({0})!", new Object[]{scaleFactor});
                scaleFactor = Math.max(MIN_SCALE_FACTOR, scaleFactor / SCALE_STEP);
            } else {
                logger.log(Level.INFO, "Zoom In ({0})!", new Object[]{scaleFactor});
                scaleFactor = Math.min(MAX_SCALE_FACTOR, scaleFactor * SCALE_STEP);
            }
        }
        
        RealPoint postZoomLoc = new RealPoint(x,y).inverseScaleBy(scaleFactor);
        
        offset.add(evtLoc.subtract(postZoomLoc));
    }
    
    public void handleMousePressedEvent(MouseEvent evt) {
        if (evt.getButton() == MOUSE_NAV_BUTTON){
            lastMouseDrag = evt.getPoint();
        } else if (evt.getButton() == MOUSE_SELECT_BUTTON) {
            lastMouseDrag = evt.getPoint();
            
            selectedPort = getClickedPort(evt);
            if (selectedPort == null) {
                selected = getClickedModule(evt);
            } else {
                selected = null;
            }
            
            repaint();
        }
    }
    
    public void handleMouseReleasedEvent(MouseEvent evt) {
        if (evt.getButton() == MOUSE_SELECT_BUTTON) {
            if (hoverModule != null && selected != null && hoverModule != selected && selected.parent != hoverModule) {
                
                if (selected.isTopModule()) {
                    synchronized (modules) {
                        modules.remove(selected);
                    }
                } else {
                    synchronized (selected.parent.submodules) {
                        selected.parent.submodules.remove(selected);
                    }
                }
                modified = true;
                
                selected.location = selected.getModuleOffset().subtract(hoverModule.getModuleOffset());
                hoverModule.addSubmodule(selected);
                hoverModule.resize();
            }
            if (hoverPort != null && selectedPort != null && hoverPort != selectedPort) {
                // TODO: simplify and/or add error checking
                if (selectedPort.parent == hoverPort.parent) {
                    selectedPort.parent.connections.add(new DrawConnection(this, selectedPort.parent, selectedPort, hoverPort));
                    modified = true;
                } else if (selectedPort.parent == hoverPort.parent.parent) {
                    selectedPort.parent.connections.add(new DrawConnection(this, selectedPort.parent, selectedPort, hoverPort));
                    modified = true;
                } else if (selectedPort.parent.parent == hoverPort.parent) {
                    hoverPort.parent.connections.add(new DrawConnection(this, hoverPort.parent, selectedPort, hoverPort));
                    modified = true;
                } else if (hoverPort.parent.parent != null && selectedPort.parent.parent != null && selectedPort.parent.parent == hoverPort.parent.parent) {
                    selectedPort.parent.parent.connections.add(new DrawConnection(this, selectedPort.parent.parent, selectedPort, hoverPort));
                    modified = true;
                } else {
                    logger.log(Level.SEVERE, "Attempted to add bad connection!");
                }
                logger.log(Level.INFO, "Connection Added");
            }
        }
        
        lastMouseDrag = null;
        selectedPort = null;
        hoverPort = null;
        hoverModule = null;
        repaint();
    }
    
    Point lastMouseDrag;
    
    public void handleMouseDraggedEvent(MouseEvent evt) {
        Point newLoc = evt.getPoint();
        if (evt.getButton() == MOUSE_NAV_BUTTON){
            offset.x += (lastMouseDrag.getX() - newLoc.getX()) / scaleFactor;
            offset.y += (lastMouseDrag.getY() - newLoc.getY()) / scaleFactor;

            lastMouseDrag = newLoc;
            repaint();
        } else if (evt.getButton() == MOUSE_SELECT_BUTTON) {
            if (selected != null) {
                selected.location.x -= (lastMouseDrag.getX() - newLoc.getX()) / scaleFactor;
                selected.location.y -= (lastMouseDrag.getY() - newLoc.getY()) / scaleFactor;
                lastMouseDrag = newLoc;
                hoverModule = getClickedModule(evt);
                repaint();
            }
            if (selectedPort != null) {
                lastMouseDrag = newLoc;
                hoverPort = getClickedPort(evt);
                repaint();
            }
        }
    }
    
    public void handleMouseClickEvent(MouseEvent evt) {
        if (evt.getButton() == MOUSE_OPTION_BUTTON) {
            selectedPort = getClickedPort(evt);
            
            if (selectedPort == null) {
                selected = getClickedModule(evt);
                if (selected == null) {
                    // clicked canvas (background)
                    canvasPopup.parent = this;
                    canvasPopup.clickLocation = evt.getPoint();
                    canvasPopup.show(this, evt.getX(), evt.getY());
                } else{
                    // clicked a module (or submodule)
                    modulePopup.frame = (JFrame)this.getTopLevelAncestor();
                    modulePopup.canvas = this;
                    modulePopup.pasteItem.setEnabled(pasteBin != null);
                    modulePopup.clickLocation = evt.getPoint();
                    modulePopup.show(this, evt.getX(), evt.getY());
                }
            } else {
                // clicked a port
                portPopup.canvas = this;
                portPopup.frame = (JFrame)this.getTopLevelAncestor();
                portPopup.show(this, evt.getX(), evt.getY());
            }
            
            repaint();
        }
    }
    
    public DrawModule getClickedModule (MouseEvent evt) {
        for (Iterator<DrawModule> iterator = modules.descendingIterator(); iterator.hasNext();) {
            DrawModule module = iterator.next();
            if (hoverModule != null && module == selected) {
                continue;
            }
            DrawModule clicked = module.getClickedModule(evt);
            if (clicked != null) {
                return clicked;
            }
        }
        return null;
    }
    
    public DrawPort getClickedPort (MouseEvent evt) {
        for (Iterator<DrawModule> iterator = modules.descendingIterator(); iterator.hasNext();) {
            DrawModule module = iterator.next();
            DrawPort clicked = module.getClickedPort(evt);
            if (clicked != null) {
                return clicked;
            } 
        }
        return null;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Font ogFont = g.getFont();
        
        g.setFont(ogFont.deriveFont(DEFAULT_FONT_SIZE * (float)scaleFactor));
        
        // Draw each module
        for (DrawModule module : modules) {
            module.draw(g);
        }
        
        // Draw drag lines
        if (selectedPort != null && lastMouseDrag != null) {
            Point portLoc = selectedPort.getScreenCenter();
            g.drawLine(portLoc.x, portLoc.y, lastMouseDrag.x, lastMouseDrag.y);
            
            fillLineArrow(g, portLoc.x, portLoc.y, lastMouseDrag.x, lastMouseDrag.y);
        }
        
        g.setFont(ogFont);
    }
    
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
