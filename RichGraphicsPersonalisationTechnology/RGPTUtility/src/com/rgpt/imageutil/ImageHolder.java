// RGPT PACKAGES
package com.rgpt.imageutil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.rgpt.util.RGPTParams;
import com.rgpt.util.RGPTActionListener;
import com.rgpt.util.RGPTActionListener.ImageFilterActions;
import com.rgpt.util.RGPTFileFilter;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTRectangle;
import com.rgpt.util.RGPTUtil;
import com.rgpt.util.ScalingFactor;

public class ImageHolder implements Serializable {
	public final transient static int SCALED_IMAGE_MODE = 0;
	public final transient static int FULL_IMAGE_MODE = 1;
	public final transient static int CROP_IMAGE_MODE = 1;

	// This are indicaters to view Image along Width or Height.
	public final transient static int SCALED_ALONG_HEIGHT = 0;
	public final transient static int SCALED_ALONG_WIDTH = 1;

	// Digital Asset Id in the Database for the Image File
	public int m_AssetId;

	// This variables specifies the Theme this Image is part off
	public int m_ThemeId = -1;

	// This holds the Bytes for Thumbview Image
	public byte[] m_ImgStr;

	// This holds the Directory where the Enlarged and Thumbview Image are
	// stored
	public String m_SrcDir;

	// This is the File name of Thumbview Image File
	public String m_FileName;

	// Caption for the Image
	public transient String m_ImgCaption = "";

	// This variable is to true if the Images are cached. In this scenario there
	// is no need to read Image from Image Stream
	public boolean m_UseCachedImages;
	public transient Image m_ThumbviewImage;
	public transient BufferedImage m_EnlargedImage;
	public transient BufferedImage m_ScaledImage;
	public transient BufferedImage m_ClippedImage; // Clipped Image is treated
													// as Selected Image

	// This variables are used for Clip Image and Background Image or Image
	// Personalization
	public transient BufferedImage m_LowResImage;

	// Regular Image is the Original Image Object. In case of Image Maker this
	// Image is the Compressed Image and goes throgh series of transformation
	public transient BufferedImage m_RegularImage;

	// Original Image has no Transformation. This 2 Copies of the Image are
	// maintained
	// in the memory
	public transient BufferedImage m_OrigRegularImage;

	// Transformation Vector is mainly used by Image Maker. This represents the
	// operation performed on the Original Image like cutout image, etc
	public transient Vector<ImageTransformHolder> m_ImageTransformHolder;

	// This indicates the Images are from Server, Desktop, etc.
	public int m_ImageFromMode;

	// This takes the value THUMBVIEW_IMAGE or REGULAR_IMAGE defined
	// in CustomerAssetRequest
	public int m_ImageSizeType;

	// Original Image Size
	public int m_OrigImageWidth;
	public int m_OrigImageHeight;

	// Scale WRT the Downsampled Image
	public double m_Scale;
	public transient double m_ScaledImageWt;
	public transient double m_ScaledImageHt;

	// Clipped Flag and Clip Rectangle Object
	public boolean m_IsClipped;
	public Rectangle m_ClipRectangle;

	// Actual Image BBox derived from the Variable Data Image in the Printable
	// PDF Document.
	public RGPTRectangle m_ImageBBox;
	public transient RGPTRectangle m_DeviceBBox;
	public transient Rectangle m_DisplayRect;
	public transient Rectangle m_DisplayClipRect;

	// This variable indicates if the Image is Scaled along the Width or the
	// Height.
	// This means if Scaled along the Height, Adjustment to see the other part
	// of
	// Image can be done along the Width.
	public transient int m_ScalingMode = -1;

	// Image Name mainly used by XON Image Designer in Communication with
	// ImageMaker
	public String m_ImageName = "";

	public ImageHolder(ImageHolder imgHldr) {
		m_AssetId = imgHldr.m_AssetId;
		if (imgHldr.m_ImgStr != null && imgHldr.m_ImgStr.length > 0) {
			int imgStrLen = imgHldr.m_ImgStr.length;
			m_ImgStr = new byte[imgStrLen];
			System.arraycopy(imgHldr.m_ImgStr, 0, m_ImgStr, 0, imgStrLen);
		}
		m_SrcDir = imgHldr.m_SrcDir;
		m_FileName = imgHldr.m_FileName;
		m_ImgCaption = imgHldr.m_ImgCaption;

		m_UseCachedImages = imgHldr.m_UseCachedImages;
		if (imgHldr.m_ThumbviewImage != null)
			m_ThumbviewImage = ImageUtils.ScaleToSize(imgHldr.m_ThumbviewImage,
					-1, -1);
		if (imgHldr.m_EnlargedImage != null)
			m_EnlargedImage = ImageUtils.ScaleToSize(imgHldr.m_EnlargedImage,
					-1, -1);
		if (imgHldr.m_ScaledImage != null)
			m_ScaledImage = ImageUtils.ScaleToSize(imgHldr.m_ScaledImage, -1,
					-1);

		m_ImageFromMode = imgHldr.m_ImageFromMode;

		m_ImageSizeType = imgHldr.m_ImageSizeType;

		m_OrigImageWidth = imgHldr.m_OrigImageWidth;
		m_OrigImageHeight = imgHldr.m_OrigImageHeight;

		m_Scale = imgHldr.m_Scale;

		m_IsClipped = imgHldr.m_IsClipped;
		if (imgHldr.m_ClipRectangle != null)
			m_ClipRectangle = new Rectangle(imgHldr.m_ClipRectangle);

		if (imgHldr.m_ImageBBox != null)
			m_ImageBBox = RGPTRectangle.getReactangle(imgHldr.m_ImageBBox
					.getRectangle2D());
		// if (imgHldr.m_DeviceBBox != null)
		// m_DeviceBBox = RGPTRectangle.getReactangle(imgHldr.m_DeviceBBox.
		// getRectangle2D());
		m_DeviceBBox = null;
		if (imgHldr.m_DisplayRect != null)
			m_DisplayRect = new Rectangle(imgHldr.m_DisplayRect);

		m_ScalingMode = imgHldr.m_ScalingMode;
	}

