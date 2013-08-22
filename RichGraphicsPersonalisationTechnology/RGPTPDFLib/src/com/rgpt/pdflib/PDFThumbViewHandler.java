package com.rgpt.pdflib;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * The purpose of this interface is to show PDF Pages as thumb view for
 * Navigation purposes
 * 
 * @author Narayan
 * 
 */

public interface PDFThumbViewHandler {
	/**
	 * Sets the Document to be displayed for Navigation
	 * 
	 * @param pdfView
	 *            - The Viewer to notify when the user clicks on a PDF Page to
	 *            be viewed
	 */
	public void setPDFView(PDFViewHandler pdfView);

	/**
	 * This method returns the thumb view component to be displayed on the
	 * Template Maker
	 * 
	 * @return The Thumbview Component for display
	 */
	public JComponent getViewPanel();

	/**
	 * Sets the Thumbview content pane of pdf doc to the SplitPane as the LHS
	 * Component
	 * 
	 * @return The Thumbview Component for display
	 */
	public JPanel setContentPane(JTabbedPane tabPane);

	/**
	 * Nullifies the current view to the PDF Document
	 */
	public void close();

	/**
	 * Closes all the handle and Frees the native memory of the object.
	 */
	public void destroy();
}
