// RGPT PACKAGES
package com.rgpt.imageutil;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.rgpt.util.ImagePointHolder;
import com.rgpt.util.RGPTActionListener;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTShapeUtil;
import com.rgpt.util.RGPTUtil;
import com.rgpt.util.SelectedPixelPointHolder;

// Image Holder Object life spans beyond Image Maker and Image Panel. It is used 
// in XONImageDesigner, which uses the Final Image in creating a Composite
// Image Canvas
public class ImageTransformHolder {
	// Thumb Image and Screen rect are merely ment for display in the Image
	// Maker
	// Screenrect is used to capture the Mouse Click
	public Rectangle m_ScreenRect = null;
	public BufferedImage m_ThumbPreviewImage = null;
	public String m_WorkflowProcessId = null;
	public String m_PathSelectionId = null;
	public String m_ProcessSelectionId = null;
	// All the Data are in Image Coordinate
	public Map<String, Object> m_ImageTransformData;

	public ImageTransformHolder(BufferedImage thumbImg, String wfId,
			String pathSelId, String procSelId, Map<String, Object> imgTransData) {
		m_ThumbPreviewImage = thumbImg;
		m_WorkflowProcessId = wfId;
		m_PathSelectionId = pathSelId;
		m_ProcessSelectionId = procSelId;
		m_ImageTransformData = imgTransData;
	}

	public static ImageTransformHolder createCutoutImageTransform(
			BufferedImage thumbImg, String wfId, String pathSelId,
			Vector<Point2D.Double> gPathPts, AffineTransform affine) {
		Vector<Point2D.Double> gPathPoints = null, imgPathPoints = null;
		Map<String, Object> imgTranData = new HashMap<String, Object>();
		gPathPoints = (Vector<Point2D.Double>) gPathPts.clone();
		imgPathPoints = RGPTUtil.getTransformedPt(affine, gPathPoints);
		imgTranData.put("GPathPoints", imgPathPoints);
		return new ImageTransformHolder(thumbImg, wfId, pathSelId, null,
				imgTranData);
	}

	public static ImageTransformHolder createModifyImageTransform(
			BufferedImage thumbImg, String wfId, String procSelId,
			Vector<SelectedPixelPointHolder> selTransPt,
			Vector<ImagePointHolder> newTransPt,
			Vector<ImagePointHolder> resetTransPt) {
		// Creating the Clone of the Original Object
		Vector<SelectedPixelPointHolder> selTransPoint = null;
		Vector<ImagePointHolder> newTransPoint = null, resetTransPoint = null;
		selTransPoint = (Vector<SelectedPixelPointHolder>) selTransPt.clone();
		newTransPoint = (Vector<ImagePointHolder>) newTransPt.clone();
		resetTransPoint = (Vector<ImagePointHolder>) resetTransPt.clone();

		Map<String, Object> imgTranData = new HashMap<String, Object>();
		imgTranData.put("SelectedTransPoint", selTransPoint);
		imgTranData.put("NewTransPoint", newTransPoint);
		imgTranData.put("ResetTransPoint", resetTransPoint);
		return new ImageTransformHolder(thumbImg, wfId, "", procSelId,
				imgTranData);
	}

	public static BufferedImage getTransformedImage(ImageHolder imgHolder,
			boolean useCompressedImage) throws Exception {
		String imgAction = "";
		RGPTActionListener.ImageActions action = null;
		BufferedImage transImg = imgHolder.getOriginalImage();
		Color bgColor = null;
		try {
			if (useCompressedImage) {
				transImg = imgHolder.getCompressedImage(transImg, true);
				bgColor = imgHolder.m_BackgroundColor;
			}
		} catch (Exception ex) {
			RGPTLogger.logToFile("Exception at compressed image", ex);
		}
		ImageTransformHolder imtTransHldr = null;
		Vector<Point2D.Double> gPathPoints = null;
		Vector<SelectedPixelPointHolder> selTransPt = null;
		Map<String, Object> imgTransData = null;
		Vector<ImagePointHolder> newTransPt = null, resetTransPt = null;
		for (int i = 0; i < imgHolder.m_ImageTransformHolder.size(); i++) {
			imtTransHldr = imgHolder.m_ImageTransformHolder.elementAt(i);
			imgTransData = imtTransHldr.m_ImageTransformData;
			imgAction = imtTransHldr.m_ProcessSelectionId;
			if (imgAction == null)
				imgAction = imtTransHldr.m_WorkflowProcessId;
			action = RGPTActionListener.ImageActions.valueOf(imgAction);
			switch (action) {
			case CUTOUT_IMAGE:
				String pathSelId = imtTransHldr.m_PathSelectionId;
				gPathPoints = (Vector<Point2D.Double>) imgTransData
						.get("GPathPoints");
				transImg = createCutoutImage(transImg, pathSelId, gPathPoints);
				break;
			case MAKE_PIXEL_TRANSPERENT_MENU:
				selTransPt = (Vector<SelectedPixelPointHolder>) imgTransData
						.get("SelectedTransPoint");
				newTransPt = (Vector<ImagePointHolder>) imgTransData
						.get("NewTransPoint");
				resetTransPt = (Vector<ImagePointHolder>) imgTransData
						.get("ResetTransPoint");
				transImg = showTransformedImage(transImg, bgColor, null,
						selTransPt, newTransPt, resetTransPt, false);
				break;
			}
		}
		return transImg;
	}