	// This method is used if the Images are cached in memory.
	public ImageHolder(int imgId, Image thumbImg, BufferedImage bigImg) {
		m_AssetId = imgId;
		m_ThumbviewImage = thumbImg;
		m_EnlargedImage = bigImg;
		m_ImgCaption = "Drag And Drop Image";
		m_UseCachedImages = false;
	}

	public ImageHolder(String srcDir, String fileName, RGPTRectangle imgBBox) {
		m_AssetId = -1;
		m_SrcDir = srcDir;
		m_FileName = fileName;
		m_ImageBBox = imgBBox;
		m_IsClipped = false;
		m_ImgCaption = "Drag And Drop Image";
		m_UseCachedImages = false;
	}

	public ImageHolder(int imgId, String srcDir, String fileName,
			String caption, int imgSizeTyp, byte[] imgStr) {
		m_UseCachedImages = false;
		m_AssetId = imgId;
		m_SrcDir = srcDir;
		m_ImgCaption = caption;
		m_FileName = fileName;
		m_ImgStr = imgStr;
		m_ImageSizeType = imgSizeTyp;
		m_ImgCaption = "Drag And Drop Image";
	}

	public ImageHolder(String srcDir, String fileName, byte[] imgStr) {
		m_UseCachedImages = false;
		m_AssetId = -1;
		m_SrcDir = srcDir;
		m_ImgCaption = "Drag And Drop Image";
		m_FileName = fileName;
		m_ImgStr = imgStr;
	}

	public ImageHolder(int imgId, int imgSizeTyp, byte[] imgStr) {
		m_UseCachedImages = false;
		m_AssetId = imgId;
		m_SrcDir = "";
		m_ImgCaption = "";
		m_FileName = "";
		m_ImgStr = imgStr;
		m_ImageSizeType = imgSizeTyp;
	}

	public ImageHolder(byte[] imgStr) {
		m_UseCachedImages = false;
		m_AssetId = -1;
		m_SrcDir = "";
		m_ImgCaption = "";
		m_FileName = "";
		m_ImgStr = imgStr;
	}

	public BufferedImage getImage(boolean inlowRes, Rectangle bounds)
			throws Exception {
		if (inlowRes)
			return getLowResImage(bounds);
		else
			return getRegularImage();
	}

	public static int LOW_RES_DPI = 92;

	public BufferedImage getLowResImage(Rectangle bounds) throws Exception {
		if (m_LowResImage != null)
			return m_LowResImage;
		getRegularImage();

		double w = bounds.width / 72;
		double h = bounds.height / 72;
		BufferedImage buffImg = ImageUtils.CreateCopy(m_RegularImage);
		int dpi = ImageUtils.FindResolution(buffImg, w, h);
		System.out
				.println("\n\n****\nDPI of the Image: " + dpi + "\n\n*****\n");
		if (dpi > LOW_RES_DPI)
			m_LowResImage = ImageUtils.DownSampleImage(buffImg, w, h, 92);
		else
			m_LowResImage = m_RegularImage;
		return m_LowResImage;
	}

	public BufferedImage getRegularImage() throws Exception {
		if (m_RegularImage != null)
			return m_RegularImage;
		m_RegularImage = getOriginalImage();
		return m_RegularImage;
	}

	public BufferedImage getOriginalImage() throws Exception {
		if (m_ImgStr != null && m_ImgStr.length > 0) {
			RGPTLogger.logToFile("Original Image Size: " + m_ImgStr.length);
			return ImageUtils.getBufferedImage(m_ImgStr);
		}
		if ((m_SrcDir != null && m_SrcDir.length() > 0)
				&& (m_FileName != null && m_FileName.length() > 0)) {
			String filePath = m_SrcDir + "/" + m_FileName;
			byte[] imgStr = RGPTUtil.getBytes(filePath);
			return ImageUtils.getBufferedImage(imgStr);
		}
		throw new RuntimeException("No Image Stream to construct Image");
	}

	// This method is predominantly used by XONImageMaker
	public boolean m_ImageHasAlpha = false;
	public Color m_BackgroundColor = null;
	public transient Color m_PanelColor = null;

	public void setVDPImageData(int thumbImgWt, int thumbImgHt, String imgSrcDir)
			throws Exception {
		BufferedImage newImage = null;
		getRegularImage();
		m_OrigImageWidth = m_RegularImage.getWidth();
		m_OrigImageHeight = m_RegularImage.getHeight();
		RGPTLogger.logToFile("Original Image Wt: " + m_OrigImageWidth + " Ht: "
				+ m_OrigImageHeight);
		m_ImageHasAlpha = ImageUtils.hasAlpha(m_RegularImage);
		// Check for Transperency and correspondingly choose Background to
		// create Image
		if (!m_ImageHasAlpha) {
			float compQuality = (float) RGPTParams
					.getIntVal("IMAGE_COMPRESSION") / (float) 10;
			m_RegularImage = ImageUtils.compressImage(m_RegularImage,
					compQuality);
		}
		m_ThumbviewImage = ImageUtils.scaleImage(m_RegularImage, -1,
				thumbImgHt, true, false);
		// Initially setting the Thumb Image to Regular Image.
		m_ScaledImage = (BufferedImage) m_ThumbviewImage;
		m_SrcDir = RGPTUtil.saveImageData(m_FileName, m_ImgStr, imgSrcDir);
		// Image Stream is made null. It will be reconstructed when
		m_ImgStr = null;
	}

	public void setImageData(int thumbImgWt, int thumbImgHt) throws Exception {
		setImageData(thumbImgWt, thumbImgHt, false);
	}

