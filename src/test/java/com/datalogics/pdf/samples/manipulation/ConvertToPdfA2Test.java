/*
 * Copyright 2017 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;

import static org.junit.Assert.assertTrue;

import com.adobe.pdfjt.services.pdfa2.PDFA2ConformanceLevel;

import com.datalogics.pdf.samples.SampleTestBase;

import org.junit.Test;

import java.io.File;
import java.net.URL;

/**
 * Tests the ConvertToPdfA2 sample.
 */
public class ConvertToPdfA2Test extends SampleTestBase {

    private static final String FILE_NAME = "ConvertedPdfa-2b.pdf";

    @Test
    public void testConvertToPdfA2B() throws Exception {
        final URL inputUrl = ConvertToPdfA2.class.getResource(ConvertToPdfA2.INPUT_PDF_PATH);
        final File outputFile = newOutputFileWithDelete(FILE_NAME);
        final URL outputUrl = outputFile.toURI().toURL();

        ConvertToPdfA2.convertToPdfA2(inputUrl, outputUrl, PDFA2ConformanceLevel.Level_2b);
        // Make sure the Output file exists.
        assertTrue(outputFile.getPath() + " must exist after run", outputFile.exists());

        // Validate the converted document
        final boolean validity = ConvertToPdfA2.validate(outputUrl, PDFA2ConformanceLevel.Level_2b);
        assertTrue(outputFile.getPath() + " must be pdfa2-b compliant", validity);
    }
}
