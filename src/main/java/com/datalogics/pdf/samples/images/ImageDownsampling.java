/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.images;

import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidParameterException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
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

import java.io.InputStream;
import java.util.Iterator;

/**
 * This sample demonstrates how to downsample images that are in a PDF and replace the originals with the downsampled
 * versions.
 * <p>
 * The images in the input document are down-sampled using the Bicubic downsampling method by default.
 * </p>
 * Supported downsampling methods are:
 * <ul>
 * <li>Resampler.kResampleNearestNeighbor: Nearest Neighbor downsampling.</li>
 * <li>Resampler.kResampleBicubic: Bi-cubic downsampling.</li>
 * <li>Resampler.kResampleLinear: Linear downsampling.</li>
 * </ul>
 */
public final class ImageDownsampling {

    private static final String INPUT_IMAGE_PATH = "ducky.pdf";
    private static final String OUTPUT_IMAGE_PATH = "downsampled_ducky.pdf";


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

        String inputPath = null;
        String outputPath = null;
        if (args.length > 1) {
            inputPath = args[0];
            outputPath = args[1];
        } else {
            inputPath = INPUT_IMAGE_PATH;
            outputPath = OUTPUT_IMAGE_PATH;
        }

        final InputStream inputStream = ImageDownsampling.class.getResourceAsStream(inputPath);
        final PDFDocument pdfDoc = DocumentUtils.openPdfDocumentWithStream(inputStream);
        downsampleImage(pdfDoc);
        DocumentHelper.saveFullAndClose(pdfDoc, outputPath);
    }


    /**
     * This method is used to downsample an image using the Resample Bicubic method.
     *
     * @param pdfDoc PDFDocument
     * @throws PDFInvalidDocumentException a general problem with the PDF document, which may now be in an invalid state
     * @throws PDFIOException there was an error reading or writing a PDF file or temporary caches
     * @throws PDFSecurityException some general security issue occurred during the processing of the request
     * @throws PDFInvalidParameterException one or more parameters passed were invalid
     */
    public static void downsampleImage(final PDFDocument pdfDoc)
                    throws PDFInvalidDocumentException, PDFIOException,
                    PDFSecurityException, PDFInvalidParameterException {
        final double scaleFactor = 0.5; /* Valid range between 0-1 */
        final int method = Resampler.kResampleBicubic;
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
    }
}
