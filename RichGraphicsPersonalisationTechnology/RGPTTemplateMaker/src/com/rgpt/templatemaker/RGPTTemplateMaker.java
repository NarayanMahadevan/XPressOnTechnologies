package com.rgpt.templatemaker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import com.rgpt.imageutil.ImageUtils;
import com.rgpt.layoututil.BasicGridLayout;
import com.rgpt.pdflib.PDFPageListener;
import com.rgpt.util.CursorController;
import com.rgpt.util.LocalizationUtil;
import com.rgpt.util.RGPTActionListener;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTParams;
import com.rgpt.util.RGPTUIManager;
import com.rgpt.util.RGPTUIUtil;
import com.rgpt.util.RGPTUtil;

class RGPTTemplateStatusBar extends JPanel implements PDFPageListener {

	private static final long serialVersionUID = 1L;

	JLabel page_label;
	JLabel mode_label;
	JLabel mesg_label;

	RGPTTemplateMaker m_RGPTTemplateMaker;

	public RGPTTemplateStatusBar(RGPTTemplateMaker templMkr, int width,
			int height) {
		super.setPreferredSize(new Dimension(width, height));
		m_RGPTTemplateMaker = templMkr;
		page_label = new JLabel(" ");

		mode_label = new JLabel(" ");
		int mode_label_width = RGPTParams.getIntVal("EastPanelWidth");
		mode_label.setPreferredSize(new Dimension(mode_label_width, height));
		// JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 1,
		// 5));
		// labelPanel.add(mode_label);

		mesg_label = new JLabel(" ", 0);

		this.setLayout(new BorderLayout());
		this.add(createWestPanel(), BorderLayout.WEST);
		this.add(mesg_label, BorderLayout.CENTER);
		this.add(mode_label, BorderLayout.EAST);

		this.setBorder(new BevelBorder(BevelBorder.LOWERED));
		// this.setBackground(new Color(51, 153, 0));
	}

	JTextField m_PageNumField;
	JLabel m_PageLabel;

	private JPanel createWestPanel() {
		int westPanelWidth = RGPTParams.getIntVal("WestPanelWidth");
		int southPanelHeight = RGPTParams.getIntVal("SouthPanelHeight");
		int txtFldHeight = southPanelHeight - 10;
		JPanel westPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 1));
		westPanel.setPreferredSize(new Dimension(westPanelWidth,
				southPanelHeight));

		JButton navButton = null;
		String icon, tip = "";
		// int actionId;
		Dimension size = new Dimension(20, 20);
		icon = "rgpticons/up.png";
		tip = LocalizationUtil.getText("ToolTip_PrevPage");
		RGPTActionListener.PDFTemplateActions action = null;
		action = RGPTActionListener.PDFTemplateActions.PREV_PAGE;
		navButton = RGPTUIUtil.createImageButton(icon, tip, size,
				m_RGPTTemplateMaker, action.toString(), false);
		// This button is pressed and action performed when a user presses
		// VK_XXX keycodes.
		// registered for this Button
		navButton.setMnemonic(KeyEvent.VK_PAGE_UP);
		m_RGPTTemplateMaker.m_ActionButtonMap.put(action.toString(), navButton);
		westPanel.add(navButton);

		icon = "rgpticons/down.png";
		tip = LocalizationUtil.getText("ToolTip_NextPage");
		action = RGPTActionListener.PDFTemplateActions.NEXT_PAGE;
		navButton = RGPTUIUtil.createImageButton(icon, tip, size,
				m_RGPTTemplateMaker, action.toString(), false);
		navButton.setMnemonic(KeyEvent.VK_PAGE_DOWN);
		m_RGPTTemplateMaker.m_ActionButtonMap.put(action.toString(), navButton);
		westPanel.add(navButton);

		JPanel pageInfoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 1,
				1));
		m_PageNumField = new JTextField();
		m_PageNumField.setText("0");
		m_PageNumField.setHorizontalAlignment(JTextField.CENTER);
		m_PageNumField.setColumns(2);
		m_PageNumField.setPreferredSize(new Dimension(m_PageNumField
				.getPreferredSize().width, txtFldHeight));
		m_PageNumField.setBorder(new LineBorder(Color.BLACK));
		pageInfoPanel.add(m_PageNumField);
		m_PageNumField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				gotoPage();
			}
		});

		m_PageLabel = new JLabel(" / 0");
		pageInfoPanel.add(m_PageLabel);
		westPanel.add(pageInfoPanel);

		return westPanel;
	}

	public void gotoPage() {
		try {
			// int pageNum = Integer.parseInt(m_PageNumField.getText());
			// m_RGPTTemplateMaker.m_RGPTPDFViewHandler.gotoPage(pageNum);
			// int pgNo = m_RGPTTemplateMaker.m_RGPTPDFViewHandler
			// .getCurrentPageNum();
			// m_PageNumField.setText(String.valueOf(pgNo));
		} catch (Exception ex) {
			// int pgNo = m_RGPTTemplateMaker.m_RGPTPDFViewHandler
			// .getCurrentPageNum();
			// m_PageNumField.setText(String.valueOf(pgNo));
		}
	}

	@Override
	public void reportCurrentPage(int current_page, int num_pages, Object data) {
		m_PageNumField.setText(String.valueOf(current_page));
		m_PageLabel.setText(" / " + num_pages);
	}

	public void displaystatus(String text) {
		if (text == null || text.length() == 0)
			return;
		mesg_label.setText(text);
	}

	public void displaymode(String text) {
		if (text == null || text.length() == 0)
			return;
		int fontSz = RGPTParams.getIntVal("LabelFontSize");
		text = RGPTUIUtil.getHTMLTextForComp(text, fontSz);
		mode_label.setText(text);
	}
}

