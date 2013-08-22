// RGPT PACKAGES
package com.rgpt.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import com.rgpt.imageutil.ImageFilterController;
import com.rgpt.imageutil.ImageFilterHandler;
import com.rgpt.imageutil.ImageHolder;
import com.rgpt.imageutil.ImageUtils;
import com.rgpt.layoututil.BasicGridLayout;
import com.rgpt.templateutil.VDPFieldInfo;
import com.rgpt.templateutil.VDPImageFieldInfo;
import com.rgpt.templateutil.XODImageInfo;
import com.rgpt.util.CursorController;
import com.rgpt.util.FileFilterFactory;
import com.rgpt.util.LocalizationUtil;
import com.rgpt.util.RGPTActionListener;
import com.rgpt.util.RGPTFileFilter;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTParams;
import com.rgpt.util.RGPTUIManager;
import com.rgpt.util.RGPTUIUtil;
import com.rgpt.util.RGPTUtil;
import com.rgpt.util.ScalingFactor;
import com.rgpt.util.StaticFieldInfo;
import com.rgpt.util.XONAppLaucherInterface;
import com.rgpt.viewer.XONCanvasPanel;
import com.rgpt.viewer.XONImageDesigner;
import com.rgpt.viewer.XONImageMaker;

public class XONImageDesigner extends JFrame implements RGPTActionListener,
		XONAppLaucherInterface {
	// lightcolor for panel
	public static Color m_PanelColor;
	// darkercolor for button
	public static Color m_ButtonColor;
	// darkestcolor for text
	public static Color m_FontColor;

	// Title
	public String m_Title = LocalizationUtil.getText("IDAppsTitle");

	// West Panel Width
	public static int m_WestPanelWidth = 100;
	public static int m_SouthPanelHeight;

	// Use Windows Look and Feel
	public static boolean m_UseWindowsLookAndFeel = false;

	// Is Windows App
	public static boolean m_IsWindowsApp = true;

	// Content Pane

	// This Panel is the one that is added to Frame's Content Pane
	JPanel m_ContentPane;
	// This Panel holds the comple UI Element and is aaded to the Content Pane.
	// This kind of design allows multiple UI to be launched but at a time only
	// one
	// is active
	JPanel m_XONContentPane;

	// This holds the Image Maker Pane to modify individual Image using Image
	// Maker
	JPanel m_XONImageMakerPane;

	// Image Maker App for Image Make Over
	XONImageMaker m_XONImageMakerApp;

	// This Panel shows all the Preview Images
	JPanel m_ThumbnailPane;

	// This Panel shows all the Preview Images
	Map<String, JPanel> m_ThumbFilterPane;

	// This Panel shows the realtime advertisements
	JPanel m_AdsViewPane;

	Dimension m_ContentPaneSize;

	// This specifies the zoom value of the Image Openened
	JTextField m_ZoomInfoField;

	// This Label shows the Current Selection Mode
	JLabel m_SelectionModeLabel;

	// XODImageInfo maintains all the data of XON Image Designer and serializes
	// its
	// data in XOD (Xpress On Design) File
	XODImageInfo m_XODImageInfo;

	// This Panel is used for File Open and Close Button
	JPanel m_FileOpenClosePanel;

	// This Panel is used for File Open and Close Button
	JPanel m_FileNewInsertPanel;

	// This Panel mainly shows Image Previews and Image Filter Parameter
	JPanel m_EastPreviewPanel;

	// This Panel mainly to show Image Filter Parameter
	JPanel m_ImageFilterParamPanel;

	// This Maintains Map of Different Panels that can be shown in a East
	// Preview Panel.
	public Map<Integer, JComponent> m_EastPreviewPanelItems;

	// This Maintains Map of Different Image Filter Panels that can be shown in
	// a East Preview Panel.
	public Map<String, JPanel> m_ActionFilterPanelMap;

	// This shows the different items that can be shown in a East Preview Panel
	public static final int SHOW_IMAGE_PREVIEW = 0;
	public static final int SHOW_IMAGE_FILTER = 1;
	public static final int SHOW_SHAPE_PREVIEW = 2;
	public int m_ShowPreviewItem = 0;

	// This Panel mainly shows Workflow Steps for the Button that is Clicked.
	// This Panel
	// is also used to show tool tip, advertisements when no button is clicked.
	JPanel m_XONWorkflowPanel;

	// This Maintains Map of Different Panels that can be shown in a Workflow
	// Panel.
	public Map<Integer, JComponent> m_WorkflowPanelItems;

	// This shows the different items that can be shown in a Workflow Panel
	public static final int SHOW_TOOL_TIP = 0;
	public static final int SHOW_ADV_INFO = 1;
	public static final int SHOW_WORKFLOW_STEPS = 2;
	public static final int DEFAULT_WORKFLOW_ITEM = SHOW_ADV_INFO;
	public int m_ShowWorkflowItem = 1;

	// This Panel holds either the Image that is openend or the XON Image Maker
	// Logo
	JPanel m_XONImageContentPane;

	// When a Image File is openend, XONImagePanel is responsible to draw the
	// Image
	// as well as for all the Mouse Operation
	XONCanvasPanel m_XONCanvasPanel = null;

	// This Panel is added to the content pane and is visible when no files are
	// open
	JPanel m_IDAppsIconPanel;

	// This Map maintains the Panels that needs to be displayed ImageContentPane
	// which
	// is the center contant pane
	public Map<Integer, JComponent> m_XONImageContentItem;

	// This shows the different items that can be shown in a ImageContentPane
	public static final int SHOW_XON_ICON = 0;
	public static final int SHOW_CANVAS_PANEL = 1;
	public static final int DEFAULT_XON_IMAGE_PANE = SHOW_XON_ICON;
	public int m_ShowXONImagePane = SHOW_XON_ICON;

	// This map maintains the HashMap of ActionId to the Corresponding Workflow
	// Panel with Steps
	public Map<String, JPanel> m_ActionWFPanelMap;

	// This map maintains the HashMap of ActionId to the Corresponding Button
	public Map<String, JButton> m_ActionButtonMap;

	// This map maintains the HashMap of ActionId to the Corresponding Button
	public static Map<String, String> m_ActionMnemonicKeysMap;

	// This map maintains the HashMap of ActionId to the Corresponding Message
	// to be
	// showed on the Status Bar
	public static Map<String, String> m_ActionMessage;

	// This map maintains the HashMap of WF Steps to Action Id
	public static Map<String, Vector> m_WFActionSteps;

	// This map maintains the map of WF Steps to correseponding Image Action
	public static Map<String, String> m_WFImageAction;

	// This maintains a map of Actions to Popup Menu
	Map<String, JPopupMenu> m_ActionPopupMenu;

	// This maintains a map of Popup Menu Action to Image Action
	Map<String, String> m_PopupMenuImageAction;

	// This maintains the map of Image Filter Action to the Corresponding Button
	Map<String, Map<String, JComponent>> m_ImageFilterActionComponent = null;

	// This maps the Image Filter Action to the Position shown in the Preview
	// Pane
	Map<String, Rectangle> m_ImageFilterPreviewPos = null;

	// This map maintains the HashMap of ActionId to the Corresponding Button
	// Selection to be
	// showed on the Status Bar
	public static Map<String, String> m_ActionSelection;

	// This Image show up in the Content Pane when no files are opened
	private static final BufferedImage XON_IMAGE_DESIGNER_ICON = ImageUtils
			.getFinalBufferedImage("res/XPressOnImageDesigner.jpg",
					XONImageDesigner.class);;
	// This Image is the thumb image that shows up in the Preview Pane and Zoom
	// Pane
	// when no files are opened
	private static final BufferedImage THUMB_IMAGE_DESIGNER_ICON = ImageUtils
			.getFinalBufferedImage("res/rgpticons/XONImageDesignerLogo.png",
					XONImageDesigner.class);
	private static BufferedImage THUMB_SCALED_IMAGE_DESIGNER_ICON;
	private static BufferedImage THUMB_ZOOM_PREVIEW_IMAGE;

	// MNEMONIC_KEYS correspond to Action Id
	static {
		m_ActionMessage = new HashMap<String, String>();
		m_ActionSelection = new HashMap<String, String>();
		m_ActionMnemonicKeysMap = new HashMap<String, String>();
		m_ActionMnemonicKeysMap.put(IDAppsActions.FILE_OPEN.toString(),
				"control O");
		m_ActionMnemonicKeysMap.put(IDAppsActions.FILE_CLOSE.toString(),
				"control W");
		m_ActionMnemonicKeysMap.put(IDAppsActions.FILE_NEW.toString(),
				"control N");
		m_ActionMnemonicKeysMap.put(IDAppsActions.MA_ADD_IMAGE.toString(),
				"control I");
		m_ActionMnemonicKeysMap.put(IDAppsActions.FILE_SAVE.toString(),
				"control S");
		m_ActionMnemonicKeysMap.put(IDAppsActions.FILE_UPLOAD.toString(),
				"control U");
	}

	// START FUNCTION TO CONSTRUCT AND DISTRUCT THIS OBJECT

	public XONImageDesigner() {
		this(true, null);
	}

	// This XONImageDesigner can be an independent App or can be invoked inside
	// another
	// App. If isWindowsAppis set to true, this is set as an independent App.
	// Content Pane Size is specified if this is not an Windows App
	public XONImageDesigner(boolean isWindowsApp, Dimension contentPaneSize) {
		// AppletParameters.createServerProperties(this.getClass(), false);
		m_WorkflowPanelItems = new HashMap<Integer, JComponent>();
		m_EastPreviewPanelItems = new HashMap<Integer, JComponent>();
		m_XONImageContentItem = new HashMap<Integer, JComponent>();
		m_WFActionSteps = new HashMap<String, Vector>();
		m_WFImageAction = new HashMap<String, String>();
		m_ActionPopupMenu = new HashMap<String, JPopupMenu>();
		m_PopupMenuImageAction = new HashMap<String, String>();
		m_XONCanvasPanel = new XONCanvasPanel(this);
		m_WestPanelWidth = RGPTParams.getIntVal("WestPanelWidth");
		m_SouthPanelHeight = RGPTParams.getIntVal("SouthPanelHeight");
		m_ThumbFilterPane = new HashMap<String, JPanel>();
		m_ActionWFPanelMap = new HashMap<String, JPanel>();
		m_ActionFilterPanelMap = new HashMap<String, JPanel>();
		m_ActionButtonMap = new HashMap<String, JButton>();
		m_ImageFilterPreviewPos = new HashMap<String, Rectangle>();
		m_ImageFilterActionComponent = new HashMap<String, Map<String, JComponent>>();
		if (isWindowsApp)
			this.setLookAndFeel();
		else
			m_ContentPaneSize = contentPaneSize;
		m_IsWindowsApp = isWindowsApp;
		if (m_IsWindowsApp) {
			this.createContentPane();
			this.setContentPane(m_ContentPane);
			JComponent cont = (JComponent) getContentPane();
			Border raisedBorder = BorderFactory.createRaisedBevelBorder();
			cont.setBorder(raisedBorder);
			this.pack();
			this.show();
		}
	}

	private void setLookAndFeel() {
		RGPTUIManager.setLookAndFeel();
		RGPTUIManager.setUIDefaults(m_PanelColor, m_ButtonColor, m_FontColor);
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		try {
			if (m_UseWindowsLookAndFeel)
				UIManager
						.setLookAndFeel("com.jgoodies.looks.windows.WindowsLookAndFeel");
		} catch (Exception ex) {
			RGPTLogger.logToFile("Unable to set L&F ", ex);
		}
		this.setTitle(m_Title);
		this.setResizable(false);
		this.setLocation(0, 5);

		// Maximizing the XPresson Designer UI to fit the complete window
		this.setExtendedState(this.getExtendedState() | this.MAXIMIZED_BOTH);

		// Setting the Logo Image of the Frame
		BufferedImage logoimg = null;
		try {
			logoimg = ImageUtils.getBufferedImage("res/XPressOnLogo.png",
					this.getClass());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		this.setIconImage(logoimg);

		// Setting the Content Pane for the Frame
		Rectangle maxBounds = this.getScreenSize();
		this.setMaximizedBounds(maxBounds);
		// Adjusting the Maximum Bounds to set the size of the Content Pane
		m_ContentPaneSize = new Dimension(maxBounds.width - 10,
				maxBounds.height - 36);

		// Setting the Close operation on XPressOn
		this.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent arg0) {
				RGPTLogger.logToConsole(m_Title + " Window Closed");
			}

			public void windowClosing(WindowEvent arg0) {
				RGPTLogger.logToConsole(m_Title + " Window Closing");
				closeImageDesignerApp();
			}
		});
	}

	private Rectangle getScreenSize() {
		Toolkit t = Toolkit.getDefaultToolkit();
		Dimension sz = t.getScreenSize();
		RGPTLogger.logToConsole("Screen Resolution: " + sz.toString());
		GraphicsEnvironment env = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		/*
		 * The next line determines if the taskbar (win) is covered if
		 * unremarked, the task will not be covered by the maximized JFRAME.
		 */
		Rectangle bounds = env.getDefaultScreenDevice()
				.getDefaultConfiguration().getBounds();
		Rectangle maxBounds = env.getMaximumWindowBounds();
		RGPTLogger.logToFile("Available Size after the Tool Bar for Display: "
				+ maxBounds);
		return maxBounds;
	}

	public void closeImageDesignerApp() {
		this.cleanup();
		System.exit(0);
	}

	protected void finalize() throws Throwable {
		RGPTLogger.logToConsole(m_Title + " Window Closing");
		this.cleanup();
	}

	public void cleanup() {
		if (m_XODImageInfo != null)
			m_XODImageInfo.cleanup();
	}

	public JPanel createContentPane() {
		m_ContentPane = new JPanel(new BorderLayout());
		m_ContentPane.setPreferredSize(m_ContentPaneSize);
		createXONImageMakerPane();
		createXONImageDesignerPane();
		m_ContentPane.add(m_XONContentPane, BorderLayout.CENTER);
		// Creating the UI Component
		this.createUI();
		return m_ContentPane;
	}

	public JPanel createXONImageDesignerPane() {
		m_XONContentPane = new JPanel(new BorderLayout());
		m_XONContentPane.setBackground(Color.WHITE);
		return m_XONContentPane;
	}

	public void createXONImageMakerPane() {
		m_XONImageMakerApp = new XONImageMaker(false, m_ContentPaneSize);
		m_XONImageMakerPane = m_XONImageMakerApp.createContentPane();
	}

	// END FUNCTION TO CONSTRUCT AND DISTRUCT THIS OBJECT

	// START FUNCTIONS TO CREATE UI COMPONENTS

	public void createUI() {
		try {
			JPanel centerPanel = this.createCenterPane();
			JPanel westPanel = this.createWestPanel();
			m_XONContentPane.add(westPanel, BorderLayout.WEST);
			m_XONContentPane.add(centerPanel, BorderLayout.CENTER);
			m_XONContentPane.revalidate();
			// Activating Buttons that needs to be active when the app is opened
			// first
			setButtonActivations(null, FILE_CLOSED_ACTIVATED_IDS);
		} catch (Exception ex) {
			RGPTLogger.logToFile("Exception at createUI ", ex);
		}
	}

	private JPanel createCenterPane() throws Exception {
		JPanel centerPane = new JPanel(new BorderLayout());
		JPanel centerContentPane = this.createCenterContentPanel();
		centerPane.add(centerContentPane, BorderLayout.CENTER);

		m_XONWorkflowPanel = new JPanel(new BorderLayout());
		int panelHt = RGPTParams.getIntVal("IDAppsWFPanelHeight");
		int panelWt = m_ContentPaneSize.width - m_WestPanelWidth
				- RGPTParams.getIntVal("IDAppsEastPanelWidth");
		Dimension northPanelSz = new Dimension(panelWt, panelHt);
		m_XONWorkflowPanel.setPreferredSize(northPanelSz);
		String lblTxt = LocalizationUtil.getText("IDAppsAdvText"), fontSzProp = "LabelFontSize";
		JLabel xonAdvLabel = RGPTUIUtil.createLabel(lblTxt, panelWt, panelHt,
				fontSzProp);
		JLabel xonHelpLabel = RGPTUIUtil.createLabel("", panelWt, panelHt,
				fontSzProp);
		m_WorkflowPanelItems.put(SHOW_ADV_INFO, xonAdvLabel);
		m_WorkflowPanelItems.put(SHOW_TOOL_TIP, xonHelpLabel);
		m_WorkflowPanelItems.put(SHOW_WORKFLOW_STEPS, new JPanel(
				new BorderLayout()));
		m_XONWorkflowPanel.add(xonAdvLabel, BorderLayout.CENTER);
		RGPTUIUtil.setBorder(m_XONWorkflowPanel, RGPTUIUtil.LOWERED_BORDER);
		centerPane.add(m_XONWorkflowPanel, BorderLayout.NORTH);
		return centerPane;
	}

	private JPanel createCenterContentPanel() throws Exception {
		JPanel centerContentPane = new JPanel(new BorderLayout());
		m_XONImageContentPane = new JPanel(new BorderLayout());
		this.createXONImageDesignerIconPane();
		this.createXONIMageViewerUI();
		m_XONImageContentItem.put(SHOW_XON_ICON, m_IDAppsIconPanel);
		m_XONImageContentPane.add(m_IDAppsIconPanel, BorderLayout.CENTER);
		centerContentPane.add(m_XONImageContentPane, BorderLayout.CENTER);
		JPanel eastPanel = this.createEastPanel();
		centerContentPane.add(eastPanel, BorderLayout.EAST);
		return centerContentPane;
	}

	private void createXONIMageViewerUI() {
		JPanel imageViewerPanel = new JPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(m_XONCanvasPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		imageViewerPanel.add(scrollPane, BorderLayout.CENTER);
		m_XONImageContentItem.put(SHOW_CANVAS_PANEL, imageViewerPanel);
	}

	// This Method creates Panel to display XPressOnDesign Image
	private void createXONImageDesignerIconPane() throws Exception {
		if (m_IDAppsIconPanel != null)
			return;
		ImageUtils iu = new ImageUtils();
		m_IDAppsIconPanel = new JPanel() {
			public void paint(Graphics g) {
				Rectangle visibleRect = this.getVisibleRect();
				Dimension panelSize = new Dimension(
						(int) visibleRect.getWidth(),
						(int) visibleRect.getHeight());
				// RGPTLogger.logToConsole("Panel Size: "+panelSize);
				g.drawImage(XON_IMAGE_DESIGNER_ICON, 0, 0, panelSize.width,
						panelSize.height, this);
			}
		};
	}

	JPanel m_LabelPanel;

	private JPanel createEastPanel() {
		LocalizationUtil lu = new LocalizationUtil();
		JPanel eastPanel = new JPanel(new BorderLayout());
		int sthEastHt = RGPTParams.getIntVal("IDAppsSouthEastPanelHeight");
		int eastPanelWt = RGPTParams.getIntVal("IDAppsEastPanelWidth");
		Dimension eastPanelSz = new Dimension(eastPanelWt,
				m_ContentPaneSize.height);
		eastPanel.setPreferredSize(eastPanelSz);

		m_EastPreviewPanel = new JPanel(new BorderLayout());
		m_ImageFilterParamPanel = new JPanel(new BorderLayout());
		JPanel eastCenterPanel = new JPanel(new BorderLayout());
		int wt = eastPanelWt, ht = RGPTParams.getIntVal("LabelHeight");
		String lblTxt = lu.getText("ImagePreview"), fontSzProp = "LabelFontSize";
		JPanel imgLabel = RGPTUIUtil.createLabelPanel(lblTxt, wt, ht,
				fontSzProp);
		eastCenterPanel.add(imgLabel, BorderLayout.NORTH);
		m_ThumbnailPane = new JPanel() {
			public void paint(Graphics g) {
				repaintImagePreviewPane(g);
			}
		};
		JScrollPane scrollPane = new JScrollPane(m_ThumbnailPane,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JPanel thumbViewPane = RGPTUIUtil.createPanel(scrollPane, true,
				RGPTUIUtil.LOWERED_BORDER);
		scrollPane.addMouseListener(this);
		eastCenterPanel.add(thumbViewPane, BorderLayout.CENTER);
		// eastCenterPanel.add(createImageEnhanceFilterPanel(),
		// BorderLayout.CENTER);
		m_EastPreviewPanel.add(eastCenterPanel, BorderLayout.CENTER);
		eastPanel.add(m_EastPreviewPanel, BorderLayout.CENTER);

		m_ShowPreviewItem = SHOW_IMAGE_PREVIEW;
		JPanel shapePreviewPanel = createShapePreviewPanel();
		m_EastPreviewPanelItems.put(SHOW_IMAGE_PREVIEW, eastCenterPanel);
		m_EastPreviewPanelItems.put(SHOW_IMAGE_FILTER, m_ImageFilterParamPanel);
		m_EastPreviewPanelItems.put(SHOW_SHAPE_PREVIEW, shapePreviewPanel);

		// South Panel Consisting of Preview Button and Selected Command Text
		m_LabelPanel = new JPanel();
		Dimension sthEastPanelSz = new Dimension(eastPanelWt, sthEastHt);
		m_LabelPanel.setPreferredSize(sthEastPanelSz);
		m_LabelPanel.setLayout(new BoxLayout(m_LabelPanel, BoxLayout.Y_AXIS));

		lblTxt = lu.getText("IDAppsSelMode");
		JPanel selLabel = RGPTUIUtil.createLabelPanel(lblTxt, wt, ht,
				fontSzProp);
		m_LabelPanel.add(selLabel);
		JPanel labelPanel = new JPanel(new BorderLayout());
		RGPTUIUtil.setCompSize(labelPanel, eastPanelWt, m_SouthPanelHeight, 1);
		m_SelectionModeLabel = RGPTUIUtil.createLabel("", eastPanelWt,
				m_SouthPanelHeight, fontSzProp);
		RGPTUIUtil.setBorder(m_SelectionModeLabel, RGPTUIUtil.LOWERED_BORDER);
		labelPanel.add(m_SelectionModeLabel, BorderLayout.CENTER);
		m_LabelPanel.add(labelPanel);
		eastPanel.add(createToolBar(), BorderLayout.WEST);
		eastPanel.add(m_LabelPanel, BorderLayout.SOUTH);
		return eastPanel;
	}

	JPanel m_ZoomViewPane;

	private JPanel createShapePreviewPanel() {
		LocalizationUtil lu = new LocalizationUtil();

		// Shape Preview Panel
		JPanel eastCenterPanel = new JPanel(new BorderLayout());

		// Preview Pane
		int wt = RGPTParams.getIntVal("IDAppsPreviewPanelWidth");
		int labelHt = RGPTParams.getIntVal("LabelHeight");
		int zoomPanelHt = RGPTParams.getIntVal("IDAppsZoomPanelHeight");
		String lblTxt = lu.getText("ImagePreview"), fontSzProp = "LabelFontSize";
		JPanel imgLabel = RGPTUIUtil.createLabelPanel(lblTxt, wt, labelHt,
				fontSzProp);
		eastCenterPanel.add(imgLabel, BorderLayout.NORTH);
		JPanel thumbnailPane = new JPanel() {
			public void paint(Graphics g) {
				repaintShapePreviewPane(g);
			}
		};
		JScrollPane scrollPane = new JScrollPane(thumbnailPane,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JPanel thumbViewPane = RGPTUIUtil.createPanel(scrollPane, true,
				RGPTUIUtil.LOWERED_BORDER);
		scrollPane.addMouseListener(this);
		eastCenterPanel.add(thumbViewPane, BorderLayout.CENTER);

		// Zoom Pane
		JPanel southEastPanel = new JPanel();
		Dimension sthEastPanelSz = new Dimension(wt, zoomPanelHt);
		southEastPanel.setPreferredSize(sthEastPanelSz);
		southEastPanel
				.setLayout(new BoxLayout(southEastPanel, BoxLayout.Y_AXIS));
		lblTxt = lu.getText("ImageZoomPreview");
		JPanel zoomLabel = RGPTUIUtil.createLabelPanel(lblTxt, wt, labelHt,
				fontSzProp);
		southEastPanel.add(zoomLabel);
		m_ZoomViewPane = new JPanel() {
			public void paint(Graphics g) {
				repaintZoomViewPane(g);
			}
		};
		JPanel zoomPanel = RGPTUIUtil.createPanel(m_ZoomViewPane, true,
				RGPTUIUtil.LOWERED_BORDER);
		int zoomlHt = RGPTParams.getIntVal("IDAppsZoomViewHeight");
		RGPTUIUtil.setCompSize(m_ZoomViewPane, wt, zoomlHt, 5);
		southEastPanel.add(zoomPanel);
		eastCenterPanel.add(southEastPanel, BorderLayout.SOUTH);
		return eastCenterPanel;
	}

	public Map<String, JComponent> m_ToolBarComponents = null;

	private JToolBar createToolBar() {
		m_ToolBarComponents = new HashMap<String, JComponent>();
		JToolBar toolbar = RGPTUIUtil.createToolBar("XONIDApps_Toolbar",
				JToolBar.VERTICAL, false, this, m_ToolBarComponents);
		return toolbar;
	}

	private JPanel createWestPanel() {
		LocalizationUtil lu = new LocalizationUtil();
		String icon = "", name = "", tip = "", resetAction = "";
		String[] names;
		IDAppsActions action = null;
		JPanel cutoutImgLabel, pixelSetterLabel, emptyPanel;
		int emptyPanelHt = RGPTParams.getIntVal("EmptyPanelHeight");

		JPanel mainWestPanel = new JPanel(new BorderLayout());
		JPanel westPanel = new JPanel();
		Dimension westPanelSz = new Dimension(m_WestPanelWidth,
				m_ContentPaneSize.height);
		westPanel.setPreferredSize(westPanelSz);
		westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));

		// File Action Panel
		JPanel fileActionPanel = this.createFileActionPanel();
		RGPTLogger.logToConsole("fileActionPanel Pref Size: "
				+ fileActionPanel.getPreferredSize());
		westPanel.add(fileActionPanel);
		// fileActionPanel.setBorder(new LineBorder(Color.BLACK));

		// Add New Image Button
		emptyPanel = RGPTUIUtil
				.createEmptyPanel(m_WestPanelWidth, emptyPanelHt);
		westPanel.add(emptyPanel);
		action = IDAppsActions.MA_ADD_IMAGE;
		icon = "rgpticons/InsertNewImage.gif";
		tip = lu.getText("ToolTip_IDAppsInsertImage");
		names = lu.getText("IDAppsInsertImage").split("::");
		JPanel addImgPanel = createAddActionPanel(icon, names, tip, action);
		westPanel.add(addImgPanel);
		icon = "rgpticons/AddImageSettingsWF.gif";
		name = lu.getText("EndImageSettings");
		resetAction = WFActions.END_SETTINGS.toString();
		createWFSteps(icon, name, tip, resetAction, action.toString(),
				"AddImageWF");
		// Adding Image Filters in WF Panel
		createImageFilterWFPanel();

		// Add Text Button
		emptyPanel = RGPTUIUtil
				.createEmptyPanel(m_WestPanelWidth, emptyPanelHt);
		westPanel.add(emptyPanel);
		action = IDAppsActions.MA_ADD_TEXT;
		icon = "rgpticons/AddText.gif";
		tip = lu.getText("ToolTip_AddText");
		names = lu.getText("AddText").split("::");
		JPanel addTextPanel = createAddActionPanel(icon, names, tip, action);
		westPanel.add(addTextPanel);
		icon = "rgpticons/AddTextSettingsWF.gif";
		name = lu.getText("AddTextSettings");
		resetAction = WFActions.RESET_WF.toString();
		createWFSteps(icon, name, tip, resetAction, action.toString(),
				"AddTextWF");

		// Set Shape Panel to Create and Add Shape
		emptyPanel = RGPTUIUtil
				.createEmptyPanel(m_WestPanelWidth, emptyPanelHt);
		westPanel.add(emptyPanel);
		JPanel setShapePanel = createSetShapPanel();
		westPanel.add(setShapePanel);

		JPanel buttonPanel = new JPanel(new BasicGridLayout(1, 2, 5, 1));
		buttonPanel.setMinimumSize(new Dimension(m_WestPanelWidth - 20, 120));
		action = IDAppsActions.MA_ADD_SHAPE;
		icon = "rgpticons/AddShapeIcon.gif";
		tip = lu.getText("ToolTip_AddShape");
		names = lu.getText("AddShape").split("::");
		// JPanel addShapePanel = createAddActionPanel(icon, names, tip,
		// action);
		// westPanel.add(addShapePanel);

		// Add XPressOn logo
		emptyPanel = RGPTUIUtil.createEmptyPanel(m_WestPanelWidth, 15);
		westPanel.add(emptyPanel);
		icon = "rgpticons/XONLogo.png";
		JPanel logoPanel = RGPTUIUtil.createLabelPanel(icon, m_WestPanelWidth,
				50, RGPTUIUtil.LOWERED_BORDER);
		// JPanel logoPanel =
		// RGPTUIUtil.createImagePanel("rgpticons/XONLogo.png", 140, 30);
		// RGPTUIUtil.setBorder(logoPanel, RGPTUIUtil.LINE_BORDER);
		westPanel.add(logoPanel);

		// Personalized Ads
		emptyPanel = RGPTUIUtil
				.createEmptyPanel(m_WestPanelWidth, emptyPanelHt);
		westPanel.add(emptyPanel);
		String lblTxt = lu.getText("IDAppsAdsPreview");
		JPanel adsLabel = RGPTUIUtil.createLabelPanel(lblTxt, m_WestPanelWidth,
				RGPTParams.getIntVal("LabelHeight"), "LabelFontSize");
		westPanel.add(adsLabel);
		m_AdsViewPane = new JPanel() {
			public void paint(Graphics g) {
				repaintAdsViewPane(g);
			}
		};
		JPanel adsPanel = RGPTUIUtil.createPanel(m_AdsViewPane, true,
				RGPTUIUtil.LOWERED_BORDER);
		int adsHt = RGPTParams.getIntVal("IDAppsAdsPanelHeight");
		RGPTUIUtil.setCompSize(m_AdsViewPane, m_WestPanelWidth, adsHt, -1);
		westPanel.add(adsPanel);

		// emptyPanel = RGPTUIUtil.createEmptyPanel(m_WestPanelWidth, 120);
		// westPanel.add(emptyPanel);
		// JPanel logoPanel =
		// RGPTUIUtil.createImagePanel("rgpticons/XONLogo.png", 140, 30);
		// westPanel.add(logoPanel);

		mainWestPanel.add(createZoomPanel(), BorderLayout.SOUTH);
		mainWestPanel.add(westPanel, BorderLayout.CENTER);
		return mainWestPanel;
	}

	private JPanel createSetShapPanel() {
		String icon = "", name = "", tip = "";
		String[] names;
		IDAppsActions action = null;
		LocalizationUtil lu = new LocalizationUtil();

		JPanel setShapePanel = new JPanel(new BorderLayout());
		int panelHt = RGPTParams.getIntVal("IDAppsWestPanelHeight");
		RGPTUIUtil.setCompSize(setShapePanel, m_WestPanelWidth, panelHt, 5);

		int wt = m_WestPanelWidth, ht = RGPTParams.getIntVal("LabelHeight");
		String lblTxt = lu.getText("SetShapeLabel"), fontSzProp = "LabelFontSize";
		JPanel setShapeLabel = RGPTUIUtil.createLabelPanel(lblTxt, wt, ht,
				fontSzProp);
		setShapePanel.add(setShapeLabel, BorderLayout.NORTH);

		JPanel buttonPanel = new JPanel(new BasicGridLayout(1, 2, 5, 1));
		buttonPanel.setMinimumSize(new Dimension(m_WestPanelWidth - 20, 120));
		action = IDAppsActions.CREATE_SHAPE;
		icon = "rgpticons/CutoutShapesIcon.gif";
		tip = lu.getText("ToolTip_CreateShape");
		names = lu.getText("CreateShape").split("::");
		JButton button = createActionButton(buttonPanel, icon, names, tip,
				action, false);
		icon = "rgpticons/CreateShapeWF.gif";
		name = lu.getText("EndShapeCreation");
		String resetAction = WFActions.END_CREATE_SHAPE_WF.toString();
		createWFSteps(icon, name, tip, resetAction, action.toString(),
				"CutoutShapeWFSteps");

		action = IDAppsActions.MA_ADD_SHAPE;
		icon = "rgpticons/AddShapeIcon.gif";
		tip = lu.getText("ToolTip_AddShape");
		names = lu.getText("AddShape").split("::");
		button = createActionButton(buttonPanel, icon, names, tip, action,
				false);
		icon = "rgpticons/AddShapeSettingsWF.gif";
		name = lu.getText("AddShapeSettings");
		resetAction = WFActions.RESET_WF.toString();
		createWFSteps(icon, name, tip, resetAction, action.toString(),
				"AddShapeWF");

		setShapePanel.add(buttonPanel, BorderLayout.CENTER);
		// RGPTUIUtil.setBorder(setShapePanel);
		return setShapePanel;
	}

	private JPanel createAddActionPanel(String icon, String[] names,
			String tip, IDAppsActions action) {
		JPanel addActionPanel = new JPanel(new BorderLayout());
		int panelHt = RGPTParams.getIntVal("IDAppsWestPanelHeight");
		RGPTUIUtil.setCompSize(addActionPanel, m_WestPanelWidth, panelHt, 5);
		Dimension size = new Dimension(120, 70);
		JButton button = RGPTUIUtil.createButton(icon, names[0], null, tip,
				size, this, action.toString(), true);
		m_ActionSelection.put(action.toString(), names[1]);
		m_ActionButtonMap.put(action.toString(), button);
		m_ActionMessage.put(action.toString(),
				RGPTUtil.replaceTextWithDelim(tip, "<br>", " "));
		addActionPanel.add(button, BorderLayout.CENTER);
		return addActionPanel;
	}

	private JPanel createFileActionPanel() {
		String icon = "", name = "", tip = "";
		String[] names;
		IDAppsActions action = null;
		LocalizationUtil lu = new LocalizationUtil();
		JPanel fileActionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,
				15, 2));
		JPanel buttonPanel = new JPanel(new BasicGridLayout(2, 2, 5, 5));
		int filePanelHt = RGPTParams.getIntVal("IDAppsFileActionPanelHeight");
		buttonPanel.setMinimumSize(new Dimension(m_WestPanelWidth - 20, 120));
		m_FileOpenClosePanel = new JPanel(new BorderLayout());
		m_FileNewInsertPanel = new JPanel(new BorderLayout());

		// File Open Button
		JButton fopen, fclose, fnew, finsert, fsave, fupload;
		icon = "rgpticons/FileOpen.gif";
		tip = lu.getText("ToolTip_IDAppsFileOpen");
		action = IDAppsActions.FILE_OPEN;
		names = lu.getText("IDAppsFileOpen").split("::");
		fopen = createActionButton(null, icon, names, tip, action, true);
		fopen.setEnabled(true);
		m_FileOpenClosePanel.add(fopen, BorderLayout.CENTER);
		buttonPanel.add(m_FileOpenClosePanel);

		// File Close Button
		icon = "rgpticons/FileClose.gif";
		tip = lu.getText("ToolTip_IDAppsFileClose");
		action = IDAppsActions.FILE_CLOSE;
		names = lu.getText("IDAppsFileClose").split("::");
		fclose = createActionButton(null, icon, names, tip, action, true);

		// File New Button
		icon = "rgpticons/NewCanvas.gif";
		tip = lu.getText("ToolTip_IDAppsFileNew");
		action = IDAppsActions.FILE_NEW;
		names = lu.getText("IDAppsFileNew").split("::");
		fnew = createActionButton(null, icon, names, tip, action, true);
		m_FileNewInsertPanel.add(fnew, BorderLayout.CENTER);
		buttonPanel.add(m_FileNewInsertPanel);

		// File Insert Image Button - This is currently not used in the File
		// Panel
		icon = "rgpticons/InsertNewImage.gif";
		tip = lu.getText("ToolTip_IDAppsInsertImageFile");
		action = IDAppsActions.MA_ADD_IMAGE;
		names = lu.getText("IDAppsInsertImageFile").split("::");
		finsert = createActionButton(null, icon, names, tip, action, true);

		// File Save Button
		icon = "rgpticons/FileSave.gif";
		tip = lu.getText("ToolTip_IDAppsFileSave");
		action = IDAppsActions.FILE_SAVE;
		names = lu.getText("IDAppsFileSave").split("::");
		fsave = createActionButton(buttonPanel, icon, names, tip, action, true);

		// File Compress Button
		icon = "rgpticons/FileUpload.gif";
		tip = lu.getText("ToolTip_IDAppsFileUpload");
		action = IDAppsActions.FILE_UPLOAD;
		names = lu.getText("IDAppsFileUpload").split("::");
		fupload = createActionButton(buttonPanel, icon, names, tip, action,
				true);

		RGPTUIUtil.setCompSize(fileActionPanel, m_WestPanelWidth, filePanelHt,
				-1);
		fileActionPanel.add(buttonPanel);
		return fileActionPanel;
	}

	private JButton createActionButton(JPanel buttonPanel, String icon,
			String[] names, String tip, IDAppsActions action,
			boolean setMnemonicKey) {
		Dimension size = new Dimension(50, 50);
		JButton actionBtn = RGPTUIUtil.createButton(icon, names[0], null, tip,
				size, this, action.toString(), true);
		m_ActionSelection.put(action.toString(), names[1]);
		if (setMnemonicKey)
			RGPTUIUtil
					.setMnemonicKey(m_XONContentPane, this, action.toString());
		m_ActionButtonMap.put(action.toString(), actionBtn);
		m_ActionMessage.put(action.toString(),
				RGPTUtil.replaceTextWithDelim(tip, "<br>", " "));
		if (buttonPanel != null)
			buttonPanel.add(actionBtn);
		return actionBtn;
	}

	private JPanel createZoomPanel() {
		LocalizationUtil lu = new LocalizationUtil();
		int westPanelWidth = RGPTParams.getIntVal("WestPanelWidth");
		int southPanelHeight = RGPTParams.getIntVal("SouthPanelHeight");
		int txtFldHeight = southPanelHeight - 10;
		JPanel zoomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 1));
		zoomPanel.setPreferredSize(new Dimension(westPanelWidth,
				southPanelHeight));

		JButton navButton = null;
		String icon, tip = "";
		Dimension size = new Dimension(20, 20);
		icon = "rgpticons/DecreaseZoom.png";
		tip = lu.getText("ToolTip_DecreaseZoom");
		IDAppsActions action = IDAppsActions.DECREASE_ZOOM;
		navButton = RGPTUIUtil.createImageButton(icon, tip, size, this,
				action.toString(), false);
		navButton.setBackground(XONImageDesigner.m_PanelColor);
		navButton.setForeground(XONImageDesigner.m_PanelColor);
		navButton.setBorder(new LineBorder(XONImageDesigner.m_PanelColor));
		navButton.setMnemonic(KeyEvent.VK_PAGE_UP);
		m_ActionButtonMap.put(action.toString(), navButton);
		zoomPanel.add(navButton);

		icon = "rgpticons/IncreaseZoom.png";
		tip = lu.getText("ToolTip_IncreaseZoom");
		action = IDAppsActions.INCREASE_ZOOM;
		navButton = RGPTUIUtil.createImageButton(icon, tip, size, this,
				action.toString(), false);
		// navButton.setEnabled(true);
		navButton.setBackground(XONImageDesigner.m_PanelColor);
		navButton.setForeground(XONImageDesigner.m_PanelColor);
		navButton.setBorder(new LineBorder(XONImageDesigner.m_PanelColor));
		navButton.setMnemonic(KeyEvent.VK_PAGE_DOWN);
		m_ActionButtonMap.put(action.toString(), navButton);
		zoomPanel.add(navButton);

		JPanel zoomInfoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 1,
				1));
		m_ZoomInfoField = new JTextField();
		m_ZoomInfoField.setText("0");
		m_ZoomInfoField.setHorizontalAlignment(JTextField.CENTER);
		m_ZoomInfoField.setColumns(3);
		m_ZoomInfoField.setPreferredSize(new Dimension(m_ZoomInfoField
				.getPreferredSize().width, txtFldHeight));
		m_ZoomInfoField.setBorder(new LineBorder(Color.BLACK));
		zoomInfoPanel.add(m_ZoomInfoField);
		m_ZoomInfoField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int zoomLevel = Integer.parseInt(m_ZoomInfoField.getText());
				setZoom(zoomLevel);
			}
		});

		zoomPanel.add(zoomInfoPanel);
		RGPTUIUtil.setBorder(zoomPanel);
		return zoomPanel;
	}

	private void createImageFilterWFPanel() {
		LocalizationUtil lu = new LocalizationUtil();
		String icon = "", name = "", tip = "";
		String[] names;
		WFActions action = WFActions.NEW_WF_IMAGE_FILTERS_STEP;
		icon = "rgpticons/AddImageFilterWF.gif";
		name = lu.getText("SetImageFilters");
		tip = lu.getText("ToolTip_SetImageFilters");
		String resetAction = WFActions.END_IMAGE_FILTER_SETTINGS.toString();
		// createWFSteps(icon, name, tip, resetAction, action.toString(),
		// "AddImageFilterWF");
		createImageFilterPanel(WFActions.IMAGE_ENHANCE_FILTERS.toString(), 12);
		createImageFilterThumbPanel(WFActions.FUNNY_EFFECT_FILTERS.toString());
		createImageFilterThumbPanel(WFActions.COLOR_EFFECT_FILTERS.toString());
		createImageFilterThumbPanel(WFActions.LIGHT_EFFECT_FILTERS.toString());
		createImageFilterThumbPanel(WFActions.FADE_EFFECT_FILTERS.toString());
		createImageFilterThumbPanel(WFActions.BORDER_EFFECT_FILTERS.toString());
		createImageFilterThumbPanel(WFActions.COLOR_FILTERS.toString());
		createImageFilterThumbPanel(WFActions.DISTORTION_FILTERS.toString());
		createImageFilterThumbPanel(WFActions.BLUR_FILTERS.toString());
		// createImageFilterPanel(WFActions.DISTORTION_FILTERS.toString(), 12);
	}

	public void createImageFilterThumbPanel(String action) {
		JPanel thumbFilterPane = new JPanel() {
			public void paint(Graphics g) {
				repaintImageFilterThumbPane(g);
			}
		};
		m_ThumbFilterPane.put(action, thumbFilterPane);
		JScrollPane scrollPane = new JScrollPane(thumbFilterPane,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JPanel imageFilterPanel = RGPTUIUtil.createPanel(scrollPane, true,
				RGPTUIUtil.LOWERED_BORDER);
		scrollPane.addMouseListener(this);
		m_ActionFilterPanelMap.put(action, imageFilterPanel);
	}

	private JPanel createImageFilterPanel(String action, int vgap) {
		int panelWt = RGPTParams.getIntVal("IDAppsPreviewPanelWidth");
		Map<String, JComponent> actionComp = new HashMap<String, JComponent>();
		m_ImageFilterActionComponent.put(action, actionComp);
		JPanel imageFilterPanel = RGPTUIUtil.createImageFilterPanel(action,
				panelWt, vgap, actionComp, this);
		m_ActionFilterPanelMap.put(action, imageFilterPanel);
		return imageFilterPanel;
	}

	private void createWFSteps(String icon, String name, String tip,
			String resetAction, String action, String wfProp) {
		JButton button = null;
		Vector<JButton> actionButtons = null;
		int panelWt = m_ContentPaneSize.width - m_WestPanelWidth;
		Dimension size = new Dimension(120, 50);
		JPanel wfMainPanel = new JPanel();
		button = RGPTUIUtil.createButton(icon, name, "", tip, size, this,
				resetAction, true);
		button.setEnabled(true);
		actionButtons = RGPTUIUtil.createWFSteps(wfMainPanel, button, panelWt,
				wfProp, this, m_ActionSelection, 5);
		RGPTUIUtil.setWFActionMap(actionButtons, action, m_WFImageAction);
		m_ActionWFPanelMap.put(action, wfMainPanel);
		m_WFActionSteps.put(action, actionButtons);
		// Creating a Popup Menu for Save Image WF Step which is part of Cutout
		// Image WF Process
		JPopupMenu menu = null;
		Dimension menuItemSz = RGPTUtil.getCompSize("WFMenuSize");
		if (!RGPTUtil.isIDAppsActionPerformed(action))
			return;
		IDAppsActions appAction = IDAppsActions.valueOf(action);
		switch (appAction) {
		case MA_ADD_IMAGE:
			menu = RGPTUIUtil.createPopupMenu(menuItemSz,
					"IMAGE_EFFECTS_MENU_ITEM", this);
			m_ActionPopupMenu.put(WFActions.IMAGE_EFFECTS_FILTERS.toString(),
					menu);
			menu = RGPTUIUtil.createPopupMenu(menuItemSz,
					"IMAGE_FILTERS_MENU_ITEM", this);
			m_ActionPopupMenu.put(WFActions.SET_IMAGE_FILTERS.toString(), menu);
			break;
		}
	}

	// START FUNCTION FOR INTERFACE METHODS

	public void setComponentData(String actionStr, String compName,
			JComponent comp) {
		if (RGPTUtil.isIDAppsActionPerformed(actionStr))
			setIDAppsCompData(actionStr, compName, comp);
		if (RGPTUtil.isImageFilterActionPerformed(actionStr))
			setImageFilterCompData(actionStr, compName, comp);
	}

	public void setImageFilterCompData(String actionStr, String compName,
			JComponent comp) {
		LocalizationUtil lu = new LocalizationUtil();
		if (compName != null && compName.length() > 0) {
			String name = lu.getText(compName);
			m_ActionSelection.put(actionStr, name);
		}
		if (comp instanceof JSlider) {
			JSlider slider = (JSlider) comp;
			slider.setOrientation(JSlider.HORIZONTAL);
		}
	}

	public void setIDAppsCompData(String actionStr, String compName,
			JComponent comp) {
		LocalizationUtil lu = new LocalizationUtil();
		IDAppsActions action = IDAppsActions.valueOf(actionStr);
		// Adding Toolbar Buttons
		if (comp instanceof JButton) {
			String name = lu.getText(compName), tip = lu.getText("ToolTip_"
					+ compName);
			m_ActionButtonMap.put(action.toString(), (JButton) comp);
			m_ActionSelection.put(action.toString(), name);
			m_ActionMessage.put(action.toString(),
					RGPTUtil.replaceTextWithDelim(tip, "<br>", " "));
		}
		switch (action) {
		case TB_SET_ALPHA_VALUE:
			JSlider slider = (JSlider) comp;
			slider.setInverted(true);
			slider.setMinimum(0);
			slider.setMaximum(10);
			slider.setOrientation(JSlider.VERTICAL);
			break;
		}
	}

	public void setZoom(int zoomLevel) {
		m_XONCanvasPanel.setZoom(zoomLevel);
	}

	public void updateZoom(int zoomLevel) {
		m_ZoomInfoField.setText(String.valueOf(zoomLevel));
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		if (m_XONCanvasPanel == null
				|| m_XONCanvasPanel.m_SelVDPFieldInfo == null)
			return;
		else if (m_XONCanvasPanel.m_ActionPerformedId == IDAppsActions.TB_SET_ALPHA
				.toString())
			performRGPTAction(IDAppsActions.TB_SET_ALPHA_VALUE.toString());
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
		String action = m_XONCanvasPanel.m_ActionPerformedId;
		// action == WFActions.DISTORTION_FILTERS.toString()
		if (action == WFActions.IMAGE_ENHANCE_FILTERS.toString()) {
			HashMap actionComp = (HashMap) m_ImageFilterActionComponent
					.get(action);
			Object actionObj = RGPTUtil.containsValue(actionComp,
					e.getSource(), false);
			if (actionObj == null) {
				RGPTLogger.logToFile("No Image Filter found: " + e);
				return;
			}
			if (((String) actionObj).contains("Slider"))
				performImageFilterAction((String) actionObj);
		}
	}

	public void mouseClicked(MouseEvent e) {
		Point pt = e.getPoint();
		String action = m_XONCanvasPanel.m_ActionPerformedId;
		if (action == WFActions.FUNNY_EFFECT_FILTERS.toString()
				|| action == WFActions.COLOR_EFFECT_FILTERS.toString()
				|| action == WFActions.LIGHT_EFFECT_FILTERS.toString()
				|| action == WFActions.FADE_EFFECT_FILTERS.toString()
				|| action == WFActions.BORDER_EFFECT_FILTERS.toString()
				|| action == WFActions.COLOR_FILTERS.toString()
				|| action == WFActions.DISTORTION_FILTERS.toString()
				|| action == WFActions.BLUR_FILTERS.toString()) {
			if (!m_OrigPreviewRect.contains(pt))
				return;
			String[] imgFilters = m_ImageFilterPreviewPos.keySet().toArray(
					new String[0]);
			for (int i = 0; i < imgFilters.length; i++) {
				int x = m_VisiblePreviewRect.x + pt.x, y = m_VisiblePreviewRect.y
						+ pt.y;
				if (m_ImageFilterPreviewPos.get(imgFilters[i]).contains(x, y))
					performImageFilterAction(imgFilters[i]);
			}
			return;
		}
		// RGPTLogger.logToFile("e source: "+e.getSource().getClass().getName());
		// RGPTLogger.logToFile("Pt Pressed: "+pt+" OrigPanelRect: "+m_OrigPreviewRect+
		// " VisiblePanelRect: "+m_VisiblePreviewRect);
		if (!(e.getSource() instanceof JButton))
			m_XONCanvasPanel.setSelectedField(pt, true, m_OrigPreviewRect,
					m_VisiblePreviewRect);
	}

	public void mouseEntered(MouseEvent e) {
		Object actionObj = RGPTUtil.containsValue((HashMap) m_ActionButtonMap,
				e.getSource());
		if (actionObj == null)
			return;
		// int actionId = ((Integer)actionIdObj).intValue();
		this.resetWorkflowPanel((String) actionObj, SHOW_TOOL_TIP);
	}

	public void mouseExited(MouseEvent e) {
		Object actionObj = RGPTUtil.containsValue((HashMap) m_ActionButtonMap,
				e.getSource());
		if (actionObj == null)
			return;
		// int actionId = ((Integer)actionIdObj).intValue();
		// RGPTLogger.logToFile("Resetting WF Panel to show Adv after action: "+(String)actionObj);
		this.resetWorkflowPanel((String) actionObj, SHOW_ADV_INFO);
	}

	public void resetPreviewPanel() {
		m_ShowPreviewItem = SHOW_IMAGE_PREVIEW;
		resetPanelItems(m_EastPreviewPanel, m_ShowPreviewItem,
				m_EastPreviewPanelItems);
		m_EastPreviewPanel.repaint();
	}

	private void resetPreviewPanel(String action, int showPreviewItem) {
		m_ShowPreviewItem = showPreviewItem;
		// This shows the different items that can be shown in a East Preview
		// Panel
		if (m_ShowPreviewItem == SHOW_IMAGE_PREVIEW) {
			resetPreviewPanel();
			return;
		}
		// Showing Image Filter on the Preview Pane
		if (m_ShowPreviewItem == SHOW_IMAGE_FILTER) {
			JPanel previewPanel = (JPanel) m_EastPreviewPanelItems
					.get(m_ShowPreviewItem);
			JPanel actionFilterPanel = m_ActionFilterPanelMap.get(action);
			RGPTLogger.logToFile("Setting FilterPanel for action: " + action);
			if (actionFilterPanel == null) {
				RGPTLogger.logToFile("Image Filter Panel Null for action: "
						+ action);
				resetPreviewPanel();
				return;
			}
			previewPanel.removeAll();
			previewPanel.add(actionFilterPanel, BorderLayout.CENTER);
		}
		resetPanelItems(m_EastPreviewPanel, m_ShowPreviewItem,
				m_EastPreviewPanelItems);
		m_EastPreviewPanel.repaint();
	}

	private void resetWorkflowPanel(int actionId, int showWFItem) {
		resetWorkflowPanel((new Integer(actionId)).toString(), showWFItem);
	}

	private void resetWorkflowPanel(String action, int showWFItem) {
		// RGPTLogger.logToFile("Current WorkflowItem: "+m_ShowWorkflowItem+
		// " New WorkflowItem: "+showWFItem+" Action: "+action);
		if (m_ShowWorkflowItem == SHOW_WORKFLOW_STEPS)
			if (showWFItem == SHOW_TOOL_TIP || showWFItem == SHOW_ADV_INFO)
				return;
		m_ShowWorkflowItem = showWFItem;
		if (m_ShowWorkflowItem == SHOW_TOOL_TIP) {
			String tip = m_ActionMessage.get(action);
			if (tip == null || tip.length() == 0) {
				RGPTLogger.logToConsole("Tool Tip is Empty");
				return;
			}
			JLabel xonHelpLabel = (JLabel) m_WorkflowPanelItems
					.get(m_ShowWorkflowItem);
			Dimension sz = xonHelpLabel.getSize(null);
			// RGPTLogger.logToFile("xonHelpLabel: "+xonHelpLabel+" size: "+sz+
			// " tip: "+tip);
			RGPTUIUtil.setLabelText(xonHelpLabel, tip, sz.width,
					"LabelFontSize");
		} else if (m_ShowWorkflowItem == SHOW_WORKFLOW_STEPS) {
			JPanel wfPanel = (JPanel) m_WorkflowPanelItems
					.get(m_ShowWorkflowItem);
			JPanel actionWFPanel = m_ActionWFPanelMap.get(action);
			RGPTLogger.logToFile("Setting WFPanel for action: " + action);
			if (actionWFPanel == null) {
				RGPTLogger.logToFile("WF Panel Null for action: " + action);
				resetWorkflowPanel();
				return;
			}
			wfPanel.removeAll();
			wfPanel.add(actionWFPanel, BorderLayout.CENTER);
		}
		resetPanelItems(m_XONWorkflowPanel, showWFItem, m_WorkflowPanelItems);
		m_XONWorkflowPanel.repaint();
	}

	private void resetWorkflowPanel() {
		m_ShowWorkflowItem = SHOW_ADV_INFO;
		resetPanelItems(m_XONWorkflowPanel, m_ShowWorkflowItem,
				m_WorkflowPanelItems);
		m_XONWorkflowPanel.repaint();
	}

	public void resetImageDesigner() {
		m_XONCanvasPanel.resetActionPerformed(null);
		resetWorkflowPanel();
		m_ThumbnailPane.repaint();
		m_XONCanvasPanel.repaint();
		resetXONImagePane(SHOW_CANVAS_PANEL);
	}

	JButton m_SelectedButton = null;

	public void actionPerformed(ActionEvent evnt) {
		String actionCmnd = evnt.getActionCommand();
		RGPTLogger.logToFile("Action Cmnd: " + actionCmnd);
		if (actionCmnd == null || actionCmnd.length() == 0)
			return;
		String[] cmnd = evnt.getActionCommand().split("=");
		setButtonSelection(cmnd[1]);
		// This part of the code is executed to reset the Preview Panel
		if (m_ShowPreviewItem == SHOW_IMAGE_FILTER) {
			if (!RGPTUtil.contains(SHOW_IMAGE_FILTER_ACTIONS, cmnd[1])
					&& !RGPTUtil.isImageFilterActionPerformed(cmnd[1]))
				resetPreviewPanel();
		}
		VDPFieldInfo selVDPFldInfo = m_XONCanvasPanel.m_SelVDPFieldInfo;
		if (selVDPFldInfo != null) {
			selVDPFldInfo.resetFields();
			if (RGPTUtil
					.isImageFilterActionPerformed(m_XONCanvasPanel.m_ActionPerformedId))
				m_XONCanvasPanel.repaint();
		}
		if (m_ActionSelection.get(cmnd[1]) != null)
			m_SelectionModeLabel.setText(m_ActionSelection.get(cmnd[1]));
		this.setCursor(cmnd[1]);
		if (RGPTUtil.isWFActionPerformed(cmnd[1]))
			performRGPTWFAction(cmnd[1]);
		else if (RGPTUtil.isDialogActionPerformed(cmnd[1]))
			performRGPTDialogAction(cmnd[1]);
		else if (RGPTUtil.isImageFilterActionPerformed(cmnd[1]))
			performImageFilterAction(cmnd[1]);
		else
			performRGPTAction(cmnd[1]);
		this.resetCursor();
	}

	// The Set Cursor will not work if the Paint Function is taking time, it has
	// to be called from Paint function
	// itself, as Paint is a asynch operation.
	public void setCursor(String action) {
		String[] busyCursorIds = RGPTActionListener.BUSY_CURSOR_ACTIONS;
		Arrays.sort(busyCursorIds);
		int index = Arrays.binarySearch(busyCursorIds, action);
		if (index < 0)
			return;
		this.setCursor(CursorController.m_BusyCursor);
	}

	public void resetCursor() {
		this.setCursor(CursorController.m_DefaultCursor);
	}

	public void performImageFilterAction(String actionStr) {
		float filterVal = 0.0f;
		Map<String, JComponent> actionComp = null;
		this.setCursor(CursorController.m_BusyCursor);
		JSlider slider = null;
		JTextField textBox = null;
		String[] filterParams = null;
		ImageFilterHandler imgFilterHdlr = null;
		VDPImageFieldInfo selVDPFldInfo = null;
		actionComp = m_ImageFilterActionComponent
				.get(m_XONCanvasPanel.m_ActionPerformedId);
		selVDPFldInfo = (VDPImageFieldInfo) m_XONCanvasPanel.m_SelVDPFieldInfo;
		ImageFilterController imgFilterCtr = selVDPFldInfo
				.getImageFilterController();
		ImageFilterActions action = ImageFilterActions.valueOf(actionStr);
		RGPTLogger.logToFile("Image Filter Action is: " + action.toString());
		filterParams = action.toString().split("_");
		if (filterParams.length == 1) {
			imgFilterHdlr = imgFilterCtr.getImageFilterHandler(action
					.toString());
			String filterAction = "_Off";
			if (imgFilterHdlr == null)
				filterAction = "_On";
			else {
				float status = imgFilterHdlr.getValue();
				// Checking is the status is On Status, If currently in On
				// Status, the filterAction is set to Active and
				// from Active to Off Status
				if ((selVDPFldInfo.getImageFilterAction() == null)
						&& (status == 1.0f || status == 2.0f))
					filterAction = "_Off";
				else if ((selVDPFldInfo.getImageFilterAction() != null)
						&& (status == 1.0f || status == 2.0f))
					filterAction = "_Off";
				else
					filterAction = "_Off";
			}
			imgFilterHdlr = selVDPFldInfo.setImageFilterParam(filterParams[0]
					+ filterAction, -1.0f);
			JPanel thumbFilterPane = m_ThumbFilterPane
					.get(m_XONCanvasPanel.m_ActionPerformedId);
			thumbFilterPane.repaint();
		} else if (action.toString().contains("Slider")) {
			slider = (JSlider) actionComp.get(action.toString());
			imgFilterHdlr = selVDPFldInfo.setImageFilterParam(
					action.toString(), slider.getValue());
			if (imgFilterHdlr != null)
				filterVal = imgFilterHdlr.getValue();
			textBox = (JTextField) actionComp.get(filterParams[0] + "_TextBox");
			textBox.setText(String.valueOf(filterVal));
		} else if (action.toString().contains("TextBox")) {
			textBox = (JTextField) actionComp.get(action.toString());
			float value = Float.valueOf(textBox.getText()).floatValue();
			imgFilterHdlr = selVDPFldInfo.setImageFilterParam(
					action.toString(), value);
			if (imgFilterHdlr != null)
				filterVal = imgFilterHdlr.getValue();
			slider = (JSlider) actionComp.get(filterParams[0] + "_Slider");
			textBox.setText(String.valueOf(filterVal));
			slider.setValue((int) filterVal);
		} else
			imgFilterHdlr = selVDPFldInfo.setImageFilterParam(
					action.toString(), -1.0f);

		RGPTLogger.logToFile("Image Filter Handler is: " + imgFilterHdlr);
		selVDPFldInfo.applyFilters(m_XODImageInfo.m_ScreenToImageCTM);
		m_XONCanvasPanel.repaint();
		this.resetCursor();
	}

	public void performRGPTDialogAction(String actionStr) {
		DialogActions action = DialogActions.valueOf(actionStr);
		RGPTLogger.logToFile("Dialog Action is: " + action.toString());
		switch (action) {
		case SHAPE_TYPE_COMBO_ACTION:
		case SHAPE_TYPE_FIELD_ACTION:
		case SHAPE_NAME_FIELD_ACTION:
		case SAVE_SHAPE_OK_ACTION:
		case SAVE_SHAPE_CANCEL_ACTION:
			m_XONCanvasPanel.processSaveDialog(action);
			break;
		}
	}

	public void performRGPTWFAction(String actionStr) {
		JButton button = null;
		JPopupMenu menu = null;
		WFActions action = WFActions.valueOf(actionStr);
		// Highlighting the WF Step
		boolean showMainWFSteps = false, isApproved = false;
		String parentWFAction = m_WFImageAction.get(actionStr);
		String mainWFAction = m_WFImageAction.get(parentWFAction);
		RGPTLogger.logToFile("WF Action is: " + action.toString()
				+ " Parent WF Action: " + parentWFAction + " Main WF Action: "
				+ mainWFAction);
		if (!isShapeDesign() && m_XONCanvasPanel.m_SelVDPFieldInfo == null) {
			RGPTLogger.logToFile("ERROR: NO FIELD IS SELECTED");
			return;
		}
		switch (action) {
		case LAUNCH_IMAGE_MAKEOVER_STEP:
			launchImageMakerApp();
			break;
		case NEW_WF_IMAGE_FILTERS_STEP:
			RGPTLogger.logToFile("Setting Image Filters for: "
					+ m_XONCanvasPanel.m_SelVDPFieldInfo.m_FieldName);
			resetWorkflowPanel(action.toString(), SHOW_WORKFLOW_STEPS);
			break;
		case IMAGE_EFFECTS_FILTERS:
		case SET_IMAGE_FILTERS:
			m_XONCanvasPanel.setActionPerformed(action.toString(), false);
			showMenuItem(IDAppsActions.valueOf("MA_ADD_IMAGE"), action);
			break;
		case IMAGE_ENHANCE_FILTERS:
		case FUNNY_EFFECT_FILTERS:
		case COLOR_EFFECT_FILTERS:
		case LIGHT_EFFECT_FILTERS:
		case FADE_EFFECT_FILTERS:
		case BORDER_EFFECT_FILTERS:
		case COLOR_FILTERS:
		case DISTORTION_FILTERS:
		case BLUR_FILTERS:
			RGPTLogger.logToFile("Setting for: " + action + " Fld Name: "
					+ m_XONCanvasPanel.m_SelVDPFieldInfo.m_FieldName);
			m_XONCanvasPanel.setActionPerformed(action.toString(), false);
			resetPreviewPanel(action.toString(), SHOW_IMAGE_FILTER);
			break;
		case END_IMAGE_FILTER_SETTINGS:
			showMainWFSteps = true;
			break;
		case DRAW_SHAPE_PATH_STEP:
		case ADJUST_SHAPE_PATH_STEP:
			m_XONCanvasPanel.setActionPerformed(action.toString(), true);
			break;
		case PREVIEW_SHAPE:
		case PREVIEW_IMAGE_CUTOUT:
			m_XONCanvasPanel.previewPath(action.toString());
			break;
		case SAVE_SHAPE:
			m_XONCanvasPanel.savePath(action.toString());
			break;
		case END_SETTINGS:
			resetImageDesigner();
			break;
		case END_CREATE_SHAPE_WF:
			resetImageDesigner();
			isApproved = RGPTUIUtil
					.getUserApproval(this, "END_CREATE_SHAPE_WF");
			if (isApproved)
				closeFile();
			break;
		case RESET_WF:
			isApproved = RGPTUIUtil.getUserApproval(this, "RESET_WF_INFO");
			if (isApproved)
				resetImageDesigner();
			break;
		}
		if (showMainWFSteps) {
			// Resetting the Workflow Panel
			if (mainWFAction != null)
				resetWorkflowPanel(mainWFAction, SHOW_WORKFLOW_STEPS);
			else
				RGPTLogger.logToFile("WF Action: " + mainWFAction + " is Null");
		}
	}

	public void setButtonSelection(String action) {
		JButton actionButton = m_ActionButtonMap.get(action);
		// Disabling Slider if the action is not set alpha value
		if (!action.equals(IDAppsActions.TB_SET_ALPHA_VALUE.toString())) {
			JSlider slider = (JSlider) m_ToolBarComponents
					.get(IDAppsActions.TB_SET_ALPHA_VALUE.toString());
			if (slider != null)
				slider.setEnabled(false);
		}
		if (m_SelectedButton != null) {
			m_SelectedButton.setSelected(false);
		}
		if (actionButton != null) {
			m_SelectedButton = actionButton;
			m_SelectedButton.setSelected(true);
		} else
			m_SelectedButton = null;
	}

	private String getMainAction4SelField() {
		String action = null;
		VDPFieldInfo selVDPFldInfo = m_XONCanvasPanel.m_SelVDPFieldInfo;
		if (selVDPFldInfo == null)
			return null;
		if (selVDPFldInfo.isTextField())
			action = IDAppsActions.MA_ADD_TEXT.toString();
		else if (selVDPFldInfo.isImageField())
			action = IDAppsActions.MA_ADD_IMAGE.toString();
		else if (selVDPFldInfo.isShapeField())
			action = IDAppsActions.MA_ADD_SHAPE.toString();
		return action;
	}

	// This method is called by MneomonicKeyListener when user presses ctrl O,
	// etc
	public void performRGPTAction(String actionStr) {
		boolean showWFSteps = false, activateTB = true;
		String[] activationIds = null;
		IDAppsActions action = IDAppsActions.valueOf(actionStr), wfAction = action;
		VDPFieldInfo selVDPFldInfo = m_XONCanvasPanel.m_SelVDPFieldInfo;
		if (selVDPFldInfo != null)
			selVDPFldInfo.setFieldActions(action);
		IDAppsActions buttonEnabledAction = action;
		String wfStepInit = null;
		RGPTLogger.logToFile("Image Designer Actions is: " + action.toString());
		switch (action) {
		// File Action Buttons
		case FILE_OPEN:
			this.openFile(action);
			break;
		case FILE_CLOSE:
			this.closeFile();
			break;
		case CREATE_SHAPE:
			if (!this.openShapeFile())
				return;
			activationIds = new String[] { "FILE_CLOSE", "INCREASE_ZOOM",
					"DECREASE_ZOOM" };
			m_XONCanvasPanel.setWorkFlowProcessId(action.toString());
			resetPreviewPanel(action.toString(), SHOW_SHAPE_PREVIEW);
			wfStepInit = WFActions.DRAW_SHAPE_PATH_STEP.toString();
			break;
		case MA_ADD_IMAGE:
			if (this.addImageFile(action)) {
				showWFSteps = true;
				buttonEnabledAction = IDAppsActions.TB_TRANSLATE_SHAPE;
			} else {
				activateButtons(true);
				m_SelectedButton.setSelected(false);
				return;
			}
			break;
		case TB_SELECT_ELEMENT:
			resetImageDesigner();
			break;
		case MA_SELECT_BG:
			wfAction = IDAppsActions.MA_ADD_IMAGE;
			activationIds = new String[] { "TB_SELECT_ELEMENT", "TB_SET_ALPHA" };
			break;
		case TB_EDIT_ELEMENT:
			showWFSteps = true;
			if (selVDPFldInfo != null) {
				if (selVDPFldInfo.isTextField())
					wfAction = IDAppsActions.MA_ADD_TEXT;
				else if (selVDPFldInfo.isImageField())
					wfAction = IDAppsActions.MA_ADD_IMAGE;
				else if (selVDPFldInfo.isShapeField())
					wfAction = IDAppsActions.MA_ADD_SHAPE;
				else
					showWFSteps = false;
			} else
				showWFSteps = false;
			break;
		case TB_SHOW_HIDE_ELEM:
			if (selVDPFldInfo != null && m_XONCanvasPanel.containsSelField()) {
				if (selVDPFldInfo.m_ShowElement) {
					selVDPFldInfo.m_ShowElement = false;
					selVDPFldInfo.m_IsFieldSelected = false;
					resetImageDesigner();
				} else {
					selVDPFldInfo.m_ShowElement = true;
					selVDPFldInfo.m_IsFieldSelected = true;
				}
				RGPTLogger.logToFile(selVDPFldInfo.m_FieldName + " is shown: "
						+ selVDPFldInfo.m_ShowElement);
				m_XONCanvasPanel.repaint();
				m_ThumbnailPane.repaint();
			}
			break;
		case TB_TRANSLATE_SHAPE:
		case TB_SCALE_SHAPE:
		case TB_ROTATE_SHAPE:
		case TB_EDIT_SHAPE:
			m_XONCanvasPanel.setActionPerformed(action.toString(), false);
			m_XONCanvasPanel.repaint();
			break;
		case TB_DELETE_ELEM:
			if (selVDPFldInfo != null) {
				boolean isApproved = RGPTUIUtil.getUserApproval(this,
						"DELETE_FIELD_INFO");
				if (isApproved) {
					if (!m_XODImageInfo.removeField(selVDPFldInfo))
						RGPTUIUtil.showInfoMesg(this, null,
								"DELETE_FIELD_FAILED_INFO");
					resetImageDesigner();
				}
			}
			break;
		case TB_SET_ALPHA:
			if (selVDPFldInfo != null) {
				JSlider slider = (JSlider) m_ToolBarComponents
						.get(IDAppsActions.TB_SET_ALPHA_VALUE.toString());
				if (slider != null) {
					slider.setEnabled(true);
					slider.setValue((int) (selVDPFldInfo.m_AlphaValue * 10));
					m_XONCanvasPanel.setActionPerformed(action.toString(),
							false);
				}
			}
			break;
		case TB_SET_ALPHA_VALUE:
			if (selVDPFldInfo != null) {
				JSlider slider = (JSlider) m_ToolBarComponents
						.get(IDAppsActions.TB_SET_ALPHA_VALUE.toString());
				if (slider != null) {
					double alpha = (double) slider.getValue() / 10.0;
					RGPTLogger.logToFile("Alpha Value: " + alpha
							+ "Slider Value: " + slider.getValue());
					selVDPFldInfo.m_AlphaValue = alpha;
					m_XONCanvasPanel.repaint();
				}
			}
			break;
		}
		// Resetting the Action Performed on Canvas Panel as soon as the new
		// Action is Performed.
		JButton button = m_ActionButtonMap.get(buttonEnabledAction.toString());
		if (button != null) {
			if (!action.toString().equals(buttonEnabledAction.toString())) {
				if (m_SelectedButton != null)
					m_SelectedButton.setSelected(false);
				m_SelectedButton = button;
				m_SelectedButton.setSelected(true);
			}
		}
		Vector<String> wfProcIds = RGPTUtil.createVector(WF_PROCESS_IDS);
		if (wfProcIds.contains(wfAction.toString()) || showWFSteps) {
			if (activationIds != null && activationIds.length > 0)
				setButtonActivations(null, activationIds);
			else
				activateButtons(false);
			// m_XONCanvasPanel.resetActionPerformed(wfAction.toString());
			// Resetting the Workflow Panel
			m_XONCanvasPanel.setWorkFlowProcessId(wfAction.toString());
			resetWorkflowPanel(wfAction.toString(), SHOW_WORKFLOW_STEPS);
			m_XONCanvasPanel.setActionPerformed(buttonEnabledAction.toString(),
					true);
			if (wfStepInit != null)
				performRGPTWFAction(wfStepInit);
		}
	}

	// This method returns the mneomonic key for the action id else null
	public String getMnemonicKey(String action) {
		return m_ActionMnemonicKeysMap.get(action);
	}

	public void setAppTitle(String title) {
		this.setTitle(title);
	}

	public void resetApp() {
		this.setCursor(CursorController.m_BusyCursor);
		this.setTitle(m_Title + ": "
				+ m_XODImageInfo.m_CanvasImageHolder.m_FileName);
		RGPTUIUtil.resetDisplayComponent(m_ContentPane, m_XONImageMakerPane,
				m_XONContentPane);
		VDPImageFieldInfo vdpImgFld = (VDPImageFieldInfo) m_XONCanvasPanel.m_SelVDPFieldInfo;
		try {
			vdpImgFld.m_ImageHolder.saveTransformedImage();
			vdpImgFld.setGPathPts(m_XODImageInfo.m_ScreenToImageCTM, true);
			vdpImgFld.m_ImageMakeoverActivated = false;
		} catch (Exception ex) {
			RGPTLogger.logToFile(
					"Exception while extracting Transformed Image", ex);
			RGPTUIUtil.showErrorMesg(this, null, "ERROR_IMAGE_TRANSFORM");
			return;
		}
		this.repaint();
		resetCursor();
	}

	// END FUNCTION FOR INTERFACE METHODS

	// START FUNCTION FOR LOGIC PROCESSING

	// This method is used to Show Menu Item attached to buttons
	private void showMenuItem(IDAppsActions imgAction, WFActions wfAction) {
		int stepHt = RGPTParams.getIntVal("WFStepHeight");
		Vector<JButton> actionButtons = m_WFActionSteps.get(imgAction
				.toString());
		JButton button = RGPTUIUtil.getWFStep(actionButtons,
				wfAction.toString());
		if (button == null) {
			RGPTLogger.logToFile("Unable to Find button for: " + imgAction
					+ " and WF: " + wfAction);
			return;
		}
		JPopupMenu menu = m_ActionPopupMenu.get(wfAction.toString());
		menu.show(button, 0, stepHt);
	}

	public void launchImageMakerApp() {
		if (m_XONCanvasPanel.m_SelVDPFieldInfo == null
				&& !(m_XONCanvasPanel.m_SelVDPFieldInfo instanceof VDPImageFieldInfo))
			return;
		this.setCursor(CursorController.m_BusyCursor);
		VDPImageFieldInfo vdpImgFld = (VDPImageFieldInfo) m_XONCanvasPanel.m_SelVDPFieldInfo;
		RGPTUIUtil.resetDisplayComponent(m_ContentPane, m_XONContentPane,
				m_XONImageMakerPane);
		m_XONImageMakerApp.performImageMakeover(this, vdpImgFld.m_ImageHolder);
		vdpImgFld.m_ImageMakeoverActivated = true;
		m_ContentPane.repaint();
		m_ContentPane.revalidate();
		resetCursor();
	}

	private boolean addImageFile(IDAppsActions action) {
		LocalizationUtil lu = new LocalizationUtil();
		// Use FileChooser to get the Image File and Image Holder Object
		ImageHolder imgHolder = RGPTUIUtil.getImageFile(this);
		BufferedImage infoImg = null;
		boolean showError = false;
		if (imgHolder != null) {
			try {
				if (m_XONCanvasPanel.m_SelVDPFieldInfo != null) {
					m_XONCanvasPanel.m_SelVDPFieldInfo.resetFields();
					m_XONCanvasPanel.m_SelVDPFieldInfo.m_IsFieldSelected = false;
					m_XONCanvasPanel.m_SelVDPFieldInfo.m_IsBGCanvasSelected = false;
				}
				m_XONCanvasPanel.m_SelVDPFieldInfo = m_XODImageInfo
						.addImageField(imgHolder);
				m_XONCanvasPanel.m_SelVDPFieldInfo.m_IsFieldSelected = true;

				m_ThumbnailPane.repaint();
				m_XONCanvasPanel.repaint();
			} catch (Exception ex) {
				showError = true;
				RGPTLogger.logToFile("Exception while adding Image. ", ex);
			}
		} else
			showError = true;
		if (showError) {
			String[] errorInfo = lu.getText("ERROR_OPEN_FILE").split("::");
			RGPTUIUtil.showError(this, errorInfo[1], errorInfo[0]);
			return false;
		}
		return true;
	}

	// Specifies XON Design Type
	public StaticFieldInfo.XONDesignType m_XONDesignType = null;

	public boolean isShapeDesign() {
		if (m_XONDesignType == StaticFieldInfo.XONDesignType.SHAPE_DESIGNS)
			return true;
		return false;
	}

	private boolean openFile(IDAppsActions action) {
		return openFile(action, null);
	}

	private boolean openShapeFile() {
		return openFile(IDAppsActions.FILE_OPEN,
				StaticFieldInfo.XONDesignType.SHAPE_DESIGNS);
	}

	private boolean openFile(IDAppsActions action,
			StaticFieldInfo.XONDesignType xonDesnType) {
		BufferedImage infoImg = null;
		boolean showError = false;
		LocalizationUtil lu = new LocalizationUtil();
		ImageHolder imgHolder = null;
		// Use FileChooser to get the Image File and Image Holder Object
		m_XONDesignType = xonDesnType;
		if (xonDesnType == StaticFieldInfo.XONDesignType.SHAPE_DESIGNS)
			imgHolder = RGPTUIUtil.getImageFile(this);
		else {
			File file = RGPTUIUtil.getXONDesignFile(this);
			if (file != null) {
				RGPTFileFilter fileFltr = FileFilterFactory.getFileFilter(file);
				if (fileFltr.m_FileType.equals("IMAGE")) {
					imgHolder = RGPTUIUtil.getImageHolder(file);
					m_XONDesignType = StaticFieldInfo.XONDesignType.IMAGE_DESIGNS;
				} else if (fileFltr.m_FileType.equals("PDF")) {
					m_XONDesignType = StaticFieldInfo.XONDesignType.PDF_DESIGNS;
					return true;
				} else if (fileFltr.m_FileType.equals("XON_PDF_IMAGE")) {
					m_XONDesignType = StaticFieldInfo.XONDesignType.IMAGE_DESIGNS;
					return true;
				}
				if (xonDesnType != null)
					m_XONDesignType = xonDesnType;
			} else
				showError = true;
		}
		if (imgHolder != null) {
			try {
				int thumbImgWt = RGPTParams.getIntVal("ThumbviewImageWidth");
				int thumbImgHt = RGPTParams.getIntVal("ThumbviewImageHeight");
				// Compress the Image File and save it in the Image Holder
				imgHolder.setImageData(thumbImgWt, thumbImgHt, true);
				if (imgHolder.m_ImageHasAlpha) {
					infoImg = ImageUtils.createColorImage(
							imgHolder.m_BackgroundColor, "InfoMesgImageSize");
					// RGPTUIUtil.showInfoMesg(this, infoImg, action+"_INFO");
				}
			} catch (Exception ex) {
				showError = true;
				RGPTLogger.logToFile("Exception while opening Canvas Image. ",
						ex);
			}
		} else
			showError = true;
		if (showError) {
			String[] errorInfo = lu.getText("ERROR_OPEN_FILE").split("::");
			RGPTUIUtil.showError(this, errorInfo[1], errorInfo[0]);
			return false;
		}
		// Open the Image File using the XONImagePanel
		m_XODImageInfo = new XODImageInfo(imgHolder);
		m_XONCanvasPanel.setXODImageInfo(m_XODImageInfo);
		m_XONCanvasPanel.setDoubleBufferingActivation(true);
		this.setTitle(m_Title + ": " + imgHolder.m_FileName);
		resetFilePanel(m_FileOpenClosePanel, action.toString(),
				IDAppsActions.FILE_CLOSE.toString());
		// resetFilePanel(m_FileNewInsertPanel,
		// IDAppsActions.FILE_NEW.toString(),
		// IDAppsActions.ADD_IMAGE.toString());
		resetXONImagePane(SHOW_CANVAS_PANEL);
		return true;
	}

	public void closeFile() {
		RGPTLogger.logToFile("Closing Image File");
		m_XODImageInfo.cleanup();
		m_XODImageInfo = null;
		m_ZoomInfoField.setText("0");
		this.setTitle(m_Title);
		m_SelectionModeLabel.setText("");
		m_XONCanvasPanel.resetXONImage();
		resetFilePanel(m_FileOpenClosePanel,
				IDAppsActions.FILE_CLOSE.toString(),
				IDAppsActions.FILE_OPEN.toString());
		// resetFilePanel(m_FileNewInsertPanel,
		// IDAppsActions.ADD_IMAGE.toString(),
		// IDAppsActions.FILE_NEW.toString());
		resetXONImagePane(SHOW_XON_ICON);
		resetPreviewPanel();
		resetWorkflowPanel();
	}

	private void resetXONImagePane(int xonImagePaneItem) {
		m_ShowXONImagePane = xonImagePaneItem;
		if (m_ShowXONImagePane == SHOW_XON_ICON)
			setButtonActivations(null, FILE_CLOSED_ACTIVATED_IDS);
		else
			activateButtons(true);
		resetPanelItems(m_XONImageContentPane, xonImagePaneItem,
				m_XONImageContentItem);
		m_XONImageContentPane.repaint();
		m_ThumbnailPane.repaint();
		m_AdsViewPane.repaint();
	}

	private void resetPanelItems(JPanel mainPanel, int dispCompId,
			Map<Integer, JComponent> panelItems) {
		JComponent[] nonDispComps = getNonDisplayComponents(dispCompId,
				panelItems);
		RGPTUIUtil.resetDisplayComponent(mainPanel, nonDispComps,
				panelItems.get(dispCompId));
	}

	private JComponent[] getNonDisplayComponents(int dispCompId,
			Map<Integer, JComponent> panelItems) {
		Vector<JComponent> nonDispComps = new Vector<JComponent>();
		Integer[] keys = panelItems.keySet().toArray(new Integer[0]);
		for (int i = 0; i < keys.length; i++) {
			if (keys[i].intValue() == dispCompId)
				continue;
			nonDispComps.addElement(panelItems.get(keys[i]));
		}
		JComponent[] nonDispCompList = nonDispComps.toArray(new JComponent[0]);
		return nonDispCompList;
	}

	// Toggles between File Action Buttons like the File Open and File Close
	private void resetFilePanel(JPanel filePanel, String resetAction,
			String newAction) {
		JButton resetButton = m_ActionButtonMap.get(resetAction);
		JButton actButton = m_ActionButtonMap.get(newAction);
		RGPTUIUtil.resetDisplayComponent(filePanel, resetButton, actButton);
		filePanel.repaint();
	}

	// This activates Main Activation Buttons of IDAppsActions
	public void activateButtons(boolean isMainAction) {
		String filter = "MA_";
		if (!isMainAction)
			filter = "TB_";
		String[] idAppsButtons = RGPTUtil.enumToStringArray(IDAppsActions
				.values());
		Vector<String> activationIds = RGPTUtil.filterActionValues(
				idAppsButtons, filter);
		if (isMainAction)
			activationIds
					.addAll(RGPTUtil.createVector(FILE_OPEN_ACTIVATED_IDS));
		setButtonActivations(null, activationIds);
	}

	public void setButtonActivations(String action) {
		setButtonActivations(action, FILE_OPEN_ACTIVATED_IDS);
	}

	public void setButtonActivations(String action, String[] activationIds) {
		Vector<String> exceptionIds = RGPTUtil.createVector(activationIds);
		setButtonActivations(action, exceptionIds);
	}

	public void setButtonActivations(String action, Vector<String> exceptionIds) {
		String[] imageActions = RGPTUtil.enumToStringArray(IDAppsActions
				.values());
		for (int i = 0; i < imageActions.length; i++) {
			JButton button = m_ActionButtonMap.get(imageActions[i]);
			if (button == null) {
				RGPTLogger.logToFile("Action Button: " + imageActions[i]
						+ " does not exists");
				continue;
			}
			if ((action != null && action.equals(imageActions[i]))
					|| exceptionIds.contains(imageActions[i])) {
				// RGPTLogger.logToFile("Activating button: "+imageActions[i]);
				button.setEnabled(true);
			} else
				button.setEnabled(false);
		}
	}

	// END FUNCTION FOR LOGIC PROCESSING

	// START FUNCTION FOR REPAINT METHODS
	public static int m_RepaintImgFilterCounter = 0;

	public void repaintImageFilterThumbPane(Graphics g) {
		try {
			this.setCursor(CursorController.m_BusyCursor);
			this.showImageFilterPreview(g);
		} catch (Exception ex) {
		} finally {
			this.resetCursor();
		}
	}

	private void showImageFilterPreview(Graphics g) {
		m_RepaintImgFilterCounter++;
		Graphics2D g2d = (Graphics2D) g;
		g2d.setBackground(XONImageDesigner.m_PanelColor);
		JPanel thumbFilterPane = m_ThumbFilterPane
				.get(m_XONCanvasPanel.m_ActionPerformedId);
		m_VisiblePreviewRect = thumbFilterPane.getVisibleRect();
		// RGPTLogger.logToFile("Prview Rect: "+m_VisiblePreviewRect);
		Dimension panelSize = new Dimension(
				(int) m_VisiblePreviewRect.getWidth(),
				(int) m_VisiblePreviewRect.getHeight());
		if (m_OrigPreviewRect == null)
			m_OrigPreviewRect = m_VisiblePreviewRect;
		Dimension previewPanelSize = thumbFilterPane.getPreferredSize();
		int newPanelHt = previewPanelSize.height;
		if (newPanelHt < panelSize.height)
			newPanelHt = panelSize.height;
		if (newPanelHt < m_OrigPreviewRect.height)
			newPanelHt = m_OrigPreviewRect.height;
		g.clearRect(0, 0, panelSize.width, newPanelHt);
		ImageFilterHandler imgFilterHdlr = null;
		VDPImageFieldInfo selVDPFldInfo = null;
		selVDPFldInfo = (VDPImageFieldInfo) m_XONCanvasPanel.m_SelVDPFieldInfo;
		ImageFilterController imgFilterCtr = selVDPFldInfo
				.getImageFilterController();
		m_ImageFilterPreviewPos.clear();
		// Displaying all the images filters
		BufferedImage thumbImg = selVDPFldInfo.getPreviewImage(), filterImg = null;
		int vGap = RGPTParams.getIntVal("ThumbviewVerticalGap");
		int y = vGap, w = thumbImg.getWidth(), h = thumbImg.getHeight();
		Rectangle imgRect = new Rectangle(0, 0, 0, 0);
		boolean setSel = false;
		double x = (double) (panelSize.width - w) / (double) 2;
		if (x < 10.0D) {
			x = 10.0D;
			w = panelSize.width - (int) x * 2;
		}
		String actionId = m_XONCanvasPanel.m_ActionPerformedId;
		// RGPTLogger.logToFile(actionId+" Image Filter Actions are: "+
		// AppletParameters.getVal(actionId));
		String[] imageFilters = RGPTParams.getVal(actionId).split("::");
		for (int i = 0; i < imageFilters.length; i++) {
			ImageFilterActions action = ImageFilterActions
					.valueOf(imageFilters[i]);
			// RGPTLogger.logToFile(action+" Preview Set");
			if (i != 0)
				y += imgRect.height + vGap;
			imgFilterHdlr = imgFilterCtr.getImageFilterHandler(action
					.toString());
			if (imgFilterHdlr != null)
				setSel = true;
			filterImg = selVDPFldInfo.applyImageFilter(action);
			String text = LocalizationUtil.getText(imageFilters[i]);
			imgRect = drawImagePreview(g2d, filterImg, text, x, y, w, h, setSel);
			m_ImageFilterPreviewPos.put(action.toString(), imgRect);
			setSel = false;
		}
		newPanelHt = y + imgRect.height + vGap;
		if (newPanelHt < m_OrigPreviewRect.height)
			newPanelHt = m_OrigPreviewRect.height;
		Dimension newPanelSize = new Dimension(panelSize.width, newPanelHt);
		thumbFilterPane.setPreferredSize(newPanelSize);
		thumbFilterPane.revalidate();
		m_XONWorkflowPanel.repaint();
		m_LabelPanel.repaint();
		// RGPTLogger.logToFile("# of times Image Filter Thumbview Repaint: "+m_RepaintImgFilterCounter);
	}

	public void repaintZoomViewPane(Graphics g) {
		boolean drawPt = false;
		Color origColor = g.getColor();
		Rectangle visibleRect = m_ZoomViewPane.getVisibleRect();
		Dimension panelSize = new Dimension((int) visibleRect.getWidth(),
				(int) visibleRect.getHeight());
		// RGPTLogger.logToConsole("Zoom Preview Panel Size: "+panelSize);
		g.clearRect(0, 0, panelSize.width, panelSize.height);
		if (THUMB_ZOOM_PREVIEW_IMAGE == null) {
			THUMB_ZOOM_PREVIEW_IMAGE = ImageUtils.scaleImage(
					THUMB_IMAGE_DESIGNER_ICON, panelSize.width,
					panelSize.height, true);
			THUMB_ZOOM_PREVIEW_IMAGE = ImageUtils.fillTransparentPixels(
					THUMB_ZOOM_PREVIEW_IMAGE, Color.WHITE);
		}
		BufferedImage prevImg = THUMB_ZOOM_PREVIEW_IMAGE;
		if (m_ShowPreviewItem == SHOW_SHAPE_PREVIEW
				&& m_XONCanvasPanel.m_ZoomPreviewImage != null) {
			drawPt = true;
			prevImg = m_XONCanvasPanel.m_ZoomPreviewImage;
		}
		g.drawImage(prevImg, 0, 0, panelSize.width, panelSize.height, this);
		if (drawPt) {
			g.setColor(ImageUtils.getUniqueColor(prevImg, true));
			g.drawOval(panelSize.width / 2, panelSize.height / 2, 10, 10);
		}
		g.setColor(origColor);
	}

	public void repaintShapePreviewPane(Graphics g) {
		repaintImagePreviewPane(g, false);
	}

	public void repaintImagePreviewPane(Graphics g) {
		repaintImagePreviewPane(g, true);
	}

	Rectangle m_VisiblePreviewRect = null;
	Rectangle m_OrigPreviewRect = null;

	public void repaintImagePreviewPane(Graphics g, boolean showVDPFields) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setBackground(XONImageDesigner.m_PanelColor);
		m_VisiblePreviewRect = m_ThumbnailPane.getVisibleRect();
		Dimension panelSize = new Dimension(
				(int) m_VisiblePreviewRect.getWidth(),
				(int) m_VisiblePreviewRect.getHeight());
		if (m_OrigPreviewRect == null)
			m_OrigPreviewRect = m_VisiblePreviewRect;
		Dimension previewPanelSize = m_ThumbnailPane.getPreferredSize();
		int newPanelHt = previewPanelSize.height;
		if (newPanelHt < panelSize.height)
			newPanelHt = panelSize.height;
		if (newPanelHt < m_OrigPreviewRect.height)
			newPanelHt = m_OrigPreviewRect.height;
		g.clearRect(0, 0, panelSize.width, newPanelHt);
		// RGPTLogger.logToConsole("Panel Size: "+panelSize);
		if (THUMB_SCALED_IMAGE_DESIGNER_ICON == null) {
			int thumbImgWt = RGPTParams.getIntVal("ThumbviewImageWidth");
			int thumbImgHt = RGPTParams.getIntVal("ThumbviewImageHeight");
			THUMB_SCALED_IMAGE_DESIGNER_ICON = ImageUtils.scaleImage(
					THUMB_IMAGE_DESIGNER_ICON, thumbImgWt, thumbImgHt, true);
			THUMB_SCALED_IMAGE_DESIGNER_ICON = ImageUtils
					.fillTransparentPixels(THUMB_SCALED_IMAGE_DESIGNER_ICON,
							Color.WHITE);
		}
		BufferedImage thumbImg = THUMB_SCALED_IMAGE_DESIGNER_ICON;
		int vGap = RGPTParams.getIntVal("ThumbviewVerticalGap");
		int y = vGap, w = thumbImg.getWidth(), h = thumbImg.getHeight();
		double x = (double) (panelSize.width - w) / (double) 2;
		if (x < 5.0D)
			x = 5.0D;
		if (m_ShowXONImagePane == SHOW_XON_ICON) {
			drawImagePreview(g2d, thumbImg, "", x, y, w, h, false);
			return;
		}

		if (!showVDPFields) {
			// Displaying the canvas or background image
			thumbImg = (BufferedImage) m_XODImageInfo.m_CanvasImageHolder.m_ThumbviewImage;
			y = vGap;
			w = thumbImg.getWidth();
			h = thumbImg.getHeight();
			x = (double) (panelSize.width - w) / (double) 2;
			drawImagePreview(g2d, thumbImg, "", x, y, w, h, false);
			return;
		}

		// Displaying all the inserted images
		VDPFieldInfo vdpFld = null;
		boolean setSel = false;
		Rectangle imgRect = null;
		Vector<VDPFieldInfo> vdpFldList = m_XODImageInfo.m_VDPFieldInfoList;
		for (int i = 0; i < vdpFldList.size(); i++) {
			vdpFld = vdpFldList.elementAt(i);
			if (imgRect == null)
				y = vGap;
			else
				y += imgRect.height + vGap;
			thumbImg = (BufferedImage) vdpFld.getPreviewImage();
			w = thumbImg.getWidth();
			h = thumbImg.getHeight();
			x = (double) (panelSize.width - w) / (double) 2;
			if (x < 5.0D)
				x = 5.0D;
			if (vdpFld.m_IsFieldSelected)
				setSel = true;
			imgRect = drawImagePreview(g2d, thumbImg, "", x, y, w, h, setSel);
			vdpFld.m_PreviewPos = imgRect;
			setSel = false;
		}
		newPanelHt = y + imgRect.height + vGap;
		if (newPanelHt < m_OrigPreviewRect.height)
			newPanelHt = m_OrigPreviewRect.height;
		Dimension newPanelSize = new Dimension(panelSize.width, newPanelHt);
		m_ThumbnailPane.setPreferredSize(newPanelSize);
		m_ThumbnailPane.revalidate();
	}

	Font m_PreviewFont = null;

	private Rectangle drawImagePreview(Graphics2D g2d, BufferedImage thumbImg,
			String text, double x, int imgStartY, int w, int imgHt,
			boolean setSel) {
		int frameMargin = RGPTParams.getIntVal("FrameMargin");
		int arc = RGPTParams.getIntVal("ThumbviewRoundRectArc");
		int h = imgHt, y = imgStartY;
		if (text != null && text.length() > 0) {
			imgStartY = y + 14;
			h = imgHt + 14;
		}
		RoundRectangle2D.Double roundRect = new RoundRectangle2D.Double(x, y,
				w, h, arc, arc);
		Color origColor = g2d.getColor();
		Shape origClip = g2d.getClip();
		g2d.setColor(XONImageDesigner.m_PanelColor);
		Rectangle imgRect = new Rectangle((int) x - frameMargin, y
				- frameMargin, w + 2 * frameMargin, h + 2 * frameMargin);
		if (setSel) {
			g2d.setColor(Color.WHITE);
			g2d.fill3DRect((int) x - frameMargin, y - frameMargin, w + 2
					* frameMargin, h + 2 * frameMargin, true);
		} else
			g2d.draw3DRect((int) x - frameMargin, y - frameMargin, w + 2
					* frameMargin, h + 2 * frameMargin, true);
		g2d.setClip(roundRect);
		if (text != null && text.length() > 0) {
			Font origFont = g2d.getFont();
			if (m_PreviewFont == null) {
				m_PreviewFont = origFont.deriveFont(10.0f);
				m_PreviewFont = m_PreviewFont.deriveFont(Font.BOLD);
			}
			g2d.setColor(Color.BLACK);
			g2d.setFont(m_PreviewFont);
			java.awt.FontMetrics fm = g2d.getFontMetrics(m_PreviewFont);
			Rectangle2D textRect = fm.getStringBounds(text, g2d);
			double startx = x + (w - textRect.getWidth()) / 2;
			if (startx < x)
				startx = x;
			float starty = y + 10.0f;
			g2d.drawString(text, (float) startx, starty);
			// RGPTLogger.logToFile("New Font: "+m_PreviewFont+" And Orig Font: "+origFont);
			g2d.setFont(origFont);
		}
		g2d.drawImage(thumbImg, (int) x, imgStartY, w, imgHt, this);
		g2d.setColor(origColor);
		g2d.setClip(origClip);
		return imgRect;
	}

	public void repaintAdsViewPane(Graphics g) {
		boolean drawPt = false;
		Color origColor = g.getColor();
		Graphics2D g2d = (Graphics2D) g;
		Rectangle visibleRect = m_AdsViewPane.getVisibleRect();
		Dimension panelSize = new Dimension((int) visibleRect.getWidth(),
				(int) visibleRect.getHeight());
		// RGPTLogger.logToConsole("Ads Panel Size: "+panelSize);
		g.clearRect(0, 0, panelSize.width, panelSize.height);
		BufferedImage prevImg = THUMB_IMAGE_DESIGNER_ICON;
		// Insert code for Ads, other wise default Image Designer Icon will be
		// shown
		Map<String, Object> derivedData = ImageUtils.deriveDeviceCTM(
				ScalingFactor.FIT_PAGE, panelSize, 0, prevImg.getWidth(),
				prevImg.getHeight());
		AffineTransform deviceCTM = (AffineTransform) derivedData
				.get("WindowDeviceCTM");
		g2d.drawImage(prevImg, deviceCTM, this);
	}

	// END FUNCTION FOR REPAINT METHODS

	public static void main(String[] args) {
		RGPTLogger.m_LogFile = RGPTLogger.XON_ID_LOG;
		RGPTParams.createServerProperties();
		Color colorpanel, buttonColor, fontColor;
		colorpanel = RGPTUtil.getColVal(RGPTParams.getVal("PANEL_COLOR"), ":");
		buttonColor = RGPTUtil
				.getColVal(RGPTParams.getVal("BUTTON_COLOR"), ":");
		fontColor = RGPTUtil.getColVal(RGPTParams.getVal("FONT_COLOR"), ":");
		XONImageDesigner.m_PanelColor = colorpanel;
		XONImageDesigner.m_ButtonColor = buttonColor;
		XONImageDesigner.m_FontColor = fontColor;
		new XONImageDesigner();
	}

}
