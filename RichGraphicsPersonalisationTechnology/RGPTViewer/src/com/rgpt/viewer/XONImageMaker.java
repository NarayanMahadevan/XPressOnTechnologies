// RGPT PACKAGES
package com.rgpt.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
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
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import com.rgpt.imageutil.ImageHolder;
import com.rgpt.imageutil.ImageTransformHolder;
import com.rgpt.imageutil.ImageUtils;
import com.rgpt.layoututil.BasicGridLayout;
import com.rgpt.util.CursorController;
import com.rgpt.util.LocalizationUtil;
import com.rgpt.util.RGPTActionListener;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTParams;
import com.rgpt.util.RGPTUIManager;
import com.rgpt.util.RGPTUIUtil;
import com.rgpt.util.RGPTUtil;
import com.rgpt.util.XONAppLaucherInterface;

public class XONImageMaker extends JFrame implements RGPTActionListener {
	// lightcolor for panel
	public static Color m_PanelColor;
	// darkercolor for button
	public static Color m_ButtonColor;
	// darkestcolor for text
	public static Color m_FontColor;

	// Title
	public String m_Title = LocalizationUtil.getText("XONIMageMakerTitle");

	// West Panel Width
	public static int m_WestPanelWidth = 100;
	public static int m_SouthPanelHeight;

	// Use Windows Look and Feel
	public static boolean m_UseWindowsLookAndFeel = true;

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
	// This Panel shows all the Preview Images
	JPanel m_ThumbnailPane;
	// This Panel shows all the Zoom Preview of Image being worked on
	JPanel m_ZoomViewPane;

	// This specifies the zoom value of the Image Openened
	JTextField m_ZoomInfoField;

	Dimension m_ContentPaneSize;

	// This Label shows the Current Selection Mode
	JLabel m_SelectionModeLabel;

	ImageHolder m_OrigImageHolder;

	// This Panel is used for File Open and Close Button
	JPanel m_FileOpenClosePanel;

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
	XONImagePanel m_XONImageViewerPanel = null;

	// This Panel is added to the content pane and is visible when no files are
	// open
	JPanel m_XONImageMakerIconPanel;

	// This Map maintains the Panels that needs to be displayed ImageContentPane
	// which
	// is the center contant pane
	public Map<Integer, JComponent> m_XONImageContentItem;

	// This shows the different items that can be shown in a ImageContentPane
	public static final int SHOW_XON_ICON = 0;
	public static final int SHOW_IMAGE_PANEL = 1;
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

	// This map maintains the HashMap of ActionId to the Corresponding Button
	// Selection to be
	// showed on the Status Bar
	public static Map<String, String> m_ActionSelection;

	// This Image show up in the Content Pane when no files are opened
	private static final BufferedImage XON_IMAGE_MAKER_ICON = ImageUtils
			.getFinalBufferedImage("res/XPressOnImageMaker.jpg",
					XONImageMaker.class);;
	// This Image is the thumb image that shows up in the Preview Pane and Zoom
	// Pane
	// when no files are opened
	private static final BufferedImage THUMB_IMAGE_MAKER_ICON = ImageUtils
			.getFinalBufferedImage("res/rgpticons/XONImageMakerLogo.png",
					XONImageMaker.class);
	private static BufferedImage THUMB_SCALED_IMAGE_MAKER_ICON;

	// MNEMONIC_KEYS correspond to Action Id
	static {
		m_ActionMessage = new HashMap<String, String>();
		m_ActionSelection = new HashMap<String, String>();
		m_ActionMnemonicKeysMap = new HashMap<String, String>();
		m_ActionMnemonicKeysMap.put(ImageActions.IMAGE_FILE_OPEN.toString(),
				"control O");
		m_ActionMnemonicKeysMap.put(ImageActions.IMAGE_FILE_CLOSE.toString(),
				"control W");
		m_ActionMnemonicKeysMap.put(ImageActions.IMAGE_FILE_SAVE.toString(),
				"control S");
		m_ActionMnemonicKeysMap.put(ImageActions.IMAGE_UPLOAD.toString(),
				"control U");
	}

	// START FUNCTION TO CONSTRUCT AND DISTRUCT THIS OBJECT

	public XONImageMaker() {
		this(true, null);
	}

	// This ImageMakerApp can be an independent App or can be invoked inside
	// another
	// App. If isWindowsAppis set to true, this is set as an independent App.
	// Content Pane Size is specified if this is not an Windows App
	public XONImageMaker(boolean isWindowsApp, Dimension contentPaneSize) {
		// AppletParameters.createServerProperties(this.getClass(), false);
		m_WorkflowPanelItems = new HashMap<Integer, JComponent>();
		m_XONImageContentItem = new HashMap<Integer, JComponent>();
		m_WFActionSteps = new HashMap<String, Vector>();
		m_WFImageAction = new HashMap<String, String>();
		m_ActionPopupMenu = new HashMap<String, JPopupMenu>();
		m_PopupMenuImageAction = new HashMap<String, String>();
		m_XONImageViewerPanel = new XONImagePanel(this);
		m_WestPanelWidth = RGPTParams.getIntVal("WestPanelWidth");
		m_SouthPanelHeight = RGPTParams.getIntVal("SouthPanelHeight");
		m_ActionWFPanelMap = new HashMap<String, JPanel>();
		m_ActionButtonMap = new HashMap<String, JButton>();
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
			ex.printStackTrace();
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
				closeImageMakerApp();
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

	public void closeImageMakerApp() {
		System.exit(0);
	}

	public JPanel createContentPane() {
		m_ContentPane = new JPanel(new BorderLayout());
		m_ContentPane.setPreferredSize(m_ContentPaneSize);
		createXONImageMakerPane();
		m_ContentPane.add(m_XONContentPane, BorderLayout.CENTER);
		// Creating the UI Component
		this.createUI();
		return m_ContentPane;
	}

	public JPanel createXONImageMakerPane() {
		m_XONContentPane = new JPanel(new BorderLayout());
		m_XONContentPane.setBackground(Color.WHITE);
		return m_XONContentPane;
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
		} catch (Exception ex) {
			RGPTLogger.logToFile("Exception at createUI ", ex);
		}
	}

	private JPanel createCenterPane() throws Exception {
		JPanel centerPane = new JPanel(new BorderLayout());
		JPanel centerContentPane = this.createCenterContentPanel();
		centerPane.add(centerContentPane, BorderLayout.CENTER);

		m_XONWorkflowPanel = new JPanel(new BorderLayout());
		int panelHt = RGPTParams.getIntVal("NorthPanelHeight");
		int panelWt = m_ContentPaneSize.width - m_WestPanelWidth
				- RGPTParams.getIntVal("ImageMakerEastPanelWidth");
		Dimension northPanelSz = new Dimension(panelWt, panelHt);
		m_XONWorkflowPanel.setPreferredSize(northPanelSz);
		String lblTxt = LocalizationUtil.getText("AdvText"), fontSzProp = "LabelFontSize";
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
		this.createXONImageMakerIconPane();
		this.createXONIMageViewerUI();
		m_XONImageContentItem.put(SHOW_XON_ICON, m_XONImageMakerIconPanel);
		m_XONImageContentPane
				.add(m_XONImageMakerIconPanel, BorderLayout.CENTER);
		centerContentPane.add(m_XONImageContentPane, BorderLayout.CENTER);
		JPanel eastPanel = this.createEastPanel();
		centerContentPane.add(eastPanel, BorderLayout.EAST);
		return centerContentPane;
	}

