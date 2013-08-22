// XODImageInfo maintains all the data of XON Image Designer and serializes its 
// data in XOD (Xpress On Design) File

// RGPT PACKAGES
package com.rgpt.templateutil;

// This files are added to support serialization
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Map;
import java.util.Vector;

import com.rgpt.imageutil.ImageHolder;
import com.rgpt.util.RGPTParams;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTUtil;
import com.rgpt.util.ScalingFactor;

public class XODImageInfo implements Serializable {
	// This defines the current XOD Image Info VERSION
	// public static final long serialVersionUID = 5293013250085507480L;

	// Background Image on which all the Images and Texts are Placed
	public transient ImageHolder m_CanvasImageHolder = null;
	public VDPImageFieldInfo m_CanvasImageField = null;
	public transient BufferedImage m_CanvasImage = null;

	// Original Canvas Size
	public int m_OrigImageWidth;
	public int m_OrigImageHeight;

	// Shows the Scale Factor of the Canvas Image
	public double m_Scale;

	// Scaled Size
	public double m_ScaledImageWt;
	public double m_ScaledImageHt;

	// This are Transient because all the coordinates points on serialization
	// are kept in Image Points
	// During the Working Stage they are kept as Screen Points Only
	public transient AffineTransform m_WindowDeviceCTM = null;
	public transient AffineTransform m_ScreenToImageCTM = null;
	public transient Rectangle m_ScreenImageRect = null;

	// This holds all the Variable Data Field i.e. Text, Image and Shape Defined
	// for this Image
	public Vector<VDPFieldInfo> m_VDPFieldInfoList;

	// All the Temperary Image Data pertaining to this session is stored in this
	// Directory. Its Deleted
	// on cleanup
	public transient String m_TempStorageDir = null;

	public XODImageInfo(ImageHolder canvasImgHldr) {
		m_VDPFieldInfoList = new Vector<VDPFieldInfo>();
		setCanvasImage(canvasImgHldr);
	}

	// This method is used to set new Image as Canvas Image. In this case all
	// the
	public void setCanvasImage(ImageHolder canvasImgHldr) {
		m_CanvasImageHolder = canvasImgHldr;
		m_TempStorageDir = m_CanvasImageHolder.m_SrcDir;
		m_CanvasImage = m_CanvasImageHolder.m_RegularImage;
		m_OrigImageWidth = m_CanvasImageHolder.m_OrigImageWidth;
		m_OrigImageHeight = m_CanvasImageHolder.m_OrigImageHeight;
		int fldCntr = m_VDPFieldInfoList.size() + 1;
		String fldName = "VdpImg" + fldCntr;
		m_CanvasImageField = new VDPImageFieldInfo(fldName, m_OrigImageWidth,
				m_OrigImageHeight, false);
		m_CanvasImageField.m_ImageHolder = canvasImgHldr;
		m_CanvasImageField.m_IsBackgroundImage = true;
		m_CanvasImageField.m_ScreenPathPoints = new Vector<Point2D.Double>();
		m_VDPFieldInfoList.addElement(m_CanvasImageField);
		BufferedImage srcImg = m_CanvasImage;
		String filAction = "ImageOutlineFilter";
		// int[][][] pixel_rgb = ImageUtils.BufferedImagetoPixel3DRGB(srcImg);
		// srcImg = ImageUtils.Pixel3DRGBtoBufferedImage(pixel_rgb);
		// BufferedImage finImg = ImageFilterController.
		// applyFilter(srcImg, filAction, "Inc", 0.5);
		// BufferedImage finImg = m_CanvasImageField.applyImageFilter(
		// RGPTActionListener.ImageFilterActions.valueOf(filAction));
		// String fileName = "test_output/OrigImg.png";
		// BufferedImage thumbImg = (BufferedImage)
		// m_CanvasImageHolder.m_ThumbviewImage;
		// ImageUtils.SaveImageToFile(thumbImg, fileName, "PNG");
		// fileName = "test_output/finalImg.png";
		// ImageUtils.SaveImageToFile(finImg, fileName, "PNG");
		// ImageUtils.displayImage(finImg, filAction);
	}

