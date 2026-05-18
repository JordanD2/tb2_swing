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

package io.github.jordand2.tb2_swing.yamlio;

import io.github.jordand2.tb2_swing.core.ConfigField;
import io.github.jordand2.tb2_swing.core.Connection;
import io.github.jordand2.tb2_swing.core.Module;
import io.github.jordand2.tb2_swing.core.Port;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 *
 * @author jordan
 */
public class YamlWriter {
    
    public YamlWriter() {
        
    }
    
    private Yaml getYamlEngine() {
        DumperOptions opts = new DumperOptions();
        Representer rep = new Representer(opts);
        rep.setPropertyUtils(new ModulePropertyUtils());
        rep.addClassTag(Module.class, new Tag("!module"));
        rep.addClassTag(Port.class, Tag.MAP);
        rep.addClassTag(Connection.class, Tag.MAP);
        rep.addClassTag(ConfigField.class, Tag.MAP);
        return new Yaml(rep, new DumperOptions());
    }
    
    public void writeModules(File outputFile, Map<String, Module> modules) {
        final Yaml yaml = getYamlEngine();
        
        try (FileWriter fw = new FileWriter(outputFile)) {
            yaml.dump(modules, fw);
        } catch (IOException e) {
            System.getLogger(YamlWriter.class.getName()).log(System.Logger.Level.ERROR, String.format("Failed to write file %s!", outputFile.getPath()));
        }
    }
    
    public void writeModules(File outputFile, List<Module> modules) {
        Map<String, Module> moduleMap = new LinkedHashMap<>();
        for (Module mod : modules) {
            moduleMap.put(mod.name, mod);
        }
        writeModules(outputFile, moduleMap);
    }
    
    public void writeModule(File outputFile, Module module) {
        Map<String, Module> moduleMap = new LinkedHashMap<>();
        moduleMap.put(module.name, module);
        writeModules(outputFile, moduleMap);
    }
    
    public void writeModule(Module module) {
        writeModule(new File(String.format("%s.yaml", module.name)), module);
    }
}
