package bg.sofia.uni.fmi.mjt.csvprocessor.table.printer;

import bg.sofia.uni.fmi.mjt.csvprocessor.table.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;

public class MarkdownTablePrinter implements TablePrinter {

    private final int minLength = 3;

    private HashMap<String, Integer> getMaxLengths(Table table) {
        Collection<String> temp = table.getColumnNames();
        ArrayList<String> headers = new ArrayList<>(temp);
        HashMap<String, Integer> maxLengths = new HashMap<>();

        for (int i = 0; i < headers.size(); i++) {
            String currentHeader = headers.get(i);
            int currentLength = currentHeader.length();
            if (currentLength < minLength) {
                maxLengths.put(currentHeader, minLength);
            } else {
                maxLengths.put(currentHeader, currentLength);
            }
            for (int j = 0; j < table.getRowsCount() - 1; j++) {
                if (table.getColumnData(currentHeader).toArray()[j].toString().length()
                        > maxLengths.get(currentHeader)) {
                    maxLengths.put(currentHeader, table.getColumnData(currentHeader).toArray()[j].toString().length());
                }
            }
        }
        return maxLengths;
    }

    private StringBuilder constructSecondLine(ArrayList<String> headers, HashMap<String, Integer> maxLengths,
                                              ColumnAlignment... alignments) {
        StringBuilder secondLine = new StringBuilder();
        int size = headers.size();
        for (int i = 0; i < size; i++) {
            int currentLength = secondLine.length();
            secondLine.append("| ");
            for (int j = 0; j < maxLengths.get(headers.get(i)); j++) {
                secondLine.append('-');
            }
            secondLine.append(" ");
            if (alignments.length > i) {
                if (alignments[i] == ColumnAlignment.LEFT) {
                    secondLine.replace(currentLength + 2, currentLength + minLength, ":");
                    continue;
                }
                if (alignments[i] == ColumnAlignment.RIGHT) {
                    secondLine.replace(currentLength + maxLengths.get(headers.get(i)) + 1, currentLength +
                            maxLengths.get(headers.get(i)) + 2, ":");
                    continue;
                }
                if (alignments[i] == ColumnAlignment.CENTER) {
                    secondLine.replace(currentLength + 2, currentLength + minLength, ":");
                    secondLine.replace(currentLength + maxLengths.get(headers.get(i)) + 1, currentLength +
                            maxLengths.get(headers.get(i)) + 2, ":");
                }
            }
        }
        secondLine.append("|");
        return secondLine;
    }

    private StringBuilder constructFirstLine(ArrayList<String> headers, HashMap<String, Integer> maxLengths) {
        StringBuilder line = new StringBuilder();

        for (String header : headers) {
            line.append("| ");
            line.append(header);
            line.append(" ".repeat(Math.max(0, maxLengths.get(header) - header.length())));
            line.append(' ');
        }
        line.append('|');

        return line;
    }

    private StringBuilder constructLine(Table table, ArrayList<String> headers, HashMap<String,
            Integer> maxLengths, int index) {
        StringBuilder line = new StringBuilder();

        for (String header : headers) {
            String newElement = table.getColumnData(header).toArray()[index].toString();
            line.append("| ");
            line.append(newElement);
            line.append(" ".repeat(Math.max(0, maxLengths.get(header) - newElement.length())));
            line.append(' ');
        }
        line.append('|');

        return line;
    }

    @Override
    public Collection<String> printTable(Table table, ColumnAlignment... alignments) {
        Collection<String> output = new LinkedHashSet<>();
        if (table == null || table.getColumnNames() == null) {
            return new ArrayList<>();
        }

        HashMap<String, Integer> maxLengths = getMaxLengths(table);
        Collection<String> temp = table.getColumnNames();
        ArrayList<String> headers = new ArrayList<>(temp);

        output.add(constructFirstLine(headers, maxLengths).toString());
        output.add(constructSecondLine(headers, maxLengths, alignments).toString());

        for (int i = 0; i < table.getRowsCount() - 1; i++) {
            output.add(constructLine(table, headers, maxLengths, i).toString());
        }
        return Collections.unmodifiableCollection(output);
    }
}