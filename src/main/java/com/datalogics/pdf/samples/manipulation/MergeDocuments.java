/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.manipulation;

import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.InputStreamByteReader;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.core.types.ASRectangle;
import com.adobe.pdfjt.pdf.document.PDFCatalog;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.interactive.PDFViewerPreferences;
import com.adobe.pdfjt.pdf.page.PDFPageLayout;
import com.adobe.pdfjt.pdf.page.PDFPageMode;
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
     * @param args command line arguments
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
            // If nothing was passed in, use our default list of files.
            run(outputPDFPath);
        }
    }

    public static void run(final String outputPath) throws Exception {
        // Start by creating a new PDF document that will be used to merge the other documents into. The new document
        // will contain a single blank page but we'll remove this just before saving the merged file.
        final PDFDocument mergedDocument = PDFDocument.newInstance(new ASRectangle(new double[] { 0, 0, 612, 792 }),
                                                                   PDFOpenOptions.newInstance());

        // Setting the initial view is not required. However, the PMMService which is used to merge the documents will
        // also merge the bookmarks from the individual files. Setting the initial view to display the bookmarks makes
        // it easier to see that the sample has worked correctly.
        setInitialView(mergedDocument, true, PDFPageLayout.SinglePage, PDFPageMode.WithBookmarks);

        // Create the new PMMService that will be used to manipulate the pages.
        final PMMService pmmService = new PMMService(mergedDocument);

        InputStream is = null;
        ByteReader byteReader = null;
        PDFDocument pdfToAppend = null;
        try {
            // Add the files in the input directory to the new PDF file. This process will append the pages from each
            // document to the end of the new document creating a continuous series of pages.
            // Folders will be skipped.
            for (final String pdfFileName : fileNames) {
                is = MergeDocuments.class.getResourceAsStream(pdfFileName);

                byteReader = new InputStreamByteReader(is);
                pdfToAppend = PDFDocument.newInstance(byteReader, PDFOpenOptions.newInstance());

                // Create the Bookmark Title String to imitate the behavior of Acrobat. This will be the title of the
                // new bookmark that wraps the bookmarks in the source document before it is added after the last
                // bookmark in the merged document. Acrobat uses the basefile name of the source PDF so we'll do that
                // too.
                final String documentBookmarkRootName = pdfFileName.substring(0, pdfFileName.length() - 4);

                // Here's the interesting part. PMMOptions control what elements of the source document are copied into
                // the target. "All" will copy bookmarks, links, annotations, layer content (though not the layers
                // themselves), form fields, and structure.
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

    /**
     * Sets the initial view of the PDF file passed.
     *
     * @param pdfDocument The PDFDocument object in question
     * @param fitWindow Set the initial zoom of the PDF to fit page if true
     * @param layout Sets the initial page layout in the PDF viewer
     * @param mode Sets the initial page mode to display one of the navigational tabs in the PDF viewer
     * @return none
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     */
    private static void setInitialView(final PDFDocument pdfDocument,
                                       final boolean fitWindow, final PDFPageLayout layout, final PDFPageMode mode)
                                       throws PDFInvalidDocumentException, PDFIOException, PDFSecurityException {
        final PDFCatalog catalog = pdfDocument.requireCatalog();
        catalog.setPageLayout(layout);
        catalog.setPageMode(mode);
        final PDFViewerPreferences pdfViewerPreferences = PDFViewerPreferences.newInstance(pdfDocument);
        pdfViewerPreferences.setFitWindow(fitWindow);
        pdfDocument.setViewerPreferences(pdfViewerPreferences);
    }
}
