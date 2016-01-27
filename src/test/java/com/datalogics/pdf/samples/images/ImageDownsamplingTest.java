/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.images;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertTrue;

import com.adobe.internal.io.ByteReader;
import com.adobe.internal.io.InputStreamByteReader;
import com.adobe.pdfjt.core.exceptions.PDFIOException;
import com.adobe.pdfjt.core.exceptions.PDFInvalidDocumentException;
import com.adobe.pdfjt.core.exceptions.PDFSecurityException;
import com.adobe.pdfjt.core.types.ASName;
import com.adobe.pdfjt.image.Resampler;
import com.adobe.pdfjt.pdf.document.PDFDocument;
import com.adobe.pdfjt.pdf.document.PDFOpenOptions;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObject;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObjectImage;
import com.adobe.pdfjt.pdf.graphics.xobject.PDFXObjectMap;
import com.adobe.pdfjt.pdf.page.PDFPage;
import com.adobe.pdfjt.services.imageconversion.ImageManager;

import com.datalogics.pdf.samples.SampleTest;
import com.datalogics.pdf.samples.util.Checksum;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests the ImageDownsamplingTest Sample.
 */
@RunWith(Parameterized.class)
public class ImageDownsamplingTest extends SampleTest {
    private static final String FILE_NAME = "downsampled_ducky.pdf";
    private static final String ORIGINAL_FILE_NAME = "ducky.pdf";
    private static final double SCALE_FACTOR = 0.5;

    private final DownsamplingTest params;

    public ImageDownsamplingTest(final DownsamplingTest parameters) {
        this.params = parameters;
    }

    /**
     * Return an iterable with all the parameters for running these tests. In a parameterized test, a test case object
     * is instantiated with each set of parameters, and then the runner runs the tests in the case. The beauty is that
     * the name listed in the Parameters annotation is shown for each test, so it's easy to associate each image with
     * the errors it gives.
     *
     * @return Iterable
     * @throws Exception This method return a general Exception.
     */
    @Parameters(name = "image: {0}")
    public static Iterable<Object[]> parameters() throws Exception {
        final List<Object[]> parameters = new ArrayList<Object[]>() { // Create anonymous inner class.
            private static final long serialVersionUID = 7720303970424830948L;

            private void add(final DownsamplingTest params) {
                add(new Object[] { params });
            }

            // Instance initialization block
            {
                final DownsamplingTest.Builder builder = new DownsamplingTest.Builder(FILE_NAME);
                builder.imageColorSpace(ASName.k_ICCBased).imageCompression(ASName.k_DCTDecode);

                // Note that you can modify the builder repeatedly and use it to make more tests.
                add(builder.method(Resampler.kResampleBicubic)
                           .imageChecksum("f214dfd6bf398b1a66cf569350335d2c").build());
            }
        };
        return parameters;
    }

    @Test
    public void testMain() throws Exception {
        final File file = newOutputFileWithDelete(params.getFileName());

        /*
         * Run sample which generates files using all three methods: {NearestNeighbor, Bicubic, Linear}
         */
        final String outputPath = newOutputFile(FILE_NAME).getCanonicalPath();

        ImageDownsampling.main(ORIGINAL_FILE_NAME, outputPath);

        // Make sure the Output file exists.
        assertTrue(file.getPath() + " must exist after run", file.exists());

        // Downsample the original image PDF file.
        final InputStream inputStream = ImageDownsamplingTest.class.getResourceAsStream(ORIGINAL_FILE_NAME);
        final ByteReader byteReader = new InputStreamByteReader(inputStream);
        PDFDocument pdfDoc = PDFDocument.newInstance(byteReader, PDFOpenOptions.newInstance());
        PDFPage page = pdfDoc.requirePages().getPage(0);
        PDFXObjectMap objMap = page.getResources().getXObjectMap();
        int images = 0;
        int newWidth = -1;
        int newHeight = -1;

        for (final ASName name : objMap.keySet()) {
            final PDFXObject o = objMap.get(name);
            if (o instanceof PDFXObjectImage) {
                assertThat("there should only be one image on the first page of the test document", images++,
                           equalTo(0));
                final PDFXObjectImage originalImage = (PDFXObjectImage) o;
                final PDFXObjectImage resampledImage = ImageManager.resampleXObjImage(pdfDoc, originalImage,
                                                                                      SCALE_FACTOR, SCALE_FACTOR,
                                                                                      params.getMethod());
                newWidth = (int) Math.round(originalImage.getWidth() * SCALE_FACTOR);
                newHeight = (int) Math.round(originalImage.getHeight() * SCALE_FACTOR);

                objMap.set(name, resampledImage);
            }
        }
        assertThat(images, equalTo(1));

        // Read the document output from the ImageDownsampling Sample.
        pdfDoc = openPdfDocument(file.getCanonicalPath());
        page = pdfDoc.requirePages().getPage(0);
        objMap = page.getResources().getXObjectMap();
        images = 0;
        for (final ASName name : objMap.keySet()) {
            final PDFXObject o = objMap.get(name);
            if (o instanceof PDFXObjectImage) {
                assertThat("there should only be one image on the first page of the test document", images++,
                           equalTo(0));
                final PDFXObjectImage resampledImage = (PDFXObjectImage) o;

                // Check characteristics of the main image
                if (params.getImageChecksum() == null) {
                    assertThat("resampled image has correct size",
                               resampledImage, allOf(hasProperty("width", equalTo(newWidth)),
                                                     hasProperty("height", equalTo(newHeight))));
                } else {
                    assertThat("resampled image has correct size and contents",
                               resampledImage, allOf(hasProperty("width", equalTo(newWidth)),
                                                     hasProperty("height", equalTo(newHeight)),
                                                     hasChecksum(params.getImageChecksum())));
                }
                assertThat("resampled image has one input filter",
                           resampledImage.getInputFilters().size(), equalTo(1));
                assertThat("resampled image has expected compression",
                           resampledImage.getInputFilters().get(0).getFilterName(),
                           equalTo(params.getImageCompression()));
                assertThat("resampled image has 8 bits per component",
                           resampledImage.getBitsPerComponent(), equalTo(8));
                assertThat("resampled image has expected color space",
                           resampledImage.getColorSpace().getName(), equalTo(params.getImageColorSpace()));
            }
        }
        assertThat(images, equalTo(1));
    }

