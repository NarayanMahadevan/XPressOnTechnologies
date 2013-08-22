package com.rgpt.pdflib;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 * RGPT Library
 * 
 * This interface specifies the methods to be implemented by Adapter to Original
 * PDF Lib like PDFNet Library from PDFTron
 * 
 * @author Narayan
 * 
 */
public interface PDFViewHandler {

	public enum PageViewMode {
		FIT_PAGE, FIT_WIDTH
	}

	public enum PagePresentationMode {
		SINGLE_PAGE, SINGLE_CONTINUOUS, FACING, FACING_CONTINUOUS
	}

	public enum SelectionMode {
		PAN_MODE, TEXT_RECT_SELECT, TEXT_STRUCT_SELECT
	}

	/**
	 * This method is invoked to register a listener for error reported by PDF
	 * Library.
	 * 
	 * @param listener
	 *            This object reports the error to the user
	 * @param data
	 *            This object is used by the caller to send data to the receiver
	 */
	public void setErrorReportListener(PDFErrorReportListener listener,
			Object data);

	/**
	 * Sets the PDF Page Listener to notify the current page being viewed
	 * 
	 * @param listener
	 *            Listener object to listen to the Notification
	 * @param data
	 *            Data passed in the notification
	 */
	public void setPDFPageListener(PDFPageListener listener, Object data);

	/**
	 * Associate the PDF Handler/Controller with the PDF Document
	 * 
	 * @param doc
	 *            A document to be displayed in the view
	 */
	public void setDoc(PDFDoc doc);

	/**
	 * Gets the Document being viewed
	 * 
	 * @return the PDFDoc being viewed
	 */
	public PDFDoc getDoc();

	/**
	 * Gets the PDF Page Count
	 * 
	 * @return total number of pages in the PDF
	 */
	public int getPageCount();

	/**
	 * Sets the current page of the PDF Document
	 * 
	 * @param pageNum
	 *            - The Page Number of the PDF Document
	 */

	public void setCurrentPage(int pageNum);

	/**
	 * Gets the Current Page being viewed by the PDF Viewer
	 */
	public int getCurrentPage();

	/**
	 * Get the device transformation matrix for the current page
	 * 
	 * @return The device transformation matrix. The device transformation
	 *         matrix maps the page coordinate system to screen (or device)
	 *         coordinate system.
	 */
	public PDFMatrix2D getDeviceTransform() throws PDFLibException;

	/**
	 * Get the device transformation matrix for a page
	 * 
	 * @param pageNum
	 *            the page number for the page used as the origin of the
	 *            destination coordinate system.
	 * @return The device transformation matrix. The device transformation
	 *         matrix maps the page coordinate system to screen (or device)
	 *         coordinate system.
	 */
	public PDFMatrix2D getDeviceTransform(int pageNum) throws PDFLibException;

	/**
	 * Set the current page to the next page in the pdf document.
	 * 
	 * @return true if successful, false otherwise
	 */
	public boolean gotoNextPage();

	/**
	 * Set the current page to the last page in the pdf document.
	 * 
	 * @return true if successful, false otherwise
	 */
	public boolean gotoLastPage();

	/**
	 * Set the current page to the first page in the pdf document.
	 * 
	 * @return true if successful, false otherwise
	 */
	public boolean gotoFirstPage();

	/**
	 * Set the current page to the previous page in the pdf document.
	 * 
	 * @return true if successful, false otherwise
	 */
	public boolean gotoPreviousPage();

	/**
	 * This method returns the view pane of the PDF to be displayed on the
	 * Template Maker
	 * 
	 * @return The Component for display
	 */
	public JPanel getViewPanel();

	/**
	 * Sets the PDFViewer content pane to the SplitPane as the RHS Component
	 * 
	 * @return The Component for display
	 */
	public JPanel setContentPane(JSplitPane splitPane);

	/**
	 * Set the core graphics library to enable or disable rasterization and
	 * rendering.
	 * 
	 * @param isEnabled
	 *            - set to true to enable rasterization and false otherwise
	 */
	public void setRasterizer(boolean isEnabled);

	/**
	 * Enables or Disables support for overprint
	 * 
	 * @param operator
	 *            - The operator values are: 0 => always disabled; 1 => always
	 *            enabled; 2 => enabled for PDF/X files only.
	 */
	public void setOverprint(int operator) throws PDFLibException;

	/**
	 * Enable or disable anti-aliasing. Anti-Aliasing is a technique used to
	 * improve the visual quality of images when displaying them on low
	 * resolution devices
	 * 
	 * @param isEnabled
	 *            - set to true to enable Anti-Aliasing and false otherwise
	 */
	public void setAntiAliasing(boolean isEnabled);

	/**
	 * Enable or disable image smoothing.
	 * 
	 * Note: image smoothing option has effect only if the source image has
	 * higher resolution that the output resolution of the image on the
	 * rasterized page.
	 * 
	 * @param isEnabled
	 *            - set to true to enable image smoothing and false otherwise
	 */
	public void setImageSmoothing(boolean isEnabled);

	/**
	 * Enables or disables the transparency grid (check board pattern) to
	 * reflect page transparency.
	 * 
	 * @param isEnabled
	 *            - set to true to enable transparency and false otherwise
	 */
	public void setPageTransparencyGrid(boolean isEnabled);

	/**
	 * Get the current zoom factor.
	 * 
	 * @return the current zoom used to display the page content.
	 */
	public double getZoom();

	/**
	 * Set the zoom factor to a new value.
	 * 
	 * @param val
	 *            New zoom value to display the page content
	 * @return true if successful, false otherwise.
	 */
	public boolean setZoom(double val);

	/**
	 * Rotates all pages in the document 90 degrees clockwise.
	 */
	public void rotateClockwise();

	/**
	 * Rotates all pages in the document 90 degrees counter clockwise.
	 */
	public void rotateCounterClockwise();

	/**
	 * Set the page viewing mode.
	 * 
	 * @param mode
	 *            Sets the Page View Mode to view the PDF Document
	 */
	public void setPageViewMode(PageViewMode mode);

	/**
	 * Set the page presentation mode.
	 * 
	 * @param mode
	 *            Sets the Page Presentation Mode to view the PDF Document
	 */
	public void setPagePresentationMode(PagePresentationMode mode);

	/**
	 * Set the page selection mode.
	 * 
	 * @param mode
	 *            Sets the Page Selection Mode to select elements from the PDF
	 *            Document
	 */
	public void setSelectionMode(SelectionMode mode);

	/**
	 * Nullifies the current view to the PDF Document
	 */
	public void close();

	/**
	 * Closes all the handle and Frees the native memory of the object.
	 */
	public void destroy();
}
