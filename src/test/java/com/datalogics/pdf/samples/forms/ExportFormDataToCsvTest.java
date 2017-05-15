/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.forms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.datalogics.pdf.samples.SampleTest;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests the ExportFormDataToCsv sample.
 */
public class ExportFormDataToCsvTest extends SampleTest {
    private static final int DEFAULT_SAMPLE_INPUT_RECORD_SIZE = 1;
    private static final HashMap<String, String> DEFAULT_SAMPLE_OUTPUT_CSV_DATA = new HashMap<String, String>() {
        private static final long serialVersionUID = -5083671805004050295L;

        {
            put("accountNumber", "123456");
            put("name", "John Doe");
            put("address", "101 North Wacker Drive Suite 1800");
            put("city", "Chicago");
            put("state", "Illinois");
            put("zip", "60606");
            put("phone", "(312) 853-8322");
            put("email", "tech_support@datalogics.com");
            put("quantity", "5");
            put("cost", "$ 20.00");
            put("deliveryCharge", "$ 15.75");
            put("total", "$ 115.75");
            put("apples", "Yes");
            put("pears", "Yes");
            put("bananas", "");
            put("oranges", "");
            put("durian", "");
        }
    };

    @Test
    public void testExportFormFieldsDefaultSampleInput() throws Exception {
        final URL inputUrl = ExportFormDataToCsv.class.getResource(ExportFormDataToCsv.DEFAULT_INPUT);

        final File outputCsvFile = newOutputFileWithDelete(ExportFormDataToCsv.CSV_OUTPUT);
        ExportFormDataToCsv.exportFormFields(inputUrl, outputCsvFile.toURI().toURL());
        assertTrue(outputCsvFile.getPath() + " must exist after run", outputCsvFile.exists());

        final BufferedReader in = new BufferedReader(new FileReader(outputCsvFile));
        final Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
        final List<CSVRecord> recordsAsList = IteratorUtils.toList(records.iterator());

        assertEquals("There should be one record for the default sample input",
                     DEFAULT_SAMPLE_INPUT_RECORD_SIZE, recordsAsList.size());

        checkCsvRecordValues(recordsAsList, DEFAULT_SAMPLE_OUTPUT_CSV_DATA);
    }

    private void checkCsvRecordValues(final List<CSVRecord> records, final HashMap<String, String> expectedCsvData)
                    throws Exception {
        for (final CSVRecord record : records) {
            final Map<String, String> recordAsMap = record.toMap();
            assertEquals("Record size should match expected CSV data size", expectedCsvData.size(),
                         recordAsMap.size());
            assertEquals("", recordAsMap, expectedCsvData);
            recordAsMap.clear();
        }
    }
}
