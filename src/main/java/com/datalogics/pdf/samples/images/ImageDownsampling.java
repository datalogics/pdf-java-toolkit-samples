/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.images;

import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidParameterException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.exceptions.PDFUnableToCompleteOperationException;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.core.types.ASName;
import com.adobe.pdfjt.image.Resampler;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFResources;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObject;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObjectImage;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObjectMap;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.services.imageconversion.ImageManager;

import com.datalogics.pdf.document.DocumentHelper;
import com.datalogics.pdf.samples.util.DocumentUtils;
import com.datalogics.pdf.samples.util.IoUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

/**
 * This sample demonstrates how to downsample images that are in a PDF and replace the originals with the downsampled
 * versions.
 * 
 * <p>
 * The downsampling process serves to reduce the size and resolution of images in a document and thus may also help
 * to reduce the size of the final PDF document, though a variety of other factors can affect the size of a PDF. 
 * 
 * <p>
 * The images in the input document are down-sampled using the Bicubic downsampling method by default.
 * 
 * <p>
 * Supported downsampling methods are:
 * <ul>
 * <li>Resampler.kResampleNearestNeighbor: Nearest Neighbor downsampling.</li>
 * <li>Resampler.kResampleBicubic: Bi-cubic downsampling.</li>
 * <li>Resampler.kResampleLinear: Linear downsampling.</li>
 * </ul>
 */
public final class ImageDownsampling {

    public static final String INPUT_IMAGE_PATH = "ducky.pdf";
    public static final String OUTPUT_IMAGE_PATH = "downsampled_ducky.pdf";


    /**
     * This is a utility class, and won't be instantiated.
     */
    private ImageDownsampling() {}

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

        URL inputUrl = null;
        URL outputUrl = null;
        if (args.length > 1) {
            inputUrl = IoUtils.createUrlFromPath(args[0]);
            outputUrl = IoUtils.createUrlFromPath(args[1]);
        } else {
            inputUrl = ImageDownsampling.class.getResource(INPUT_IMAGE_PATH);
            outputUrl = IoUtils.createUrlFromPath(OUTPUT_IMAGE_PATH);
        }

        // final PDFDocument pdfDoc = getPdfDocument(i);
        downsampleImage(inputUrl, outputUrl);
    }


    /**
     * This method is used to downsample an image using the Resample Bicubic method.
     *
     * @param inputUrl the path to the input document
     * @param outputUrl the path to the output document
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFInvalidParameterException one or more of the parameters passed to a method is invalid
     * @throws IOException an I/O operation failed or was interrupted
     * @throws PDFUnableToCompleteOperationException the operation was unable to be completed
     */
    public static void downsampleImage(final URL inputUrl, final URL outputUrl)
                    throws PDFInvalidDocumentException, PDFIOException,
                    PDFSecurityException, PDFInvalidParameterException, IOException,
                    PDFUnableToCompleteOperationException {
        final double scaleFactor = 0.5; /* Valid range between 0-1 */
        final int method = Resampler.kResampleBicubic;
        PDFDocument pdfDoc = null;
        try {
            pdfDoc = DocumentUtils.openPdfDocument(inputUrl);
            /*
             * Downsample all images in the doc and replace the original images with the resampled images.
             */
            final Iterator<PDFPage> pagesIter = pdfDoc.requirePages().iterator();
            while (pagesIter.hasNext()) {
                final PDFPage pdfPage = pagesIter.next();
                final PDFResources pdfResources = pdfPage.getResources();
                if (pdfResources != null) {
                    final PDFXObjectMap xobjMap = pdfResources.getXObjectMap();
                    if (xobjMap != null) {
                        final Iterator<ASName> xobjIter = xobjMap.keySet().iterator();
                        while (xobjIter.hasNext()) {
                            final ASName key = xobjIter.next();
                            final PDFXObject xobj = xobjMap.get(key);
                            if (xobj instanceof PDFXObjectImage) {
                                final PDFXObjectImage originalImage = (PDFXObjectImage) xobj;
                                final PDFXObjectImage resampledImage = ImageManager.resampleXObjImage(pdfDoc,
                                                                                                  originalImage,
                                                                                                  scaleFactor,
                                                                                                  scaleFactor,
                                                                                                  method);
                                xobjMap.set(key, resampledImage);
                            }
                        }
                    }
                }
            }

            DocumentHelper.saveFullAndClose(pdfDoc, outputUrl.toURI().getPath());
        } catch (final URISyntaxException e) {
            throw new PDFIOException(e);
        } finally {
            if (pdfDoc != null) {
                pdfDoc.close();
            }
        }

    }
}
