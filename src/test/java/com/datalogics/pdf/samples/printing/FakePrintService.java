/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.printing;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.ServiceUIFactory;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.ResolutionSyntax;
import javax.print.attribute.standard.PrinterResolution;
import javax.print.event.PrintServiceAttributeListener;

/*
 * TestPrintService implements a 'fake' PrintService to be returned by our mock PrintServiceLookup.
 */
public class FakePrintService implements PrintService {
    static final double DOTS_PER_INCH = 400.0;

    /*
     * Return a name for our 'fake' PrintService.
     */
    @Override
    public String getName() {
        return "Virtual Test Printer (400 DPI)";
    }

    /*
     * Return default attribute values for our 'fake' PrintService. The only attribute we care about is
     * PrinterResolution; all others return null.
     */
    @Override
    public Object getDefaultAttributeValue(final Class<? extends Attribute> category) {
        if (category == PrinterResolution.class) {
            return new PrinterResolution((int) DOTS_PER_INCH, (int) DOTS_PER_INCH, ResolutionSyntax.DPI);
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
