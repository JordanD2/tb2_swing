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
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

/**
 *
 * @author jordan
 */
public class ModulePopup extends JPopupMenu{
    
    final static int DUPLICATE_TRANSLATION = 20;
    
    JFrame frame;
    Canvas canvas;
    Point clickLocation;
    
    JMenuItem pasteItem;
    
    public ModulePopup() {
        
        this.add("Edit config fields").addActionListener((e) -> {
            new ConfigFieldEditor(frame, canvas.selected).initialize().display();
        });
        
        this.add("Assign config fields").addActionListener((e) -> {
            new ConfigAssignEditor(frame, canvas.selected).initialize().display();
        });
        
        this.add("Add Port").addActionListener((e) -> {
            NewPortDialog popup = new NewPortDialog(frame, true);
            popup.canvas = canvas;
            popup.moduleTarget = canvas.selected;
            popup.setVisible(true);
            popup.setLocation(frame.getX() + frame.getWidth()/2 - popup.getWidth()/2, frame.getY() + frame.getHeight()/2 - popup.getHeight()/2);
        });
                
        JMenu addUvm = new JMenu("Add Submodule...");
        addUvm.add("Custom").addActionListener((e) -> {
            DrawModule newSubModule = new DrawModule(
                    canvas,
                    "submodule",
                    "uvm_component",
                    new RealPoint(clickLocation).inverseScaleBy(canvas.scaleFactor).subtract(canvas.selected.getModuleOffset()).add(canvas.offset)
            );
            canvas.selected.addSubmodule(newSubModule);
            canvas.selected.resize();
            canvas.repaint();
        });
        addUvm.add("Agent").addActionListener((e) -> {
            DrawModule agt = new DrawModule(canvas, "agent", "uvm_agent", new RealPoint(clickLocation).inverseScaleBy(canvas.scaleFactor).subtract(canvas.selected.getModuleOffset()).add(canvas.offset));
            agt.addPort("mon_port", "uvm_object", DrawPort.OUTPUT_PORT);
            DrawModule mon = new DrawModule(canvas, "mon", "uvm_monitor", new RealPoint(0,0));
            mon.addPort("mon_port", "uvm_object", DrawPort.OUTPUT_PORT);
            agt.connections.add(new DrawConnection(agt, mon.outputPorts.get(0), agt.outputPorts.get(0)));
            agt.hideInternals = true;
            
            agt.addSubmodule(mon);
            canvas.selected.addSubmodule(agt);
            canvas.selected.resize();
            canvas.repaint();
        });
        addUvm.add("Monitor").addActionListener((e) -> {
            DrawModule mon = new DrawModule(
                    canvas,
                    "monitor",
                    "uvm_monitor",
                    new RealPoint(clickLocation).inverseScaleBy(canvas.scaleFactor).subtract(canvas.selected.getModuleOffset()).add(canvas.offset)
            );
            mon.addPort("seen_port", "uvm_object", DrawPort.OUTPUT_PORT);
            canvas.selected.addSubmodule(mon);
            canvas.selected.resize();
            canvas.repaint();
        });
        addUvm.add("Subscriber").addActionListener((e) -> {
            DrawModule sbcrbr = new DrawModule(
                    canvas,
                    "subscriber",
                    "uvm_subscriber",
                    new RealPoint(clickLocation).inverseScaleBy(canvas.scaleFactor).subtract(canvas.selected.getModuleOffset()).add(canvas.offset)
            );
            sbcrbr.addPort("analysis_export", "uvm_object", DrawPort.INPUT_PORT);
            canvas.selected.addSubmodule(sbcrbr);
            canvas.selected.resize();
            canvas.repaint();
        });
        addUvm.add("Filter").addActionListener((e) -> {
            DrawModule fil = new DrawModule(
                    canvas,
                    "filter",
                    "tlm_filter",
                    new RealPoint(clickLocation).inverseScaleBy(canvas.scaleFactor).subtract(canvas.selected.getModuleOffset()).add(canvas.offset)
            );
            fil.addPort("analysis_export", "uvm_object", DrawPort.INPUT_PORT);
            fil.addPort("filtered_port", "uvm_object", DrawPort.OUTPUT_PORT);
            canvas.selected.addSubmodule(fil);
            canvas.selected.resize();
            canvas.repaint();
        });
        addUvm.add("Translator").addActionListener((e) -> {
            DrawModule fil = new DrawModule(
                    canvas,
                    "translator",
                    "tlm_translator",
                    new RealPoint(clickLocation).inverseScaleBy(canvas.scaleFactor).subtract(canvas.selected.getModuleOffset()).add(canvas.offset)
            );
            fil.addPort("analysis_export", "uvm_object", DrawPort.INPUT_PORT);
            fil.addPort("output_port", "uvm_object", DrawPort.OUTPUT_PORT);
            canvas.selected.addSubmodule(fil);
            canvas.selected.resize();
            canvas.repaint();
        });
        addUvm.add("Scoreboard").addActionListener((e) -> {
            DrawModule sb = new DrawModule(
                    canvas,
                    "scoreboard",
                    "uvm_sb",
                    new RealPoint(clickLocation).inverseScaleBy(canvas.scaleFactor).subtract(canvas.selected.getModuleOffset()).add(canvas.offset)
            );
            sb.addPort("obs_export", "uvm_object", DrawPort.INPUT_PORT);
            sb.addPort("exp_export", "uvm_object", DrawPort.INPUT_PORT);
            canvas.selected.addSubmodule(sb);
            canvas.selected.resize();
            canvas.repaint();
        });
        this.add(addUvm);
        
        this.addSeparator();
        
        this.add("Rename").addActionListener((e) -> {
            RenameModuleDialog popup = new RenameModuleDialog(frame, true);
            popup.canvas = canvas;
            popup.target = canvas.selected;
            popup.nameField.setText(canvas.selected.name);
            popup.typeField.setText(canvas.selected.type);
            popup.setVisible(true);
            popup.setLocation(frame.getX() + frame.getWidth()/2 - popup.getWidth()/2, frame.getY() + frame.getHeight()/2 - popup.getHeight()/2);
        });
        
        this.add("Resize").addActionListener((e) -> {
            canvas.selected.resize();
            canvas.repaint();
        });
        
        this.add("Auto-layout").addActionListener((e) -> {
            canvas.selected.layout();
            canvas.selected.resize();
            canvas.repaint();
        });
        
        this.add("Validate").addActionListener((e) -> {
            if (canvas.selected.validate()) {
                System.out.println("Validation passed!");
            } else {
                System.err.println("Validation failed!");
            }
            
        });
        
        this.addSeparator();
        
        JMenuItem dupeItem = this.add("Duplicate");
        dupeItem.setAccelerator(KeyStroke.getKeyStroke('D', Canvas.EDIT_MODIFIER_MASK));
        dupeItem.addActionListener((e) -> {
            try {
                DrawModule duplicated = (DrawModule)canvas.selected.clone();
                duplicated.location.translate(DUPLICATE_TRANSLATION, DUPLICATE_TRANSLATION);
                canvas.addModule(duplicated, canvas.selected);
                canvas.repaint();
            } catch (CloneNotSupportedException ex) {
                System.getLogger(ModulePopup.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        });
        
        JMenuItem cutItem = this.add("Cut");
        cutItem.setAccelerator(KeyStroke.getKeyStroke('X', Canvas.EDIT_MODIFIER_MASK));
        cutItem.addActionListener((e) -> {
            canvas.pasteBin = canvas.selected;
            canvas.removeModule(canvas.selected);
            canvas.selected = null;
            canvas.repaint();
        });
        
        JMenuItem copyItem = this.add("Copy");
        copyItem.setAccelerator(KeyStroke.getKeyStroke('C', Canvas.EDIT_MODIFIER_MASK));
        copyItem.addActionListener((e) -> {
            canvas.pasteBin = canvas.selected;
        });
        
        pasteItem = this.add("Paste");
        pasteItem.setAccelerator(KeyStroke.getKeyStroke('V', Canvas.EDIT_MODIFIER_MASK));
        pasteItem.addActionListener((e) -> {
            if (canvas.pasteBin != null) {
                try {
                    DrawModule pasted = (DrawModule) canvas.pasteBin.clone();
                    final RealPoint pastedLoc = new RealPoint(clickLocation).inverseScaleBy(canvas.scaleFactor).subtract(canvas.selected.getModuleOffset()).add(canvas.offset);
                    pasted.location.setLocation(pastedLoc.x, pastedLoc.y); 
                    canvas.addModule(pasted, canvas.selected);
                } catch (CloneNotSupportedException ex) {
                    System.getLogger(ModulePopup.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                canvas.repaint();
            }
        });
        
        JMenuItem deleteItem = this.add("Delete");
        deleteItem.setAccelerator(KeyStroke.getKeyStroke("DELETE"));
        deleteItem.addActionListener((e) -> {
            canvas.removeModule(canvas.selected);
            canvas.selected = null;
            canvas.repaint();
        });
        
        this.addSeparator();
        
        this.add("Send to Back").addActionListener((e) -> {
            canvas.bringModuleToBack(canvas.selected);
            canvas.repaint();
        });
        
        this.add("Bring to Front").addActionListener((e) -> {
            canvas.bringModuleToFront(canvas.selected);
            canvas.repaint();
        });
        
        this.addSeparator();
        
//        if (canvas.selected.hidePortLabels) {
            this.add("Show Port Labels").addActionListener((e) -> {
                canvas.selected.hidePortLabels = false;
                canvas.repaint();
            });
//        } else {
            this.add("Hide Port Labels").addActionListener((e) -> {
                canvas.selected.hidePortLabels = true;
                canvas.repaint();
            });
//        }

        this.addSeparator();
        
//        if (canvas.selected.hideInternals) {
            this.add("Show Internals").addActionListener((e) -> {
                canvas.selected.hideInternals = false;
                canvas.selected.resize();
                canvas.repaint();
            }); 
//        } else {
            this.add("Hide Internals").addActionListener((e) -> {
                canvas.selected.hideInternals = true;
                canvas.selected.resize();
                canvas.repaint();
            }); 
//        }   
    }
    
}
