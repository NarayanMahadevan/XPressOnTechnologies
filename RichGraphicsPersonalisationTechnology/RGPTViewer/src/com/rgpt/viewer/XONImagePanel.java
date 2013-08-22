// RGPT PACKAGES
package com.rgpt.viewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.rgpt.imageutil.ImageHolder;
import com.rgpt.imageutil.ImageTransformHolder;
import com.rgpt.imageutil.ImageUtils;
import com.rgpt.util.ImagePointHolder;
import com.rgpt.util.RGPTParams;
import com.rgpt.util.CursorController;
import com.rgpt.util.FileFilterFactory;
import com.rgpt.util.LocalizationUtil;
import com.rgpt.util.RGPTActionListener;
import com.rgpt.util.RGPTFileFilter;
import com.rgpt.util.RGPTListCellRenderer;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTShapeUtil;
import com.rgpt.util.RGPTUIUtil;
import com.rgpt.util.RGPTUtil;
import com.rgpt.util.ScalingFactor;
import com.rgpt.util.SelectedPixelPointHolder;
import com.rgpt.viewer.XONImageMaker;

public class XONImagePanel extends JPanel implements MouseListener,
		MouseMotionListener {
	ScalingFactor m_ScalingFactor;
	public XONImageMaker m_XONImageMakerApp;

	// Image Opened by the User
	public ImageHolder m_XONImageHolder;

	// Stores the Action Performed by the User
	public String m_ActionPerformedId = "";

	// Main WF Id which corresponds to Image Action
	public String m_WorkflowProcessId = null;

	// Stores the Path Selection selected by the User
	public String m_PathSelectionId = null;

	// Stores the Process selected by the User
	public String m_ProcessSelectionId = null;

	// Stores the Preview Action selected by the User
	public String m_PreviewSelectionId = null;

	// Graphic Path Points clicked for drawing shape or cutting image
	public Vector<Point2D.Double> m_GPathPoints;

	// Affine Transform to take care of Scaling, Rotation and Transformation
	public AffineTransform m_ShapeAffine;

	// This variable define the General Path of the shape
	public GeneralPath m_GPath;

	// This point is used by Change and Patch Pixels Actions. The selected point
	// is used for
	// Image Transformation
	public Point2D.Double m_SelectedPixelPoint = null;
	public Vector<SelectedPixelPointHolder> m_SelectedPixelPointList = null;

	// This Vector maointain s the Pixels that are transformed in screen and
	// Image coords
	Vector<Integer> m_TransformationPixel = null;
	// This maintains the Additional Pixals that needs to be added or removed as
	// specified
	// by the user from the original transformation pixels. This are maintained
	// in Image coords.
	Vector<ImagePointHolder> m_AddTransformationPixel = null;
	Vector<ImagePointHolder> m_RemTransformationPixel = null;

	// This size is set by the user initally defaults are set in the Prop File
	int m_BrushSize = 0;
	int m_SelectionImageSize = 0;
	int m_SetColorRange = 0;

	// This maintains Action to the coresponding Dialog Box
	public Map<String, JDialog> m_ActionDialogBox = null;

	// This maintains Action to the coresponding Component in the Dialog Box
	public Map<String, Map> m_DialogActionComponent = null;

	// This image is created and populated when Double Buffering is on.
	public BufferedImage m_BuffDispImage = null;
	public Rectangle m_BuffDispImageRect = null;
	public boolean m_ActivateDoubleBuffering = false;
	public BufferedImage m_ZoomPreviewImage = null;

	// This variables are defined for dragging the graphic path points
	private final static int NOT_DRAGGING = -1;
	private final static int NEIGHBORHOOD = 15;
	private int m_DragIndex = NOT_DRAGGING;

	// This variable notifies if the Image is Zoomed
	public boolean m_ImageZoomed = false;

	public XONImagePanel(XONImageMaker xonImgMakerApp) {
		super();
		m_ImageZoomed = false;
		m_ActionPerformedId = "";
		m_GPathPoints = new Vector<Point2D.Double>();
		m_DialogActionComponent = new HashMap<String, Map>();
		m_ActionDialogBox = new HashMap<String, JDialog>();
		m_ScalingFactor = ScalingFactor.FIT_PAGE;
		m_XONImageMakerApp = xonImgMakerApp;
		addMouseListener(this);
		addMouseMotionListener(this);
		m_ShapeAffine = new AffineTransform();
		m_ShapeAffine.setToIdentity();
	}

	public void setXONImage(String action, ImageHolder imgHolder) {
		m_ActionPerformedId = action;
		m_XONImageHolder = imgHolder;
	}

	// START FUNCTION FOR ACTION LISTENERS

	public void setWorkFlowProcessId(String action) {
		m_WorkflowProcessId = action;
	}

	public void setZoom(int zoomLevel) {
		resetZoom();
		m_ScalingFactor.setZoom(zoomLevel);
		revalidate();
		repaint();
	}

	public void zoomOut() {
		resetZoom();
		m_ScalingFactor.zoomOut((int) (m_XONImageHolder.m_Scale * 100));
		revalidate();
		repaint();
	}

	public void zoomIn() {
		resetZoom();
		m_ScalingFactor.zoomIn((int) (m_XONImageHolder.m_Scale * 100));
		revalidate();
		repaint();
	}

	public void resetZoom() {
		m_ImageZoomed = true;
		m_BuffDispImage = null;
		m_TransformationPixel = new Vector<Integer>();
		m_ScalingFactor = ScalingFactor.ZOOM_IN_OUT;
	}

	// This method is common for both Image Cutout and Shape Cutout along the
	// Path
	public void cutoutPath(String action) {
		m_ActionPerformedId = action;
		// Storing the Path Selection Id which can be used for redraw
		m_PathSelectionId = action;
		m_ActivateDoubleBuffering = true;
		repaint();
	}

	public void drawGPath() {
		m_ActionPerformedId = m_PathSelectionId;
		repaint();
	}

	public void adjustGPath(String action) {
		m_ActionPerformedId = action;
		repaint();
	}

	public void previewPath(String action) {
		if (m_GPath == null) {
			RGPTUIUtil.showErrorMesg(this, null, "ERROR_NO_PATH");
			return;
		}
		m_ActionPerformedId = action;
		repaint();
	}

	public void savePath(String action) {
		if (m_GPath == null) {
			RGPTUIUtil.showErrorMesg(this, null, "ERROR_NO_PATH");
			return;
		}
		m_ActionPerformedId = action;
		saveShapeDialogBox(action);
	}

	public void saveImage(RGPTActionListener.WFActions action) {
		String wfId = "", procId = "", pathId = "";
		String origFileName = "", append2FileName = "", imgFilePath = "";
		ImageTransformHolder imtTransHldr = null;
		BufferedImage origTransImg = null, thumbImg = null;
		if (m_PreviewSelectionId == null) {
			RGPTUIUtil.showErrorMesg(this, null, "ERROR_NO_IMAGE");
			return;
		}
		m_ActionPerformedId = m_PathSelectionId;
		switch (action) {
		case SAVE_IMAGE_CUTOUT:
			repaint();
			break;
		case SAVE_IMAGE_TO_FILE:
			if (m_GPath == null) {
				RGPTUIUtil.showErrorMesg(this, null, "ERROR_NO_PATH");
				return;
			}
			origFileName = RGPTFileFilter
					.getFileName(m_XONImageHolder.m_FileName);
			imgFilePath = RGPTUIUtil.chooseFilePath(this, ".png",
					append2FileName, FileFilterFactory.IMAGE_FILE_FILTER,
					origFileName);
			RGPTLogger.logToFile("Serialized File Path: " + imgFilePath);
			try {
				// Its assumed that the Image Cutout will always have alpha
				// value. Hence saved as PNG
				origTransImg = ImageTransformHolder.getTransformedImage(
						m_XONImageHolder, false);
				BufferedImage cutoutImg = createCutoutImage(origTransImg);
				ImageUtils.SaveImageToFile(cutoutImg, imgFilePath, "png");
			} catch (Exception ex) {
				RGPTLogger.logToFile("Exception at Save Image", ex);
				RGPTUIUtil.showErrorMesg(this, null, "ERROR_FILE_NOT_SAVED");
				return;
			}
			RGPTUIUtil.showInfoMesg(this, null, "SUCCESS_IMAGE_SAVED");
			m_XONImageMakerApp.closeImageFile();
			break;
		case SAVE_CHANGE_IMAGE_TO_FILE:
			origFileName = RGPTFileFilter
					.getFileName(m_XONImageHolder.m_FileName);
			imgFilePath = RGPTUIUtil.chooseFilePath(this, ".png",
					append2FileName, FileFilterFactory.IMAGE_FILE_FILTER,
					origFileName);
			RGPTLogger.logToFile("Serialized File Path: " + imgFilePath);
			try {
				BufferedImage transImg = null;
				origTransImg = ImageTransformHolder.getTransformedImage(
						m_XONImageHolder, false);
				RGPTLogger.logToFile("Regular Image: "
						+ m_XONImageHolder.m_RegularImage
						+ "\nTransformed Img: " + origTransImg);
				transImg = showTransformedImage(origTransImg, null, null, null,
						false);
				ImageUtils.SaveImageToFile(transImg, imgFilePath, "png");
			} catch (Exception ex) {
				RGPTLogger.logToFile("Exception at Save Image", ex);
				RGPTUIUtil.showErrorMesg(this, null, "ERROR_FILE_NOT_SAVED");
				return;
			}
			RGPTUIUtil.showInfoMesg(this, null, "SUCCESS_IMAGE_SAVED");
			m_XONImageMakerApp.closeImageFile();
			break;
		case SAVE_IMAGE_IN_MEM:
			m_XONImageHolder.m_RegularImage = createCutoutImage(m_XONImageHolder.m_RegularImage);
			m_XONImageHolder.m_RegularImage = ImageUtils.fillTransparentPixels(
					m_XONImageHolder.m_RegularImage,
					m_XONImageHolder.m_BackgroundColor);
			thumbImg = RGPTUIUtil
					.createThumbPreviewImage(m_XONImageHolder.m_RegularImage);
			wfId = m_WorkflowProcessId;
			pathId = m_PathSelectionId;
			imtTransHldr = ImageTransformHolder.createCutoutImageTransform(
					thumbImg, wfId, pathId, m_GPathPoints,
					m_XONImageHolder.m_ScreenToImageCTM);
			m_XONImageHolder.m_ImageTransformHolder.addElement(imtTransHldr);
			m_XONImageMakerApp.resetImageMaker();
			break;
		case SAVE_CHANGE_IMAGE_IN_MEM:
			showTransformedImage(m_XONImageHolder.m_RegularImage, null, null,
					null, false);
			thumbImg = RGPTUIUtil
					.createThumbPreviewImage(m_XONImageHolder.m_RegularImage);
			wfId = m_WorkflowProcessId;
			procId = m_ProcessSelectionId;
			imtTransHldr = ImageTransformHolder.createModifyImageTransform(
					thumbImg, wfId, procId, m_SelectedPixelPointList,
					m_AddTransformationPixel, m_RemTransformationPixel);
			m_XONImageHolder.m_ImageTransformHolder.addElement(imtTransHldr);
			m_XONImageMakerApp.resetImageMaker();
			break;
		}
	}

	public void processDialogAction(RGPTActionListener.DialogActions action) {
		JDialog actionDialogBox = m_ActionDialogBox.get(m_ActionPerformedId);
		if (actionDialogBox == null)
			return;
		Map<String, JComponent> actionComp = m_DialogActionComponent
				.get(m_ActionPerformedId);
		if (actionComp == null)
			return;
		// Local UI Comps
		JTextField txtFldBox = null;
		switch (action) {
		case SET_COLOR_RANGE_OK:
			txtFldBox = (JTextField) actionComp.get("SET_COLOR_RANGE_FIELD");
			m_SetColorRange = (Integer.valueOf(txtFldBox.getText())).intValue();
			RGPTLogger.logToFile("Color Range Set to: " + m_SetColorRange);
			actionDialogBox.setVisible(false);
			break;
		case SET_COLOR_RANGE_CANCEL:
			actionDialogBox.setVisible(false);
			break;
		case SET_BRUSH_SIZE_OK:
			txtFldBox = (JTextField) actionComp.get("SET_BRUSH_SIZE_FIELD");
			m_BrushSize = (Integer.valueOf(txtFldBox.getText())).intValue();
			RGPTLogger.logToFile("Color Range Set to: " + m_BrushSize);
			actionDialogBox.setVisible(false);
			break;
		case SET_BRUSH_SIZE_CANCEL:
			actionDialogBox.setVisible(false);
			break;
		}
	}

	public void processSaveDialog(RGPTActionListener.DialogActions action) {
		Map<String, JComponent> actionComp = null;
		JComboBox comboBox = null;
		JTextField typeFldBox = null, nameFldBox = null;
		LocalizationUtil lu = new LocalizationUtil();
		String newShapeType = lu.getText("NewShapeType");
		String shNameEntry = lu.getText("ShapeName");
		String saveShapeAction = RGPTActionListener.WFActions.SAVE_SHAPE
				.toString();
		actionComp = m_DialogActionComponent.get(saveShapeAction);
		comboBox = (JComboBox) actionComp.get("SHAPE_TYPE_COMBO_ACTION");
		typeFldBox = (JTextField) actionComp.get("SHAPE_TYPE_FIELD_ACTION");
		nameFldBox = (JTextField) actionComp.get("SHAPE_NAME_FIELD_ACTION");
		String selItem = (String) comboBox.getSelectedItem();
		String shTypeTxt = typeFldBox.getText(), shNameTxt = nameFldBox
				.getText();
		// Settings the UI Components based on Combobox Value
		if (!selItem.equals(newShapeType)) {
			typeFldBox.setEditable(false);
			typeFldBox.setText("");
			nameFldBox.setEditable(true);
		} else {
			nameFldBox.setEditable(false);
			typeFldBox.setEditable(true);
			if (!shTypeTxt.equals(newShapeType))
				nameFldBox.setEditable(true);
		}
		if (action == null)
			return;
		switch (action) {
		case SHAPE_TYPE_COMBO_ACTION:
			break;
		case SHAPE_TYPE_FIELD_ACTION:
			if (!selItem.equals(newShapeType))
				return;
			if (shTypeTxt.equals(newShapeType)) {
				typeFldBox.setText("");
				nameFldBox.setEditable(false);
			} else {
				nameFldBox.setEditable(true);
			}
			break;
		case SHAPE_NAME_FIELD_ACTION:
			if (shTypeTxt.equals(newShapeType))
				return;
			if (shNameTxt.equals(shNameEntry))
				nameFldBox.setText("");
			break;
		case SAVE_SHAPE_OK_ACTION:
			if ((selItem.equals(newShapeType) && shTypeTxt.equals(newShapeType))
					|| (shNameTxt.equals(shNameEntry))) {
				RGPTUIUtil.showErrorMesg(this, null, "ERROR_NO_SHAPE_ENTRY");
				return;
			}
			if (saveShape(selItem, shTypeTxt, shNameTxt)) {
				m_DialogActionComponent.remove(saveShapeAction);
				m_SaveShapeDialog.setVisible(false);
				m_SaveShapeDialog = null;
				RGPTUIUtil.showInfoMesg(this, null, "SUCCESS_SHAPE_SAVED");
				m_XONImageMakerApp.closeImageFile();
			}
			break;
		case SAVE_SHAPE_CANCEL_ACTION:
			m_SaveShapeDialog.setVisible(false);
			drawGPath();
			break;
		}
	}

	public void selectImageLoc(String action) {
		setActionPerformed(action, false);
		m_PathSelectionId = action;
		m_GPathPoints.clear();
		m_GPath = null;
	}

	public void activateDialogBox(String action) {
		setActionPerformed(action, true);
		createDialogBox(action);
	}

	// Helper Method and calls setActionPerformed with appropriate params
	public void resetProcessSel(boolean repaintPanel) {
		resetProcessSel();
		setActionPerformed(null, false, true, repaintPanel);
		// boolean isApproved = RGPTUIUtil.getUserApproval(this,
		// "RESET_PROCESS_SELECTION_INFO");
		// if (isApproved) {
		// resetProcessSel();
		// setActionPerformed(null, false, true, repaintPanel);
		// }
	}

	public void setProcessSel(String action, boolean repaintPanel) {
		// RGPTUIUtil.showInfoMesg(this, null, action+"_INFO");
		resetProcessSel();
		m_ActivateDoubleBuffering = true;
		m_TransformationPixel = new Vector<Integer>();
		m_AddTransformationPixel = new Vector<ImagePointHolder>();
		m_RemTransformationPixel = new Vector<ImagePointHolder>();
		m_SelectedPixelPointList = new Vector<SelectedPixelPointHolder>();
		setActionPerformed(action, true, false, repaintPanel);
	}

	public void resetProcessSel() {
		// m_BuffDispImage = null;
		// m_SelectedPixelPoint = null;
	}

	// Here action indicates User Action,
	// isProcessSel indicates if the current Action is a New Process within the
	// WF
	// setProcessSel indicates reset the Action Performed to Process Selection
	// Id
	// repaintPanel if set to true repaints this Image Panel
	public void setActionPerformed(String action, boolean repaintPanel) {
		setActionPerformed(action, false, false, repaintPanel);
	}

	public void setActionPerformed(String action, boolean isProcessSel,
			boolean setProcessSel, boolean repaintPanel) {
		if (setProcessSel)
			m_ActionPerformedId = m_ProcessSelectionId;
		else
			m_ActionPerformedId = action;
		if (isProcessSel)
			m_ProcessSelectionId = m_ActionPerformedId;
		// RGPTLogger.logToFile("Checking Action is Valid: "+m_ActionPerformedId+" processSel: "+
		// m_ProcessSelectionId+" WFProcess: "+m_WorkflowProcessId);
		if (repaintPanel)
			repaint();
	}

	// END FUNCTION FOR ACTION LISTENERS

	// START FUNCTION FOR MOUSE ACTIONS
	boolean m_ActivatePixelTransformation = false;
	boolean m_ActivatePatchPixel = false;
	boolean m_PatchPixelSet = false;

	public void mousePressed(MouseEvent e) {
		if (!isMousePointValid(e))
			return;
		if (isDrawGPathAction() && m_ActionPerformedId == m_PathSelectionId) {
			if (m_GPathPoints.size() == 0) {
				Point2D.Double pt2D = new Point2D.Double(e.getX(), e.getY());
				m_GPathPoints.add(pt2D);
				repaint();
			}
			// This allows Dragging on Draw Path Action also
			else if (isGPathDragEnabled())
				findDraggingPt(e);
		} else if (isGPathDragEnabled()) {
			findDraggingPt(e);
		} else if (isValidAction(RGPTActionListener.MODIFY_PIXEL_ACTIONS)) {
			m_ActivatePixelTransformation = true;
			m_XONImageMakerApp.setCursor(CursorController
					.createRoundCursor(m_BrushSize));
			RGPTLogger.logToFile("Activated Pixel Trans: "
					+ m_ActivatePixelTransformation);
		} else if (isValidAction(RGPTActionListener.PATCH_PIXEL_ACTIONS)) {
			m_ActivatePatchPixel = true;
			RGPTLogger.logToFile("Activated Pixel Trans: "
					+ m_ActivatePatchPixel);
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (isGPathDragEnabled())
			resetMouseDrag();
		else if (isValidAction(RGPTActionListener.MODIFY_PIXEL_ACTIONS)) {
			m_ActivatePixelTransformation = false;
			m_XONImageMakerApp.resetCursor();
		} else if (isValidAction(RGPTActionListener.PATCH_PIXEL_ACTIONS)) {
			m_ActivatePatchPixel = false;
			m_XONImageMakerApp.resetCursor();
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (!isMousePointValid(e)) {
			RGPTLogger.logToFile("Mouse Click is In Valid, Clicked at: "
					+ e.getPoint());
			RGPTLogger.logToFile("BuffDispImage: " + m_BuffDispImage);
			return;
		}
		if (isDrawGPathAction() && m_ActionPerformedId == m_PathSelectionId) {
			Point2D.Double pt2D = new Point2D.Double(e.getX(), e.getY());
			m_GPathPoints.add(pt2D);
			repaint();
		} else if (isValidAction(RGPTActionListener.POINT_SELECTION_ACTIONS)) {
			// RGPTLogger.logToFile("Selection is Valid, Clicked at: "+e.getPoint());
			// This part of the code is executed only when the current Workflow
			// and ActionPerformed actions are
			// for selecting point for image transformation
			Point2D.Double pt2D = new Point2D.Double(e.getX(), e.getY());
			Color selColor = new Color(m_BuffDispImage.getRGB(e.getX(),
					e.getY()));
			BufferedImage infoImg = ImageUtils.createColorImage(selColor,
					"InfoMesgImageSize");
			// ImageUtils.displayImage(infoImg, "Info Image");
			int retVal = RGPTUIUtil.showConfirmMesg(this, infoImg,
					"SEL_IMAGE_PIXEL_COLOR_SHOW_INFO");
			if (retVal == JOptionPane.YES_OPTION) {
				int imgWt = m_BuffDispImage.getWidth();
				if (!ImageUtils.containsPixalPoint(imgWt,
						m_TransformationPixel, e.getX(), e.getY())) {
					m_SelectedPixelPoint = (Point2D.Double) m_XONImageHolder.m_ScreenToImageCTM
							.transform(pt2D, null);
					RGPTLogger.logToFile("Point Selected for: "
							+ m_ActionPerformedId + " is: "
							+ m_SelectedPixelPoint);
					repaint();
				}
			}
		} else {
			RGPTLogger.logToFile("No Action performed, Mouse Clicked at: "
					+ e.getPoint());
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		if (!isMousePointValid(e))
			return;
		showZoomPreview(e.getPoint());
		if (isGPathDragEnabled() && m_DragIndex != NOT_DRAGGING)
			setDraggingPt(e);
		else if (isDrawGPathAction()
				&& m_ActionPerformedId == m_PathSelectionId) {
			if (m_GPathPoints.size() > 0) {
				Point2D.Double pt2D = new Point2D.Double(e.getX(), e.getY());
				Point2D.Double pathPt2D = m_GPathPoints.lastElement();
				if (RGPTUtil.getDistance(pathPt2D, pt2D) > 25.0D) {
					// int pixelPathPt = m_BuffDispImage.getRGB((int)pathPt2D.x,
					// (int)pathPt2D.y);
					// int pixelMousePt = m_BuffDispImage.getRGB((int)pt2D.x,
					// (int)pt2D.y);
					// if (ImageUtils.findColorRange(pixelPathPt, pixelMousePt)
					// <= 100)
					m_GPathPoints.add(pt2D);
					repaint();
				}
			}
		} // if (isDrawGPathAction() || m_ActionPerformedId ==
			// m_PathSelectionId)
		else if (m_ActivatePixelTransformation) {
			// RGPTLogger.logToFile("Resetting the Transform Pixel");
			boolean setNewPixels = false, resetPixels = false;
			if (RGPTUtil.isWFActionEqual(m_ActionPerformedId,
					"ADD_IMAGE_PIXEL_COLOR"))
				setNewPixels = true;
			resetPixels = ImageUtils.resetTranformPixel(m_BuffDispImage,
					e.getX(), e.getY(), m_TransformationPixel, setNewPixels,
					m_BrushSize);
			if (resetPixels) {
				// First get the Transformation Pixel to Image Coords before any
				// resetting
				Point2D.Double pt2D = new Point2D.Double(e.getX(), e.getY());
				ImagePointHolder ptHolder = null;
				double scale = m_XONImageHolder.m_Scale;
				ptHolder = ImagePointHolder.createImagePointHolder(pt2D,
						m_BrushSize, m_XONImageHolder.m_ScreenToImageCTM);
				// RGPTLogger.logToFile("Image Pixel PtHldr : "+ptHolder);
				if (setNewPixels)
					m_AddTransformationPixel.addElement(ptHolder);
				else
					m_RemTransformationPixel.addElement(ptHolder);
				repaint();
			}
		}
	}

	public void mouseMoved(MouseEvent e) {
		if (m_ActionPerformedId == null)
			return;
		if (!isMousePointValid(e))
			return;
		showZoomPreview(e.getPoint());
	}

	private boolean isMousePointValid(MouseEvent e) {
		Point pt = e.getPoint();
		// First check is the Point contained in the Panel
		if (m_XONImageHolder == null
				|| m_XONImageHolder.m_ScreenImageRect == null)
			return false;
		Rectangle imgRect = m_XONImageHolder.m_ScreenImageRect;
		if (!imgRect.contains(pt) || m_BuffDispImage == null
				|| !m_BuffDispImageRect.contains(pt))
			return false;
		return true;
	}

	// END FUNCTION FOR MOUSE ACTIONS

	// START FUNCTION FOR LOGIC PROCESSING

	public void resetXONImage() {
		m_XONImageHolder = null;
		resetActionPerformed(null);
	}

	// This verifies if the user action pertains to current WF. If true then no
	// reset is done, else
	// complete reset and clean up of Image Panel Memory is done
	public boolean resetActionPerformed(String newActionId) {
		String[] wfProcessIds = null;
		if (newActionId != null) {
			if (newActionId
					.equals(RGPTActionListener.ImageActions.INCREASE_ZOOM
							.toString())
					|| newActionId
							.equals(RGPTActionListener.ImageActions.DECREASE_ZOOM
									.toString()))
				return false;
		}
		m_GPath = null;
		m_GPathPoints.clear();
		m_ImageZoomed = false;
		m_BuffDispImage = null;
		m_PathSelectionId = null;
		m_ZoomPreviewImage = null;
		m_PreviewSelectionId = null;
		m_WorkflowProcessId = null;
		m_ActionPerformedId = null;
		m_SelectedPixelPoint = null;
		m_TransformationPixel = null;
		m_AddTransformationPixel = null;
		m_RemTransformationPixel = null;
		m_SelectedPixelPointList = null;
		m_ProcessSelectionId = null;
		m_ActivateDoubleBuffering = false;
		m_DragIndex = NOT_DRAGGING;
		m_ShapeAffine.setToIdentity();
		m_ScalingFactor = ScalingFactor.FIT_PAGE;
		m_XONImageMakerApp.m_ZoomViewPane.repaint();
		m_BrushSize = RGPTParams.getIntVal("SET_BRUSH_SIZE_FIELD_VALUE");
		m_SetColorRange = RGPTParams
				.getIntVal("SET_COLOR_RANGE_FIELD_VALUE");
		return true;
	}

	// Once the Mouse is Released the Draging index to adjust the GPath is reset
	private void resetMouseDrag() {
		m_DragIndex = NOT_DRAGGING;
	}

	// Adjustment of Graphic Path Points is allowed only when isDrawGPathAction
	// is
	// enabled and the current step in the WF is ADJUST_GRAPHIC_PATH_STEP,
	// ADJUST_SHAPE_PATH_STEP
	public boolean isGPathDragEnabled() {
		if (isDrawGPathAction()) {
			// If Commented Drag is allowed even if the current wf step is not
			// adjust path
			// if (m_ActionPerformedId == RGPTActionListener.WFActions.
			// ADJUST_GRAPHIC_PATH_STEP.toString())
			return true;
		}
		return false;
	}

	// This method checks if the current Workflow and ActionPerformed actions
	// are
	// valid actions for image transformation. They are valid if not null and
	// contained
	// in the supplied action ids.
	public boolean isValidAction(String[] actionIds) {
		// RGPTLogger.logToFile("Checking Action is Valid: "+m_ActionPerformedId+" processSel: "+
		// m_ProcessSelectionId+" WFProcess: "+m_WorkflowProcessId);
		if (m_WorkflowProcessId == null || m_ProcessSelectionId == null
				|| m_ActionPerformedId == null)
			return false;
		Arrays.sort(actionIds);
		int index = Arrays.binarySearch(actionIds,
				m_ActionPerformedId.toString());
		if (index < 0) {
			// RGPTLogger.logToFile("Action is inValid: "+m_ActionPerformedId);
			return false;
		}
		return true;
	}

	// This method checks if the current Workflow and ActionPerformed actions
	// are
	// for drawing the Graphic Path
	public boolean isDrawGPathAction() {
		if (m_WorkflowProcessId == null || m_PathSelectionId == null
				|| m_ActionPerformedId == null)
			return false;
		String[] gPathIds = RGPTActionListener.DRAW_GRAPHIC_PATH_IDS;
		Arrays.sort(gPathIds);
		int index = Arrays.binarySearch(gPathIds, m_PathSelectionId.toString());
		if (index < 0)
			return false;
		return true;
	}

	private void findDraggingPt(MouseEvent e) {
		m_DragIndex = NOT_DRAGGING;
		int minDistance = Integer.MAX_VALUE;
		int indexOfClosestPoint = -1;
		Point2D.Double[] points = m_GPathPoints.toArray(new Point2D.Double[0]);
		for (int i = 0; i < points.length; i++) {
			double deltaX = points[i].x - e.getX();
			double deltaY = points[i].y - e.getY();
			int distance = (int) (Math.sqrt(deltaX * deltaX + deltaY * deltaY));
			if (distance < minDistance) {
				minDistance = distance;
				indexOfClosestPoint = i;
			}
		}
		if (minDistance > NEIGHBORHOOD)
			return;
		m_DragIndex = indexOfClosestPoint;
	}

	private boolean setDraggingPt(MouseEvent e) {
		if (m_DragIndex == NOT_DRAGGING)
			return false;
		Point2D.Double dragPt = m_GPathPoints.elementAt(m_DragIndex);
		dragPt.x = e.getX();
		dragPt.y = e.getY();
		repaint();
		return true;
	}

	// Cuts the portion of the image from the screen and zooms it to show in the
	// Preview
	public void showZoomPreview(Point pt) {
		int margin = RGPTParams.getIntVal("ZoomPreviewMargin");
		Rectangle zommRect = RGPTUtil.getRectangle(pt, margin);
		if (!m_BuffDispImageRect.contains(zommRect))
			return;
		BufferedImage prevImg = m_BuffDispImage.getSubimage(zommRect.x,
				zommRect.y, zommRect.width, zommRect.height);
		m_ZoomPreviewImage = ImageUtils.brightenImage(prevImg);
		m_XONImageMakerApp.m_ZoomViewPane.repaint();
	}

	private void createDialogBox(String action) {
		JDialog actionDialogBox = m_ActionDialogBox.get(action);
		if (actionDialogBox != null) {
			actionDialogBox.setVisible(true);
			return;
		}

		Map<String, JComponent> actionComp = null;
		LocalizationUtil lu = new LocalizationUtil();

		String basePropName = action + "_DIALOG";
		JPanel contentPane = new JPanel();
		Dimension size = RGPTUtil.getCompSize((basePropName + "_SIZE"));
		actionComp = RGPTUIUtil.createDialogContent(contentPane, basePropName,
				m_XONImageMakerApp);
		m_DialogActionComponent.put(action, actionComp);

		String title = lu.getText(basePropName + "_TITLE");
		int locx = (int) (m_BuffDispImageRect.width / 2)
				- (int) (size.width / 2);
		int locy = (int) (m_BuffDispImageRect.height / 2)
				- (int) (size.height / 2);
		actionDialogBox = RGPTUIUtil.createDialogBox(null, title, true,
				contentPane, size.width, size.height, locx, locy, true);
		m_ActionDialogBox.put(action, actionDialogBox);
		setActionDialog(action);
		actionDialogBox.setVisible(true);
	}

	private void setActionDialog(String action) {
		JDialog actionDialogBox = m_ActionDialogBox.get(action);
		if (actionDialogBox == null)
			return;
		Map<String, JComponent> actionComp = m_DialogActionComponent
				.get(action);
		if (actionComp == null)
			return;

		// Local UI Comps
		JTextField txtFldBox = null;
		LocalizationUtil lu = new LocalizationUtil();
		RGPTActionListener.WFActions wfAction = null;
		wfAction = RGPTActionListener.WFActions.valueOf(action);
		switch (wfAction) {
		case SET_COLOR_RANGE:
			txtFldBox = (JTextField) actionComp.get("SET_COLOR_RANGE_FIELD");
			txtFldBox.setColumns(4);
			txtFldBox.setHorizontalAlignment(JTextField.CENTER);
			txtFldBox.setText(Integer.toString(m_SetColorRange));
			break;
		case SET_BRUSH_SIZE:
			txtFldBox = (JTextField) actionComp.get("SET_BRUSH_SIZE_FIELD");
			txtFldBox.setColumns(4);
			txtFldBox.setHorizontalAlignment(JTextField.CENTER);
			txtFldBox.setText(Integer.toString(m_BrushSize));
			break;
		}
	}

	public JDialog m_SaveShapeDialog = null;

	private void saveShapeDialogBox(String action) {
		if (m_SaveShapeDialog != null) {
			m_SaveShapeDialog.setVisible(true);
			return;
		}
		Map<String, JComponent> actionComp = null;
		LocalizationUtil lu = new LocalizationUtil();
		int wt = RGPTParams.getIntVal("SaveShapeUIWidth");
		int ht = RGPTParams.getIntVal("SaveShapeUIHeight");
		// RGPTUIUtil.setCompSize(contentPane, wt, ht, 1);
		String basePropName = "SAVE_SHAPE_DIALOG";
		JPanel contentPane = new JPanel();
		actionComp = RGPTUIUtil.createDialogContent(contentPane, basePropName,
				m_XONImageMakerApp);
		m_DialogActionComponent.put(action, actionComp);

		// Setting the Renderrer and Data Model for Shape Type Combo Box
		DefaultComboBoxModel model = null;
		String selItem = "", newShapeTpe = lu.getText("NewShapeType");
		String shapeOutDir = RGPTUtil.getShapesDirectory(RGPTParams
				.getVal("UserType"));
		File shapeDir = new File(shapeOutDir);
		String[] files = shapeDir.list();
		RGPTListCellRenderer cellRen = new RGPTListCellRenderer(files, wt, ht);
		JComboBox comboBox = (JComboBox) actionComp
				.get("SHAPE_TYPE_COMBO_ACTION");
		comboBox.setRenderer(cellRen);
		if (files != null && files.length > 0) {
			model = new DefaultComboBoxModel(files);
			selItem = files[0];
		} else
			model = new DefaultComboBoxModel();
		model.insertElementAt(newShapeTpe, 0);
		comboBox.setModel(model);
		if (selItem.length() == 0)
			selItem = newShapeTpe;
		comboBox.setSelectedItem(selItem);
		JTextField nameFldBox = (JTextField) actionComp
				.get("SHAPE_NAME_FIELD_ACTION");
		nameFldBox.setText(RGPTFileFilter
				.getFileName(m_XONImageHolder.m_FileName));
		// Presetting the Dialog Box
		processSaveDialog(null);

		String title = lu.getText("SaveShapeTitle");
		int locx = (int) (m_BuffDispImageRect.width / 2) - (int) (wt / 2);
		int locy = (int) (m_BuffDispImageRect.height / 2) - (int) (ht / 2);
		m_SaveShapeDialog = RGPTUIUtil.createDialogBox(null, title, true,
				contentPane, wt, ht, locx, locy, true);
		m_SaveShapeDialog.setVisible(true);
	}

	private boolean saveShape(String selShapeType, String newShapeType,
			String shapeName) {
		boolean result = true;
		LocalizationUtil lu = new LocalizationUtil();
		String shapeTypeEntry = lu.getText("NewShapeType");
		String shapeOutDir = RGPTUtil.getShapesDirectory(RGPTParams
				.getVal("UserType"));
		if (selShapeType.equals(shapeTypeEntry)) {
			shapeOutDir = shapeOutDir + newShapeType;
			RGPTUtil.createDir(shapeOutDir, true);
		} else
			shapeOutDir = shapeOutDir + "/" + selShapeType;
		File shapeDir = new File(shapeOutDir);
		String[] files = shapeDir.list();
		Arrays.sort(files);
		int index = Arrays.binarySearch(files, shapeName + ".xsh");
		if (index >= 0) {
			boolean isApproved = RGPTUIUtil.getUserApproval(this,
					"ERROR_SHAPE_FILE_EXISTS");
			if (!isApproved)
				return false;
		}
		String serFilePath = shapeOutDir + "/" + shapeName + ".xsh";
		RGPTLogger.logToFile("Serialized File Path: " + serFilePath);
		// Saving the Shape to a Serialized File
		try {
			Vector<Point2D.Double> gPathPoints = saveGPath();
			Map<String, Vector> shapeInfo = new HashMap<String, Vector>();
			shapeInfo.put(m_PathSelectionId, gPathPoints);
			RGPTUtil.serializeObject(serFilePath, shapeInfo);
			result = true;
		} catch (Exception ex) {
			result = false;
			RGPTLogger.logToFile("Exception at Save Shape", ex);
		}
		return result;
	}

	private Vector<Point2D.Double> saveGPath() {
		Vector<Point2D.Double> gPathPoints = null;
		GeneralPath gPath = null;
		Point2D.Double[] boundPts;
		Rectangle pathBounds = m_GPath.getBounds();

		// Cloning to maintain the reference of the original object
		gPathPoints = (Vector<Point2D.Double>) m_GPathPoints.clone();
		AffineTransform affine = (AffineTransform) m_ShapeAffine.clone();

		// Scaling the Shape down to new Bounds before saving
		Rectangle newBounds = new Rectangle(100, 100);
		double sx = (double) newBounds.width / (double) pathBounds.width;
		double sy = 1.0;
		if (pathBounds.height > 0)
			sy = (double) newBounds.height / (double) pathBounds.height;

		affine = RGPTUtil.getAffineTransform(affine, pathBounds, sx, sy, 0);
		sx = affine.getScaleX();
		sy = affine.getScaleY();
		RGPTLogger.logToFile("New Affine: " + affine + " sx: " + sx + " sy: "
				+ sy);

		// Drawing the GPath to get the
		// The functions in Shape Util draw the shape using the Graphics and
		// Path Points
		if (m_PathSelectionId == RGPTActionListener.ImageActions.SHAPE_LINE_PATH_MENU
				.toString())
			gPath = RGPTShapeUtil.drawLine(affine, gPathPoints, null);
		else if (m_PathSelectionId == RGPTActionListener.ImageActions.SHAPE_QUAD_PATH_MENU
				.toString())
			gPath = RGPTShapeUtil.drawQuadCurve(affine, gPathPoints, null);
		else if (m_PathSelectionId == RGPTActionListener.ImageActions.SHAPE_CUBIC_PATH_MENU
				.toString())
			gPath = RGPTShapeUtil.drawBezierCurve(affine, gPathPoints, null);
		if (gPath != null) {
			pathBounds = gPath.getBounds();
			boundPts = RGPTUtil.getRectPoints(pathBounds, 8);
			RGPTLogger.logToFile("Scaled PathBounds: " + pathBounds);
		}

		// Translating the GPath to the upper left screen. So when ever the
		// shape is
		// imported, it always appears in that place
		int tx = 0, ty = 0;
		Point transPt = new Point(50, 50);
		Point2D.Double[] points = gPathPoints.toArray(new Point2D.Double[0]);
		tx = transPt.x - pathBounds.x;
		ty = transPt.y - pathBounds.y;
		RGPTLogger.logToFile("New Traslation tx: " + tx + " ty: " + ty);
		RGPTUtil.adjustPt(affine, points, tx, ty);
		return gPathPoints;
	}

	private BufferedImage createCutoutImage(BufferedImage origImg) {
		Vector<Point2D.Double> gPathPoints = null, imgPathPoints = null;
		GeneralPath gPath = null;
		Point2D.Double[] boundPts;
		Rectangle pathBounds = m_GPath.getBounds();
		BufferedImage cutoutImg = null;

		// Cloning to maintain the reference of the original object
		gPathPoints = (Vector<Point2D.Double>) m_GPathPoints.clone();
		AffineTransform affine = (AffineTransform) m_ShapeAffine.clone();
		imgPathPoints = RGPTUtil.getTransformedPt(
				m_XONImageHolder.m_ScreenToImageCTM, gPathPoints);
		// RGPTLogger.logToFile("Screen Path Points: "+gPathPoints+
		// "\n\nImage Path Points: "+imgPathPoints);
		// Narayan - Moved to ImageTransformHolder
		// Drawing the GPath to get the
		// The functions in Shape Util draw the shape using the Graphics and
		// Path Points
		// affine is always identity matrix.
		if (m_PathSelectionId == RGPTActionListener.ImageActions.IMAGE_LINE_PATH_MENU
				.toString())
			gPath = RGPTShapeUtil.drawLine(affine, imgPathPoints, null);
		else if (m_PathSelectionId == RGPTActionListener.ImageActions.IMAGE_QUAD_PATH_MENU
				.toString())
			gPath = RGPTShapeUtil.drawQuadCurve(affine, imgPathPoints, null);
		else if (m_PathSelectionId == RGPTActionListener.ImageActions.IMAGE_CUBIC_PATH_MENU
				.toString())
			gPath = RGPTShapeUtil.drawBezierCurve(affine, imgPathPoints, null);
		// RGPTLogger.logToFile("gPath: "+gPath);
		cutoutImg = ImageUtils.cutoutImage(origImg, gPath);
		// RGPTLogger.logToFile("cutoutImg: "+cutoutImg);
		return cutoutImg;
	}

	// END FUNCTION FOR LOGIC PROCESSING

	// START FUNCTION FOR PAINT IMPLEMENTATION

	// This variable is necessary to manage image to pan across the Panel Size.
	Dimension m_OrigPanelSize = null;

	public void paint(Graphics g) {
		boolean setBusyCursor = false;
		Rectangle visibleRect = this.getVisibleRect();
		Dimension panelSize = new Dimension((int) visibleRect.getWidth(),
				(int) visibleRect.getHeight());
		if (m_XONImageHolder == null)
			return;
		if (m_OrigPanelSize == null)
			m_OrigPanelSize = panelSize;
		Graphics2D g2d = (Graphics2D) g;
		g2d.clearRect(0, 0, panelSize.width, panelSize.height);

		// PREVIEW Action do not use Double Buffering. Checking if the Action
		// Performed is to Preview
		if (m_ActionPerformedId != null) {
			String[] wfPreviewActions = RGPTActionListener.WF_PREVIEW_ACTIONS;
			Arrays.sort(wfPreviewActions);
			int index = Arrays.binarySearch(wfPreviewActions,
					m_ActionPerformedId);
			if (index >= 0) {
				processWFPreviewActions(g2d, panelSize);
				return;
			}
		}
		// This loop is executed if the Image is zoomed. Firstly image points
		// before zoom is calculated and then new screen points is
		// calculated based on derived ctm
		Vector<Point2D.Double> imgPathPoints = null;
		if (m_ImageZoomed) {
			imgPathPoints = RGPTUtil.getTransformedPt(
					m_XONImageHolder.m_ScreenToImageCTM, m_GPathPoints);
			setBusyCursor = true;
			m_XONImageMakerApp.setCursor(CursorController.m_BusyCursor);
		}
		m_XONImageHolder.deriveDeviceCTM(m_ScalingFactor, panelSize, false);
		if (m_ImageZoomed) {
			m_GPathPoints = RGPTUtil.getTransformedPt(
					m_XONImageHolder.m_WindowDeviceCTM, imgPathPoints);
			m_ImageZoomed = false;
		}
		Dimension oldPanelSize = panelSize;
		panelSize = setPanelSize(panelSize);
		g2d.clearRect(0, 0, panelSize.width, panelSize.height);
		// Drawing the Image and the Graphic Path Points on the screen. For Path
		// Points Double Buffering is used.
		if (m_ActivateDoubleBuffering) {
			if (m_BuffDispImage == null) {
				m_BuffDispImage = (BufferedImage) createImage(panelSize.width,
						panelSize.height);
				m_BuffDispImageRect = new Rectangle(panelSize.width,
						panelSize.height);
			}
			g2d = (Graphics2D) m_BuffDispImage.createGraphics();
		}
		setPanelColor(g2d, panelSize); // Setting the Panel Color Based on the
										// Image
		m_XONImageMakerApp.updateZoom((int) (m_XONImageHolder.m_Scale * 100));
		// int w = m_XONImageHolder.m_RegularImage.getWidth();
		// int h = m_XONImageHolder.m_RegularImage.getHeight();
		g2d.drawImage(m_XONImageHolder.m_RegularImage,
				m_XONImageHolder.m_WindowDeviceCTM, null);

		// if (isValidAction(RGPTActionListener.DISPLAY_SELECTION_ACTIONS)) {
		if (RGPTUtil.isImageActionEqual(m_ProcessSelectionId,
				"MAKE_PIXEL_TRANSPERENT_MENU")) {
			if (m_ActivateDoubleBuffering)
				showTranformedImage(g2d, true);
			// else showTranformedImage(g2d, false);
		}
		if (isDrawGPathAction())
			drawGraphicPath(g2d);
		if (m_ActivateDoubleBuffering) {
			g2d = (Graphics2D) g;
			// Draws the buffered image to the screen.
			g2d.drawImage(m_BuffDispImage, 0, 0, this);
		}
		if (setBusyCursor)
			m_XONImageMakerApp.resetCursor();
	}

	// Setting the Panel Color Based on the Image
	private void setPanelColor(Graphics2D g2d, Dimension panelSize) {
		Color origColor = g2d.getColor();
		g2d.setColor(m_XONImageHolder.m_PanelColor);
		g2d.fillRect(0, 0, panelSize.width, panelSize.height);
		g2d.setColor(origColor);
	}

	private void processWFPreviewActions(Graphics2D g2d, Dimension panelSize) {
		RGPTActionListener.WFActions action = null;
		m_PreviewSelectionId = m_ActionPerformedId;
		action = RGPTActionListener.WFActions.valueOf(m_ActionPerformedId);
		setPanelColor(g2d, panelSize);
		m_XONImageMakerApp.setCursor(CursorController.m_BusyCursor);
		switch (action) {
		case PREVIEW_SHAPE:
			g2d.draw(m_GPath);
			break;
		case PREVIEW_IMAGE_CUTOUT:
			previewCutoutImage(g2d, panelSize);
			break;
		case PREVIEW_CHANGE_IMAGE:
			showTransformedImage(m_XONImageHolder.m_RegularImage,
					m_XONImageHolder.m_PanelColor, g2d, panelSize, true);
			break;
		}
		m_XONImageMakerApp.resetCursor();
	}

	// Setting the Panel Size based on the Image Being Scaled
	private Dimension setPanelSize(Dimension panelSz) {
		int imgWt = m_XONImageHolder.m_OrigImageWidth;
		int imgHt = m_XONImageHolder.m_OrigImageHeight;
		int wt = (int) (imgWt * m_XONImageHolder.m_Scale);
		int ht = (int) (imgHt * m_XONImageHolder.m_Scale);
		int margin = RGPTParams.getIntVal("PanelMargin");
		int panelWt = wt;
		if (panelSz.width > wt)
			panelWt = panelSz.width;
		int panelHt = ht;
		if (panelHt < m_OrigPanelSize.height)
			panelHt = m_OrigPanelSize.height;
		Point2D.Double ptSrc = new Point2D.Double(imgWt, imgHt), ptDes = null;
		ptDes = (Point2D.Double) m_XONImageHolder.m_WindowDeviceCTM.transform(
				ptSrc, null);
		// RGPTLogger.logToFile("\n\nOrigPanelSize: "+m_OrigPanelSize+" Curr Panel Size: "+
		// panelSz+" Scaled Img Wt: "+wt+" Ht: "+ht+" New PanelWt: "+
		// panelWt+" ht: "+panelHt+" New Pt Dest: "+ptDes);
		Dimension newPanelSize = new Dimension(panelWt, panelHt);
		this.setPreferredSize(newPanelSize);
		this.revalidate();
		return newPanelSize;
	}

	// This is done to activate scroll pane by setting the Dimension of the
	// Panel
	// which holds the Contents i.e. the Image of the PDF Page and the VDP Data.
	private void activateScrollPane() {
		int w = m_XONImageHolder.m_RegularImage.getWidth();
		int h = m_XONImageHolder.m_RegularImage.getHeight();
		int x = (int) m_XONImageHolder.m_WindowDeviceCTM.getTranslateX();
		int y = (int) m_XONImageHolder.m_WindowDeviceCTM.getTranslateY();
		int width = (int) (w * m_XONImageHolder.m_Scale);
		int height = (int) (h * m_XONImageHolder.m_Scale);
		Dimension panelSize = new Dimension(width, height);
		Rectangle rect = new Rectangle(x, y, width, height);
		// this.scrollRectToVisible(rect);
		this.setPreferredSize(panelSize);
		this.revalidate();
	}

	// Draws Graphic Path using the points clicked by the user
	private void drawGraphicPath(Graphics g) {
		// Transforming the Graphic Path Points based on the Affine and
		// resetting the
		// affine to identiity matrix
		Point2D.Double[] points = m_GPathPoints.toArray(new Point2D.Double[0]);
		if (m_GPathPoints != null && m_ShapeAffine != null) {
			RGPTUtil.adjustPt(m_ShapeAffine, points, 0, 0);
			m_ShapeAffine.setToIdentity();
		}

		// The functions in Shape Util draw the shape using the Graphics and
		// Path Points
		if (m_PathSelectionId == RGPTActionListener.ImageActions.SHAPE_LINE_PATH_MENU
				.toString()
				|| m_PathSelectionId == RGPTActionListener.ImageActions.IMAGE_LINE_PATH_MENU
						.toString()
				|| m_PathSelectionId == RGPTActionListener.WFActions.SELECT_IMAGE_LOCATION_STEP
						.toString())
			m_GPath = RGPTShapeUtil.drawLine(m_ShapeAffine, m_GPathPoints, g);
		else if (m_PathSelectionId == RGPTActionListener.ImageActions.SHAPE_QUAD_PATH_MENU
				.toString()
				|| m_PathSelectionId == RGPTActionListener.ImageActions.IMAGE_QUAD_PATH_MENU
						.toString())
			m_GPath = RGPTShapeUtil.drawQuadCurve(m_ShapeAffine, m_GPathPoints,
					g);
		else if (m_PathSelectionId == RGPTActionListener.ImageActions.SHAPE_CUBIC_PATH_MENU
				.toString()
				|| m_PathSelectionId == RGPTActionListener.ImageActions.IMAGE_CUBIC_PATH_MENU
						.toString())
			m_GPath = RGPTShapeUtil.drawBezierCurve(m_ShapeAffine,
					m_GPathPoints, g);
	}

	private BufferedImage showTransformedImage(BufferedImage origImg,
			Color bgColor, Graphics2D g2d, Dimension panelSize,
			boolean createCopy) {
		boolean useBufferedImage = false;
		Vector<Integer> transPixel = new Vector<Integer>();
		resetTransformationPixels(origImg, useBufferedImage, transPixel);
		resetTransformationPixels(origImg, useBufferedImage, transPixel, true,
				m_AddTransformationPixel);
		resetTransformationPixels(origImg, useBufferedImage, transPixel, false,
				m_RemTransformationPixel);
		BufferedImage transImg = null;
		transImg = ImageUtils.tranformImage(origImg, transPixel, bgColor,
				createCopy);
		if (g2d != null)
			displayImage(transImg, g2d, panelSize);
		return transImg;
	}

	private void previewCutoutImage(Graphics2D g2d, Dimension panelSize) {
		// RGPTLogger.logToFile("RegImg: "+m_XONImageHolder.m_RegularImage);
		BufferedImage cutoutImg = createCutoutImage(m_XONImageHolder.m_RegularImage);
		// RGPTLogger.logToFile("cutoutImg: "+cutoutImg);
		displayImage(cutoutImg, g2d, panelSize);
	}

	private void displayImage(BufferedImage img, Graphics2D g2d,
			Dimension panelSize) {
		// Retriving the Device CTM for Display
		Map<String, Object> derivedData = null;
		int newImgWt = img.getWidth(), newImgHt = img.getHeight();
		int margin = RGPTParams.getIntVal("PanelMargin");

		derivedData = ImageUtils.deriveDeviceCTM(m_ScalingFactor, panelSize,
				margin, newImgWt, newImgHt);
		AffineTransform deviceCTM = (AffineTransform) derivedData
				.get("WindowDeviceCTM");
		// Draws the buffered image to the screen.
		g2d.drawImage(img, deviceCTM, null);
	}

	private BufferedImage showTranformedImage(Graphics2D g2d,
			boolean useBufferedImage) {
		try {
			if (m_SelectedPixelPoint == null
					&& m_SelectedPixelPointList.size() == 0
					&& m_GPathPoints.size() == 0) {
				// RGPTLogger.logToFile("No Transformation: SelPt: "+m_SelectedPixelPoint+
				// " SelectedPixelPointList size: "+m_SelectedPixelPointList);
				return null;
			}
			SelectedPixelPointHolder selImgPtHldr = null;
			GeneralPath gpath = null;
			BufferedImage origImg = m_BuffDispImage;
			boolean createCopy = false;
			AffineTransform winAffine = m_XONImageHolder.m_WindowDeviceCTM;
			AffineTransform imgAffine = m_XONImageHolder.m_ScreenToImageCTM;
			if (!useBufferedImage) {
				origImg = m_XONImageHolder.m_RegularImage;
				createCopy = true;
			}

			// Transfering the Graphic Path to Object holding the Selected Image
			// Locations
			if (isValidAction(RGPTActionListener.POINT_SELECTION_ACTIONS)) {
				if (m_GPathPoints.size() > 0) {
					selImgPtHldr = SelectedPixelPointHolder.create(null,
							m_GPathPoints, imgAffine, false, m_SetColorRange);
					m_SelectedPixelPointList.addElement(selImgPtHldr);
					m_GPathPoints.clear();
					m_GPath = null;
					m_PathSelectionId = null;
				}
				// Draw All Graphic Paths containing selected areas of the image
				for (int i = 0; i < m_SelectedPixelPointList.size(); i++) {
					selImgPtHldr = m_SelectedPixelPointList.elementAt(i);
					selImgPtHldr.drawGPath(g2d, winAffine);
				}
			}

			// Converting the Image Points to Screen Points in case of Double
			// Buffering
			if (m_SelectedPixelPoint != null) {
				Point2D.Double selPixelPt = m_SelectedPixelPoint;
				if (useBufferedImage)
					selPixelPt = (Point2D.Double) winAffine.transform(
							selPixelPt, null);
				Vector<Point2D.Double> gPathPts = null;
				gPathPts = SelectedPixelPointHolder.addSelectedPoint(
						m_SelectedPixelPointList, m_SelectedPixelPoint,
						m_GPathPoints, winAffine, imgAffine, m_SetColorRange);
				// Even though the code supports clicking on any area, it is
				// restricted here.
				if (gPathPts == null) {
					m_SelectedPixelPointList
							.removeElement(m_SelectedPixelPointList
									.lastElement());
					RGPTUIUtil.showErrorMesg(this, null,
							"ERROR_SELECT_CHANGE_PIXEL_STEP");
				} else {
					gpath = RGPTShapeUtil.drawLine(null, gPathPts, null);
					// If Shape is selected then the Selected Pixel Point is
					// elliminated.
					if (RGPTUtil.isWFActionEqual(m_ActionPerformedId,
							"SEL_CUTOUT_SHAPE"))
						selPixelPt = null;
					setTransformationPixels(origImg, selPixelPt,
							m_TransformationPixel, gpath, m_SetColorRange);
					m_SelectedPixelPoint = null;
				}
			}
			if (m_TransformationPixel.size() == 0) {
				resetTransformationPixels(m_BuffDispImage, useBufferedImage,
						m_TransformationPixel);
				resetTransformationPixels(m_BuffDispImage, useBufferedImage,
						m_TransformationPixel, true, m_AddTransformationPixel);
				// RGPTLogger.logToFile("Image Trans Pixel Hldr : "+resetTransformationPixels);
				resetTransformationPixels(m_BuffDispImage, useBufferedImage,
						m_TransformationPixel, false, m_RemTransformationPixel);
			}
			BufferedImage newImg = null;
			Color panalColor = m_XONImageHolder.m_PanelColor;
			newImg = ImageUtils.tranformImage(origImg, m_TransformationPixel,
					panalColor, createCopy);
			// Draws the buffered image to the screen.
			// ImageUtils.SaveImage(m_BuffDispImage, "test_trans_buff.jpg",
			// "jpg");
			// ImageUtils.SaveImage(newImg, "test_trans_new.jpg", "jpg");
			// if (g2d != null) {
			// if (useBufferedImage) g2d.drawImage(newImg, 0, 0, this);
			// else g2d.drawImage(newImg, m_XONImageHolder.m_WindowDeviceCTM,
			// null);
			// }
			return newImg;
		} catch (Exception ex) {
			RGPTLogger.logToFile("Unable Trannsform Image Pixel", ex);
			// RGPTUIUtil.showErrorMesg(this, null, "ERROR_IMAGE_TRANSFORM");
		}
		return null;
	}

	private void resetTransformationPixels(BufferedImage origImg,
			boolean useBufferedImage, Vector<Integer> transPixel,
			boolean setNewPixels, Vector<ImagePointHolder> imgPixelPtList) {
		Point2D.Double selPixelPt = null;
		double selSize = 0.0;
		ImagePointHolder pixelPtHldr = null, transPixelPtHldr = null;
		for (int i = 0; i < imgPixelPtList.size(); i++) {
			pixelPtHldr = imgPixelPtList.elementAt(i);
			selPixelPt = pixelPtHldr.pixelPt;
			selSize = pixelPtHldr.selSize;
			if (useBufferedImage) {
				pixelPtHldr = ImagePointHolder
						.createImagePointHolder(selPixelPt, selSize,
								m_XONImageHolder.m_WindowDeviceCTM);
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

	private void resetTransformationPixels(BufferedImage origImg,
			boolean useBufferedImage, Vector<Integer> transPixel) {
		Point2D.Double selPixelPt = null;
		SelectedPixelPointHolder selImgPtHldr = null, selPtHldr = null;
		AffineTransform winAffine = m_XONImageHolder.m_WindowDeviceCTM;
		for (int i = 0; i < m_SelectedPixelPointList.size(); i++) {
			selImgPtHldr = m_SelectedPixelPointList.elementAt(i);
			for (int j = 0; j < selImgPtHldr.selectedPts.size(); j++) {
				selPixelPt = selImgPtHldr.selectedPts.elementAt(j);
				if (useBufferedImage) {
					selPtHldr = SelectedPixelPointHolder.create(selPixelPt,
							selImgPtHldr.gpathPts, winAffine, true,
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

	private void setTransformationPixels(BufferedImage origImg,
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

	// END FUNCTION FOR PAINT IMPLEMENTATION

}