/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.rendering;

import static com.datalogics.pdf.samples.util.EnvironmentUtils.IS_OPENJDK_8;
import static com.datalogics.pdf.samples.util.Matchers.bufferedImageHasChecksum;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.datalogics.pdf.samples.SampleTestBase;

import org.apache.commons.lang3.SystemUtils;
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
                } else if (SystemUtils.IS_JAVA_1_8) {
                    add(CLASS_NAME + ".1.png", "359806f590dee0e642a10b9b5043e46fd74fa3ce");
                    add(CLASS_NAME + ".2.png", "3192548a1abf89fec8de8d8b38f906d8717613b8");

                    add(CLASS_NAME + ".1.jpg", "bff446bb308a9bf45fa698c087515dc33454e5cd");
                    add(CLASS_NAME + ".2.jpg", "284ddfc066d209f7b1705f63c8a08eb6289d205a");
                } else {
                    add(CLASS_NAME + ".1.png", "1992474437f5b1ee5a885322fb089915a57fe8c9");
                    add(CLASS_NAME + ".2.png", "560bae832b056507eac24c81d6f6ef65d2685667");

                    add(CLASS_NAME + ".1.jpg", "387a2721566553d92a936f75d3024faf83fc4430");
                    add(CLASS_NAME + ".2.jpg", "453730cfbb8e556feb532e0110fc580c2dff6a77");
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
