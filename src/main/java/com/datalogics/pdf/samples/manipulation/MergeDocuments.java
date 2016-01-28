/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;

import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.core.types.ASRectangle;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.services.manipulations.PMMOptions;
import com.adobe.pdfjt.services.manipulations.PMMService;

import com.datalogics.pdf.document.DocumentHelper;
import com.datalogics.pdf.samples.util.DocumentUtils;

/**
 * This sample shows how to merge two PDF documents into one. It will also show how to properly merge PDF documents that
 * contain forms, links, bookmarks, and annotations.
 */
public final class MergeDocuments {

    private static final String OUTPUT_PDF_PATH = "MergedDocument.pdf";
    public static final String FIRST_DOCUMENT = "Merge1.pdf";
    public static final String SECOND_DOCUMENT = "Merge2.pdf";

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
            mergeTwoDocuments(args[0]);
        } else {
            mergeTwoDocuments(OUTPUT_PDF_PATH);
        }
    }

    /**
     * Merge two sample documents, with the output written to the given path.
     *
     * @param outputPath the path to the file to contain the output document
     * @throws Exception a general exception was thrown
     */
    public static void mergeTwoDocuments(final String outputPath) throws Exception {
        // Start by creating a new PDF document that will be used to merge the other documents into. The new document
        // will contain a single blank page but we'll remove this just before saving the merged file.
        final PDFDocument mergedDocument = PDFDocument.newInstance(new ASRectangle(ASRectangle.US_LETTER),
                                                                   PDFOpenOptions.newInstance());

        // Append each document to the blank one.
        appendDocument(FIRST_DOCUMENT, mergedDocument);
        appendDocument(SECOND_DOCUMENT, mergedDocument);

        // Remove the first page. We don't need it anymore.
        mergedDocument.requirePages().removePage(mergedDocument.requirePages().getPage(0));

        // Save the file.
        DocumentHelper.saveFullAndClose(mergedDocument, outputPath);
    }

    /**
     * Append a PDF file to the end of an existing PDF document.
     *
     * @param resourceName The name of the resource to be appended
     * @param pdfDocument The document object to which a new PDF should be appended
     * @throws Exception a general exception was thrown
     */
    private static void appendDocument(final String resourceName, final PDFDocument pdfDocument) throws Exception {
        PDFDocument pdfToAppend = null;

        // Create the new PMMService that will be used to manipulate the pages.
        final PMMService pmmService = new PMMService(pdfDocument);

        try {
            // Read in the input file.
            pdfToAppend = DocumentUtils.openPdfDocument(MergeDocuments.class.getResource(resourceName).getPath());


            // Create the Bookmark Title String to imitate the behavior of Acrobat. This will be the title of the
            // new bookmark that wraps the bookmarks in the source document before it is added after the last
            // bookmark in the merged document. Acrobat uses the base filename of the source PDF so we'll do that
            // too.
            final String documentBookmarkRootName = resourceName.substring(0, resourceName.length() - 4);

            // PMMOptions control what elements of the source document are copied into the target. "newInstanceAll"
            // will copy bookmarks, links, annotations, layer content (though not the layers themselves), form
            // fields, and structure.
            //
            // Form fields with the same name will be merged and assume the value of the first field encountered
            // during the appends.
            //
            // Bookmark destinations and links will be automatically resolved.
            pmmService.appendPages(pdfToAppend, documentBookmarkRootName, PMMOptions.newInstanceAll());
        } finally {
            if (pdfToAppend != null) {
                pdfToAppend.close();
            }
        }
    }
}
