/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples;

import org.junit.runner.RunWith;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;

/**
 * Run all the integration tests.
 */
@RunWith(WildcardPatternSuite.class)
@SuiteClasses({ "**/*IntegrationTest.class", "**/*IT.class" })
public class AllIntegrationTests {

}
