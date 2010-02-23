package org.mightyfrog.i18n.editor;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Shigehiro Soejima
 */
class KeyPanel extends JPanel {
    //
    private final JSplitPane SP;

    //
    private final KeyTable TABLE;
    private final TextArea TA;
    private final JButton DELETE_BUTTON;
    private final JButton SEND_BUTTON;
    private final JCheckBox SYNC_CHKBOX;

    //
    private boolean syncCheckBoxEnabled = false;

    /**
     *
     * @param deleteButtonVisible
     * @param syncCheckBoxEnabled
     */
    public KeyPanel(final boolean deleteButtonVisible,
                    final boolean syncCheckBoxEnabled) {
        this.syncCheckBoxEnabled = syncCheckBoxEnabled;

        setLayout(new BorderLayout());
        SP = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        TA = new TextArea(true);
        TA.setEditable(false);
        TABLE = new KeyTable();
        TABLE.addPropertyChangeListener(new PropertyChangeListener() {
                /** */
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String name = evt.getPropertyName();
                    if (name.equals("keyChange")) {
                        fireKeyChange(evt);
                    } else if (name.equals("selectionState")) {
                        boolean b = (Boolean) evt.getNewValue();
                        SEND_BUTTON.setEnabled(b);
                        DELETE_BUTTON.setVisible(b & deleteButtonVisible);
                    }
                }
            });
        SP.setTopComponent(new JScrollPane(TABLE));
        SP.setBottomComponent(new JScrollPane(TA));

        add(SP);

        addComponentListener(new ComponentAdapter() {
                /** */
                @Override
                public void componentShown(ComponentEvent evt) {
                    SP.setDividerLocation(0.5);
                    removeComponentListener(this);
                }
            });
        SYNC_CHKBOX = new JCheckBox(I18N.get("CheckBox.sync.with.tree"));
        SYNC_CHKBOX.setEnabled(syncCheckBoxEnabled);

        SEND_BUTTON = new JButton(I18N.get("Button.send.to.clipboard"));
        SEND_BUTTON.setEnabled(false);
        SEND_BUTTON.addActionListener(new ActionListener() {
                /** */
                @Override
                public void actionPerformed(ActionEvent evt) {
                    String[] keys = TABLE.getSelectedKeys();
                    StringBuilder sb = new StringBuilder();
                    for (String key : keys) {
                        sb.append(key + "\n");
                    }
                    Util.exportToClipboard(sb.toString());
                }
            });

        DELETE_BUTTON = new JButton(I18N.get("Button.delete.selection"));
        DELETE_BUTTON.setVisible(false);
        if (deleteButtonVisible) {
            DELETE_BUTTON.addActionListener(new ActionListener() {
                    /** */
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        String[] keys = TABLE.getSelectedKeys();
                        String fileName = Util.getLocalizedFileName();
                        int option = DialogUtil.self().
                            showYesNoDialog(I18N.get("Dialog.delete.selected.properties",
                                                     fileName));
                        if (option == JOptionPane.YES_OPTION) {
                            firePropertyChange("propertyDeleted", null, keys);
                            removeAllRows();
                        }
                    }
                });
        }

        add(createSouthPanel(), BorderLayout.SOUTH);
    }

    //
    //
    //

    /**
     * Clears table and text area contents.
     *
     */
    void clear() {
        removeAllRows();
        TA.setText(null);
        firePropertyChange("rowFilled", true, false);
    }

    /**
     * Removes all the rows from the table.
     *
     */
    void removeAllRows() {
        int[] rows = TABLE.getSelectedRows();
        for (int i = rows.length - 1; i >= 0; i--) {
            int row = TABLE.convertRowIndexToModel(rows[i]);
            ((DefaultTableModel) TABLE.getModel()).removeRow(row);
        }
    }

    /**
     * Adds property keys in the table.
     *
     * @param keys
     */
    void addKeys(String[] keys) {
        TABLE.addRows(keys);

        if (TABLE.getRowCount() == 0) {
            firePropertyChange("rowFilled", true, false);
        } else {
            firePropertyChange("rowFilled", false, true);
        }
        TABLE.setColumnIdentifierMode(KeyTable.KeyTableModel.KEY_MODE);
        SYNC_CHKBOX.setEnabled(this.syncCheckBoxEnabled && true);
    }

    /**
     * Adds property keys in the table.
     *
     * @param keys
     */
    void addKeys(KeyNode[] nodes) {
        TABLE.addRows(nodes);

        if (TABLE.getRowCount() == 0) {
            firePropertyChange("rowFilled", true, false);
        } else {
            firePropertyChange("rowFilled", false, true);
        }
        TABLE.setColumnIdentifierMode(KeyTable.KeyTableModel.KEY_MODE);
        SYNC_CHKBOX.setEnabled(this.syncCheckBoxEnabled && true);
    }

    /**
     * Adds property values in the table.
     *
     * @param values
     */
    void addValues(String[] keys) {
        TABLE.addRows(keys);

        if (TABLE.getRowCount() == 0) {
            firePropertyChange("rowFilled", true, false);
        } else {
            firePropertyChange("rowFilled", false, true);
        }
        TABLE.setColumnIdentifierMode(KeyTable.KeyTableModel.VALUE_MODE);

        // these are values so they cannot be sync'ed with the tree
        SYNC_CHKBOX.setEnabled(false);
    }

    /**
     *
     * @param visible
     */
    void setButtonVisible(boolean visible) {
        DELETE_BUTTON.setVisible(visible);
    }

    /**
     *
     * @param mode
     * @see org.mightyfrog.i18n.editor.KeyTable#KEY_MODE
     * @see org.mightyfrog.i18n.editor.KeyTable#VALUE_MODE
     */
    void setTableColumnIdentifierMode(int mode) {
        TABLE.setColumnIdentifierMode(mode);
        TA.setEnabled(mode == KeyTable.KeyTableModel.KEY_MODE);
    }

    //
    //
    //

    /**
     *
     */
    private void fireKeyChange(PropertyChangeEvent evt) {
        String key = (String) evt.getNewValue();
        String value = Util.getLocalizedValue(key);
        TA.setText(value);
        if (SYNC_CHKBOX.isSelected()) {
            firePropertyChange("keyChange", null, key);
        }
    }

    /**
     *
     */
    private JPanel createSouthPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(3, 3, 3, 3);
        gbc.weightx = 0.0;

        gbc.gridwidth = 4;
        panel.add(SYNC_CHKBOX, gbc);
        gbc.weightx = 1.0;
        panel.add(javax.swing.Box.createHorizontalBox(), gbc);
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.weightx = 0.0;
        panel.add(SEND_BUTTON, gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(DELETE_BUTTON, gbc);

        return panel;
    }
}
