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

import io.github.jordand2.tb2_swing.core.ConfigAssign;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author jordan
 */
public class ConfigAssignEditor extends JDialog {
    
    private DrawModule target;
    
    private JTable tab;
    
    public ConfigAssignEditor(JFrame parent, DrawModule target) {
        super(parent, true);
        this.target = target;
    }
    
    public ConfigAssignEditor initialize() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(400, 400));
        setMinimumSize(new Dimension(400, 400));
        
        // Center - Table
        JComboBox<String> fieldTypeSelector = new JComboBox<>(new String[] {"int", "string", "object", "object_wrapper"});
        String[] headers = {"Context", "Path", "Field Name", "Type", "Value"};
        
        DefaultTableModel model = new DefaultTableModel(getTableDataFromAssigns(target.configAssigns), headers);
        
        tab = new JTable(model);
        tab.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(fieldTypeSelector));
        JScrollPane scrollPane = new JScrollPane(tab);
        this.add(scrollPane, BorderLayout.CENTER);
        
        // North - Table Controls
        JButton addRowButton = new JButton("+");
        addRowButton.addActionListener((f) -> {
            model.addRow(new Object[]{"this", "", "a", "int", 5});
        });
            
        JButton removeRowButton = new JButton("-");
        removeRowButton.addActionListener((f) -> {
            if (tab.getSelectedRow() >= 0) {
                model.removeRow(tab.getSelectedRow());
            } else {
                model.removeRow(model.getRowCount()-1);
            }
        });

        JPanel tableControls = new JPanel(new FlowLayout());
        tableControls.add(removeRowButton);
        tableControls.add(addRowButton);

        this.add(tableControls, BorderLayout.NORTH);
        
        // SOUTH - Dialog Controls
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener((e) -> {
            dispose();
        });
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener((e) -> {
            Logger.getLogger(ConfigFieldEditor.class.getName()).log(Level.INFO, String.format("Updating config fields for %s...", target.type));
            target.configAssigns.clear();
            target.configAssigns.addAll(model.getDataVector().stream().map((t) -> getConfigAssignFromRowData(t.toArray())).toList());
            target.canvas.modified = true;
            dispose();
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        this.add(buttonPanel, BorderLayout.SOUTH);
        
        return this;
    }
    
    public void display() {
        EventQueue.invokeLater(() -> {
            setVisible(true);
        });
    }
    
    protected Object[][] getTableDataFromAssigns(List<ConfigAssign> assigns) {
         return assigns.stream().map((a) -> {
             return new Object[]{a.context, a.relPath, a.fieldName, a.configType, a.value};
         }).toArray(Object[][]::new);
    }
    
    protected ConfigAssign getConfigAssignFromRowData(Object[] rowData) {
        ConfigAssign ca = new ConfigAssign();
        ca.setContext((String)rowData[0]);
        ca.setRelPath((String)rowData[1]);
        ca.setFieldName((String)rowData[2]);
        ca.setConfigType((String)rowData[3]);
        ca.setValue(String.valueOf(rowData[4]));
        return ca;
    }
    
    
    
}
