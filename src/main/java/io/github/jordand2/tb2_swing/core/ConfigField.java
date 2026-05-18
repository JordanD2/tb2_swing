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

/**
 *
 * @author jordan
 */
public class ConfigField implements Serializable {
    
    public boolean isSArray;
    public boolean isArray;
    public boolean isQueue;
    public boolean isAA;
    public int sArraySize;
    public String aAKey;
    
    public String name;
    public String type;
    public String defaultValue;
    public String comment;
    public String macroType;
    public String macroFlags = "UVM_DEFAULT";
    
    public ConfigField() {
        
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getComment() {
        return comment;
    }

    public boolean isIsSArray() {
        return isSArray;
    }

    public void setIsSArray(boolean isSArray) {
        this.isSArray = isSArray;
    }

    public boolean isIsArray() {
        return isArray;
    }

    public void setIsArray(boolean isArray) {
        this.isArray = isArray;
    }

    public boolean isIsQueue() {
        return isQueue;
    }

    public void setIsQueue(boolean isQueue) {
        this.isQueue = isQueue;
    }

    public boolean isIsAA() {
        return isAA;
    }

    public void setIsAA(boolean isAA) {
        this.isAA = isAA;
    }

    public int getArraySize() {
        return sArraySize;
    }

    public void setArraySize(int arraySize) {
        this.sArraySize = arraySize;
    }

    public String getaAKey() {
        return aAKey;
    }

    public void setaAKey(String aAKey) {
        this.aAKey = aAKey;
    }

    public String getMacroType() {
        return macroType;
    }

    public void setMacroType(String macroType) {
        this.macroType = macroType;
    }

    public String getMacroFlags() {
        return macroFlags;
    }

    public void setMacroFlags(String macroFlags) {
        this.macroFlags = macroFlags;
    }
    
    public boolean hasDefaultValue() {
        return defaultValue != null && !defaultValue.isEmpty();
    }
    
    // FIXME : need to break the get* pattern or otherwise exclude these from yaml dump
    public String getTypeAndName() {
        if (isArray) {
            return String.format("%s %s[]", type, name);
        } else if (isQueue) {
            return String.format("%s %s[$]", type, name);
        } else if (isSArray) {
            return String.format("%s %s[%d]", type, name, sArraySize);
        } else if (isAA) {
            return String.format("%s %s[%s]", type, name, aAKey);
        } else {
            return String.format("%s %s", type, name);
        }   
    }
    
    // FIXME : need to break the get* pattern or otherwise exclude these from yaml dump
    public String getMacroArgs(){
        if (macroType.endsWith("_enum") || macroType.endsWith("_enumkey")) {
            return String.format("%s, %s, %s", type, name, macroFlags);
        } else {
            return String.format("%s, %s", name, macroFlags);
        }
    }
}
