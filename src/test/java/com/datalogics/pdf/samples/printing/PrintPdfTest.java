/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.printing;

import com.datalogics.pdf.samples.SampleTest;

import mockit.Mock;
import mockit.MockUp;

import org.junit.Test;

import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterIOException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUIFactory;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.ResolutionSyntax;
import javax.print.attribute.standard.PrinterResolution;
import javax.print.event.PrintServiceAttributeListener;

/**
 * Tests the PrintPdf sample.
 */
public class PrintPdfTest extends SampleTest {
    private static final String RENDERED_IMAGE_NAME = "renderedImage_page%d.png";
    @Test
    public <T extends PrinterJob> void testMain() throws Exception {
        // Mock the PrintServiceLookup.lookupDefaultPrintService() method to return a TestPrintService object
        new MockUp<PrintServiceLookup>() {
            @Mock(invocations = 1)
            PrintService lookupDefaultPrintService() {
                return new TestPrintService();
            }
        };

        // Mock the PrinterJob.getPrinterJob() method to return a TestPrinterJob object
        new MockUp<T>() {
            @Mock(invocations = 1)
            public PrinterJob getPrinterJob() {
                return new TestPrinterJob();
            }
        };

        // Call the main method
        final String[] args = new String[0];
        PrintPdf.main(args);
    }

    /*
     * TestPrintService implements a 'fake' PrintService to be returned by our mock PrintServiceLookup.
     */
    private static class TestPrintService implements PrintService {
        /*
         * Return a name for our 'fake' PrintService.
         */
        @Override
        public String getName() {
            return "Virtual Test Printer";
        }

        /*
         * Return default attribute values for our 'fake' PrintService. The only attribute we care about is
         * PrinterResolution; all others return null.
         */
        @Override
        public Object getDefaultAttributeValue(final Class<? extends Attribute> category) {
            if (category == PrinterResolution.class) {
                return new PrinterResolution(400, 400, ResolutionSyntax.DPI);
            } else {
                return null;
            }
        }

        /*
         * The following methods are not used in the test, and are given stub implementations.
         */
        @Override
        public DocPrintJob createPrintJob() {
            return null;
        }

        @Override
        public void addPrintServiceAttributeListener(final PrintServiceAttributeListener listener) {}

        @Override
        public void removePrintServiceAttributeListener(final PrintServiceAttributeListener listener) {}

        @Override
        public PrintServiceAttributeSet getAttributes() {
            return null;
        }

        @Override
        public <T extends PrintServiceAttribute> T getAttribute(final Class<T> category) {
            return null;
        }

        @Override
        public DocFlavor[] getSupportedDocFlavors() {
            return new DocFlavor[0];
        }

        @Override
        public boolean isDocFlavorSupported(final DocFlavor flavor) {
            return false;
        }

        @Override
        public Class<?>[] getSupportedAttributeCategories() {
            return new Class<?>[0];
        }

        @Override
        public boolean isAttributeCategorySupported(final Class<? extends Attribute> category) {
            return false;
        }

        @Override
        public Object getSupportedAttributeValues(final Class<? extends Attribute> category, final DocFlavor flavor,
                                                  final AttributeSet attributes) {
            return null;
        }

        @Override
        public boolean isAttributeValueSupported(final Attribute attrval, final DocFlavor flavor,
                                                 final AttributeSet attributes) {
            return false;
        }

        @Override
        public AttributeSet getUnsupportedAttributes(final DocFlavor flavor, final AttributeSet attributes) {
            return null;
        }

        @Override
        public ServiceUIFactory getServiceUIFactory() {
            return null;
        }
    }

    /*
     * TestPrinterJob implements a 'fake' PrinterJob to intercept print requests.
     */
    private static class TestPrinterJob extends PrinterJob {

        private Printable painter;
        private PageFormat format;

        /*
         * Clones the PageFormat argument and alters the clone to a default page. No changes are required in this case.
         */
        @Override
        public PageFormat defaultPage(final PageFormat page) {
            return (PageFormat) page.clone();
        }

        /*
         * Return a validated page format.
         */
        @Override
        public PageFormat validatePage(final PageFormat page) {
            return (PageFormat) page.clone();
        }

        /*
         * Print the document.
         */
        @Override
        public void print() throws PrinterException {
            // Create a BufferedImage to render into
            final int width = (int) (format.getImageableWidth() - format.getImageableX());
            final int height = (int) (format.getImageableHeight() - format.getImageableY());
            final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics2D gfx2d = image.createGraphics();
            int pageIndex = 0;

            while (painter.print(gfx2d, format, pageIndex) == Printable.PAGE_EXISTS) {
                // painter.print() disposed of the Graphics2D, obtain a new one
                gfx2d = image.createGraphics();
                pageIndex++;

                // TODO: Save buffered image to disk and checksum
                final String outputName = String.format(RENDERED_IMAGE_NAME, pageIndex);
                final File outputFile = newOutputFile(outputName);
                try {
                    ImageIO.write(image, "png", outputFile);
                } catch (final IOException exp) {
                    throw new PrinterIOException(exp);
                }
            }
        }

        /*
         * Calls painter to render the pages in the specified format.
         */
        @Override
        public void setPrintable(final Printable painter, final PageFormat format) {
            this.painter = painter;
            this.format = format;
        }

        /*
         * The following methods are not used in the test, and are given stub implementations.
         */
        @Override
        public void setPrintable(final Printable painter) {}

        @Override
        public void setPageable(final Pageable document) throws NullPointerException {}

        @Override
        public boolean printDialog() throws HeadlessException {
            return false;
        }

        @Override
        public PageFormat pageDialog(final PageFormat page) throws HeadlessException {
            return null;
        }

        @Override
        public void setCopies(final int copies) {}

        @Override
        public int getCopies() {
            return 0;
        }

        @Override
        public String getUserName() {
            return null;
        }

        @Override
        public void setJobName(final String jobName) {}

        @Override
        public String getJobName() {
            return null;
        }

        @Override
        public void cancel() {}

        @Override
        public boolean isCancelled() {
            return false;
        }
    }
}
