// RGPT PACKAGES
package com.rgpt.viewer;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.rgpt.imageutil.ImageHolder;
import com.rgpt.imageutil.ImageUtils;
import com.rgpt.imageutil.SelectedImageHandler;
import com.rgpt.serverhandler.ApprovalHandler;
import com.rgpt.serverhandler.ApprovePDFRequest;
import com.rgpt.serverhandler.CustomerAssetRequest;
import com.rgpt.serverhandler.EBookPageHandler;
import com.rgpt.serverhandler.GetThemesRequest;
import com.rgpt.serverhandler.ImageHandlerInterface;
import com.rgpt.serverhandler.ImageUploadInterface;
import com.rgpt.serverhandler.PDFPageHandler;
import com.rgpt.serverhandler.PDFViewInterface;
import com.rgpt.serverutil.PDFPageInfoManager;
import com.rgpt.templateutil.PDFPageInfo;
import com.rgpt.templateutil.PDFPersonalizationUtil;
import com.rgpt.templateutil.UserPageData;
import com.rgpt.templateutil.VDPImageFieldInfo;
import com.rgpt.templateutil.VDPTextFieldInfo;
import com.rgpt.templateutil.VDPUIHolder;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTRectangle;
import com.rgpt.util.RGPTThreadWorker;
import com.rgpt.util.RGPTUtil;
import com.rgpt.util.ScalingFactor;
import com.rgpt.util.StaticFieldInfo;
import com.rgpt.util.TextFormatter;
import com.rgpt.util.ThreadInvokerMethod;
import com.rgpt.util.ThumbViewImageHandler;