	public void setImageData(int thumbImgWt, int thumbImgHt,
			boolean createTempDir) throws Exception {
		BufferedImage newImage = null;
		getRegularImage();
		m_OrigImageWidth = m_RegularImage.getWidth();
		m_OrigImageHeight = m_RegularImage.getHeight();
		RGPTLogger.logToFile("Original Image Wt: " + m_OrigImageWidth + " Ht: "
				+ m_OrigImageHeight);
		m_PanelColor = ImageUtils.findPanelColor(m_RegularImage);
		m_BackgroundColor = m_PanelColor;
		m_ImageHasAlpha = ImageUtils.hasAlpha(m_RegularImage);
		// Check for Transperency and correspondingly choose Background to
		// create Image
		if (m_ImageHasAlpha) {
			// m_BackgroundColor = ImageUtils.getUniqueColor(m_RegularImage,
			// true);
			m_BackgroundColor = m_PanelColor;
			newImage = ImageUtils.fillTransparentPixels(m_RegularImage,
					m_BackgroundColor);
		} else
			newImage = m_RegularImage;
		float compQuality = (float) RGPTParams
				.getIntVal("IMAGE_COMPRESSION") / (float) 10;
		m_RegularImage = ImageUtils.compressImage(newImage, compQuality);
		m_ThumbviewImage = ImageUtils.scaleImage(m_RegularImage, -1,
				thumbImgHt, true, m_ImageHasAlpha);
		// This maintains the Transformation Image has fone through
		// m_OrigRegularImage = ImageUtils.CreateCopy(m_RegularImage);
		m_ImageTransformHolder = new Vector<ImageTransformHolder>();
		// Checking to create Temp Directories to save the Original Image File
		if (createTempDir) {
			m_SrcDir = RGPTUtil.saveImageData(m_FileName, m_ImgStr, null);
		}
		m_ImgStr = null;
	}

	public void saveTransformedImage() throws Exception {
		if (m_ImageTransformHolder == null
				|| m_ImageTransformHolder.size() == 0)
			return;
		m_RegularImage = ImageTransformHolder.getTransformedImage(this, false);
		m_ImageHasAlpha = ImageUtils.hasAlpha(m_RegularImage);
		String imgFilePath = m_SrcDir + RGPTFileFilter.getFileName(m_FileName);
		String format = "jpg";
		if (m_ImageHasAlpha)
			format = "png";
		imgFilePath += "." + format;
		ImageUtils.SaveImageToFile(m_RegularImage, imgFilePath, format);
		m_FileName = RGPTFileFilter.getFileName(m_FileName) + "." + format;
	}

	public BufferedImage getCompressedImage(BufferedImage image,
			boolean fillAlpha) throws Exception {
		if (image == null)
			image = getOriginalImage();
		boolean hasAlpha = ImageUtils.hasAlpha(image);
		if (m_ImageHasAlpha) {
			if (!fillAlpha)
				return image;
			// m_BackgroundColor = ImageUtils.getUniqueColor(m_RegularImage,
			// true);
			Color bgColor = ImageUtils.findPanelColor(m_RegularImage);
			image = ImageUtils.fillTransparentPixels(image, bgColor);
		}
		float compQuality = (float) RGPTParams
				.getIntVal("IMAGE_COMPRESSION") / (float) 10;
		image = ImageUtils.compressImage(image, compQuality);
		return image;
	}

	public transient AffineTransform m_WindowDeviceCTM = null;
	public transient AffineTransform m_ScreenToImageCTM = null;

	// This represents the position where this image is displayed with in the
	// display panel.
	public transient ScalingFactor m_ScaleFactor = null;
	public transient Rectangle m_ScreenImageRect = null;
	public transient Rectangle2D.Double m_CanvasImageRect = null;

	public void deriveDeviceCTM(ScalingFactor scaleFactor, Dimension panelSize,
			boolean setScaledImage) {
		deriveDeviceCTM(scaleFactor, panelSize,
				RGPTParams.getIntVal("PanelMargin"), setScaledImage);
	}

	public void deriveDeviceCTM(ScalingFactor scaleFactor, Dimension panelSize,
			int specStartYPt, boolean setScaledImage) {
		// Image Ht and Width after Scaling
		double newImgHt = 0.0, newImgWt = 0.0;

		newImgHt = (double) m_RegularImage.getHeight();
		newImgWt = (double) m_RegularImage.getWidth();
		this.deriveDeviceCTM(scaleFactor, panelSize, specStartYPt, newImgWt,
				newImgHt, setScaledImage);
	}

	public Map<String, Object> deriveDeviceCTM(ScalingFactor scaleFactor,
			Dimension panelSize, int specStartYPt, double newImgWt,
			double newImgHt, boolean setScaledImage) {
		Map<String, Object> derivedData = null;
		derivedData = ImageUtils.deriveDeviceCTM(scaleFactor, panelSize,
				specStartYPt, newImgWt, newImgHt);
		m_Scale = ((Double) derivedData.get("Scale")).doubleValue();
		m_ScaledImageWt = ((Double) derivedData.get("ScaledImageWt"))
				.doubleValue();
		m_ScaledImageHt = ((Double) derivedData.get("ScaledImageHt"))
				.doubleValue();
		m_WindowDeviceCTM = (AffineTransform) derivedData
				.get("WindowDeviceCTM");
		m_ScreenToImageCTM = (AffineTransform) derivedData
				.get("ScreenToImageCTM");
		// This is in respect to the Panel Component
		m_ScaleFactor = (ScalingFactor) derivedData.get("ScalingFactor");
		m_ScreenImageRect = (Rectangle) derivedData.get("ScreenImageRect");
		// This in respect to the Canvas Image in Rectangle2D
		m_CanvasImageRect = (Rectangle2D.Double) derivedData
				.get("CanvasImageRect");
		// RGPTLogger.logToFile("Derived Device Info: "+derivedData);
		m_ScaledImage = m_RegularImage;
		if (setScaledImage) {
			m_ScaledImage = ImageUtils.scaleImage(m_RegularImage,
					(int) m_ScaledImageWt, (int) m_ScaledImageHt, true,
					m_ImageHasAlpha);
			RGPTLogger.logToFile("Regular Image wt: "
					+ m_RegularImage.getWidth() + " ht: "
					+ m_RegularImage.getHeight() + " ScaledImage wt: "
					+ m_ScaledImage.getWidth() + " ht:"
					+ m_ScaledImage.getHeight() + " BBox: " + panelSize);
			// int x = m_ScreenImageRect.x, y = m_ScreenImageRect.y,
			// wt = m_ScreenImageRect.width, ht = m_ScreenImageRect.height;
			// if (x+wt < m_ScaledImage.getWidth() && y+ht <
			// m_ScaledImage.getHeight()) {
			// RGPTLogger.logToFile("Getting Sub Image");
			// m_ClippedImage = m_ScaledImage.getSubimage(x, y, wt, ht);
			// }
		}
		m_ClippedImage = ImageUtils.createImageCopy(m_ScaledImage);

		// RGPTLogger.logToFile("Windows CTM: "+m_WindowDeviceCTM+
		// "\nImage CTM: "+m_ScreenToImageCTM);
		// RGPTLogger.logToFile("\n\nScale: "+m_Scale+
		// " Windows CTM Scale: "+m_WindowDeviceCTM.getScaleX()+
		// " Image CTM ScaleX: "+m_ScreenToImageCTM.getScaleX()+
		// " Image CTM ScaleY: "+m_ScreenToImageCTM.getScaleY());
		return derivedData;
	}

