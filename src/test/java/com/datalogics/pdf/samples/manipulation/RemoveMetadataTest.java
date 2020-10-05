/*
 * Copyright 2017 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.adobe.pdfjt.core.exceptions.PDFException;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFDocumentInfo;
import com.adobe.pdfjt.services.xmp.DocumentMetadata;
import com.adobe.pdfjt.services.xmp.XMPService;
import com.adobe.xmp.XMPException;

import com.datalogics.pdf.samples.SampleTestBase;
import com.datalogics.pdf.samples.util.DocumentUtils;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * This tests the RemoveMetadata sample.
 */
public class RemoveMetadataTest extends SampleTestBase {

    private static final String FILE_NAME = "MetadataRemoved.pdf";
    private static final String PRODUCER = "Datalogics PDF Java Toolkit";

    @Test
    public void testRemovingMetadataFromPdf()
                    throws IOException, XMPException,
                    PDFException {
        final URL inputUrl = RemoveMetadata.class.getResource(RemoveMetadata.INPUT_PDF_PATH);
        final File outputFile = newOutputFileWithDelete(FILE_NAME);
        final URL outputUrl = outputFile.toURI().toURL();

        RemoveMetadata.removeMetadata(inputUrl, outputUrl);

        // Make sure the Output file exists.
        assertTrue(outputFile.getPath() + " must exist after run", outputFile.exists());

        // Verify metadata is removed.
        final PDFDocument removedMetadataDoc = DocumentUtils.openPdfDocument(outputUrl);
        final DocumentMetadata newMetadata = XMPService.getDocumentMetadata(removedMetadataDoc);
        final PDFDocumentInfo newInfo = removedMetadataDoc.getDocumentInfo();
        assertEquals(PRODUCER, newMetadata.getProducer());
        assertNotNull(newMetadata.getCreationDate());
        assertNotNull(newMetadata.getModificationDate());
        assertNotNull(newMetadata.getMetadataASDate());
        assertNull(newMetadata.getCreator());
        assertNull(newMetadata.getTrapped());
        assertEquals(PRODUCER, newInfo.getProducer());
        assertNotNull(newInfo.getCreationDate());
        assertNotNull(newInfo.getModificationDate());
        assertNull(newInfo.getCreator());
        assertNull(newInfo.getTrapped());
    }
}
