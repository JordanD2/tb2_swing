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
import io.github.jordand2.tb2_swing.core.Port;
import io.github.jordand2.tb2_swing.core.Module;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;

/**
 *
 * @author jordan
 */
public class YamlReader {
    
    public YamlReader() {
    }
    
    private Yaml getYamlEngine() {
        Constructor constructor = new Constructor(new LoaderOptions());
        constructor.addTypeDescription(new TypeDescription(Module.class, "!module"));
        constructor.addTypeDescription(new TypeDescription(Port.class, Tag.MAP));
        constructor.addTypeDescription(new TypeDescription(Connection.class, Tag.MAP));
        constructor.addTypeDescription(new TypeDescription(ConfigField.class, Tag.MAP));
        return new Yaml(constructor);
    }
    
    // TODO : May want to pre-validate the file contents passing them to snakeyaml
    
    public Map<String, Module> readModules(File sourceFile) {
        final Yaml yaml = getYamlEngine();
        Map<String, Module> modules = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(sourceFile)) {
            modules.putAll(yaml.load(fis));
        } catch (IOException ex) {
            System.getLogger(YamlReader.class.getName()).log(System.Logger.Level.ERROR, String.format("Failed to read file %s", sourceFile.getPath()));
        }
        return modules;
    }
    
    public Module readModule(File sourceFile, String moduleName) {
        Map<String, Module> modules = readModules(sourceFile);
        return modules.get(moduleName);
    }
    
    public Module readModule(File sourceFile) {
        String moduleName = sourceFile.getName().split("\\.")[0];
        return readModule(sourceFile, moduleName);
    }
        
}