public class RGPTTemplateMaker extends JFrame implements RGPTActionListener {

	private static final long serialVersionUID = 1L;

	// Singleton Class
	private static RGPTTemplateMaker m_RGPTTemplateMaker;
	// Title
	public String m_XPressOnTitle = LocalizationUtil
			.getText("XPressOnAppTitle");

	// lightcolor for panel
	public static Color m_PanelColor = new Color(204, 204, 204);
	// darkercolor for button
	public static Color m_ButtonColor = new Color(160, 213, 243);
	// darkestcolor for text
	public static Color m_FontColor = new Color(160, 213, 243);

	// East and West Panel Width
	public static int m_WestPanelWidth;
	public static int m_WestTopPanelHeight;
	public static int m_EastPanelWidth;
	public static int m_SouthPanelHeight;

	// Use Windows Look and Feel
	public static boolean m_UseWindowsLookAndFeel = false;

	// PDFViewMain is RGPT Class which encapsulates PDFView from PDFNet Library
	// to
	// display PDF. It has all the controller function to work with the PDF Lib
	public PDFViewController m_PDFViewController;

	// MyPDFView inherits from PDFNet's PDFView which displays the PDF.
	// MyPDFView
	// provides addtional functionality of Variable Data Selection and Insertion
	// of new
	// Elements
	// MyPDFView m_RGPTPDFView;

	// TemplateViewer is the JPanel that holds the either the PDF Template or
	// the
	// Image Template
	JPanel m_TemplateViewer;

	// ThumbnailViewer is the JPanel that holds the either the PDF Template or
	// the
	// Image Template
	JPanel m_ThumbnailViewer;

	// Content Pane
	JPanel m_ContentPane;
	JPanel m_XONContentPane;
	JPanel m_ThumbnailPane;
	Dimension m_ContentPaneSize;

	// This label is added to the content pane and is visible when no files are
	// open
	JPanel m_XPressOnDesignPane;

	// Status Bar to show Page Navigation, Current Page and VDP Selection Mode
	public RGPTTemplateStatusBar m_StatusBar;

	// This map maintains the HashMap of ActionId to the Corresponding Button
	public Map<String, JButton> m_ActionButtonMap;

	// This map maintains the HashMap of ActionId to the Corresponding Button
	public static Map<String, String> m_ActionMnemonicKeysMap;

	// This map maintains the HashMap of ActionId to the Corresponding Message
	// to be
	// showed on the Status Bar
	public static Map<String, String> m_ActionMessage;

	// This map maintains the HashMap of ActionId to the Corresponding Button
	// Selection to be
	// showed on the Status Bar
	public static Map<String, String> m_ActionSelection;

	// MNEMONIC_KEYS correspond to Action Id
	static {
		m_ActionMessage = new HashMap<String, String>();
		m_ActionSelection = new HashMap<String, String>();
		m_ActionMnemonicKeysMap = new HashMap<String, String>();
		m_ActionMnemonicKeysMap.put(PDFTemplateActions.FILE_OPEN.toString(),
				"control O");
		m_ActionMnemonicKeysMap.put(PDFTemplateActions.FILE_CLOSE.toString(),
				"control W");
		m_ActionMnemonicKeysMap.put(PDFTemplateActions.FILE_SAVE.toString(),
				"control S");
		m_ActionMnemonicKeysMap.put(PDFTemplateActions.FILE_UPLOAD.toString(),
				"control U");
	}

	// START FUNCTION TO CONSTRUCT AND DISTRUCT THIS OBJECT

	private RGPTTemplateMaker() {
		m_WestPanelWidth = RGPTParams.getIntVal("WestPanelWidth");
		m_WestTopPanelHeight = RGPTParams.getIntVal("WestTopPanelHeight");
		m_EastPanelWidth = RGPTParams.getIntVal("EastPanelWidth");
		m_SouthPanelHeight = RGPTParams.getIntVal("SouthPanelHeight");
		m_ActionButtonMap = new HashMap<String, JButton>();
		this.setLookAndFeel();
		this.createUI();
		this.pack();
		this.show();
	}

	public static RGPTTemplateMaker getInstance() {
		if (m_RGPTTemplateMaker == null)
			m_RGPTTemplateMaker = new RGPTTemplateMaker();
		return m_RGPTTemplateMaker;
	}

	public void initPDFView() {
		m_PDFViewController = PDFViewController.getInstance();
		m_PDFViewController.initPDFViewController(this);
	}

	// NOTES:
	// Setting the Font of the Main Frame or the Color or Size is not possible
	// as the control is given to native Look and Feel. This is because JFrame
	// extends from Frame.
	private void setLookAndFeel() {
		RGPTUIManager.setLookAndFeel();
		RGPTUIManager.setUIDefaults(m_PanelColor, m_ButtonColor, m_FontColor);
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		this.setTitle(m_XPressOnTitle);
		this.setResizable(false);
		this.setLocation(0, 5);

		// Maximizing the XPresson Designer UI to fit the complete window
		this.setExtendedState(this.getExtendedState() | Frame.MAXIMIZED_BOTH);

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
		m_XONContentPane = new JPanel(new BorderLayout());
		m_ContentPane = new JPanel(new BorderLayout());
		m_ContentPane.setPreferredSize(m_ContentPaneSize);
		m_XONContentPane.setBackground(Color.WHITE);
		m_ContentPane.add(m_XONContentPane, BorderLayout.CENTER);
		this.setContentPane(m_ContentPane);
		Border raisedBorder = BorderFactory.createRaisedBevelBorder();
		JComponent cont = (JComponent) getContentPane();
		cont.setBorder(raisedBorder);

		// Setting the Close operation on XPressOn
		this.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent arg0) {
				RGPTLogger.logToFile("Templatization Window Closed");
			}

