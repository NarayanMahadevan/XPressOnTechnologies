// RGPT PACKAGES
package com.rgpt.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.rgpt.imageutil.ImageUtils;
import com.rgpt.layoututil.BasicGridLayout;
import com.rgpt.serverhandler.PDFPageHandler;
import com.rgpt.util.CursorController;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTUIManager;
import com.rgpt.util.ScalingFactor;
import com.rgpt.util.ThreadInvokerMethod;

public class AdvancedPDFViewer extends PDFViewer implements ActionListener,
		ThreadInvokerMethod {
	private static final long serialVersionUID = 1L;

	// JLabels displaying the Bookmarks of each PDFPage
	JLabel[] m_PDFPageBookmarkViewer;

	// This are the buttons in the ToolBar for quick action that go
	// into the middlepanel
	JButton m_EditPDFButton, m_ResizeMoveButton, m_ZoomInButton,
			m_ZoomOutButton, m_PDFProofButton, m_PDFApproveButton,
			m_EBookViewButton, m_ClosePDFViewButton;

	// This are buttons to delete, zoomin and zoomout and further to undo and
	// redo.
	// These buttons are part of Basic Edit Tab
	JButton m_TabEditPDFButton, m_TabResizeMoveButton, m_TabDeleteFieldButton,
			m_TabLeftAllignButton, m_TabCenterAllignButton,
			m_TabRightAllignButton, m_TabSelectFieldButton,
			m_TabSavePDFWorkButton;

	// Page Navigation Components
	JButton m_FirstPDFPageButton, m_PrevPDFPageButton, m_NextPDFPageButton,
			m_LastPDFPageButton;
	JTextField m_PageNumField;

	JComboBox m_PageSclaingFactor, m_PageAllignmentChoiceBox, m_ZoomUIList;

	BufferedImage[] m_PageAllignmentImages;
	String[] m_PageAlignment = { PDFPageHandler.ALLIGN_PAGE_LEFT,
			PDFPageHandler.ALLIGN_PAGE_CENTER, PDFPageHandler.ALLIGN_PAGE_RIGHT };

	protected AdvancedPDFViewer(PDFPageHandler pdfPgHdlr) {
		super(pdfPgHdlr, PDFPageHandler.THUMBVIEW_IMAGE_PANE, null);
		RGPTLogger.logToFile("In Constructor of: " + this.getClass().getName());
		this.createPDFViewUI();
		this.revalidate();
		this.setVisible(true);
		final BufferedImage progBarImg = getPDFViewerImage(PROGRESS_BAR_IMAGE);
		m_PDFPageHandler.setProgBarImage(progBarImg);
		pdfPgHdlr.reapintViewer();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				initPageHandler(PDFPageHandler.THUMBVIEW_IMAGE_PANE, null,
						progBarImg);
			}
		});
	}

	private void createPDFViewUI() {
		// Settting the Layout of PDFView to BorderLayout
		RGPTLogger.logToFile("Creating PDF View UI: "
				+ this.getClass().getName());
		this.setLayout(new BorderLayout());

		// Creating Upper, Middle and Lower Panel
		JPanel middlePanel = new JPanel();
		// JPanel lowerPanel = new JPanel();

		// Setting the Layout and populating the Upper Panel
		this.populateTabPane();

		// middlePanel.setLayout(new BorderLayout());
		// this.populateMiddlePanel(middlePanel);

		// populateLowerPanel(lowerPanel);

		// Finally Adding to the PDFView Panel
		// this.add(upperPanel, BorderLayout.NORTH);
		// this.add(middlePanel, BorderLayout.CENTER);
		// this.add(lowerPanel, BorderLayout.SOUTH);
	}

	protected void activatePDFViewer() {
		// Enabling or Disabling the Button
		m_PDFPageHandler.setButtonActivation();
	}

	// Populating the Upper Panel with the Tabbed Pane. Tabbed Pane provides
	// advance options to edit and manipulate PDF.
	JTabbedPane m_AdvPDFViewTab;
	final static int ADV_EDIT_TAB = 0;
	final static int EBOOK_VIEW_TAB = 1;

	public void populateTabPane() {
		// Creating Tab Pane for Editing the PDF Page
		m_AdvPDFViewTab = new JTabbedPane();
		m_AdvPDFViewTab.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		m_AdvPDFViewTab.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				System.out.println("Event Object: " + e.toString());
				System.out.println("Source Object: "
						+ e.getSource().getClass().getName());
				System.out.println("Component Class: "
						+ m_AdvPDFViewTab.getSelectedComponent().getClass()
								.getName());
				System.out.println("Selected Index: "
						+ m_AdvPDFViewTab.getSelectedIndex());
				System.out.println("Title: "
						+ m_AdvPDFViewTab.getTitleAt(m_AdvPDFViewTab
								.getSelectedIndex()));
				if (m_AdvPDFViewTab.getSelectedIndex() == ADV_EDIT_TAB) {
					m_PDFPageHandler.editPDF();
				}
				if (m_AdvPDFViewTab.getSelectedIndex() == EBOOK_VIEW_TAB) {
					JPanel ebookPanel = (JPanel) m_AdvPDFViewTab
							.getSelectedComponent();
					m_PDFPageHandler.showEBookView(ebookPanel, false);
				}
			}
		});

		// Creating Edit Panel to edit the PDF and populating the main
		JPanel advEditPanel = new JPanel();
		advEditPanel.setLayout(new BorderLayout());
		this.populateMiddlePanel(advEditPanel);
		// m_AdvPDFViewTab.add(advEditPanel, " Advance Edit ", 0);
		m_AdvPDFViewTab.add(advEditPanel, " Advance Edit ", ADV_EDIT_TAB);

		// Adding EBook Preview to the Tab Pane
		JPanel eBookPanel = new JPanel();

		eBookPanel.addComponentListener(new ComponentListener() {
			// Invoked when the component has been made invisible.
			public void componentHidden(ComponentEvent e) {
				m_AdvPDFViewTab.setSelectedIndex(ADV_EDIT_TAB);
			}

			// Invoked when the component's position changes.
			public void componentMoved(ComponentEvent e) {
			}

			// Invoked when the component's size changes.
			public void componentResized(ComponentEvent e) {
			}

			// Invoked when the component has been made visible.
			public void componentShown(ComponentEvent e) {
			}
		});
		eBookPanel.setLayout(new BorderLayout());
		m_AdvPDFViewTab.add(eBookPanel, " EBook Preview ", EBOOK_VIEW_TAB);

		// Adding the Tab
		// this.add(m_AdvPDFViewTab);
		this.add(advEditPanel);

		// JPanel middlePanel = new JPanel();
	}

	// This method creates the Middlepanel of this component
	public void populateMiddlePanel(JPanel middlePanel) {
		JPanel activeTabPanel = new JPanel();
		activeTabPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// Making Tool Bar to Scale, Navigat Page, Edit and Generate PDF
		JToolBar scaleTbar = new JToolBar();
		scaleTbar.setFloatable(false);

		String[] scaleFactors = ScalingFactor.getScalingFactor();
		m_PageSclaingFactor = new JComboBox(scaleFactors);
		m_PageSclaingFactor.setSelectedIndex(0);
		m_PageSclaingFactor.addActionListener(this);
		scaleTbar.add(m_PageSclaingFactor);

		// createButton method by default does not register the button with
		// PDFPageHandler and register this object as Action Listener

		// Creting UI Components for Zoom In and Zoom Out of PDF Document
		m_ZoomUIList = new JComboBox(
				ScalingFactor.ZOOM_IN_OUT.getZoomValueList());
		m_ZoomUIList.setSelectedIndex(0);
		m_ZoomUIList.setEditable(false);
		m_ZoomUIList.addActionListener(this);
		m_PDFPageHandler.regPDFActionComponent(PDFPageHandler.ZOOM_PDF_BOX,
				m_ZoomUIList);
		m_ZoomInButton = createButton("zoomin", " Zoom Out ", scaleTbar);
		// scaleTbar.add(m_ZoomUIList);
		m_ZoomOutButton = createButton("zoomout", " Zoom In ", scaleTbar);

		// Page Navigation Buttons
		JToolBar pgNavTbar = new JToolBar();
		pgNavTbar.setFloatable(false);
		this.createPageNavigationPanel(pgNavTbar);

		// Edit and Generate PDF Buttons
		JToolBar tbar = new JToolBar();
		tbar.setFloatable(false);
		m_EditPDFButton = createButton("editpdf", " Edit PDF ", tbar,
				PDFPageHandler.EDIT_PDF);
		// m_ResizeMoveButton = createButton("resizeVDP",
		// " Resize and Move Field ",
		// tbar, PDFPageHandler.RESIZE_MOVE_PDF);
		m_EBookViewButton = createButton("ebookview", " Print Preview ", tbar);
		boolean regAction = false;
		m_PDFProofButton = createButton("pdfproof", " View PDF Proof ", tbar,
				PDFPageHandler.PDF_PROOF, regAction);
		m_PDFApproveButton = createButton("approvePDF", " Approve PDF Proof ",
				tbar, PDFPageHandler.APPROVE_PDF);
		m_ClosePDFViewButton = createButton("closePDF", "Close PDF Viewer",
				tbar);

		ActionListener genPDFAction = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				m_PDFPageHandler.showPDFProof();
			}
		};
		ActionListener setCursorAction = null;
		setCursorAction = CursorController.createListener(this, genPDFAction,
				CursorController.BUSY_CURSOR);
		m_PDFProofButton.addActionListener(setCursorAction);

		activeTabPanel.add(scaleTbar);
		// activeTabPanel.add(pgNavTbar);
		activeTabPanel.add(tbar);
		middlePanel.add(activeTabPanel, BorderLayout.NORTH);

		JPanel leftpane = new JPanel();
		this.populateLeftPanel(leftpane);
		/*
		 * HashMap requestData = new HashMap(); requestData.put("RequestType",
		 * "PopulateLeftPanel"); requestData.put("LeftPanel", leftpane);
		 * RGPTThreadWorker threadWorker = null; threadWorker = new
		 * RGPTThreadWorker(Thread.MIN_PRIORITY, this, requestData);
		 * threadWorker.startThreadInvocation();
		 */
		JScrollPane lscroll = null;
		lscroll = new JScrollPane(leftpane,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		lscroll.setPreferredSize(new Dimension(70, 0));
		JPanel rightpane = m_PDFPageHandler.createContentPane();
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				lscroll, rightpane);

		// JPanel contentPane = m_PDFPageHandler.createContentPane();
		middlePanel.add(splitPane, BorderLayout.CENTER);
	}

	// Processing the Thread Request to load Font Streams
	public void processThreadRequest(HashMap requestData) throws Exception {
		int pgNum = -1;
		String reqType = (String) requestData.get("RequestType");
		if (reqType.equals("PopulateLeftPanel")) {
			JPanel leftpane = (JPanel) requestData.get("LeftPanel");
			this.populateLeftPanel(leftpane);
		}
		this.repaint();
	}

	private void createPageNavigationPanel(JToolBar tbar) {
		// This is to go to First Page
		m_FirstPDFPageButton = createButton("first", " First Page ", tbar);
		m_PrevPDFPageButton = createButton("prev", " Previous Page ", tbar);
		JLabel label = new JLabel(" Page ");
		tbar.add(label);
		m_PageNumField = new JTextField();
		int currPage = m_PDFPageHandler.getPDFPageNum();
		m_PageNumField.setText((new Integer(currPage)).toString());
		m_PageNumField.setColumns(3);
		m_PDFPageHandler.regPDFActionComponent(PDFPageHandler.PDF_PAGE_FIELD,
				m_PageNumField);
		tbar.add(m_PageNumField);
		label = new JLabel(" of " + m_PDFPageHandler.getPageCount() + " ");
		tbar.add(label);
		m_NextPDFPageButton = createButton("next", " Next Page ", tbar);
		m_LastPDFPageButton = createButton("last", " Last Page ", tbar);
	}

	JPanel m_PDFPageThumbPane;

	// Creating the Lower Panel which display the Bookmarks od PDF Page
	public void populateLeftPanel(JPanel leftPanel) {
		m_PDFPageThumbPane = new JPanel(
				new BasicGridLayout(0, 1, 0, 10, 10, 10));
		// leftPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		leftPanel.setLayout(new BorderLayout());
		// leftPanel.add(new JLabel(), BorderLayout.NORTH);
		leftPanel.add(m_PDFPageThumbPane, BorderLayout.CENTER);
		// leftPanel.add(m_PDFPageThumbPane);
		if (m_PDFPageBookmarkViewer == null)
			this.handlePDFPageBookmarks();
	}

	protected void updatePDFThumbPage(int pgNum, BufferedImage pdfThumbPg) {
		super.updatePDFThumbPage(pgNum, pdfThumbPg);
		BufferedImage buf = (BufferedImage) m_PDFPageBookmarkImages.get(pgNum);
		if (buf == null)
			return;
		m_PDFPageBookmarkViewer[pgNum - 1].setIcon(new ImageIcon(buf));
	}

	protected void handlePDFPageBookmarks() {
		if (m_PDFPageThumbPane == null || m_PDFPageBookmarkImages == null)
			return;
		// Retrieves PDF Page Bookmarks from PDFPageHandler and display at the
		// Lower Panel
		int numberofpage = m_PDFPageBookmarkImages.size();
		System.out.println("#####** ENTERED populateLeftPanel. Size is **"
				+ numberofpage);

		// Creating JLabel for every
		m_PDFPageBookmarkViewer = new JLabel[numberofpage];
		for (int i = 0; i < numberofpage; i++) {
			int hgap = 5;
			if (i % 2 == 0)
				hgap = 20;
			// lowerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, hgap, 10));
			m_PDFPageBookmarkViewer[i] = new JLabel();
			Border borderline = BorderFactory.createLineBorder(Color.BLACK);
			m_PDFPageBookmarkViewer[i].setBorder(borderline);
			m_PDFPageThumbPane.add(m_PDFPageBookmarkViewer[i]);
			int pageNum = i + 1;
			System.out.println("pagenum :- " + pageNum);
			BufferedImage origbuf = (BufferedImage) m_PDFPageBookmarkImages
					.get(pageNum);
			if (origbuf == null)
				System.out.println(" PDF Page Bookmark is null ");
			BufferedImage buf = ImageUtils.scaleImage(origbuf, THUMBVIEW_WIDTH,
					THUMBVIEW_HEIGHT, true);

			m_PDFPageBookmarkViewer[i].setIcon(new ImageIcon(buf));
		}
		// By default displaying the first PDF Page
		displayPDFPage(0);
		m_PDFPageThumbPane.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				System.out.println("#####** ENTERED mouseClicked at pages **");
				int x = me.getX();
				int y = me.getY();
				System.out.println("mouse Clicked in innerpanel at " + x + ","
						+ y);
				int numberofpage = m_PDFPageBookmarkImages.size();
				for (int i = 0; i < numberofpage; i++) {
					if (m_PDFPageBookmarkViewer[i].getBounds().contains(x, y)) {
						System.out
								.println("*****************************************");
						System.out.println("pagelabel[" + i
								+ "] contains mouse clicked...");
						System.out
								.println("*****************************************");
						displayPDFPage(i);
					}
				}
			}
		});
	}

	private JButton createButton(String imageName, String toolTipText,
			JToolBar comp, int actionId) {
		boolean regAction = true;
		return this.createButton(imageName, toolTipText, comp, actionId,
				regAction);
	}

	private JButton createButton(String imageName, String toolTipText,
			JToolBar comp) {
		int actionId = -1;
		boolean regAction = true;
		return this.createButton(imageName, toolTipText, comp, actionId,
				regAction);
	}

	private JButton createButton(String imageName, String toolTipText,
			JToolBar comp, int actionId, boolean regAction) {
		ImageIcon imgIcon = null;
		// Loading the image.
		String imgLocation = PDFPageHandler.IMAGE_PATH + imageName + ".gif";
		System.out.println("Load Image Direct: " + imgLocation);

		// Create and initialize the button.
		JButton button = new JButton();
		button.setToolTipText(toolTipText);
		boolean useStream = false;
		try {
			imgIcon = new ImageIcon(imgLocation);
			System.out.println("Direct ImageIcon : " + imgIcon.toString());
			System.out.println("Direct Image : "
					+ imgIcon.getImage().toString());
			System.out.println("Image Size W: " + imgIcon.getIconWidth()
					+ " H: " + imgIcon.getIconHeight());
			if (imgIcon.getIconWidth() <= 0 || imgIcon.getIconHeight() <= 0)
				useStream = true;
		} catch (Throwable th) {
			useStream = true;
		}
		if (useStream) {
			String imgPath = "/" + PDFPageHandler.IMAGE_PATH + imageName
					+ ".gif";
			byte[] buf = ImageUtils.loadImage(imgPath, this.getClass());
			try {
				imgIcon = new ImageIcon(java.awt.Toolkit.getDefaultToolkit()
						.createImage(buf));
			} catch (Exception ex) {
				ex.printStackTrace();
				button.setLabel(imageName);
			}
		}
		if (imgIcon != null)
			button.setIcon(imgIcon);
		else
			button.setName(imageName);

		// Add Action Listener and add the button to toolbar
		if (regAction)
			button.addActionListener(this);
		if (actionId != -1)
			m_PDFPageHandler.regPDFActionComponent(actionId, button);
		comp.add(button);
		return button;
	}

	// TODO DisplayPages
	public void displayPDFPage(int highlightPage) {
		System.out.println("** ENTERED DisplayPages -1-** " + highlightPage);
		Border redborderline = BorderFactory.createLineBorder(Color.RED);
		Border blackborderline = BorderFactory.createLineBorder(Color.BLACK);
		for (int i = 0; i < m_PDFPageBookmarkImages.size(); i++) {
			if (i != highlightPage) {
				m_PDFPageBookmarkViewer[i].setBorder(blackborderline);
				continue;
			}
			int pageNum = i + 1;
			m_PDFPageHandler.displayPDFPage(pageNum);
			m_PDFPageBookmarkViewer[i].setBorder(redborderline);
		}
		System.out.println("** EXITED DisplayPages -LAST-**");
	}

	// This creates BasicEditTab which enables Basic Edits on PDF
	public JPanel createAdvanceEditTab() {
		JPanel editTabPanel = new JPanel();
		editTabPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		// Making Tool Bar for Edit, Resize and Delete
		JToolBar editTbar = new JToolBar();
		editTbar.setFloatable(false);
		// m_TabEditPDFButton = createButton("editpdf", " Edit PDF ", editTbar);
		// m_TabResizeMoveButton =
		// createButton("resizeVDP", " Resize and Move Field ", editTbar);
		m_TabSelectFieldButton = createButton("selectVDP", " Select Field ",
				editTbar, PDFPageHandler.SELECT_PDF);
		m_TabDeleteFieldButton = createButton("deleteVDP", " Delete Field ",
				editTbar, PDFPageHandler.DELETE_PDF);
		m_TabSavePDFWorkButton = createButton("savePDF", "Save Work", editTbar,
				PDFPageHandler.SAVE_PDF_WORK);
		editTabPanel.add(editTbar);

		// Making Tool Bar for Allignment along BBox
		JToolBar allignBoxTbar = new JToolBar();
		allignBoxTbar.setFloatable(false);
		JLabel allignBoxLabel = new JLabel(" Allign with in Box ");
		allignBoxTbar.add(allignBoxLabel);
		m_TabLeftAllignButton = createButton("leftAllign", " Left Allign PDF ",
				allignBoxTbar);
		m_TabCenterAllignButton = createButton("centerAllign",
				" Center Allign PDF ", allignBoxTbar);
		m_TabRightAllignButton = createButton("rightAllign",
				" Right Allign PDF ", allignBoxTbar);
		editTabPanel.add(allignBoxTbar);

		// Making Tool Bar for Allignment along Page
		JToolBar allignPageTbar = new JToolBar();
		allignPageTbar.setFloatable(false);
		JLabel allignPageLabel = new JLabel(" Allign with in Page ");
		allignPageTbar.add(allignPageLabel);

		String[] pageAllign = { "pageleftallign", "pagecenterallign",
				"pagerightallign" };
		m_PageAllignmentImages = new BufferedImage[pageAllign.length];
		Integer[] intArray = new Integer[pageAllign.length];
		for (int i = 0; i < pageAllign.length; i++) {
			intArray[i] = new Integer(i);
			try {
				String imgLocation = PDFPageHandler.IMAGE_PATH + pageAllign[i]
						+ ".gif";
				m_PageAllignmentImages[i] = ImageUtils.getBufferedImage(
						imgLocation, this.getClass());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		m_PageAllignmentChoiceBox = new JComboBox(intArray);
		ComboBoxRenderer renderer = new ComboBoxRenderer();
		m_PageAllignmentChoiceBox.setRenderer(renderer);

		m_PageAllignmentChoiceBox.setSelectedIndex(0);
		allignPageTbar.add(m_PageAllignmentChoiceBox);
		editTabPanel.add(allignPageTbar);

		return editTabPanel;
	}

	// TODO createAddImagesTab
	private JPanel createAddImagesTab() {

		JPanel imagepanel = new JPanel();
		imagepanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		return imagepanel;
	}

	// TODO createImagePersTab
	public JPanel createImagePersTab() {

		JPanel imgperspanel = new JPanel();
		imgperspanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		return imgperspanel;
	}

	class ComboBoxRenderer extends JLabel implements ListCellRenderer {
		public ComboBoxRenderer() {
			setOpaque(true);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
		}

		// * This method finds the image and text corresponding to the selected
		// * value and returns the label, set up to display the text and image.

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			// Get the selected index. (The index param isn't
			// always valid, so just use the value.)
			System.out.println("value :- " + value);
			System.out.println("index :- " + index);
			int selectedIndex = ((Integer) value).intValue();

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			// Set the icon and text. If icon was null, say so.
			ImageIcon icon = new ImageIcon(
					m_PageAllignmentImages[selectedIndex]);
			String pgAllign = m_PageAlignment[selectedIndex];
			setIcon(icon);
			if (icon != null) {
				setText(pgAllign);
				setFont(list.getFont());
			}
			return this;
		}
	}

	// TODO main

	public static void main(String[] args) {
		RGPTUIManager.setLookAndFeel();
		/*
		 * //lightcolor for panel Color colorpanel = new Color(65, 160, 255);
		 * //darkercolor for button Color colorbg = new Color(153, 255, 255);
		 * //darkestcolor for text Color colordark = new Color(214, 255,255);
		 */
		/*
		 * //lightcolor for panel Color colorpanel = new Color(51,255, 153 );
		 * //darkercolor for button Color colorbg = new Color(153, 255, 255);
		 * //darkestcolor for text Color colordark = new Color(214, 255,255);
		 */

		// lightcolor for panel
		Color colorpanel = new Color(204, 204, 255);
		// darkercolor for button
		Color colorbg = new Color(153, 153, 255);
		// darkestcolor for text
		Color colordark = new Color(214, 255, 255);

		/*
		 * Color colorpanel =null ,colorbg = null , colordark = null;
		 * 
		 * //lightcolor for panel if(args[0] != null) { int colval =
		 * Integer.parseInt(args[0], 16); colorpanel = new Color(colval); }
		 * //darkercolor for button if(args[1] != null) { int colval =
		 * Integer.parseInt(args[1], 16); colorbg = new Color(colval); }
		 * 
		 * //darkestcolor for text if(args[2] != null) { int colval =
		 * Integer.parseInt(args[2], 16);
		 * 
		 * colordark = new Color(colval); }
		 */

		RGPTUIManager.setUIDefaults(colorpanel, colorbg, colordark);
		/*
		 * PDFPageViewer pdfPgViewer = new PDFPageViewer(); PDFViewer ui = new
		 * PDFViewer(pdfPgViewer); JFrame frame = new JFrame(); Container cp =
		 * frame.getContentPane(); cp.add(ui); frame.setSize(800, 600);
		 * frame.setTitle("PDF Personalization Viewer"); frame.setVisible(true);
		 * frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 */
	}

	// This actionPerformed method is invoked when the any Action is taken
	// on this component
	public void actionPerformed(ActionEvent e) {
		Object eObj = e.getSource();
		if (eObj == m_TabSelectFieldButton)
			m_PDFPageHandler.selectPDF();
		else if (eObj == m_EditPDFButton || eObj == m_TabEditPDFButton)
			m_PDFPageHandler.editPDF();
		else if (eObj == m_ResizeMoveButton || eObj == m_TabResizeMoveButton)
			m_PDFPageHandler.resizePDF();
		else if (eObj == m_TabDeleteFieldButton)
			m_PDFPageHandler.deleteVDPField();
		else if (eObj == m_ZoomInButton)
			m_PDFPageHandler.zoomInPDFPage();
		else if (eObj == m_ZoomOutButton)
			m_PDFPageHandler.zoomOutPDFPage();
		else if (eObj == m_EBookViewButton) {
			if (m_PDFPageHandler.isPDFProofGenerated())
				m_AdvPDFViewTab.setSelectedIndex(EBOOK_VIEW_TAB);
			else
				m_PDFPageHandler.showEBookView(null, true);
		} else if (eObj == m_TabSavePDFWorkButton)
			m_PDFPageHandler.savePDFWork();
		else if (eObj == m_PDFApproveButton)
			m_PDFPageHandler.approvePDF();
		else if (eObj == m_ClosePDFViewButton)
			m_PDFPageHandler.closePDFViwer();
		else if (eObj == m_FirstPDFPageButton)
			m_PDFPageHandler.displayFirstPDFPage();
		else if (eObj == m_PrevPDFPageButton)
			m_PDFPageHandler.displayPrevPDFPage();
		else if (eObj == m_NextPDFPageButton)
			m_PDFPageHandler.displayNextPDFPage();
		else if (eObj == m_LastPDFPageButton)
			m_PDFPageHandler.displayLastPDFPage();
		else if (eObj == m_TabLeftAllignButton)
			m_PDFPageHandler.allignWRTBBox(PDFPageHandler.ALLIGN_LEFT);
		else if (eObj == m_TabCenterAllignButton)
			m_PDFPageHandler.allignWRTBBox(PDFPageHandler.ALLIGN_CENTER);
		else if (eObj == m_TabRightAllignButton)
			m_PDFPageHandler.allignWRTBBox(PDFPageHandler.ALLIGN_RIGHT);
		else if (eObj == m_PageNumField) {
			int pageNum = (new Integer(e.getActionCommand())).intValue();
			m_PDFPageHandler.displayPDFPage(pageNum);
		} else if (eObj == m_PageSclaingFactor) {
			String value = (String) ((JComboBox) e.getSource())
					.getSelectedItem();
			m_PDFPageHandler.scalePDFPage(value);
		} else if (eObj == m_PageAllignmentChoiceBox) {
			String value = (String) ((JComboBox) eObj).getSelectedItem();
			m_PDFPageHandler.allignWRTPage(value);
		} else if (eObj == m_ZoomUIList) {
			String value = (String) ((JComboBox) eObj).getSelectedItem();
			int zoomValue = new Integer(value).intValue();
			m_PDFPageHandler.setPDFZoomValue(zoomValue);
		}
	}
}
