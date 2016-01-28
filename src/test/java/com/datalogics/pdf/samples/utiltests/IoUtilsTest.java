/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.utiltests;

import static org.junit.Assert.assertNotNull;

import com.adobe.internal.io.ByteWriter;

import com.datalogics.pdf.samples.util.IoUtils;

import org.junit.Test;

import java.net.URI;

/**
 * Tests for the IoUtils class.
 */
public class IoUtilsTest {
    static final String FILE_NAME = "pdfjavatoolkit ds.pdf";

    @Test
    public void testMain() throws Exception {

        // Use an URI to handle spaces in file names
        final String inputPath = new URI(DocumentUtilsTest.class.getResource(FILE_NAME).toString()).getPath();

        final ByteWriter byteWriter = IoUtils.getByteWriterFromFile(inputPath);

        assertNotNull("PDF", byteWriter);
    }
}
