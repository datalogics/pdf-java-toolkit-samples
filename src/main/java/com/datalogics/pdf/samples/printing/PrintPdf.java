/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.printing;

import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.InputStreamByteReader;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.services.rasterizer.PageRasterizer;
import com.adobe.pdfjt.services.rasterizer.RasterizationOptions;

import com.datalogics.pdf.document.FontSetLoader;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

    private static List<BufferedImage> images = new ArrayList<BufferedImage>();
    private static final String inputPDF = "http://dev.datalogics.com/cookbook/document/pdfjavatoolkit-ds.pdf";

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
        try {
            // If you are using an evaluation version of the product (License Managed, or LM), set the path to where
            // PDFJT can find the license file.
            //
            // If you are not using an evaluation version of the product you can ignore or remove this code.
            LicenseManager.setLicensePath(".");

            // Only log info messages and above
            LOGGER.setLevel(Level.INFO);

            // Read the PDF input file and detect the page size of the first page. This sample assumes all pages in
            // the document are the same size.
            final InputStream fis = new URL(inputPDF).openStream();
            final ByteReader byteReader = new InputStreamByteReader(fis);
            final PDFDocument pdfDocument = PDFDocument.newInstance(byteReader, PDFOpenOptions.newInstance());
            final PDFPage pdfPage = pdfDocument.requirePages().getPage(0);
            final int pdfPageWidth = (int) pdfPage.getMediaBox().width();
            final int pdfPageHeight = (int) pdfPage.getMediaBox().height();

            // Detect the resolution of the default printer.
            final PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
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

            // Create a bitmap for each page. NOTE: Acrobat and Reader will also create bitmaps when normal printing
            // does not produce the desired results.
            final PageRasterizer pageRasterizer = new PageRasterizer(pdfDocument.requirePages(), rasterizationOptions);
            while (pageRasterizer.hasNext()) {
                images.add(pageRasterizer.next());
            }

            // Print the images.
            final PrinterJob printerJob = PrinterJob.getPrinterJob();
            if (printerJob.printDialog()) {
                final PageFormat pageFormat = printerJob.defaultPage();
                final Paper paper = pageFormat.getPaper();
                paper.setSize(pdfPageWidth, pdfPageHeight);
                paper.setImageableArea(0, 0, pdfPageWidth, pdfPageHeight);
                pageFormat.setOrientation(PageFormat.PORTRAIT);
                pageFormat.setPaper(paper);
                final PageFormat validatePage = printerJob.validatePage(pageFormat);
                printerJob.setPrintable(new BufferedImagePrintable(), validatePage);
                printerJob.print();
            }
        } catch (final IOException | PrinterException exp) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.warning(exp.getMessage());
            }
        }
    }

    private static class BufferedImagePrintable implements Printable {
        @Override
        public int print(final Graphics g, final PageFormat pageFormat, final int pageIndex) throws PrinterException {
            if (pageIndex < images.size()) {
                final Graphics2D g2d = (Graphics2D) g;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g2d.drawImage(images.get(pageIndex), 0, 0, (int) pageFormat.getImageableWidth(),
                              (int) pageFormat.getImageableHeight(), null);
                g2d.dispose();
                return PAGE_EXISTS;
            } else {
                return NO_SUCH_PAGE;
            }
        }
    }
}
