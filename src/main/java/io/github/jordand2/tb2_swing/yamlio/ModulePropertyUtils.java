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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import io.github.jordand2.tb2_swing.core.Module;
import io.github.jordand2.tb2_swing.core.Port;
import io.github.jordand2.tb2_swing.core.Connection;

/**
 *
 * @author jordan
 */
public class ModulePropertyUtils extends PropertyUtils{
    
    private final static String MODULE_FIELD_ORDER[] = {
        "template",
        "pkg",
        "type",
        "superType",
        "name",
        "longComment",
        "configFields",
        "inputPorts",
        "outputPorts",
        "submodules",
        "submoduleComments",
        "submoduleConfigAssigns",
        "connections"
    };
    
    private final static String PORT_FIELD_ORDER[] = {
        "name",
        "type",
        "comment"
    };
    
    private final static String CONN_FIELD_ORDER[] = {
        "srcPath",
        "dstPath"
    };
    
    private final static String CFG_FIELD_ORDER[] = {
        "name",
        "type",
        "defaultValue",
        "macroType",
        "macroFlags",
        "comment"
    };
    
    private final static String ILLEGAL_FIELDS[] = {
        "macroArgs",
        "typeAndName"
    };

    @Override
    protected Set<Property> createPropertySet(Class<? extends Object> type, BeanAccess bAccess) {
        Map<String, Property> unsorted = getPropertiesMap(type, bAccess);
        Set<Property> sorted = new LinkedHashSet<>();
        
        for (String illegalField : ILLEGAL_FIELDS) {
            unsorted.remove(illegalField);
        }
        
        // Add "important" fields in order
        if (type.equals(Module.class)) {
            for (String field : MODULE_FIELD_ORDER) {
                sorted.add(unsorted.remove(field));
            }
        } else if (type.equals(Port.class)) {
            for (String field : PORT_FIELD_ORDER) {
                sorted.add(unsorted.remove(field));
            }
        } else if (type.equals(Connection.class)) {
            for (String field : CONN_FIELD_ORDER) {
                sorted.add(unsorted.remove(field));
            }
        } else if (type.equals(ConfigField.class)) {
            for (String field : CFG_FIELD_ORDER) {
                sorted.add(unsorted.remove(field));
            }
        }
        
        // Add any remaining fields after
        sorted.addAll(unsorted.values());
        return sorted;
    }
}
