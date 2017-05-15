/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.forms;

import static org.junit.Assert.assertTrue;

import com.datalogics.pdf.samples.SampleTest;

import org.junit.Test;

import java.io.File;
import java.net.URL;

/**
 * Tests the ExportFormDataToCsv sample.
 */
public class ExportFormDataToCsvTest extends SampleTest {
    @Test
    public void testExportFormFields() throws Exception {
        final URL inputUrl = ExportFormDataToCsv.class.getResource(ExportFormDataToCsv.DEFAULT_INPUT);

        final File outputCsvFile = newOutputFileWithDelete(ExportFormDataToCsv.CSV_OUTPUT);
        ExportFormDataToCsv.exportFormFields(inputUrl, outputCsvFile.toURI().toURL());
        assertTrue(outputCsvFile.getPath() + " must exist after run", outputCsvFile.exists());
    }
}
