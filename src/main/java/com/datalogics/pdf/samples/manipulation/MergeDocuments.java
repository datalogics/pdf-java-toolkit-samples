/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;

import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.InputStreamByteReader;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.core.types.ASRectangle;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.services.manipulations.PMMOptions;
import com.adobe.pdfjt.services.manipulations.PMMService;

import com.datalogics.pdf.document.DocumentHelper;

import java.io.InputStream;

/**
 * This sample shows how to merge two PDF documents into one. It will also show how to properly merge two PDF documents
 * that contain forms, links, bookmarks, and annotations.
 */
public final class MergeDocuments {

    private static final String outputPDFPath = "MergedDocument.pdf";
    private static final String[] fileNames = { "Merge1.pdf", "Merge2.pdf", "Merge3.pdf" };

    /**
     * This is a utility class, and won't be instantiated.
     */
    private MergeDocuments() {}

    /**
     * Main program.
     *
     * @param args The path to the merged output file
     * @throws Exception a general exception was thrown
     */
    public static void main(final String... args) throws Exception {
        // If you are using an evaluation version of the product (License Managed, or LM), set the path to where PDFJT
        // can find the license file.
        //
        // If you are not using an evaluation version of the product you can ignore or remove this code.
        LicenseManager.setLicensePath(".");

        if (args.length > 0) {
            run(args[0]);
        } else {
            run(outputPDFPath);
        }
    }

    public static void run(final String outputPath) throws Exception {
        // Start by creating a new PDF document that will be used to merge the other documents into. The new document
        // will contain a single blank page but we'll remove this just before saving the merged file.
        final PDFDocument mergedDocument = PDFDocument.newInstance(new ASRectangle(ASRectangle.US_LETTER),
                                                                   PDFOpenOptions.newInstance());

        // Create the new PMMService that will be used to manipulate the pages.
        final PMMService pmmService = new PMMService(mergedDocument);

        InputStream is = null;
        ByteReader byteReader = null;
        PDFDocument pdfToAppend = null;
        try {
            // Add the input files to the new PDF file. This process will append the pages from each document to the
            // end of the new document creating a continuous series of pages.
            for (final String pdfFileName : fileNames) {
                is = MergeDocuments.class.getResourceAsStream(pdfFileName);

                byteReader = new InputStreamByteReader(is);
                pdfToAppend = PDFDocument.newInstance(byteReader, PDFOpenOptions.newInstance());

                // Create the Bookmark Title String to imitate the behavior of Acrobat. This will be the title of the
                // new bookmark that wraps the bookmarks in the source document before it is added after the last
                // bookmark in the merged document. Acrobat uses the basefile name of the source PDF so we'll do that
                // too.
                final String documentBookmarkRootName = pdfFileName.substring(0, pdfFileName.length() - 4);

                // PMMOptions control what elements of the source document are copied into the target. "newInstanceAll"
                // will copy bookmarks, links, annotations, layer content (though not the layers themselves), form
                // fields, and structure.
                //
                // Form fields with the same name will be merged and assume the value of the first field encountered
                // during the appends.
                //
                // Bookmark destinations and links will be automatically resolved.
                pmmService.appendPages(pdfToAppend, documentBookmarkRootName, PMMOptions.newInstanceAll());
            }

            // Remove the first page. We don't need it anymore.
            mergedDocument.requirePages().removePage(mergedDocument.requirePages().getPage(0));

            // Save the file
            DocumentHelper.saveFullAndClose(mergedDocument, outputPath);

        } finally {
            mergedDocument.close();
            if (is != null) {
                is.close();
            }
            if (byteReader != null) {
                byteReader.close();
            }
            if (pdfToAppend != null) {
                pdfToAppend.close();
            }
        }

    }
}
