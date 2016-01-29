/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.creation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.adobe.pdfjt.core.types.ASName;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObject;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObjectImage;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObjectMap;

import com.datalogics.pdf.samples.SampleTest;
import com.datalogics.pdf.samples.util.DocumentUtils;

import org.junit.Test;

import java.io.File;

/**
 * Test the Create PDF From Image sample.
 */
public class CreatePdfFromImageTest extends SampleTest {

    public static final String OUTPUT_PNG = "PDF_from_PNG.pdf";
    public static final String OUTPUT_JPG = "PDF_from_JPG.pdf";
    public static final String OUTPUT_GIF = "PDF_from_GIF.pdf";
    public static final String OUTPUT_BMP = "PDF_from_BMP.pdf";

    // Each test will check to see that an output file is successfully created and that the first page contains exactly
    // one image.

    @Test
    public void testBmp() throws Exception {
        final File outputBmp = newOutputFileWithDelete(OUTPUT_BMP);
        CreatePdfFromImage.main(outputBmp.getCanonicalPath(), CreatePdfFromImage.INPUT_BMP);
        assertTrue(outputBmp.getPath() + " must exist after run", outputBmp.exists());
        checkImageExists(outputBmp);
    }

    @Test
    public void testGif() throws Exception {
        final File outputGif = newOutputFileWithDelete(OUTPUT_GIF);
        CreatePdfFromImage.main(outputGif.getCanonicalPath(), CreatePdfFromImage.INPUT_GIF);
        assertTrue(outputGif.getPath() + " must exist after run", outputGif.exists());
        checkImageExists(outputGif);
    }

    @Test
    public void testPng() throws Exception {
        final File outputPng = newOutputFileWithDelete(OUTPUT_PNG);
        CreatePdfFromImage.main(outputPng.getCanonicalPath(), CreatePdfFromImage.INPUT_PNG);
        assertTrue(outputPng.getPath() + " must exist after run", outputPng.exists());
        checkImageExists(outputPng);
    }

    @Test
    public void testJpg() throws Exception {
        final File outputJpg = newOutputFileWithDelete(OUTPUT_JPG);
        CreatePdfFromImage.main(outputJpg.getCanonicalPath(), CreatePdfFromImage.INPUT_JPG);
        assertTrue(outputJpg.getPath() + " must exist after run", outputJpg.exists());
        checkImageExists(outputJpg);
    }

    // Open a PDF from a File, get the resources on the first (should be only) page, and make sure we've got exactly
    // one image there.
    private void checkImageExists(final File file) throws Exception {
        final PDFDocument doc = DocumentUtils.openPdfDocument(file.getCanonicalPath());

        final PDFXObjectMap objMap = pageResources(doc, 0).getXObjectMap();
        int numImages = 0;

        for (final ASName name : objMap.keySet()) {
            final PDFXObject o = objMap.get(name);
            if (o instanceof PDFXObjectImage) {
                numImages++;
            }
        }
        assertThat(numImages, equalTo(1));
    }

}
