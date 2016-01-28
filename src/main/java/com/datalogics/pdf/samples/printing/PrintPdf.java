/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.printing;

import com.adobe.pdfjt.core.exceptions.PDFFontException;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidParameterException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.services.rasterizer.PageRasterizer;
import com.adobe.pdfjt.services.rasterizer.RasterizationOptions;

import com.datalogics.pdf.document.FontSetLoader;
import com.datalogics.pdf.samples.util.DocumentUtils;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterIOException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.standard.PrinterResolution;

/**
 * This sample shows how to rasterize a PDF page so it can be reliably printed. The code will attempt to detect the
 * resolution of your printer. The higher the resolution, the larger heap you'll need.
 */
public class PrintPdf {

    private static final Logger LOGGER = Logger.getLogger(PrintPdf.class.getName());
    private static final String DEFAULT_INPUT = "pdfjavatoolkit-ds.pdf";

    private static PageRasterizer pageRasterizer;

    /**
     * This is a utility class, and won't be instantiated.
     */
    private PrintPdf() {}

    /**
     * Main program.
     *
     * @param args command line arguments
     * @throws Exception a general exception was thrown
     */
    public static void main(final String... args) throws Exception {
        // If you are using an evaluation version of the product (License Managed, or LM), set the path to where
        // PDFJT can find the license file.
        //
        // If you are not using an evaluation version of the product you can ignore or remove this code.
        LicenseManager.setLicensePath(".");
        String path;
        if (args.length > 0) {
            path = args[0];
        } else {
            path = new URI(PrintPdf.class.getResource(DEFAULT_INPUT).toString()).getPath();
        }
        printPdf(path);
    }

    /**
     * Print the specified PDF.
     *
     * @param inputPath path to the PDF to print
     * @throws Exception a general exception was thrown
     */
    private static void printPdf(final String inputPath) throws Exception {
        // Only log info messages and above
        LOGGER.setLevel(Level.INFO);

        // Find the default printer.
        final PrintService printService = PrintServiceLookup.lookupDefaultPrintService();

        // If no printer is available, give up: we can't go any further.
        if (printService == null) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.severe("No printer available, aborting.");
            }
            throw new PrinterException("Printer failed to exist.");
        }

        try {
            // Read the PDF input file and detect the page size of the first page. This sample assumes all pages in
            // the document are the same size
            final PDFDocument pdfDocument = DocumentUtils.openPdfDocument(inputPath);
            final PDFPage pdfPage = pdfDocument.requirePages().getPage(0);
            final int pdfPageWidth = (int) pdfPage.getMediaBox().width();
            final int pdfPageHeight = (int) pdfPage.getMediaBox().height();

            // Describe the selected printer.
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Printer: " + printService.getName());
            }

            final PrinterResolution printerResolution =
                (PrinterResolution) printService.getDefaultAttributeValue(PrinterResolution.class);
            int resolution = 300;
            if (printerResolution != null) {
                resolution = printerResolution.getResolution(PrinterResolution.DPI)[0];
            }
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Resolution: " + resolution + " DPI");
            }

            // Create a default FontSetLoader. This will include the Base 14 fonts, plus all fonts in the standard
            // system locations.
            final FontSetLoader fontSetLoader = FontSetLoader.newInstance();

            // Create a set of options that will be used to rasterize the pages. We use the page width, height, and the
            // printer resolution to tell the Java Toolkit what dimensions the bitmap should be. Matching the resolution
            // of the printer will give us as high a quality output as the device is capable of.
            final RasterizationOptions rasterizationOptions = new RasterizationOptions();
            rasterizationOptions.setFontSet(fontSetLoader.getFontSet());
            rasterizationOptions.setWidth(pdfPageWidth / 72 * resolution);
            rasterizationOptions.setHeight(pdfPageHeight / 72 * resolution);

            // Use a PageRasterizer to create a bitmap for each page. NOTE: Acrobat and Reader will also create bitmaps
            // when normal printing does not produce the desired results.
            pageRasterizer = new PageRasterizer(pdfDocument.requirePages(), rasterizationOptions);

            // Print the images. We send them to the default printer without presenting a dialog panel to the user.
            // If we wanted to let the user select a printer, we could do so with "printerJob.printDialog()"
            // or similar.
            final PrinterJob printerJob = PrinterJob.getPrinterJob();
            final PageFormat pageFormat = printerJob.defaultPage();
            final Paper paper = pageFormat.getPaper();
            paper.setSize(pdfPageWidth, pdfPageHeight);
            paper.setImageableArea(0, 0, pdfPageWidth, pdfPageHeight);
            pageFormat.setOrientation(PageFormat.PORTRAIT);
            pageFormat.setPaper(paper);
            final PageFormat validatePage = printerJob.validatePage(pageFormat);
            printerJob.setPrintable(new BufferedImagePrintable(), validatePage);
            printerJob.print();
        } catch (final IOException | PrinterException exp) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.warning(exp.getMessage());
            }
        }
    }

    private static class BufferedImagePrintable implements Printable {
        @Override
        public int print(final Graphics gfx, final PageFormat pageFormat, final int pageIndex) throws PrinterException {
            if (pageRasterizer.hasNext()) {
                BufferedImage page = null;
                try {
                    page = pageRasterizer.next();
                } catch (PDFFontException | PDFInvalidDocumentException | PDFInvalidParameterException
                         | PDFIOException | PDFSecurityException e) {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE, "Error rasterizing a page", e);
                    }
                    // This double-wrap allows us to throw the rasterizer exception to the PrinterJob.
                    throw new PrinterIOException(new IOException("Error rasterizing a page", e));
                }
                final Graphics2D gfx2d = (Graphics2D) gfx;
                gfx2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                gfx2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                gfx2d.drawImage(page, 0, 0, (int) pageFormat.getImageableWidth(),
                              (int) pageFormat.getImageableHeight(), null);
                gfx2d.dispose();
                return PAGE_EXISTS;
            } else {
                return NO_SUCH_PAGE;
            }
        }
    }
}
