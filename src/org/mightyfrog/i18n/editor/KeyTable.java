package org.mightyfrog.i18n.editor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 */
class KeyTable extends JTable implements MouseListener {
    //
    private final KeyTable.KeyTableModel MODEL;
    private final transient TableRowSorter<KeyTable.KeyTableModel> SORTER;

    /**
     *
     * @param ta
     */
    public KeyTable() {
        MODEL = new KeyTable.KeyTableModel();
        setModel(MODEL);
        SORTER = new TableRowSorter<KeyTable.KeyTableModel>() {
            {
                setModel(MODEL);
                setSortsOnUpdates(true);
                List<RowSorter.SortKey> list =
                new ArrayList<RowSorter.SortKey>();
                list.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
                setSortKeys(list);
            }
        };
        setRowSorter(SORTER);

        addMouseListener(this);
    }

    /** */
    @Override
    public void valueChanged(ListSelectionEvent evt) {
        super.valueChanged(evt);

        if (!evt.getValueIsAdjusting()) {
            if (getSelectedRow() != -1) {
                String key = (String) getValueAt(getSelectedRow(), 0);
                firePropertyChange("keyChange", null, key);
                firePropertyChange("selectionState", false, true);
            } else {
                firePropertyChange("selectionState", true, false);
            }
        }
    }

    /** */
    @Override
    public void mouseEntered(MouseEvent evt) {
        // no-op
    }

    /** */
    @Override
    public void mousePressed(MouseEvent evt) {
        // no-op
    }

    /** */
    @Override
    public void mouseClicked(MouseEvent evt) {
        // no-op
    }

    /** */
    @Override
    public void mouseReleased(MouseEvent evt) {
        // no-op
    }

    /** */
    @Override
    public void mouseExited(MouseEvent evt) {
        // no-op
    }

    //
    //
    //

    /**
     *
     */
    void clear() {
        for (int i = getRowCount() - 1; i >= 0; i--) {
            MODEL.removeRow(i);
        }
    }

    /**
     *
     */
    String[] getSelectedKeys() {
        int[] rows = getSelectedRows();
        String[] keys = new String[rows.length];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = (String) getValueAt(rows[i], 0);
        }

        return keys;
    }

    /**
     *
     * @param keys
     */
    void addRows(String[] keys) {
        clear();
        if (keys.length == 0) {
            return;
        }
        for (String key : keys) {
            MODEL.addRow(new String[]{key});
        }
    }

    /**
     *
     * @param keys
     */
    void addRows(KeyNode[] nodes) {
        clear();
        if (nodes.length == 0) {
            return;
        }
        for (KeyNode node : nodes) {
            MODEL.addRow(new String[]{node.getKey()});
        }
    }

    /**
     *
     * @param mode
     */
    void setColumnIdentifierMode(int mode) {
        MODEL.setMode(mode);
    }

    //
    //
    //

    /**
     *
     */
    static class KeyTableModel extends DefaultTableModel {
        //
        public static final int KEY_MODE = 0;
        public static final int VALUE_MODE = 1;

        /**
         *
         */
        public KeyTableModel() {
            setMode(KEY_MODE);
        }

        /** */
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        /** */
        @Override
        public int getColumnCount() {
            return 1;
        }

        /**
         *
         * @param mode
         */
        void setMode(int mode) {
            if (mode == KEY_MODE) {
                setColumnIdentifiers(new String[]{I18N.get("Table.col.property.key")});
            } else {
                setColumnIdentifiers(new String[]{I18N.get("Table.col.property.value")});
            }
        }
    }
}
