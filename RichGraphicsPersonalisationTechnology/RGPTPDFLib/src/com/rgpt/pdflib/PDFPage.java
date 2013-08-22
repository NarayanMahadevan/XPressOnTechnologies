package com.rgpt.pdflib;

import java.util.Map;

/**
 * This is an Abstract Class representing the Page of the PDF Doc.
 * 
 * @author Narayan
 * 
 */

public abstract class PDFPage {

	/**
	 * Specified the possible Page Rotations
	 */
	public enum PageRotation {
		Rot_0, Rot_90, Rot_180, Rot_270
	}

	/**
	 * Specifies the page attribute keys
	 */
	public enum PageData {
		PageNum, PDFPage, PageWt, PageHt, PageRot, PageMatrix, PageRotMatrix
	}

	/**
	 * Holds the Page Data
	 */
	public Map<PageData, Object> m_PageData;

	/**
	 * Returns the Page Data Map corresponding to the Page Attribute Key
	 * 
	 * @return the Page Data Map
	 * @throws PDFLibException
	 */
	public abstract Map<PageData, Object> getPageData() throws PDFLibException;

	/**
	 * Gets the default Page Matrix
	 * 
	 * @return the default Page Matrix
	 * @throws PDFLibException
	 */
	public abstract PDFMatrix2D getPageMatrix() throws PDFLibException;

	/**
	 * Get the rotation
	 * 
	 * @return the page rotation
	 * @throws PDFLibException
	 */
	public abstract PageRotation getPageRotation() throws PDFLibException;

	/**
	 * Get the crop box page width
	 * 
	 * @return the width for the given page region/box taking into account page
	 *         rotation attribute
	 * @throws PDFLibException
	 */
	public abstract double getPageWidth() throws PDFLibException;

	/**
	 * Get the crop box page height.
	 * 
	 * @return the width for the given page region/box taking into account page
	 *         rotation attribute
	 * @throws PDFLibException
	 */
	public abstract double getPageHeight() throws PDFLibException;

	/**
	 * Get the page index/number.
	 * 
	 * @return the Page number indication the position of this Page in
	 *         document's page sequence.
	 * @throws PDFLibException
	 */
	public abstract int getPageNum() throws PDFLibException;

}
