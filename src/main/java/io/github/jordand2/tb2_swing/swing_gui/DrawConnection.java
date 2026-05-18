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

import io.github.jordand2.tb2_swing.core.Connection;

/**
 *
 * @author jordan
 */
public class DrawConnection {
    
    Canvas canvas;
    DrawModule parent;
    
    DrawPort sourcePort;
    DrawPort destPort;

    public DrawConnection(Canvas canvas, DrawModule parent, DrawPort sourcePort, DrawPort destPort) {
        this.canvas = canvas;
        this.parent = parent;
        this.sourcePort = sourcePort;
        this.destPort = destPort;
    }
    
    public Connection getAsConnection() {
        return new Connection(parent.getPathTo(sourcePort), parent.getPathTo(destPort));
    }
    
    public boolean validate() {
        boolean valid = true;
  
        // Check 1: input/output mismatches
        if ((sourcePort.type == DrawPort.INPUT_PORT) && (destPort.type == DrawPort.OUTPUT_PORT)) {
            if (sourcePort.parent != destPort.parent) {
                System.err.println("Port I/O mismatch!");
                valid = false;
            }
        }
        
        // Check 1 : source/destination types must match
        if (!sourcePort.dataType.equals(destPort.dataType)) {
            System.err.println("Port dataType mismatch!");
            valid = false;
        }
        
        return valid;
    }
}
