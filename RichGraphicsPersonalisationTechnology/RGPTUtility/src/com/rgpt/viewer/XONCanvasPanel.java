// RGPT PACKAGES
package com.rgpt.viewer;

import java.awt.Color;
import java.awt.Cursor;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.rgpt.imageutil.ImageUtils;
import com.rgpt.imageutil.XONImagingEngine;
import com.rgpt.templateutil.VDPFieldInfo;
import com.rgpt.templateutil.VDPImageFieldInfo;
import com.rgpt.templateutil.XODImageInfo;
import com.rgpt.util.AppletParameters;
import com.rgpt.util.CursorController;
import com.rgpt.util.LocalizationUtil;
import com.rgpt.util.RGPTActionListener;
import com.rgpt.util.RGPTFileFilter;
import com.rgpt.util.RGPTListCellRenderer;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTShapeUtil;
import com.rgpt.util.RGPTUIUtil;
import com.rgpt.util.RGPTUtil;
import com.rgpt.util.ScalingFactor;
import com.rgpt.util.StaticFieldInfo;

public class XONCanvasPanel extends JPanel implements MouseListener,
		MouseMotionListener {
	// Indicating the Zoom of the Canvas Image
	ScalingFactor m_ScalingFactor;

	// Apps Object which contains this canvas panel
	public XONImageDesigner m_XONImageDesignerApp;

	// XODImageInfo maintains all the data of XON Image Designer and serializes
	// its
	// data in XOD (Xpress On Design) File
	XODImageInfo m_XODImageInfo;

	// This identifies the VDP Field Selected
	public VDPFieldInfo m_SelVDPFieldInfo = null;

	// This variable notifies if the Image is Zoomed
	public boolean m_ImageZoomed = false;

	// Stores the Action Performed by the User
	public String m_ActionPerformedId = "";

	// Main WF Id which corresponds to Image Designer Action
	public String m_WorkflowProcessId = null;

	// Stores the Process selected by the User
	public String m_ProcessSelectionId = null;

	// Stores the Path Selection selected by the User
	public StaticFieldInfo.GraphicsPaths m_PathSelectionId = null;

	// Stores the Preview Action selected by the User
	public String m_PreviewSelectionId = null;

	// This image is created and populated when Double Buffering is on.
	public BufferedImage m_BuffDispImage = null;
	public Rectangle m_BuffDispImageRect = null;
	public boolean m_ActivateDoubleBuffering = false;
	public BufferedImage m_ZoomPreviewImage = null;

	// This variables are defined for dragging the graphic path points
	private final static int NOT_DRAGGING = -1;
	private final static int NEIGHBORHOOD = 15;
	private int m_DragIndex = NOT_DRAGGING;

	// Graphic Path Points clicked for drawing shape or cutting image
	public Vector<Point2D.Double> m_GPathPoints;

	// This variable define the General Path of the shape
	public GeneralPath m_GPath;

	// Affine Transform to take care of Scaling, Rotation and Transformation
	public AffineTransform m_ShapeAffine;

	// This maintains Action to the coresponding Dialog Box
	public Map<String, JDialog> m_ActionDialogBox = null;

	// This maintains Action to the coresponding Component in the Dialog Box
	public Map<String, Map> m_DialogActionComponent = null;

	public XONCanvasPanel(XONImageDesigner xonImageDesigner) {
		super();
		m_ActivateDoubleBuffering = false;
		m_ScalingFactor = ScalingFactor.FIT_PAGE;
		m_PathSelectionId = StaticFieldInfo.DEF_SHAPE_PATH;
		m_XONImageDesignerApp = xonImageDesigner;
		m_GPathPoints = new Vector<Point2D.Double>();
		m_DialogActionComponent = new HashMap<String, Map>();
		m_ActionDialogBox = new HashMap<String, JDialog>();
		addMouseListener(this);
		addMouseMotionListener(this);
		m_ShapeAffine = new AffineTransform();
		m_ShapeAffine.setToIdentity();
	}

	public void setXODImageInfo(XODImageInfo xodImgInfo) {
		m_XODImageInfo = xodImgInfo;
	}

	// START FUNCTION FOR ACTION LISTENERS

	// Here action indicates User Action,
	// isProcessSel indicates if the current Action is a New Process within the
	// WF
	// setProcessSel indicates reset the Action Performed to Process Selection
	// Id
	// repaintPanel if set to true repaints this Image Panel
	// Helper Method and calls setActionPerformed with appropriate params
	public void resetProcessSel(boolean repaintPanel) {
		setActionPerformed(null, false, true, repaintPanel);
	}

	public void setProcessSel(String action, boolean repaintPanel) {
		// RGPTUIUtil.showInfoMesg(this, null, action+"_INFO");
		setActionPerformed(action, true, false, repaintPanel);
	}

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
		m_ScalingFactor.zoomOut((int) (m_XODImageInfo.m_Scale * 100));
		revalidate();
		repaint();
	}

	public void zoomIn() {
		resetZoom();
		m_ScalingFactor.zoomIn((int) (m_XODImageInfo.m_Scale * 100));
		revalidate();
		repaint();
	}

	public void resetZoom() {
		m_ImageZoomed = true;
		m_BuffDispImage = null;
		m_ScalingFactor = ScalingFactor.ZOOM_IN_OUT;
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
		int margin = AppletParameters.getIntVal("ZoomPreviewMargin");
		Rectangle zommRect = RGPTUtil.getRectangle(pt, margin);
		if (m_BuffDispImage == null || m_BuffDispImageRect == null
				|| !m_BuffDispImageRect.contains(zommRect))
			return;
		BufferedImage prevImg = m_BuffDispImage.getSubimage(zommRect.x,
				zommRect.y, zommRect.width, zommRect.height);
		m_ZoomPreviewImage = ImageUtils.brightenImage(prevImg);
		m_XONImageDesignerApp.m_ZoomViewPane.repaint();
	}

	public void previewPath(String action) {
		if (m_GPath == null) {
			RGPTUIUtil.showErrorMesg(this, null, "ERROR_NO_PATH");
			return;
		}
		m_ActionPerformedId = action;
		repaint();
	}

	public void drawGPath() {
		m_ActionPerformedId = RGPTActionListener.WFActions.DRAW_SHAPE_PATH_STEP
				.toString();
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

	public JDialog m_SaveShapeDialog = null;

	private void saveShapeDialogBox(String action) {
		if (m_SaveShapeDialog != null) {
			m_SaveShapeDialog.setVisible(true);
			return;
		}
		Map<String, JComponent> actionComp = null;
		LocalizationUtil lu = new LocalizationUtil();
		int wt = AppletParameters.getIntVal("SaveShapeUIWidth");
		int ht = AppletParameters.getIntVal("SaveShapeUIHeight");
		// RGPTUIUtil.setCompSize(contentPane, wt, ht, 1);
		String basePropName = "SAVE_SHAPE_DIALOG";
		JPanel contentPane = new JPanel();
		actionComp = RGPTUIUtil.createDialogContent(contentPane, basePropName,
				m_XONImageDesignerApp);
		m_DialogActionComponent.put(action, actionComp);

		// Setting the Renderrer and Data Model for Shape Type Combo Box
		DefaultComboBoxModel model = null;
		String selItem = "", newShapeTpe = lu.getText("NewShapeType");
		String shapeOutDir = RGPTUtil.getShapesDirectory(AppletParameters
				.getVal("UserType"));
		Vector<String> shapeTypes = null;
		String shTypSerFile = shapeOutDir + "/"
				+ lu.getText("ShapeTypeFileName");
		try {
			shapeTypes = (Vector<String>) RGPTUtil
					.getSerializeObject(shTypSerFile);
		} catch (Exception ex) {
			shapeTypes = new Vector<String>();
		}
		// File shapeDir = new File(shapeOutDir); String[] files =
		// shapeDir.list();
		String[] files = shapeTypes.toArray(new String[0]);
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
				.getFileName(m_XODImageInfo.m_CanvasImageHolder.m_FileName));
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
		String shapeType = selShapeType;
		String shapeTypeEntry = lu.getText("NewShapeType");
		String shapeOutDir = RGPTUtil.getShapesDirectory(AppletParameters
				.getVal("UserType"));
		if (selShapeType.equals(shapeTypeEntry)) {
			shapeType = newShapeType;
			Vector<String> shapeTypes = null;
			String shTypSerFile = shapeOutDir + "/"
					+ lu.getText("ShapeTypeFileName");
			try {
				shapeTypes = (Vector<String>) RGPTUtil
						.getSerializeObject(shTypSerFile);
			} catch (Exception ex) {
				shapeTypes = new Vector<String>();
			}
			shapeTypes.addElement(shapeType);
			try {
				RGPTUtil.serializeObject(shTypSerFile, shapeTypes);
			} catch (Exception ex) {
				RGPTLogger.logToFile("Exception Saving Shape Types", ex);
			}
		}
		String shFileName = shapeType.toUpperCase() + "_"
				+ shapeName.toUpperCase();
		File shapeDir = new File(shapeOutDir);
		String[] files = shapeDir.list();
		int index = 0;
		while (true) {
			if (!RGPTUtil.contains(files, shFileName + ".xsh"))
				break;
			index++;
			shFileName = shFileName + "_" + index;
		}
		String serFilePath = shapeOutDir + "/" + shFileName + ".xsh";
		RGPTLogger.logToFile("Serialized File Path: " + serFilePath);
		// Saving the Shape to a Serialized File
		try {
			Vector<Point2D.Double> gPathPoints = saveGPath();
			Map<String, Object> shapeInfo = new HashMap<String, Object>();
			shapeInfo.put(m_PathSelectionId.toString(), gPathPoints);
			shapeInfo.put("ShapeType", shapeType);
			shapeInfo.put("ShapeName", shapeName);
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
		double sx = 1.0;
		double sy = 1.0;
		if (pathBounds.width > 0)
			sx = (double) newBounds.width / (double) pathBounds.width;
		if (pathBounds.height > 0)
			sy = (double) newBounds.height / (double) pathBounds.height;
		if (sy > sx)
			sy = sx;
		else
			sx = sy;
		affine = RGPTUtil.getAffineTransform(affine, pathBounds, sx, sy, 0);
		sx = affine.getScaleX();
		sy = affine.getScaleY();
		RGPTLogger.logToFile("Orig Bounds: " + pathBounds + " New Affine: "
				+ affine + " sx: " + sx + " sy: " + sy);

		// Drawing the GPath to get the
		// The functions in Shape Util draw the shape using the Graphics and
		// Path Points
		if (m_PathSelectionId == StaticFieldInfo.GraphicsPaths.LINE_PATH)
			gPath = RGPTShapeUtil.drawLine(affine, gPathPoints, null);
		else if (m_PathSelectionId == StaticFieldInfo.GraphicsPaths.QUAD_PATH)
			gPath = RGPTShapeUtil.drawQuadCurve(affine, gPathPoints, null);
		else if (m_PathSelectionId == StaticFieldInfo.GraphicsPaths.CUBIC_PATH)
			gPath = RGPTShapeUtil.drawBezierCurve(affine, gPathPoints, null);
		if (gPath != null) {
			pathBounds = gPath.getBounds();
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
			typeFldBox.setText("");
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
				m_XONImageDesignerApp.closeFile();
			}
			break;
		case SAVE_SHAPE_CANCEL_ACTION:
			m_SaveShapeDialog.setVisible(false);
			drawGPath();
			break;
		}
	}

	// END FUNCTION FOR ACTION LISTENERS

	// START FUNCTION FOR MOUSE ACTIONS

	// This identifies the Action to be performed on Mouse Pressed and Dragged
	String m_MouseAction = null;
	Point m_OrigMousePt = null;

	// This is initiated only when Scale or Rotation is activated. It holds the
	// boundary pt that
	// is being dragged as well as the cursor. Keys are DragIndex, DragCursor,
	// AffineTransform,
	// and RotAngle
	Map<String, Object> m_BoundaryDragData = null;

	public void mousePressed(MouseEvent e) {
		Point pt = e.getPoint();
		Cursor cursor = CursorController.m_DefaultCursor;
		if (m_ActionPerformedId == null || m_ActionPerformedId.isEmpty())
			return;
		if (m_XONImageDesignerApp.isShapeDesign()) {
			if (m_XODImageInfo.m_CanvasImageHolder == null)
				return;
			if (m_ActionPerformedId
					.equals(RGPTActionListener.WFActions.DRAW_SHAPE_PATH_STEP
							.toString())) {
				Point2D.Double pt2D = new Point2D.Double(pt.x, pt.y);
				findDraggingPt(e);
				if (m_GPathPoints.size() == 0 || m_DragIndex == NOT_DRAGGING) {
					m_GPathPoints.add(pt2D);
					repaint();
				}
			} else if (m_ActionPerformedId
					.equals(RGPTActionListener.WFActions.ADJUST_SHAPE_PATH_STEP
							.toString())) {
				findDraggingPt(e);
			}
			return;
		} // if (m_XONImageDesignerApp.isShapeDesign())

		boolean checkTranslation = false;
		if (m_SelVDPFieldInfo != null) {
			Rectangle pathBounds = m_SelVDPFieldInfo.m_ScreenBounds;
			StringBuffer ctrlPtsBuf = new StringBuffer();
			if (m_ActionPerformedId
					.equals(RGPTActionListener.IDAppsActions.TB_TRANSLATE_SHAPE
							.toString())) {
				checkTranslation = true;
			} else if (m_ActionPerformedId
					.equals(RGPTActionListener.IDAppsActions.TB_SCALE_SHAPE
							.toString())) {
				m_BoundaryDragData = RGPTUIUtil.findBoundaryDrag(true,
						pathBounds, pt);
				if (m_BoundaryDragData != null) {
					m_MouseAction = StaticFieldInfo.MouseActions.SCALED
							.toString();
					int cursorType = ((Integer) m_BoundaryDragData
							.get("DragCursor")).intValue();
					cursor = CursorController.getCursor(cursorType);
				} else
					checkTranslation = true;
			} else if (m_ActionPerformedId
					.equals(RGPTActionListener.IDAppsActions.TB_ROTATE_SHAPE
							.toString())) {
				m_BoundaryDragData = RGPTUIUtil.findBoundaryDrag(false,
						pathBounds, pt);
				if (m_BoundaryDragData != null) {
					m_MouseAction = StaticFieldInfo.MouseActions.ROTATED
							.toString();
					int cursorType = ((Integer) m_BoundaryDragData
							.get("DragCursor")).intValue();
					cursor = CursorController.getCursor(cursorType);
				} else
					checkTranslation = true;
			} else if (m_ActionPerformedId
					.equals(RGPTActionListener.IDAppsActions.TB_EDIT_SHAPE
							.toString())) {
				m_BoundaryDragData = RGPTUtil.findDraggingPt(
						m_SelVDPFieldInfo.m_ScreenPathPoints, pt);
				if (m_BoundaryDragData == null)
					return;
				m_MouseAction = StaticFieldInfo.MouseActions.PATH_ADJUSTED
						.toString();
			} else if (m_SelVDPFieldInfo instanceof VDPImageFieldInfo) {
				VDPImageFieldInfo selImgFld = (VDPImageFieldInfo) m_SelVDPFieldInfo;
				if (selImgFld.isCircleFilter()) {
					String[] ctrlKeys = { "CenterPt", "RotAngPt", "SpreadPt",
							"RadialPt" };
					Vector<Point2D.Double> ctrlPts = selImgFld
							.getFilterControlPoints(null, ctrlKeys);
					m_BoundaryDragData = RGPTUtil.findDraggingPt(ctrlPts, pt);
					if (m_BoundaryDragData == null)
						return;
					int index = ((Integer) m_BoundaryDragData.get("DragIndex"))
							.intValue();
					if (index == 0)
						return;
					cursor = CursorController.getCursor(Cursor.MOVE_CURSOR);
					m_MouseAction = StaticFieldInfo.MouseActions.CIRCLE_FILTER_ADJUSTED
							.toString();
				} else if (selImgFld.isWarpFilter()) {
					Map<String, Object> ctrlValues = selImgFld
							.getImageFilterControlValues();
					Vector<Point2D.Double> desGridPtList = null;
					desGridPtList = (Vector<Point2D.Double>) ctrlValues
							.get("DesGirdPts");
					m_BoundaryDragData = RGPTUtil.findDraggingPt(desGridPtList,
							pt);
					if (m_BoundaryDragData == null)
						return;
					cursor = CursorController.getCursor(Cursor.MOVE_CURSOR);
					m_MouseAction = StaticFieldInfo.MouseActions.GRID_POINT_ADJUSTED
							.toString();
				} else if (selImgFld.isRadialFilter(ctrlPtsBuf)) {
					String[] ctrlKeys = ctrlPtsBuf.toString().split("::");
					Vector<Point2D.Double> ctrlPts = selImgFld
							.getFilterControlPoints(null, ctrlKeys);
					m_BoundaryDragData = RGPTUtil.findDraggingPt(ctrlPts, pt);
					if (m_BoundaryDragData == null)
						return;
					int index = ((Integer) m_BoundaryDragData.get("DragIndex"))
							.intValue();
					if (index == -1)
						return;
					// No Center Pt adjustment allowed if RadialPt is not
					// specified as control keys
					if (index == 0) {
						if (!selImgFld.isCenterPtAdj())
							return;
					}
					String dragedPt = ctrlKeys[index];
					m_BoundaryDragData.put("DragPt", dragedPt);
					if (dragedPt.equals("RadialPt")) {
						m_OrigMousePt = pt;
						m_MouseAction = StaticFieldInfo.MouseActions.FILTER_CIRCLE_ADJUSTED
								.toString();
					} else
						m_MouseAction = StaticFieldInfo.MouseActions.RADIAL_FILTER_ADJUSTED
								.toString();
					cursor = CursorController.getCursor(Cursor.MOVE_CURSOR);
				}
			}
		}
		if (checkTranslation) {
			if (setSelectedField(pt, false, m_OrigPanelSize, m_VisiblePanelRect)) {
				m_OrigMousePt = pt;
				m_MouseAction = StaticFieldInfo.MouseActions.TRANSLATED
						.toString();
				cursor = CursorController.getCursor(Cursor.MOVE_CURSOR);
			}
		}
		m_XONImageDesignerApp.setCursor(cursor);
	}

	public void mouseDragged(MouseEvent e) {
		Point pt = e.getPoint();
		if (!m_OrigPanelSize.contains(pt))
			return;

		// Create Shape....
		if (m_XONImageDesignerApp.isShapeDesign()) {
			if (m_XODImageInfo.m_CanvasImageHolder == null)
				return;
			showZoomPreview(pt);
			if (m_ActionPerformedId
					.equals(RGPTActionListener.WFActions.DRAW_SHAPE_PATH_STEP
							.toString())) {
				if (m_DragIndex != NOT_DRAGGING) {
					setDraggingPt(e);
					repaint();
					return;
				} else if (m_GPathPoints.size() > 0) {
					Point2D.Double pt2D = new Point2D.Double(pt.x, pt.y);
					Point2D.Double pathPt2D = m_GPathPoints.lastElement();
					if (RGPTUtil.getDistance(pathPt2D, pt2D) > 25.0D) {
						m_GPathPoints.add(pt2D);
						repaint();
						return;
					}
				}
			} else if (m_ActionPerformedId
					.equals(RGPTActionListener.WFActions.ADJUST_SHAPE_PATH_STEP
							.toString())) {
				setDraggingPt(e);
				repaint();
				return;
			}
			return;
		} // if (m_XONImageDesignerApp.isShapeDesign())

		if (m_MouseAction == null || m_MouseAction.length() == 0)
			return;
		if (m_SelVDPFieldInfo == null)
			return;
		int dragIndex = -1;
		Map<String, Object> ctrlValues = null;
		VDPImageFieldInfo vdpImgFld = null;
		String[] ctrlKeys = null;
		Vector<Point2D.Double> filterCtrlPoints = null;
		Point2D.Double cntrPt = null;
		Point2D.Double ctrlPt = null, radialPt = null, sliderPt = null, anglePt = null;
		Point2D.Double[] boundPts = null, pathPts = null;
		boolean isTransformed = true;
		Rectangle pathBounds = m_SelVDPFieldInfo.m_ScreenBounds;
		if (m_SelVDPFieldInfo.isImageField())
			vdpImgFld = (VDPImageFieldInfo) m_SelVDPFieldInfo;
		pathPts = m_SelVDPFieldInfo.m_ScreenPathPoints
				.toArray(new Point2D.Double[0]);
		AffineTransform affine = null;
		double rotAngle = 0.0D;
		StringBuffer ctrlPtsBuf = null;
		StaticFieldInfo.MouseActions action = StaticFieldInfo.MouseActions
				.valueOf(m_MouseAction);
		switch (action) {
		case TRANSLATED:
			isTransformed = false;
			int tx = pt.x - m_OrigMousePt.x,
			ty = pt.y - m_OrigMousePt.y;
			boundPts = RGPTUtil.getRectPoints(pathBounds, 4);
			if (!RGPTUtil.isTransformValid(m_OrigPanelSize, boundPts, tx, ty))
				return;
			RGPTUtil.adjustPt(pathPts, tx, ty);
			m_OrigMousePt = pt;
			break;
		case SCALED:
			RGPTUIUtil.processBoundaryDrag(true, m_OrigPanelSize, pathBounds,
					m_BoundaryDragData, pt);
			affine = (AffineTransform) m_BoundaryDragData
					.get("AffineTransform");
			RGPTUtil.adjustPt(affine, pathPts, 0, 0);
			if (vdpImgFld != null)
				vdpImgFld.adjustImageFilter(affine);
			affine.setToIdentity();
			break;
		case ROTATED:
			RGPTUIUtil.processBoundaryDrag(false, m_OrigPanelSize, pathBounds,
					m_BoundaryDragData, pt);
			affine = (AffineTransform) m_BoundaryDragData
					.get("AffineTransform");
			RGPTUtil.adjustPt(affine, pathPts, 0, 0);
			affine.setToIdentity();
			if (m_BoundaryDragData.get("RotAngle") != null
					&& m_SelVDPFieldInfo.isImageField()) {
				rotAngle = ((Double) m_BoundaryDragData.get("RotAngle"))
						.doubleValue();
				vdpImgFld.m_RotAngle = rotAngle;
				vdpImgFld.adjustImageFilter(affine);
			}
			affine.setToIdentity();
			break;
		case PATH_ADJUSTED:
			dragIndex = ((Integer) m_BoundaryDragData.get("DragIndex"))
					.intValue();
			if (dragIndex == RGPTUtil.NOT_DRAGGING)
				return;
			Point2D.Double dragPt = pathPts[dragIndex];
			dragPt.x = pt.x;
			dragPt.y = pt.y;
			break;
		case GRID_POINT_ADJUSTED:
			dragIndex = ((Integer) m_BoundaryDragData.get("DragIndex"))
					.intValue();
			if (dragIndex == RGPTUtil.NOT_DRAGGING)
				return;
			ctrlValues = vdpImgFld.getImageFilterControlValues();
			Vector<Point2D.Double> desGridPtList = null;
			desGridPtList = (Vector<Point2D.Double>) ctrlValues
					.get("DesGirdPts");
			Point2D.Double gridDragPt = desGridPtList.elementAt(dragIndex);
			if (vdpImgFld.m_ScreenBounds.contains(pt)) {
				gridDragPt.x = pt.x;
				gridDragPt.y = pt.y;
			}
			vdpImgFld.applyFilters();
			repaint();
			return;
		case CIRCLE_FILTER_ADJUSTED:
			float radFactor = AppletParameters
					.getFloatVal("ImageFilterRadialFactor");
			dragIndex = ((Integer) m_BoundaryDragData.get("DragIndex"))
					.intValue();
			String[] crclCtrlKeys = { "CenterPt", "RotAngPt", "SpreadPt",
					"RadialPt" };
			filterCtrlPoints = vdpImgFld.getFilterControlPoints(null,
					crclCtrlKeys);
			cntrPt = filterCtrlPoints.elementAt(0);
			ctrlValues = vdpImgFld.getImageFilterControlValues();
			// Setting the Rotation Angle in the Circle Image Filter
			if (dragIndex == 1) {
				Point2D.Double rotPt = filterCtrlPoints.elementAt(1);
				double rotRad = ((Double) ctrlValues.get("RotRadius"))
						.doubleValue();
				double dx = Math.abs(pt.x - cntrPt.x), dy = Math.abs(pt.y
						- cntrPt.y);
				double newAngle = Math.atan(dy / dx);
				dy = rotRad * Math.sin(newAngle);
				dx = rotRad * Math.cos(newAngle);
				if (pt.x > cntrPt.x)
					rotPt.x = cntrPt.x + dx;
				else
					rotPt.x = cntrPt.x - dx;
				if (pt.y > cntrPt.y)
					rotPt.y = cntrPt.y + dy;
				else
					rotPt.y = cntrPt.y - dy;
				double angle = RGPTUtil.getAngle(cntrPt, rotPt);
				// RGPTLogger.logToFile("Radians RotAngle: "+angle+" rotRad: "+rotRad+
				// " dx: "+dx+" dy: "+dy);
				float rotAng = (float) Math.toDegrees(angle);
				ctrlValues.put("RotAngle", rotAng);
				// RGPTLogger.logToFile("RotAngle: "+rotAng);
				vdpImgFld.applyFilters();
				repaint();
				return;
			} else if (dragIndex == 2) {
				Point2D.Double spreadPt = filterCtrlPoints.elementAt(2);
				double spreadRad = ((Double) ctrlValues.get("SpreadRadius"))
						.doubleValue();
				double dx = Math.abs(pt.x - cntrPt.x), dy = Math.abs(pt.y
						- cntrPt.y);
				double newAngle = Math.atan(dy / dx);
				dy = spreadRad * Math.sin(newAngle);
				dx = spreadRad * Math.cos(newAngle);
				if (pt.x > cntrPt.x)
					spreadPt.x = cntrPt.x + dx;
				else
					spreadPt.x = cntrPt.x - dx;
				if (pt.y > cntrPt.y)
					spreadPt.y = cntrPt.y + dy;
				else
					spreadPt.y = cntrPt.y - dy;
				double spread = RGPTUtil.getAngle(cntrPt, spreadPt), spreadAng = 0.0D;
				// RGPTLogger.logToFile("Spread In Rad: "+spread+" SpreadRad: "+spreadRad+
				// " dx: "+dx+" dy: "+dy);
				if (spread >= Math.PI)
					spreadAng = spread - Math.PI;
				else
					spreadAng = spread + Math.PI;
				ctrlValues.put("SpreadAngle", (float) spreadAng);
				// RGPTLogger.logToFile("SpreadAngle: "+spreadAng);
				vdpImgFld.applyFilters();
				repaint();
				return;
			} else if (dragIndex == 3) {
				Point2D.Double radPt = filterCtrlPoints.elementAt(3);
				double transx = (double) pt.x - cntrPt.x;
				if (transx <= 5.0f)
					return;
				radPt.x = cntrPt.x + transx;
				ctrlValues.put("Radius", transx);
				ctrlValues.put("Height", (float) (radFactor * transx));
				vdpImgFld.applyFilters();
				repaint();
				return;
			}
		case RADIAL_FILTER_ADJUSTED:
			dragIndex = ((Integer) m_BoundaryDragData.get("DragIndex"))
					.intValue();
			ctrlPtsBuf = new StringBuffer();
			vdpImgFld.isRadialFilter(ctrlPtsBuf);
			String[] ctrlPts = ctrlPtsBuf.toString().split("::");
			String dragedPt = ctrlPts[dragIndex];
			// RGPTLogger.logToFile(action+" dragIndex: "+dragIndex+" ctrlPts: "+ctrlPtsBuf+
			// " drgPt: "+dragedPt);
			filterCtrlPoints = vdpImgFld.getFilterControlPoints(null, ctrlPts);
			// RGPTLogger.logToFile("Ctrl Pts are: "+filterCtrlPoints);
			cntrPt = filterCtrlPoints.elementAt(0);
			int indPt = RGPTUtil.getKeyIndex(ctrlPts, "SliderPt");
			// RGPTLogger.logToFile("SliderPt index: "+indPt);
			if (indPt != -1)
				sliderPt = filterCtrlPoints.elementAt(indPt);
			indPt = RGPTUtil.getKeyIndex(ctrlPts, "AnglePt");
			// RGPTLogger.logToFile("AnglePt index: "+indPt);
			if (indPt != -1)
				anglePt = filterCtrlPoints.elementAt(indPt);
			indPt = RGPTUtil.getKeyIndex(ctrlPts, "RadialPt");
			// RGPTLogger.logToFile("RadialPt index: "+indPt);
			if (indPt != -1)
				radialPt = filterCtrlPoints.elementAt(indPt);
			// radialPt = filterCtrlPoints.elementAt(2);
			ctrlValues = vdpImgFld.getImageFilterControlValues();
			// Setting the Rotation Angle in the Circle Image Filter
			if (dragIndex == 0) {
				double transx = (double) pt.x - cntrPt.x, transy = (double) pt.y
						- cntrPt.y;
				cntrPt.x = pt.x;
				cntrPt.y = pt.y;
				if (sliderPt != null) {
					sliderPt.x += transx;
					sliderPt.y += transy;
				}
				if (anglePt != null) {
					anglePt.x += transx;
					anglePt.y += transy;
				}
				if (radialPt != null) {
					radialPt.x += transx;
					radialPt.y += transy;
				}
				vdpImgFld.applyFilters();
				repaint();
				return;
			}
			if (dragedPt.equals("SliderPt")) {
				ctrlPt = sliderPt;
				double deltax = pt.x - cntrPt.x;
				float amt = 0.1F;
				double ctrlRad = ((Double) ctrlValues.get("SliderRad"))
						.doubleValue();
				if (deltax <= 0) {
					ctrlPt.x = cntrPt.x + 0.1 * ctrlRad;
					amt = 0.1F;
				} else if (deltax > ctrlRad) {
					ctrlPt.x = cntrPt.x + ctrlRad;
					amt = 1.0F;
				} else {
					ctrlPt.x = pt.x;
					amt = (float) (ctrlPt.x / ctrlRad);
				}
				ctrlValues.put("Amount", Float.toString(amt));
				// RGPTLogger.logToFile("Slider Activated. Amount is: "+amt);
			} else if (dragedPt.equals("AnglePt")) {
				// RGPTLogger.logToFile("Angle Activated");
				double dx = Math.abs(pt.x - cntrPt.x), dy = Math.abs(pt.y
						- cntrPt.y);
				double newAngle = Math.atan(dy / dx);
				ctrlPt = anglePt;
				double ctrlRad = ((Double) ctrlValues.get("AngleRad"))
						.doubleValue();
				dy = ctrlRad * Math.sin(newAngle);
				dx = ctrlRad * Math.cos(newAngle);
				if (pt.x > cntrPt.x)
					ctrlPt.x = cntrPt.x + dx;
				else
					ctrlPt.x = cntrPt.x - dx;
				if (pt.y > cntrPt.y)
					ctrlPt.y = cntrPt.y + dy;
				else
					ctrlPt.y = cntrPt.y - dy;
				double angle = RGPTUtil.getAngle(cntrPt, ctrlPt);
				// RGPTLogger.logToFile("Radians RotAngle: "+angle+" rotRad: "+rotRad+
				// " dx: "+dx+" dy: "+dy);
				float ctrlAng = (float) Math.toDegrees(angle);
				ctrlValues.put("Angle", ctrlAng); // RGPTLogger.logToFile("Angle: "+ctrlAng);
			}
			vdpImgFld.applyFilters();
			repaint();
			return;
		case FILTER_CIRCLE_ADJUSTED:
			// RGPTLogger.logToFile(action+" Activated...");
			ctrlValues = vdpImgFld.getImageFilterControlValues();
			cntrPt = (Point2D.Double) ctrlValues.get("CenterPt");
			if (!vdpImgFld.isRadialAdjFilter()) {
				Point2D.Double radPt = (Point2D.Double) ctrlValues
						.get("RadialPt");
				double transx = (double) pt.x - cntrPt.x;
				if (transx <= 0.0f)
					transx = 0.0F;
				radPt.x = cntrPt.x + transx;
				ctrlValues.put("Radius", (float) transx);
				vdpImgFld.applyFilters();
				repaint();
				return;
			}
			double dx = Math.abs(pt.x - cntrPt.x),
			dy = Math.abs(pt.y - cntrPt.y);
			double radius = Math.sqrt(Math.pow(dx, 2.0) + Math.pow(dy, 2.0));
			// RGPTLogger.logToFile("New Radius: "+radius);
			ctrlValues.put("Radius", (float) radius);
			vdpImgFld.applyFilters();
			repaint();
			return;
		}
		try {
			if (containsSelField())
				m_SelVDPFieldInfo.m_ShowElement = true;
			else
				m_SelVDPFieldInfo.m_ShowElement = false;
			m_SelVDPFieldInfo.setGPathPts(m_XODImageInfo.m_ScreenToImageCTM,
					isTransformed);
			repaint();
		} catch (Exception ex) {
			RGPTLogger.logToFile("Excep setting GPathPts", ex);
		}
	}

	public void mouseReleased(MouseEvent e) {
		m_MouseAction = null;
		m_OrigMousePt = null;
		m_BoundaryDragData = null;
		m_ZoomPreviewImage = null;
		m_DragIndex = NOT_DRAGGING;
		if (m_XONImageDesignerApp.isShapeDesign())
			m_XONImageDesignerApp.m_ZoomViewPane.repaint();
		m_XONImageDesignerApp.resetCursor();
	}

	public void mouseClicked(MouseEvent e) {
		Point pt = e.getPoint();
		if (m_XONImageDesignerApp.isShapeDesign()) {
			if (m_XODImageInfo.m_CanvasImageHolder == null)
				return;
			if (m_ActionPerformedId
					.equals(RGPTActionListener.WFActions.DRAW_SHAPE_PATH_STEP
							.toString())) {
				Point2D.Double pt2D = new Point2D.Double(pt.x, pt.y);
				findDraggingPt(e);
				if (m_GPathPoints.size() == 0 || m_DragIndex == NOT_DRAGGING) {
					m_GPathPoints.add(pt2D);
					repaint();
					// RGPTLogger.logToFile("Added GPathPt. #of Pts: "+m_GPathPoints.size());
				}
			}
			return;
		} // if (m_XONImageDesignerApp.isShapeDesign())

		boolean isPtSel = setSelectedField(pt, false, m_OrigPanelSize,
				m_VisiblePanelRect);
		if (isPtSel && e.getClickCount() == 2
				&& m_SelVDPFieldInfo instanceof VDPImageFieldInfo) {
			RGPTLogger.logToFile("Launching Image Makeover for: "
					+ m_SelVDPFieldInfo.m_FieldName);
			m_XONImageDesignerApp.launchImageMakerApp();
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
		if (m_XONImageDesignerApp.isShapeDesign()) {
			if (m_XODImageInfo.m_CanvasImageHolder == null)
				return;
			showZoomPreview(e.getPoint());
		}
	}

	// END FUNCTION FOR MOUSE ACTIONS

	// START FUNCTION FOR LOGIC PROCESSING

	public boolean containsSelField() {
		if (m_SelVDPFieldInfo == null)
			return false;
		if (m_XODImageInfo.m_ScreenImageRect
				.contains(m_SelVDPFieldInfo.m_ScreenBounds))
			return true;
		return false;
	}

	public boolean setSelectedField(Point pt, boolean isPreviewPanel,
			Rectangle panelSize, Rectangle visibleRect) {
		if (!panelSize.contains(pt) && m_XODImageInfo == null)
			return false;
		VDPFieldInfo selVDPFldInfo = null;
		selVDPFldInfo = m_XODImageInfo.setFieldSelection(m_SelVDPFieldInfo,
				isPreviewPanel, visibleRect.x + pt.x, visibleRect.y + pt.y);
		if (selVDPFldInfo == null)
			return false;
		if (m_SelVDPFieldInfo != null) {
			if (m_SelVDPFieldInfo.equals(selVDPFldInfo))
				return true;
			m_SelVDPFieldInfo.resetFields();
			m_SelVDPFieldInfo.m_IsFieldSelected = false;
			m_SelVDPFieldInfo.m_IsBGCanvasSelected = false;
		}
		m_SelVDPFieldInfo = selVDPFldInfo;
		m_XONImageDesignerApp.resetPreviewPanel();
		RGPTLogger.logToFile("SelVDPFieldInfo: "
				+ m_SelVDPFieldInfo.m_FieldName + " isFldVisible: "
				+ m_SelVDPFieldInfo.m_IsFieldSelected);

		String defAction = RGPTActionListener.IDAppsActions.MA_SELECT_BG
				.toString();
		if (!m_SelVDPFieldInfo.m_IsBGCanvasSelected) {
			defAction = RGPTActionListener.IDAppsActions.TB_EDIT_ELEMENT
					.toString();
			if (!containsSelField())
				defAction = RGPTActionListener.IDAppsActions.TB_TRANSLATE_SHAPE
						.toString();
		}
		m_XONImageDesignerApp.setButtonSelection(defAction);
		m_XONImageDesignerApp.performRGPTAction(defAction);
		// If no Action is Selected and a new Element is selected it is
		// defaulted to be in Translation
		// if (m_ActionPerformedId == null)
		// m_ActionPerformedId = IDAppsActions.TB_TRANSLATE_SHAPE.toString();
		repaint();
		return true;
	}

	public void setDoubleBufferingActivation(boolean activate) {
		m_ActivateDoubleBuffering = activate;
	}

	public void resetXONImage() {
		m_XODImageInfo = null;
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
		m_ImageZoomed = false;
		m_GPath = null;
		m_GPathPoints.clear();
		m_BuffDispImageRect = null;
		m_BuffDispImage = null;
		m_ActivateDoubleBuffering = false;
		m_WorkflowProcessId = null;
		m_ActionPerformedId = null;
		m_ProcessSelectionId = null;
		m_PreviewSelectionId = null;
		m_PathSelectionId = StaticFieldInfo.DEF_SHAPE_PATH;
		m_DragIndex = NOT_DRAGGING;
		if (m_SelVDPFieldInfo != null) {
			m_SelVDPFieldInfo.m_IsFieldSelected = false;
			m_SelVDPFieldInfo.resetFields();
		}
		m_SelVDPFieldInfo = null;
		m_ScalingFactor = ScalingFactor.FIT_PAGE;
		return true;
	}

	// Once the Mouse is Released the Draging index to adjust the GPath is reset
	private void resetMouseDrag() {
		m_DragIndex = NOT_DRAGGING;
	}

	// START FUNCTION FOR PAINT IMPLEMENTATION

	// This variable is necessary to manage image to pan across the Panel Size.
	Rectangle m_OrigPanelSize = null;
	Rectangle m_VisiblePanelRect = null;

	public void paint(Graphics g) {
		boolean setBusyCursor = false;
		m_VisiblePanelRect = this.getVisibleRect();
		Dimension panelSize = new Dimension(
				(int) m_VisiblePanelRect.getWidth(),
				(int) m_VisiblePanelRect.getHeight());
		if (m_XODImageInfo == null)
			return;
		if (m_OrigPanelSize == null)
			m_OrigPanelSize = m_VisiblePanelRect;
		Graphics2D g2d = (Graphics2D) g;
		Graphics buffG = g;
		g2d.clearRect(0, 0, panelSize.width, panelSize.height);

		// PREVIEW Action do not use Double Buffering. Checking if the Action
		// Performed is to Preview
		if (m_ActionPerformedId != null) {
			if (RGPTActionListener.WFActions
					.isShapePreviewAction(m_ActionPerformedId)) {
				RGPTLogger.logToFile("Preview Shape Action Selected");
				processWFPreviewActions(g2d, panelSize);
				return;
			}
		}

		// This loop is executed if the Image is zoomed.
		// Firstly if any objects is maintained in Screen Coords, then image
		// points before zoom is calculated
		// and then new screen points is calculated based on derived ctm after
		// the zoom
		if (m_ImageZoomed) {
			setBusyCursor = true;
			m_XONImageDesignerApp.setCursor(CursorController.m_BusyCursor);
		}

		// Deriving the Screen CTM(Coord Transformation Matrix) based on the
		// panel size during refresh
		if (m_ImageZoomed || m_XODImageInfo.m_WindowDeviceCTM == null)
			m_XODImageInfo.deriveDeviceCTM(m_ScalingFactor, panelSize);

		// Resetting the m_ImageZoomed to false since all the Objects have been
		// recalc.
		if (m_ImageZoomed)
			m_ImageZoomed = false;

		// This activates the scroll bar if needed
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
			buffG = m_BuffDispImage.createGraphics();
			g2d = (Graphics2D) buffG;
		}

		setPanelColor(g2d, panelSize); // Setting the Panel Color Based on the
										// Image
		m_XONImageDesignerApp.updateZoom((int) (m_XODImageInfo.m_Scale * 100));
		XONImagingEngine.createXONImage(g2d, m_XODImageInfo);

		if (m_XONImageDesignerApp.isShapeDesign())
			drawGraphicPath(g2d);

		if (m_ActivateDoubleBuffering) {
			g2d = (Graphics2D) g;
			// Draws the buffered image to the screen.
			g2d.drawImage(m_BuffDispImage, 0, 0, this);
		}
		if (setBusyCursor)
			m_XONImageDesignerApp.resetCursor();
	}

	// Setting the Panel Size based on the Image Being Scaled
	private Dimension setPanelSize(Dimension panelSz) {
		int imgWt = m_XODImageInfo.m_OrigImageWidth;
		int imgHt = m_XODImageInfo.m_OrigImageHeight;
		int wt = (int) (imgWt * m_XODImageInfo.m_Scale);
		int ht = (int) (imgHt * m_XODImageInfo.m_Scale);
		int margin = AppletParameters.getIntVal("PanelMargin");
		int panelWt = wt;
		if (panelSz.width > wt)
			panelWt = panelSz.width;
		int panelHt = ht;
		if (panelHt < m_OrigPanelSize.height)
			panelHt = m_OrigPanelSize.height;
		Point2D.Double ptSrc = new Point2D.Double(imgWt, imgHt), ptDes = null;
		ptDes = (Point2D.Double) m_XODImageInfo.m_WindowDeviceCTM.transform(
				ptSrc, null);
		// RGPTLogger.logToFile("\n\nOrigPanelSize: "+m_OrigPanelSize+" Curr Panel Size: "+
		// panelSz+" Scaled Img Wt: "+wt+" Ht: "+ht+" New PanelWt: "+
		// panelWt+" ht: "+panelHt+" New Pt Dest: "+ptDes);
		Dimension newPanelSize = new Dimension(panelWt, panelHt);
		this.setPreferredSize(newPanelSize);
		this.revalidate();
		return newPanelSize;
	}

	// Setting the Panel Color Based on the Image
	private void setPanelColor(Graphics2D g2d, Dimension panelSize) {
		Color origColor = g2d.getColor();
		g2d.setColor(m_XODImageInfo.m_CanvasImageHolder.m_PanelColor);
		g2d.fillRect(0, 0, panelSize.width, panelSize.height);
		g2d.setColor(origColor);
	}

	// Draws Graphic Path using the points clicked by the user
	private void drawGraphicPath(Graphics g) {
		if (m_GPathPoints.size() == 0) {
			RGPTLogger.logToFile("No Path Pts: ");
			return;
		}
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
		if (m_PathSelectionId == StaticFieldInfo.GraphicsPaths.LINE_PATH)
			m_GPath = RGPTShapeUtil.drawLine(m_ShapeAffine, m_GPathPoints, g);
		else if (m_PathSelectionId == StaticFieldInfo.GraphicsPaths.QUAD_PATH)
			m_GPath = RGPTShapeUtil.drawQuadCurve(m_ShapeAffine, m_GPathPoints,
					g);
		else if (m_PathSelectionId == StaticFieldInfo.GraphicsPaths.CUBIC_PATH)
			m_GPath = RGPTShapeUtil.drawBezierCurve(m_ShapeAffine,
					m_GPathPoints, g);
	}

	private void processWFPreviewActions(Graphics2D g2d, Dimension panelSize) {
		RGPTActionListener.WFActions action = null;
		m_PreviewSelectionId = m_ActionPerformedId;
		action = RGPTActionListener.WFActions.valueOf(m_ActionPerformedId);
		setPanelColor(g2d, panelSize);
		m_XONImageDesignerApp.setCursor(CursorController.m_BusyCursor);
		switch (action) {
		case PREVIEW_SHAPE:
			g2d.draw(m_GPath);
			break;
		}
		m_XONImageDesignerApp.resetCursor();
	}

}