	private void createXONIMageViewerUI() {
		JPanel imageViewerPanel = new JPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(m_XONImageViewerPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		imageViewerPanel.add(scrollPane, BorderLayout.CENTER);
		m_XONImageContentItem.put(SHOW_IMAGE_PANEL, imageViewerPanel);
	}

	// This Method creates Panel to display XPressOnDesign Image
	private void createXONImageMakerIconPane() throws Exception {
		if (m_XONImageMakerIconPanel != null)
			return;
		ImageUtils iu = new ImageUtils();
		m_XONImageMakerIconPanel = new JPanel() {
			public void paint(Graphics g) {
				Rectangle visibleRect = this.getVisibleRect();
				Dimension panelSize = new Dimension(
						(int) visibleRect.getWidth(),
						(int) visibleRect.getHeight());
				// RGPTLogger.logToConsole("Panel Size: "+panelSize);
				g.drawImage(XON_IMAGE_MAKER_ICON, 0, 0, panelSize.width,
						panelSize.height, this);
			}
		};
	}

	private JPanel createEastPanel() {
		LocalizationUtil lu = new LocalizationUtil();
		JPanel eastPanel = new JPanel(new BorderLayout());
		int sthEastHt = RGPTParams.getIntVal("ImageMakerSouthEastPanelHeight");
		int eastPanelWt = RGPTParams.getIntVal("ImageMakerEastPanelWidth");
		Dimension eastPanelSz = new Dimension(eastPanelWt,
				m_ContentPaneSize.height);
		eastPanel.setPreferredSize(eastPanelSz);

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
		eastCenterPanel.add(thumbViewPane, BorderLayout.CENTER);
		eastPanel.add(eastCenterPanel, BorderLayout.CENTER);

		JPanel southEastPanel = new JPanel();
		Dimension sthEastPanelSz = new Dimension(eastPanelWt, sthEastHt);
		southEastPanel.setPreferredSize(sthEastPanelSz);
		southEastPanel
				.setLayout(new BoxLayout(southEastPanel, BoxLayout.Y_AXIS));
		lblTxt = lu.getText("ImageZoomPreview");
		JPanel zoomLabel = RGPTUIUtil.createLabelPanel(lblTxt, wt, ht,
				fontSzProp);
		southEastPanel.add(zoomLabel);
		m_ZoomViewPane = new JPanel() {
			public void paint(Graphics g) {
				repaintZoomViewPane(g);
			}
		};
		JPanel zoomPanel = RGPTUIUtil.createPanel(m_ZoomViewPane, true,
				RGPTUIUtil.LOWERED_BORDER);
		int zoomlHt = RGPTParams.getIntVal("ImageMakerZoomPanelHeight");
		RGPTUIUtil.setCompSize(m_ZoomViewPane, eastPanelWt, zoomlHt, 5);
		southEastPanel.add(zoomPanel);
		lblTxt = lu.getText("ImageMakerSelMode");
		JPanel selLabel = RGPTUIUtil.createLabelPanel(lblTxt, wt, ht,
				fontSzProp);
		southEastPanel.add(selLabel);
		JPanel labelPanel = new JPanel(new BorderLayout());
		RGPTUIUtil.setCompSize(labelPanel, eastPanelWt, m_SouthPanelHeight, 1);
		m_SelectionModeLabel = RGPTUIUtil.createLabel("", eastPanelWt,
				m_SouthPanelHeight, fontSzProp);
		RGPTUIUtil.setBorder(m_SelectionModeLabel, RGPTUIUtil.LOWERED_BORDER);
		labelPanel.add(m_SelectionModeLabel, BorderLayout.CENTER);
		southEastPanel.add(labelPanel);
		eastPanel.add(southEastPanel, BorderLayout.SOUTH);
		return eastPanel;
	}

	private JPanel createWestPanel() {
		LocalizationUtil lu = new LocalizationUtil();
		String icon = "", name = "", tip = "";
		String[] names;
		int actionId = -1;
		ImageActions action = null;
		JPanel cutoutImgLabel, pixelSetterLabel;

		JPanel mainWestPanel = new JPanel(new BorderLayout());
		JPanel westPanel = new JPanel();
		Dimension westPanelSz = new Dimension(m_WestPanelWidth,
				m_ContentPaneSize.height);
		westPanel.setPreferredSize(westPanelSz);
		westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
		// westPanel.setBorder(new LineBorder(Color.BLACK));

		// File Action Panel
		JPanel fileActionPanel = this.createFileActionPanel();
		RGPTLogger.logToConsole("fileActionPanel Pref Size: "
				+ fileActionPanel.getPreferredSize());
		westPanel.add(fileActionPanel);

		// Cutout Shape Button
		JPanel cutoutShapePanel = createCutoutPanel(false);
		westPanel.add(cutoutShapePanel);

		// Image Selection
		createImagingPanel(westPanel, true);

		// Cutout Image Button
		int emptyPanelHt = RGPTParams.getIntVal("EmptyPanelHeight");
		JPanel emptyPanel = RGPTUIUtil.createEmptyPanel(m_WestPanelWidth,
				emptyPanelHt);
		westPanel.add(emptyPanel);
		JPanel cutoutImagePanel = createCutoutPanel(true);
		westPanel.add(cutoutImagePanel);

		// Pixel Setting Selection
		createImagingPanel(westPanel, false);

		// Adding Popup Menu for Patching up Pixels in Images
		JMenuItem menuItem = null;
		Dimension menuItemSz = RGPTUtil.getCompSize("ImageMakerMainMenuSize");
		JPopupMenu patchPixelMenu = new JPopupMenu("PatchPixelMenu");
		action = ImageActions.PATCH_PIXEL_SEL_MENU;
		names = lu.getText("PatchPixelItem").split("::");
		icon = "rgpticons/PatchPixel.gif";
		tip = lu.getText("ToolTip_PatchPixel");
		menuItem = RGPTUIUtil.createMenuItem(icon, names[0], tip, menuItemSz,
				this, action.toString());
		m_ActionSelection.put(action.toString(), names[1]);
		m_PopupMenuImageAction.put(action.toString(),
				ImageActions.PATCH_SEL.toString());
		patchPixelMenu.add(menuItem);
		action = ImageActions.PATCH_PIXEL_AREA_SEL_MENU;
		names = lu.getText("PatchPixelAreaItem").split("::");
		icon = "rgpticons/PatchPixelArea.gif";
		tip = lu.getText("ToolTip_PatchPixelArea");
		menuItem = RGPTUIUtil.createMenuItem(icon, names[0], tip, menuItemSz,
				this, action.toString());
		m_ActionSelection.put(action.toString(), names[1]);
		m_PopupMenuImageAction.put(action.toString(),
				ImageActions.PATCH_SEL.toString());
		patchPixelMenu.add(menuItem);
		m_ActionPopupMenu
				.put(ImageActions.PATCH_SEL.toString(), patchPixelMenu);

		// Adding Popup Menu Changing the Pixels in Images
		JPopupMenu changePixelMenu = new JPopupMenu("ChangePixelMenu");
		action = ImageActions.MAKE_PIXEL_TRANSPERENT_MENU;
		names = lu.getText("TransperentPixelItem").split("::");
		icon = "rgpticons/MakeTransperent.gif";
		tip = lu.getText("ToolTip_TransperentPixel");
		menuItem = RGPTUIUtil.createMenuItem(icon, names[0], tip, menuItemSz,
				this, action.toString());
		m_ActionSelection.put(action.toString(), names[1]);
		m_PopupMenuImageAction.put(action.toString(),
				ImageActions.CHANGE_PIXEL_SEL.toString());
		changePixelMenu.add(menuItem);

		action = ImageActions.CHANGE_PIXEL_COLOR_MENU;
		names = lu.getText("ChangeColorPixelItem").split("::");
		icon = "rgpticons/ColorChange.gif";
		tip = lu.getText("ToolTip_ChangeColorPixel");
		menuItem = RGPTUIUtil.createMenuItem(icon, names[0], tip, menuItemSz,
				this, action.toString());
		m_ActionSelection.put(action.toString(), names[1]);
		m_PopupMenuImageAction.put(action.toString(),
				ImageActions.CHANGE_PIXEL_SEL.toString());
		changePixelMenu.add(menuItem);
		m_ActionPopupMenu.put(ImageActions.CHANGE_PIXEL_SEL.toString(),
				changePixelMenu);

		// Image Filter Button
		emptyPanel = RGPTUIUtil
				.createEmptyPanel(m_WestPanelWidth, emptyPanelHt);
		westPanel.add(emptyPanel);
		int panelHt = RGPTParams.getIntVal("ImageMakerWestPanelHeight");
		JPanel imgFilterPanel = new JPanel(new BorderLayout());
		RGPTUIUtil.setCompSize(imgFilterPanel, m_WestPanelWidth, panelHt, 5);
		Dimension size = new Dimension(120, 70);
		icon = "rgpticons/ImageFilters.gif";
		tip = lu.getText("ToolTip_ImageFilters");
		names = lu.getText("ImageFiltersBtn").split("::");
		action = ImageActions.IMAGE_FILTERS;
		JButton imgFilterBtn = RGPTUIUtil.createButton(icon, names[0], null,
				tip, size, this, action.toString(), true);
		m_ActionSelection.put(action.toString(), names[1]);
		m_ActionButtonMap.put(action.toString(), imgFilterBtn);
		m_ActionMessage.put(action.toString(),
				RGPTUtil.replaceTextWithDelim(tip, "<br>", " "));
		imgFilterPanel.add(imgFilterBtn, BorderLayout.CENTER);
		// imgFilterPanel.setBorder(new LineBorder(Color.BLACK));
		westPanel.add(imgFilterPanel);

		emptyPanel = RGPTUIUtil.createEmptyPanel(m_WestPanelWidth, 60);
		westPanel.add(emptyPanel);
		JPanel logoPanel = RGPTUIUtil.createImagePanel("rgpticons/XONLogo.png",
				140, 30);
		westPanel.add(logoPanel);

		mainWestPanel.add(createZoomPanel(), BorderLayout.SOUTH);
		mainWestPanel.add(westPanel, BorderLayout.CENTER);
		return mainWestPanel;
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
		int actionId;
		Dimension size = new Dimension(20, 20);
		icon = "rgpticons/DecreaseZoom.png";
		tip = lu.getText("ToolTip_DecreaseZoom");
		ImageActions action = ImageActions.DECREASE_ZOOM;
		navButton = RGPTUIUtil.createImageButton(icon, tip, size, this,
				action.toString(), false);
		navButton.setBackground(XONImageMaker.m_PanelColor);
		navButton.setForeground(XONImageMaker.m_PanelColor);
		navButton.setBorder(new LineBorder(XONImageMaker.m_PanelColor));
		navButton.setMnemonic(KeyEvent.VK_PAGE_UP);
		m_ActionButtonMap.put(action.toString(), navButton);
		zoomPanel.add(navButton);

		icon = "rgpticons/IncreaseZoom.png";
		tip = lu.getText("ToolTip_IncreaseZoom");
		action = ImageActions.INCREASE_ZOOM;
		navButton = RGPTUIUtil.createImageButton(icon, tip, size, this,
				action.toString(), false);
		// navButton.setEnabled(true);
		navButton.setBackground(XONImageMaker.m_PanelColor);
		navButton.setForeground(XONImageMaker.m_PanelColor);
		navButton.setBorder(new LineBorder(XONImageMaker.m_PanelColor));
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

	private void createImagingPanel(JPanel westPanel, boolean isImageSelPanel) {
		JButton imagingBtn = null;
		LocalizationUtil lu = new LocalizationUtil();
		String icon = "", name = "", tip = "", wfIcon = "", wfName = "";
		String[] names;

		int emptyPanelHt = RGPTParams.getIntVal("EmptyPanelHeight");
		JPanel emptyPanel = RGPTUIUtil.createEmptyPanel(m_WestPanelWidth,
				emptyPanelHt);
		westPanel.add(emptyPanel);

		// JPanel imagingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,
		// 15, 1));
		JPanel imagingPanel = new JPanel(new BorderLayout());
		int panelHt = RGPTParams.getIntVal("ImageMakerImagingPanelHeight");
		RGPTUIUtil.setCompSize(imagingPanel, m_WestPanelWidth, panelHt, 5);

		int wt = m_WestPanelWidth, ht = RGPTParams.getIntVal("LabelHeight");
		String lblTxt = lu.getText("ImageSelectionText"), fontSzProp = "LabelFontSize";
		if (!isImageSelPanel)
			lblTxt = lu.getText("ImagePixelSettingsText");
		JPanel imgLabel = RGPTUIUtil.createLabelPanel(lblTxt, wt, ht,
				fontSzProp);
		imagingPanel.add(imgLabel, BorderLayout.NORTH);

		JPanel buttonPanel = new JPanel(new BasicGridLayout(1, 2, 5, 1));
		buttonPanel.setMinimumSize(new Dimension(m_WestPanelWidth - 20, 120));

		icon = "rgpticons/SelectPixel.gif";
		tip = lu.getText("ToolTip_ImagePixel");
		names = lu.getText("ImagePixelBtn").split("::");
		ImageActions action = ImageActions.IMAGE_PIXEL_SEL;
		if (!isImageSelPanel) {
			icon = "rgpticons/Patch.gif";
			tip = lu.getText("ToolTip_PatchPixel");
			action = ImageActions.PATCH_SEL;
			names = lu.getText("PatchPixelBtn").split("::");
			wfIcon = "rgpticons/PatchPixelsWF.gif";
			wfName = lu.getText("PatchPixelsWFBtn");
			createWFSteps(wfIcon, wfName, tip, action, "PatchPixelsWF");
		}
		imagingBtn = createActionButton(buttonPanel, icon, names, tip, action,
				false);
		// imagingBtn.setEnabled(true);

		icon = "rgpticons/SelectCropArea.gif";
		tip = lu.getText("ToolTip_CropImage");
		action = ImageActions.IMAGE_CROP_SEL;
		names = lu.getText("CropImageBtn").split("::");
		if (!isImageSelPanel) {
			icon = "rgpticons/ChangeImagePixels.gif";
			tip = lu.getText("ToolTip_ChangePixel");
			action = ImageActions.CHANGE_PIXEL_SEL;
			names = lu.getText("ChangePixelBtn").split("::");
			wfIcon = "rgpticons/ChangeImagePixelsWF.gif";
			wfName = lu.getText("ChangePixelsWFBtn");
			createWFSteps(wfIcon, wfName, tip, action, "ChangePixelsWF");
		}
		imagingBtn = createActionButton(buttonPanel, icon, names, tip, action,
				false);
		// imagingBtn.setEnabled(true);
		imagingPanel.add(buttonPanel, BorderLayout.CENTER);
		RGPTUIUtil.setBorder(imagingPanel);
		westPanel.add(imagingPanel);
	}

	private JPanel createCutoutPanel(boolean isImageCutout) {
		LocalizationUtil lu = new LocalizationUtil();
		String icon = "", name = "", tip = "", wfIcon = "", wfPropName = "";
		int panelHt = RGPTParams.getIntVal("ImageMakerWestPanelHeight");
		JPanel cutoutPanel = new JPanel(new BorderLayout());
		RGPTUIUtil.setCompSize(cutoutPanel, m_WestPanelWidth, panelHt, 5);
		Dimension size = new Dimension(120, 70);
		icon = "rgpticons/CutoutShapes.gif";
		tip = lu.getText("ToolTip_CutoutShape");
		String[] names = lu.getText("CutoutShapeBtn").split("::");
		ImageActions action = ImageActions.CUTOUT_SHAPES;
		wfIcon = "rgpticons/CutoutShapesWF.gif";
		wfPropName = "CutoutShapeWFSteps";

		if (isImageCutout) {
			icon = "rgpticons/CutoutImage.gif";
			tip = lu.getText("ToolTip_CutoutImage");
			names = lu.getText("CutoutImageBtn").split("::");
			action = ImageActions.CUTOUT_IMAGE;
			wfIcon = "rgpticons/CutoutImageWF.gif";
			wfPropName = "CutoutImageWFSteps";
		}
		JButton cutoutBtn = RGPTUIUtil.createButton(icon, names[0], null, tip,
				size, this, action.toString(), true);
		createWFSteps(wfIcon, names[0], tip, action, wfPropName);
		m_ActionSelection.put(action.toString(), names[1]);
		m_ActionButtonMap.put(action.toString(), cutoutBtn);
		m_ActionMessage.put(action.toString(),
				RGPTUtil.replaceTextWithDelim(tip, "<br>", " "));
		cutoutPanel.add(cutoutBtn, BorderLayout.CENTER);

		// Adding Popup Menu to Cutout Shape Button
		JPopupMenu cutoutMenu = addMenuItem(action.toString(), isImageCutout);
		m_ActionPopupMenu.put(action.toString(), cutoutMenu);
		return cutoutPanel;
	}

	private JPopupMenu addMenuItem(String mainImageAction, boolean isImageCutout) {
		LocalizationUtil lu = new LocalizationUtil();
		String icon = "", name = "", tip = "";
		String[] names;
		int actionId = -1;
		ImageActions action = null;
		JMenuItem menuItem = null;
		Dimension menuItemSz = RGPTUtil.getCompSize("ImageMakerMainMenuSize");
		String menuName = "Shape Path";
		if (isImageCutout)
			menuName = "Image Path";
		JPopupMenu cutoutShapeMenu = new JPopupMenu(menuName);
		icon = "rgpticons/PolyLine.gif";
		tip = lu.getText("ToolTip_LinePath");
		names = lu.getText("LinePathItem").split("::");
		action = ImageActions.SHAPE_LINE_PATH_MENU;
		if (isImageCutout)
			action = ImageActions.IMAGE_LINE_PATH_MENU;
		menuItem = RGPTUIUtil.createMenuItem(icon, names[0], tip, menuItemSz,
				this, action.toString());
		m_ActionSelection.put(action.toString(), names[1]);
		m_PopupMenuImageAction.put(action.toString(), mainImageAction);
		cutoutShapeMenu.add(menuItem);
		icon = "rgpticons/QuadCurve.gif";
		tip = lu.getText("ToolTip_QuadPath");
		names = lu.getText("QuadPathItem").split("::");
		action = ImageActions.SHAPE_QUAD_PATH_MENU;
		if (isImageCutout)
			action = ImageActions.IMAGE_QUAD_PATH_MENU;
		menuItem = RGPTUIUtil.createMenuItem(icon, names[0], tip, menuItemSz,
				this, action.toString());
		m_ActionSelection.put(action.toString(), names[1]);
		m_PopupMenuImageAction.put(action.toString(), mainImageAction);
		cutoutShapeMenu.add(menuItem);
		icon = "rgpticons/CubicCurve.gif";
		tip = lu.getText("ToolTip_CubicPath");
		names = lu.getText("CubicPathItem").split("::");
		action = ImageActions.SHAPE_CUBIC_PATH_MENU;
		if (isImageCutout)
			action = ImageActions.IMAGE_CUBIC_PATH_MENU;
		menuItem = RGPTUIUtil.createMenuItem(icon, names[0], tip, menuItemSz,
				this, action.toString());
		m_ActionSelection.put(action.toString(), names[1]);
		m_PopupMenuImageAction.put(action.toString(), mainImageAction);
		cutoutShapeMenu.add(menuItem);
		return cutoutShapeMenu;
	}

	private void createWFSteps(String icon, String name, String tip,
			ImageActions action, String wfProp) {
		JButton button = null;
		Vector<JButton> actionButtons = null;
		int panelWt = m_ContentPaneSize.width - m_WestPanelWidth;
		Dimension size = new Dimension(100, 30);
		JPanel wfMainPanel = new JPanel();
		String resetAction = WFActions.RESET_WF.toString();
		button = RGPTUIUtil.createButton(icon, name, "", tip, size, this,
				resetAction, true);
		button.setEnabled(true);
		actionButtons = RGPTUIUtil.createWFSteps(wfMainPanel, button, panelWt,
				wfProp, this, m_ActionSelection, 2);
		RGPTUIUtil.setWFActionMap(actionButtons, action.toString(),
				m_WFImageAction);
		m_ActionWFPanelMap.put(action.toString(), wfMainPanel);
		m_WFActionSteps.put(action.toString(), actionButtons);
		// Creating a Popup Menu for Save Image WF Step which is part of Cutout
		// Image WF Process
		JPopupMenu menu = null;
		Dimension menuItemSz = RGPTUtil.getCompSize("WFMenuSize");
		switch (action) {
		case CUTOUT_IMAGE:
			menu = RGPTUIUtil.createPopupMenu(menuItemSz,
					"SAVE_IMAGE_MENU_ITEM", this);
			m_ActionPopupMenu.put(WFActions.SAVE_IMAGE_CUTOUT.toString(), menu);
			break;
		case PATCH_SEL:
			menu = RGPTUIUtil.createPopupMenu(menuItemSz,
					"SAVE_PATCH_IMAGE_MENU_ITEM", this);
			m_ActionPopupMenu.put(WFActions.SAVE_PATCH_IMAGE.toString(), menu);
			break;
		case CHANGE_PIXEL_SEL:
			menu = RGPTUIUtil.createPopupMenu(menuItemSz,
					"SELECT_CHANGE_PIXEL_STEP_MENU", this);
			m_ActionPopupMenu.put(
					WFActions.SELECT_CHANGE_PIXEL_STEP.toString(), menu);
			menu = RGPTUIUtil.createPopupMenu(menuItemSz,
					"MODIFY_PIXEL_STEP_MENU", this);
			m_ActionPopupMenu.put(WFActions.MODIFY_PIXEL_STEP.toString(), menu);
			menu = RGPTUIUtil.createPopupMenu(menuItemSz,
					"SAVE_CHANGE_IMAGE_MENU_ITEM", this);
			m_ActionPopupMenu.put(WFActions.SAVE_CHANGE_IMAGE.toString(), menu);
			break;
		}
	}

	private JPanel createFileActionPanel() {
		String icon = "", name = "", tip = "";
		String[] names;
		ImageActions action = null;
		LocalizationUtil lu = new LocalizationUtil();
		JPanel fileActionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,
				15, 2));
		JPanel buttonPanel = new JPanel(new BasicGridLayout(1, 2, 5, 5));
		int filePanelHt = RGPTParams.getIntVal("IMAppsFileActionPanelHeight");
		buttonPanel.setMinimumSize(new Dimension(m_WestPanelWidth - 20,
				filePanelHt));
		m_FileOpenClosePanel = new JPanel(new BorderLayout());

		// File Open Button
		JButton fopen, fclose, fsave, fupload;
		icon = "rgpticons/FileOpen.gif";
		tip = lu.getText("ToolTip_ImageFileOpen");
		action = ImageActions.IMAGE_FILE_OPEN;
		names = lu.getText("ImageFileOpen").split("::");
		fopen = createActionButton(null, icon, names, tip, action, true);
		fopen.setEnabled(true);
		m_FileOpenClosePanel.add(fopen, BorderLayout.CENTER);
		buttonPanel.add(m_FileOpenClosePanel);
		// File Close Button
		icon = "rgpticons/FileClose.gif";
		tip = lu.getText("ToolTip_ImageFileClose");
		action = ImageActions.IMAGE_FILE_CLOSE;
		names = lu.getText("FileClose").split("::");
		fclose = createActionButton(null, icon, names, tip, action, true);
		// File Save Button
		icon = "rgpticons/FileSave.gif";
		tip = lu.getText("ToolTip_ImageFileSave");
		action = ImageActions.IMAGE_FILE_SAVE;
		names = lu.getText("FileSave").split("::");
		// fsave = createActionButton(buttonPanel, icon, names, tip, action,
		// true);
		// File Compress Button
		icon = "rgpticons/FileUpload.gif";
		tip = lu.getText("ToolTip_ImageUpload");
		action = ImageActions.IMAGE_UPLOAD;
		names = lu.getText("ImageUpload").split("::");
		fupload = createActionButton(buttonPanel, icon, names, tip, action,
				true);

		RGPTUIUtil.setCompSize(fileActionPanel, m_WestPanelWidth, filePanelHt,
				-1);
		fileActionPanel.add(buttonPanel);
		return fileActionPanel;
	}

