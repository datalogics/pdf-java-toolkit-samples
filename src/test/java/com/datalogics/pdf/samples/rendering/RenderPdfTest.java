/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.rendering;

import static com.datalogics.pdf.samples.util.Matchers.bufferedImageHasChecksum;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.datalogics.pdf.samples.SampleTest;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;


/**
 * Test the RenderPdf sample.
 */
public class RenderPdfTest extends SampleTest {

    private static final int RESOLUTION = 72;
    private static final String[] CHECKSUMS;

    static {
        // Different versions of Java render some colors differently
        if (SystemUtils.IS_JAVA_1_8) {
            CHECKSUMS = new String[] {
                "", // dummy
                "359806f590dee0e642a10b9b5043e46fd74fa3ce", // page 1
                "3192548a1abf89fec8de8d8b38f906d8717613b8" // page 2
            };
        } else {
            CHECKSUMS = new String[] {
                "", // dummy
                "1992474437f5b1ee5a885322fb089915a57fe8c9", // page 1
                "560bae832b056507eac24c81d6f6ef65d2685667" // page 2
            };

        }

    }

    @Test
    public void testRenderPdf() throws Exception {
        final URL inputUrl = RenderPdf.class.getResource(RenderPdf.DEFAULT_INPUT);
        final List<File> files = new ArrayList<>();
        files.add(null); // dummy for 0 to 1 based indexing

        final String outputBaseName = getClass().getSimpleName();
        for (int i = 1; i <= 2; i++) {
            final File outputFile = newOutputFileWithDelete(outputBaseName + "." + i + ".png");
            files.add(outputFile);
        }

        // This is the base filename, to which will be appended the page number and the .png extension
        final URL outputUrl = newOutputFile(outputBaseName).toURI().toURL();

        RenderPdf.renderPdf(inputUrl, RESOLUTION, outputUrl);

        for (int i = 1; i <= 2; i++) {
            final File outputFile = files.get(i);

            // Make sure the Output file exists.
            assertTrue(outputFile.getPath() + " must exist after run", outputFile.exists());

            // and has the correct checksum
            final BufferedImage image = ImageIO.read(outputFile);
            assertThat("Page " + i + " has correct checksum", image, bufferedImageHasChecksum(CHECKSUMS[i]));
        }
    }
}
