/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.creation;

import static com.adobe.pdfjt.pdf.graphics.font.impl.PDFFontUtils.getActualBaseFontName;

import static com.datalogics.pdf.samples.util.Matchers.hasChecksum;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.anyOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.adobe.pdfjt.core.types.ASName;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFResources;
import com.adobe.pdfjt.pdf.graphics.font.PDFFont;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObject;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObjectImage;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObjectMap;
import com.adobe.pdfjt.pdf.page.PDFPage;

import com.datalogics.pdf.samples.SampleTest;
import com.datalogics.pdf.samples.util.DocumentUtils;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

/**
 * Tests the MakeWhiteFangBook sample.
 */
public class MakeWhiteFangBookTest extends SampleTest {
    static final String FILE_NAME = "WhiteFang.pdf";

    @Test
    public void testMakeWhiteFangBook() throws Exception {
        final File file = newOutputFile(FILE_NAME);
        if (file.exists()) {
            Files.delete(file.toPath());
        }
        MakeWhiteFangBook.makeWhiteFangBook(file.toURI().toURL());
        assertTrue(file.getPath() + " must exist after run", file.exists());

        final PDFDocument doc = DocumentUtils.openPdfDocument(file.toURI().toURL());

        try {
            // Verify the resources
            final PDFResources resources = pageResources(doc, 1);

            final PDFFont f0 = resources.getFont(ASName.create("F0"));
            assertThat(getActualBaseFontName(f0), anyOf(equalTo(ASName.k_Times_Bold.asString()),
                                                        equalTo("TimesNewRomanPS-BoldMT")));
            assertNiceFont(f0);

            final PDFFont f1 = resources.getFont(ASName.create("F1"));
            assertThat(getActualBaseFontName(f1), anyOf(equalTo(ASName.k_Times_Roman.asString()),
                                                        equalTo("TimesNewRomanPSMT")));
            assertNiceFont(f1);

            // Verify the checksum of the image
            final PDFPage page = doc.requirePages().getPage(0);
            final PDFXObjectMap objMap = page.getResources().getXObjectMap();
            int numImages = 0;

            for (final ASName name : objMap.keySet()) {
                final PDFXObject o = objMap.get(name);
                if (o instanceof PDFXObjectImage) {
                    assertThat("there should only be one image on the first page of the test document", numImages++,
                               equalTo(0));
                    final PDFXObjectImage image = (PDFXObjectImage) o;
                    assertThat(image, hasChecksum("ea7454d6be178a1db357fda17f0d130cad314995"));
                }
            }
            assertThat(numImages, equalTo(1));

            // Verify the contents
            for (int i = 1; i < 7; i++) {
                final String contentsAsString = pageContentsAsString(doc, i);
                final String resourceName = String.format("WhiteFang.pdf.page%d.txt", i);

                assertEquals(contentsOfResource(resourceName), contentsAsString);
            }

        } finally {
            doc.close();
        }
    }
}