	public void deriveDeviceCTM(ScalingFactor scaleFactor, Dimension panelSize) {
		deriveDeviceCTM(scaleFactor, panelSize,
				RGPTParams.getIntVal("PanelMargin"));
	}

	public void deriveDeviceCTM(ScalingFactor scaleFactor, Dimension panelSize,
			int specStartYPt) {
		// Image Ht and Width after Scaling
		double newImgHt = 0.0, newImgWt = 0.0;

		newImgHt = (double) m_CanvasImageHolder.m_RegularImage.getHeight();
		newImgWt = (double) m_CanvasImageHolder.m_RegularImage.getWidth();
		this.deriveDeviceCTM(scaleFactor, panelSize, specStartYPt, newImgWt,
				newImgHt);
	}

	public void deriveDeviceCTM(ScalingFactor scaleFactor, Dimension panelSize,
			int specStartYPt, double newImgWt, double newImgHt) {
		Map<String, Object> derivedData = null;
		derivedData = m_CanvasImageHolder.deriveDeviceCTM(scaleFactor,
				panelSize, specStartYPt, newImgWt, newImgHt, true);
		// derivedData = ImageUtils.deriveDeviceCTM(scaleFactor, panelSize,
		// specStartYPt,
		// newImgWt, newImgHt);
		m_Scale = ((Double) derivedData.get("Scale")).doubleValue();
		m_ScaledImageWt = ((Double) derivedData.get("ScaledImageWt"))
				.doubleValue();
		m_ScaledImageHt = ((Double) derivedData.get("ScaledImageHt"))
				.doubleValue();
		m_WindowDeviceCTM = (AffineTransform) derivedData
				.get("WindowDeviceCTM");
		m_ScreenToImageCTM = (AffineTransform) derivedData
				.get("ScreenToImageCTM");
		m_ScreenImageRect = (Rectangle) derivedData.get("ScreenImageRect");
		m_CanvasImageField.m_ScreenBounds = m_ScreenImageRect;
		m_CanvasImage = m_CanvasImageHolder.m_ClippedImage;
		int x = m_ScreenImageRect.x, y = m_ScreenImageRect.y;
		int wt = m_ScreenImageRect.width, ht = m_ScreenImageRect.height;
		setPathPoints(m_CanvasImageField.m_ScreenPathPoints, x, y, wt, ht);

		// RGPTLogger.logToFile("Windows CTM: "+m_WindowDeviceCTM+
		// "\nImage CTM: "+m_ScreenToImageCTM);
		// RGPTLogger.logToFile("\n\nScale: "+m_Scale+
		// " Windows CTM Scale: "+m_WindowDeviceCTM.getScaleX()+
		// " Image CTM ScaleX: "+m_ScreenToImageCTM.getScaleX()+
		// " Image CTM ScaleY: "+m_ScreenToImageCTM.getScaleY());
	}

