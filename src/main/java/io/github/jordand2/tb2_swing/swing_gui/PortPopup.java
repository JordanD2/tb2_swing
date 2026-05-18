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

import javax.swing.JFrame;
import javax.swing.JPopupMenu;

/**
 *
 * @author jordan
 */
public class PortPopup extends JPopupMenu {
    
    JFrame frame;
    Canvas canvas;
    
    public PortPopup() {
        
        this.add("Modify").addActionListener((e) -> {
            NewPortDialog popup = new NewPortDialog(frame, true);
            popup.canvas = canvas;
            popup.moduleTarget = null;
            popup.portTarget = canvas.selectedPort;
            popup.usePortInfo(canvas.selectedPort);
            popup.setVisible(true);
        });
        
        this.add("Delete").addActionListener((e) -> {
            canvas.selectedPort.parent.deletePort(canvas.selectedPort);
            canvas.repaint();
        });
        
    }
}
