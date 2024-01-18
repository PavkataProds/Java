package bg.sofia.uni.fmi.mjt.csvprocessor;

import bg.sofia.uni.fmi.mjt.csvprocessor.exceptions.CsvDataNotCorrectException;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.BaseTable;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.printer.ColumnAlignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvProcessorTest {

    private CsvProcessor csvProcessor;

    @BeforeEach
    void setUp() {
        csvProcessor = new CsvProcessor(new BaseTable());
    }

    @Test
    void testReadCsvAndWriteTable() throws IOException, CsvDataNotCorrectException {
        String csvData = "Name,Age,Country\nJohn,25,USA\nAlice,30,UK\nBob,22,Canada";
        String expectedMarkdownTable =
                "| Name  | Age | Country |\n" +
                        "|-------|-----|---------|\n" +
                        "| John  | 25  | USA     |\n" +
                        "| Alice | 30  | UK      |\n" +
                        "| Bob   | 22  | Canada  |";

        StringReader stringReader = new StringReader(csvData);
        StringWriter stringWriter = new StringWriter();

        csvProcessor.readCsv(stringReader, ",");
        csvProcessor.writeTable(stringWriter, ColumnAlignment.LEFT, ColumnAlignment.RIGHT, ColumnAlignment.CENTER);

        assertEquals(expectedMarkdownTable, stringWriter.toString().trim());
    }

    @Test
    void testReadCsvWithIncorrectData() {
        String incorrectCsvData = "Name,Age,Country\nJohn,25,USA\nAlice,30,UK,ExtraColumn";
        StringReader stringReader = new StringReader(incorrectCsvData);

        assertThrows(CsvDataNotCorrectException.class, () -> {
            csvProcessor.readCsv(stringReader, ",");
        });
    }

    @Test
    void testWriteTableWithNoData() {
        StringWriter stringWriter = new StringWriter();
        csvProcessor.writeTable(stringWriter, ColumnAlignment.CENTER);

        assertEquals("", stringWriter.toString().trim());
    }

    @Test
    void testWriteTableWithCustomAlignment() throws CsvDataNotCorrectException {
        String csvData = "Name,Age,Country\nJohn,25,USA\nAlice,30,UK\nBob,22,Canada";
        String expectedMarkdownTable =
                "| Name  | Age | Country |\n" +
                        "|:-----:|:---:|:-------:|\n" +
                        "| John  | 25  |   USA   |\n" +
                        "| Alice | 30  |   UK    |\n" +
                        "|  Bob  | 22  | Canada  |";

        StringReader stringReader = new StringReader(csvData);
        csvProcessor.readCsv(stringReader, ",");

        StringWriter stringWriter = new StringWriter();
        csvProcessor.writeTable(stringWriter, ColumnAlignment.CENTER);

        assertEquals(expectedMarkdownTable, stringWriter.toString().trim());
    }
}