			public void windowClosing(WindowEvent arg0) {
				RGPTLogger.logToFile("Templatization Window Closing");
				closeTemplateMaker();
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
		RGPTLogger
				.logToConsole("Available Size after the Tool Bar for Display: "
						+ maxBounds);
		return maxBounds;
	}

	public void closeTemplateMaker() {
		m_PDFViewController.terminatePDFLib();
		String currDate = RGPTUtil.getCurrentDate("yyyy.MM.dd 'at' HH:mm:ss");
		RGPTLogger.logToFile("Templatization Software Stopped at: " + currDate);
		RGPTLogger
				.logToFile("\n***********************************************\n\n");
		System.exit(0);
	}

	// END FUNCTION TO CONSTRUCT AND DISTRUCT THIS OBJECT

	// START FUNCTIONS TO CREATE UI COMPONENTS

	public void createUI() {
		try {
			this.createXPressOnDesignPane();
			JPanel eastPanel = this.createEastPanel();
			JPanel westPanel = this.createWestPanel();
			JPanel southPanel = this.createSouthPanel();
			// eastPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
			// westPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
			// southPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
			// m_XPressOnDesignPane.setBorder(new
			// BevelBorder(BevelBorder.RAISED));
			m_XONContentPane.add(westPanel, BorderLayout.WEST);
			m_XONContentPane.add(eastPanel, BorderLayout.EAST);
			m_XONContentPane.add(southPanel, BorderLayout.SOUTH);
			m_XONContentPane.add(m_XPressOnDesignPane, BorderLayout.CENTER);
			m_XONContentPane.revalidate();
		} catch (Exception ex) {
			RGPTLogger.logToFile("Exception at createUI ", ex);
		}
	}

	// This Method creates Panel to display XPressOnDesign Image
	private void createXPressOnDesignPane() throws Exception {
		if (m_XPressOnDesignPane != null)
			return;
		final BufferedImage xpressOnImage = ImageUtils.getBufferedImage(
				"res/XPressOnDesignLabel.png", this.getClass());
		m_XPressOnDesignPane = new JPanel() {
			private static final long serialVersionUID = 1L;

			public void paint(Graphics g) {
				Rectangle visibleRect = this.getVisibleRect();
				Dimension panelSize = new Dimension(
						(int) visibleRect.getWidth(),
						(int) visibleRect.getHeight());
				// RGPTLogger.logToConsole("Panel Size: "+panelSize);
				g.drawImage(xpressOnImage, 0, 0, panelSize.width,
						panelSize.height, this);
			}
		};
	}