	public static BufferedImage createCutoutImage(BufferedImage origImg,
			String pathSelId, Vector<Point2D.Double> imgPathPoints) {
		// The functions in Shape Util draw the shape using the Graphics and
		// Path Points
		// affine is always identity matrix.
		GeneralPath gPath = null;
		// RGPTLogger.logToFile("Orig Img: "+origImg+"  pathSelId: "+ pathSelId+
		// "\nimgPathPoints: "+imgPathPoints);
		if (pathSelId == RGPTActionListener.ImageActions.IMAGE_LINE_PATH_MENU
				.toString())
			gPath = RGPTShapeUtil.drawLine(null, imgPathPoints, null);
		else if (pathSelId == RGPTActionListener.ImageActions.IMAGE_QUAD_PATH_MENU
				.toString())
			gPath = RGPTShapeUtil.drawQuadCurve(null, imgPathPoints, null);
		else if (pathSelId == RGPTActionListener.ImageActions.IMAGE_CUBIC_PATH_MENU
				.toString())
			gPath = RGPTShapeUtil.drawBezierCurve(null, imgPathPoints, null);
		BufferedImage cutoutImg = ImageUtils.cutoutImage(origImg, gPath);
		return cutoutImg;
	}

	public static BufferedImage showTransformedImage(BufferedImage origImg,
			Color bgColor, AffineTransform affine,
			Vector<SelectedPixelPointHolder> selTransPt,
			Vector<ImagePointHolder> newTransPt,
			Vector<ImagePointHolder> resetTransPt, boolean createCopy) {
		Vector<Integer> transPixel = new Vector<Integer>();
		resetTransformationPixels(origImg, affine, selTransPt, transPixel);
		resetTransformationPixels(origImg, affine, transPixel, true, newTransPt);
		resetTransformationPixels(origImg, affine, transPixel, false,
				resetTransPt);
		BufferedImage transImg = null;
		transImg = ImageUtils.tranformImage(origImg, transPixel, bgColor,
				createCopy);
		return transImg;
	}

	// AffineTransform is provided to transform from Image to Windows Decice Pt
	public static void resetTransformationPixels(BufferedImage origImg,
			AffineTransform affine,
			Vector<SelectedPixelPointHolder> selTransPt,
			Vector<Integer> transPixel) {
		Point2D.Double selPixelPt = null;
		SelectedPixelPointHolder selImgPtHldr = null, selPtHldr = null;
		for (int i = 0; i < selTransPt.size(); i++) {
			selImgPtHldr = selTransPt.elementAt(i);
			for (int j = 0; j < selImgPtHldr.selectedPts.size(); j++) {
				selPixelPt = selImgPtHldr.selectedPts.elementAt(j);
				if (affine != null) {
					selPtHldr = SelectedPixelPointHolder.create(selPixelPt,
							selImgPtHldr.gpathPts, affine, true,
							selImgPtHldr.colorRange);
				} else
					selPtHldr = new SelectedPixelPointHolder(selPixelPt,
							selImgPtHldr.gpathPts, selImgPtHldr.colorRange);
				selPixelPt = selPtHldr.selectedPts.firstElement();
				GeneralPath gpath = null;
				if (selPtHldr.gpathPts != null)
					gpath = RGPTShapeUtil.drawLine(null, selPtHldr.gpathPts,
							null);
				setTransformationPixels(origImg, selPixelPt, transPixel, gpath,
						selPtHldr.colorRange);
			}
		}
	}

	public static void setTransformationPixels(BufferedImage origImg,
			Point2D.Double selPixelPt, Vector<Integer> transPixel,
			GeneralPath gpath, int colorRange) {
		int colorRangePerc = colorRange;
		Color selColor = null;
		if (selPixelPt != null)
			selColor = new Color(origImg.getRGB((int) selPixelPt.x,
					(int) selPixelPt.y));
		ImageUtils.setTranformImagePixels(origImg, selColor, colorRangePerc,
				transPixel, gpath);
	}

	// AffineTransform is provided to transform from Image to Windows Decice Pt
	public static void resetTransformationPixels(BufferedImage origImg,
			AffineTransform affine, Vector<Integer> transPixel,
			boolean setNewPixels, Vector<ImagePointHolder> imgPixelPtList) {
		Point2D.Double selPixelPt = null;
		double selSize = 0.0;
		ImagePointHolder pixelPtHldr = null, transPixelPtHldr = null;
		for (int i = 0; i < imgPixelPtList.size(); i++) {
			pixelPtHldr = imgPixelPtList.elementAt(i);
			selPixelPt = pixelPtHldr.pixelPt;
			selSize = pixelPtHldr.selSize;
			if (affine != null) {
				pixelPtHldr = ImagePointHolder.createImagePointHolder(
						selPixelPt, selSize, affine);
				selPixelPt = pixelPtHldr.pixelPt;
				selSize = pixelPtHldr.selSize;
				// RGPTLogger.logToFile("Screen Pixel PtHldr : "+pixelPtHldr);
			}
			ImageUtils
					.resetTranformPixel(origImg, (int) selPixelPt.x,
							(int) selPixelPt.y, transPixel, setNewPixels,
							(int) selSize);
		}
	}

}
