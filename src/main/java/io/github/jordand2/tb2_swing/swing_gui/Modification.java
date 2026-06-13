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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JordanD2
 */
public interface Modification {
    void apply();
    void revert();
}

abstract class CanvasModification implements Modification {
    final protected Canvas canvas;

    public CanvasModification(Canvas canvas) {
        this.canvas = canvas;
    }
}

class AddModule extends CanvasModification {
    final protected DrawModule module;
    final protected DrawModule parent;
    
    public AddModule(Canvas canvas, DrawModule mod, DrawModule parent) {
        super(canvas);
        this.module = mod;
        this.parent = parent;
    }

    @Override
    public void apply() {
        if (parent == null) {
            canvas.modules.add(module);
        } else {
            parent.submodules.add(module);
            module.parent = parent;
        }
    }
    
    @Override
    public void revert() {
        if (parent == null) {
            canvas.modules.remove(module);
        } else {
            parent.submodules.remove(module);
            module.parent = null;
        }
    }
}

class RemoveModule extends CanvasModification {
    final protected DrawModule module;
    final protected DrawModule parent;
    final protected List<DrawConnection> connList;
    
    public RemoveModule(Canvas canvas, DrawModule mod) {
        super(canvas);
        this.module = mod;
        parent = module.parent;
        connList = new ArrayList<>();
        connList.addAll(parent.connections.stream().filter((conn) -> conn.sourcePort.parent == module || conn.destPort.parent == module).toList());
    }

    @Override
    public void apply() {
        if (parent == null) {
            canvas.modules.remove(module);
        } else {
            parent.submodules.remove(module);
            parent.connections.removeAll(connList);
        }
    }
    
    @Override
    public void revert() {
        if (parent == null) {
            canvas.modules.add(module);
        } else {
            parent.submodules.add(module);
            parent.connections.addAll(connList);
        }
    }
}

// TODO : preserve location during undo
class MoveModule extends CanvasModification {
    final protected DrawModule module;
    final protected DrawModule oldParent;
    final protected DrawModule newParent;
    final protected List<DrawConnection> connList;

    public MoveModule(Canvas canvas, DrawModule module, DrawModule newParent) {
        super(canvas);
        this.module = module;
        this.newParent = newParent;
        oldParent = module.parent;
        connList = new ArrayList<>();
        if (oldParent != null) {
            synchronized (oldParent.connections) {
                connList.addAll(oldParent.connections.stream().filter((conn) -> conn.sourcePort.parent == module || conn.destPort.parent == module).toList());
            }
        }
    }

    @Override
    public void apply() {
        if (oldParent == null) {
            synchronized (canvas.modules) {
                canvas.modules.remove(module);
            }
        } else {
            synchronized (oldParent.submodules) {
                oldParent.submodules.remove(module);
            }
            synchronized (oldParent.connections) {
                oldParent.connections.removeAll(connList);
            }
        }
        synchronized (newParent.submodules) {
            newParent.submodules.add(module);
        }
        module.parent = newParent;
    }

    @Override
    public void revert() {
        synchronized (newParent.submodules) {
            newParent.submodules.remove(module);
        }
        
        if (oldParent == null) {
            synchronized (canvas.modules) {
                canvas.modules.add(module);
            }
        } else {
            synchronized (oldParent.submodules) {
                oldParent.submodules.add(module);
            }
            synchronized (oldParent.connections) {
                oldParent.connections.addAll(connList);
            }
        }
        module.parent = oldParent;
    }
}

class AddPort extends CanvasModification {
    final protected DrawModule module;
    final protected DrawPort port;

    public AddPort(Canvas canvas, DrawPort port) {
        super(canvas);
        module = port.parent;
        this.port = port;
    }

    @Override
    public void apply() {
        if (port.type == DrawPort.INPUT_PORT) {
            port.idx = module.inputPorts.size();
            module.inputPorts.add(port);
        } else {
            port.idx = module.outputPorts.size();
            module.outputPorts.add(port);
        }
    }

    @Override
    public void revert() {
        if (port.type == DrawPort.INPUT_PORT) {
            module.inputPorts.remove(port);
        } else {
            module.outputPorts.remove(port);
        }
    }
}

class RemovePort extends CanvasModification {
    
    final protected DrawModule module;
    final protected DrawPort port;
    final protected List<DrawConnection> conns;
    final protected List<DrawConnection> parentConns;
    
    public RemovePort(Canvas canvas, DrawModule module, DrawPort port) {
        super(canvas);
        this.module = module;
        this.port = port;
        conns = new ArrayList(module.connections.stream().filter((conn) -> conn.sourcePort == port || conn.destPort == port).toList());
        parentConns = new ArrayList<>();
        if (module.parent != null) {
            parentConns.addAll(module.parent.connections.stream().filter((conn) -> conn.sourcePort == port || conn.destPort == port).toList());
        }
    }

    @Override
    public void apply() {
        module.inputPorts.remove(port);
        module.outputPorts.remove(port);
        
        module.connections.removeAll(conns);
        if (module.parent != null) {
            module.parent.connections.removeAll(parentConns);
        }
    }

    @Override
    public void revert() {
        if (port.type == DrawPort.INPUT_PORT) {
            module.inputPorts.add(port);
        } else {
            module.outputPorts.add(port);
        }
        
        module.connections.addAll(conns);
        if (module.parent != null) {
            module.parent.connections.addAll(parentConns);
        }
    }
    
}

class AddConnection extends CanvasModification {
    final protected DrawModule module;
    final protected DrawPort srcPort, dstPort;
    
    final protected DrawConnection conn;

    public AddConnection(Canvas canvas, DrawModule module, DrawPort srcPort, DrawPort dstPort) {
        super(canvas);
        this.module = module;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        conn = new DrawConnection(module, srcPort, dstPort);
    }

    @Override
    public void apply() {
        module.connections.add(conn);
    }

    @Override
    public void revert() {
        module.connections.remove(conn);
    }
}

class EditModule extends CanvasModification {
    final protected DrawModule module;
    final protected String newName;
    final protected String newType;
    final protected String oldName;
    final protected String oldType;

    public EditModule(Canvas canvas, DrawModule module, String newName, String newType) {
        super(canvas);
        this.module = module;
        this.newName = newName;
        this.newType = newType;
        this.oldName = module.name;
        this.oldType = module.type;
    }

    @Override
    public void apply() {
        module.name = newName;
        module.type = newType;
    }

    @Override
    public void revert() {
        module.name = oldName;
        module.type = oldType;
    }
    
}
