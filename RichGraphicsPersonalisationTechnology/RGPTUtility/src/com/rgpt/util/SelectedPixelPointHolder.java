package com.rgpt.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.Vector;

public class SelectedPixelPointHolder {
	public int colorRange;
	public Vector<Point2D.Double> selectedPts;
	public Vector<Point2D.Double> gpathPts;

	public SelectedPixelPointHolder(Point2D.Double selectedPt, int colorRange) {
		this(selectedPt, null, colorRange);
	}

	public SelectedPixelPointHolder(Vector<Point2D.Double> gpathPts) {
		this(null, gpathPts, -1);
	}

	public SelectedPixelPointHolder(Point2D.Double selectedPt,
			Vector<Point2D.Double> gpathPts, int colorRange) {
		selectedPts = new Vector<Point2D.Double>();
		this.colorRange = colorRange;
		if (selectedPt != null)
			selectedPts.addElement(selectedPt);
		if (gpathPts != null)
			this.gpathPts = gpathPts;
	}

	public void drawGPath(Graphics g, AffineTransform winAffine) {
		// RGPTLogger.logToFile(this.toString());
		if (gpathPts == null)
			return;
		Vector<Point2D.Double> scrPathPts = null;
		scrPathPts = RGPTUtil.getTransformedPt(winAffine, gpathPts);
		// RGPTLogger.logToFile("Drawing GPath: "+scrPathPts);
		// GeneralPath gpath = RGPTShapeUtil.drawLine(null, scrPathPts, g);
		GeneralPath gpath = RGPTShapeUtil.drawLine(g, null, null, Color.BLACK,
				scrPathPts);
	}

	// Returns the Screen Path Points
	public static Vector<Point2D.Double> addSelectedPoint(
			Vector<SelectedPixelPointHolder> selPts, Point2D.Double newImgPt,
			Vector<Point2D.Double> gPathPoints, AffineTransform winAffine,
			AffineTransform imgAffine, int colorRange) {
		Point2D.Double newScrPt = (Point2D.Double) winAffine.transform(
				newImgPt, null);
		Vector<Point2D.Double> scrPathPts = null, imgPathPts = null;
		SelectedPixelPointHolder selImgPtHldr = null, selScrPtHldr = null, newPtHldr = null;
		for (int i = 0; i < selPts.size(); i++) {
			selImgPtHldr = selPts.elementAt(i);
			if (selImgPtHldr.gpathPts == null)
				continue;
			scrPathPts = RGPTUtil.getTransformedPt(winAffine,
					selImgPtHldr.gpathPts);
			GeneralPath gpath = RGPTShapeUtil.drawLine(null, scrPathPts, null);
			if (gpath.contains(newScrPt)) {
				selImgPtHldr.selectedPts.addElement(newImgPt);
				return scrPathPts;
			}
		}
		// Checking if the Point Selected is with in the Path
		GeneralPath gpath = null;
		if (gPathPoints != null) {
			gpath = RGPTShapeUtil.drawLine(null, gPathPoints, null);
			// RGPTLogger.logToFile("New Sc Pt: "+newScrPt+" gPathBounds: "+gpath.getBounds());
			if (gpath.contains(newScrPt)) {
				// RGPTLogger.logToFile("New Scr Pt is contained in GPath.");
				newPtHldr = create(newImgPt, gPathPoints, imgAffine, false,
						colorRange);
			} else
				newPtHldr = new SelectedPixelPointHolder(newImgPt, colorRange);
		} else
			newPtHldr = new SelectedPixelPointHolder(newImgPt, colorRange);
		// RGPTLogger.logToFile("Adding new Point Holder: "+newPtHldr.toString());
		selPts.addElement(newPtHldr);
		return gPathPoints;
	}

	public static Vector<Point2D.Double> getGPathPoints(
			Vector<SelectedPixelPointHolder> selPts, Point2D.Double newScrPt,
			AffineTransform winAffine) {
		Vector<Point2D.Double> scrPathPts = null, imgPathPts = null;
		SelectedPixelPointHolder selImgPtHldr = null, selScrPtHldr = null, newPtHldr = null;
		for (int i = 0; i < selPts.size(); i++) {
			selImgPtHldr = selPts.elementAt(i);
			if (selImgPtHldr.gpathPts == null)
				continue;
			scrPathPts = RGPTUtil.getTransformedPt(winAffine,
					selImgPtHldr.gpathPts);
			GeneralPath gpath = RGPTShapeUtil.drawLine(null, scrPathPts, null);
			if (gpath.contains(newScrPt)) {
				return scrPathPts;
			}
		}
		return null;
	}

	public static SelectedPixelPointHolder create(Point2D.Double selectedPt,
			Vector<Point2D.Double> gPathPoints, AffineTransform affine,
			boolean transPt, int colorRange) {
		Vector<Point2D.Double> transPathPoints = null, gpathPts = null;
		if (gPathPoints != null) {
			gpathPts = (Vector<Point2D.Double>) gPathPoints.clone();
			transPathPoints = RGPTUtil.getTransformedPt(affine, gpathPts);
		}
		if (selectedPt != null && transPt)
			selectedPt = (Point2D.Double) affine.transform(selectedPt, null);
		return new SelectedPixelPointHolder(selectedPt, transPathPoints,
				colorRange);
	}

	public String toString() {
		return "GpathPts: " + gpathPts + " selectedPts: " + selectedPts;
	}

}
