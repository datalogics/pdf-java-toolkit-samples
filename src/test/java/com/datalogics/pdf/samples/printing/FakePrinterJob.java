/*
 * Copyright 2015 Datalogics, Inc.
 */

package com.datalogics.pdf.samples.printing;

import static com.datalogics.pdf.samples.printing.FakePrintService.DOTS_PER_INCH;

import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterIOException;
import java.awt.print.PrinterJob;

/**
 * A fake printer job.
 *
 * <p>
 * This {@link PrinterJob} pretends accept pages, and in fact draws them to buffered images, but does nothing with them.
 * A subclass can override {@link FakePrinterJob#processPageImage(BufferedImage, int)} to take actions with the image.
 */
public class FakePrinterJob extends PrinterJob {

    protected Printable painter;
    protected PageFormat format;
    static final double DOTS_PER_POINT = DOTS_PER_INCH / 72.0;

    /**
     * Construct a printer job.
     *
     * <p>
     * The actual printer job should be created using the {@link PrinterJob#getPrinterJob()} method.
     */
    public FakePrinterJob() {
        super();
        painter = null;
        format = null;
    }

    @Override
    public PageFormat defaultPage(final PageFormat page) {
        return (PageFormat) page.clone();
    }

    @Override
    public PageFormat validatePage(final PageFormat page) {
        // Factor our reported DPI into the final page size
        final PageFormat stretchedPageFormat = (PageFormat) page.clone();
        final Paper paper = stretchedPageFormat.getPaper();

        final double x = stretchedPageFormat.getImageableX();
        final double y = stretchedPageFormat.getImageableY();
        final double width = (stretchedPageFormat.getImageableWidth() - x) * DOTS_PER_POINT;
        final double height = (stretchedPageFormat.getImageableHeight() - y) * DOTS_PER_POINT;

        paper.setSize(width, height);
        paper.setImageableArea(x, y, width, height);
        stretchedPageFormat.setPaper(paper);
        return stretchedPageFormat;
    }

    /*
     * Print the document.
     */
    @Override
    public void print() throws PrinterException {
        // Create a BufferedImage to render into
        final int width = (int) (format.getImageableWidth() - format.getImageableX());
        final int height = (int) (format.getImageableHeight() - format.getImageableY());
        // NOTE: We use a TYPE_4BYTE_ABGR because it is guaranteed to use a single contiguous
        // image data buffer. This lets us checksum the raw data for the entire image.
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);

        Graphics2D gfx2d = image.createGraphics();
        int pageIndex = 0;

        while (painter.print(gfx2d, format, pageIndex) == Printable.PAGE_EXISTS) {
            processPageImage(image, pageIndex);

            // painter.print() disposed of the Graphics2D, obtain a new one
            gfx2d = image.createGraphics();
            gfx2d.clearRect(0, 0, width, height);
            pageIndex++;
        }
    }

    /**
     * Process a page image during the printing loop.
     *
     * <p>
     * When fake-printing, a page image is created for each page, and then passed to this routine, which does nothing.
     * Override this to perform actions in your test or application.
     *
     * @param image the {@link BufferedImage} containing the printed page
     * @param pageIndex the index of the page within the print job
     * @throws PrinterIOException ex
     */
    public void processPageImage(final BufferedImage image, final int pageIndex) throws PrinterIOException {}

    @Override
    public void setPrintable(final Printable painter, final PageFormat format) {
        this.painter = painter;
        this.format = format;
    }

    @Override
    public void setPrintable(final Printable painter) {}

    @Override
    public void setPageable(final Pageable document) throws NullPointerException {}

    @Override
    public boolean printDialog() throws HeadlessException {
        return false;
    }

    @Override
    public PageFormat pageDialog(final PageFormat page) throws HeadlessException {
        return null;
    }

    @Override
    public void setCopies(final int copies) {}

    @Override
    public int getCopies() {
        return 0;
    }

    @Override
    public String getUserName() {
        return null;
    }

    @Override
    public void setJobName(final String jobName) {}

    @Override
    public String getJobName() {
        return null;
    }

    @Override
    public void cancel() {}

    @Override
    public boolean isCancelled() {
        return false;
    }

}