    /**
     * Check that a {@link PDFXObjectImage} has a particular checksum.
     *
     * @param checksum the image checksum to check for
     * @return a {@link Matcher}
     */
    private Matcher<PDFXObjectImage> hasChecksum(final String checksum) {
        // see http://www.planetgeek.ch/2012/03/07/create-your-own-matcher/ for an explanation
        return new ImageFeatureMatcher(equalTo(checksum), "has checksum", "checksum");
    }


    /**
     * Supporting class for matching feature of an image.
     */
    private static final class ImageFeatureMatcher extends FeatureMatcher<PDFXObjectImage, String> {

        private ImageFeatureMatcher(final Matcher<? super String> subMatcher, final String featureDescription,
                                    final String featureName) {
            super(subMatcher, featureDescription, featureName);
        }

        private void throwChecksumError(final Throwable exception) {
            throw new IllegalStateException("Getting an image checksum threw " + exception, exception);
        }

        @Override
        protected String featureValueOf(final PDFXObjectImage image) {
            final PDFXObjectImage xObjectImage = image;
            try {
                return Checksum.getMD5Checksum(xObjectImage.getImageStreamData());
            } catch (final PDFInvalidDocumentException e) {
                throwChecksumError(e);
            } catch (final PDFIOException e) {
                throwChecksumError(e);
            } catch (final PDFSecurityException e) {
                throwChecksumError(e);
            } catch (final Exception e) {
                throwChecksumError(e);
            }
            return null;
        }
    }

    /**
     * Parameters for tests. Note that there are no setters; this class should be built with its Builder.
     */
    private static class DownsamplingTest {
        private String fileName;
        private int method;
        private String imageChecksum;
        private ASName imageColorSpace;
        private ASName imageCompression;

        private DownsamplingTest() {}

        private DownsamplingTest(final Builder builder) {
            fileName = builder.fileName;
            imageChecksum = builder.imageChecksum;
            method = builder.method;
            imageCompression = builder.imageCompression;
            imageColorSpace = builder.imageColorSpace;
        }

        /**
         * Get downsampling method (int).
         *
         * @return int
         */
        public int getMethod() {
            return method;
        }

        /**
         * Get the image checksum.
         *
         * @return String
         */
        public String getImageChecksum() {
            return imageChecksum;
        }

        /**
         * Get the file name.
         *
         * @return String
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * Get image Colorspace.
         *
         * @return ASName
         */
        public ASName getImageColorSpace() {
            return imageColorSpace;
        }

        /**
         * Get image compression.
         *
         * @return ASName
         */
        public ASName getImageCompression() {
            return imageCompression;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "fileName=" + fileName;
        }

        public static class Builder {
            private final String fileName;
            private String imageChecksum = null;
            private int method = 0;
            private ASName imageColorSpace = null;
            private ASName imageCompression = null;

            public Builder(final String fileName) {
                this.fileName = fileName;
            }

            public Builder method(final int method) {
                this.method = method;
                return this;
            }

            public Builder imageChecksum(final String imageChecksum) {
                this.imageChecksum = imageChecksum;
                return this;
            }

            public Builder imageColorSpace(final ASName imageColorSpace) {
                this.imageColorSpace = imageColorSpace;
                return this;
            }

            public Builder imageCompression(final ASName imageCompression) {
                this.imageCompression = imageCompression;
                return this;
            }

            public DownsamplingTest build() {
                return new DownsamplingTest(this);
            }
        }
    }
}
