package com.rgpt.pdflib;

import java.awt.geom.Rectangle2D;

/**
 * This is the holder class that holds the rectangular bounding box of the
 * actual pdf element.
 * 
 * @author Narayan
 * 
 */

public abstract class PDFRect {
	/**
	 * Describes x and y coordinate of the lower-left point
	 */
	public double x1, y1;

	/**
	 * Describes x and y coordinate of the upper-right point
	 */
	public double x2, y2;

	/**
	 * Sets PDFRect from Rectangle2D.Double Object
	 * 
	 * @param rect
	 *            Rectangle 2D object
	 */
	public abstract void set(Rectangle2D.Double rect) throws PDFLibException;

	/**
	 * Sets PDFRect from Rectangle Object defined in the PDF Lib
	 * 
	 * @param rect
	 *            Rectangle object in the PDF Lib
	 */
	public abstract void set(Object rect) throws PDFLibException;

	/**
	 * Returns the Rectangle as Rectangle2D.Double Object
	 * 
	 * @return Rectangle2D.Double Object
	 * @throws PDFLibException
	 */
	public abstract Rectangle2D.Double getRectangle() throws PDFLibException;

	/**
	 * Retrieves the PDF Library Object in the Raw Format
	 * 
	 * @return PDF Lib Rect Object
	 */
	public abstract Object getPDFRect();
}