	// This fields mailntains the handle to Image Filter Controller which keeps
	// the record of Image Filters
	// applied on the Image
	private ImageFilterController m_ImageFilterController = null;
	// This holds the last Image Filter that is applied on the Image as well as
	// the control points
	public transient String m_ImageFilterAction = null;
	public transient Map<String, Object> m_ImageFilterControlValues;

	public void resetFields() {
		m_ImageFilterAction = null;
		m_ImageFilterControlValues = null;
	}

	public ImageFilterController getImageFilterController() {
		if (m_ImageFilterController == null)
			m_ImageFilterController = new ImageFilterController();
		return m_ImageFilterController;
	}

	public ImageFilterHandler setImageFilterParam(String action, float val,
			Rectangle bounds) {
		RGPTLogger.logToFile("Setting Filter Param Values for action: "
				+ action);
		String[] actionParam = action.split("_");
		m_ImageFilterAction = actionParam[0];
		OnOffParamControlFilterHdlr paramFilterHdlr = null;
		if (actionParam.length == 2) {
			if (actionParam[1].equals("On")) {
				String pinchFltr = RGPTActionListener.ImageFilterActions.PinchFilter
						.toString();
				String bulgeFltr = RGPTActionListener.ImageFilterActions.BulgeFilter
						.toString();
				String sphereFltr = RGPTActionListener.ImageFilterActions.SphereFilter
						.toString();
				float radFactor = RGPTParams
						.getFloatVal("ImageFilterRadialFactor");
				StringBuffer ctrlPtsBuf = new StringBuffer();
				if (m_ImageFilterAction.equals(ImageFilterActions.CircleFilter
						.toString())) {
					ImageFilterHandler imgFilterHdlr = m_ImageFilterController
							.setImageFilterHandler(m_ImageFilterAction);
					paramFilterHdlr = (OnOffParamControlFilterHdlr) imgFilterHdlr;
					Map<String, String> paramValues = paramFilterHdlr.m_ParamSetValues;
					float paramHt = radFactor
							* (float) m_ScaledImage.getHeight() / 2.0f;
					paramValues.put("Height", Float.toString(paramHt));
					float rotAng = Float.valueOf(paramValues.get("RotAngle"));
					Point2D.Double centerPt = null, rotPt = null, spreadPt = null, radPt = null;
					int x = bounds.x, y = bounds.y, w = bounds.width, h = bounds.height;
					double rotRad = (double) w / 4.0D, spreadRad = (double) w / 3.0D, rad = (double) w / 2.0D;
					centerPt = new Point2D.Double(x + (double) w / 2.0D, y
							+ (double) h / 2.0D);
					rotPt = new Point2D.Double(centerPt.x + rotRad, centerPt.y);
					spreadPt = new Point2D.Double(centerPt.x + spreadRad,
							centerPt.y);
					radPt = new Point2D.Double(centerPt.x + rad, centerPt.y);
					m_ImageFilterControlValues = new HashMap<String, Object>();
					Map<String, Object> fltrVals = m_ImageFilterControlValues;
					fltrVals.put("CenterPt", centerPt);
					fltrVals.put("RotAngPt", rotPt);
					fltrVals.put("SpreadPt", spreadPt);
					fltrVals.put("RadialPt", radPt);
					fltrVals.put("RotRadius", rotRad);
					fltrVals.put("SpreadRadius", spreadRad);
					fltrVals.put("Radius", rad);
					fltrVals.put("Height", paramHt);
					fltrVals.put("RotAngle",
							Float.valueOf(paramValues.get("RotAngle")));
					fltrVals.put("SpreadAngle",
							Float.valueOf(paramValues.get("SpreadAngle")));
					// RGPTLogger.logToFile("Set Param Values: "+paramFilterHdlr.m_ParamSetValues);
				} // If Loop for Circle Filter
				else if (m_ImageFilterAction
						.equals(ImageFilterActions.WarpFilter.toString())) {
					ImageFilterHandler imgFilterHdlr = m_ImageFilterController
							.setImageFilterHandler(m_ImageFilterAction);
					paramFilterHdlr = (OnOffParamControlFilterHdlr) imgFilterHdlr;
					Map<String, Object> paramValues = paramFilterHdlr.m_FilterParamValues;
					Vector<Point2D.Double> srcGridPts = null, desGridPts = null;
					// srcGridPts = (Vector<Point2D.Double>)
					// paramValues.get("SrcGirdPts");
					// desGridPts = (Vector<Point2D.Double>)
					// paramValues.get("DesGirdPts");
					srcGridPts = new Vector<Point2D.Double>();
					desGridPts = new Vector<Point2D.Double>();
					int numGrid = RGPTParams.getIntVal("NumOfGrid");
					int x = bounds.x, y = bounds.y, w = bounds.width, h = bounds.height;
					float gridWt = 0.0F, gridHt = 0.0F;
					double xPt = 0.0, yPt = 0.0;
					gridWt = ((float) w) / (float) numGrid;
					gridHt = ((float) h) / (float) numGrid;
					for (int row = 0; row < numGrid; row++) {
						yPt = y + (gridHt / 2.0F) + row * gridHt; // if (yPt >
																	// imgHt)
																	// continue;
						yPt = y + (row * (h - 1) / (numGrid - 1)); // if (yPt >
																	// imgHt)
																	// continue;
						for (int col = 0; col < numGrid; col++) {
							xPt = x + (gridWt / 2.0F) + col * gridWt; // if (xPt
																		// >
																		// imgWt)
																		// continue;
							xPt = x + (col * (w - 1) / (numGrid - 1)); // if
																		// (xPt
																		// >
																		// imgWt)
																		// continue;
							srcGridPts.addElement(new Point2D.Double(xPt, yPt));
							desGridPts.addElement(new Point2D.Double(xPt, yPt));
						}
					}
					m_ImageFilterControlValues = new HashMap<String, Object>();
					m_ImageFilterControlValues.put("SrcGirdPts", srcGridPts);
					m_ImageFilterControlValues.put("DesGirdPts", desGridPts);
					// RGPTLogger.logToFile("Initial Set Points: "+srcGridPts);
				} else if (isRadialFilter(ctrlPtsBuf)) {
					ImageFilterHandler imgFilterHdlr = m_ImageFilterController
							.setImageFilterHandler(m_ImageFilterAction);
					paramFilterHdlr = (OnOffParamControlFilterHdlr) imgFilterHdlr;
					Map<String, String> paramValues = paramFilterHdlr.m_ParamSetValues;
					float radius = radFactor * (float) m_ScaledImage.getWidth()
							/ 2.0f;
					String[] ctrlPts = ctrlPtsBuf.toString().split("::");
					Point2D.Double centerPt = null, radialPt = null;
					Point2D.Double sliderPt = null, anglePt = null;
					int x = bounds.x, y = bounds.y, w = bounds.width, h = bounds.height;
					double ctrlRad = (double) w / 3.0D, radialPtRad = (double) w / 2.0D;
					double sliderRad = 0.0D, angleRad = 0.0D;
					float amt = 1.0f, angle = 0.0f;
					radius = (float) (radialPtRad);
					paramValues.put("Radius", Float.toString(radius));
					paramValues.put("RadialFactor", Float.toString(radFactor));
					if (RGPTUtil.contains(ctrlPts, "SliderPt")) {
						sliderRad = ctrlRad;
						amt = Float.valueOf(paramValues.get("Amount"));
					}
					if (RGPTUtil.contains(ctrlPts, "AnglePt")) {
						angleRad = ctrlRad;
						angle = Float.valueOf(paramValues.get("Angle"));
					}
					if (sliderRad == angleRad)
						angleRad = (double) w / 4.0D;
					centerPt = new Point2D.Double(x + (double) w / 2.0D, y
							+ (double) h / 2.0D);
					sliderPt = new Point2D.Double(centerPt.x + amt * sliderRad,
							centerPt.y);
					anglePt = new Point2D.Double(centerPt.x + angleRad,
							centerPt.y);
					radialPt = new Point2D.Double(centerPt.x + radialPtRad,
							centerPt.y);
					m_ImageFilterControlValues = new HashMap<String, Object>();
					Map<String, Object> fltrVals = m_ImageFilterControlValues;
					fltrVals.put("CenterPt", centerPt);
					fltrVals.put("CircleCenterPt", centerPt);
					fltrVals.put("SliderPt", sliderPt);
					fltrVals.put("AnglePt", anglePt);
					fltrVals.put("SliderRad", sliderRad);
					fltrVals.put("AngleRad", angleRad);
					fltrVals.put("RadialPt", radialPt);
					fltrVals.put("Radius", radius);
					fltrVals.put("Amount", amt);
					fltrVals.put("Angle", angle);
					fltrVals.put("CenterX",
							Float.valueOf(paramValues.get("CenterX")));
					fltrVals.put("CenterY",
							Float.valueOf(paramValues.get("CenterY")));
					RGPTLogger.logToFile("Set Param Values: "
							+ paramFilterHdlr.m_ParamSetValues);
				} // If Loop for Pinch Filter
			} else if (actionParam[1].equals("Off")) {
				m_ImageFilterAction = null;
				m_ImageFilterControlValues = null;
			}
		}
		return m_ImageFilterController.setImageFilterParam(action, val);
	}

