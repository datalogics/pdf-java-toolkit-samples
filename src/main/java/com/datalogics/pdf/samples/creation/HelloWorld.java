/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.creation;

import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;

import com.datalogics.pdf.document.DocumentHelper;
import com.datalogics.pdf.layout.LayoutEngine;
import com.datalogics.pdf.text.Paragraph;

/**
 * This sample shows how to create a basic PDF containing the text 'Hello World'.
 *
 */
public final class HelloWorld {
    private static final String OUTPUT_PDF_PATH = "HelloWorld.pdf";

    /**
     * This is a utility class, and won't be instantiated.
     */
    private HelloWorld() {}

    /**
     * Main program.
     *
     * @param args command line arguments
     * @throws Exception a general exception was thrown
     */
    public static void main(final String... args) throws Exception {
        // If you are using an evaluation version of the product (License Managed, or LM), set the path to where PDFJT
        // can find the license file.
        //
        // If you are not using an evaluation version of the product you can ignore or remove this code.
        LicenseManager.setLicensePath(".");
        String path;
        if (args.length > 0) {
            path = args[0];
        } else {
            path = OUTPUT_PDF_PATH;
        }
        run(path);
    }

    static void run(final String outputPath) throws Exception {
        PDFDocument document = null;

        try {
            document = PDFDocument.newInstance(PDFOpenOptions.newInstance());

            addText(document);

            DocumentHelper.saveFullAndClose(document, outputPath);
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    /**
     * Add the Hello World text to the PDF document.
     *
     * @param pdfDoc The document to add the text to
     * @throws Exception a general exception was thrown
     */
    private static void addText(final PDFDocument pdfDoc) throws Exception {
        // Read in a text and add each line as separate paragraph
        try (LayoutEngine layoutEngine = new LayoutEngine(pdfDoc)) {
            layoutEngine.add(new Paragraph("Hello World"));
        }
    }
}
