/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.printing;

import static com.datalogics.pdf.samples.util.Matchers.bufferedImageHasChecksum;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import com.datalogics.pdf.samples.SampleTest;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import mockit.Mock;
import mockit.MockUp;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterIOException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;

/**
 * Tests the PrintPdf sample.
 */
@SuppressFBWarnings(value = { "SIC_INNER_SHOULD_BE_STATIC_ANON", "UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS" },
                    justification = "JMockit coding pattern depends on anonymous classes "
                                    + "and methods with no discernable call site")
public class PrintPdfTest extends SampleTest {
    private static final String RENDERED_IMAGE_NAME = "renderedImage_page%d.png";
    private static final String RENDERED_MULTI_PAGE_IMAGE_NAME = "renderedMultiPageImage_page%d.png";
    private static final String[] PAGE_IMAGE_CHECKSUMS = { "897ac162b0ab9e798771250ca8fdd7997f03cbd1",
        "f2e86261405b8e6e1a1d94f9f67571d4a8f7fef6" };
    private static final String[] PAGE_MULTI_PAGE_IMAGE_CHECKSUMS = { "897ac162b0ab9e798771250ca8fdd7997f03cbd1",
        "897ac162b0ab9e798771250ca8fdd7997f03cbd1", "f2e86261405b8e6e1a1d94f9f67571d4a8f7fef6",
        "f2e86261405b8e6e1a1d94f9f67571d4a8f7fef6" };
    private static final String DEFAULT_INPUT = "pdfjavatoolkit-ds.pdf";

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public <T extends PrinterJob> void testPrintPdf() throws Exception {
        assumeThat("This test requires a Java 7 JRE for the checksums to work",
                   System.getProperty("java.runtime.version"), startsWith("1.7."));
        // Mock the PrintServiceLookup.lookupDefaultPrintService() method to return a TestPrintService object
        new MockUp<PrintServiceLookup>() {
            @Mock(invocations = 1)
            PrintService lookupDefaultPrintService() {
                return new FakePrintService();
            }
        };

        // Mock the PrinterJob.getPrinterJob() method to return a TestPrinterJob object
        new MockUp<T>() {
            @Mock(invocations = 1)
            public PrinterJob getPrinterJob() {
                return new TestPrinterJob();
            }
        };

        // Call the printPdf method
        final URL inputUrl = PrintPdf.class.getResource(DEFAULT_INPUT);
        PrintPdf.printPdf(inputUrl);
    }

    @Test
    public <T extends PrinterJob> void testPrintPdfWithMultiPagePrinterJob() throws Exception {
        assumeThat("This test requires a Java 7 JRE for the checksums to work",
                   System.getProperty("java.runtime.version"), startsWith("1.7."));
        // Mock the PrintServiceLookup.lookupDefaultPrintService() method to return a TestPrintService object
        new MockUp<PrintServiceLookup>() {
            @Mock(invocations = 1)
            PrintService lookupDefaultPrintService() {
                return new FakePrintService();
            }
        };

        // Mock the PrinterJob.getPrinterJob() method to return a TestPrinterJob object
        new MockUp<T>() {
            @Mock(invocations = 1)
            public PrinterJob getPrinterJob() {
                return new TestMultiPagePrinterJob();
            }
        };

        // Call the printPdf method
        final URL inputUrl = PrintPdf.class.getResource(DEFAULT_INPUT);
        PrintPdf.printPdf(inputUrl);
    }

    @Test
    public void testPrintPdfNoPrinter() throws Exception {
        // Mock the PrinterServiceLookup.lookupDefaultPrintService() method to return nothing (no printer available)
        new MockUp<PrintServiceLookup>() {
            @Mock(invocations = 1)
            PrintService lookupDefaultPrintService() {
                return null;
            }
        };

        // Call the printPdf method
        expected.expect(PrinterException.class);
        expected.expectMessage("Printer failed to exist.");
        final URL inputUrl = PrintPdf.class.getResource(DEFAULT_INPUT);
        PrintPdf.printPdf(inputUrl);
    }

    /*
     * TestPrinterJob implements a 'fake' PrinterJob to intercept print requests.
     *
     * Note: This class will utilize the default functionality of the virtual Fake PrinterJob and make a single call to
     * the the Printable.print() method for each page index.
     */
    private static class TestPrinterJob extends FakePrinterJob {
        @Override
        public void processPageImage(final BufferedImage image, final int pageIndex) throws PrinterIOException {
            savePageImage(image, pageIndex);
            assertThat(image, bufferedImageHasChecksum(PAGE_IMAGE_CHECKSUMS[pageIndex]));
        }

        // Based on a code snippet from the Java tutorials:
        // https://docs.oracle.com/javase/tutorial/2d/images/saveimage.html
        private void savePageImage(final BufferedImage image, final int pageIndex) throws PrinterIOException {
            final String outputName = String.format(RENDERED_IMAGE_NAME, pageIndex);
            final File outputFile = newOutputFile(outputName);
            try {
                ImageIO.write(image, "png", outputFile);
            } catch (final IOException ioe) {
                throw new PrinterIOException(ioe);
            }
        }
    }

    /*
     * TestMultiPagePrinterJob implements a 'fake' PrinterJob to intercept print requests.
     *
     * Note: This class will be used to test printing a page multiple times. This will test the scenario when printer
     * settings cause the Printable.print() method to be called with the same page index more than once.
     */
    private static class TestMultiPagePrinterJob extends FakePrinterJob {
        /*
         * Print the document.
         */
        @Override
        public void print() throws PrinterException {
            // Create a BufferedImage to render into
            final int width = (int) (format.getImageableWidth() - format.getImageableX());
            final int height = (int) (format.getImageableHeight() - format.getImageableY());
            // NOTE: We use a TYPE_4BYTE_ABGR because it is guaranteed to use a single contiguous
            // image data buffer. This lets us checksum the raw data for the entire image.
            final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);

            Graphics2D gfx2d = image.createGraphics();

            for (int i = 0; i < 4; i++) {
                if (painter.print(gfx2d, format, (i / 2)) == Printable.NO_SUCH_PAGE) {
                    break;
                }
                processPageImage(image, i);

                // painter.print() disposed of the Graphics2D, obtain a new one
                gfx2d = image.createGraphics();
                gfx2d.clearRect(0, 0, width, height);
            }
        }

        @Override
        public void processPageImage(final BufferedImage image, final int pageIndex) throws PrinterIOException {
            savePageImage(image, pageIndex);
            assertThat(image, bufferedImageHasChecksum(PAGE_MULTI_PAGE_IMAGE_CHECKSUMS[pageIndex]));
        }

        // Based on a code snippet from the Java tutorials:
        // https://docs.oracle.com/javase/tutorial/2d/images/saveimage.html
        private void savePageImage(final BufferedImage image, final int pageIndex) throws PrinterIOException {
            final String outputName = String.format(RENDERED_MULTI_PAGE_IMAGE_NAME, pageIndex);
            final File outputFile = newOutputFile(outputName);
            try {
                ImageIO.write(image, "png", outputFile);
            } catch (final IOException ioe) {
                throw new PrinterIOException(ioe);
            }
        }
    }
}
