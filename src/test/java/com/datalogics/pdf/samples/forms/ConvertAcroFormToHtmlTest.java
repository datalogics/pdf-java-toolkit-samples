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
 * Tests the ConvertAcroFormToHtml sample.
 */
public class ConvertAcroFormToHtmlTest extends SampleTest {
    @Test
    public void testConvertAcroFormToHtmlWithDefaultSampleInput() throws Exception {
        final URL inputUrl = ConvertAcroFormToHtml.class.getResource(ConvertAcroFormToHtml.DEFAULT_INPUT);

        final File outputHtmlFile = newOutputFileWithDelete(ConvertAcroFormToHtml.HTML_OUTPUT);
        ConvertAcroFormToHtml.createHtmlForm(inputUrl, outputHtmlFile.toURI().toURL());
        assertTrue(outputHtmlFile.getPath() + " must exist after run", outputHtmlFile.exists());

    }
}
