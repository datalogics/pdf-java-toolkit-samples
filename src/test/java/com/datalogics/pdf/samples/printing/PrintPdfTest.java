/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.printing;

import com.datalogics.pdf.samples.SampleTest;

import mockit.Mock;
import mockit.MockUp;

import org.junit.Test;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

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
    @Test
    public <T extends PrinterJob, U extends Printable> void testMain() throws Exception {
        // Mock the PrintServiceLookup.lookupDefaultPrintService() method to return a TestPrintService object
        new MockUp<PrintServiceLookup>() {
            @Mock
            PrintService lookupDefaultPrintService() {
                return new TestPrintService();
            }
        };

        // Mock the PrinterJob class
        new MockUp<T>() {
            @Mock
            public void print() throws PrinterException {
                // Do nothing...
            }
        };

        // Mock implementors of Printable
        new MockUp<U>() {
            @Mock(invocations = 1)
            int print(final Graphics graphics, final PageFormat pageFormat, final int pageIndex)
                            throws PrinterException {
                return Printable.NO_SUCH_PAGE;
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
}
