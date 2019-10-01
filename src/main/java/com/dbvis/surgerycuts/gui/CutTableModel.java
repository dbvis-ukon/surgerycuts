/*
 * Publication:
 * SurgeryCuts: Embedding Additional Information in Maps without Occluding Features. Computer Graphics Forum. 38. 10.1111/cgf.13685.
 * Angelini, Marco & Buchmüller, Juri & Keim, Daniel & Meschenmoser, Philipp & Santucci, Giuseppe. (2019).
 *
 * Programmer: Meschenmoser, Philipp
 */

package com.dbvis.surgerycuts.gui;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class CutTableModel extends AbstractTableModel {
    private static final int COLUMN_CHECK     = 0;
    private static final int COLUMN_ID     = 1;
    private static final int COLUMN_TS    = 2;
    private static final int COLUMN_AREA     = 3;
    private String[] columnNames = {"Active","ID", "TS", "Area"};
    private List<TableEntry> listFisheyes;

    public CutTableModel(List<TableEntry> listEmployees) {
        this.listFisheyes = listEmployees;
        int indexCount = 1;
        for (TableEntry entry : listEmployees) {
            entry.setID(indexCount++);
        }
    }


    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return listFisheyes.size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (listFisheyes.isEmpty()) {
            return Boolean.class;
        }
        return getValueAt(0, columnIndex).getClass();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        TableEntry employee = listFisheyes.get(rowIndex);
        Object returnValue;

        switch (columnIndex) {
            case COLUMN_CHECK:
                returnValue = employee.getChecked();
                break;
            case COLUMN_ID:
                returnValue = employee.getID();
                break;
            case COLUMN_TS:
                returnValue = employee.getTS();
                break;
            case COLUMN_AREA:
                returnValue = employee.getArea();
                break;
            default:
                throw new IllegalArgumentException("Invalid column index");
        }

        return returnValue;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        TableEntry entry = listFisheyes.get(rowIndex);
        switch (columnIndex) {
            case COLUMN_CHECK:
                entry.setChecked((Boolean) value);
                this.fireTableDataChanged();
                break;
            case COLUMN_ID:
                entry.setID((int) value);
                break;
            case COLUMN_TS:
                entry.setTS((String) value);
                break;
            case COLUMN_AREA:
                entry.setArea((double) value);
        }

    }

    public void addRow(TableEntry e){
        listFisheyes.add(e);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

}