package bg.sofia.uni.fmi.mjt.csvprocessor;

import bg.sofia.uni.fmi.mjt.csvprocessor.exceptions.CsvDataNotCorrectException;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.BaseTable;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.Table;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.printer.ColumnAlignment;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.printer.MarkdownTablePrinter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;

public class CsvProcessor implements CsvProcessorAPI {

    private final Table table;

    public CsvProcessor() {
        this(new BaseTable());
    }

    public CsvProcessor(Table table) {
        this.table = table;
    }

    private boolean containsSameValues(Collection<String> v) {
        ArrayList<String> values = new ArrayList<>(v);
        int size = values.size();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if (values.get(i).equals(values.get(j))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void readCsv(Reader reader, String delimiter) throws CsvDataNotCorrectException {
        try (BufferedReader br = new BufferedReader(reader)) {
            String line;

            while ((line = br.readLine()) != null) {
                table.addData(line.split(String.format("\\%s", delimiter)));
            }
            if (containsSameValues(table.getColumnNames())) {
                throw new CsvDataNotCorrectException();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeTable(Writer writer, ColumnAlignment... alignments) {
        MarkdownTablePrinter printer = new MarkdownTablePrinter();
        try {
            Collection<String> temp = printer.printTable(table, alignments);
            int index = 0;
            int size = temp.size();
            for (String string : temp) {
                writer.write(string);
                index++;
                if (index != size) {
                    writer.write('\n');
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }
}