/*
 * Copyright 2017 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.util;

import com.adobe.pdfjt.Version;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class that holds methods related to checking the version of PDF Java Toolkit components.
 */
public final class VersionUtils {

    private VersionUtils() {}

    /**
     * Checks if the version string passed in matches the string for the current PDFJT.
     *
     * @param versionToCompare String specifying version to check against
     * @return true if the version of PDFJT is before the version specified as an argument
     * @throws IOException an I/O operation failed or was interrupted
     */
    public static boolean pdfjtIsBeforeVersion(final String versionToCompare) throws IOException {
        try (final InputStream propertiesStream = Version.class.getResourceAsStream("version.properties")) {
            final Properties versionProperties = new Properties();
            versionProperties.load(propertiesStream);
            final String pdfjtVersion = versionProperties.getProperty("Implementation-Version");

            final DefaultArtifactVersion pdfjtArtifactVersion = new DefaultArtifactVersion(pdfjtVersion);
            final DefaultArtifactVersion version = new DefaultArtifactVersion(versionToCompare);
            return pdfjtArtifactVersion.compareTo(version) < 0;
        }
    }

}
