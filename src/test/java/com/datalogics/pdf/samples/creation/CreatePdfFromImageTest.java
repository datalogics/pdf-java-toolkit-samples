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

import org.junit.Test;

import java.io.File;

/**
 * Test the Create PDF From Image sample.
 */
public class CreatePdfFromImageTest extends SampleTest {

    @Test
    public void testMain() throws Exception {

        // Instantiate an object for each output file. This will first delete the files if they currently exist on disc.
        final File outputBmp = newOutputFileWithDelete(CreatePdfFromImage.outputBmp);
        final File outputGif = newOutputFileWithDelete(CreatePdfFromImage.outputGif);
        final File outputPng = newOutputFileWithDelete(CreatePdfFromImage.outputPng);
        final File outputJpg = newOutputFileWithDelete(CreatePdfFromImage.outputJpg);

        // Create a PDF from each test image, confirm the file exists, then make sure it contains an image.
        CreatePdfFromImage.main(outputBmp.getCanonicalPath(), CreatePdfFromImage.inputBmp);
        assertTrue(outputBmp.getPath() + " must exist after run", outputBmp.exists());
        checkImageExists(outputBmp);

        CreatePdfFromImage.main(outputGif.getCanonicalPath(), CreatePdfFromImage.inputGif);
        assertTrue(outputGif.getPath() + " must exist after run", outputGif.exists());
        checkImageExists(outputGif);

        CreatePdfFromImage.main(outputPng.getCanonicalPath(), CreatePdfFromImage.inputPng);
        assertTrue(outputPng.getPath() + " must exist after run", outputPng.exists());
        checkImageExists(outputPng);

        CreatePdfFromImage.main(outputJpg.getCanonicalPath(), CreatePdfFromImage.inputJpg);
        assertTrue(outputJpg.getPath() + " must exist after run", outputJpg.exists());
        checkImageExists(outputJpg);
    }

    // Open a PDF from a File, get the resources on the first (should be only) page, and make sure we've got exactly
    // one image there.
    private void checkImageExists(final File file) throws Exception {
        final PDFDocument doc = openPdfDocument(file.getCanonicalPath());

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