	private JPanel createEastPanel() {
		JPanel eastPanel = new JPanel();
		Dimension eastPanelSz = new Dimension(m_EastPanelWidth,
				m_ContentPaneSize.height);
		eastPanel.setPreferredSize(eastPanelSz);
		eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));

		// Creating JComponents that go into East Panel.
		JPanel pdfElemLabel, insertElemLabel, editElemLabel, imageUtilityLabel;
		JPanel pdfElemPanel = this.createPDFElemSelPanel();
		int wt = m_EastPanelWidth, ht = RGPTParams.getIntVal("LabelTextHeight");
		String lblTxt = LocalizationUtil.getText("SelPDFElementText"), fontSzProp = "LabelFontSize";
		pdfElemLabel = RGPTUIUtil.createWhiteLabelPanel(lblTxt, wt, ht,
				fontSzProp);
		eastPanel.add(pdfElemLabel);
		eastPanel.add(pdfElemPanel);

		JPanel editElemPanel = this.createEditElemPanel();
		lblTxt = LocalizationUtil.getText("EditElementText");
		editElemLabel = RGPTUIUtil.createWhiteLabelPanel(lblTxt, wt, ht,
				fontSzProp);
		eastPanel.add(editElemLabel);
		eastPanel.add(editElemPanel);

		JPanel insertElemPanel = this.createInsertElemPanel();
		lblTxt = LocalizationUtil.getText("InsertElementText");
		insertElemLabel = RGPTUIUtil.createWhiteLabelPanel(lblTxt, wt, ht,
				fontSzProp);
		eastPanel.add(insertElemLabel);
		eastPanel.add(insertElemPanel);

		JPanel imageUtilityPanel = this.createImageUtilityPanel();
		lblTxt = LocalizationUtil.getText("ImageUtilityText");
		imageUtilityLabel = RGPTUIUtil.createWhiteLabelPanel(lblTxt, wt, ht,
				fontSzProp);
		eastPanel.add(imageUtilityLabel);
		eastPanel.add(imageUtilityPanel);

		return eastPanel;
	}

	private JPanel createPDFElemSelPanel() {
		JButton selPDFElemButton;
		String icon = "", tip = "";
		String[] names;
		PDFTemplateActions action = null;

		int align = FlowLayout.CENTER;
		JPanel pdfElemPanel = new JPanel(new FlowLayout(align, 15, 2));
		int panelHt = RGPTParams.getIntVal("PDFElemPanelHeight");
		Dimension pdfElemPanelSz = new Dimension(m_EastPanelWidth, panelHt);
		pdfElemPanel.setPreferredSize(pdfElemPanelSz);

		Dimension size = new Dimension(75, 50);
		JPanel buttonPanel = new JPanel(new BasicGridLayout(2, 2, 5, 5));

		icon = "rgpticons/SelectImage.png";
		action = PDFTemplateActions.SELECT_IMAGE;
		names = LocalizationUtil.getText("SelectImage").split("::");
		tip = LocalizationUtil.getText("ToolTip_SelectImage");
		m_ActionSelection.put(action.toString(), names[1]);
		selPDFElemButton = RGPTUIUtil.createButton(icon, names[0], null, tip,
				size, this, action.toString(), true);
		m_ActionMessage.put(action.toString(),
				RGPTUtil.replaceTextWithDelim(tip, "<br>", " "));
		m_ActionButtonMap.put(action.toString(), selPDFElemButton);
		buttonPanel.add(selPDFElemButton);

		icon = "rgpticons/SelectParaText.png";
		action = PDFTemplateActions.SELECT_PARA;
		names = LocalizationUtil.getText("SelectPara").split("::");
		tip = LocalizationUtil.getText("ToolTip_SelectPara");
		m_ActionSelection.put(action.toString(), names[1]);
		selPDFElemButton = RGPTUIUtil.createButton(icon, names[0], null, tip,
				size, this, action.toString(), true);
		m_ActionMessage.put(action.toString(),
				RGPTUtil.replaceTextWithDelim(tip, "<br>", " "));
		m_ActionButtonMap.put(action.toString(), selPDFElemButton);
		buttonPanel.add(selPDFElemButton);

		icon = "rgpticons/SelectWordText.png";
		action = PDFTemplateActions.SELECT_WORD;
		names = LocalizationUtil.getText("SelectWord").split("::");
		tip = LocalizationUtil.getText("ToolTip_SelectWord");
		m_ActionSelection.put(action.toString(), names[1]);
		selPDFElemButton = RGPTUIUtil.createButton(icon, names[0], null, tip,
				size, this, action.toString(), true);
		m_ActionMessage.put(action.toString(),
				RGPTUtil.replaceTextWithDelim(tip, "<br>", " "));
		m_ActionButtonMap.put(action.toString(), selPDFElemButton);
		buttonPanel.add(selPDFElemButton);

		icon = "rgpticons/SelectLineText.png";
		action = PDFTemplateActions.SELECT_LINE;
		names = LocalizationUtil.getText("SelectLine").split("::");
		tip = LocalizationUtil.getText("ToolTip_SelectLine");
		m_ActionSelection.put(action.toString(), names[1]);
		selPDFElemButton = RGPTUIUtil.createButton(icon, names[0], null, tip,
				size, this, action.toString(), true);
		m_ActionMessage.put(action.toString(),
				RGPTUtil.replaceTextWithDelim(tip, "<br>", " "));
		m_ActionButtonMap.put(action.toString(), selPDFElemButton);
		buttonPanel.add(selPDFElemButton);

		pdfElemPanel.add(buttonPanel);
		return pdfElemPanel;
	}

	private JPanel createEditElemPanel() {
		JButton editElemButton;
		String icon = "", tip = "";
		String[] names;
		PDFTemplateActions action = null;

		JPanel editElemPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15,
				5));
		int panelHt = RGPTParams.getIntVal("EditElemPanelHeight");
		Dimension editElemPanelSz = new Dimension(m_EastPanelWidth, panelHt);
		editElemPanel.setPreferredSize(editElemPanelSz);

		Dimension size = new Dimension(75, 50);
		JPanel buttonPanel = new JPanel(new BasicGridLayout(1, 2, 5, 6));

		icon = "rgpticons/EditTextAndImageBtn.png";
		action = PDFTemplateActions.EDIT_ELEM;
		names = LocalizationUtil.getText("EditPDFElement").split("::");
		tip = LocalizationUtil.getText("ToolTip_EditPDFElement");
		m_ActionSelection.put(action.toString(), names[1]);
		editElemButton = RGPTUIUtil.createButton(icon, names[0], null, tip,
				size, this, action.toString(), true);
		m_ActionMessage.put(action.toString(),
				RGPTUtil.replaceTextWithDelim(tip, "<br>", " "));
		m_ActionButtonMap.put(action.toString(), editElemButton);
		buttonPanel.add(editElemButton);

		icon = "rgpticons/DeleteTextAndImageBtn.png";
		action = PDFTemplateActions.DELETE_ELEM;
		names = LocalizationUtil.getText("DeleteElement").split("::");
		tip = LocalizationUtil.getText("ToolTip_DeleteElement");
		m_ActionSelection.put(action.toString(), names[1]);
		editElemButton = RGPTUIUtil.createButton(icon, names[0], null, tip,
				size, this, action.toString(), true);
		m_ActionMessage.put(action.toString(),
				RGPTUtil.replaceTextWithDelim(tip, "<br>", " "));
		m_ActionButtonMap.put(action.toString(), editElemButton);
		buttonPanel.add(editElemButton);

		editElemPanel.add(buttonPanel);
		return editElemPanel;
	}

	private JPanel createInsertElemPanel() {
		JButton insertElemButton;
		String icon = "", tip = "";
		String[] names;
		PDFTemplateActions action = null;

		JPanel insertElemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15,
				2));
		int panelHt = RGPTParams.getIntVal("InsertElemPanelHeight");
		Dimension insertElemPanelSz = new Dimension(m_EastPanelWidth, panelHt);
		insertElemPanel.setPreferredSize(insertElemPanelSz);

		Dimension size = new Dimension(50, 50);
		JPanel buttonPanel = new JPanel(new BasicGridLayout(2, 2, 5, 5));

		// Insert Text On Line Button
		icon = "rgpticons/InsertTextOnLine.png";
		action = PDFTemplateActions.INSERT_TEXT_ON_LINE;
		names = LocalizationUtil.getText("InsertTextOnLine").split("::");
		tip = LocalizationUtil.getText("ToolTip_InsertTextOnLine");
		m_ActionSelection.put(action.toString(), names[1]);
		insertElemButton = RGPTUIUtil.createButton(icon, names[0], null, tip,
				size, this, action.toString(), true);
		m_ActionMessage.put(action.toString(),
				RGPTUtil.replaceTextWithDelim(tip, "<br>", " "));
		m_ActionButtonMap.put(action.toString(), insertElemButton);
		buttonPanel.add(insertElemButton);

		// Insert Image Button
		// icon = "rgpticons/InsertImage.png"; actionId = INSERT_IMAGE;
		// String name = LocalizationUtil.getText("InsertImage"); tip =
		// LocalizationUtil.getText("ToolTip_InsertImage");
		// insertElemButton = RGPTUIUtil.createButton(icon, name, tip, size,
		// this, actionId);
		// m_ActionIdMessage.put(actionId, RGPTUtil.replaceTextWithDelim(tip,
		// "<br>", " "));
		// m_ActionIdButtonMap.put(actionId, insertElemButton);
		// buttonPanel.add(insertElemButton);

		// Insert Text Button
		// icon = "rgpticons/InsertText.png"; actionId = INSERT_TEXT;
		// name = LocalizationUtil.getText("InsertText"); tip =
		// LocalizationUtil.getText("ToolTip_InsertText");
		// insertElemButton = RGPTUIUtil.createButton(icon, name, tip, size,
		// this, actionId);
		// m_ActionIdMessage.put(actionId, RGPTUtil.replaceTextWithDelim(tip,
		// "<br>", " "));
		// m_ActionIdButtonMap.put(actionId, insertElemButton);
		// buttonPanel.add(insertElemButton);

		// Insert Image on Shape Button
		// icon = "rgpticons/InsertImageOnShape.png"; actionId =
		// INSERT_IMAGE_ON_SHAPE;
		// name = LocalizationUtil.getText("InsertImageOnShape"); tip =
		// LocalizationUtil.getText("ToolTip_InsertImageOnShape");
		// insertElemButton = RGPTUIUtil.createButton(icon, name, tip, size,
		// this, actionId);
		// buttonPanel.add(insertElemButton);

		// Insert Insert Text On Shape Button
		// icon = "rgpticons/InsertTextOnShape.png"; actionId =
		// INSERT_TEXT_ON_SHAPE;
		// name = LocalizationUtil.getText("InsertTextOnShape"); tip =
		// LocalizationUtil.getText("ToolTip_InsertTextOnShape");
		// insertElemButton = RGPTUIUtil.createButton(icon, name, tip, size,
		// this, actionId);
		// m_ActionIdMessage.put(actionId, RGPTUtil.replaceTextWithDelim(tip,
		// "<br>", " "));
		// m_ActionIdButtonMap.put(actionId, insertElemButton);
		// buttonPanel.add(insertElemButton);

		// Insert Insert Image On Path Button
		// icon = "rgpticons/InsertImageOnPath.png"; actionId =
		// INSERT_IMAGE_ON_PATH;
		// name = LocalizationUtil.getText("InsertImageOnPath"); tip =
		// LocalizationUtil.getText("ToolTip_InsertImageOnPath");
		// insertElemButton = RGPTUIUtil.createButton(icon, name, tip, size,
		// this, actionId);
		// buttonPanel.add(insertElemButton);

		// Insert Insert Text On Path Button
		// icon = "rgpticons/InsertTextOnPath.png"; actionId =
		// INSERT_TEXT_ON_PATH;
		// name = LocalizationUtil.getText("InsertTextOnPath"); tip =
		// LocalizationUtil.getText("ToolTip_InsertTextOnPath");
		// insertElemButton = RGPTUIUtil.createButton(icon, name, tip, size,
		// this, actionId);
		// m_ActionIdMessage.put(actionId, RGPTUtil.replaceTextWithDelim(tip,
		// "<br>", " "));
		// m_ActionIdButtonMap.put(actionId, insertElemButton);
		// buttonPanel.add(insertElemButton);

		insertElemPanel.add(buttonPanel);
		return insertElemPanel;
	}

	private JPanel createImageUtilityPanel() {
		// JButton selPDFElemButton;
		// String icon = "", name = "", tip = "";
		// int actionId = -1;

		JPanel imageUtilityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,
				15, 5));
		int panelHt = RGPTParams.getIntVal("ImageUtilityPanelHeight");
		Dimension imageUtilityPanelSz = new Dimension(m_EastPanelWidth, panelHt);
		imageUtilityPanel.setPreferredSize(imageUtilityPanelSz);

		// Dimension size = new Dimension(50, 50);
		JPanel buttonPanel = new JPanel(new BasicGridLayout(2, 2, 5, 5));
		// File Open Button
		// icon = "rgpticons/FileOpen.png"; actionId = FILE_OPEN;
		// name = LocalizationUtil.getText("FileOpen"); tip =
		// LocalizationUtil.getText("ToolTip_FileOpen");
		// JButton fopen = RGPTUIUtil.createButton(icon, name, tip, size, this,
		// actionId);
		// m_ActionIdMessage.put(actionId, RGPTUtil.replaceTextWithDelim(tip,
		// "<br>", " "));
		// buttonPanel.add(fopen);

		imageUtilityPanel.add(buttonPanel);
		return imageUtilityPanel;
	}

	private JPanel createWestPanel() {
		String icon = "", tip = "";
		String[] names;
		PDFTemplateActions action = null;
		JPanel westPanel = new JPanel(new BorderLayout());
		Dimension westPanelSz = new Dimension(m_WestPanelWidth,
				m_ContentPaneSize.height);
		westPanel.setPreferredSize(westPanelSz);

		// Creating TopPanel for File Action and Java Preview and Center Panel
		// for
		// Creative Page Preview
		JPanel westTopPanel = new JPanel();
		westTopPanel.setPreferredSize(new Dimension(m_WestPanelWidth,
				m_WestTopPanelHeight));
		westTopPanel.setLayout(new BoxLayout(westTopPanel, BoxLayout.Y_AXIS));

		// File Action Panel

		JPanel fileActionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,
				15, 2));
		JPanel buttonPanel = new JPanel(new BasicGridLayout(2, 2, 5, 5));
		Dimension size = new Dimension(50, 50);
		// File Open Button
		icon = "rgpticons/FileOpen.png";
		action = PDFTemplateActions.FILE_OPEN;
		names = LocalizationUtil.getText("FileOpen").split("::");
		tip = LocalizationUtil.getText("ToolTip_FileOpen");
		JButton fopen = RGPTUIUtil.createButton(icon, names[0], "", tip, size,
				this, action.toString(), true);
		fopen.setEnabled(true);
		RGPTUIUtil.setMnemonicKey(m_XONContentPane, this, action.toString());
		m_ActionSelection.put(action.toString(), names[1]);
		m_ActionMessage.put(action.toString(),
				RGPTUtil.replaceTextWithDelim(tip, "<br>", " "));
		m_ActionButtonMap.put(action.toString(), fopen);
		// File Close Button
		icon = "rgpticons/FileClose.png";
		action = PDFTemplateActions.FILE_CLOSE;
		names = LocalizationUtil.getText("FileClose").split("::");
		tip = LocalizationUtil.getText("ToolTip_FileClose");
		JButton fclose = RGPTUIUtil.createButton(icon, names[0], "", tip, size,
				this, action.toString(), true);
		RGPTUIUtil.setMnemonicKey(m_XONContentPane, this, action.toString());
		m_ActionSelection.put(action.toString(), names[1]);
		m_ActionMessage.put(action.toString(),
				RGPTUtil.replaceTextWithDelim(tip, "<br>", " "));
		m_ActionButtonMap.put(action.toString(), fclose);
		// File Save Button
		icon = "rgpticons/FileSave.png";
		action = PDFTemplateActions.FILE_SAVE;
		names = LocalizationUtil.getText("FileSave").split("::");
		tip = LocalizationUtil.getText("ToolTip_FileSave");
		JButton fsave = RGPTUIUtil.createButton(icon, names[0], "", tip, size,
				this, action.toString(), true);
		RGPTUIUtil.setMnemonicKey(m_XONContentPane, this, action.toString());
		m_ActionSelection.put(action.toString(), names[1]);
		m_ActionMessage.put(action.toString(),
				RGPTUtil.replaceTextWithDelim(tip, "<br>", " "));
		m_ActionButtonMap.put(action.toString(), fsave);
		// File Upload Button
		icon = "rgpticons/FileUpload.png";
		action = PDFTemplateActions.FILE_UPLOAD;
		names = LocalizationUtil.getText("FileUpload").split("::");
		tip = LocalizationUtil.getText("ToolTip_FileUpload");
		JButton fupload = RGPTUIUtil.createButton(icon, names[0], "", tip,
				size, this, action.toString(), true);
		RGPTUIUtil.setMnemonicKey(m_XONContentPane, this, action.toString());
		m_ActionSelection.put(action.toString(), names[1]);
		m_ActionMessage.put(action.toString(),
				RGPTUtil.replaceTextWithDelim(tip, "<br>", " "));
		m_ActionButtonMap.put(action.toString(), fupload);

		// buttonPanel.setMinimumSize(new Dimension(m_WestPanelWidth-20, 120));
		int filePanelHt = RGPTParams.getIntVal("FileActionPanelHeight");
		fileActionPanel.setPreferredSize(new Dimension(m_WestPanelWidth,
				filePanelHt));
		buttonPanel.add(fopen);
		buttonPanel.add(fclose);
		buttonPanel.add(fsave);
		buttonPanel.add(fupload);
		// fileActionPanel.add(buttonPanel, BorderLayout.CENTER);
		fileActionPanel.add(buttonPanel);
		// fileActionPanel.setBorder(new LineBorder(Color.BLACK));
		westTopPanel.add(fileActionPanel);

		// END File Action Panel

		// Design Previoew Panel

		JPanel designPreviewPanel = new JPanel(new BorderLayout());
		int previewPanelHt = RGPTParams.getIntVal("DesignPreviewPanelHeight");
		designPreviewPanel.setPreferredSize(new Dimension(m_WestPanelWidth,
				previewPanelHt));
		// designPreviewPanel.setLayout(new BoxLayout(designPreviewPanel,
		// BoxLayout.Y_AXIS));
		// Design Preview Button
		size = new Dimension(m_WestPanelWidth, 70);
		icon = "rgpticons/TryNow.png";
		action = PDFTemplateActions.TRY_DESIGN;
		tip = LocalizationUtil.getText("ToolTip_TryNow");
		JButton tryNow = RGPTUIUtil.createImageButton(icon, tip, size, this,
				action.toString(), true);
		m_ActionMessage.put(action.toString(),
				RGPTUtil.replaceTextWithDelim(tip, "<br>", " "));
		m_ActionButtonMap.put(action.toString(), tryNow);
		designPreviewPanel.add(tryNow, BorderLayout.CENTER);
		// int wt = m_WestPanelWidth, ht = 50;
		// String xpressOnText = LocalizationUtil.getText("XPressOnText");
		// JLabel label = RGPTUIUtil.createLabel(xpressOnText, wt, ht,
		// "LabelFontSize");
		icon = "rgpticons/XPressOnLogo.png";
		JLabel label = RGPTUIUtil.createLabel(icon);
		designPreviewPanel.add(label, BorderLayout.SOUTH);
		// designPreviewPanel.setBorder(new LineBorder(Color.BLACK));
		westTopPanel.add(designPreviewPanel);

		m_ThumbnailPane = new JPanel(new BorderLayout());
		// westCenterPanel.setBorder(new LineBorder(Color.BLACK));

		westPanel.add(westTopPanel, BorderLayout.NORTH);
		westPanel.add(m_ThumbnailPane, BorderLayout.CENTER);

		return westPanel;
	}

	private JPanel createSouthPanel() {
		JPanel southPanel = new JPanel(new BorderLayout());
		Dimension southPanelSz = new Dimension(m_ContentPaneSize.width,
				m_SouthPanelHeight);
		southPanel.setPreferredSize(southPanelSz);
		m_StatusBar = new RGPTTemplateStatusBar(this, m_ContentPaneSize.width,
				m_SouthPanelHeight);
		southPanel.add(m_StatusBar, BorderLayout.CENTER);
		return southPanel;
	}

	// END FUNCTIONS TO CREATE UI COMPONENTS

	// START FUNCTION FOR INTERFACE METHODS

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setComponentData(String action, String compName, JComponent comp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCursor(String action) {
		// TODO Auto-generated method stub

	}

	@Override
	public void resetCursor() {
		this.setCursor(CursorController.m_DefaultCursor);
	}

	@Override
	// This method returns the mneomonic key for the action id else null
	public String getMnemonicKey(String action) {
		return m_ActionMnemonicKeysMap.get(action); // m_ActionMnemonicKeysMap.get(action);
	}

	@Override
	public void actionPerformed(ActionEvent evnt) {
		RGPTLogger.logToConsole("Action Cmnd: " + evnt.getActionCommand()
				+ " source: " + evnt.getSource());
		String[] cmnd = evnt.getActionCommand().split("=");
		if (RGPTUtil.isWFActionPerformed(cmnd[1]))
			performRGPTWFAction(cmnd[1]);
		else if (RGPTUtil.isDialogActionPerformed(cmnd[1]))
			performRGPTDialogAction(cmnd[1]);
		else
			performRGPTAction(cmnd[1]);
	}

	@Override
	public void performRGPTWFAction(String actionStr) {
		WFActions action = WFActions.valueOf(actionStr);
	}

	@Override
	public void performRGPTDialogAction(String actionStr) {
		DialogActions action = DialogActions.valueOf(actionStr);
	}

	@Override
	public void performImageFilterAction(String actionStr) {
		ImageFilterActions action = ImageFilterActions.valueOf(actionStr);
		RGPTLogger.logToFile("Image Filter Action is: " + action.toString());
		switch (action) {
		}
	}

	@Override
	public void performRGPTAction(String actionStr) {
		PDFTemplateActions action = PDFTemplateActions.valueOf(actionStr);
		RGPTLogger.logToFile("PDF Action Performed is: " + action);
		switch (action) {
		case FILE_OPEN:
			m_PDFViewController.open();
			break;
		case FILE_CLOSE:
			m_PDFViewController.close();
			break;
		case SELECT_IMAGE:
			m_PDFViewController.setvdpImgmode();
			break;
		}
		RGPTLogger.logToConsole("Action Id Mesg: "
				+ m_ActionMessage.get(actionStr));
		m_StatusBar.displaymode(m_ActionSelection.get(actionStr));
		m_StatusBar.displaystatus(m_ActionMessage.get(actionStr));
	}

	// END FUNCTION FOR INTERFACE METHODS

	// START FUNCTION TO WORK WITH UI COMPONENTS

	public void setView(String pdfFileName, int pgno, int totPgs,
			JComponent pdfThumbView, JPanel pdfViewer) {
		setTitle(m_XPressOnTitle + " - [" + pdfFileName + "]");
		setButtonActivation(pgno, totPgs);
		m_StatusBar.reportCurrentPage(pgno, totPgs, null);
		setPDFThumbView(pdfThumbView);
		setPDFViewer(pdfViewer);
	}

	public void resetView() {
		setTitle(this.m_XPressOnTitle);
		m_StatusBar.displaystatus(" ");
		m_StatusBar.reportCurrentPage(0, 0, null);
		removeThumbView();
		showXPressOnLogo();
		setButtonActivationOnClose();
		repaint();
	}

	public void setButtonActivationOnClose() {
		setButtonsEnabled(PDF_BUTTON_ACTIVATION_IDS, false);
		setButtonsEnabled(ELEM_SEL_BUTTON_ACTIVATION_IDS, false);
	}

	public void setButtonActivation() {
		setButtonsEnabled(PDF_BUTTON_ACTIVATION_IDS, true);
	}

	public void setButtonActivation(int pgno, int totPgs) {
		// If no PDF Document is active
		if (!m_PDFViewController.isPDFDocActive()) {
			setButtonsEnabled(PDF_BUTTON_ACTIVATION_IDS, false);
			setButtonsEnabled(ELEM_SEL_BUTTON_ACTIVATION_IDS, false);
			return;
		}
		setButtonsEnabled(PDF_BUTTON_ACTIVATION_IDS, true);
		if (pgno == 1) {
			JButton button = m_ActionButtonMap.get(PDFTemplateActions.PREV_PAGE
					.toString());
			if (button != null)
				button.setEnabled(false);
		} else if (pgno == totPgs) {
			JButton button = m_ActionButtonMap.get(PDFTemplateActions.NEXT_PAGE
					.toString());
			if (button != null)
				button.setEnabled(false);
		}
	}

	public void setButtonsEnabled(String[] actionIds, boolean enabled) {
		for (int i = 0; i < actionIds.length; i++) {
			JButton button = m_ActionButtonMap.get(actionIds[i]);
			if (button != null)
				button.setEnabled(enabled);
			else
				RGPTLogger.logToConsole("Button ID: " + actionIds[i]
						+ " not Registered");
		}
	}

	public void setPDFThumbView(JComponent pdfThumbView) {
		m_ThumbnailViewer = new JPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(pdfThumbView,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		m_ThumbnailViewer.add(scrollPane, BorderLayout.CENTER);
		resetThumbnailPane(true, "PDF");
	}

	public void removeThumbView() {
		resetThumbnailPane(false, "");
	}

	// If PDF Viewer is set to False then the Logo is set otherwise logo is
	// unset
	private void resetThumbnailPane(boolean setTemplateViewer,
			String templateType) {
		int zorder = -1;
		if (m_ThumbnailViewer == null)
			return;
		if (setTemplateViewer) {
			zorder = m_ThumbnailPane.getComponentZOrder(m_ThumbnailViewer);
			if (zorder == -1)
				m_ThumbnailPane.add(m_ThumbnailViewer, BorderLayout.CENTER);
			m_ThumbnailPane.revalidate();
			this.repaint();
			return;
		}
		// This case is executed if the PDF Viewer needs to be removed
		zorder = m_ThumbnailPane.getComponentZOrder(m_ThumbnailViewer);
		if (zorder != -1)
			m_ThumbnailPane.remove(m_ThumbnailViewer);
		m_ThumbnailPane.revalidate();
		this.repaint();
	}

	public void setImageViewer() {
		resetContentPane(true, "IMAGE");
	}

	public void showXPressOnLogo() {
		resetContentPane(false, "");
	}

	public void setPDFViewer(JPanel pdfViewer) {
		m_TemplateViewer = new JPanel(new BorderLayout());
		m_TemplateViewer.add(pdfViewer, BorderLayout.CENTER);
		resetContentPane(true, "PDF");
	}

	// If PDF Viewer is set to False then the Logo is set otherwise logo is
	// unset
	private void resetContentPane(boolean setTemplateViewer, String templateType) {
		int zorder = -1;
		if (m_XPressOnDesignPane == null || m_TemplateViewer == null)
			return;
		if (setTemplateViewer) {
			zorder = m_XONContentPane.getComponentZOrder(m_XPressOnDesignPane);
			if (zorder != -1)
				m_XONContentPane.remove(m_XPressOnDesignPane);
			zorder = m_XONContentPane.getComponentZOrder(m_TemplateViewer);
			if (zorder == -1)
				m_XONContentPane.add(m_TemplateViewer, BorderLayout.CENTER);
			m_XONContentPane.revalidate();
			this.repaint();
			return;
		}
		// This case is executed if the PDF Viewer needs to be removed
		zorder = m_XONContentPane.getComponentZOrder(m_TemplateViewer);
		if (zorder != -1)
			m_XONContentPane.remove(m_TemplateViewer);
		zorder = m_XONContentPane.getComponentZOrder(m_XPressOnDesignPane);
		if (zorder == -1)
			m_XONContentPane.add(m_XPressOnDesignPane, BorderLayout.CENTER);
		m_XONContentPane.revalidate();
		this.repaint();
	}

	// END FUNCTION TO WORK WITH UI COMPONENTS

	// START FUNCTION MAIN
	public static void main(String[] args) {
		RGPTLogger.logToConsole("Test Begin");
		try {
			RGPTParams.createServerProperties();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		RGPTLogger.logToConsole("Test1");
		RGPTLogger.m_LogFile = RGPTLogger.TEMPLATE_MAKER_LOG;
		Color colorpanel, buttonColor, fontColor;
		colorpanel = RGPTUtil.getColVal(RGPTParams.getVal("PANEL_COLOR"), ":");
		buttonColor = RGPTUtil
				.getColVal(RGPTParams.getVal("BUTTON_COLOR"), ":");
		fontColor = RGPTUtil.getColVal(RGPTParams.getVal("FONT_COLOR"), ":");
		RGPTLogger.logToConsole("Test2");
		RGPTTemplateMaker.m_PanelColor = colorpanel;
		RGPTTemplateMaker.m_ButtonColor = buttonColor;
		RGPTTemplateMaker.m_FontColor = fontColor;
		RGPTTemplateMaker myRGPTTemplateMaker = RGPTTemplateMaker.getInstance();
		RGPTParams
				.createServerProperties(myRGPTTemplateMaker.getClass(), false);
		myRGPTTemplateMaker.initPDFView();
	}
	// END FUNCTION MAIN

}
