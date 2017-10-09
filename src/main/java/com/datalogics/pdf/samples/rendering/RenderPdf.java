/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.rendering;

import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.services.rasterizer.PageRasterizer;
import com.adobe.pdfjt.services.rasterizer.RasterizationOptions;

import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.FontUtils;
import com.datalogics.pdf.samples.util.IoUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;

import javax.imageio.ImageIO;

/**
 * This sample shows how to render the pages of a PDF. The higher the resolution, the larger heap you'll need.
 */
public final class RenderPdf {

    private static final int RESOLUTION = 300;
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final String DEFAULT_INPUT = "/com/datalogics/pdf/samples/printing/pdfjavatoolkit-ds.pdf";

    private static PageRasterizer pageRasterizer;

    /**
     * This is a utility class, and won't be instantiated.
     */
    private RenderPdf() {}

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
        URL inputUrl = null;
        if (args.length > 0) {
            inputUrl = IoUtils.createUrlFromPath(args[0]);
        } else {
            inputUrl = RenderPdf.class.getResource(DEFAULT_INPUT);
        }

        renderPdf(inputUrl);
    }

    /**
     * Render the specified PDF at the default resolution.
     *
     * @param inputUrl path to the PDF to render
     * @throws Exception a general exception was thrown
     */
    public static void renderPdf(final URL inputUrl) throws Exception {
        renderPdf(inputUrl, RESOLUTION);
    }


    /**
     * Render the specified PDF.
     *
     * @param inputUrl path to the PDF to render
     * @param resolution the desired resolution in dpi
     * @throws Exception a general exception was thrown
     */
    public static void renderPdf(final URL inputUrl, final int resolution) throws Exception {
        final String path = URLDecoder.decode(inputUrl.getPath(), "UTF-8");
        final int slash = path.lastIndexOf('/');
        final String imageBaseName = path.substring(slash + 1);
        final URL outputBaseUrl = new File(imageBaseName).toURI().toURL();
        renderPdf(inputUrl, resolution, outputBaseUrl);
    }

    /**
     * Render the specified PDF.
     *
     * @param inputUrl path to the PDF to render
     * @param resolution the desired resolution in dpi
     * @param outputBaseUrl the URL for the output file, to which will be added the page number and ".png" extension.
     * @throws Exception a general exception was thrown
     */
    public static void renderPdf(final URL inputUrl, final int resolution, final URL outputBaseUrl) throws Exception {
        // Read the PDF input file and detect the page size of the first page. This sample assumes all pages in
        // the document are the same size.
        final PDFDocument pdfDocument = DocumentUtils.openPdfDocument(inputUrl);
        final PDFPage pdfPage = pdfDocument.requirePages().getPage(0);
        final int pdfPageWidth = (int) pdfPage.getMediaBox().width();
        final int pdfPageHeight = (int) pdfPage.getMediaBox().height();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Resolution: " + resolution + " DPI");
        }

        // Create a set of options that will be used to rasterize the pages. We use the page width, height, and the
        // desired resolution to tell the Java Toolkit what dimensions the bitmap should be.
        final RasterizationOptions rasterizationOptions = new RasterizationOptions();
        rasterizationOptions.setFontSet(FontUtils.getDocFontSet(pdfDocument));
        rasterizationOptions.setWidth(pdfPageWidth / 72 * resolution);
        rasterizationOptions.setHeight(pdfPageHeight / 72 * resolution);

        // Use a PageRasterizer to create a bitmap for each page.
        pageRasterizer = new PageRasterizer(pdfDocument.requirePages(), rasterizationOptions);

        int pageNo = 0;

        while (pageRasterizer.hasNext()) {
            pageNo += 1;
            final BufferedImage page = pageRasterizer.next();
            savePageAsPng(outputBaseUrl, pageNo, page);
            savePageAsJpeg(outputBaseUrl, pageNo, page);
        }
    }

    /**
     * Save one page to a PNG file.
     *
     * @param imageBaseUrl the URL to the image output
     * @param pageNo the pageNumber
     * @param page the image of the page
     * @throws IOException an I/O operation failed or was interrupted
     * @throws URISyntaxException a string could not be parsed as a URI reference
     */
    private static void savePageAsPng(final URL imageBaseUrl, final int pageNo, final BufferedImage page)
                    throws IOException, URISyntaxException {
        final File outputFile = new File(imageBaseUrl.toURI().getPath() + "." + pageNo + ".png");
        // Saving raster image
        ImageIO.write(page, "png", outputFile);
    }

    /**
     * Save one page to a JPEG file.
     *
     * @param imageBaseUrl the URL to the image output
     * @param pageNo the pageNumber
     * @param page the image of the page
     * @throws IOException an I/O operation failed or was interrupted
     * @throws URISyntaxException a string could not be parsed as a URI reference
     */
    private static void savePageAsJpeg(final URL imageBaseUrl, final int pageNo, final BufferedImage page)
                    throws IOException, URISyntaxException {
        final File outputFile = new File(imageBaseUrl.toURI().getPath() + "." + pageNo + ".jpg");

        // NOTE: Convert the RGBA image to RGB before passing off to the JPEG encoder. Otherwise, the ImageIO JPEG image
        // writer will write a JPEG-encoded RGBA image that few readers will correctly decode.
        final BufferedImage rgbPage = convertToRgb(page);

        // Saving raster image
        ImageIO.write(rgbPage, "jpeg", outputFile);
    }

    /**
     * Convert a {@link BufferedImage} to RGB.
     *
     * @param input the {@link BufferedImage} to convert
     * @return the resulting {@link BufferedImage}
     */
    private static BufferedImage convertToRgb(final BufferedImage input) {
        final BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(),
                                                       BufferedImage.TYPE_INT_RGB);
        final Graphics graphics = output.getGraphics();
        try {
            graphics.drawImage(input, 0, 0, input.getWidth(), input.getHeight(), null);
        } finally {
            graphics.dispose();
        }
        return output;
    }
}
