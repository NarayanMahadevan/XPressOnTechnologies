package com.rgpt.pdflib;

/**
 * A PDFMatrix2D object represents a 3x3 matrix that, in turn, represents an
 * affine transformation. A PDFMatrix2D object stores only six of the nine
 * numbers in a 3x3 matrix because all 3x3 matrices that represent affine
 * transformations have the same third column (0, 0, 1). Affine transformations
 * include rotating, scaling, reflecting, shearing, and translating. In PDF Lib,
 * the PDFMatrix2D class should provide the foundation for performing affine
 * transformations on vector drawings, images, and text. A transformation matrix
 * also specifies the relationship between two coordinate spaces.
 * 
 * @author Narayan
 * 
 */

public abstract class PDFMatrix2D {

	/**
	 * This method is used to retrieve the actual PDF Libs Matrix2D
	 * 
	 * @return the actual PDF Libs Matrix
	 */
	public abstract Object getPDFMatrix2D();

	/**
	 * Multiplies this matrix with another matrix and return the result in a new
	 * matrix.
	 * 
	 * @param mtx
	 * @return the result of the multiplication
	 */
	public abstract PDFMatrix2D multiply(PDFMatrix2D mtx)
			throws PDFLibException;

}
