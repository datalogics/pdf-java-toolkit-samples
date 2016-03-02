/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.util;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LogRecordListCollector implements AutoCloseable {
    private static class LogRecordHandler extends Handler {
        private final List<LogRecord> logRecordList;

        /**
         * Create a handler that records records to a list.
         *
         * @param logRecordList the list to receive the log records.
         */
        public LogRecordHandler(final List<LogRecord> logRecordList) {
            super();
            this.logRecordList = logRecordList;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
         */
        @Override
        public void publish(final LogRecord record) {
            logRecordList.add(record);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.logging.Handler#flush()
         */
        @Override
        public void flush() {}

        /*
         * (non-Javadoc)
         * 
         * @see java.util.logging.Handler#close()
         */
        @Override
        public void close() throws SecurityException {}
    }

    private final Logger targetLogger;
    private final Handler handler;


    /**
     * Make a list of every {@link LogRecord} sent to a {@link Logger}.
     *
     * @param targetLogger the logger to monitor
     * @param logRecordList a list to receive the
     */
    public LogRecordListCollector(final Logger targetLogger, final List<LogRecord> logRecordList) {
        super();
        this.targetLogger = targetLogger;
        this.handler = new LogRecordHandler(logRecordList);
        this.handler.setLevel(Level.ALL);
        this.targetLogger.addHandler(this.handler);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        targetLogger.removeHandler(handler);
    }

}
