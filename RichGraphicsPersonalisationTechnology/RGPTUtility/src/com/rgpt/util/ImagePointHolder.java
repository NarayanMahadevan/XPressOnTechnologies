package com.rgpt.util;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class ImagePointHolder {

	public double selSize;
	public Point2D.Double pixelPt;

	public ImagePointHolder(Point2D.Double pixelPt, double selSize) {
		this.pixelPt = pixelPt;
		this.selSize = selSize;
	}

	// This creates a Image Point Holder by applying the Transformation
	public static ImagePointHolder createImagePointHolder(
			Point2D.Double pixelPt, double selSize, AffineTransform affine) {
		Point2D.Double transPt = null, selImgPt = null;
		transPt = (Point2D.Double) affine.transform(pixelPt, null);
		selSize = selSize * affine.getScaleX();
		ImagePointHolder ptHolder = new ImagePointHolder(transPt, selSize);
		return ptHolder;
	}

	public String toString() {
		return "Pixel Sel Size: " + selSize + " PixelPt: " + pixelPt.toString();
	}

}
