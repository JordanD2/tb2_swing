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
package io.github.jordand2.tb2_swing.jte_render;

import io.github.jordand2.tb2_swing.core.Module;
import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.FileOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author JordanD2
 */
public class ModuleRenderer {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ModuleRenderer.class.getName());
    
    static final String TEMPLATES_DEFAULT_PATH = "templates";
    
    CodeResolver codeResolver;
    TemplateEngine templateEngine;
            
    public ModuleRenderer() {
        codeResolver = new DirectoryCodeResolver(Path.of(TEMPLATES_DEFAULT_PATH));
        templateEngine = TemplateEngine.create(codeResolver, ContentType.Plain);
        templateEngine.setTrimControlStructures(true);
    }
    
    /**
     * Recursively render the given module and all of its submodules by type.
     * 
     * @param module The module to be rendered
     * @param outputPath The target directory where the rendered files will be dumped
     * 
     * @throws IOException 
     */
    public void renderTopModule(Module module, String outputPath) throws IOException {
        ArrayList<Module> instances = new ArrayList<>();
        Map<String, Module> typeMap = new LinkedHashMap<>();
        
        instances.add(module);
        module.getAllSubmodules(instances, true);
        
        for(Module mod : instances) {
            typeMap.put(mod.type, mod);
        }
        
        logger.log(Level.INFO, String.format("Found %d types and %d instances.", typeMap.size(), instances.size()));
        
        // Only render one module per type, using whatever reference module was found last (first?... probably last... doesn't matter)
        for(Map.Entry<String, Module> type : typeMap.entrySet()) {
            Module refModule = type.getValue();
            renderModule(refModule, refModule.template, outputPath);
        }
    }
    
    /**
     * Renders a single module by type.
     * 
     * @param module The module to be rendered
     * @param template The template that will be used to render the file
     * @param outputPath The directory path where the output file will go
     * 
     * @throws IOException 
     */
    public void renderModule(Module module, String template, String outputPath) throws IOException {
        logger.log(Level.INFO, String.format("Rendering %s using %s", module.name, template));
        
        try (FileOutput output = new FileOutput(Path.of(String.format("%s/%s.sv", outputPath, module.type)))) {
            templateEngine.render(template, module, output);
        }
        
        // FIXME
        // Need to revisit rendering walk strategy
        // 1) Walk whole tree   +done
        // 2) Make list of types and instancces   +done
        // 3) Create files for each type   +done
        // 4) Need a way to render "modules" that only appear as supertypes...   -TODO
    }
    
}