// The PDFPageViewHandler Displays the PDF Page Content. The User Interface is 
// contained inside the Content Panel which also holds PDFPageViewer Object.
public class PDFPageViewHandler extends JPanel implements PDFPageHandler,
		MouseListener, MouseMotionListener, KeyListener, SelectedImageHandler,
		ApprovalHandler, ThreadInvokerMethod {

	// This holds all the PDF Page Information
	PDFPageInfoManager m_PDFPageInfoManager;

	// Container having this Panel as a Content Pane
	Container m_PDFViewContainer;

	// Image Dimension
	int m_ImageWidth;
	int m_ImageHeight;

	// Option Index to indicate the Mode to display image. They are
	// 0 - Normal - Here the Image is Displayed As Is. This is the default mode.
	// 1 - Fit Page
	// 2 - Fit Width,
	// Zoom In or Zoom Out
	int m_OptionIndex;

	// PDF Page Information
	PDFPageInfo m_PDFPageInfo;
	HashMap m_PageVDPFields;
	ScalingFactor m_ScalingFactor;

	// This is the HashMap of User Assets downloaded from the server
	HashMap m_ImageAssets;

	// This contains the User Defined Values for VDP Data in the Page. Eash Key
	// is the Page Number and holds the corresponding UserPageData Object.
	HashMap m_UserPageData;

	// This holds the complete user interface screen including this object
	JPanel m_ContentPane;

	// This holds info of the Previous Page Number
	int m_PrevPageNum;

	// PDFViewInterface to get High Quality PDF Page as Image
	PDFViewInterface m_PDFView;

	// BufferedImage for this Page
	BufferedImage m_BufferedImage;

	// Auality Mode is defined in PDFViewInterface. Which can be Regular,
	// High or Best. The default is set to Regular
	int m_QualityMode;

	// This boolean value indicates if the Image Assets is populated from the
	// server
	boolean m_ImageAssetsPopulated;

	// This will be indicated
	public final static int EDIT_MODE = 0;

	// This will be indicated by Preview Button
	public final static int JAVA_PREVIEW_MODE = 1;

	// This mode will be indicated by View PDF in PC and Save PDF in Web
	public final static int PDF_PREVIEW_MODE = 2;

	// This mode will be indicated when user wants to resize or reposition text
	public final static int RESIZE_MODE = 3;

	// This mode will be indicated when user wants to resize or reposition text
	public final static int MOVE_MODE = 4;

	// This mode will be indicated when user wants to resize or reposition text
	public final static int RESIZE_MOVE_MODE = 5;

	// This mode will be indicated when user wants to allign or deleted VDP
	// Field
	public final static int SELECT_MODE = 6;

	// This mode will be indicated when user has selected Image to be dragged
	public final static int SELECT_IMAGE_MODE = 7;

	// This mode will be indicated when user has selected Image to be dragged
	public final static int SELECT_CROP_IMAGE = 8;

	// This mode will be indicated when user has selected Image to be dragged
	public final static int SHOW_FULL_IMAGE = 9;

	// This mode will be indicated when user has selected individual Field to be
	// resized
	public final static int RESIZE_MOVE_FIELD_MODE = 10;

	// This indicate one of the above View Modes,
	public int m_ViewMode;

	// Image Uploader Interface. This is used to loads Images for the user
	ImageUploadInterface m_ImageUploader;

	// This Text Field Displays the Page Number
	JTextField m_PageNumField;

	// Buttons in Toolbar defined in PDFViewer and registered through
	// PDFPageHandler
	JButton m_EditButton, m_GenPDFButton, m_SavePDFWorkButton,
			m_FinishedButton, m_ApproveButton, m_ResizeMoveButton,
			m_DeleteVDPButton, m_SelectVDPButton;

	// Specifies PDF Zoom Percentage
	private JComboBox m_ZoomUIList;

	// Text Area used to read the text typed for VDPTextField
	JTextArea m_VDPTextArea;

	EBookInterface m_EBookViewer;

	// Tooltip for Text Entry and Image Upload
	JToolTip m_ToolTip;

	public PDFPageViewHandler(Container pdfViewContainer,
			PDFViewInterface pdfView, ImageUploadInterface imgUploader,
			PDFPageInfoManager pdfPageInfoManager, EBookInterface eBookViewer) {
		m_PrevPageNum = 0;
		m_OptionIndex = 0;

		m_UserPageData = new HashMap();
		m_PageVDPFields = new HashMap();

		m_PDFView = pdfView;
		m_EBookViewer = eBookViewer;
		m_ImageUploader = imgUploader;
		m_PDFViewContainer = pdfViewContainer;
		m_PDFPageInfoManager = pdfPageInfoManager;

		// Setting by efault to Edit Mode
		m_ViewMode = EDIT_MODE;

		// Calculating the Scaling, Translation and the CTM for the
		// ScalingFactor.
		// This is set innitally to FIT_PAGE.
		m_ScalingFactor = ScalingFactor.FIT_PAGE;

		m_ImageAssetsPopulated = false;
		this.addMouseListener(this);
		this.addMouseMotionListener(this);

		this.getInputMap()
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0, true),
						"showKey");
		Action showKey = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("VK_PAGE_UP");
			}
		};
		this.getActionMap().put("showKey", showKey);
		// this.setFocusable(true);
		// this.addKeyListener(this);

		populateApprovalDialog();

		m_ToolTip = this.createToolTip();

		// Set the Layout to Border Layout
		m_ContentPane = new JPanel();
		m_ContentPane.setLayout(new BorderLayout());
		m_PDFViewContainer.repaint();
		// this.createContentPanel();
	}

	public void reapintViewer() {
		m_PDFViewContainer.repaint();
	}

	// This initialization has no significance to UI. The Actual UI Component
	// References are provided by PDFViewer classes as and when and which
	// ever needed.
	public void doDummyInitialization() {
		// This Text Field Displays the Page Number
		m_PageNumField = new JTextField();

		// Buttons in Toolbar defined in PDFViewer and registered through
		// PDFPageHandler
		m_EditButton = new JButton();
		m_GenPDFButton = new JButton();
		m_SavePDFWorkButton = new JButton();
		m_FinishedButton = new JButton();
		m_ApproveButton = new JButton();
		m_ResizeMoveButton = new JButton();
		m_DeleteVDPButton = new JButton();
		m_SelectVDPButton = new JButton();
	}

	public static int USE_THUMBVIEW_IMAGE_MODE;
	public ThumbViewImageHandler m_ThumbViewImageHandler = null;

	private BufferedImage m_BackgroundImage;
	private BufferedImage m_ProgBarImage;

	public void setBackgroundImage(BufferedImage backGrdImg) {
		m_BackgroundImage = backGrdImg;
	}

	public void setProgBarImage(BufferedImage progBarImg) {
		RGPTLogger.logToFile("Setting the Progress Bar Image with Wt: "
				+ progBarImg.getWidth() + " Ht: " + progBarImg.getHeight());
		m_ProgBarImage = progBarImg;
		m_PDFViewContainer.repaint();
	}

	PDFViewer m_PDFViewer;

	public void initPDFPageHandler(PDFViewer pdfViewer, int thumbImgViewMode,
			BufferedImage progBarImg, BufferedImage backGrdImg) {
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

		// This initialization has no significance to UI. The Actual UI
		// Component
		// References are provided by PDFViewer classes. The ideal way is to
		// check
		// if the Objects are null before assignment since this is not done,
		// this is the
		// dirty way.
		this.doDummyInitialization();

		m_PDFViewer = pdfViewer;
		m_ProgBarImage = progBarImg;
		m_BackgroundImage = backGrdImg;
		USE_THUMBVIEW_IMAGE_MODE = thumbImgViewMode;

		// Populating the PDFPageInfo Object
		m_PDFPageInfo = m_PDFPageInfoManager.firstPage();

		// Populating the User Page Data with Default Values for all pages
		this.populateAllUserPageData();

		// Enabling and Disabling Buttons based on PDF Page Template retieved
		this.checkToEnableButtons();

		m_QualityMode = PDFViewInterface.REGULAR_QUALITY_PDF;
		m_BufferedImage = m_PDFPageInfo.m_BufferedImage;
		m_ImageWidth = m_PDFPageInfo.m_BufferedImage.getWidth(null);
		m_ImageHeight = m_PDFPageInfo.m_BufferedImage.getHeight(null);
		RGPTLogger.logToFile("Loaded Buffered Image for First Page: "
				+ " PDF Page Image Wt: " + m_ImageWidth + " Image Ht: "
				+ m_ImageHeight);

		// This is done if the this Panel is running inside an Applet and Making
		// Server calls

		// Loading the Thumbview Images in the Background thread and invoking
		// the Frame to load the thumbviews

		// SwingUtilities provides a collection of utility methods for Swing.
		// The invokeLater Causes doRun.run() to be executed asynchronously on
		// the
		// AWT event dispatching thread. This will happen after all pending AWT
		// events havebeen processed. This method should be used when an
		// application
		// thread needs to update the GUI.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				populateImageAssets();
			}
		});
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		m_PDFViewContainer.repaint();
	}

	private void populateImageAssets() {
		int downloadMode = -1;
		HashMap servResp = null;
		HashMap imageAssets = null;
		Map<Integer, HashMap> templateThemesInfo = null;
		if (m_PDFPageInfoManager.m_Mode == m_PDFPageInfoManager.STANDALONE_MODE) {
			imageAssets = ImageViewer.loadImages();
			downloadMode = ImageHandlerInterface.DESKTOP_IMAGES;
		} else if (m_PDFPageInfoManager.m_Mode == m_PDFPageInfoManager.SERVER_MODE) {
			// Loading all the Themes Supported in this Template. This call
			// retrieves
			// Theme Icon and Name used for display purposes
			if (VDPImageFieldInfo.m_TemplateThemes.size() > 0) {
				servResp = m_PDFPageInfoManager.getThemeData(
						GetThemesRequest.GET_THEME_DATA,
						VDPImageFieldInfo.m_TemplateThemes);
				templateThemesInfo = ((Map<Integer, HashMap>) servResp
						.get("Themes"));
			}

			// Loading Images Stored in the Server by the User
			imageAssets = loadImages();
			downloadMode = ImageHandlerInterface.SERVER_IMAGES;
		}
		// Image Assets are not populated with Enlarged Image. Hence a Server
		// call is needed to retrieve enlarged Image. Hence useImageAssets is
		// set to false.
		if (USE_THUMBVIEW_IMAGE_MODE == PDFPageHandler.THUMBVIEW_IMAGE_FRAME) {
			// This is an Internal Fram to load and Select Thumbview Images
			ThumbViewImageFrame thumbViewImageFrame;
			thumbViewImageFrame = new ThumbViewImageFrame(this, imageAssets,
					m_ImageUploader, downloadMode, templateThemesInfo,
					m_PDFPageInfoManager);
			thumbViewImageFrame.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
			// thumbViewImageFrame.getContentPane().setBackground(Color.GREEN);
			thumbViewImageFrame.setLocation(15, 15);
			m_ThumbViewImageHandler = thumbViewImageFrame;
			// thumbViewImageFrame.pack();
			// thumbViewImageFrame.setVisible(false);
			m_ThumbViewImageHandler.setVisibility(false);
		} else if (USE_THUMBVIEW_IMAGE_MODE == PDFPageHandler.THUMBVIEW_IMAGE_PANE) {
			ThumbViewImagePanel thumbViewImagePane = null;
			thumbViewImagePane = new ThumbViewImagePanel(this, imageAssets,
					m_ImageUploader, downloadMode, templateThemesInfo,
					m_PDFPageInfoManager);
			System.out.println("ThumbViewImagePanel Instantiated: "
					+ thumbViewImagePane);
			m_ThumbViewImageHandler = thumbViewImagePane;
			// m_ThumbViewImagePane.setVisible(false);
			m_ContentPane.add(thumbViewImagePane, BorderLayout.SOUTH);
			m_ThumbViewImageHandler.setVisibility(false);
		}
		m_ImageAssetsPopulated = true;
		RGPTLogger.logToFile("Finish Populating Image Assets: "
				+ m_ImageAssetsPopulated);

		if (m_PDFPageInfoManager.m_Mode == m_PDFPageInfoManager.STANDALONE_MODE)
			return;
		// Retriving the Thumbview Images for the theme in a new Thread in
		// Server Mode
		if (VDPImageFieldInfo.m_TemplateThemes.size() == 0)
			return;
		RGPTThreadWorker threadWorker = null;
		HashMap requestData = new HashMap();
		requestData.put("RequestType", "LoadThemeImages");
		threadWorker = new RGPTThreadWorker(Thread.MIN_PRIORITY, this,
				requestData);
		threadWorker.startThreadInvocation();
	}

	ApprovalDialog m_ApprovalDialog;

	private void populateApprovalDialog() {
		String[] approvalMesgs = { "Approve PDF and Exit", "PDF Not Approved",
				"Decide Later" };
		m_ApprovalDialog = new ApprovalDialog(approvalMesgs, this);
		m_ApprovalDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		m_ApprovalDialog.setLocation(200, 200);
		m_ApprovalDialog.setVisible(false);
	}

	// Invoked when a key has been pressed.
	public void keyPressed(KeyEvent e) {
		System.out.println("Key Pressed: " + this.getKeyString(e));
		int keyCode = e.getKeyCode();
		if (keyCode == 16)
			m_ShiftKeyPressed = true;
		// else m_ShiftKeyPressed = false;
	}

	private boolean m_ShiftKeyPressed = false;

	// Invoked when a key has been released.
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == 18)
			return;
		if (m_SelectedVDPData == null)
			return;
		System.out.println("Char: " + e.getKeyChar() + " CharCode: "
				+ e.getKeyCode());
		System.out.println("KeyEvent: " + e.paramString());
		System.out.println("KeyReleased: " + this.getKeyString(e));
		// System.out.println("Text Area Size: " +
		// m_VDPTextArea.getSize(null).toString());
		String vdpTxt = m_VDPTextArea.getText();
		System.out.println("VDP Text Area: " + vdpTxt);
		// String vdpTxt = (String) m_SelectedVDPData.get("UserSetValue");
		// if (vdpTxt == null) vdpTxt = "";
		// char c = vdpTxt.charAt(vdpTxt.length()-1);
		char c = e.getKeyChar();
		int charCode = e.getKeyCode();
		m_SelectedVDPData.put("BlankDefaultText", false);
		if (c == '\t') {
			if (m_ShiftKeyPressed)
				this.getVdpData(false);
			else
				handleNextVdpData();
			return;
		}

		// Shift Key is Released
		if (charCode == 16) {
			m_ShiftKeyPressed = false;
			return;
		}
		// Replacing new line with space if text mode is Line. Nothing is done
		// if
		// it Word and new line is processed if it Para.
		int vdpTxtMode = ((Integer) m_SelectedVDPData.get("VDPTextMode"))
				.intValue();
		if (c == '\n') {
			if (!(vdpTxtMode == StaticFieldInfo.PARA)) {
				handleNextVdpData();
				return;
			}
		}
		HashMap vdpData = m_SelectedVDPData;
		boolean fldLthFixed = ((Boolean) vdpData.get("FieldLengthFixed"))
				.booleanValue();
		boolean txtWdthFixed = ((Boolean) vdpData.get("TextWidthFixed"))
				.booleanValue();
		boolean isOverFlowField = ((Boolean) vdpData.get("IsOverFlowField"))
				.booleanValue();
		String overFlowVDPField = (String) vdpData.get("OverFlowVDPField");
		int fldLth = Integer.parseInt((String) vdpData.get("FieldLength"));
		String dataType = (String) vdpData.get("TextDataType");
		if (!vdpTxt.trim().isEmpty() && dataType.equals("NUMBER")) {
			try {
				long numEnt = Long.parseLong(vdpTxt);
			} catch (Exception ex) {
				vdpTxt = vdpTxt.substring(0, vdpTxt.length() - 1);
				m_VDPTextArea.setText(vdpTxt);
				String mesg = "Please Enter only Numbers";
				JOptionPane.showMessageDialog(m_PDFViewContainer, mesg,
						"Incorrect Entry", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		if (overFlowVDPField.length() == 0) {
			if (fldLthFixed) {
				String prefixVal = (String) vdpData.get("PrefixValue");
				String suffixVal = (String) vdpData.get("SuffixValue");
				fldLth = fldLth - prefixVal.length() - suffixVal.length();
				if (vdpTxt.length() > fldLth) {
					vdpTxt = vdpTxt.substring(0, vdpTxt.length() - 1);
					m_VDPTextArea.setText(vdpTxt);
					return;
				}
			} else if (txtWdthFixed) {
				RGPTRectangle devBBox = (RGPTRectangle) vdpData
						.get("DeviceBBox");
				Font font = (Font) vdpData.get("DerivedFont");
				java.awt.FontMetrics fm = Toolkit.getDefaultToolkit()
						.getFontMetrics(font);
				String prefixVal = (String) vdpData.get("PrefixValue");
				String suffixVal = (String) vdpData.get("SuffixValue");
				String text = prefixVal + vdpTxt + suffixVal;
				int width = fm.stringWidth(text);
				if (width > devBBox.width) {
					vdpTxt = vdpTxt.substring(0, vdpTxt.length() - 1);
					m_VDPTextArea.setText(vdpTxt);
					return;
				}
			}
		}
		Rectangle2D.Double bbox = ((RGPTRectangle) vdpData.get("BBox"))
				.getRectangle2D();
		VDPTextFieldInfo vdpTextFieldInfo = null;
		vdpTextFieldInfo = (VDPTextFieldInfo) m_PDFPageInfo.getVDPFieldInfo(
				"Text", bbox);
		if (vdpTextFieldInfo != null) {
			int formatType = vdpTextFieldInfo.m_TextFormatType;
			String formatSpec = vdpTextFieldInfo.m_TextFormatValue;
			try {
				String formTxt = TextFormatter.getFormattedText(vdpTxt,
						formatType, formatSpec);
			} catch (Exception ex) {
				vdpTxt = vdpTxt.substring(0, vdpTxt.length() - 1);
				m_VDPTextArea.setText(vdpTxt);
				String mesg = ex.getMessage();
				JOptionPane.showMessageDialog(m_PDFViewContainer, mesg,
						"Incorrect Entry", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		m_SelectedVDPData.put("UserSetValue", vdpTxt);
		System.out.println("New Text Entered: " + vdpTxt);
		// If Back Space is Enetered then the whole graphic area is refreshed.
		if (charCode == 8 || m_CntrKeyActivated
				|| vdpTxtMode == StaticFieldInfo.PARA) {
			m_PDFViewContainer.repaint();
			m_CntrKeyActivated = false;
			return;
		}
		if (charCode == 17) {
			m_CntrKeyActivated = true;
			return;
		}

		// Graphics2D g2d = (Graphics2D)this.getGraphics();
		boolean drawSelVDPTextOnly = true;
		Graphics2D g2d = (Graphics2D) this.getGraphics();

		UserPageData userPgData = null;
		Object obj = m_UserPageData.get(new Integer(m_PDFPageInfo.m_PageNum));
		if (obj == null)
			return;
		userPgData = (UserPageData) obj;

		PDFPersonalizationUtil persUtil = new PDFPersonalizationUtil(true);
		persUtil.processVDPTextFields(g2d, m_PDFPageInfo, userPgData,
				m_MultiWordSelLine, drawSelVDPTextOnly, m_SelectedVDPData,
				m_ViewMode, null, m_VDPTextArea);
	}

	boolean m_CntrKeyActivated = false;

	// Invoked when a key has been typed.
	public void keyTyped(KeyEvent e) {
		// System.out.println("Key Typed: " + this.getKeyString(e));
	}

	private String getKeyString(KeyEvent e) {
		int id = e.getID();
		String keyString;
		if (id == KeyEvent.KEY_TYPED) {
			char c = e.getKeyChar();
			keyString = "key character = '" + c + "'";
		} else {
			int keyCode = e.getKeyCode();
			keyString = "key code = " + keyCode + " ("
					+ KeyEvent.getKeyText(keyCode) + ")";
		}
		return keyString;
	}

	public void handleNextVdpData() {
		this.getVdpData(true);
	}

	public void getVdpData(boolean isNext) {
		UserPageData userPgData = null;
		Object obj = m_UserPageData.get(new Integer(m_PDFPageInfo.m_PageNum));
		if (obj == null)
			return;
		userPgData = (UserPageData) obj;
		HashMap vdpData = null;
		int currCounter = -1;
		if (m_SelectedVDPData != null) {
			currCounter = ((Integer) m_SelectedVDPData.get("Counter"))
					.intValue();
			if (isNext)
				currCounter = currCounter + 1;
			else
				currCounter = currCounter - 1;
		} else
			RGPTLogger.logToFile("No VDP Data Selection. Counter: "
					+ currCounter);
		vdpData = userPgData.getNextVDPData(currCounter);
		if (vdpData == null)
			return;
		// String fldType = (String) vdpData.get("FieldType");
		// if (fldType.equals("Text"))
		// {
		boolean isAllowEdit = ((Boolean) vdpData.get("AllowFieldEdit"))
				.booleanValue();
		if (!isAllowEdit)
			return;
		boolean isFieldFixed = ((Boolean) vdpData.get("IsFieldFixed"))
				.booleanValue();
		if (isFieldFixed)
			return;
		this.populateTextFieldData();
		m_SelectedVDPData = vdpData;
		this.activateVDPTextArea();
		m_PDFViewContainer.repaint();
		return;
		// }

		// Handling the Image Field Type
		/*
		 * m_VDPTextArea.setEnabled(false); m_VDPTextArea.setEditable(false);
		 * this.populateVDPImage(vdpData); m_PDFViewContainer.repaint(); return;
		 */
	}

	public void activateVDPTextArea() {
		// System.out.println("In Activate VDPTextArea: " +
		// m_SelectedVDPData.toString());
		m_SelectedVDPData.put("VDPViewMode", -1);
		m_ViewMode = EDIT_MODE;
		m_VDPTextArea.setEnabled(true);
		m_VDPTextArea.setFocusable(true);
		m_VDPTextArea.setCursor(Cursor.getDefaultCursor());
		m_VDPTextArea.setEditable(true);
		Object vdpTextObj = m_SelectedVDPData.get("UserSetValue");
		String vdpText = "";
		if (vdpTextObj != null)
			vdpText = (String) vdpTextObj;
		// vdpText = vdpText.trim();
		m_VDPTextArea.setText(vdpText);
		m_VDPTextArea.requestFocus();
		m_TextScrollPane.setVisible(true);

		// Setting the View Port
		resetViewPort();
	}

	private void calcVDPImageDisplay(double endPtX, double endPtY) {
		System.out.println("In calcVDPImageDisplay Method");
		HashMap vdpData = m_SelectedVDPData;
		ImageHolder imgHldr = (ImageHolder) vdpData.get("ImageHolder");
		double startPtX = ((Double) vdpData.get("StartImgPtX")).doubleValue();
		double startPtY = ((Double) vdpData.get("StartImgPtY")).doubleValue();
		double dispx = imgHldr.m_DisplayRect.getX(), dispy = imgHldr.m_DisplayRect
				.getY(), deltax = 0.0, deltay = 0.0;
		// double wt = imgHldr.m_DisplayRect.getWidth(),
		// ht = imgHldr.m_DisplayRect.getHeight();
		double wt = imgHldr.m_DeviceBBox.width, ht = imgHldr.m_DeviceBBox.height;
		// double wt = (double) imgHldr.m_DisplayRect.width,
		// ht = (double) imgHldr.m_DisplayRect.height;
		System.out.println("\nPRIOR VALUES.. Start Ptx : " + startPtX
				+ " :StartPtY: " + startPtY);
		System.out.println("EndPtX: " + endPtX + " :EndPtY: " + endPtY);
		System.out.println("Disp X: " + dispx + " :DispY: " + dispy);
		if (imgHldr.m_ScalingMode == ImageHolder.SCALED_ALONG_HEIGHT) {
			deltax = endPtX - startPtX;
			if ((dispx + deltax + wt) < imgHldr.m_ScaledImage.getWidth()
					&& (dispx + deltax) > 0) {
				dispx = dispx + deltax;
				imgHldr.m_DisplayRect.setLocation((int) dispx, 0);
			}
		} else if (imgHldr.m_ScalingMode == ImageHolder.SCALED_ALONG_WIDTH) {
			deltay = startPtY - endPtY;
			if ((dispy + deltay + ht) < imgHldr.m_ScaledImage.getHeight()
					&& (dispy + deltay) > 0) {
				dispy = dispy + deltay;
				imgHldr.m_DisplayRect.setLocation(0, (int) dispy);
			}
		}
		imgHldr.m_DisplayRect.setSize((int) wt, (int) ht);
		System.out.println("Delta X: " + deltax + " :DeltaY: " + deltay);
		System.out.println("Disp X: " + dispx + " :DispY: " + dispy);
		m_PDFViewContainer.repaint();
	}

	public void handleCropImage(int x, int y) {
		HashMap vdpData = m_SelectedVDPData;
		ImageHolder imgHldr = (ImageHolder) vdpData.get("ImageHolder");
		BufferedImage origImg = (BufferedImage) vdpData.get("UserSetImage");
		if (!imgHldr.m_DeviceBBox.contains(x, y))
			return;
		Graphics g = this.getGraphics();
		// Graphics g = getGraphics();
		g.setXORMode(Color.lightGray);
		// Drawing Rectangle twice gives the Rubberband Effect
		System.out.println("Rubbing the old Rect at StartX: "
				+ m_ImageSelStartX + " :StartY: " + m_ImageSelStartY
				+ " :EndX: " + m_ImageSelEndX + " :EndY: " + m_ImageSelEndY);
		drawRectangle(g, m_ImageSelStartX, m_ImageSelStartY, m_ImageSelEndX,
				m_ImageSelEndY);
		double aspectRatio = imgHldr.m_DeviceBBox.height
				/ imgHldr.m_DeviceBBox.width;
		// New code to Drag along the Aspect Ratio
		int w = x - m_ImageSelStartX;
		int h = (int) (w * aspectRatio);
		y = m_ImageSelStartY + h;
		// if (x > m_SelectedImageObject.m_ScaledImage.getWidth() ||
		// y > m_SelectedImageObject.m_ScaledImage.getHeight())
		if (!imgHldr.m_DeviceBBox.contains((double) x, (double) y)) {
			x = m_ImageSelEndX;
			y = m_ImageSelEndY;
		}
		// x = x - (int) m_DisplaySize.getX();
		// x = y - (int) m_DisplaySize.getY();
		System.out.println("Mouse Dragged x: " + x + " :y: " + y);
		g.setXORMode(Color.lightGray);
		drawRectangle(g, m_ImageSelStartX, m_ImageSelStartY, x, y);
		m_ImageSelEndX = x;
		m_ImageSelEndY = y;
		double scImgWt = imgHldr.m_ScaledImage.getWidth(), scImgHt = imgHldr.m_ScaledImage
				.getHeight();
		scImgWt = (double) origImg.getWidth();
		scImgHt = (double) origImg.getHeight();
		double scalex = scImgWt / imgHldr.m_DeviceBBox.width;
		double scaley = scImgHt / imgHldr.m_DeviceBBox.height;
		double dispx = (m_ImageSelStartX - imgHldr.m_DeviceBBox.x) * scalex;
		double dispy = (m_ImageSelStartY - imgHldr.m_DeviceBBox.y) * scaley;
		double wt = (m_ImageSelEndX - m_ImageSelStartX) * scalex;
		double ht = (m_ImageSelEndY - m_ImageSelStartY) * scaley;
		// Map<String, Integer> scImgSize =
		// RGPTUtil.calcScaledImageSize(scImgWt, scImgHt,
		// wt, ht);
		// imgHldr.m_DisplayRect = new Rectangle((int)dispx, (int)dispy,
		// scImgSize.get("Width").intValue(),
		// scImgSize.get("Height").intValue());
		imgHldr.m_DisplayClipRect = new Rectangle((int) dispx, (int) dispy,
				(int) wt, (int) ht);
		imgHldr.m_DisplayRect = new Rectangle(0, 0,
				(int) imgHldr.m_DeviceBBox.width,
				(int) imgHldr.m_DeviceBBox.height);
	}

	// Draw the rectangle, adjusting the x, y, w, h to correctly accommodate for
	// the opposite corner of the rubberband box relative to the start position.
	private void drawRectangle(Graphics g, int startX, int startY, int stopX,
			int stopY) {
		int x, y, w, h;
		// g = m_DisplayLabel.getGraphics();
		x = Math.min(startX, stopX);
		y = Math.min(startY, stopY);
		w = Math.abs(startX - stopX);
		h = Math.abs(startY - stopY);
		System.out.println("drawRectangle startx: " + x + " :y: " + y
				+ " :Endx: " + stopX + " :EndY: " + stopY + " :Width: " + w
				+ " :Ht: " + h);
		g.drawRect(x, y, w, h);
	}

	// This draws a rubberband rectangle to reseze the VDP Element.
	public void mouseDragged(MouseEvent e) {
		double x = e.getX(), y = e.getY();
		// RGPTLogger.logToFile("MouseDragged. X: " + x + " Y: " + y);

		// This part of the code is to drag the view area of the Selected VDP
		// Image
		if (m_ViewMode == SELECT_IMAGE_MODE) {
			calcVDPImageDisplay(x, y);
			return;
		}

		if (m_ViewMode == SELECT_CROP_IMAGE) {
			handleCropImage((int) x, (int) y);
			return;
		}

		// This part of the code is for resize the VDP Text Field
		boolean procMouseAction = false;
		Vector vdpSelData = null;
		// if (m_ViewMode == RESIZE_MOVE_MODE || m_ViewMode == RESIZE_MODE ||
		if (m_ViewMode == RESIZE_MODE || m_ViewMode == MOVE_MODE
				|| m_ViewMode == RESIZE_MOVE_FIELD_MODE)
			procMouseAction = true;
		if (!procMouseAction)
			return;

		// This case is executed if the element was not properly selected.
		if (!m_ResizeMoveModeSel)
			return;

		HashMap vdpData = m_SelectedVDPData;
		// Resize and Move are only supported for VDP Text and that to for Line
		// or Word Mode only.
		if (!((String) vdpData.get("FieldType")).equals("Text"))
			return;
		int vdpTextMode = ((Integer) vdpData.get("VDPTextMode")).intValue();
		if (vdpTextMode == StaticFieldInfo.PARA)
			return;

		// m_Graphics2D DeviceBBox SelPt
		Rectangle2D.Double bbox = ((RGPTRectangle) vdpData.get("DeviceBBox"))
				.getRectangle2D();
		RGPTLogger.logToFile("VDP Bfor Fld BBox: " + bbox.toString());

		Object newBBoxObj = vdpData.get("NewDeviceBBox");
		if (newBBoxObj != null) {
			bbox = ((RGPTRectangle) newBBoxObj).getRectangle2D();
		}
		int viewFldMode = ((Integer) vdpData.get("VDPViewMode")).intValue();
		RGPTLogger.logToFile("VDP After Fld BBox: " + bbox.toString());
		if (m_ViewMode == RESIZE_MODE || viewFldMode == RESIZE_MODE)
			this.handleReseize(x, y, bbox, vdpData);
		else if (m_ViewMode == MOVE_MODE || viewFldMode == MOVE_MODE)
			this.handleMove(x, y, bbox, vdpData);
	}

	private void handleReseize(double x, double y, Rectangle2D.Double bbox,
			HashMap vdpData) {
		if (vdpData.get("SelPt") == null) {
			RGPTLogger.logToFile("No Pt Selected");
			return;
		}
		m_Graphics2D = (Graphics2D) this.getGraphics();
		m_Graphics2D.setXORMode(Color.lightGray);
		m_Graphics2D.draw(bbox);
		RGPTLogger.logToFile("Pt Sel for Dragging In MouseDrag: "
				+ vdpData.get("SelPt").toString());
		int selPt = ((Integer) vdpData.get("SelPt")).intValue();
		RGPTLogger.logToFile("Sel Pt: " + selPt);
		double x1 = bbox.getX(), y1 = bbox.getY(), w = bbox.getWidth(), h = bbox
				.getHeight();
		double newX = x, newY = y1, newW = w, newH = h;
		if (selPt == 1 || selPt == 2) {
			if (x1 > x)
				newW = w + (x1 - x);
			else if (x1 < x)
				newW = w - (x - x1);
		} else if (selPt == 3 || selPt == 4) {
			newX = x1;
			double x2 = newX + w;
			if (x2 > x)
				newW = w - (x2 - x);
			else if (x2 < x)
				newW = w + (x - x2);
		}
		Rectangle2D.Double newBBox = new Rectangle2D.Double(newX, newY, newW,
				newH);
		RGPTLogger.logToFile("VDP Fld New Device BBox: " + newBBox.toString());
		vdpData.put("NewDeviceBBox", RGPTRectangle.getReactangle(newBBox));
		m_Graphics2D.setXORMode(Color.lightGray);
		m_Graphics2D.draw(newBBox);
	}

	private void handleMove(double x, double y, Rectangle2D.Double bbox,
			HashMap vdpData) {
		m_Graphics2D = (Graphics2D) this.getGraphics();
		m_Graphics2D.setXORMode(Color.lightGray);
		m_Graphics2D.draw(bbox);

		// Calculating the new BBox based on Mouse Moved
		double dragx = ((Double) vdpData.get("MouseDragX")).doubleValue();
		double dragy = ((Double) vdpData.get("MouseDragY")).doubleValue();
		double deltax = x - dragx;
		double deltay = y - dragy;
		double x1 = bbox.getX(), y1 = bbox.getY(), w = bbox.getWidth(), h = bbox
				.getHeight();
		double newX = x1 + deltax, newY = y1 + deltay, newW = w, newH = h;
		Rectangle2D.Double newBBox = new Rectangle2D.Double(newX, newY, newW,
				newH);
		RGPTLogger.logToFile("VDP Fld New Device BBox: " + newBBox.toString());
		vdpData.put("NewDeviceBBox", RGPTRectangle.getReactangle(newBBox));
		m_Graphics2D.setXORMode(Color.lightGray);
		m_Graphics2D.draw(newBBox);
		vdpData.put("MouseDragX", x);
		vdpData.put("MouseDragY", y);
	}

	public void mouseMoved(MouseEvent e) {
		if (m_PDFActionComps != null) {
			int actionId = containsActionComp(e.getX(), e.getY());
			if (actionId != -1)
				this.setCursor(new Cursor(Cursor.HAND_CURSOR));
			else
				this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		if (m_PDFProofGenerated)
			return;
		double x = e.getX();
		double y = e.getY();
		UserPageData userPgData = null;
		if (m_PDFPageInfo == null)
			return;
		Object obj = m_UserPageData.get(new Integer(m_PDFPageInfo.m_PageNum));
		if (obj == null) {
			m_ToolTip.setVisible(false);
			return;
		}
		userPgData = (UserPageData) obj;
		HashMap vdpData = null;
		Vector vdpSelData = userPgData.getVDPData("Text", x, y);
		// Only taking the First Element in case of Text
		if (vdpSelData.size() > 0)
			vdpData = (HashMap) vdpSelData.elementAt(0);
		if (vdpData != null) {
			String fldName = (String) vdpData.get("VDPFieldName");
			this.setToolTipText("Enter " + fldName);
			m_ToolTip.setVisible(true);
			return;
		}
		vdpSelData = userPgData.getVDPData("Image", x, y);
		if (vdpSelData.size() == 0) {
			m_ToolTip.setVisible(false);
			return;
		}
		if (vdpSelData.size() > 1) {
			vdpData = userPgData.getContainedVDPField(vdpSelData);
			if (vdpData == null)
				vdpData = (HashMap) vdpSelData.elementAt(0);
		} else
			vdpData = (HashMap) vdpSelData.elementAt(0);
		if (vdpData != null) {
			String fldName = (String) vdpData.get("VDPFieldName");
			this.setToolTipText("Click to add " + fldName);
			m_ToolTip.setVisible(true);
			return;
		}
		m_ToolTip.setVisible(false);
	}

	private int containsActionComp(int x, int y) {
		if (m_PDFActionComps == null)
			return -1;
		Integer[] actionIds = m_PDFActionComps.keySet().toArray(new Integer[0]);
		Rectangle compRect = null;
		for (int i = 0; i < actionIds.length; i++) {
			compRect = m_PDFActionComps.get(actionIds[i]);
			if (!compRect.contains(x, y))
				continue;
			return actionIds[i].intValue();
		}
		return -1;
	}

	// This indicates if any VDP Element is Selected for Resize
	boolean m_ResizeMoveModeSel = false;

	public void mouseReleased(MouseEvent e) {
		if (m_ViewMode == SELECT_IMAGE_MODE || m_ViewMode == SELECT_CROP_IMAGE) {
			int viewMode = ((Integer) m_SelectedVDPData.get("VDPViewMode"))
					.intValue();
			if (viewMode == SELECT_CROP_IMAGE) {
				PDFPersonalizationUtil persUtil = new PDFPersonalizationUtil(
						true);
				persUtil.getScaledImage(m_SelectedVDPData);
			}
			m_ViewMode = EDIT_MODE;
			m_SelectedVDPData.put("VDPViewMode", -1);
			m_PDFViewContainer.repaint();
			m_ShowNewPDFThumbImage = true;
			return;
		}
		boolean procMouseAction = false;
		if (m_ViewMode == RESIZE_MOVE_MODE || m_ViewMode == RESIZE_MODE
				|| m_ViewMode == MOVE_MODE
				|| m_ViewMode == RESIZE_MOVE_FIELD_MODE)
			procMouseAction = true;
		if (!procMouseAction)
			return;

		// This case is executed if the element was not properly selected.
		if (!m_ResizeMoveModeSel)
			return;

		// At this stage the VDP Element is selected and released and
		// hence this variable is reset.
		m_ResizeMoveModeSel = false;
		HashMap vdpData = m_SelectedVDPData;
		// Resize and Move are only supported for VDP Text and that to for Line
		// or Word Mode only.
		if (!((String) vdpData.get("FieldType")).equals("Text"))
			return;
		int vdpTextMode = ((Integer) vdpData.get("VDPTextMode")).intValue();
		if (vdpTextMode == StaticFieldInfo.PARA)
			return;
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		RGPTLogger.logToFile("MouseReleased. X: " + e.getX() + " Y: "
				+ e.getY());
		vdpData.remove("SelPt");
		vdpData.remove("MouseDragX");
		vdpData.remove("MouseDragY");
		this.calcNewBBox(vdpData);
		m_PDFViewContainer.repaint();
	}

	private void calcNewBBox(HashMap vdpData) {
		this.calcNewBBox(m_PDFPageInfo, vdpData);
	}

	private void calcNewBBox(PDFPageInfo pgInfo, HashMap vdpData) {
		RGPTRectangle rgptRect = (RGPTRectangle) vdpData
				.remove("NewDeviceBBox");
		if (rgptRect == null)
			return;
		Rectangle2D.Double bbox = rgptRect.getRectangle2D();
		RGPTLogger.logToFile("Screen BBox: " + bbox.toString());
		// Screen Points
		double x = bbox.getX(), y = bbox.getY(), w = bbox.getWidth(), h = bbox
				.getHeight();
		Point2D.Double scrPt1 = new Point2D.Double(x, y);
		Point2D.Double scrPt2 = new Point2D.Double(x, y + h);
		Point2D.Double scrPt3 = new Point2D.Double(x + w, y);
		Point2D.Double scrPt4 = new Point2D.Double(x + w, y + h);
		RGPTLogger.logToFile("Screen Pt 1: " + scrPt1.toString());
		RGPTLogger.logToFile("Screen Pt 2: " + scrPt2.toString());
		RGPTLogger.logToFile("Screen Pt 3: " + scrPt3.toString());
		RGPTLogger.logToFile("Screen Pt 4: " + scrPt4.toString());

		// Calc Page Points
		AffineTransform finalDevCTM = (AffineTransform) pgInfo.m_FinalDeviceCTM
				.clone();
		AffineTransform invCTM = null;
		try {
			invCTM = finalDevCTM.createInverse();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		RGPTLogger.logToFile("Inv Transform: " + invCTM.toString());

		Point2D.Double pgPt1 = new Point2D.Double();
		invCTM.transform(scrPt1, pgPt1);
		Point2D.Double pgPt2 = new Point2D.Double();
		invCTM.transform(scrPt2, pgPt2);
		Point2D.Double pgPt3 = new Point2D.Double();
		invCTM.transform(scrPt3, pgPt3);
		Point2D.Double pgPt4 = new Point2D.Double();
		invCTM.transform(scrPt4, pgPt4);
		RGPTLogger.logToFile("Page Pt 1: " + pgPt1.toString());
		RGPTLogger.logToFile("Page Pt 2: " + pgPt2.toString());
		RGPTLogger.logToFile("Page Pt 3: " + pgPt3.toString());
		RGPTLogger.logToFile("Page Pt 4: " + pgPt4.toString());

		double pgWth = Math.abs(pgPt3.getX() - pgPt1.getX());
		double pgHt = Math.abs(pgPt2.getY() - pgPt1.getY());

		double pgPtX = Math.min(pgPt1.getX(), pgPt3.getX());
		double pgPtY = Math.min(pgPt1.getY(), pgPt2.getY());
		Rectangle2D.Double pgBBox = new Rectangle2D.Double(pgPtX, pgPtY, pgWth,
				pgHt);
		Rectangle2D.Double origPgBBox = ((RGPTRectangle) vdpData.get("BBox"))
				.getRectangle2D();
		vdpData.put("NewBBox", RGPTRectangle.getReactangle(pgBBox));
		RGPTLogger.logToFile("Orig Page BBox: " + origPgBBox.toString());
		RGPTLogger.logToFile("New Page BBox: " + pgBBox.toString());

		// Calculating the Translation in Y direction
		double tx = pgBBox.getX() - origPgBBox.getX();
		double ty = pgBBox.getY() - origPgBBox.getY();
		vdpData.put("Tx", tx);
		vdpData.put("Ty", ty);

		// Calc Text Start Point
		this.calcTextStartPt(vdpData, bbox);
	}

	public void calcTextStartPt(HashMap vdpData, Rectangle2D.Double bbox) {
		double x = bbox.getX(), y = bbox.getY(), w = bbox.getWidth(), h = bbox
				.getHeight();
		Point2D.Double scrPt1 = new Point2D.Double(x, y);
		Point2D.Double scrPt2 = new Point2D.Double(x, y + h);
		Point2D.Double scrPt3 = new Point2D.Double(x + w, y);
		Point2D.Double scrPt4 = new Point2D.Double(x + w, y + h);
		Point2D.Double pgPt1 = new Point2D.Double();
		Point2D.Double pgPt2 = new Point2D.Double();
		Point2D.Double pgPt3 = new Point2D.Double();
		Point2D.Double pgPt4 = new Point2D.Double();
		AffineTransform invTextCTM = (AffineTransform) vdpData
				.get("FinalInvCTM");
		invTextCTM.transform(scrPt1, pgPt1);
		invTextCTM.transform(scrPt2, pgPt2);
		invTextCTM.transform(scrPt3, pgPt3);
		invTextCTM.transform(scrPt4, pgPt4);
		RGPTLogger.logToFile("Txt Page Pt 1: " + pgPt1.toString());
		RGPTLogger.logToFile("Txt Page Pt 2: " + pgPt2.toString());
		RGPTLogger.logToFile("Txt Page Pt 3: " + pgPt3.toString());
		RGPTLogger.logToFile("Txt Page Pt 4: " + pgPt4.toString());
		double txtPtX = Math.min(pgPt1.getX(), pgPt3.getX());
		// double txtPtY = Math.min(pgPt1.getY(), pgPt2.getY());
		double txtPtY = pgPt2.getY();
		// vdpData.put("StartPtX", txtPtX);
		// if (m_ViewMode == MOVE_MODE) {
		// vdpData.put("StartPtY", txtPtY);
		// AffineTransform textCTM = (AffineTransform)
		// vdpData.get("TextMatrix");
		// textCTM.translate(0.0, txtPtY);
		// }
	}

	// When the user presses the mouse, record the location of the top-left
	// corner of rectangle.
	public int m_ImageSelStartX, m_ImageSelStartY;
	public int m_ImageSelEndX, m_ImageSelEndY;
	public int m_ImageSelWidth = 0, m_ImageSelHeight = 0;

	public void mousePressed(MouseEvent e) {
		double x = e.getX(), y = e.getY();
		// Checking if the Mouse is pressed on any Action Components
		if (m_PDFActionComps != null) {
			int actionId = containsActionComp(e.getX(), e.getY());
			if (actionId == PDFPageHandler.PDF_PROOF)
				this.showPDFProof();
			else if (actionId == PDFPageHandler.APPROVE_PDF)
				this.approvePDF();
			if (actionId != -1)
				return;
		}

		// This part of the code is to drag the view area of the Selected VDP
		// Image
		Vector vdpSelData = null;
		UserPageData userPgData = null;
		if (m_PDFPageInfo == null)
			return;
		Object obj = m_UserPageData.get(new Integer(m_PDFPageInfo.m_PageNum));
		if (obj == null)
			return;
		userPgData = (UserPageData) obj;
		HashMap vdpData = null;
		if (USE_THUMBVIEW_IMAGE_MODE == PDFPageHandler.THUMBVIEW_IMAGE_PANE) {
			vdpSelData = userPgData.getVDPData("Image", x, y);
			if (vdpSelData.size() != 0) {
				boolean foundContVDPData = false;
				if (vdpSelData.size() > 1) {
					vdpData = userPgData.getContainedVDPField(vdpSelData);
					if (vdpData != null)
						foundContVDPData = true;
				}

				if (!foundContVDPData)
					vdpData = (HashMap) vdpSelData.elementAt(0);
				if (vdpData.get("UserSetImage") == null)
					return;
				m_ViewMode = SELECT_IMAGE_MODE;
				m_SelectedVDPData = vdpData;
				int viewMode = ((Integer) m_SelectedVDPData.get("VDPViewMode"))
						.intValue();
				if (viewMode == SELECT_CROP_IMAGE)
					m_ViewMode = SELECT_CROP_IMAGE;
				vdpData.put("StartImgPtX", x);
				vdpData.put("StartImgPtY", y);
				// Image Coordinate System
				m_ImageSelStartX = (int) x;
				m_ImageSelStartY = (int) y;
				m_ImageSelEndX = m_ImageSelStartX;
				m_ImageSelEndY = m_ImageSelStartY;
				System.out
						.println("VDP Image Field Selected for Image Adjustments: "
								+ vdpData.get("VDPFieldName"));
				System.out.println("StartPtX: " + x + " StartPyY: " + y);
				System.out
						.println("VDP Image Field Selected for Image Adjustments: "
								+ vdpData.get("VDPFieldName"));
				return;
			}
		}

		boolean procMouseAction = false;
		if (m_ViewMode == RESIZE_MOVE_MODE || m_ViewMode == RESIZE_MODE
				|| m_ViewMode == MOVE_MODE
				|| m_ViewMode == RESIZE_MOVE_FIELD_MODE)
			procMouseAction = true;
		if (!procMouseAction)
			return;
		RGPTLogger
				.logToFile("MousePressed. X: " + e.getX() + " Y: " + e.getY());
		boolean useMargin = true;
		vdpSelData = userPgData.getVDPData("Text", x, y, useMargin);
		// Only taking the First Element in case of Text
		if (vdpSelData.size() == 0) {
			System.out.println("No VDP Text Clicked");
			vdpSelData = userPgData.getVDPData("TextOnGraphics", x, y,
					useMargin);
		}
		if (vdpSelData.size() > 0)
			vdpData = (HashMap) vdpSelData.elementAt(0);
		if (vdpData == null)
			return;
		int viewFldMode = -1;
		if (m_ViewMode == RESIZE_MOVE_FIELD_MODE) {
			// Checking again to find the VDP Field that needs to be resized
			if (m_SelectedVDPData != null && m_SelectedVDPData.equals(vdpData)) {
				viewFldMode = ((Integer) vdpData.get("VDPViewMode")).intValue();
				if (viewFldMode == -1)
					return;
			} else
				return;
		}
		// Resize and Move are only supported for VDP Text and that to for Line
		// or Word Mode only.
		if (!((String) vdpData.get("FieldType")).equals("Text"))
			return;
		int vdpTextMode = ((Integer) vdpData.get("VDPTextMode")).intValue();
		if (vdpTextMode == StaticFieldInfo.PARA)
			return;
		m_SelectedVDPData = vdpData;
		m_ResizeMoveModeSel = true;
		RGPTLogger.logToFile("VDP Field Selected: "
				+ vdpData.get("VDPFieldName"));
		RGPTLogger.logToFile("Apply RESIZE_MOVE_MODE Logic");
		double size = 10.0;
		Rectangle2D.Double bbox = ((RGPTRectangle) vdpData.get("DeviceBBox"))
				.getRectangle2D();
		RGPTLogger.logToFile("VDP Fld Device BBox: " + bbox.toString());
		double x1 = bbox.getX(), y1 = bbox.getY(), w = bbox.getWidth() / 3, h = bbox
				.getHeight() / 3;
		Rectangle2D.Double ptRect = new Rectangle2D.Double();
		// Checking if the Mouse Clicked is at the First Point which is X, Y
		ptRect.setRect(bbox.getX() - w, bbox.getY() - h, 2 * w, 2 * h);
		RGPTLogger.logToFile("Pt1 Rect: " + ptRect.toString());
		int selPt = -1;
		if (ptRect.contains(x, y))
			selPt = 1;
		// Checking if the Mouse Clicked is at the Second Point which is X, Y+ht
		ptRect.setRect(bbox.getX() - w, bbox.getY() + bbox.getHeight() - h,
				2 * w, 2 * h);
		RGPTLogger.logToFile("Pt2 Rect: " + ptRect.toString());
		if (ptRect.contains(x, y))
			selPt = 2;
		// Checking if the Mouse Clicked is at the Third Point which is X+Width,
		// Y
		ptRect.setRect(bbox.getX() + bbox.getWidth() - w, bbox.getY() - h,
				2 * w, 2 * h);
		RGPTLogger.logToFile("Pt3 Rect: " + ptRect.toString());
		if (ptRect.contains(x, y))
			selPt = 3;
		// Checking if the Mouse Clicked is at the Fourth Point which is
		// X+Width, Y+ht
		ptRect.setRect(bbox.getX() + bbox.getWidth() - w,
				bbox.getY() + bbox.getHeight() - h, 2 * w, 2 * h);
		RGPTLogger.logToFile("Pt4 Rect: " + ptRect.toString());
		if (ptRect.contains(x, y))
			selPt = 4;
		if (selPt != -1 && (viewFldMode == -1 || viewFldMode == RESIZE_MODE)) {
			vdpData.put("SelPt", selPt);
			if (selPt == 1 || selPt == 2)
				setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
			else
				setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
			if (viewFldMode == -1)
				m_ViewMode = RESIZE_MODE;
			RGPTLogger.logToFile("Pt Sel for Dragging: "
					+ vdpData.get("SelPt").toString());
		} else {
			if (viewFldMode == -1)
				m_ViewMode = MOVE_MODE;
			vdpData.put("MouseDragX", x);
			vdpData.put("MouseDragY", y);
			RGPTLogger.logToFile("MouseDragX Start Pt: " + x
					+ "MouseDragY Start Pt: " + y);
			setCursor(new Cursor(Cursor.MOVE_CURSOR));
		}
		m_PDFViewContainer.repaint();
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void creatTextPopup() {
		m_TextPopup = new JPopupMenu("Resize/Move Text");
		JMenuItem item = m_TextPopup.add("Enter Text");
		item.setHorizontalTextPosition(JMenuItem.LEFT);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				m_SelectedVDPData.put("VDPViewMode", -1);
				m_TextPopup.setVisible(false);
				m_ViewMode = EDIT_MODE;
				m_PDFViewContainer.repaint();
			}
		});
		item = m_TextPopup.add("Resize Text");
		item.setHorizontalTextPosition(JMenuItem.LEFT);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				m_SelectedVDPData.put("VDPViewMode", RESIZE_MODE);
				m_TextPopup.setVisible(false);
				m_ViewMode = RESIZE_MOVE_FIELD_MODE;
				m_PDFViewContainer.repaint();
			}
		});
		item = m_TextPopup.add("Move Text");
		item.setHorizontalTextPosition(JMenuItem.LEFT);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				m_SelectedVDPData.put("VDPViewMode", MOVE_MODE);
				m_TextPopup.setVisible(false);
				m_ViewMode = RESIZE_MOVE_FIELD_MODE;
				m_PDFViewContainer.repaint();
			}
		});
	}

	public void creatImagePopup() {
		m_ImagePopup = new JPopupMenu("Select Image");
		JMenuItem item = m_ImagePopup.add("View Standard Image");
		item.setHorizontalTextPosition(JMenuItem.LEFT);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				m_SelectedVDPData.put("VDPViewMode", SELECT_IMAGE_MODE);
				HashMap vdpData = m_SelectedVDPData;
				ImageHolder imgHldr = (ImageHolder) vdpData.get("ImageHolder");
				imgHldr.m_DisplayRect = new Rectangle(
						(int) imgHldr.m_DeviceBBox.width,
						(int) imgHldr.m_DeviceBBox.height);
				imgHldr.m_DisplayClipRect = null;
				imgHldr.m_ClippedImage = null;
				m_ImagePopup.setVisible(false);
				m_ViewMode = EDIT_MODE;
				m_PDFViewContainer.repaint();
			}
		});
		item = m_ImagePopup.add("View Complete Image");
		item.setHorizontalTextPosition(JMenuItem.LEFT);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				m_ImagePopup.setVisible(false);
				m_SelectedVDPData.put("VDPViewMode", SHOW_FULL_IMAGE);
				HashMap vdpData = m_SelectedVDPData;
				ImageHolder imgHldr = (ImageHolder) vdpData.get("ImageHolder");
				imgHldr.m_DisplayRect = new Rectangle(imgHldr.m_ScaledImage
						.getWidth(), imgHldr.m_ScaledImage.getHeight());
				m_ViewMode = EDIT_MODE;
				m_PDFViewContainer.repaint();
			}
		});
		item = m_ImagePopup.add("Crop Image");
		item.setHorizontalTextPosition(JMenuItem.LEFT);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				m_SelectedVDPData.put("VDPViewMode", SELECT_CROP_IMAGE);
				m_ImagePopup.setVisible(false);
				HashMap vdpData = m_SelectedVDPData;
				ImageHolder imgHldr = (ImageHolder) vdpData.get("ImageHolder");
				imgHldr.m_DisplayRect = new Rectangle(
						(int) imgHldr.m_DeviceBBox.width,
						(int) imgHldr.m_DeviceBBox.height);
				imgHldr.m_DisplayClipRect = null;
				imgHldr.m_ClippedImage = null;
				PDFPersonalizationUtil persUtil = new PDFPersonalizationUtil(
						true);
				persUtil.getScaledImage(m_SelectedVDPData);
				imgHldr.m_DisplayRect = new Rectangle(imgHldr.m_ScaledImage
						.getWidth(), imgHldr.m_ScaledImage.getHeight());
				m_ViewMode = EDIT_MODE;
				m_PDFViewContainer.repaint();
			}
		});
	}

	JPopupMenu m_ImagePopup, m_TextPopup;

	public void showPopupMenu(MouseEvent e) {
		String fldName = "";
		double x = e.getX();
		double y = e.getY();
		RGPTLogger.logToFile("Show Popup Menu at x: " + x + " y: " + y);
		UserPageData userPgData = null;
		Object obj = m_UserPageData.get(new Integer(m_PDFPageInfo.m_PageNum));
		if (obj == null)
			return;
		userPgData = (UserPageData) obj;
		HashMap vdpData = null;
		Vector vdpSelData = userPgData.getVDPData("Text", x, y);
		// Only taking the First Element in case of Text
		if (vdpSelData.size() > 0)
			vdpData = (HashMap) vdpSelData.elementAt(0);
		if (vdpData != null) {
			int vdpTextMode = ((Integer) vdpData.get("VDPTextMode")).intValue();
			if (vdpTextMode == StaticFieldInfo.PARA)
				return;
			fldName = (String) vdpData.get("VDPFieldName");
			RGPTLogger.logToFile("Show Popup Menu for: " + fldName);
			m_SelectedVDPData = vdpData;
			if (m_TextPopup == null)
				creatTextPopup();
			m_TextPopup.show(this, (int) x, (int) y);
			RGPTLogger.logToFile("Popup Label: " + m_TextPopup.getLabel());
			m_PDFViewContainer.repaint();
			return;
		}
		vdpSelData = userPgData.getVDPData("Image", x, y);
		if (vdpSelData.size() == 0)
			return;
		if (vdpSelData.size() > 1) {
			vdpData = userPgData.getContainedVDPField(vdpSelData);
		}
		if (vdpData == null)
			vdpData = (HashMap) vdpSelData.elementAt(0);
		if (vdpData.get("UserSetImage") == null)
			return;
		fldName = (String) vdpData.get("VDPFieldName");
		RGPTLogger.logToFile("Show Popup Menu for: " + fldName);
		m_SelectedVDPData = vdpData;
		if (m_ImagePopup == null)
			creatImagePopup();
		m_ImagePopup.show(this, (int) x, (int) y);
		RGPTLogger.logToFile("Popup Label: " + m_ImagePopup.getLabel());
		m_PDFViewContainer.repaint();
	}

	Vector m_SelectedVDPFields;

	public void mouseClicked(MouseEvent e) {
		// System.out.println("Mouse Clicked");
		Vector vdpSelData = null;
		VDPUIHolder vdpField = null;
		VDPTextFieldInfo vdpTextFieldInfo = null;
		VDPImageFieldInfo vdpImageFieldInfo = null;

		double x = e.getX();
		double y = e.getY();
		if (m_TextPopup != null)
			m_TextPopup.setVisible(false);
		RGPTLogger.logToFile("Mouse Clicked at x: " + x + " y: " + y);
		if (e.getButton() == MouseEvent.BUTTON3) {
			this.showPopupMenu(e);
			return;
		}

		// Edit and Selections are possible for only Edit Mode or Resize Mode
		boolean procMouseClicked = false;
		if (m_ViewMode == EDIT_MODE || m_ViewMode == SELECT_MODE
				|| m_ViewMode == RESIZE_MOVE_FIELD_MODE)
			procMouseClicked = true;
		if (!procMouseClicked)
			return;
		UserPageData userPgData = null;
		Object obj = m_UserPageData.get(new Integer(m_PDFPageInfo.m_PageNum));
		if (obj == null)
			return;
		userPgData = (UserPageData) obj;
		HashMap vdpData = null;
		vdpSelData = userPgData.getVDPData("Text", x, y);
		// Only taking the First Element in case of Text
		if (vdpSelData.size() == 0) {
			System.out.println("No VDP Text Clicked");
			vdpSelData = userPgData.getVDPData("TextOnGraphics", x, y);
		}
		if (vdpSelData.size() > 0)
			vdpData = (HashMap) vdpSelData.elementAt(0);
		if (vdpData != null) {
			if (m_ViewMode == RESIZE_MOVE_FIELD_MODE) {
				// Changing to Edit Mode if different field is selected
				if (m_SelectedVDPData != null
						&& !m_SelectedVDPData.equals(vdpData))
					m_ViewMode = EDIT_MODE;
			}
			boolean isAllowEdit = ((Boolean) vdpData.get("AllowFieldEdit"))
					.booleanValue();
			if (!isAllowEdit)
				return;
			boolean isFieldFixed = ((Boolean) vdpData.get("IsFieldFixed"))
					.booleanValue();
			if (isFieldFixed)
				return;
			if (m_ViewMode == SELECT_MODE) {
				int vdpTextMode = ((Integer) vdpData.get("VDPTextMode"))
						.intValue();
				if (vdpTextMode == StaticFieldInfo.PARA)
					return;
				RGPTLogger.logToFile("Adding VDP Field to Selection: "
						+ vdpData.get("VDPFieldName"));
				m_SelectedVDPFields.addElement(vdpData);
				m_PDFViewContainer.repaint();
				return;
			}
			this.populateTextFieldData();
			m_SelectedVDPData = vdpData;
			RGPTLogger.logToFile("VDP Text Field Selected: "
					+ vdpData.get("VDPFieldName"));
			String mesg = "Please Enter VDP text:";
			String value = (String) vdpData.get("DefaultValue");
			vdpData.put("BlankDefaultText", true);
			this.activateVDPTextArea();
			m_TextScrollPane.setVisible(true);
			// if(USE_THUMBVIEW_IMAGE_MODE ==
			// PDFPageHandler.THUMBVIEW_IMAGE_FRAME &&
			if (m_ThumbViewImageHandler != null) {
				m_ThumbViewImageHandler.setVisibility(false);
			}
			String fldName = (String) vdpData.get("VDPFieldName");
			this.setToolTipText("Enter " + fldName);
			m_ToolTip.setVisible(true);
			m_PDFViewContainer.repaint();
			return;
		}

		// Checking for Image
		// m_VDPTextArea.setEnabled(false);
		m_VDPTextArea.setEditable(false);
		m_TextScrollPane.setVisible(false);
		vdpSelData = userPgData.getVDPData("Image", x, y);
		if (vdpSelData.size() == 0) {
			System.out.println("No VDP Image Clicked");
			return;
		}
		boolean foundContVDPData = false;
		if (vdpSelData.size() > 1) {
			vdpData = userPgData.getContainedVDPField(vdpSelData);
			if (vdpData == null) {
				this.selectExactImage(vdpSelData);
				return;
			} else
				foundContVDPData = true;
		}

		if (!foundContVDPData)
			vdpData = (HashMap) vdpSelData.elementAt(0);
		boolean isAllowEdit = ((Boolean) vdpData.get("AllowFieldEdit"))
				.booleanValue();
		if (!isAllowEdit)
			return;
		RGPTLogger.logToFile("VDP Image Field Selected: "
				+ vdpData.get("VDPFieldName"));
		this.populateVDPImage(vdpData, true);
		String fldName = (String) vdpData.get("VDPFieldName");
		this.setToolTipText("Click to add " + fldName);
		m_ToolTip.setVisible(true);
		resetViewPort();
		m_PDFViewContainer.repaint();
		return;
	}

	private void resetViewPort() {
		try {
			if (m_SelectedVDPData == null)
				return;
			RGPTRectangle bbox = (RGPTRectangle) m_SelectedVDPData
					.get("DeviceBBox");
			if (bbox == null)
				return;
			Rectangle viewRect = m_PDFImageScroller.getViewport().getViewRect();
			// RGPTLogger.logToFile("The Viewport is: " + viewRect.toString());
			// RGPTLogger.logToFile("The VDP Text BBox: " + bbox.toString());
			if (!viewRect.contains(RGPTUtil.getRectangle(bbox))) {
				// RGPTLogger.logToFile("The Selected VDP Data is not in the Viewport");
				m_PDFImageScroller.getViewport().setViewPosition(
						new Point(viewRect.x, (int) bbox.y));
			}
		} catch (Exception ex) {
		}
	}

	private void populateVDPImage(HashMap vdpData, boolean addImgFile) {
		System.out.println("VDP Image Clicked, Field Name: "
				+ (String) vdpData.get("VDPFieldName"));
		System.out.println("VDP Image BBox: "
				+ (((RGPTRectangle) vdpData.get("BBox")).getRectangle2D())
						.toString());
		// Retriving Image File from the User
		m_SelectedVDPData = vdpData;
		showImageAssets(addImgFile);
	}

	public Vector m_VDPSelImages;
	public ThumbviewToolBar m_ImageSelUI;

	// This method call is implemented to show the selected Image on the screen
	// by drawing lines around the Image. This method is invoked when there are
	// multiple images for a particular selection to be handled.
	public void showImage(int assetId) {
		System.out.println("Show Image Asset: " + assetId);
		Rectangle2D.Double dispRect = null;
		HashMap vdpImageData = getSelectedImage(assetId);
		Object obj = vdpImageData.get("DeviceBBox");
		if (obj == null)
			throw new RuntimeException("DeviceBBox Key Must un-defined");
		dispRect = ((RGPTRectangle) obj).getRectangle2D();

		m_ViewMode = JAVA_PREVIEW_MODE;
		update(this.getGraphics());
		Graphics2D g2d = (Graphics2D) this.getGraphics();
		g2d.setPaint(Color.BLACK);
		BasicStroke basicStroke = new BasicStroke(2.0f);
		g2d.setStroke(basicStroke);
		g2d.draw(dispRect);
		Color col = definecolor();
		g2d.setColor(col);
		g2d.fill(dispRect);
		m_ViewMode = EDIT_MODE;
	}

	public HashMap getSelectedImage(int selAssetId) {
		HashMap selImageElement = null;
		if (m_VDPSelImages == null)
			return null;
		for (int i = 0; i < m_VDPSelImages.size(); i++) {
			selImageElement = (HashMap) m_VDPSelImages.elementAt(i);
			int assetId = ((Integer) selImageElement.get("TempAssetId"))
					.intValue();
			if (assetId == selAssetId)
				return selImageElement;
		}
		return null;
	}

	// This method call is implemented to further process the Selected Image.
	// This method is invoked when there are multiple images for a particular
	// selection to be handled.
	public void selectImage(int assetId) {
		System.out.println("Select Image Asset: " + assetId);
		HashMap vdpImageData = getSelectedImage(assetId);
		this.populateVDPImage(vdpImageData, true);
		if (!m_VDPSelImages.isEmpty())
			m_VDPSelImages.removeAllElements();
		// Closing the Image Selection UI if any in the memory
		if (m_ImageSelUI != null) {
			m_ImageSelUI.dispose();
			m_ImageSelUI = null;
		}
		m_PDFViewContainer.repaint();
	}

	// This method is called when the Display Box is d. In this scenario,
	// the this object does the necessary clean-up and refresh operations.
	public void windowClosing() {
		System.out.println("In windowClosing Method");
		if (!m_VDPSelImages.isEmpty())
			m_VDPSelImages.removeAllElements();
		// Closing the Image Selection UI if any in the memory
		if (m_ImageSelUI != null) {
			m_ImageSelUI.dispose();
			m_ImageSelUI = null;
		}
		m_PDFViewContainer.repaint();
	}

	// Multiple Images are selected. The user will select any one using
	// ThumbViewImageFrame. And correspondingly the call will be made to
	// Thumbview Image Frame for Image selection.
	public void selectExactImage(Vector vdpSelImages) {
		int thumbWidth = 50, thumbHt = 50;
		ImageHolder imgHldr = null;
		BufferedImage bigImage = null, thumbImage = null;
		HashMap vdpImageData = null;
		HashMap userSelImages = new HashMap();
		// if (!m_VDPSelImages.isEmpty()) m_VDPSelImages.removeAllElements();
		m_VDPSelImages = vdpSelImages;
		for (int i = 0; i < m_VDPSelImages.size(); i++) {
			vdpImageData = (HashMap) m_VDPSelImages.elementAt(i);
			bigImage = (BufferedImage) vdpImageData.get("UserSetImage");
			if (bigImage == null) {
				try {
					String imageName = "vdp_seal";
					String imgPath = "/res/" + imageName + ".gif";
					byte[] imgStr = ImageUtils.loadImage(imgPath,
							this.getClass());
					bigImage = ImageUtils.getBufferedImage(imgStr);
				} catch (Exception ex) {
					ex.printStackTrace();
					throw new RuntimeException(
							"Unable to convert PDF Image to Buffered Image");
				}
			}
			// this.displayImage(bigImage);
			thumbImage = ImageUtils.ScaleToSize(bigImage, thumbWidth, thumbHt);
			int assetId = i + 1;
			// this.displayImage(thumbImage);
			imgHldr = new ImageHolder(assetId, thumbImage, bigImage);
			imgHldr.m_UseCachedImages = true;
			userSelImages.put(assetId, imgHldr);
			vdpImageData.put("TempAssetId", assetId);
		}

		// Closing the Image Selection UI if any in the memory
		if (m_ImageSelUI != null) {
			m_ImageSelUI.dispose();
			m_ImageSelUI = null;
		}

		// Populating the Handler and the Image Selection UI for Selecting the
		// Image and adding to the selection
		m_ImageSelUI = new ThumbviewToolBar(this, userSelImages);
		m_ImageSelUI.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		m_ImageSelUI.setLocation(100, 600);
		m_ImageSelUI.setVisible(true);
		m_ImageSelUI.repaintContent();
	}

	// This returns VDPImageField for the Selected VDP Image Data
	private VDPImageFieldInfo findVDPImageField() {
		VDPImageFieldInfo vdpImageFieldInfo;
		Vector vdpImageFields = m_PDFPageInfo.m_VDPImageFieldInfo;
		Rectangle2D.Double vdpBBox = null;
		vdpBBox = (Rectangle2D.Double) m_SelectedVDPData.get("BBox");
		for (int i = 0; i < vdpImageFields.size(); i++) {
			vdpImageFieldInfo = (VDPImageFieldInfo) vdpImageFields.elementAt(i);
			if (vdpImageFieldInfo.m_PageRectangle.equals(vdpBBox))
				return vdpImageFieldInfo;
		}
		return null;
	}

	public void showImageAssets(boolean addImgFile) {
		RGPTRectangle imgBBox = (RGPTRectangle) m_SelectedVDPData.get("BBox");
		int themeId = ((Integer) m_SelectedVDPData.get("ThemeId")).intValue();
		boolean allowImgUpld = ((Boolean) m_SelectedVDPData
				.get("AllowUploadWithTheme")).booleanValue();
		System.out.println("ThumbViewImageHandler: " + m_ThumbViewImageHandler);
		m_ThumbViewImageHandler.manageImageSrcActivation(themeId, allowImgUpld);
		if (themeId != -1)
			System.out.println("Use Theme Images with Theme id " + themeId
					+ " Use Other Image Source: " + allowImgUpld);
		if (imgBBox != null)
			System.out.println("Image BBox: " + imgBBox.toString());
		else
			System.out.println("Image BBox is Null");
		m_ThumbViewImageHandler.setImageBBox(imgBBox);
		m_ThumbViewImageHandler.setVisibility(true);
		if (addImgFile)
			m_ThumbViewImageHandler.addImageFile();
	}

	public boolean containsVDPImage(MouseEvent me) {
		int screenx = me.getXOnScreen();
		int screeny = me.getYOnScreen();

		System.out.println("#### m_cpane.getLocationOnScreen() #### "
				+ this.getLocationOnScreen());
		System.out.println("ScreenX n ScreenY :: " + screenx + " , " + screeny);

		int x = screenx - this.getLocationOnScreen().x;
		int y = screeny - this.getLocationOnScreen().y;
		UserPageData userPgData = null;
		Object obj = m_UserPageData.get(new Integer(m_PDFPageInfo.m_PageNum));
		if (obj == null)
			return false;
		userPgData = (UserPageData) obj;
		if (m_SelectedVDPData != null) {
			RGPTRectangle devBBox = (RGPTRectangle) m_SelectedVDPData
					.get("DeviceBBox");
			if (devBBox.contains(x, y))
				return true;
		}
		Vector vdpSelData = userPgData.getVDPData("Image", x, y);
		if (vdpSelData.size() == 0)
			return false;
		HashMap vdpData = null;
		if (vdpSelData.size() > 1) {
			vdpData = userPgData.getContainedVDPField(vdpSelData);
			if (vdpData != null)
				System.out.println("\n\n**** VDP data Found"
						+ vdpData.toString() + "\n\n******\n");
		}
		if (vdpData == null)
			vdpData = (HashMap) vdpSelData.elementAt(0);
		this.populateVDPImage(vdpData, false);
		// m_SelectedVDPData = vdpData;
		return true;
	}

	public boolean containsUserSelImage() {
		if (m_SelectedVDPData == null)
			throw new RuntimeException("No VDP Image Selected");
		if (m_SelectedVDPData.get("UserSetImage") != null)
			return true;
		return false;
	}

	// This holds the Image or Text VDP data Selected by the User.
	// This function is predominantly used in the Server Mode where
	// images are selected from ThumbviewImageFrame
	HashMap m_SelectedVDPData;
	boolean m_ShowNewPDFThumbImage = false;

	public void updateImageData(BufferedImage image, ImageHolder imgHldr) {
		System.out.println("Updating Image Data for: "
				+ m_SelectedVDPData.get("VDPFieldName"));
		int themeId = ((Integer) m_SelectedVDPData.get("ThemeId")).intValue();
		boolean allowUpld = ((Boolean) m_SelectedVDPData
				.get("AllowUploadWithTheme")).booleanValue();
		RGPTLogger.logToFile("Selected Theme Id: " + themeId
				+ " Allow Upload with Theme: " + allowUpld
				+ " Image Holder Theme: " + imgHldr.m_ThemeId);
		if (themeId != -1 && !allowUpld) {
			if (themeId != imgHldr.m_ThemeId) {
				System.out
						.println("The Selected Image is not part of the Theme "
								+ "supported.");
				return;
			}
		}
		m_ShowNewPDFThumbImage = true;
		// if(USE_THUMBVIEW_IMAGE_MODE == PDFPageHandler.THUMBVIEW_IMAGE_FRAME
		// &&
		if (m_ThumbViewImageHandler != null) {
			m_ThumbViewImageHandler.setVisibility(false);
		}
		System.out.println("The Original Image Width: " + image.getWidth()
				+ " And Height: " + image.getHeight());
		m_SelectedVDPData.put("UserSetImage", image);
		ImageHolder selImgHldr = new ImageHolder(imgHldr);
		m_SelectedVDPData.put("ImageHolder", selImgHldr);
		System.out.println("Updated VDP Data: " + m_SelectedVDPData.toString());
		m_PDFViewContainer.repaint();
		return;
	}

	private HashMap loadImages() {
		HashMap imageAssets = null;
		try {
			// This will retrive all Thumbview Images
			int assetId = -1;
			int templateId = m_PDFPageInfoManager.m_AppletParameters
					.getIntVal("template_id");
			int imageSize = CustomerAssetRequest.THUMBVIEW_IMAGE;
			String assetType = CustomerAssetRequest.IMAGE_ASSET_TYPE;
			imageAssets = m_PDFPageInfoManager.getCustomerAssets(templateId,
					assetType, assetId, imageSize);
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(m_PDFViewContainer,
					"Unable to Download Images From Server",
					"Download Image Error", JOptionPane.ERROR_MESSAGE);
			System.out.println("Exception " + ex.getMessage());
			// Creating a Dummy Image Assets, as no Image was retrieved from
			// Server
			imageAssets = new HashMap();
		}
		return imageAssets;
	}

	// Checking conditions to enable or disable Button
	public void setButtonActivation() {
		m_SelectVDPButton.setEnabled(true);
		m_DeleteVDPButton.setEnabled(false);
		m_SavePDFWorkButton.setEnabled(false);
		boolean isButtonEnabled = false;
		if (m_PDFPageInfo.m_IsVDPFieldDefined)
			isButtonEnabled = true;
		else
			isButtonEnabled = false;
		m_EditButton.setEnabled(isButtonEnabled);
		m_ResizeMoveButton.setEnabled(isButtonEnabled);
		m_GenPDFButton.setEnabled(isButtonEnabled);

		boolean isServerMode = true;
		if (m_PDFPageInfoManager.m_Mode == m_PDFPageInfoManager.STANDALONE_MODE)
			isServerMode = false;
		if (!isServerMode)
			m_ApproveButton.setEnabled(false);
	}

	// This method is used to create and retrieve the Content Pane for viewing
	// and manipulating the PDF Page Content
	JPanel m_TextScrollPane = null;
	JScrollPane m_PDFImageScroller = null;

	public JPanel createContentPane() {
		// Defining Panel North and South to be added to the Content Pane

		System.out.println("Create textScroller");
		m_VDPTextArea = new JTextArea(2, 1);
		// this.addKeyListener(this);
		m_VDPTextArea.addKeyListener(this);
		m_VDPTextArea.setEditable(false);
		JScrollPane textScroller = null;
		textScroller = new JScrollPane(m_VDPTextArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		m_TextScrollPane = new JPanel(new BorderLayout());
		m_TextScrollPane.add(textScroller, BorderLayout.CENTER);
		m_ContentPane.add(m_TextScrollPane, BorderLayout.NORTH);
		m_TextScrollPane.setVisible(false);

		// Adding Scroll Pane to the PDFPage Content and laying it in the Center
		System.out.println("Create Image Scroller");
		m_PDFImageScroller = new JScrollPane(this,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		m_PDFImageScroller.setViewportBorder(BorderFactory
				.createLineBorder(Color.black));
		m_ContentPane.add(m_PDFImageScroller, BorderLayout.CENTER);

		return m_ContentPane;
	}

	// Page Navigation Functions
	public void displayFirstPDFPage() {
		m_PrevPageNum = m_PDFPageInfo.m_PageNum;
		m_PDFPageInfo = m_PDFPageInfoManager.firstPage();
		doActivitiesOnPageNavigation();
	}

	public void displayPrevPDFPage() {
		m_PrevPageNum = m_PDFPageInfo.m_PageNum;
		m_PDFPageInfo = m_PDFPageInfoManager.prevPage(m_PDFPageInfo.m_PageNum);
		doActivitiesOnPageNavigation();
	}

	public void displayNextPDFPage() {
		m_PrevPageNum = m_PDFPageInfo.m_PageNum;
		m_PDFPageInfo = m_PDFPageInfoManager.nextPage(m_PDFPageInfo.m_PageNum);
		doActivitiesOnPageNavigation();
	}

	public void displayLastPDFPage() {
		m_PrevPageNum = m_PDFPageInfo.m_PageNum;
		m_PDFPageInfo = m_PDFPageInfoManager.lastPage();
		doActivitiesOnPageNavigation();
	}

	public int getPageCount() {
		return m_PDFPageInfoManager.m_PageCount;
	}

	public int getPDFPageNum() {
		if (m_PDFPageInfo == null)
			return 1;
		return m_PDFPageInfo.m_PageNum;
	}

	// This method is invoked to scale PDF Page along Height, Width and coplete
	// Page
	public void scalePDFPage(String scaleFactor) {
		RGPTLogger.logToFile("IN SCALING COMBOBOX SOURCE: " + scaleFactor);
		m_ScalingFactor = ScalingFactor.getScalingFactor(scaleFactor);
		RGPTLogger.logToFile("SCALING FACTOR SET TO: "
				+ m_ScalingFactor.toString());
		m_PDFViewContainer.repaint();
	}

	// This method is invoked to edit and personalize PDF
	public void editPDF() {
		m_ViewMode = EDIT_MODE;
		m_PDFViewContainer.repaint();
	}

	// This method is invoked to resize or move VDP field
	public void resizePDF() {
		m_ViewMode = RESIZE_MOVE_MODE;
		m_PDFViewContainer.repaint();
	}

	// This method is invoked to save the PDF Work
	public void savePDFWork() {
	}

	// This method is invoked to select VDP field for delete or allign
	public void selectPDF() {
		m_ViewMode = SELECT_MODE;
		System.out.println("Setting to Select Mode: " + m_ViewMode);
		m_SelectedVDPFields = new Vector();
		m_PDFViewContainer.repaint();
	}

	// This method is invoked to delete VDP field
	public void deleteVDPField() {
	}

	// This method is invoked to allign VDP field to Left, Center or Right WRT
	// BBox
	public void allignWRTBBox(String allign) {
		if (m_SelectedVDPFields == null || m_SelectedVDPFields.size() == 0)
			return;
		String textAllign = "LEFT";
		HashMap vdpData = null;
		for (int i = 0; i < m_SelectedVDPFields.size(); i++) {
			vdpData = (HashMap) m_SelectedVDPFields.elementAt(i);
			// if (allign.equals(PDFPageHandler.ALLIGN_LEFT)) textAllign =
			// "LEFT";
			// if (allign.equals(PDFPageHandler.ALLIGN_CENTER)) textAllign =
			// "CENTER";
			// if (allign.equals(PDFPageHandler.ALLIGN_RIGHT)) textAllign =
			// "RIGHT";
			vdpData.put("TextAllignment", allign);
		}
		m_PDFViewContainer.repaint();
	}

	// This method is invoked to allign VDP field to Left, Center or Right WRT
	// BBox
	public void allignWRTPage(String allign) {
	}

	// This method is to register Action Buttons with PDF Page Handler Object.
	// This is to mainly to activate and de-activate button based on conditions.
	public void regPDFActionComponent(int actionId, JComponent actionComp) {
		JButton actionButton = null;
		JComboBox listBox = null;
		JTextField txtFld = null;
		String compClassName = actionComp.getClass().getSimpleName();
		if (compClassName.equals("JButton"))
			actionButton = (JButton) actionComp;
		else if (compClassName.equals("JComboBox"))
			listBox = (JComboBox) actionComp;
		else if (compClassName.equals("JTextField"))
			txtFld = (JTextField) actionComp;
		if (actionId == PDFPageHandler.RESIZE_MOVE_PDF)
			m_ResizeMoveButton = actionButton;
		else if (actionId == PDFPageHandler.EDIT_PDF)
			m_EditButton = actionButton;
		else if (actionId == PDFPageHandler.PDF_PROOF)
			m_GenPDFButton = actionButton;
		else if (actionId == PDFPageHandler.APPROVE_PDF)
			m_ApproveButton = actionButton;
		else if (actionId == PDFPageHandler.SELECT_PDF)
			m_SelectVDPButton = actionButton;
		else if (actionId == PDFPageHandler.DELETE_PDF)
			m_DeleteVDPButton = actionButton;
		else if (actionId == PDFPageHandler.SAVE_PDF_WORK)
			m_SavePDFWorkButton = actionButton;
		else if (actionId == PDFPageHandler.ZOOM_PDF_BOX)
			m_ZoomUIList = listBox;
		else if (actionId == PDFPageHandler.PDF_PAGE_FIELD)
			m_PageNumField = txtFld;
		else {
			String mesg = "Invalid Action Indicator " + actionComp.toString();
			System.out.println(mesg);
			throw new RuntimeException(mesg);
		}
	}

	private boolean m_PDFProofGenerated = false;

	public void showPDFProof() {
		try {
			if (m_PDFProofGenerated)
				return;
			StringBuffer mesg = new StringBuffer();
			boolean isVDPFldNonPopulated = checkNonPopulatedFields(mesg);
			if (isVDPFldNonPopulated) {
				JOptionPane.showMessageDialog(m_PDFViewContainer, mesg,
						"All Fields not Specified", JOptionPane.ERROR_MESSAGE);
				return;
			}
			// activateProgressMonitor();
			boolean isServerMode = true;
			if (m_PDFPageInfoManager.m_Mode == m_PDFPageInfoManager.STANDALONE_MODE)
				isServerMode = false;
			if (isServerMode)
				cleanUserData();
			setCursor(Cursor.WAIT_CURSOR);
			RGPTLogger.logToFile("GENERATING PDF PROOF.....");
			m_PDFView.createPersonalizedDocument(m_UserPageData,
					PDFViewInterface.SAVE_PDF_IN_MEMORY, "");
			RGPTLogger.logToFile("GENERATED PDF PROOF.....");
			m_PDFProofGenerated = true;
			setCursor(Cursor.DEFAULT_CURSOR);
			if (isServerMode)
				m_ApproveButton.setEnabled(true);
			resetFieldsOnPDFGeneration();
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(m_PDFViewContainer,
					"Unable to Upload Customized Data", "Upload Data Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// This API is to show Busy Cursor when the Image is getting uploaded
	public void showBusyCursor() {
		setCursor(Cursor.WAIT_CURSOR);
	}

	// This API is to reset the Cursor to default Cursor
	public void setDefaultCursor() {
		setCursor(Cursor.DEFAULT_CURSOR);
	}

	protected void setCursor(int cursorType) {
		this.setCursor(new Cursor(cursorType));
	}

	// This method is invoked to close the PDF Viewer
	public void closePDFViwer() {
		boolean isServerMode = true;
		if (m_PDFPageInfoManager.m_Mode == m_PDFPageInfoManager.STANDALONE_MODE)
			isServerMode = false;
		if (isServerMode)
			closeUI();
		else
			((JFrame) m_PDFViewContainer).dispose();
	}

	// This method is invoked to approve PDF Proof
	public void approvePDF() {
		if (m_PDFProofGenerated)
			m_ApprovalDialog.setVisible(true);
	}

	// This method is used to display PDF Page corresponding to the Page Number
	public void displayPDFPage(int pageNum) {
		m_PDFPageInfo = m_PDFPageInfoManager.getPDFPage(pageNum);
		m_PrevPageNum = m_PDFPageInfo.m_PageNum - 1;
		doActivitiesOnPageNavigation();
	}

	// This is to check if the PDFProof is generated
	public boolean isPDFProofGenerated() {
		return m_PDFProofGenerated;
	}

	// This method is invoked to show PDF in EBook Preview
	public void showEBookView(JPanel ebookPanel, boolean showPreview) {
		RGPTLogger.logToFile("Show Java Preview: " + showPreview);
		if (showPreview) {
			m_ViewMode = JAVA_PREVIEW_MODE;
			m_PDFViewContainer.repaint();
			return;
		}
		if (!m_EBookViewer.isEBookSet())
			this.setEBookViewer(ebookPanel);
		m_EBookViewer.setVisibility(true);
		m_EBookViewer.displayEBook();
	}

	public void setEBookViewer(JPanel ebookPanel) {
		boolean enablePDFAppr = false;
		boolean enablePgAnim = true;
		int animSpeed = 20;
		int animStep = 16;
		int pgCnt = m_PDFPageInfoManager.m_PageCount;
		System.out.println("Setting EBook UI Controls");
		m_EBookViewer.setEBookUIControls(ebookPanel,
				(EBookPageHandler) m_PDFView, -1, pgCnt, enablePDFAppr,
				enablePgAnim, animSpeed, animStep);
		m_EBookViewer.populateContentPane(ebookPanel);
	}

	private int m_CurrentZoomValue;

	private void setZoom(boolean isZoomIn) {
		m_ScalingFactor = ScalingFactor.ZOOM_IN_OUT;
		int currZoomValue = m_CurrentZoomValue;
		int zoomVal = currZoomValue;
		if (isZoomIn)
			zoomVal = m_ScalingFactor.zoomIn(currZoomValue);
		else
			zoomVal = m_ScalingFactor.zoomOut(currZoomValue);
		m_CurrentZoomValue = zoomVal;
	}

	// This method is invoked to zoom in and out of PDF Page
	public void zoomInPDFPage() {
		boolean isZoomIn = false;
		setZoom(isZoomIn);
		m_PDFViewContainer.repaint();
	}

	// This method is invoked to zoom in and out of PDF Page
	public void zoomOutPDFPage() {
		boolean isZoomIn = true;
		setZoom(isZoomIn);
		m_PDFViewContainer.repaint();
	}

	public void setPDFZoomValue(int zoomValue) {
		// System.out.println("Zoom Value: " + zoomValue);
		if (m_ScalingFactor.m_ZoomValue == zoomValue)
			return;
		m_ScalingFactor = ScalingFactor.ZOOM_IN_OUT;
		m_ScalingFactor.setZoom(zoomValue);
		m_PDFViewContainer.repaint();
	}

	// This method retrieves the Bookmark BufferedImages of all PDF Pages
	public Map<Integer, BufferedImage> getPDFPageBookmarks() {
		try {
			int pdfPageNum = 0;
			Vector pdfPages = new Vector();
			for (int i = 0; i < m_PDFPageInfoManager.m_PageCount; i++) {
				pdfPageNum = i + 1;
				pdfPages.addElement(pdfPageNum);
			}
			// Converting the HashMap into Map<Integer, BufferedImage>
			Map<Integer, BufferedImage> pdfPageThumbImgs = null;
			pdfPageThumbImgs = new HashMap<Integer, BufferedImage>();
			HashMap pdfThumbPages = m_PDFView.getPDFPageThumbviews(pdfPages);
			Object[] pdfPageNums = pdfThumbPages.keySet().toArray();
			for (int i = 0; i < pdfPageNums.length; i++) {
				pdfPageThumbImgs.put((Integer) pdfPageNums[i],
						(BufferedImage) pdfThumbPages.get(pdfPageNums[i]));
			}
			return pdfPageThumbImgs;
		} catch (Exception ex) {
			ex.printStackTrace();
			// return dummy hashmap
			return new HashMap();
		}
	}

	// This method is invoked when Page is Navidated
	private void doActivitiesOnPageNavigation() {
		// Setting the Image of the PDF Pag
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		this.setBufferedImage();
		UserPageData userPgData = null;
		this.populateUserPageData(m_PDFPageInfo.m_PageNum);
		m_SelectedVDPData = null;
		m_VDPTextArea.setText("");
		m_VDPTextArea.setEditable(false);
		m_TextScrollPane.setVisible(false);
		m_PageNumField.setText((new Integer(m_PDFPageInfo.m_PageNum))
				.toString());
		// Checking to Enable or disable buttons
		this.checkToEnableButtons();
		if (m_MultiWordSelLine != null)
			m_MultiWordSelLine.clear();
		m_MultiWordSelLine = null;
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		m_PDFViewContainer.repaint();
	}

	public void checkToEnableButtons() {
		boolean isPageEnable = false, isPDFEnable = false;
		if (m_PDFPageInfo == null)
			return;
		if (m_PDFPageInfo.m_IsVDPFieldDefined)
			isPageEnable = true;
		if (m_ResizeMoveButton != null)
			m_ResizeMoveButton.setEnabled(isPageEnable);
		if (m_EditButton != null)
			m_EditButton.setEnabled(isPageEnable);

		// Checking to enable Generate PDF Button
		if (m_PDFPageInfoManager.isVDPDefinedInPDF())
			isPDFEnable = true;
		m_GenPDFButton.setEnabled(isPDFEnable);
		RGPTLogger.logToFile("Values for m_ResizeMoveButton: " + isPageEnable
				+ " m_EditButton: " + isPageEnable + " m_GenPDFButton: "
				+ m_GenPDFButton.isEnabled());
	}

	public void displayPage() {
		JDialog imgBox = ImageUtils.displayImage(m_BufferedImage, "PDF PAGE",
				100, 100, 500, 500);
		imgBox.setVisible(true);
	}

	// private JDialog m_ProgressMonitorBox;
	private ProgressDialogBox m_ProgressMonitorBox;

	private void activateProgressMonitor() {
		String mesg = "Creating Personalized PDF";
		if (m_ProgressMonitorBox == null)
			m_ProgressMonitorBox = new ProgressDialogBox(mesg);
		m_ProgressMonitorBox.startProgressMonitor();
	}

	private void populateTextFieldData() {
		UserPageData userPgData = null;
		if (m_SelectedVDPData == null)
			return;
		String fldType = (String) m_SelectedVDPData.get("FieldType");
		if (!fldType.equals("Text"))
			return;
		for (int i = 0; i < m_PDFPageInfoManager.m_PageCount; i++) {
			int pgNum = i + 1;
			userPgData = (UserPageData) m_UserPageData.get(new Integer(pgNum));
			if (userPgData == null)
				continue;
			userPgData.setUserValue(m_SelectedVDPData);
		}
	}

	HashMap m_ServerPopulatedFields;

	private void populateAllUserPageData() {
		int pgNum = 0;
		if (m_PDFPageInfoManager.m_PageCount == 0)
			return;

		// Populating the First Page with the original thread, while the
		// remaining
		// pages with a minimum thread priority
		this.populateUserPageData(1);
		if (m_PDFPageInfoManager.m_PageCount == 1)
			return;

		// The thread is used for mult-page document
		RGPTThreadWorker threadWorker = null;
		HashMap requestData = new HashMap();
		requestData.put("RequestType", "LoadPDFPageInfo");
		threadWorker = new RGPTThreadWorker(Thread.MIN_PRIORITY, this,
				requestData);
		threadWorker.startThreadInvocation();
	}

	// Processing the Thread Request to load Font Streams
	public void processThreadRequest(HashMap requestData) throws Exception {
		HashMap servResp = null;
		Map<Integer, Vector> themeImages = null;
		int pgNum = -1;
		String reqType = (String) requestData.get("RequestType");
		if (reqType.equals("LoadPDFPageInfo")) {
			for (int i = 1; i < m_PDFPageInfoManager.m_PageCount; i++) {
				pgNum = i + 1;
				this.populateUserPageData(pgNum);
			}
		} else if (reqType.equals("LoadThemeImages")) {
			// The foll 2 line code is to get Images for individual Template
			// Theme
			// Integer[] themeIds = null;
			// themeIds = VDPImageFieldInfo.m_TemplateThemes.toArray(new
			// Integer[0]);
			// Getting Images for all the Template themes
			if (VDPImageFieldInfo.m_TemplateThemes.size() == 0)
				return;
			servResp = m_PDFPageInfoManager.getThemeData(
					GetThemesRequest.GET_THEME_IMAGES,
					VDPImageFieldInfo.m_TemplateThemes);
			themeImages = (Map<Integer, Vector>) servResp.get("ThemeImages");
			m_ThumbViewImageHandler.handleThemeImages(themeImages);
		}
	}

	Map<String, HashMap> m_VDPMapFieldValues = null;

	// Returns Fields that need to be prepopulated
	private void populateUserPageData(int pgNum) {
		HashMap servPopulatedFlds4Page = null;
		PDFPageInfo pgInfo = null;
		Object obj = m_UserPageData.get(pgNum);
		if (obj != null)
			return;
		pgInfo = m_PDFPageInfoManager.getPDFPage(pgNum);
		RGPTLogger.logToFile("Retrieved PDF Page From Server: " + pgNum);
		// Populating the User Page Data with Default Values
		// UserPageData usrPgData =
		// pgInfo.populateUserPageData(servPopulatedFlds4Page);
		UserPageData usrPgData = pgInfo.populateUserPageData();
		RGPTLogger.logInfoMesg("Retrieved User Page Data for Pag: " + pgNum
				+ " Num of Text Field: " + usrPgData.m_VDPTextData.size()
				+ " Num of Image Field: " + usrPgData.m_VDPImageData.size());
		m_UserPageData.put(pgNum, usrPgData);
		if (VDPTextFieldInfo.m_VDPPrepopulatedFields.size() == 0
				&& VDPImageFieldInfo.m_VDPPrepopulatedFields.size() == 0)
			return;
		RGPTLogger.logToFile("Populating Server Data for Fields for: "
				+ VDPTextFieldInfo.m_VDPPrepopulatedFields.toString());
		// Retriving the pre-mapped field from server
		try {
			if (m_VDPMapFieldValues == null) {
				m_VDPMapFieldValues = m_PDFPageInfoManager
						.populateMappedFields();
				// This is the case if this is unable to retrieve data from
				// Server. In this case resetting the
				// Prepopulated Field in the User Page Data.
				if (m_VDPMapFieldValues == null) {
					usrPgData.resetAllPrepopulatedFields("Text");
					usrPgData.resetAllPrepopulatedFields("Image");
					return;
				}
				RGPTLogger.logToFile("Retrieved Server Data: "
						+ m_VDPMapFieldValues.toString());
			}
			// Use vdpFldData to populate the UserPageData
			// Data for Text Template Field
			Map<String, String> textTempFieldData = m_VDPMapFieldValues
					.get("TEXT");
			// Data for IMAGE Template Field
			Map<String, ImageHolder> imgTempFieldData = m_VDPMapFieldValues
					.get("IMAGE");
			if (textTempFieldData != null)
				populateTextVDPFields(usrPgData, textTempFieldData);
			if (imgTempFieldData != null)
				populateImageVDPFields(usrPgData, imgTempFieldData);

		} catch (Exception ex) {
			RGPTLogger.logToFile("Exception while retrieving Field Val: ", ex);
		}
	}

	// This returns the HashMap of VDP Data with Device Points
	public void populateImageVDPFields(UserPageData usrPgData,
			Map<String, ImageHolder> vdpFldData) {
		String vdpFldName = null;
		ImageHolder imgHldr = null;
		BufferedImage servImg = null;
		HashMap vdpData = null;
		Object[] vdpFldNames = vdpFldData.keySet().toArray();
		for (int i = 0; i < vdpFldNames.length; i++) {
			vdpFldName = (String) vdpFldNames[i];
			vdpData = usrPgData.getVDPData(vdpFldName);
			if (vdpData == null)
				continue;
			try {
				imgHldr = vdpFldData.get(vdpFldName);
				servImg = ImageUtils.getBufferedImage(imgHldr.m_ImgStr);
				System.out.println("Data Mapped Image Size Wt: "
						+ servImg.getWidth() + " Ht: " + servImg.getHeight());
				// ImageUtils.displayImage(servImg,
				// "Showing Server Mapped Image");
				imgHldr.m_ImageBBox = (RGPTRectangle) vdpData.get("BBox");
				vdpData.put("UserSetImage", servImg);
				vdpData.put("ImageHolder", imgHldr);
				vdpData.put("AllowFieldEdit", false);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void populateTextVDPFields(UserPageData usrPgData,
			Map<String, String> vdpFldData) {
		String vdpFldName = "", vdpFldVal = "", vdpFldType = "";
		HashMap vdpData = null;
		Object[] vdpFldNames = vdpFldData.keySet().toArray();
		// First Resetting all the Prepopulated Fields
		usrPgData.resetAllPrepopulatedFields("Text");
		for (int i = 0; i < vdpFldNames.length; i++) {
			vdpFldName = (String) vdpFldNames[i];
			vdpFldVal = (String) vdpFldData.get(vdpFldName);
			Vector vdpSelData = usrPgData.getAllVDPDataForField("Text",
					vdpFldName);
			for (int j = 0; j < vdpSelData.size(); j++) {
				vdpData = (HashMap) vdpSelData.elementAt(j);
				vdpFldType = (String) vdpData.get("FieldType");
				vdpData.put("UserSetValue", vdpFldVal);
				vdpData.put("AllowFieldEdit", false);
				vdpData.put("IsVDPPrepopulated", true);
			}
		}
	}

	private boolean checkNonPopulatedFields(StringBuffer mesg) {
		boolean isFieldsNonPopulated = false;
		UserPageData userPgData = null;
		HashMap nonPopulatedFields = null;
		int pgNum = 0, numNonPopTextFld = 0, numNonPopImgFld = 0;
		mesg.append("Variable Data Fields are not fully populated in: \n");
		for (int i = 0; i < m_PDFPageInfoManager.m_PageCount; i++) {
			pgNum = i + 1;
			userPgData = (UserPageData) m_UserPageData.get(new Integer(pgNum));
			if (userPgData == null)
				continue;
			nonPopulatedFields = userPgData.getNonPopulatedFields();
			RGPTLogger.logToFile("Non Populated VDP Fields: "
					+ nonPopulatedFields.toString());
			numNonPopTextFld = ((Integer) nonPopulatedFields.get("TextField"))
					.intValue();
			numNonPopImgFld = ((Integer) nonPopulatedFields.get("ImageField"))
					.intValue();
			if (numNonPopTextFld == 0 && numNonPopImgFld == 0)
				continue;
			isFieldsNonPopulated = true;
			mesg.append("Page Number: " + pgNum
					+ ". Number of Non Specified: \n");
			if (numNonPopTextFld > 0)
				mesg.append("  Text Fields: " + numNonPopTextFld);
			if (numNonPopImgFld > 0)
				mesg.append("  Image Fields: " + numNonPopImgFld);
			mesg.append("\n");
		}
		return isFieldsNonPopulated;
	}

	// This method is invoked to Process PDF Approval
	public void processApproval(int approvalMode) {
		m_ApprovalDialog.setVisible(false);
		if (approvalMode == ApprovalHandler.DICIDE_LATER)
			return;
		if (approvalMode == ApprovalHandler.DISAPPROVE_PDF) {
			m_PDFPageInfoManager.setPDFApproval(false);
		} else if (approvalMode == ApprovalHandler.APPROVE_PDF) {
			m_PDFPageInfoManager.setPDFApproval(true);
		}
		this.closeUI();
	}

	// This is called when this UI is shown in Applet Mode especially after the
	// PDF Approval or when the Main window is closed. This is called for
	// necessary clean up.
	public void closeUI() {
		checkApproval();
		m_PDFViewContainer.setVisible(false);
	}

	private void resetFieldsOnPDFGeneration() {
		m_PDFPageInfo = m_PDFPageInfoManager.firstPage();
		m_BufferedImage = m_PDFPageInfo.m_BufferedImage;
		if (m_MultiWordSelLine != null)
			m_MultiWordSelLine.clear();
		m_ViewMode = PDF_PREVIEW_MODE;
		if (m_EditButton != null) {
			m_EditButton.setEnabled(false);
			m_EditButton.setFocusable(false);
		}
		if (m_GenPDFButton != null) {
			m_GenPDFButton.setEnabled(false);
			m_GenPDFButton.setFocusable(false);
		}
		m_ScalingFactor = ScalingFactor.FIT_PAGE;
		// Disabling the VDP Text Area
		m_VDPTextArea.setEnabled(false);
		m_VDPTextArea.setFocusable(false);
		m_TextScrollPane.setVisible(false);
		// Setting the Page Number in the Text Field
		m_PageNumField.setText((new Integer(m_PDFPageInfo.m_PageNum))
				.toString());
		m_EBookViewer.resetEBookCache();
		RGPTLogger.logToFile("DISPLAYING GENERATED PDF PROOF.....");
		m_PDFViewContainer.repaint();
	}

	private void checkApproval() {
		String mesg = null;
		if (m_PDFPageInfoManager.m_IsPDFApproved == ApprovePDFRequest.PDF_NO_ACTION_TAKEN)
			JOptionPane
					.showMessageDialog(m_PDFViewContainer,
							"No Action is Taken on the PDF",
							"PDF Approval Message Box",
							JOptionPane.INFORMATION_MESSAGE);
		else if (m_PDFPageInfoManager.m_IsPDFApproved == ApprovePDFRequest.PDF_DISAPPROVED)
			JOptionPane.showMessageDialog(m_PDFViewContainer,
					"PDF Generated is Diaapproved", "PDF Approval Message Box",
					JOptionPane.INFORMATION_MESSAGE);
		else if (m_PDFPageInfoManager.m_IsPDFApproved == ApprovePDFRequest.PDF_APPROVED)
			JOptionPane
					.showMessageDialog(
							m_PDFViewContainer,
							"PDF Generated is Approved and can be added to Cart",
							"PDF Approval Message Box",
							JOptionPane.INFORMATION_MESSAGE);
	}

	private void cleanUserData() {
		Object[] keys = m_UserPageData.keySet().toArray();
		for (int i = 0; i < keys.length; i++) {
			((UserPageData) m_UserPageData.get(keys[i])).cleanUserData();
		}
	}

	private Rectangle getRect(Rectangle2D rect2D) {
		int x = new Double(rect2D.getX()).intValue();
		int y = new Double(rect2D.getY()).intValue();
		int w = new Double(rect2D.getWidth()).intValue();
		int h = new Double(rect2D.getHeight()).intValue();
		return new Rectangle(x, y, w, h);
	}

	private void setBufferedImage() {
		if (m_QualityMode == PDFViewInterface.REGULAR_QUALITY_PDF)
			m_BufferedImage = m_PDFPageInfo.m_BufferedImage;
		if (m_BufferedImage != null)
			return;
		try {
			m_BufferedImage = m_PDFView.getPDFPage(m_QualityMode,
					m_PDFPageInfo.m_PageNum);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(m_PDFViewContainer,
					"Unable to load PDF Page", "Load PDF Page Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public Map<RGPTRectangle, Vector> m_MultiWordSelLine;
	/*
	 * pdfdraw.setDPI(200) Alpha interpolation—can be set to default, quality,
	 * or speed. Antialiasing—can be set to default, on, or off. Color
	 * Rendering–can be set to default, quality, or speed. Dithering—can be set
	 * to default, disable, or enable. Fractional Metrics—can be set to default,
	 * on, or off. Interpolation—can be set to nearest-neighbor, bilinear, or
	 * bicubic. Rendering—can be set to default, quality, or speed. Text
	 * antialiasing—can be set to default, on, or off.
	 */
	private Graphics2D m_Graphics2D;

	public void paint(Graphics g) {
		// System.out.println("In Paint");
		Graphics2D g2d = (Graphics2D) g;
		m_Graphics2D = g2d;
		Rectangle visibleRect = this.getVisibleRect();
		Dimension panelSize = new Dimension((int) visibleRect.getWidth(),
				(int) visibleRect.getHeight());
		// This check is done to see if the PDF Page is loaded from the Server
		if (m_BufferedImage == null) {
			// RGPTLogger.logToFile("The Template is not yet loaded. Drawing Progress "
			// +
			// "Bar Image " + m_ProgBarImage);
			int wt = m_ProgBarImage.getWidth();
			int ht = m_ProgBarImage.getHeight();
			int x = (int) ((panelSize.width - wt) / 2);
			int y = (int) ((panelSize.height - ht) / 2);
			Font font = g2d.getFont();
			font.deriveFont(font.BOLD, (float) 20.0);
			g2d.setFont(font);
			g2d.drawString("LOADING PERSONALIZATION SERVICE", (float) x,
					(float) (y - 100));
			g2d.drawImage(m_ProgBarImage, x, y, wt, ht, this);
			// Painting the Action Comps and ThumbImages while loadinf of PDF
			// Page happens in the Background
			this.paintActionComps(g2d, panelSize);
			return;
		}

		resetViewPort();
		// g2d.setClip(null);
		if (USE_THUMBVIEW_IMAGE_MODE == PDFPageHandler.THUMBVIEW_IMAGE_PANE
				&& m_ThumbViewImageHandler != null
				&& m_PDFPageInfo.m_VDPImageFieldInfo.size() > 0) {
			if (m_PDFViewer.showAlwaysPDFThumbImages())
				m_ThumbViewImageHandler.setVisibility(true);
		}

		if (m_PDFProofGenerated && m_ThumbViewImageHandler != null)
			m_ThumbViewImageHandler.setVisibility(false);
		int adjStartPty = 0;
		if (m_PDFViewer.hasPDFActionComps())
			adjStartPty = STAT_POS_ADJ;

		if (m_BackgroundImage != null) {
			// System.out.println("PDFViewer CenterPane Size: " +
			// this.getSize().toString());
			g2d.drawImage(m_BackgroundImage, 0, 0, panelSize.width,
					panelSize.height, this);
		}

		// Painting the Action Comps and ThumbImages
		this.paintActionComps(g2d, panelSize);

		Object obj = m_UserPageData.get(new Integer(m_PDFPageInfo.m_PageNum));
		if (obj == null)
			throw new RuntimeException("User Page Data not Created");
		;

		// Getting the User Set Data and Drawing the String with the specified
		// Font
		UserPageData userPgData = (UserPageData) obj;

		PDFPersonalizationUtil persUtil = new PDFPersonalizationUtil(true);
		if (m_MultiWordSelLine == null && !m_PDFProofGenerated) {
			Vector vdpTextFields = m_PDFPageInfo.m_VDPTextFieldInfo;
			m_MultiWordSelLine = persUtil.createMultiWordSelLine(m_PDFPageInfo,
					userPgData);
			RGPTLogger.logToFile("Final MultiwordSelObject "
					+ m_MultiWordSelLine.toString());
		}

		persUtil.drawOnGraphics(g, m_PDFPageInfo, userPgData, panelSize,
				m_ScalingFactor, adjStartPty, m_MultiWordSelLine, m_ViewMode,
				m_SelectedVDPFields, m_SelectedVDPData, m_VDPTextArea);

		boolean isTextSeqSet = (m_PDFPageInfo.m_IsPageSequenceSet4Text
				.get(m_PDFPageInfo.m_PageNum)).booleanValue();
		if (!isTextSeqSet && !userPgData.m_AssignedSeqId) {
			userPgData.assignSeqId();
			m_PDFPageInfo.m_IsPageSequenceSet4Text.put(m_PDFPageInfo.m_PageNum,
					true);
		}

		if (m_SelectedVDPData == null
				&& m_PDFPageInfo.m_VDPTextFieldInfo.size() > 0) {
			RGPTLogger.logToFile("Initial Selection of VDP Data");
			handleNextVdpData();
			return;
		}

		// Setting the ThumbPages with the new view
		if (m_ShowNewPDFThumbImage) {
			try {
				System.out.println("\n\n**********\n\n");
				System.out.println("In ShowNewPDFThumbImage");
				m_ShowNewPDFThumbImage = false;
				Image pageImg = null;
				System.out.println("PanelSize: " + panelSize.toString());
				Point p = this.getLocationOnScreen();
				int x = p.x
						+ (int) m_PDFPageInfo.m_WindowDeviceCTM.getTranslateX();
				int y = p.y
						+ (int) m_PDFPageInfo.m_WindowDeviceCTM.getTranslateY();
				int width = (int) m_PDFPageInfo.m_ScaledPDFPageWd;
				int height = (int) m_PDFPageInfo.m_ScaledPDFPageHt;
				Rectangle rect = new Rectangle(x, y, width, height);
				System.out.println("Rect Screen Image to capture: "
						+ rect.toString());
				if (m_Robot4PDFThumbPages == null)
					m_Robot4PDFThumbPages = new Robot();
				BufferedImage pdfPageImg = null, pdfThumbPg = null;
				pdfPageImg = m_Robot4PDFThumbPages.createScreenCapture(rect);
				int thmbWt = PDFViewer.THUMBVIEW_WIDTH;
				int thmbHt = PDFViewer.THUMBVIEW_HEIGHT;
				pdfThumbPg = ImageUtils.scaleImage(pdfPageImg, thmbWt, thmbHt,
						true);
				m_PDFViewer.updatePDFThumbPage(m_PDFPageInfo.m_PageNum,
						pdfThumbPg);
				System.out.println("Created NewPDFThumbImage: "
						+ pdfPageImg.toString() + " Update PDF Page: "
						+ m_PDFPageInfo.m_PageNum);
				System.out.println("End ShowNewPDFThumbImage");
				System.out.println("\n\n**********\n\n");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (m_PDFViewer.showPDFThumbPages()) {
			this.paintThumbPages(g2d, panelSize);
		}

		// Calling this method to set the scroll if needed
		int scale = (int) (m_PDFPageInfo.m_Scale * 100);
		// System.out.println("SCALE VALUE " + scale);
		m_CurrentZoomValue = scale;
		if (m_ZoomUIList != null)
			m_ZoomUIList.setSelectedItem(new Integer(scale).toString());
		this.activateScrollPane();
	}

	Robot m_Robot4PDFThumbPages;

	public void paintThumbPages(Graphics2D g2d, Dimension panelSize) {
		System.out.println("In paintThumbPages");
		Rectangle pdfPageThumbBBox = null;
		BufferedImage pdfPageThumbImg = null;
		Map<Integer, BufferedImage> pdfThumbPages = m_PDFViewer
				.getPDFThumbPages();
		Map<Integer, Rectangle> pdfThumbPageComps = null;
		pdfThumbPageComps = m_PDFViewer.getPDFThumbPageViewBBox(panelSize);
		Integer[] pdfPageNums = pdfThumbPages.keySet().toArray(new Integer[0]);
		for (int i = 0; i < pdfPageNums.length; i++) {
			pdfPageThumbImg = pdfThumbPages.get(pdfPageNums[i]);
			pdfPageThumbBBox = pdfThumbPageComps.get(pdfPageNums[i]);
			g2d.drawImage(pdfPageThumbImg, pdfPageThumbBBox.x,
					pdfPageThumbBBox.y, pdfPageThumbBBox.width,
					pdfPageThumbBBox.height, this);
		}
	}

	public static int STAT_POS_ADJ = 70;
	Map<Integer, BufferedImage> m_PDFActionCompImages;
	Map<Integer, Rectangle> m_PDFActionComps;

	public void paintActionComps(Graphics2D g2d, Dimension panelSize) {
		if (m_PDFViewer == null)
			return;
		m_PDFActionComps = m_PDFViewer.getPDFActions(panelSize);
		if (m_PDFActionComps == null)
			return;
		if (m_PDFActionCompImages == null)
			m_PDFActionCompImages = m_PDFViewer.getPDFActionImages();
		if (m_PDFActionCompImages == null)
			return;
		Integer[] actionIds = m_PDFActionComps.keySet().toArray(new Integer[0]);
		BufferedImage compImg = null;
		Rectangle compRect = null;
		for (int i = 0; i < actionIds.length; i++) {
			compImg = m_PDFActionCompImages.get(actionIds[i]);
			compRect = m_PDFActionComps.get(actionIds[i]);
			if (compImg == null || compRect == null)
				continue;
			g2d.drawImage(compImg, compRect.x, compRect.y, compRect.width,
					compRect.height, this);
		}
	}

	// This is done to activate scroll pane by setting the Dimension of the
	// Panel
	// which holds the Contents i.e. the Image of the PDF Page and the VDP Data.
	private void activateScrollPane() {
		int x = (int) m_PDFPageInfo.m_WindowDeviceCTM.getTranslateX();
		int y = (int) m_PDFPageInfo.m_WindowDeviceCTM.getTranslateY();
		int width = (int) m_PDFPageInfo.m_ScaledPDFPageWd;
		int height = (int) m_PDFPageInfo.m_ScaledPDFPageHt;
		Dimension panelSize = new Dimension(width, height);

		Rectangle rect = new Rectangle(x, y, width, height);
		// this.scrollRectToVisible(rect);
		this.setPreferredSize(panelSize);
		this.revalidate();
	}

	private Rectangle2D.Double calcPageRect4TextOnGraphics(
			AffineTransform finalDevCTM, Rectangle2D srcRect) {
		Point2D.Double ptSrc, ptDst1, ptDst2;
		double desX1, desY1, desWidth, desHeight;
		AffineTransform pageCTM = null;
		Rectangle2D.Double desRect = null;

		ptSrc = new Point2D.Double();
		ptDst1 = new Point2D.Double();
		ptDst2 = new Point2D.Double();

		// Assigning the Final Device CTM
		// finalDevCTM = (AffineTransform)
		// m_PDFPageInfo.m_FinalDeviceCTM.clone();
		try {
			pageCTM = finalDevCTM.createInverse();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(
					"Exception at calcPageRect4TextOnGraphics: "
							+ ex.getMessage());
		}
		RGPTLogger.logToFile("Final Screen Device CTM: "
				+ finalDevCTM.toString());
		RGPTLogger.logToFile("Final Page CTM: " + pageCTM.toString());

		// Deriving x1, y1 of the Rectangle
		ptSrc.setLocation(srcRect.getX(), srcRect.getY());
		pageCTM.transform(ptSrc, ptDst1);
		RGPTLogger
				.logToFile("Screen PT " + ptSrc + ": Page PT DEST1 " + ptDst1);

		// Deriving x2, y2 of the Rectangle
		ptSrc.setLocation(srcRect.getX() + srcRect.getWidth(), srcRect.getY()
				+ srcRect.getHeight());
		pageCTM.transform(ptSrc, ptDst2);
		RGPTLogger.logToFile("Screen PT " + ptSrc + ": Pg PT DEST2 " + ptDst2);

		desX1 = Math.min(ptDst1.getX(), ptDst2.getX());
		desY1 = Math.min(ptDst1.getY(), ptDst2.getY());
		desWidth = Math.abs(ptDst1.getX() - ptDst2.getX());
		desHeight = Math.abs(ptDst1.getY() - ptDst2.getY());
		;

		desRect = new Rectangle2D.Double(desX1, desY1, desWidth, desHeight);
		return desRect;
	}

	public Color definecolor() {
		float rcol, gcol, bcol, alpha;
		rcol = new Float(0.5);
		gcol = new Float(0.1);
		bcol = new Float(0.7);
		alpha = new Float(0.2);
		Color col = new Color(rcol, gcol, bcol, alpha);
		return col;
	}

	// This is for Clean-up Activity. When called this releases the PDFPageInfo
	// Memory for every Page as well User VDP Data for every Page
	public void cleanUpMemory() {
		System.out.println("In CleanUp Memory");
		m_PDFPageInfo = null;
		m_MultiWordSelLine = null;
		if (m_ThumbViewImageHandler != null) {
			m_ThumbViewImageHandler.cleanUpMemory();
			m_ThumbViewImageHandler = null;
		}
		if (m_ImageAssets != null) {
			m_ImageAssets.clear();
			m_ImageAssets = null;
		}
		if (m_PageVDPFields != null) {
			m_PageVDPFields.clear();
			m_PageVDPFields = null;
		}
		if (m_UserPageData != null) {
			m_UserPageData.clear();
			m_UserPageData = null;
		}
		if (m_ApprovalDialog != null) {
			m_ApprovalDialog.dispose();
			m_ApprovalDialog = null;
		}
		m_BufferedImage = null;
	}
}
