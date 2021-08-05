/*
 * Copyright 2020 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.util;

import org.apache.commons.lang3.SystemUtils;

/**
 * Describe the characteristics of the operating environment.
 */
public class EnvironmentUtils {
    public static final boolean IS_OPENJDK_8 = SystemUtils.IS_JAVA_1_8
                                               && SystemUtils.JAVA_VM_NAME.startsWith("OpenJDK");
}
