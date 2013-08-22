// RGPT PACKAGES
package com.rgpt.templateutil;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
// This files are added to support serialization
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.rgpt.imageutil.ImageFilterController;
import com.rgpt.imageutil.ImageFilterHandler;
import com.rgpt.imageutil.ImageHolder;
import com.rgpt.imageutil.ImageUtils;
import com.rgpt.util.RGPTParams;
import com.rgpt.util.ClipPath;
import com.rgpt.util.RGPTActionListener;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTMemoryHandle;
import com.rgpt.util.RGPTMemoryManager;
import com.rgpt.util.RGPTShapeUtil;
import com.rgpt.util.RGPTUtil;
import com.rgpt.util.RecycleMemoryData;

public class VDPImageFieldInfo extends VDPFieldInfo implements Serializable,
		RGPTMemoryHandle {
	static final long serialVersionUID = -5176844419587977367L;

	public int m_BitsPerComponent;
	public int m_ColorComponents;
	public double m_ImageHeight;
	public double m_ImageWidth;
	public int m_RenderingIntent;
	public int m_ClipSegmentCount;
	public Vector m_ClipPath;
	public double m_ImageAlphaValue = 1.0;

	// This is process Background Images
	public boolean m_IsBackgroundImage = false;

	// Indicator if the Image Mask is set on this Image
	public boolean m_SetImageMask = false;

	// Image Mask will be extracted in the server using the PDFUtil function
	public java.awt.Image m_ImageMask;

	// NEW FIELDS for setting the Theme and if Theme Images are used, can the
	// user still upload Images. Added as of Sept 13, 2009
	public int m_ThemeId = -1;
	public boolean m_AllowUploadWithTheme = false;
	public static Vector<Integer> m_TemplateThemes;

	// This is a static field and holds VDP Image Fields that need to be
	// prepopulated
	public static Vector<String> m_VDPPrepopulatedFields;

	// Picture Frame Image File . This can be a transperant or opaque frame
	public boolean m_ShowPictureFrame = true;
	public ImageHolder m_PictureFrameHolder = null;
	public boolean m_IsOpaquePictureFrame = true;
	public transient BufferedImage m_PictureFrame = null;

	// This field is relevant only for XONImage Designer and holds the actual
	// image
	public ImageHolder m_ImageHolder = null;

	// This field is relevant to XON IMage Designer and indicates the rotation
	// of image
	// element in degrees
	public double m_RotAngle = 0.0D;

	static {
		m_TemplateThemes = new Vector<Integer>();
		m_VDPPrepopulatedFields = new Vector<String>();
	}

	public VDPImageFieldInfo() {
		m_ClipPath = new Vector();
	}

	public VDPImageFieldInfo(String name, double imgWt, double imgHt,
			boolean isPDFElem) {
		super("Image", name, "", "", isPDFElem);
		m_ImageWidth = imgWt;
		m_ImageHeight = imgHt;
		m_ClipPath = new Vector();
	}

	// load Picture Frame
	private void loadPictureFrame() {
		try {
			RGPTLogger.logToFile("PictureFrame: "
					+ m_PictureFrameHolder.toString());
			m_PictureFrame = ImageUtils
					.getBufferedImage(m_PictureFrameHolder.m_ImgStr);
			// m_PictureFrameHolder = new
			// ImageHolder(RGPTUtil.getBytes(frameImgPath));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void buildProperFields() {
		if (m_ShowPictureFrame)
			this.loadPictureFrame();
		if (m_IsVDPPrepopulated) {
			if (!m_VDPPrepopulatedFields.contains(m_FieldName))
				m_VDPPrepopulatedFields.addElement(m_FieldName);
		}
		if (m_ClipPath == null)
			m_ClipPath = new Vector();
		m_ClipSegmentCount = m_ClipPath.size();
		System.out.println("Clip Segment Size : " + m_ClipSegmentCount
				+ " Vector Size " + m_ClipPath.size());
		for (int i = 0; i < m_ClipPath.size(); i++) {
			ClipPath clipPath = (ClipPath) m_ClipPath.elementAt(i);
			if (clipPath == null)
				continue;
			clipPath.buildProperFields();
		}
		if (!m_TemplateThemes.contains(m_ThemeId) && m_ThemeId != -1)
			m_TemplateThemes.addElement(m_ThemeId);
		m_AlphaValue = m_ImageAlphaValue;
		super.buildProperFields();
	}

	private GeneralPath drawClipPath(PDFPageInfo pdfPageInfo) {
		ClipPath clipPath = null;
		GeneralPath clipShape = null, finalShape = null;
		for (int i = 0; i < m_ClipPath.size(); i++) {
			clipPath = (ClipPath) m_ClipPath.elementAt(i);
			if (clipPath == null)
				continue;
			clipShape.append(clipPath.getClipShape(pdfPageInfo,
					new Point2D.Double(0.0, 0.0)), true);
		}
		return clipShape;
	}

	public void setFieldSelection() {
		super.setFieldSelection();
		if (m_IsBackgroundImage)
			m_IsBGCanvasSelected = true;
	}

	public void resetFields() {
		super.resetFields();
		m_ImageHolder.resetFields();
	}

	// This methods call the ImageHolder to process ImageFilter

	public String getImageFilterAction() {
		return m_ImageHolder.m_ImageFilterAction;
	}

	public ImageFilterController getImageFilterController() {
		return m_ImageHolder.getImageFilterController();
	}

	public ImageFilterHandler setImageFilterParam(String action, float val) {
		return m_ImageHolder.setImageFilterParam(action, val, m_ScreenBounds);
	}

	public Map<String, Object> getImageFilterControlValues() {
		return m_ImageHolder.m_ImageFilterControlValues;
	}

	public boolean isFilterCirclePt(Point pt) {
		return m_ImageHolder.isFilterCirclePt(pt);
	}

	public boolean isRadialFilter() {
		return isRadialFilter(null);
	}

	public boolean isRadialFilter(StringBuffer cntrlPts) {
		return m_ImageHolder.isRadialFilter(cntrlPts);
	}

	public boolean isRadialAdjFilter() {
		return m_ImageHolder.isRadialAdjFilter();
	}

	public boolean isCenterPtAdj() {
		return m_ImageHolder.isCenterPtAdj();
	}

	public boolean isCircleFilter() {
		return m_ImageHolder.isCircleFilter();
	}

	public boolean isWarpFilter() {
		return m_ImageHolder.isWarpFilter();
	}

	public Vector<Point2D.Double> getFilterControlPoints(String controlKey) {
		return m_ImageHolder.getFilterControlPoints(controlKey, null);
	}

	public Vector<Point2D.Double> getFilterControlPoints(String controlKey,
			String[] ctrlKeys) {
		return m_ImageHolder.getFilterControlPoints(controlKey, ctrlKeys);
	}

	public void adjustImageFilter(AffineTransform affine) {
		m_ImageHolder.adjustImageFilter(affine, m_ScreenBounds);
	}

	public BufferedImage applyImageFilter(
			RGPTActionListener.ImageFilterActions action) {
		return m_ImageHolder.applyImageFilter(action);
	}

	public void applyFilters() {
		applyFilters(null);
	}

	public void applyFilters(AffineTransform screenToImageCTM) {
		m_ImageHolder.applyFilters(screenToImageCTM, m_ScreenBounds);
	}

	// This functions sets the image width and height and the regenerates the
	// Scaled Image
	// to be shown in the canvas.
	private transient RecycleMemoryData m_RecycleMemoryData = null;
	public transient boolean m_ImageMakeoverActivated = false;

	public void setGPathPts(AffineTransform screenToImageCTM, boolean isScaled)
			throws Exception {
		if (m_IsBackgroundImage) {
			applyFilters();
			return;
		}
		boolean isInitialSetup = false;
		if (m_GPathPoints == null)
			isInitialSetup = true;
		m_GPathPoints = RGPTUtil.getTransformedPt(screenToImageCTM,
				m_ScreenPathPoints);
		if (!isInitialSetup && !isScaled)
			return;
		GeneralPath gPath = RGPTShapeUtil.drawPath(m_PathSelection, null,
				m_GPathPoints, null);
		java.awt.Rectangle pathBounds = gPath.getBounds();
		m_ImageWidth = pathBounds.width;
		m_ImageHeight = pathBounds.height;
		boolean handleMemory = false;
		if (m_ImageHolder.m_RegularImage == null) {
			m_ImageHolder.m_RegularImage = m_ImageHolder.getCompressedImage(
					null, false);
			handleMemory = true;
		} else if (m_RecycleMemoryData == null)
			handleMemory = true;
		else
			m_RecycleMemoryData.lastAccess = Calendar.getInstance()
					.getTimeInMillis();
		if (handleMemory) {
			int duration = RGPTParams.getIntVal("ImageMemoryDuration");
			m_RecycleMemoryData = RGPTMemoryManager.getInstance().handleMemory(
					duration * 1000, this, null);
		}
		m_ImageHolder.deriveDeviceCTM(null, pathBounds.getSize(), 0, true);
		applyFilters();
	}

	public boolean handleMamory(HashMap memoryData) {
		if (m_ImageMakeoverActivated)
			return false;
		RGPTLogger.logToFile("Handling Memory for " + m_FieldName);
		m_ImageHolder.m_RegularImage = null;
		m_RecycleMemoryData = null;
		return true;
	}

	public BufferedImage getPreviewImage() {
		if (m_PreviewImage != null)
			return m_PreviewImage;
		m_PreviewImage = (BufferedImage) m_ImageHolder.m_ThumbviewImage;
		return m_PreviewImage;
	}

	public String toString() {
		return this.toString(new StringBuffer());
	}

	public String toString(StringBuffer mesg) {
		super.toString(mesg);
		mesg.append("\n-----VDP IMAGE INFO.-----\n");
		mesg.append("Width: " + m_ImageWidth);
		mesg.append(" : Height: " + m_ImageHeight);
		mesg.append(" : Is Background Image: " + m_IsBackgroundImage);
		mesg.append(" : Bits Per Component: " + m_BitsPerComponent);
		mesg.append(" : Color Component: " + m_ColorComponents);
		mesg.append(" : Rendiring Intent: " + m_RenderingIntent);
		mesg.append(" : ImageAlphaValue: " + m_ImageAlphaValue);
		mesg.append(" : ThemeId: " + m_ThemeId);
		mesg.append(" : Allow Image Upload with Theme: "
				+ m_AllowUploadWithTheme);
		if (m_ImageMask != null)
			mesg.append(" : Mask Width " + m_ImageMask.getWidth(null)
					+ " Height: " + m_ImageMask.getHeight(null));
		mesg.append(" : ClipSegmentCount: " + m_ClipSegmentCount);
		if (m_ClipSegmentCount == 0)
			return mesg.toString();

		mesg.append("\n-----PRINT PATHS------\n");

		for (int i = 0; i < m_ClipPath.size(); i++) {
			ClipPath clipPath = (ClipPath) m_ClipPath.elementAt(i);
			if (clipPath == null)
				continue;
			clipPath.toString(mesg);
		}
		return mesg.toString();
	}

	// Serialization
	public void save(ObjectOutputStream objstream) throws IOException {
		objstream.writeObject(this);
		super.save(objstream);
	}

	// De-Serialization.
	public Object load(ObjectInputStream objstream) throws Exception {
		super.load(objstream);
		return this;
	}
}
