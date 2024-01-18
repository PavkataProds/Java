package bg.sofia.uni.fmi.mjt.csvprocessor.table;

import bg.sofia.uni.fmi.mjt.csvprocessor.exceptions.CsvDataNotCorrectException;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.column.BaseColumn;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.column.Column;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class BaseTable implements Table {

    private int rowCount;
    private ArrayList<String> headers;
    private ArrayList<Column> columns;

    @Override
    public void addData(String[] data) throws CsvDataNotCorrectException {
        if (data == null) {
            throw new IllegalArgumentException();
        }
        if (headers != null && data.length != headers.size()) {
            throw new CsvDataNotCorrectException();
        }
        if (headers == null) {
            headers = new ArrayList<>();
            columns = new ArrayList<>();
            for (int i = 0; i < data.length; i++) {
                headers.addLast(data[i]);
                columns.addLast(new BaseColumn());
            }
        } else {
            int i = 0;
            for (Column column : columns) {
                column.addData(data[i]);
                i++;
            }
        }
        rowCount++;
    }

    @Override
    public Collection<String> getColumnNames() {
        if (headers == null) {
            return Collections.unmodifiableCollection(new ArrayList<>());
        }
        return Collections.unmodifiableList(headers);
    }

    @Override
    public Collection<String> getColumnData(String column) {
        if (column == null || column.isBlank()) {
            throw new IllegalArgumentException();
        }
        int index = -1;
        for (String hdr : headers) {
            if (hdr.equals(column)) {
                index = headers.indexOf(column);
                break;
            }
        }
        if (index == -1) {
            throw new IllegalArgumentException();
        }
        return Collections.unmodifiableCollection(columns.get(index).getData());
    }

    @Override
    public int getRowsCount() {
        return rowCount;
    }
}