	public Map<String, Object> getImageFilterControlValues() {
		return m_ImageFilterControlValues;
	}

	public boolean isFilterCirclePt(Point pt) {
		Map<String, Object> fltrVals = m_ImageFilterControlValues;
		float radius = ((Float) fltrVals.get("Radius")).floatValue();
		Point2D.Double cntrPt = (Point2D.Double) fltrVals.get("CircleCenterPt");
		double dx = Math.abs(pt.x - cntrPt.x), dy = Math.abs(pt.y - cntrPt.y);
		double ptRad = Math.sqrt(Math.pow(dx, 2.0) + Math.pow(dy, 2.0));
		double radFactor = ptRad / (double) radius;
		if (radFactor > 1)
			radFactor--;
		RGPTLogger.logToFile("Radius: " + radius + " Clicked Pt Radius: "
				+ ptRad + " Radial Factor: " + radFactor);
		if (radFactor <= 0.1)
			return true;
		return false;
	}

	public boolean isRadialFilter() {
		return isRadialFilter(null);
	}

	public boolean isRadialFilter(StringBuffer cntrlPts) {
		if (m_ImageFilterAction == null)
			return false;
		if (cntrlPts == null)
			cntrlPts = new StringBuffer();
		cntrlPts.append("CenterPt::SliderPt::RadialPt");
		String imgFltr = m_ImageFilterAction;
		if (RGPTUtil.contains(
				RGPTActionListener.CENTER_SLIDER_RADIAL_PTS_FILTERS, imgFltr))
			return true;
		if (RGPTUtil.contains(RGPTActionListener.CENTER_RADIAL_PTS_FILTERS,
				imgFltr)) {
			cntrlPts.setLength(0);
			cntrlPts.append("CenterPt::RadialPt");
			return true;
		}
		if (RGPTUtil.contains(RGPTActionListener.ALL_CONTROL_PTS_FILTERS,
				imgFltr)) {
			cntrlPts.setLength(0);
			cntrlPts.append("CenterPt::AnglePt::SliderPt::RadialPt");
			return true;
		}
		if (RGPTUtil.contains(
				RGPTActionListener.CENTER_SLIDER_ANGLE_PTS_FILTERS, imgFltr)) {
			cntrlPts.setLength(0);
			cntrlPts.append("CenterPt::AnglePt::SliderPt");
			return true;
		}
		if (RGPTUtil.contains(
				RGPTActionListener.CENTER_ANGLE_RADIAL_PTS_FILTERS, imgFltr)) {
			cntrlPts.setLength(0);
			cntrlPts.append("CenterPt::AnglePt::RadialPt");
			return true;
		}
		if (RGPTUtil.contains(RGPTActionListener.CENTER_SLIDER_PTS_FILTERS,
				imgFltr)) {
			cntrlPts.setLength(0);
			cntrlPts.append("CenterPt::SliderPt");
			return true;
		}
		return false;
	}

