package com.rgpt.pdflib;

import java.awt.geom.Rectangle2D;
import java.util.Map;

public interface PDFElemSelHandler {

	/**
	 * This method is invoked when the mouse is clicked on PDF View to select
	 * Image Elements
	 * 
	 * @param x
	 *            The X Position where mouse was clicked
	 * @param y
	 *            The Y Position where mouse was clicked
	 */
	public void selectImageElements(int x, int y);

	/**
	 * This method is invoked when the mouse is Pressed on PDF View
	 * 
	 * @param x
	 *            The X Position where mouse was Pressed
	 * @param y
	 *            The Y Position where mouse was Pressed
	 */
	public void mousePressed(int x, int y);

	/**
	 * This method is invoked when the mouse is Released on PDF View
	 * 
	 * @param x
	 *            The X Position where mouse was Released
	 * @param y
	 *            The Y Position where mouse was Released
	 */
	public void mouseReleased(int x, int y);

	/**
	 * This method is invoked when the mouse is Dragged on PDF View
	 * 
	 * @param x
	 *            The X Position where mouse was Dragged
	 * @param y
	 *            The Y Position where mouse was Dragged
	 */
	public void mouseDragged(int x, int y);

	/**
	 * Updates the PDF View
	 */
	public void update();

	/**
	 * Display the Image Selected in the PDF View
	 * 
	 * @param imgBBox
	 */
	public void displaySelection(Rectangle2D.Double imgBBox)
			throws PDFLibException;

	/**
	 * Adds the Selected PDF Image Element to list Vairable Data Elements
	 * 
	 * @param selImgMap
	 * @throws PDFLibException
	 */
	public void addSelectedImage(Map<String, Object> selImgMap)
			throws PDFLibException;

	/**
	 * This method is invoked to close all the handles as the Object is going to
	 * be nullified
	 */
	public void close();
}