	public VDPImageFieldInfo addImageField(ImageHolder imgHldr)
			throws Exception {
		int wt = RGPTParams.getIntVal("ThumbviewImageWidth");
		int ht = RGPTParams.getIntVal("ThumbviewImageHeight");
		imgHldr.setVDPImageData(wt, ht, m_TempStorageDir);
		BufferedImage thumbImg = (BufferedImage) imgHldr.m_ThumbviewImage;
		wt = thumbImg.getWidth();
		ht = thumbImg.getHeight();
		int fldCntr = m_VDPFieldInfoList.size() + 1;
		String fldName = "VdpImg" + fldCntr;
		VDPImageFieldInfo vdpImgFld = new VDPImageFieldInfo(fldName, wt, ht,
				false);
		vdpImgFld.m_ImageHolder = imgHldr;
		// Set the Initial Screen Rect to position this Image within the Canvas
		int x = RGPTParams.getIntVal("IDAppsImageStartPt"), y = x;
		vdpImgFld.m_ScreenPathPoints = new Vector<Point2D.Double>();
		setPathPoints(vdpImgFld.m_ScreenPathPoints, x, y, wt, ht);
		vdpImgFld.setGPathPts(m_ScreenToImageCTM, false);
		// BufferedImage srcImg = imgHldr.m_ScaledImage;
		// String filAction = "RemoveBlurFilter";
		// int[][][] pixel_rgb = ImageUtils.BufferedImagetoPixel3DRGB(srcImg);
		// srcImg = ImageUtils.Pixel3DRGBtoBufferedImage(pixel_rgb);
		// ImageFilterController.applyFilter(srcImg, "SharpenFilter", "On", -1);
		// BufferedImage finImg = ImageFilterController.
		// applyFilter(srcImg, filAction, "Inc", -1);
		// ImageUtils.displayImage(finImg, filAction);

		// public transient Rectangle m_ScreenRect = new Rectangle(startx,
		// starty, ;
		// This are in Image Points of where it is located w.r.t to the Canvas
		// Image. This
		// is converted into RGPTRectangle while serializing
		// public transient Rectangle2D.Double m_CanvasImageRect = null;
		// Call the Image Holder to derive Image and Screen CTM and Scale the
		// Image to fit within the Canvas BBox
		m_VDPFieldInfoList.addElement(vdpImgFld);
		return vdpImgFld;
	}

	public void setPathPoints(Vector<Point2D.Double> pathPts, int x, int y,
			int wt, int ht) {
		pathPts.clear();
		pathPts.addElement(new Point2D.Double(x, y));
		pathPts.addElement(new Point2D.Double(x + wt / 2, y));
		pathPts.addElement(new Point2D.Double(x + wt, y));
		pathPts.addElement(new Point2D.Double(x + wt, y + ht / 2));
		pathPts.addElement(new Point2D.Double(x + wt, y + ht));
		pathPts.addElement(new Point2D.Double(x + wt / 2, y + ht));
		pathPts.addElement(new Point2D.Double(x, y + ht));
		pathPts.addElement(new Point2D.Double(x, y + ht / 2));
	}

	public boolean removeField(VDPFieldInfo selVDPFldInfo) {
		return m_VDPFieldInfoList.removeElement(selVDPFldInfo);
	}

	public VDPFieldInfo setFieldSelection(VDPFieldInfo currSelVDPFldInfo,
			boolean isPreviewPanel, int x, int y) {
		VDPFieldInfo vdpFld, selVDPFld = null;
		for (int i = 0; i < m_VDPFieldInfoList.size(); i++) {
			vdpFld = (VDPFieldInfo) m_VDPFieldInfoList.elementAt(i);
			if (vdpFld instanceof VDPImageFieldInfo && !isPreviewPanel) {
				VDPImageFieldInfo vdpImgFld = (VDPImageFieldInfo) vdpFld;
				if (vdpImgFld.m_IsBackgroundImage)
					continue;
			}
			if (isPreviewPanel) {
				if (vdpFld.m_PreviewPos == null
						|| !vdpFld.m_PreviewPos.contains(x, y))
					continue;
			} else if (vdpFld.m_ScreenBounds == null
					|| !vdpFld.m_ScreenBounds.contains(x, y))
				continue;
			if (currSelVDPFldInfo != null && currSelVDPFldInfo.equals(vdpFld))
				return currSelVDPFldInfo;
			if (selVDPFld == null)
				selVDPFld = vdpFld;
		}
		if (selVDPFld != null) {
			selVDPFld.resetFields();
			selVDPFld.setFieldSelection();
		}
		return selVDPFld;
	}

	public void cleanup() {
		RGPTLogger
				.logToFile("Cleaning up the Temp File Dir and the Data Structure: "
						+ m_TempStorageDir);
		try {
			RGPTUtil.deleteDir(m_TempStorageDir, null);
		} catch (Exception ex) {
			RGPTLogger.logToFile("Unable to delete: " + m_TempStorageDir, ex);
		}
		m_VDPFieldInfoList = null;
		m_CanvasImageHolder = null;
		m_CanvasImage = null;
	}

}