	public boolean isRadialAdjFilter() {
		if (m_ImageFilterAction == null)
			return false;
		String imgFltr = m_ImageFilterAction;
		if (RGPTUtil.contains(RGPTActionListener.RADIAL_ADJ_FILTER_EXCEPTIONS,
				imgFltr))
			return false;
		if (RGPTUtil.contains(RGPTActionListener.CENTER_RADIAL_PTS_FILTERS,
				imgFltr))
			return false;
		StringBuffer cntrlPts = new StringBuffer();
		isRadialFilter(cntrlPts);
		String[] ctrlKeys = cntrlPts.toString().split("::");
		if (RGPTUtil.contains(ctrlKeys, "RadialPt"))
			return true;
		return false;
	}

	public boolean isCenterPtAdj() {
		if (m_ImageFilterAction == null)
			return false;
		String imgFltr = m_ImageFilterAction;
		if (RGPTUtil.contains(RGPTActionListener.CENTER_ADJ_FILTER_EXCEPTIONS,
				imgFltr))
			return false;
		if (RGPTUtil.contains(RGPTActionListener.CENTER_RADIAL_PTS_FILTERS,
				imgFltr))
			return false;
		StringBuffer cntrlPts = new StringBuffer();
		isRadialFilter(cntrlPts);
		String[] ctrlKeys = cntrlPts.toString().split("::");
		if (RGPTUtil.contains(ctrlKeys, "RadialPt"))
			return true;
		return false;
	}

	public boolean isCircleFilter() {
		if (m_ImageFilterAction == null)
			return false;
		if (m_ImageFilterAction.equals(ImageFilterActions.CircleFilter
				.toString()))
			return true;
		return false;
	}

	public boolean isWarpFilter() {
		if (m_ImageFilterAction == null)
			return false;
		if (m_ImageFilterAction
				.equals(ImageFilterActions.WarpFilter.toString()))
			return true;
		return false;
	}

	public Vector<Point2D.Double> getFilterControlPoints(String controlKey) {
		return getFilterControlPoints(controlKey, null);
	}

	public Vector<Point2D.Double> getFilterControlPoints(String controlKey,
			String[] ctrlKeys) {
		Vector<Point2D.Double> gPathPoints = new Vector<Point2D.Double>();
		Point2D.Double centPt = (Point2D.Double) m_ImageFilterControlValues
				.get("CenterPt");
		if (controlKey != null) {
			Point2D.Double controlPt = (Point2D.Double) m_ImageFilterControlValues
					.get(controlKey);
			gPathPoints.addElement(centPt);
			gPathPoints.addElement(controlPt);
			return gPathPoints;
		}
		Point2D.Double ctrlPt = null;
		for (int i = 0; i < ctrlKeys.length; i++) {
			ctrlPt = (Point2D.Double) m_ImageFilterControlValues
					.get(ctrlKeys[i]);
			gPathPoints.addElement(ctrlPt);
		}
		return gPathPoints;
	}

	public void adjustImageFilter(AffineTransform affine, Rectangle bounds) {
		ImageFilterHandler imgFilterHdlr = null;
		getImageFilterController();
		OnOffParamControlFilterHdlr paramFilterHdlr = null;
		// Setting Warp Filters new Values if Used
		String imgFilter = RGPTActionListener.ImageFilterActions.WarpFilter
				.toString();
		imgFilterHdlr = m_ImageFilterController
				.getImageFilterHandler(imgFilter);
		if (imgFilterHdlr != null) {
			String filterAction = "_Off";
			setImageFilterParam(imgFilter + filterAction, -1.0f, bounds);
			// paramFilterHdlr = (OnOffParamControlFilterHdlr) imgFilterHdlr;
			// Map<String, Object> paramValues =
			// paramFilterHdlr.m_FilterParamValues;
			// Vector<Point2D.Double> srcGridPts = null, desGridPts = null;
			// srcGridPts = (Vector<Point2D.Double>)
			// paramValues.get("SrcGirdPts");
			// desGridPts = (Vector<Point2D.Double>)
			// paramValues.get("DesGirdPts");
			// RGPTUtil.getTransformedPt(affine, srcGridPts, srcGridPts);
			// RGPTUtil.getTransformedPt(affine, desGridPts, desGridPts);
		}
	}

