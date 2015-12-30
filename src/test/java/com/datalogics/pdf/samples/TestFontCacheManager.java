/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples;

import com.datalogics.pdf.document.FontSetLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Manages a font cache for a test suite.
 *
 * <p>
 * Create one of these to set the font cache automatically, then close it to delete the font cache.
 */
public class TestFontCacheManager implements AutoCloseable {

    private static final String OUTPUT_PATH = "target/test-output/";
    private static final String FONT_CACHE_NAME = OUTPUT_PATH + "test-fonts.ser";

    private final String cacheName;
    private final String timeout;

    /**
     * Save the font cache properties, and set up a common cache for testing.
     *
     * @throws IOException an I/O operation failed or was interrupted
     */
    public TestFontCacheManager() throws IOException {
        // Create the output folder if necessary
        Files.createDirectories(Paths.get(OUTPUT_PATH));

        cacheName = System.getProperty(FontSetLoader.FONTSET_CACHE_NAME_PROPERTY);
        timeout = System.getProperty(FontSetLoader.FONTSET_CACHE_TIMEOUT_PROPERTY);

        System.setProperty(FontSetLoader.FONTSET_CACHE_NAME_PROPERTY, FONT_CACHE_NAME);
        System.setProperty(FontSetLoader.FONTSET_CACHE_TIMEOUT_PROPERTY, "0");
    }

    /* (non-Javadoc)
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        if (cacheName == null) {
            System.clearProperty(FontSetLoader.FONTSET_CACHE_NAME_PROPERTY);
        } else {
            System.setProperty(FontSetLoader.FONTSET_CACHE_NAME_PROPERTY, cacheName);
        }
        if (timeout == null) {
            System.clearProperty(FontSetLoader.FONTSET_CACHE_TIMEOUT_PROPERTY);
        } else {
            System.setProperty(FontSetLoader.FONTSET_CACHE_TIMEOUT_PROPERTY, timeout);
        }
    }

}
