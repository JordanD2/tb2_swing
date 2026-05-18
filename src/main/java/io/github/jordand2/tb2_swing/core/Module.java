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

package io.github.jordand2.tb2_swing.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jordan
 */
public class Module implements Serializable {
    
    public String pkg;
    public String name;
    public String type;
    public String superType;
    public String longComment;
    public String template = "uvm_component.jte";
    public ArrayList<ConfigField> configFields = new ArrayList<>();
    public ArrayList<ConfigAssign> configAssigns = new ArrayList<>();
    public ArrayList<Port> inputPorts = new ArrayList<>();
    public ArrayList<Port> outputPorts = new ArrayList<>();
    public ArrayList<Module> submodules = new ArrayList<>();
    public Map<Module, String> submoduleComments = new HashMap<>();
    public Map<Module, ArrayList<ConfigAssign>> submoduleConfigAssigns = new HashMap<>();
    public ArrayList<Connection> connections = new ArrayList<>();
    
    public Module() {
        
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setInputPorts(ArrayList<Port> inputPorts) {
        this.inputPorts = inputPorts;
    }

    public ArrayList<Port> getInputPorts() {
        return inputPorts;
    }

    public void setOutputPorts(ArrayList<Port> outputPorts) {
        this.outputPorts = outputPorts;
    }

    public ArrayList<Port> getOutputPorts() {
        return outputPorts;
    }

    public void setSubmodules(ArrayList<Module> submodules) {
        this.submodules = submodules;
    }

    public ArrayList<Module> getSubmodules() {
        return submodules;
    }

    public void setConnections(ArrayList<Connection> connections) {
        this.connections = connections;
    }

    public ArrayList<Connection> getConnections() {
        return connections;
    }

    @Override
    public String toString() {
        return String.format("{name : %s; type : %s; inputPorts : %s; outputPorts : %s; submodules : %s; connections : %s}",
                name, type, inputPorts.toString(), outputPorts.toString(), submodules.toString(), connections.toString());
    }
    
    public void getAllSubmodules(List<Module> output, boolean recursive) {
        output.addAll(submodules);
        if (recursive) {
            for (Module sub : submodules) {
                sub.getAllSubmodules(output, recursive);
            }
        }
    }
}
