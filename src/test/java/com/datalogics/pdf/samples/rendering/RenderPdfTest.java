/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.rendering;

import static com.datalogics.pdf.samples.util.EnvironmentUtils.IS_OPENJDK_8;
import static com.datalogics.pdf.samples.util.Matchers.bufferedImageHasChecksum;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.datalogics.pdf.samples.SampleTestBase;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;


/**
 * Test the RenderPdf sample.
 */
@RunWith(Parameterized.class)
public class RenderPdfTest extends SampleTestBase {

    private static final String CLASS_NAME = MethodHandles.lookup().lookupClass().getSimpleName();
    private static final int RESOLUTION = 72;
    private static Boolean renderDone;


    /**
     * Generate a list of parameters for the test. These are a combination of file names and checksums.
     *
     * @return the list of parameters for the parameterized tests.
     */
    @Parameters(name = "{0}")
    public static Iterable<Object[]> parameters() throws IOException {
        return new ArrayList<Object[]>() {
            private static final long serialVersionUID = 7576159003442840992L;

            private void add(final String filename, final String checksum) throws IOException {
                final File file = newOutputFileWithDelete(filename);
                add(new Object[] { filename, file, checksum });
            }

            {
                if (IS_OPENJDK_8) {
                    add(CLASS_NAME + ".1.png", "c66daa9a924059fb44f2318e164e57111978bdc0");
                    add(CLASS_NAME + ".2.png", "1dc5105235eae1ded71fa4a72340b5650c85a0e1");

                    add(CLASS_NAME + ".1.jpg", "89e443ff8ad35bc2143282deaeee7dde9e59307d");
                    add(CLASS_NAME + ".2.jpg", "efd6e6e77e26533e754ab5d7e5069829f21bbbcf");
                } else {
                    add(CLASS_NAME + ".1.png", "ac1da1dc53e31eaec2b6152ef6ea68cf6b11cc49");
                    add(CLASS_NAME + ".2.png", "41eca76bf8daf99426ea2682869bdea29adaa08f");

                    add(CLASS_NAME + ".1.jpg", "8e56d2f061e2dc028efaf9a76bb3e713ca2aae99");
                    add(CLASS_NAME + ".2.jpg", "c7aac07c52ca078d99c66e74a95af1a674bd8a58");
                }
            }
        };
    }

    @Parameter
    public String fileName;

    @Parameter(1)
    public File outputFile;

    @Parameter(2)
    public String checksum;


    @BeforeClass
    public static void setUpClass() {
        renderDone = false;
    }

    private static void ensureRender() throws Exception {
        if (renderDone) {
            return;
        }

        final URL inputUrl = RenderPdf.class.getResource(RenderPdf.DEFAULT_INPUT);

        // This is the base filename, to which will be appended the page number and the .png extension
        final URL outputUrl = newOutputFile(CLASS_NAME).toURI().toURL();

        RenderPdf.renderPdf(inputUrl, RESOLUTION, outputUrl);

        renderDone = true;
    }

    /**
     * Check that the image checksum for a page matches.
     *
     * @throws Exception a general exception was thrown
     */
    @Test
    public void imageChecksumMatches() throws Exception {
        ensureRender();

        // Make sure the Output file exists.
        assertTrue(outputFile.getPath() + " must exist after run", outputFile.exists());

        // and has the correct checksum
        final BufferedImage image = ImageIO.read(outputFile);
        assertThat("File " + fileName + " has correct checksum", image, bufferedImageHasChecksum(checksum));
    }
}
