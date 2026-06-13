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

import io.github.jordand2.tb2_swing.core.ConfigField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 *
 * @author jordan
 */
public class ConfigFieldEditor extends JDialog {
    
    static final Dimension DELETE_BUTTON_SIZE = new Dimension(30, 25);
    static final Dimension MEDIUM_TEXT_BOX_SIZE = new Dimension(150, 25);
    static final Dimension LONG_TEXT_BOX_SIZE = new Dimension(250, 25);
    
    private Table tablePanel;
    
    private JButton cancelButton;
    private JButton saveButton;
    
    private DrawModule target;
    
    private final String[] macroTypes =  {
        "int",
        "object",
        "string",
        "enum",
        "real",
        "event",
        "sarray_int",
        "sarray_object",
        "sarray_string",
        "sarray_enum",
        "array_int",
        "array_object",
        "array_string",
        "array_enum",
        "queue_int",
        "queue_object",
        "queue_string",
        "queue_enum",
        "aa_int_string",
        "aa_object_string",
        "aa_string_string",
        "aa_object_int",
        "aa_int_int",
        "aa_int_int_unsigned",
        "aa_int_integer",
        "aa_int_integer_unsigned",
        "aa_int_byte",
        "aa_int_byte_unsigned",
        "aa_int_shortint",
        "aa_int_shortint_unsigned",
        "aa_int_longint",
        "aa_int_longint_unsigned",
        "aa_int_key",
        "aa_int_enumkey"
    };
    
    public ConfigFieldEditor(JFrame parent, DrawModule target) {
        super(parent, true);
        this.target = target;
    }
    
    class TableRow {
        private JTextField nameField;
        private JTextField typeField;
        private JTextField defaultValueField;
        private JComboBox macroTypeField;
        private JTextField macroOptions;
        private JTextField commentField;
        private JButton deleteButton;
        
        public TableRow() {
            
        }
        
        public TableRow initialize(Table parent, GridBagConstraints gbc) {
            gbc.gridx = 0;
            
            nameField = new JTextField("m_var");
            nameField.setPreferredSize(MEDIUM_TEXT_BOX_SIZE);
            parent.add(nameField, gbc);
            gbc.gridx++;
            
            typeField = new JTextField("int");
            typeField.setPreferredSize(MEDIUM_TEXT_BOX_SIZE);
            parent.add(typeField, gbc);
            gbc.gridx++;
            
            defaultValueField = new JTextField("5");
            defaultValueField.setPreferredSize(MEDIUM_TEXT_BOX_SIZE);
            parent.add(defaultValueField, gbc);
            gbc.gridx++;
            
            macroTypeField = new JComboBox(macroTypes);
            parent.add(macroTypeField, gbc);
            gbc.gridx++;
            
            macroOptions = new JTextField("UVM_DEFAULT");
            macroOptions.setPreferredSize(MEDIUM_TEXT_BOX_SIZE);
            parent.add(macroOptions, gbc);
            gbc.gridx++;
            
            commentField = new JTextField("");
            commentField.setPreferredSize(LONG_TEXT_BOX_SIZE);
            parent.add(commentField, gbc);
            gbc.gridx++;
            
            deleteButton = new JButton("x");
            deleteButton.setPreferredSize(DELETE_BUTTON_SIZE);
            deleteButton.setFocusable(false);
            deleteButton.addActionListener((e) -> {
                parent.removeRow(this);
                parent.revalidate();
                parent.repaint();
            });
            parent.add(deleteButton, gbc);
            gbc.gridx++;
            
            return this;
        }
        
        public TableRow setWithConfigField(ConfigField field) {
            nameField.setText(field.name);
            typeField.setText(field.type);
            defaultValueField.setText(field.defaultValue);
            macroTypeField.setSelectedItem(field.macroType);
            macroOptions.setText(field.macroFlags);
            commentField.setText(field.comment);
            return this;
        }
        
        public ConfigField getAsConfigField() {
            ConfigField cfg = new ConfigField();
            cfg.name = nameField.getText();
            cfg.type = typeField.getText();
            cfg.defaultValue = defaultValueField.getText();
            cfg.macroType = (String)macroTypeField.getSelectedItem();
            cfg.macroFlags = macroOptions.getText();
            cfg.comment = commentField.getText();
            return cfg;
        }
    }
    
    class Table extends JPanel {
        
