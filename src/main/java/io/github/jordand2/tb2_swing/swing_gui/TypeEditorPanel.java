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
import io.github.jordand2.tb2_swing.core.Module;
import static io.github.jordand2.tb2_swing.swing_gui.ViewportPanel.MAX_SCALE_FACTOR;
import static io.github.jordand2.tb2_swing.swing_gui.ViewportPanel.MIN_SCALE_FACTOR;
import static io.github.jordand2.tb2_swing.swing_gui.ViewportPanel.SCALE_STEP;
import java.awt.event.ActionEvent;
import java.awt.event.MouseWheelEvent;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

/**
 *
 * @author JordanD2
 */
public class TypeEditorPanel extends ViewportPanel {
    
    protected DrawModule module;
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TypeEditorPanel.class.getName());

    public TypeEditorPanel() {
        super();
        InputMap im = this.getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getActionMap();
        
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
        
        this.addMouseWheelListener((e) -> {
            handleMouseWheelEvent(e);
        });
    }

    public void handleMouseWheelEvent(MouseWheelEvent evt){
        handleZoom(evt.getUnitsToScroll(), getWidth()/2, getHeight()/2);
        repaint();
    }
    
    public void handleZoom(int unitsToScroll, int x, int y) {
        for (int i = 0; i < Math.abs(unitsToScroll); i++) {
            if (unitsToScroll > 0){
                logger.log(Level.INFO, "Zoom Out ({0})!", new Object[]{scaleFactor});
                scaleFactor = Math.max(MIN_SCALE_FACTOR, scaleFactor / SCALE_STEP);
            } else {
                logger.log(Level.INFO, "Zoom In ({0})!", new Object[]{scaleFactor});
                scaleFactor = Math.min(MAX_SCALE_FACTOR, scaleFactor * SCALE_STEP);
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (module != null) {
            module.location.x = (getWidth ()/scaleFactor - module.size.x)/2.0;
            module.location.y = (getHeight()/scaleFactor - module.size.y)/2.0;
            module.draw(g);
        }
    }
    
    @Override
    public void loadModule(Module module) {
        DrawModule mod = new DrawModule(this, (DrawModule)(null), module);
        this.module = mod;
        mod.reindexPorts(true);
        mod.layout();
        mod.resize();
        mod.location.x = (getWidth ()/scaleFactor - mod.size.x)/2.0;
        mod.location.y = (getHeight()/scaleFactor - mod.size.y)/2.0;
        repaint();
    }
    
    
}
