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
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Test the Create PDF From Image sample.
 */
public class MakePdfFromImageTest extends SampleTest {

    public static final String OUTPUT_PNG = "PDF_from_PNG.pdf";
    public static final String OUTPUT_JPG = "PDF_from_JPG.pdf";
    public static final String OUTPUT_GIF = "PDF_from_GIF.pdf";
    public static final String OUTPUT_BMP = "PDF_from_BMP.pdf";
    public static final String OUTPUT_ALL = "PDF_from_ALL.pdf";

    // Each test will check to see that an output file is successfully created and that the first page contains exactly
    // one image.

    @Test
    public void testBmp() throws Exception {
        final URL inputUrl = MakePdfFromImage.class.getResource(MakePdfFromImage.INPUT_BMP);
        final File outputBmp = newOutputFileWithDelete(OUTPUT_BMP);
        final URL outputUrl = outputBmp.toURI().toURL();

        MakePdfFromImage.createPdfFromImageAndSave(inputUrl, outputUrl);

        assertTrue(outputBmp.getPath() + " must exist after run", outputBmp.exists());
        checkImageExists(outputBmp);
    }

    @Test
    public void testGif() throws Exception {
        final URL inputUrl = MakePdfFromImage.class.getResource(MakePdfFromImage.INPUT_GIF);
        final File outputGif = newOutputFileWithDelete(OUTPUT_GIF);
        final URL outputUrl = outputGif.toURI().toURL();

        MakePdfFromImage.createPdfFromImageAndSave(inputUrl, outputUrl);

        assertTrue(outputGif.getPath() + " must exist after run", outputGif.exists());
        checkImageExists(outputGif);
    }

    @Test
    public void testPng() throws Exception {
        final URL inputUrl = MakePdfFromImage.class.getResource(MakePdfFromImage.INPUT_PNG);
        final File outputPng = newOutputFileWithDelete(OUTPUT_PNG);
        final URL outputUrl = outputPng.toURI().toURL();

        MakePdfFromImage.createPdfFromImageAndSave(inputUrl, outputUrl);

        assertTrue(outputPng.getPath() + " must exist after run", outputPng.exists());
        checkImageExists(outputPng);
    }

    @Test
    public void testJpg() throws Exception {
        final URL inputUrl = MakePdfFromImage.class.getResource(MakePdfFromImage.INPUT_JPG);
        final File outputJpg = newOutputFileWithDelete(OUTPUT_JPG);
        final URL outputUrl = outputJpg.toURI().toURL();

        MakePdfFromImage.createPdfFromImageAndSave(inputUrl, outputUrl);

        assertTrue(outputJpg.getPath() + " must exist after run", outputJpg.exists());
        checkImageExists(outputJpg);
    }

    @Test
    public void testMultipleImages() throws Exception {
        final URL inputUrlBmp = MakePdfFromImage.class.getResource(MakePdfFromImage.INPUT_BMP);
        final URL inputUrlGif = MakePdfFromImage.class.getResource(MakePdfFromImage.INPUT_GIF);
        final URL inputUrlPng = MakePdfFromImage.class.getResource(MakePdfFromImage.INPUT_PNG);
        final URL inputUrlJpg = MakePdfFromImage.class.getResource(MakePdfFromImage.INPUT_JPG);
        final List<URL> inputImages = Arrays.asList(inputUrlBmp, inputUrlGif, inputUrlPng, inputUrlJpg);

        final File outputAll = newOutputFileWithDelete(OUTPUT_ALL);
        final URL outputUrl = outputAll.toURI().toURL();

        MakePdfFromImage.createPdfFromImages(inputImages, outputUrl);

        assertTrue(outputAll.getPath() + " must exist after run", outputAll.exists());
        checkImageExists(outputAll, 0, 4);
    }

    // Open a PDF from a File, get the resources on the first (should be only) page, and make sure we've got exactly
    // one image there.
    private void checkImageExists(final File file) throws Exception {
        checkImageExists(file, 0, 1);
    }

    // Open a PDF from a File, we should have exactly one image on each page.
    private void checkImageExists(final File file, final int startPage, final int endPage) throws Exception {
        final PDFDocument doc = DocumentUtils.openPdfDocument(file.toURI().toURL());

        for (int i = startPage; i < endPage; i++) {
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
}
