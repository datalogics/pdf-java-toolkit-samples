/*
 * Copyright 2017 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.util;


import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Collect up {@link ILoggingEvent} objects for testing.
 */
public class LogEventListCollector implements AutoCloseable {
    private ListAppender<ILoggingEvent> appender = new ListAppender<>();

    /**
     * Push a new {@link ListAppender} onto the root logger, and collect up events.
     */
    public LogEventListCollector() {
        final ThresholdFilter thresholdFilter = new ThresholdFilter();
        thresholdFilter.setLevel("TRACE");
        appender.addFilter(thresholdFilter);

        // If we have removed the appender, it gets stopped, so make sure it's started here.
        appender.start();

        getRootLogger().addAppender(appender);
    }

    private ch.qos.logback.classic.Logger getRootLogger() {
        return (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    }

    public List<ILoggingEvent> getEvents() {
        return new ArrayList<>(appender.list);
    }

    @Override public void close() throws Exception {
        appender.stop();
        getRootLogger().detachAppender(appender);
    }
}
