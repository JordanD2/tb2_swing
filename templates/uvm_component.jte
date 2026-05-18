


@import io.github.jordand2.tb2_swing.core.Module
@import io.github.jordand2.tb2_swing.core.Port
@import io.github.jordand2.tb2_swing.core.Connection
@import io.github.jordand2.tb2_swing.core.ConfigField
@import io.github.jordand2.tb2_swing.core.ConfigAssign

@param Module module

@if (module.longComment != null)
// ${module.longComment}
@endif
class ${module.type} extends ${module.superType};

    @for (ConfigField field : module.configFields)
        @if (field.comment != null)
            // ${field.comment}
        @endif
        @if (field.hasDefaultValue())
            ${field.getTypeAndName()} = ${field.defaultValue};
        @else
            ${field.getTypeAndName()};
        @endif

    @endfor
    @for (Port port : module.outputPorts)
        @if (port.comment != null)
            // ${port.comment}
        @endif
        uvm_analysis_port#(${port.type}) ${port.name};

    @endfor
    @for (Port port : module.inputPorts)
        @if (port.comment != null)
            // ${port.comment}
        @endif
        uvm_analysis_export#(${port.type}) ${port.name};

    @endfor
    @if(module.configFields.isEmpty())
        `uvm_component_utils(${module.pkg}::${module.type})
    @else
        `uvm_component_utils_begin(${module.pkg}::${module.type})
        @for(ConfigField field : module.configFields)
            `uvm_field_${field.macroType}(${field.getMacroArgs()})
        @endfor
        `uvm_component_utils_end
    @endif


    @for (Module sub : module.submodules)
        @if (module.submoduleComments.containsKey(sub))
            // ${module.submoduleComments.get(sub)}
        @endif
        protected ${sub.type} ${sub.name};

    @endfor
    function new (string name = "", uvm_component parent = null);
        super.new(name, parent);
    endfunction : new

    virtual function void build_phase(uvm_phase phase);
        super.build_phase(phase);

        @if (module.configAssigns.size() > 0)
            @for (ConfigAssign assign : module.configAssigns)
                uvm_config_${assign.configType}::set(${assign.context},"${assign.relPath}","${assign.fieldName}",${assign.value});
            @endfor

        @endif
        @if (module.outputPorts.size() > 0)
            @for (Port port: module.outputPorts)
                ${port.name} = new("${port.name}");
            @endfor

        @endif
        @if (module.inputPorts.size() > 0)
            @for (Port port: module.inputPorts)
                ${port.name} = new("${port.name}");
            @endfor

        @endif
        @for (Module sub : module.submodules)
            @if (module.submoduleConfigAssigns.containsKey(sub))
                @for (ConfigAssign configAssign: module.submoduleConfigAssigns.get(sub))
                    uvm_config_${configAssign.configType}::set(this, "${sub.name}${configAssign.relPath}", "${configAssign.fieldName}", ${configAssign.value});
                @endfor
            @endif
            ${sub.name} = ${sub.type}::type_id::create("${sub.name}", this);
        @endfor
    endfunction : build_phase

    @if (module.connections.size() > 0)
        virtual function void connect_phase(uvm_phase phase);
            super.connect_phase(phase);

            @for (Connection conn : module.connections)
                ${conn.srcPath}.connect(${conn.dstPath});
            @endfor
        endfunction : connect_phase

    @endif
endclass : ${module.type}