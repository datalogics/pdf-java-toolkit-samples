/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.images;

import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.InputStreamByteReader;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidParameterException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.exceptions.PDFUnableToCompleteOperationException;
import com.adobe.pdfjt.core.license.LicenseManager;
import com.adobe.pdfjt.core.types.ASName;
import com.adobe.pdfjt.image.Resampler;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.document.PDFResources;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObject;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObjectImage;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObjectMap;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.services.imageconversion.ImageManager;

import com.datalogics.pdf.document.DocumentHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * This sample demonstrates how to downsample images that are in a PDF and replace the originals with the downsampled
 * versions.
 */
public final class ImageDownsampling {

    private static final String INPUT_IMAGE_PATH = "ducky.pdf";
    private static final String OUTPUT_IMAGE_PATH = "Resampled_ducky_";


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
        String path;
        if (args.length > 0) {
            path = args[0];
        } else {
            path = OUTPUT_IMAGE_PATH;
        }
        run(path);
    }

    static void run(final String outputPath) throws Exception {

        final int[] resampleMethods = { Resampler.kResampleNearestNeighbor, Resampler.kResampleBicubic,
            Resampler.kResampleLinear };

        for (final int method : resampleMethods) {
            final PDFDocument pdfDoc = getPdfDocument();
            downsampleImage(pdfDoc, method);
            DocumentHelper.saveFullAndClose(pdfDoc, outputPath + getResampleMethodString(method) + ".pdf");
        }
    }

    public static void downsampleImage(final PDFDocument pdfDoc, final int method)
                    throws PDFInvalidDocumentException, PDFIOException,
                    PDFSecurityException, PDFInvalidParameterException {
        final double scaleFactor = 0.5;
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

    public static String getResampleMethodString(final int method) {
        final String[] methodStrings = new String[] { "**invalid**", "NearestNeighbor", "Bicubic", "Linear" };
        return methodStrings[method];
    }

    private static PDFDocument getPdfDocument() throws PDFInvalidDocumentException, PDFIOException,
                    PDFSecurityException, PDFUnableToCompleteOperationException, IOException {
        final InputStream inputStream = ImageDownsampling.class.getResourceAsStream(INPUT_IMAGE_PATH);
        if (inputStream == null) {
            throw new PDFIOException("ImageDownsampling: Could not find input pdf file.");
        }

        PDFDocument pdfDoc = null;
        ByteReader byteReader = null;

        byteReader = new InputStreamByteReader(inputStream);
        pdfDoc = PDFDocument.newInstance(byteReader, PDFOpenOptions.newInstance());

        return pdfDoc;
    }
}
