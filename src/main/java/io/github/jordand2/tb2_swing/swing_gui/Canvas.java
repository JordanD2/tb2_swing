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
import javax.swing.KeyStroke;
import io.github.jordand2.tb2_swing.core.Module;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author jordan
 */
public class Canvas extends ViewportPanel {
    
    final static int MOUSE_SELECT_BUTTON = MouseEvent.BUTTON1;
    final static int MOUSE_NAV_BUTTON    = MouseEvent.BUTTON2;
    final static int MOUSE_OPTION_BUTTON = MouseEvent.BUTTON3;
    
    final static int NAV_STEP = 8;
    final static int NAV_UPDATE_PERIOD_MS = 16;  // 16ms -> 60fps (Now we're gaming!)
    
    final static int EDIT_MODIFIER_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Canvas.class.getName());
    
    protected final Deque<DrawModule> modules = new ArrayDeque<>();
    
    protected final ModulePopup modulePopup;
    protected final CanvasPopup canvasPopup;
    protected final PortPopup portPopup;
    
    protected final Stack<Modification> undoStack = new Stack<>();
    protected final Stack<Modification> redoStack = new Stack<>();
    
    protected boolean navUp;
    protected boolean navDown;
    protected boolean navLeft;
    protected boolean navRight;
    
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
                    removeModule(selected);
                    selected = null;
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
                    removeModule(selected);
                    selected = null;
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
                        addModule(pasted);
                        moveModule(pasted, pasted.parent);
                        pasteBin = pasted;
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
                        addModule(dupe, selected.parent);
                        selected = dupe;
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
    
    public void applyModification(Modification mod) {
        mod.apply();
        undoStack.push(mod);
        redoStack.clear();
        modified = true;
        repaint();
    }
    
    public void undo() {
        if (!undoStack.empty()) {
            Modification mod = undoStack.pop();
            mod.revert();
            redoStack.push(mod);
            modified = true;
            repaint();
        }
    }
    
    public void redo() {
        if (!redoStack.isEmpty()) {
            Modification mod = redoStack.pop();
            mod.apply();
            undoStack.push(mod);
            modified = true;
            repaint();
        }
    }
    
    /**
     * Adds a module to this Canvas
     * 
     * @param module The module to be added
     */
    public void addModule(DrawModule module) {
        applyModification(new AddModule(this, module, null));
    }
    
    // 
    /**
     * Adds a module to the specified parent module. If parent is null, then
     * the module will be added to the canvas instead.
     * 
     * @param module The module to be added
     * @param parent The parent under which the module will be added
     */
    public void addModule(DrawModule module, DrawModule parent) {
        applyModification(new AddModule(this, module, parent));
    }
    
    /**
     * Removes a module.
     * 
     * @param module The module that will be removed.
     */
    public void removeModule(DrawModule module) {
        applyModification(new RemoveModule(this, module));
    }
    
    /**
     * Moves a module from its current context to another
     * 
     * @param module The module that will be moved
     * @param parent The parent under which the module will be moved
     */
    public void moveModule(DrawModule module, DrawModule parent) {
        applyModification(new MoveModule(this, module, parent));
    }
    
    public void bringModuleToFront(DrawModule module) {
        if (selected.isTopModule()) {
            modules.remove(selected);
            modules.addLast(selected);
        } else {
            selected.parent.submodules.remove(selected);
            selected.parent.submodules.addLast(selected);
        }
        modified = true;
    }
    
    public void bringModuleToBack(DrawModule module) {
        if (selected.isTopModule()) {
            modules.remove(selected);
            modules.addFirst(selected);
        } else {
            selected.parent.submodules.remove(selected);
            selected.parent.submodules.addFirst(selected);
        }
        modified = true;
    }
    
    @Override
    public void editModule(DrawModule module, String newName, String newType) {
        applyModification(new EditModule(this, module, newName, newType));
    }
    

    public boolean validateModules() {
        return modules.stream().allMatch((t) -> t.validate());
    }
    
    @Override
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
    
    public void handleMouseWheelEvent(MouseWheelEvent evt){
        logger.log(Level.INFO, "Scroll {0} @ ({1}, {2})", new Object[]{evt.getUnitsToScroll(), evt.getX(), evt.getY()});
        handleZoom(evt.getUnitsToScroll(), evt.getX(), evt.getY());
        repaint();
        logger.log(Level.INFO, "{0} @ ({1},{2})", new Object[]{String.valueOf(evt.getUnitsToScroll()), evt.getX(), evt.getY()});
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
                selected.location = selected.getModuleOffset().subtract(hoverModule.getModuleOffset());
                // FIXME jordand : needs synchronization still?
                moveModule(selected, hoverModule);
                hoverModule.resize();
            }
            if (hoverPort != null && selectedPort != null && hoverPort != selectedPort) {
                // TODO: simplify and/or add error checking
                if (selectedPort.parent == hoverPort.parent) {
                    applyModification(new AddConnection(this, selectedPort.parent, selectedPort, hoverPort));
                } else if (selectedPort.parent == hoverPort.parent.parent) {
                    applyModification(new AddConnection(this, selectedPort.parent, selectedPort, hoverPort));
                } else if (selectedPort.parent.parent == hoverPort.parent) {
                    applyModification(new AddConnection(this, hoverPort.parent, selectedPort, hoverPort));
                } else if (hoverPort.parent.parent != null && selectedPort.parent.parent != null && selectedPort.parent.parent == hoverPort.parent.parent) {
                    applyModification(new AddConnection(this, selectedPort.parent.parent, selectedPort, hoverPort));
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
}
