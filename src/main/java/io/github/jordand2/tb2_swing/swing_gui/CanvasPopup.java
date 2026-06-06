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
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

/**
 *
 * @author jordan
 */
public class CanvasPopup extends JPopupMenu{
    
    Canvas parent;
    Point clickLocation;
    
    public CanvasPopup() {
                
        this.add("Add").addActionListener((e) -> {
            DrawModule newModule = new DrawModule(
                    parent,
                    "module",
                    "uvm_component",
                    new RealPoint(clickLocation).inverseScaleBy(parent.scaleFactor).add(parent.offset)
            );
            parent.addModule(newModule);
            newModule.resize();
            parent.repaint();
        });
        
        JMenu addUvm = new JMenu("Add UVM...");
        addUvm.add("Monitor").addActionListener((e) -> {
            DrawModule mon = new DrawModule(
                    parent,
                    "mon",
                    new RealPoint(clickLocation).inverseScaleBy(parent.scaleFactor).add(parent.offset)
            );
            mon.addPort("seen_port", "uvm_object", DrawPort.OUTPUT_PORT);
            parent.addModule(mon);
            mon.resize();
            parent.repaint();
        });
        addUvm.add("Subscriber").addActionListener((e) -> {
            DrawModule sbcrbr = new DrawModule(
                    parent,
                    "subscriber",
                    new RealPoint(clickLocation).inverseScaleBy(parent.scaleFactor).add(parent.offset)
            );
            sbcrbr.addPort("analysis_export", "uvm_object", DrawPort.INPUT_PORT);
            parent.addModule(sbcrbr);
            sbcrbr.resize();
            parent.repaint();
        });
        addUvm.add("Filter").addActionListener((e) -> {
            DrawModule fil = new DrawModule(
                    parent,
                    "filter",
                    new RealPoint(clickLocation).inverseScaleBy(parent.scaleFactor).add(parent.offset)
            );
            fil.addPort("analysis_export", "uvm_object", DrawPort.INPUT_PORT);
            fil.addPort("filtered_port", "uvm_object", DrawPort.OUTPUT_PORT);
            parent.addModule(fil);
            fil.resize();
            parent.repaint();
        });
        addUvm.add("Translator").addActionListener((e) -> {
            DrawModule fil = new DrawModule(
                    parent,
                    "translator",
                    new RealPoint(clickLocation).inverseScaleBy(parent.scaleFactor).add(parent.offset)
            );
            fil.addPort("analysis_export", "uvm_object", DrawPort.INPUT_PORT);
            fil.addPort("output_port", "uvm_object", DrawPort.OUTPUT_PORT);
            parent.addModule(fil);
            fil.resize();
            parent.repaint();
        });
        addUvm.add("Scoreboard").addActionListener((e) -> {
            DrawModule sb = new DrawModule(
                    parent,
                    "sb",
                    new RealPoint(clickLocation).inverseScaleBy(parent.scaleFactor).add(parent.offset)
            );
            sb.addPort("obs_export", "uvm_object", DrawPort.INPUT_PORT);
            sb.addPort("exp_export", "uvm_object", DrawPort.INPUT_PORT);
            parent.addModule(sb);
            sb.resize();
            parent.repaint();
        });
        
        this.add(addUvm);
    }
    
}
