/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Convenience class to obtain a checksum for an InputStream.
 *
 * <p>
 * Based on this Stack Overflow post: http://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
 */
public class Checksum {
    /**
     * Calculate a checksum from an input stream.
     *
     * @param is the input stream to perform the checksum upon
     * @param algorithm an algorithm string compatible with {@link MessageDigest#getInstance(String)}, including "MD5"
     *        or "SHA-1"
     * @return the bytes comprising the checksum
     * @throws NoSuchAlgorithmException a particular cryptographic algorithm is requested but is not available in the
     *         environment
     * @throws IOException an I/O exception of some sort has occurred
     */
    public static byte[] createChecksum(final InputStream is, final String algorithm)
                    throws NoSuchAlgorithmException, IOException {
        final byte[] buffer = new byte[1024];
        final MessageDigest complete = MessageDigest.getInstance(algorithm);
        int numRead;

        do {
            numRead = is.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        is.close();
        return complete.digest();
    }

    /**
     * Get the MD5 checksum of an input stream, as a hex string.
     *
     * @param is the input stream to perform the checksum upon
     * @return the MD5 checksum as hex digits
     * @throws NoSuchAlgorithmException a particular cryptographic algorithm is requested but is not available in the
     *         environment
     * @throws IOException an I/O exception of some sort has occurred
     */
    public static String getMD5Checksum(final InputStream is) throws NoSuchAlgorithmException, IOException {
        final byte[] b = createChecksum(is, "MD5");
        return convertToHex(b);
    }

    /**
     * Get the SHA-1 checksum of an input stream, as a hex string.
     *
     * @param is the input stream to perform the checksum upon
     * @return the SHA-1 checksum as hex digits
     * @throws NoSuchAlgorithmException a particular cryptographic algorithm is requested but is not available in the
     *         environment
     * @throws IOException an I/O exception of some sort has occurred
     */
    public static String getSha1Checksum(final InputStream is) throws NoSuchAlgorithmException, IOException {
        final byte[] b = createChecksum(is, "SHA-1");
        return convertToHex(b);
    }

    /**
     * Convert a byte array to its hexadecimal equivalent.
     *
     * @param bytes an array of bytes containing the checksum/message-digest
     * @return the hexadecimal equivalent as a string
     */
    private static String convertToHex(final byte[] bytes) {
        final StringBuilder result = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            result.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

}