	private void resetImageFilterParam(AffineTransform screenToImageCTM,
			Rectangle bounds) {
		ImageFilterHandler imgFilterHdlr = null;
		String imgFilter = "";
		OnOffParamControlFilterHdlr paramFilterHdlr = null;
		RGPTActionListener.ImageFilterActions[] onOffImgFltrs = null;
		onOffImgFltrs = RGPTActionListener.ON_OFF_PARAM_CONTROL_IMAGE_FILTERS;
		// String imgFilter =
		// RGPTActionListener.ImageFilterActions.CircleFilter.toString();
		for (int i = 0; i < onOffImgFltrs.length; i++) {
			// RGPTLogger.logToFile("ON_OFF_PARAM_CONTROL_IMAGE_FILTERS is: "+onOffImgFltrs[i]);
			// Setting Circle Filters new Values if Used
			imgFilter = onOffImgFltrs[i].toString();
			// if
			// (imgFilter.equals(RGPTActionListener.ImageFilterActions.CircleFilter.toString()))
			if (onOffImgFltrs[i] == RGPTActionListener.ImageFilterActions.CircleFilter) {
				imgFilterHdlr = m_ImageFilterController
						.getImageFilterHandler(imgFilter);
				float radFactor = RGPTParams
						.getFloatVal("ImageFilterRadialFactor");
				if (imgFilterHdlr != null) {
					paramFilterHdlr = (OnOffParamControlFilterHdlr) imgFilterHdlr;
					Map<String, String> paramValues = paramFilterHdlr.m_ParamSetValues;
					float paramHt = radFactor
							* (float) m_ScaledImage.getHeight() / 2.0f;
					float ht = Float.valueOf(paramValues.get("Height"));
					if (paramHt > ht)
						paramValues.put("Height", Float.toString(paramHt));
					if (m_ImageFilterAction != null
							&& m_ImageFilterAction.equals(imgFilter)) {
						Float flVal = (Float) m_ImageFilterControlValues
								.get("SpreadAngle");
						paramValues.put("SpreadAngle", flVal.toString());
						flVal = (Float) m_ImageFilterControlValues
								.get("RotAngle");
						paramValues.put("RotAngle", flVal.toString());
						flVal = (Float) m_ImageFilterControlValues
								.get("Height");
						paramValues.put("Height", flVal.toString());
					}
				} // ImageFilterActions.CircleFilter
			} else if (onOffImgFltrs[i] == RGPTActionListener.ImageFilterActions.WarpFilter) {
				imgFilterHdlr = m_ImageFilterController
						.getImageFilterHandler(imgFilter);
				if (imgFilterHdlr != null) {
					if (m_ImageFilterAction != null
							&& m_ImageFilterAction.equals(imgFilter)) {
						Map<String, Object> fltrVals = m_ImageFilterControlValues;
						Vector<Point2D.Double> srcScrGridPts = null, desScrGridPts = null;
						srcScrGridPts = (Vector<Point2D.Double>) fltrVals
								.get("SrcGirdPts");
						desScrGridPts = (Vector<Point2D.Double>) fltrVals
								.get("DesGirdPts");
						Map<String, Object> ctm = null;
						int numGrid = RGPTParams.getIntVal("NumOfGrid");
						float gridWt = ((float) bounds.width) / (float) numGrid;
						float gridHt = ((float) bounds.height)
								/ (float) numGrid;
						int tx = Math.round(bounds.x + gridWt / 2.0f), ty = Math
								.round(bounds.y + gridHt / 2.0f);
						tx = bounds.x;
						ty = bounds.y;
						RGPTUtil.adjustPt(srcScrGridPts, -tx, -ty);
						RGPTUtil.adjustPt(desScrGridPts, -tx, -ty);
						paramFilterHdlr = (OnOffParamControlFilterHdlr) imgFilterHdlr;
						Map<String, Object> paramValues = paramFilterHdlr.m_FilterParamValues;
						Vector<Point2D.Double> srcGridPts = null, desGridPts = null;
						srcGridPts = (Vector<Point2D.Double>) paramValues
								.get("SrcGirdPts");
						desGridPts = (Vector<Point2D.Double>) paramValues
								.get("DesGirdPts");
						double imgWt = m_ScaledImage.getWidth();
						double imgHt = m_ScaledImage.getHeight();
						ctm = ImageUtils.deriveDeviceCTM(null,
								bounds.getSize(), 0, imgWt, imgHt);
						screenToImageCTM = (AffineTransform) ctm
								.get("ScreenToImageCTM");
						// RGPTLogger.logToFile("Screen Bounds: "+bounds+" ImgWt: "+imgWt+" ImgHt: "+imgHt);
						// RGPTLogger.logToFile("After Adjustments Points: "+srcScrGridPts);
						RGPTUtil.getTransformedPt(screenToImageCTM,
								srcScrGridPts, srcGridPts);
						RGPTUtil.getTransformedPt(screenToImageCTM,
								desScrGridPts, desGridPts);
						// RGPTLogger.logToFile("Transf Points: "+srcGridPts);
						RGPTUtil.adjustPt(srcScrGridPts, tx, ty);
						RGPTUtil.adjustPt(desScrGridPts, tx, ty);
					}
				} // ImageFilterActions.WarpFilter
			} else
				populateImageFilterParam(imgFilter, bounds);
		}
	}

