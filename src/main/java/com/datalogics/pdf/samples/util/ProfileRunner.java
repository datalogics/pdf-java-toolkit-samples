
package com.datalogics.pdf.samples.util;

import com.datalogics.pdf.samples.rendering.RenderPdf;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


/**
 * Test class to run some code in a Swing app for profiling.
 *
 */
public final class ProfileRunner {

    private static final Logger LOGGER = Logger.getLogger(ProfileRunner.class.getName());

    private ProfileRunner() {
        // Private constructor
    }

    /**
     * Run a Swing app with a button that runs some code.
     *
     * @param args the args
     */
    public static void main(final String... args) {
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        final JButton button = new JButton("Do It!");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                try {
                    RenderPdf.main(args);
                } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") final Exception e1) {
                    LOGGER.log(Level.SEVERE, "Main program failed", e1);
                }
            }

        });

        frame.getContentPane().add(button);
        frame.setSize(200, 100);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                frame.setVisible(true);
            }
        });
    }
}