	private JButton createActionButton(JPanel buttonPanel, String icon,
			String[] names, String tip, ImageActions action,
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

	// START FUNCTION FOR INTERFACE METHODS

	public void setComponentData(String action, String compName, JComponent comp) {
	}

	public void setZoom(int zoomLevel) {
		m_XONImageViewerPanel.setZoom(zoomLevel);
	}

	public void updateZoom(int zoomLevel) {
		m_ZoomInfoField.setText(String.valueOf(zoomLevel));
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		HashMap dialogActionComp = (HashMap) m_XONImageViewerPanel.m_DialogActionComponent;
		Object actionObj = RGPTUtil.containsValue(dialogActionComp,
				e.getSource(), true);
		if (actionObj == null)
			return;
		performRGPTDialogAction((String) actionObj);
	}

	public void keyReleased(KeyEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
		HashMap dialogActionComp = (HashMap) m_XONImageViewerPanel.m_DialogActionComponent;
		Object actionObj = RGPTUtil.containsValue(dialogActionComp,
				e.getSource(), true);
		if (actionObj == null)
			return;
		RGPTLogger.logToFile("Calling Dialog Action: " + actionObj);
		performRGPTDialogAction((String) actionObj);
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
		this.resetWorkflowPanel((String) actionObj, SHOW_ADV_INFO);
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}

	private void resetWorkflowPanel(int actionId, int showWFItem) {
		resetWorkflowPanel((new Integer(actionId)).toString(), showWFItem);
	}

	private void resetWorkflowPanel(String action, int showWFItem) {
		if (m_ShowWorkflowItem == SHOW_WORKFLOW_STEPS)
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

	public void actionPerformed(ActionEvent evnt) {
		RGPTLogger.logToFile("Action Cmnd: " + evnt.getActionCommand());
		String[] cmnd = evnt.getActionCommand().split("=");
		if (m_ActionSelection.get(cmnd[1]) != null)
			m_SelectionModeLabel.setText(m_ActionSelection.get(cmnd[1]));
		this.setCursor(cmnd[1]);
		if (RGPTUtil.isWFActionPerformed(cmnd[1]))
			performRGPTWFAction(cmnd[1]);
		else if (RGPTUtil.isDialogActionPerformed(cmnd[1]))
			performRGPTDialogAction(cmnd[1]);
		else
			performRGPTAction(cmnd[1]);
		this.resetCursor();
	}

	public void setCursor(String action) {
		String[] busyCursorIds = RGPTActionListener.BUSY_CURSOR_ACTIONS;
		Arrays.sort(busyCursorIds);
		int index = Arrays.binarySearch(busyCursorIds, action);
		if (index < 0) {
			// RGPTLogger.logToFile("Busy Cursor not set for: "+action);
			return;
		}
		// RGPTLogger.logToFile("Busy Cursor set for: "+action);
		this.setCursor(CursorController.m_BusyCursor);
	}

	public void resetCursor() {
		this.setCursor(CursorController.m_DefaultCursor);
	}

	public void performImageFilterAction(String actionStr) {
		ImageFilterActions action = ImageFilterActions.valueOf(actionStr);
		RGPTLogger.logToFile("Image Filter Action is: " + action.toString());
		switch (action) {
		}
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
			m_XONImageViewerPanel.processSaveDialog(action);
			break;
		case SET_COLOR_RANGE_FIELD:
		case SET_COLOR_RANGE_OK:
		case SET_COLOR_RANGE_CANCEL:
			m_XONImageViewerPanel.processDialogAction(action);
			break;
		case SET_BRUSH_SIZE_FIELD:
		case SET_BRUSH_SIZE_OK:
		case SET_BRUSH_SIZE_CANCEL:
			m_XONImageViewerPanel.processDialogAction(action);
			break;
		}
	}

	public void performRGPTWFAction(String actionStr) {
		JButton button = null;
		JPopupMenu menu = null;
		// Local Reference for temp use
		XONImagePanel xonImgPanel = m_XONImageViewerPanel;
		WFActions action = WFActions.valueOf(actionStr);
		// Highlighting the WF Step
		String imageAction = m_WFImageAction.get(actionStr);
		RGPTLogger.logToFile("WF Action is: " + action.toString()
				+ " Image Action: " + imageAction);
		if (action == WFActions.RESET_WF) {
			if (xonImgPanel.m_ProcessSelectionId != null)
				imageAction = xonImgPanel.m_ProcessSelectionId;
			else if (xonImgPanel.m_PathSelectionId != null)
				imageAction = xonImgPanel.m_PathSelectionId;
		}
		if (imageAction != null)
			highWFButton(ImageActions.valueOf(imageAction), action);
		switch (action) {
		// Workflow Actions
		case DRAW_SHAPE_PATH_STEP:
		case DRAW_GRAPHIC_PATH_STEP:
			xonImgPanel.drawGPath();
			break;
		case ADJUST_SHAPE_PATH_STEP:
		case ADJUST_GRAPHIC_PATH_STEP:
			xonImgPanel.adjustGPath(action.toString());
			break;
		case PREVIEW_SHAPE:
		case PREVIEW_IMAGE_CUTOUT:
			xonImgPanel.previewPath(action.toString());
			break;
		case SAVE_SHAPE:
			xonImgPanel.savePath(action.toString());
			break;
		case SAVE_IMAGE_CUTOUT:
			xonImgPanel.saveImage(action);
			showMenuItem(ImageActions.valueOf(imageAction), action);
			break;
		case SAVE_IMAGE_TO_FILE:
		case SAVE_IMAGE_IN_MEM:
		case SAVE_CHANGE_IMAGE_TO_FILE:
		case SAVE_CHANGE_IMAGE_IN_MEM:
			xonImgPanel.saveImage(action);
			break;
		case SAVE_PATCH_IMAGE:
			showMenuItem(ImageActions.valueOf(imageAction), action);
		case SELECT_IMAGE_LOCATION_STEP:
			xonImgPanel.selectImageLoc(action.toString());
			break;
		case SELECT_CHANGE_PIXEL_STEP:
		case MODIFY_PIXEL_STEP:
			xonImgPanel.setActionPerformed(action.toString(), false);
			showMenuItem(ImageActions.valueOf(imageAction), action);
			break;
		case SEL_IMAGE_PIXEL_COLOR:
		case SEL_CUTOUT_SHAPE:
		case ADD_IMAGE_PIXEL_COLOR:
		case REM_IMAGE_PIXEL_COLOR:
		case PREVIEW_CHANGE_IMAGE:
			xonImgPanel.setActionPerformed(action.toString(), true);
			break;
		case SET_COLOR_RANGE:
		case SET_BRUSH_SIZE:
			xonImgPanel.activateDialogBox(action.toString());
			break;
		case SAVE_CHANGE_IMAGE:
			xonImgPanel.setActionPerformed(action.toString(), false);
			showMenuItem(ImageActions.valueOf(imageAction), action);
			break;
		case RESET_WF:
			boolean isApproved = RGPTUIUtil.getUserApproval(this,
					"RESET_WF_INFO");
			if (isApproved)
				resetImageMaker();
			break;
		}
	}

	// This method is called by MneomonicKeyListener when user presses ctrl O,
	// etc
	public void performRGPTAction(String actionStr) {
		String defWFAction = "";
		Dimension size = null;
		JButton button = null;
		JPopupMenu menu = null;
		ImageActions action = ImageActions.valueOf(actionStr);
		// Resetting the Action Performed on Image Viewer Panel as soon as the
		// new
		// Action is Performed.
		Vector<String> menuActionIds = RGPTUtil.createVector(MENU_ACTION_IDS);
		if (!menuActionIds.contains(actionStr)) {
			setButtonActivations(actionStr);
			boolean reset = m_XONImageViewerPanel.resetActionPerformed(action
					.toString());
			// Resetting the Workflow Panel
			if (m_ActionButtonMap.get(action.toString()) != null && reset)
				this.resetWorkflowPanel();
		}
		switch (action) {
		// File Action Buttons
		case IMAGE_FILE_OPEN:
			this.openImageFile(action);
			break;
		case IMAGE_FILE_CLOSE:
			this.closeImageFile();
			break;
		case IMAGE_FILE_SAVE:
			break;
		case IMAGE_COMPRESS:
			break;
		// Increase or Decrease Zoom Action
		case DECREASE_ZOOM:
			m_XONImageViewerPanel.zoomOut();
			break;
		case INCREASE_ZOOM:
			m_XONImageViewerPanel.zoomIn();
			break;
		// Menu Item Actions
		case CUTOUT_SHAPES:
		case CUTOUT_IMAGE:
		case PATCH_SEL:
		case CHANGE_PIXEL_SEL:
			// Resetting the Action Performed on Image Viewer Panel as soon as
			// the new
			// Action is Performed.
			button = m_ActionButtonMap.get(action.toString());
			size = button.getPreferredSize();
			menu = m_ActionPopupMenu.get(action.toString());
			m_XONImageViewerPanel.setWorkFlowProcessId(action.toString());
			resetWorkflowPanel(action.toString(), SHOW_WORKFLOW_STEPS);
			menu.show(button, size.width, 0);
			break;
		case SHAPE_LINE_PATH_MENU:
		case SHAPE_QUAD_PATH_MENU:
		case SHAPE_CUBIC_PATH_MENU:
			m_XONImageViewerPanel.cutoutPath(action.toString());
			break;
		case IMAGE_LINE_PATH_MENU:
		case IMAGE_QUAD_PATH_MENU:
		case IMAGE_CUBIC_PATH_MENU:
			m_XONImageViewerPanel.cutoutPath(action.toString());
			break;
		case MAKE_PIXEL_TRANSPERENT_MENU:
			m_XONImageViewerPanel.setProcessSel(action.toString(), true);
			break;
		}
		// Auto Activating the First Step in the WF
		if (menuActionIds.contains(actionStr)) {
			String imageAction = m_PopupMenuImageAction.get(actionStr);
			if (imageAction != null) {
				defWFAction = highWFButton(ImageActions.valueOf(imageAction),
						null);
				performRGPTWFAction(defWFAction);
			}
		}
		// if(action == Actions.SHAPE_TYPE_COMBO)
		// RGPTLogger.logToFile("If Loop Execution Action: "+action);

	}

	// This method returns the mneomonic key for the action id else null
	public String getMnemonicKey(String action) {
		return m_ActionMnemonicKeysMap.get(action);
	}

	// END FUNCTION FOR INTERFACE METHODS

	// START FUNCTION for Action Methods Implementation

	// This method is used to Show Menu Item attached to buttons
	private void showMenuItem(ImageActions imgAction, WFActions wfAction) {
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

	private String highWFButton(ImageActions imgAction, WFActions wfAction) {
		JButton button = null;
		String wfActionId = "";
		RGPTLogger.logToFile("Image Action: " + imgAction + " WFAction: "
				+ wfAction);
		Vector<JButton> actionButtons = m_WFActionSteps.get(imgAction
				.toString());
		if (actionButtons == null)
			return wfActionId;
		for (int i = 0; i < actionButtons.size(); i++) {
			button = actionButtons.elementAt(i);
			button.setBackground(RGPTUIManager.BG_COLOR);
			String[] cmnd = button.getActionCommand().split("=");
			if (wfAction == null && i == 0) {
				button.setBackground(Color.YELLOW);
				wfActionId = WFActions.valueOf(cmnd[1]).toString();
			} else if (wfAction != null) {
				if (cmnd[1].equals(wfAction.toString())) {
					wfActionId = wfAction.toString();
					button.setBackground(Color.YELLOW);
				}
			}
			button.revalidate();
		}
		RGPTLogger.logToFile("WF Step Highlighted: " + wfActionId);
		return wfActionId;
	}

	private void openImageFile(ImageActions action) {
		// Use FileChooser to get the Image File and Image Holder Object
		setImageHolder(action, RGPTUIUtil.getImageFile(this));
	}

	XONAppLaucherInterface m_XONAppLaucher;

	public void performImageMakeover(XONAppLaucherInterface xonApp,
			ImageHolder imgHldr) {
		RGPTLogger.logToFile("Makeover Image for: " + imgHldr.m_FileName);
		m_XONAppLaucher = xonApp;
		setImageHolder(ImageActions.IMAGE_FILE_OPEN, imgHldr);
		m_ContentPane.repaint();
		m_ContentPane.revalidate();
	}

	private void setImageHolder(ImageActions action, ImageHolder imgHldr) {
		LocalizationUtil lu = new LocalizationUtil();
		m_OrigImageHolder = imgHldr;
		BufferedImage infoImg = null;
		boolean showError = false;
		if (m_OrigImageHolder != null) {
			try {
				int thumbImgWt = RGPTParams.getIntVal("ThumbviewImageWidth");
				int thumbImgHt = RGPTParams.getIntVal("ThumbviewImageHeight");
				// Compress the Image File and save it in the Image Holder
				m_OrigImageHolder.setImageData(thumbImgWt, thumbImgHt);
				if (m_OrigImageHolder.m_ImageHasAlpha) {
					infoImg = ImageUtils.createColorImage(
							m_OrigImageHolder.m_BackgroundColor,
							"InfoMesgImageSize");
					RGPTUIUtil.showInfoMesg(this, infoImg,
							"IMAGE_FILE_OPEN_INFO");
				}
			} catch (Exception ex) {
				showError = true;
			}
		} else
			showError = true;
		if (showError) {
			String[] errorInfo = lu.getText("ERROR_OPEN_FILE").split("::");
			RGPTUIUtil.showError(this, errorInfo[1], errorInfo[0]);
			return;
		}
		// Open the Image File using the XONImagePanel
		m_XONImageViewerPanel.setXONImage(action.toString(), m_OrigImageHolder);
		String title = m_Title + ": " + m_OrigImageHolder.m_FileName;
		if (m_XONAppLaucher != null)
			m_XONAppLaucher.setAppTitle(title);
		else
			this.setTitle(title);
		resetFilePanel(m_FileOpenClosePanel, action.toString(),
				ImageActions.IMAGE_FILE_CLOSE.toString());
		resetXONImagePane(SHOW_IMAGE_PANEL);
	}

	private void resetFilePanel(JPanel filePanel, String resetAction,
			String newAction) {
		JButton resetButton = m_ActionButtonMap.get(resetAction);
		JButton actButton = m_ActionButtonMap.get(newAction);
		RGPTUIUtil.resetDisplayComponent(filePanel, resetButton, actButton);
		filePanel.repaint();
	}

	public void resetImageMaker() {
		m_XONImageViewerPanel.resetActionPerformed(null);
		resetWorkflowPanel();
		m_XONImageViewerPanel.repaint();
		setButtonsEnabled(IMAGE_MAKER_ACTION_ACTIVATION_IDS, true);
	}

	public void closeImageFile() {
		RGPTLogger.logToFile("Closing Image File");
		m_OrigImageHolder = null;
		m_ZoomInfoField.setText("0");
		m_SelectionModeLabel.setText("");
		m_XONImageViewerPanel.resetXONImage();
		if (m_XONAppLaucher != null) {
			m_XONAppLaucher.resetApp();
			m_XONAppLaucher = null;
		} else
			this.setTitle(m_Title);
		resetFilePanel(m_FileOpenClosePanel,
				ImageActions.IMAGE_FILE_CLOSE.toString(),
				ImageActions.IMAGE_FILE_OPEN.toString());
		resetXONImagePane(SHOW_XON_ICON);
		resetWorkflowPanel();
	}

	private void resetXONImagePane(int xonImagePaneItem) {
		m_ShowXONImagePane = xonImagePaneItem;
		setButtonActivation();
		resetPanelItems(m_XONImageContentPane, xonImagePaneItem,
				m_XONImageContentItem);
		m_XONImageContentPane.repaint();
		m_ThumbnailPane.repaint();
		m_ZoomViewPane.repaint();
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

	public void setButtonActivation() {
		if (m_ShowXONImagePane == SHOW_XON_ICON) {
			setButtonsEnabled(IMAGE_MAKER_ACTION_ACTIVATION_IDS, false);
			return;
		}
		setButtonsEnabled(IMAGE_MAKER_ACTION_ACTIVATION_IDS, true);
	}

	public void setButtonsEnabled(String[] actionIds, boolean enabled) {
		for (int i = 0; i < actionIds.length; i++) {
			JButton button = m_ActionButtonMap.get(actionIds[i]);
			if (button != null)
				button.setEnabled(enabled);
			else
				RGPTLogger.logToFile("Action Button: " + actionIds[i]
						+ " not Registered");
		}
	}

	public void setButtonActivations(String action) {
		Vector<String> exceptionIds = RGPTUtil
				.createVector(FILE_OPEN_ACTIVATED_IDS);
		String[] imageActions = RGPTUtil.enumToStringArray(ImageActions
				.values());
		for (int i = 0; i < imageActions.length; i++) {
			JButton button = m_ActionButtonMap.get(imageActions[i]);
			if (button == null) {
				RGPTLogger.logToFile("Action Button: " + imageActions[i]
						+ " does not exists");
				continue;
			}
			if (action.equals(imageActions[i])
					|| exceptionIds.contains(imageActions[i]))
				button.setEnabled(true);
			else
				button.setEnabled(false);
		}
	}

	// END FUNCTION for Action Methods Implementation

	// START FUNCTION FOR REPAINT METHODS

	public void repaintImagePreviewPane(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setBackground(XONImageMaker.m_PanelColor);
		Rectangle visibleRect = m_ThumbnailPane.getVisibleRect();
		Dimension panelSize = new Dimension((int) visibleRect.getWidth(),
				(int) visibleRect.getHeight());
		g.clearRect(0, 0, panelSize.width, panelSize.height);
		// RGPTLogger.logToConsole("Panel Size: "+panelSize);
		int panelWt = RGPTParams.getIntVal("ImageMakerEastPanelWidth");
		int thumbImgWt = RGPTParams.getIntVal("ThumbviewImageWidth");
		int thumbImgHt = RGPTParams.getIntVal("ThumbviewImageHeight");
		int vGap = RGPTParams.getIntVal("ThumbviewVerticalGap");
		int arc = RGPTParams.getIntVal("ThumbviewRoundRectArc");
		int frameMargin = RGPTParams.getIntVal("FrameMargin");
		if (THUMB_SCALED_IMAGE_MAKER_ICON == null) {
			THUMB_SCALED_IMAGE_MAKER_ICON = ImageUtils.scaleImage(
					THUMB_IMAGE_MAKER_ICON, thumbImgWt, thumbImgHt, true);
			THUMB_SCALED_IMAGE_MAKER_ICON = ImageUtils.fillTransparentPixels(
					THUMB_SCALED_IMAGE_MAKER_ICON, Color.WHITE);
		}
		BufferedImage thumbImg = THUMB_SCALED_IMAGE_MAKER_ICON;
		int y = vGap, w = thumbImg.getWidth(), h = thumbImg.getHeight();
		double x = (double) (panelSize.width - w) / (double) 2;
		if (m_ShowXONImagePane == SHOW_XON_ICON) {
			drawImagePreview(g2d, thumbImg, x, y);
			return;
		}
		// if (m_ShowXONImagePane == SHOW_IMAGE_PANEL)
		// Displaying the main Image
		thumbImg = (BufferedImage) m_OrigImageHolder.m_ThumbviewImage;
		y = vGap;
		w = thumbImg.getWidth();
		h = thumbImg.getHeight();
		x = (double) (panelSize.width - w) / (double) 2;
		Rectangle imgRect = drawImagePreview(g2d, thumbImg, x, y);

		// Displaying the transformed images
		ImageTransformHolder imtTransHldr = null;
		Vector<ImageTransformHolder> imgTransHldrList = null;
		imgTransHldrList = m_OrigImageHolder.m_ImageTransformHolder;
		for (int i = 0; i < imgTransHldrList.size(); i++) {
			imtTransHldr = imgTransHldrList.elementAt(i);
			y = (int) imgRect.height + vGap + 2 * frameMargin;
			thumbImg = imtTransHldr.m_ThumbPreviewImage;
			w = thumbImg.getWidth();
			h = thumbImg.getHeight();
			x = (double) (panelSize.width - w) / (double) 2;
			imgRect = drawImagePreview(g2d, thumbImg, x, y);
			imtTransHldr.m_ScreenRect = imgRect;
		}
	}

	private Rectangle drawImagePreview(Graphics2D g2d, BufferedImage thumbImg,
			double x, int y) {
		int frameMargin = RGPTParams.getIntVal("FrameMargin");
		int arc = RGPTParams.getIntVal("ThumbviewRoundRectArc");
		int w = thumbImg.getWidth(), h = thumbImg.getHeight();
		RoundRectangle2D.Double roundRect = new RoundRectangle2D.Double(x, y,
				w, h, arc, arc);
		Color origColor = g2d.getColor();
		Shape origClip = g2d.getClip();
		g2d.setColor(XONImageMaker.m_PanelColor);
		Rectangle imgRect = new Rectangle((int) x - frameMargin, y
				- frameMargin, w + 2 * frameMargin, h + 2 * frameMargin);
		g2d.draw3DRect((int) x - frameMargin, y - frameMargin, w + 2
				* frameMargin, h + 2 * frameMargin, true);
		g2d.setClip(roundRect);
		g2d.drawImage(thumbImg, (int) x, y, this);
		g2d.setColor(origColor);
		g2d.setClip(origClip);
		return imgRect;
	}

	public void repaintZoomViewPane(Graphics g) {
		boolean drawPt = false;
		Color origColor = g.getColor();
		Rectangle visibleRect = m_ZoomViewPane.getVisibleRect();
		Dimension panelSize = new Dimension((int) visibleRect.getWidth(),
				(int) visibleRect.getHeight());
		// RGPTLogger.logToConsole("Panel Size: "+panelSize);
		g.clearRect(0, 0, panelSize.width, panelSize.height);
		BufferedImage prevImg = THUMB_IMAGE_MAKER_ICON;
		if (m_ShowXONImagePane == SHOW_IMAGE_PANEL
				&& m_XONImageViewerPanel.m_ZoomPreviewImage != null) {
			drawPt = true;
			prevImg = m_XONImageViewerPanel.m_ZoomPreviewImage;
		}
		g.drawImage(prevImg, 0, 0, panelSize.width, panelSize.height, this);
		if (drawPt) {
			g.setColor(ImageUtils.getUniqueColor(prevImg, true));
			g.drawOval(panelSize.width / 2, panelSize.height / 2, 10, 10);
		}
		g.setColor(origColor);
	}

	// END FUNCTION FOR REPAINT METHODS

	public static void main(String[] args) {
		RGPTLogger.m_LogFile = RGPTLogger.XON_IM_LOG;
		RGPTParams.createServerProperties();
		Color colorpanel, buttonColor, fontColor;
		colorpanel = RGPTUtil.getColVal(RGPTParams.getVal("PANEL_COLOR"), ":");
		buttonColor = RGPTUtil
				.getColVal(RGPTParams.getVal("BUTTON_COLOR"), ":");
		fontColor = RGPTUtil.getColVal(RGPTParams.getVal("FONT_COLOR"), ":");
		XONImageMaker.m_PanelColor = colorpanel;
		XONImageMaker.m_ButtonColor = buttonColor;
		XONImageMaker.m_FontColor = fontColor;
		new XONImageMaker();
	}

}