        private final GridBagConstraints gbc = new GridBagConstraints();
        private final List<TableRow> rows = new ArrayList<>();
        
        public Table(){

        }
        
        public Table initialize() {
            setLayout(new GridBagLayout());
            return this;
        }
        
        public void addRow() {
            gbc.gridy = rows.size();
            rows.add(new TableRow().initialize(this, gbc));
        }
        
        public void addRow(ConfigField field) {
            gbc.gridy = rows.size();
            rows.add(new TableRow()
                    .initialize(this, gbc)
                    .setWithConfigField(field)
            );
        }
        
        public void removeRow(TableRow row) {
            rows.remove(row);
            remove(row.nameField);
            remove(row.typeField);
            remove(row.defaultValueField);
            remove(row.macroTypeField);
            remove(row.macroOptions);
            remove(row.commentField);
            remove(row.deleteButton);
        }
        
        public List<ConfigField> getConfigFields() {
            return rows.stream().map((row) -> row.getAsConfigField()).toList();
        }
    }
    
    public ConfigFieldEditor initialize() {
        
        setLayout(new BorderLayout());
        
        // NORTH - Table Controls
        JPanel tableControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addRowButton = new JButton("+");
        addRowButton.addActionListener((e) -> {
            tablePanel.addRow();
            tablePanel.revalidate();
        });
        tableControlPanel.add(addRowButton);
        add(tableControlPanel, BorderLayout.NORTH);

        // CENTER - Table
        JPanel tablePanelWrap = new JPanel(new BorderLayout());
        tablePanel = new Table().initialize();
        tablePanelWrap.add(tablePanel, BorderLayout.NORTH);
        JScrollPane tableScrollPane = new JScrollPane(tablePanelWrap, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tableScrollPane.setMinimumSize(new Dimension(500, 200));
        
        for (ConfigField field : target.configFields) {
            tablePanel.addRow(field);
        }
        
        JPanel tableHeaders = new JPanel();
        JLabel nameHeader = new JLabel("Name", JLabel.CENTER);
        nameHeader  .setPreferredSize(MEDIUM_TEXT_BOX_SIZE);
        tableHeaders.add(nameHeader);

        JLabel typeHeader = new JLabel("Type", JLabel.CENTER);
        typeHeader  .setPreferredSize(MEDIUM_TEXT_BOX_SIZE);
        tableHeaders.add(typeHeader);

        JLabel defaultValueHeader = new JLabel("Default value", JLabel.CENTER);
        defaultValueHeader .setPreferredSize(MEDIUM_TEXT_BOX_SIZE);
        tableHeaders.add(defaultValueHeader);

        JLabel macroTypeHeader = new JLabel("Macro type", JLabel.CENTER);
        macroTypeHeader .setPreferredSize(LONG_TEXT_BOX_SIZE);
        tableHeaders.add(macroTypeHeader);

        JLabel macroOptionsHeader = new JLabel("Macro options", JLabel.CENTER);
        macroOptionsHeader .setPreferredSize(MEDIUM_TEXT_BOX_SIZE);
        tableHeaders.add(macroOptionsHeader);

        JLabel commentHeader = new JLabel("Comment", JLabel.CENTER);
        commentHeader.setPreferredSize(LONG_TEXT_BOX_SIZE);
        tableHeaders.add(commentHeader);

        JLabel deleteHeader = new JLabel(" ", JLabel.CENTER);
        deleteHeader.setPreferredSize(DELETE_BUTTON_SIZE);
        tableHeaders.add(deleteHeader);
        tableScrollPane.setColumnHeaderView(tableHeaders);
        
        add(tableScrollPane, BorderLayout.CENTER);
        
        // SOUTH - Dialog Controls
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cancelButton = new JButton("Cancel");
        saveButton = new JButton("Save");
        cancelButton.addActionListener((e) -> {
            dispose();
        });
        saveButton.addActionListener((e) -> {
            Logger.getLogger(ConfigFieldEditor.class.getName()).log(Level.INFO, String.format("Updating config fields for %s...", target.type));
            target.setConfigFields(tablePanel.getConfigFields());
            dispose();
        });
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        return this;
    }
    
    public void display() {
        
        setMinimumSize(new Dimension(1200, 300));
        setPreferredSize(new Dimension(1200, 300));
        
        EventQueue.invokeLater(() -> {
            pack();
            saveButton.requestFocusInWindow();
            setVisible(true);
        });
        
    }
    
}