	private void populateImageFilterParam(String imgFilter, Rectangle bounds) {
		StringBuffer ctrlPtsBuf = new StringBuffer();
		if (!isRadialFilter(ctrlPtsBuf))
			return;
		ImageFilterHandler imgFilterHdlr = null;
		OnOffParamControlFilterHdlr paramFilterHdlr = null;
		float radFactor = RGPTParams
				.getFloatVal("ImageFilterRadialFactor");
		imgFilterHdlr = m_ImageFilterController
				.getImageFilterHandler(imgFilter);
		if (imgFilterHdlr == null)
			return;
		paramFilterHdlr = (OnOffParamControlFilterHdlr) imgFilterHdlr;
		Map<String, String> paramValues = paramFilterHdlr.m_ParamSetValues;
		// Map<String, Object> paramObjValues =
		// paramFilterHdlr.m_FilterParamValues;
		float radFactorSetting = radFactor;
		radFactor = Float.valueOf(paramValues.get("RadialFactor"));
		float radius = radFactor * (float) m_ScaledImage.getWidth() / 2.0f;
		int x = bounds.x, y = bounds.y, w = bounds.width, h = bounds.height;
		radius = (float) ((double) w / 2.0D);
		String[] ctrlKeys = ctrlPtsBuf.toString().split("::");
		if (m_ImageFilterAction != null
				&& m_ImageFilterAction.equals(imgFilter)) {
			// paramValues.put("Radius", Float.toString(radius));
			Map<String, Object> fltrVals = m_ImageFilterControlValues;
			float origRad = (float) ((double) w / 2.0D);
			radius = ((Float) fltrVals.get("Radius")).floatValue();
			float radialFactor = (float) (radius / origRad);
			Point2D.Double centerPt = (Point2D.Double) fltrVals.get("CenterPt");
			if (radialFactor > radFactorSetting && !isRadialAdjFilter()) {
				radialFactor = radFactorSetting;
				radius = (float) ((double) w / 2.0D);
				fltrVals.put("Radius", (float) radius);
				fltrVals.put("RadialFactor", radialFactor);
				Point2D.Double radPt = (Point2D.Double) fltrVals
						.get("RadialPt");
				radPt.x = centerPt.x + radius;
			}
			float centerX = (float) (centerPt.x - x) / (float) w;
			float centerY = (float) (centerPt.y - y) / (float) h;
			paramValues.put("CenterX", Float.toString(centerX));
			paramValues.put("CenterY", Float.toString(centerY));
			paramValues.put("Radius", Float.toString(radius));
			if (RGPTUtil.contains(ctrlKeys, "SliderPt")) {
				Point2D.Double sliderPt = (Point2D.Double) fltrVals
						.get("SliderPt");
				double sliderRad = ((Double) fltrVals.get("SliderRad"))
						.doubleValue();
				float amt = (float) ((sliderPt.x - centerPt.x) / sliderRad);
				paramValues.put("Amount", Float.toString(amt));
			}
			if (RGPTUtil.contains(ctrlKeys, "AnglePt")) {
				Float angle = (Float) fltrVals.get("Angle");
				paramValues.put("Angle", Float.toString(angle));
			}
			paramValues.put("RadialFactor", Float.toString(radialFactor));
		}
	}

	public transient Map<String, BufferedImage> m_ThumbFilterImage;

	public BufferedImage applyImageFilter(
			RGPTActionListener.ImageFilterActions action) {
		if (m_ThumbFilterImage == null)
			m_ThumbFilterImage = new HashMap<String, BufferedImage>();
		if (m_ImageFilterController == null)
			getImageFilterController();
		BufferedImage filterImg = m_ThumbFilterImage.get(action.toString());
		if (filterImg != null)
			return filterImg;
		BufferedImage thumbImg = (BufferedImage) m_ThumbviewImage;
		boolean usePixelRGB = false;
		String negFltr = RGPTActionListener.ImageFilterActions.NegativeFilter
				.toString();
		String imgOutlineFltr = RGPTActionListener.ImageFilterActions.ImageOutlineFilter
				.toString();
		String bwSketchFltr = RGPTActionListener.ImageFilterActions.BWSketchedFilter
				.toString();
		if (action.toString().equals(negFltr)
				|| action.toString().equals(imgOutlineFltr)
				|| action.toString().equals(bwSketchFltr)) {
			usePixelRGB = true;
			if (m_ScaledImage != null)
				thumbImg = m_ScaledImage;
		}
		// RGPTLogger.logToFile(action+" Image Set");
		filterImg = m_ImageFilterController.applyFilter(thumbImg,
				action.toString(), "On", -1, usePixelRGB);
		// if (action.toString().equals("BlurFadeFilter"))
		// ImageUtils.displayImage(filterImg, action.toString());
		m_ThumbFilterImage.put(action.toString(), filterImg);
		return filterImg;
	}

	public void applyFilters(AffineTransform screenToImageCTM, Rectangle bounds) {
		if (m_ImageFilterController == null)
			return;
		resetImageFilterParam(screenToImageCTM, bounds);
		BufferedImage finImg = m_ImageFilterController
				.applyFilter(m_ScaledImage);
		// ImageUtils.SaveImage(finImg, "test_output/filter.png", "png");
		m_ClippedImage = finImg;
	}

	public String toString() {
		StringBuffer mesg = new StringBuffer();
		mesg.append("Src Dir: " + m_SrcDir + " :FileName: " + m_FileName);
		mesg.append(" Img Width: " + m_OrigImageWidth + " :Ht: "
				+ m_OrigImageHeight);
		mesg.append(" Theme Id: " + m_ThemeId + " Use Cache: "
				+ m_UseCachedImages);
		mesg.append(" ImageFrom: " + m_ImageFromMode + " Is_Clipped: "
				+ m_IsClipped);
		if (m_ImgStr != null && m_ImgStr.length > 0)
			mesg.append(" Img Stream Size: " + m_ImgStr.length);
		if (m_ImageBBox != null)
			mesg.append("\nImage BBox in PDF: " + m_ImageBBox.toString());
		if (m_DeviceBBox != null)
			mesg.append("\nDevice BBox in PDF: " + m_DeviceBBox.toString());
		mesg.append(" Scale: " + m_Scale);
		if (m_ScalingMode == SCALED_ALONG_HEIGHT)
			mesg.append(" Scale Along Height");
		else if (m_ScalingMode == SCALED_ALONG_WIDTH)
			mesg.append(" Scale Along Width");
		if (m_DisplayRect != null)
			mesg.append(" Display Rect: " + m_DisplayRect.toString());
		if (m_ClipRectangle != null)
			mesg.append(" Clip Rect: " + m_ClipRectangle.toString());
		return mesg.toString();
	}
}
