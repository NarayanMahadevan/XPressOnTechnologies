// RGPT PACKAGES
package com.rgpt.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.rgpt.imageutil.ImageUtils;
import com.rgpt.serverhandler.ApprovalHandler;
import com.rgpt.serverhandler.EBookPageHandler;
import com.rgpt.util.AppletParameters;
import com.rgpt.util.RGPTThreadWorker;
import com.rgpt.util.RGPTUIManager;
import com.rgpt.util.ScalingFactor;
import com.rgpt.util.ThreadInvokerMethod;

class PDFPageStatus {
	int m_PageNum;
	boolean m_PageLoadStatus;
}

public class EBookViewer extends JPanel implements ComponentListener,
		EBookInterface, ApprovalHandler, ThreadInvokerMethod {
	// This is to set the Flip Direction. Will be used in Future. Where in the
	// Document/Asset will provide information of the Direction of the Flip
	public final static int NO_FLIP = 0;
	public final static int FLIP_VERTICLE_AXIS = 1;
	public final static int FLIP_HORIZONTAL_AXIS = 2;

	// Currently either it is no Flip or Flip along Verticle Axis.
	public int m_FlipMode;

	// Digital Asset currently being Displayed
	int m_DigitalAssetId;

	// Current Page Number. This is is always the right hand page. Set initally
	// to arbitary value -1, indicating not yet assigned.
	int m_CurrentPage = -1;

	// Number of Pages to be displayed in the Document
	int m_NumberOfPages;

	// This maintains HashMap of PDF Page to the corresponding PDF Page.
	HashMap m_PDFPages;

	// This maintains Loading status for every PDF Page
	Vector m_PDFPageLoadStatus;

	// This holds HashMap of Binding Options to corresponding Image like
	// Stapling,
	// Spiral Binding, etc
	HashMap m_BindingOptions;

	// List of Page numbers to be displayed. The maximum number will be Left,
	// Right, next Left, next Right, prev Left, and prev Right. This will be
	// stored
	// in the HashMap
	HashMap m_DisplayPDFPages;

	// This maintains Image Width & Height and Screen Width & Height
	public int m_PDFPageWidth, m_PDFPageHeight;
	public int m_ScreenWidth, m_ScreenHeight;

	// Start Position for Left and Right Pages.
	public int m_StartXRight = 0, m_StartXLeft = 0;
	public int m_StartY = 0;

	// Polygone Shape for various Theta Positions as the Page is turned
	Polygon m_DisplayPolygon;

	// This speeds indicates the speed of turning the Pages, mesured in
	// milliseconds
	int m_AnimationSpeed = 10; // delay in milliseconds

	// This indicates the number of steps needed to turn the Pages
	int m_AnimationStep = 16;

	// Angle in radians of the next display step
	double m_Theta = 0;

	// The PDF Page is stored in the Display Buffer and the Graphic draws the
	// Display Buffer.
	Image m_DisplayBuffer;

	// Display Content Panel
	java.awt.Container m_DisplayContentPanel;

	// This is used to retrieve the Pages to be displayed in the EBookViewer
	EBookPageHandler m_EBookPageHandler;

	// Container having this Panel as a Content Pane
	Container m_PDFViewContainer;

	// PDFViewInterface to get PDF Page as Image
	// PDFViewInterface m_PDFView;

	// Width to be assigned for Binding
	static int m_BindingWidth = 40;

	// This variable is set if the Page has to be re-drawn;
	private boolean m_PageRedraw;

	// This value is to Enable or Disable Animation
	private boolean m_EnableAnimation = true;

	// This value is to Enable or Disable PDF Approval. If set to true, by
	// default
	// all the Pages of the PDF Document will be Approved, the user can then
	// select pages which are not approved.
	private boolean m_EnablePDFApproval = false;

	// This variable maintains approved or rejected information of every page in
	// the document.
	private HashMap m_PageApprovalStatus;

	public static boolean m_UseAffine4Display = false;

	// This is populated as soon as the First Page is populated. This Width and
	// Height is used for future Animation
	public int m_DisplayPageWidth = 0;
	public int m_DisplayPageHeight = 0;

	public static boolean m_IsEBookViewSet = false;

	public EBookViewer() {
		m_IsEBookViewSet = false;
	}

	public EBookViewer(int assetId, int numOfPages, Container parentPane,
			EBookPageHandler pageHdlr, boolean enablePgAnim, int animSpeed,
			int animStep, boolean enablePDFAppr) {
		System.out.println("Invoked EBookViewer for Asset Id: " + assetId
				+ " With Num of Pages: " + numOfPages);
		this.setEBookUIControls(parentPane, pageHdlr, assetId, numOfPages,
				enablePDFAppr, enablePgAnim, animSpeed, animStep);
	}

	// This method is to set EBook UI Control and register the EBookInterface
	// for
	// Page retrivals
	public void setEBookUIControls(Container parentPane,
			EBookPageHandler eBookHdlr, int assetId, int numPages,
			boolean enablePDFAppr, boolean enablePgAnim, int animSpeed,
			int animStep) {
		m_IsEBookViewSet = true;
		m_DigitalAssetId = assetId;
		m_NumberOfPages = numPages;
		m_AnimationStep = animStep;
		m_AnimationSpeed = animSpeed;
		m_EnableAnimation = enablePgAnim;
		m_EnablePDFApproval = enablePDFAppr;
		m_EBookPageHandler = eBookHdlr;
		m_PDFViewContainer = parentPane;

		m_PDFPageLoadStatus = new Vector();
		// for (int i = 0; i < m_NumberOfPages; i++){
		// m_PDFPageLoadStatus.put(i+1, false);

		// Setting the default approved status for every page
		if (m_EnablePDFApproval) {
			m_PageApprovalStatus = new HashMap();
			for (int i = 0; i < m_NumberOfPages; i++)
				m_PageApprovalStatus.put(i + 1, true);
		}

		addComponentListener(this);
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int x = e.getX();
				if (x > (int) m_RHSStartPtX)
					nextPage();
				else
					prevPage();
				if (m_EnablePDFApproval)
					setApprovalButton();
			}// end mouseClicked
		});

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				HashMap requestData = new HashMap();
				int pgNum = -1;
				int pgCounter = 0;
				// This setting is used to set the number of page to be
				// downloaded every
				// time from server
				Vector pdfPages = new Vector();
				pdfPages.addElement(1);
				int numOfPageDownload = 2;
				getNonCachedPages(pdfPages, numOfPageDownload);
			}
		});
	}

	public void resetEBookCache() {
		if (m_PDFPages != null)
			m_PDFPages.clear();
	}

	public boolean isEBookSet() {
		return m_IsEBookViewSet;
	}

	public void setVisibility(boolean isVisible) {
		this.setVisible(isVisible);
	}

	public void processThreadRequest(HashMap requestData) throws Exception {
		String reqType = (String) requestData.get("RequestType");
		System.out.println("In processThreadRequest for:  " + reqType);
		if (reqType.equals("GetPages")) {
			Vector pdfPages = (Vector) requestData.get("Pages");
			getNonCachedPages(pdfPages);
		}
	}

	AppletParameters m_AppletParameters = null;

	public void setAppletParameters(AppletParameters params) {
		m_AppletParameters = params;
	}

	private void closeEBookViewer() {
		if (m_PageApprovalStatus == null)
			return;
		m_EBookPageHandler.setPDFBatchApproval(m_PageApprovalStatus);
	}

	private String getApprovalMesg(boolean showMesg) {
		if (m_PageApprovalStatus == null || m_AppletParameters == null)
			return "";
		System.out
				.println("Page Approvals: " + m_PageApprovalStatus.toString());
		AppletParameters params = m_AppletParameters;
		HashMap pdfDocApprStatus = m_EBookPageHandler.getPDFDocApprovals(
				params.getIntVal("page_count"),
				params.getIntVal("template_page_count"), m_PageApprovalStatus);
		System.out.println("PDF Doc Approvals: " + pdfDocApprStatus.toString());
		StringBuffer mesg = new StringBuffer();
		boolean isAllApproved = true;
		boolean isPDFApproved = true;
		for (int i = 0; i < pdfDocApprStatus.size(); i++) {
			int pdfDocCounter = i + 1;
			isPDFApproved = ((Boolean) pdfDocApprStatus.get(pdfDocCounter))
					.booleanValue();
			if (isPDFApproved)
				continue;
			isAllApproved = false;
			mesg.append(" Row # " + pdfDocCounter);
		}
		String finalMesg = "";
		if (isAllApproved)
			finalMesg = "All Rows Approved";
		else
			finalMesg = "Rejected Rows: " + mesg.toString();
		System.out.println("Final Approval Desc: " + finalMesg);
		if (showMesg)
			this.showMessage(finalMesg, "Rows Rejected");
		return finalMesg;
	}

	public void showMessage(String mesg, String title) {
		JOptionPane.showMessageDialog(m_PDFViewContainer, mesg, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	// Invoked when the component has been made invisible.
	public void componentHidden(ComponentEvent e) {
	}

	// Invoked when the component's position changes.
	public void componentMoved(ComponentEvent e) {
	}

	// Invoked when the component's size changes.
	public void componentResized(ComponentEvent e) {
		System.out.println("Windows Resized");
		createDisplayParameters();
		m_PageRedraw = true;
		repaint();
	}

	// Invoked when the component has been made visible.
	public void componentShown(ComponentEvent e) {
	}

	public void populateContentPane(JPanel contentPanel) {
		System.out.println("In populateContentPane");
		// Defining the South Page Navigation Panel
		int hgap = 10;
		int vgap = 0;
		// JToolBar pageNavPanel = new JToolBar(JToolBar.HORIZONTAL);
		// pageNavPanel.setFloatable(false);
		// pageNavPanel.setAlignmentX(JToolBar.CENTER_ALIGNMENT);
		// pageNavPanel.setAlignmentY(JToolBar.CENTER_ALIGNMENT);
		JPanel southPanel = new JPanel(new BorderLayout());
		JPanel pageNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,
				hgap, vgap));
		m_PageNavPanel = pageNavPanel;
		this.createSouthUI(pageNavPanel);
		// southPanel.add(pageNavPanel, BorderLayout.CENTER);
		// southPanel.add(pgApprPanel, BorderLayout.EAST);
		contentPanel.add(pageNavPanel, BorderLayout.SOUTH);

		// Adding Scroll Pane to the PDFPage Content and laying it in the Center
		// JScrollPane imageScroller = new JScrollPane(this,
		// JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		// JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		setBorder(BorderFactory.createLineBorder(Color.black));
		contentPanel.add(this, BorderLayout.CENTER);
	}

	JPanel m_PageNavPanel;
	JTextField m_PageNumField;

	private void createSouthUI(JPanel pageNavPanel) {
		// Approval Panel for RHS Page
		if (m_EnablePDFApproval) {
			JPanel pgApprPanel = this.createPagApprovalPanel(true);
			pageNavPanel.add(pgApprPanel);
		}

		// This is to go to First Page
		JButton button = null;
		if (m_NumberOfPages > 1) {
			button = makeNavigationButton("first", "First Page");
			pageNavPanel.add(button);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					m_LHSPageNum = -1;
					m_RHSPageNum = 1;
					m_PageRedraw = true;
					if (m_EnablePDFApproval)
						setApprovalButton();
					repaint();
				}
			});

			// This is to go to Previous Page
			button = makeNavigationButton("prev", "Previous Page");
			pageNavPanel.add(button);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					prevPage();
					if (m_EnablePDFApproval)
						setApprovalButton();
				}
			});
		}

		// Dimension getPreferredSize()
		// Adding Page Label
		JLabel label = new JLabel("Page");
		pageNavPanel.add(label);
		// Defining text field for user to type the Page Num
		m_PageNumField = new JTextField();
		m_PageNumField.setColumns(4);
		// m_PageNumField.setMaximumSize(new Dimension(40,20));
		pageNavPanel.add(m_PageNumField);
		m_PageNumField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int pageNum = (new Integer(arg0.getActionCommand())).intValue();
				if (pageNum < 1 && pageNum > m_NumberOfPages)
					return;
				// Even Page Number is Displayed in LHS and ODD in RHS
				if (pageNum % 2 == 0) {
					m_LHSPageNum = pageNum;
					m_RHSPageNum = m_LHSPageNum + 1;
				} else {
					m_RHSPageNum = pageNum;
					m_LHSPageNum = m_RHSPageNum - 1;
				}
				m_PageRedraw = true;
				if (m_EnablePDFApproval)
					setApprovalButton();
				repaint();
			}
		});

		label = new JLabel(" of " + m_NumberOfPages);
		pageNavPanel.add(label);

		// This is to go to Next Page
		if (m_NumberOfPages > 1) {
			button = makeNavigationButton("next", "Next Page");
			pageNavPanel.add(button);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					nextPage();
					if (m_EnablePDFApproval)
						setApprovalButton();
				}
			});

			// This is to go to Last Page
			button = makeNavigationButton("last", "Last Page");
			pageNavPanel.add(button);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					// Even Number of Pages
					if (m_NumberOfPages % 2 == 0) {
						m_LHSPageNum = m_NumberOfPages;
						m_RHSPageNum = 0;
					} else {
						m_RHSPageNum = m_NumberOfPages;
						m_LHSPageNum = m_RHSPageNum - 1;
					}
					System.out.println("LHS Pg Num: " + m_LHSPageNum
							+ " RHS Pg Num: " + m_RHSPageNum);
					if (m_EnablePDFApproval)
						setApprovalButton();
					// animateNextPage();
					m_PageRedraw = true;
					repaint();
				}
			});

			// Approval Panel for RHS Page
			if (m_EnablePDFApproval) {
				JPanel pgApprPanel = this.createPagApprovalPanel(false);
				pageNavPanel.add(pgApprPanel);
			}
		}

		button = makeNavigationButton("close", "Close EBook");
		pageNavPanel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (m_EnablePDFApproval) {
					m_ApprovalDialog = null;
					populateApprovalDialog();
					return;
				}
				getApprovalMesg(true);
				closeEBookViewer();
				m_PDFViewContainer.setVisible(false);
				// m_DisplayContentPanel.close();
			}
		});
	}

	ApprovalDialog m_ApprovalDialog;

	private void populateApprovalDialog() {
		String apprMesg = this.getApprovalMesg(false);
		String[] approvalMesgs = new String[3];
		approvalMesgs[0] = "Approve PDF Batch with " + apprMesg;
		approvalMesgs[1] = "Dis-Approve Complete PDF Batch";
		approvalMesgs[2] = "Decide Later";
		m_ApprovalDialog = new ApprovalDialog(approvalMesgs, this);
		m_ApprovalDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		m_ApprovalDialog.setLocation(200, 200);
		m_ApprovalDialog.setVisible(true);
	}

	// This method is invoked to Process PDF Approval
	public void processApproval(int approvalMode) {
		m_ApprovalDialog.setVisible(false);
		if (approvalMode == ApprovalHandler.DICIDE_LATER) {
			m_PageApprovalStatus.clear();
			m_PageApprovalStatus = null;
		} else if (approvalMode == ApprovalHandler.DISAPPROVE_PDF) {
			for (int i = 0; i < m_NumberOfPages; i++)
				m_PageApprovalStatus.put(i + 1, false);
		}
		this.closeEBookViewer();
		m_PDFViewContainer.setVisible(false);
	}

	public void setApprovalButton() {
		// System.out.println("LHS Page Num: " + m_LHSPageNum +
		// " RHS Page Num: " + m_RHSPageNum);
		boolean lhsPage = true, rhsPage = true;

		// LHS Page Approval settings
		Boolean boolVal = ((Boolean) m_PageApprovalStatus.get(m_LHSPageNum));
		if (boolVal != null) {
			lhsPage = boolVal.booleanValue();
			if (lhsPage)
				m_LHSApproveButton.setSelected(true);
			else
				m_LHSRejectButton.setSelected(true);
		}

		// RHS Page Approval settings
		boolVal = ((Boolean) m_PageApprovalStatus.get(m_RHSPageNum));
		if (boolVal != null) {
			rhsPage = boolVal.booleanValue();
			if (rhsPage)
				m_RHSApproveButton.setSelected(true);
			else
				m_RHSRejectButton.setSelected(true);
		}
	}

	// LHS and RHS Approval and Rejection Button
	JRadioButton m_LHSApproveButton, m_RHSApproveButton;
	JRadioButton m_LHSRejectButton, m_RHSRejectButton;

	private JPanel createPagApprovalPanel(final boolean isLHSApprPanel) {
		int allign = -1;
		int hgap = 10;
		int vgap = 0;
		if (isLHSApprPanel)
			allign = FlowLayout.LEFT;
		else
			allign = FlowLayout.RIGHT;
		JPanel pgApprPanel = new JPanel(new FlowLayout(allign, hgap, vgap));
		JRadioButton apprButton = new JRadioButton("Approved");
		apprButton.setSelected(true);
		pgApprPanel.add(apprButton);
		apprButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (isLHSApprPanel)
					m_PageApprovalStatus.put(m_LHSPageNum, true);
				else
					m_PageApprovalStatus.put(m_RHSPageNum, true);
			}
		});

		JRadioButton rejButton = new JRadioButton("Rejected");
		rejButton.setSelected(false);
		pgApprPanel.add(rejButton);
		rejButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (isLHSApprPanel)
					m_PageApprovalStatus.put(m_LHSPageNum, false);
				else
					m_PageApprovalStatus.put(m_RHSPageNum, false);
			}
		});

		// Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(apprButton);
		group.add(rejButton);

		// Setting the Buttons to the class variables
		if (isLHSApprPanel) {
			m_LHSApproveButton = apprButton;
			m_LHSRejectButton = rejButton;
		} else {
			m_RHSApproveButton = apprButton;
			m_RHSRejectButton = rejButton;
		}

		return pgApprPanel;
	}

	private void nextPage() {
		if (m_RHSPageNum >= m_NumberOfPages || m_LHSPageNum >= m_NumberOfPages)
			return;
		if (m_EnableAnimation)
			animateNextPage();
		m_LHSPageNum = m_RHSPageNum + 1;
		if (m_LHSPageNum < m_NumberOfPages)
			m_RHSPageNum = m_LHSPageNum + 1;
		else
			m_RHSPageNum = 0;
		m_PageRedraw = true;
		repaint();
	}

	private void prevPage() {
		if (m_RHSPageNum == 1)
			return;
		if (m_EnableAnimation)
			animatePrevPage();
		// This is to Handle if the Last Page is Reached.
		if (m_LHSPageNum == m_NumberOfPages)
			m_RHSPageNum = m_LHSPageNum - 1;
		else
			m_RHSPageNum = m_RHSPageNum - 2;
		m_LHSPageNum = m_RHSPageNum - 1;
		m_PageRedraw = true;
		repaint();
	}

	private void animateNextPage() {
		double theta = 0.0;
		int nextLHSPage = 0, nextRHSPage = 0;
		Vector pdfPages = new Vector();
		HashMap displayPages = new HashMap();

		// Getting the Required Pages
		nextLHSPage = m_RHSPageNum + 1;
		nextRHSPage = m_RHSPageNum + 2;

		// Ensuring the Required Pages are cached.
		pdfPages.addElement(m_LHSPageNum);
		pdfPages.addElement(m_RHSPageNum);
		pdfPages.addElement(nextLHSPage);
		pdfPages.addElement(nextRHSPage);
		int numOfPageDownload = 4;
		this.getNonCachedPages(pdfPages, numOfPageDownload);
		// this.getNonCachedPages(pdfPages);

		// Creating the Display Pages for Animation
		displayPages.put("LHSPage", m_LHSPageNum);
		displayPages.put("RHSPage", m_RHSPageNum);
		displayPages.put("NextLHSPage", nextLHSPage);
		displayPages.put("NextRHSPage", nextRHSPage);

		System.out.println("Display Pages: " + displayPages.toString());

		// Calculating Display Parameters
		// createDisplayParameters();

		Vector dispPDFPageOrdering = new Vector();
		int animatedStartPtX = 0, animatedStartPtY = 0;
		for (int i1 = 0; theta < (Math.PI); ++i1) {
			Polygon dispPolygon = createDisplayPolygon(theta);

			// Calculating the Bounds of the Polygon. The Image will be drawn on
			// the
			// Polygon Bounds and Clipped to Polygon to create a 3D Effect.
			Rectangle polygonBounds = dispPolygon.getBounds();
			int polygonWidth = (int) polygonBounds.getWidth();
			int polygonHt = (int) polygonBounds.getHeight();

			// The Way the Pages are Displayed is the LHS Page, then RHS Page
			// and
			// finally the Aninated Page. For Theta less then 90, the Animated
			// Page is r
			// the RHS Page while the RHS Page to be dislayed is the Next RHS
			// Page.
			// For Theta > 90, the Animated Page is the Next LHS Page
			dispPDFPageOrdering.addElement(displayPages.get("LHSPage"));
			dispPDFPageOrdering.addElement(displayPages.get("NextRHSPage"));
			if (theta <= (Math.PI / 2)) {
				dispPDFPageOrdering.addElement(displayPages.get("RHSPage"));
				animatedStartPtX = (int) m_RHSStartPtX;
			} else {
				dispPDFPageOrdering.addElement(displayPages.get("NextLHSPage"));
				animatedStartPtX = (int) m_LHSStartPtX;
			}
			animatedStartPtY = (int) m_StartPtY
					- (polygonHt - (int) m_SplitPanelSize.getHeight());
			if (animatedStartPtY > m_StartPtY)
				animatedStartPtY = (int) m_StartPtY;
			System.out.println("StartY: " + m_StartPtY + " :AnimateStartY: "
					+ animatedStartPtY);
			System.out.println("Display Page Ordering: "
					+ dispPDFPageOrdering.toString());
			this.displayAnimatedPage(dispPDFPageOrdering, dispPolygon,
					polygonHt, animatedStartPtX, animatedStartPtY);
			theta = (Math.PI / m_AnimationStep) * i1;
			dispPDFPageOrdering.clear();
		}
	}

	private void animatePrevPage() {
		double theta = Math.PI;
		int prevLHSPage = 0, prevRHSPage = 0;
		Vector pdfPages = new Vector();
		HashMap displayPages = new HashMap();

		// Getting the Required Pages
		prevLHSPage = m_LHSPageNum - 2;
		prevRHSPage = m_LHSPageNum - 1;

		// Ensuring the Required Pages are cached.
		pdfPages.addElement(m_LHSPageNum);
		pdfPages.addElement(m_RHSPageNum);
		pdfPages.addElement(prevLHSPage);
		pdfPages.addElement(prevRHSPage);
		this.getNonCachedPages(pdfPages);

		// Creating the Display Pages for Animation
		displayPages.put("LHSPage", m_LHSPageNum);
		displayPages.put("RHSPage", m_RHSPageNum);
		displayPages.put("PrevLHSPage", prevLHSPage);
		displayPages.put("PrevRHSPage", prevRHSPage);
		System.out.println("Display Pages: " + displayPages.toString());

		// Calculating Display Parameters
		// createDisplayParameters();

		Vector dispPDFPageOrdering = new Vector();
		int animatedStartPtX = 0, animatedStartPtY = 0;
		for (int i1 = 0; theta > 0; ++i1) {
			Polygon dispPolygon = createDisplayPolygon(theta);

			// Calculating the Bounds of the Polygon. The Image will be drawn on
			// the
			// Polygon Bounds and Clipped to Polygon to create a 3D Effect.
			Rectangle polygonBounds = dispPolygon.getBounds();
			int polygonWidth = (int) polygonBounds.getWidth();
			int polygonHt = (int) polygonBounds.getHeight();

			// The Way the Pages are Displayed is the LHS Page, then RHS Page
			// and
			// finally the Aninated Page. For Theta > 90, the Animated Page is
			// the
			// the LHS Page. The Prev LHS Page is displayed on the Left and the
			// RHS
			// Page is displayed in the Right. For Theta < 90, the Animated Page
			// is the
			// Prev RHS Page, the Left Page is the Prev LHS Page and the Right
			// Page
			// is the RHS Page
			dispPDFPageOrdering.addElement(displayPages.get("PrevLHSPage"));
			dispPDFPageOrdering.addElement(displayPages.get("RHSPage"));
			if (theta >= (Math.PI / 2)) {
				dispPDFPageOrdering.addElement(displayPages.get("LHSPage"));
				animatedStartPtX = (int) m_LHSStartPtX;
			} else {
				dispPDFPageOrdering.addElement(displayPages.get("PrevRHSPage"));
				animatedStartPtX = (int) m_RHSStartPtX;
			}
			animatedStartPtY = (int) m_StartPtY
					- (polygonHt - (int) m_SplitPanelSize.getHeight());
			if (animatedStartPtY > m_StartPtY)
				animatedStartPtY = (int) m_StartPtY;
			// System.out.println("Display Prev Page Ordering: " +
			// dispPDFPageOrdering.toString());
			this.displayAnimatedPage(dispPDFPageOrdering, dispPolygon,
					polygonHt, animatedStartPtX, animatedStartPtY);
			theta = Math.PI - ((Math.PI / m_AnimationStep) * i1);
			dispPDFPageOrdering.clear();
		}
	}

	// This Method is invoked to create an Animated effect of Page Turning.
	// For Next Page the Page Moves from Right to Left. While for previous
	// the Page moves from Left to Right
	public void dispAnimatedPageUsingPts(Vector displayPages,
			Polygon dispPolygon, int polygonHt, int animatedStartPtX,
			int animatedStartPtY) {
		BufferedImage lhsPage = null, rhsPage = null, animPage = null;
		BufferedImage lhsScaleImg = null, rhsScaleImg = null, animScaleImg = null;
		double lhsStXPt = 0.0, lhsStYPt = 0.0, rhsStXPt = 0.0, rhsStYPt = 0.0, animStXPt = 0.0, animStYPt = 0.0;

		// First Drawing the LHS Page into the Buffer
		int leftPage = ((Integer) displayPages.elementAt(0)).intValue();
		lhsPage = (BufferedImage) m_PDFPages.get(new Integer(leftPage));
		if (lhsPage != null) {
			lhsScaleImg = ImageUtils.scaleImage(lhsPage, m_SplitPanelSize);
			lhsStXPt = m_LHSStartPtX;
			lhsStYPt = m_StartPtY;
			System.out.println("LHSStartXPt: " + lhsStXPt + " LHSStartYPt: "
					+ lhsStYPt);
			System.out.println("Disp Image Size, Wt: " + lhsScaleImg.getWidth()
					+ " Ht: " + lhsScaleImg.getHeight());
		}

		// Second Drawing the RHS Page into the Buffer
		int rightPage = ((Integer) displayPages.elementAt(1)).intValue();
		rhsPage = (BufferedImage) m_PDFPages.get(new Integer(rightPage));
		if (rhsPage != null) {
			rhsScaleImg = ImageUtils.scaleImage(rhsPage, m_SplitPanelSize);
			rhsStXPt = m_RHSStartPtX;
			rhsStYPt = m_StartPtY;
			System.out.println("RHSStartXPt: " + rhsStXPt + " RHSStartYPt: "
					+ rhsStYPt);
			System.out.println("Disp Image Size, Wt: " + rhsScaleImg.getWidth()
					+ " Ht: " + rhsScaleImg.getHeight());
		}

		// Finally Drawing the Animated Page
		Dimension animatedSize = new Dimension(
				(int) m_SplitPanelSize.getWidth(), polygonHt);
		int animatedPage = ((Integer) displayPages.elementAt(2)).intValue();
		animPage = (BufferedImage) m_PDFPages.get(new Integer(animatedPage));
		if (animPage != null) {
			animScaleImg = ImageUtils.scaleImage(animPage, animatedSize);
			animStXPt = (int) animatedStartPtX;
			animStYPt = (int) animatedStartPtY;
		}

		this.drawBuffer(lhsScaleImg, (int) lhsStXPt, (int) lhsStYPt,
				animScaleImg, (int) animStXPt, (int) animStYPt, rhsScaleImg,
				(int) rhsStXPt, (int) rhsStYPt, dispPolygon, false);

		try {
			Thread.sleep(m_AnimationSpeed);
		} catch (Exception e) {
		}
	}

	// This Method is invoked to create an Animated effect of Page Turning.
	// For Next Page the Page Moves from Right to Left. While for previous
	// the Page moves from Left to Right
	public void displayAnimatedPage(Vector displayPages, Polygon dispPolygon,
			int polygonHt, int animatedStartPtX, int animatedStartPtY) {
		if (!m_UseAffine4Display) {
			// Here the Affien is not used and X, Y Pts are calculated
			this.dispAnimatedPageUsingPts(displayPages, dispPolygon, polygonHt,
					animatedStartPtX, animatedStartPtY);
			return;
		}
		double scale = 1.0;
		BufferedImage pdfPage = null;
		Graphics2D bufG = (Graphics2D) m_DisplayBuffer.getGraphics();
		bufG.clearRect(0, 0, (int) m_DisplaySize.getWidth(),
				(int) m_DisplaySize.getHeight());

		// First Drawing the LHS Page into the Buffer
		int leftPage = ((Integer) displayPages.elementAt(0)).intValue();
		pdfPage = (BufferedImage) m_PDFPages.get(new Integer(leftPage));
		if (pdfPage != null) {
			bufG.drawImage(pdfPage, m_LHSPageDisplayCTM, null);
		}

		// Second Drawing the RHS Page into the Buffer
		int rightPage = ((Integer) displayPages.elementAt(1)).intValue();
		pdfPage = (BufferedImage) m_PDFPages.get(new Integer(rightPage));
		if (pdfPage != null) {
			bufG.drawImage(pdfPage, m_RHSPageDisplayCTM, null);
		}

		// Finally Drawing the Animated Page
		Dimension animatedSize = new Dimension(
				(int) m_SplitPanelSize.getWidth(), polygonHt);
		int animatedPage = ((Integer) displayPages.elementAt(2)).intValue();
		pdfPage = (BufferedImage) m_PDFPages.get(new Integer(animatedPage));
		BufferedImage scaleImg = null;
		if (pdfPage != null) {
			scaleImg = ImageUtils.ScaleToSize(pdfPage,
					(int) m_SplitPanelSize.getWidth(), polygonHt);
			bufG.setClip(dispPolygon);
			bufG.drawImage(scaleImg, animatedStartPtX, animatedStartPtY, null);
		}
		// Setting the Page Redraw to false.
		// m_PageRedraw = false;
		// repaint();
		Graphics g = this.getGraphics();
		g.drawImage(m_DisplayBuffer, 0, 0, this);

		try {
			Thread.sleep(m_AnimationSpeed);
		} catch (Exception e) {
		}
	}

	// This is to Limit the Polygon Height by a factor.
	double m_PolygonLimitingFactor = 0.2;

	private Polygon createDisplayPolygon(double theta) {
		Polygon dispPolygon = null;

		int[] x = { 0, 0, 0, 0 };
		int[] y = { 0, 0, 0, 0 };
		// m_DisplayPageWidth m_DisplayPageHeight
		int dispWidth = m_DisplayPageWidth;
		// int dispWidth = (int) m_VisibleDisplaySize.getWidth();
		// m_LHSPageDisplayCTM.getScaleX());
		// int dispHeight = (int) m_SplitPanelSize.getHeight();
		int dispHeight = m_DisplayPageHeight;
		// int dispHeight = (int) m_VisibleDisplaySize.getHeight();
		// m_LHSPageDisplayCTM.getScaleY());

		double base = dispWidth * Math.cos(theta);
		double perpendicular = dispWidth * Math.sin(theta)
				* m_PolygonLimitingFactor;
		System.out.println("base " + base + " perp " + perpendicular);
		if (theta < (Math.PI / 2)) {
			x[0] = (int) m_RHSStartPtX;
			y[0] = (int) m_StartPtY;
			x[1] = (int) m_RHSStartPtX;
			y[1] = dispHeight + (int) m_StartPtY;
			x[2] = (int) base + (int) m_RHSStartPtX;
			y[2] = y[1] - (int) perpendicular;
			x[3] = (int) base + (int) m_RHSStartPtX;
			y[3] = y[2] - dispHeight;
		}

		int spiral_width = 0;

		if (theta >= (Math.PI / 2)) {
			x[0] = (int) m_RHSStartPtX - spiral_width;
			y[0] = (int) m_StartPtY;
			x[1] = (int) m_RHSStartPtX - spiral_width;
			y[1] = dispHeight + (int) m_StartPtY;
			x[2] = (int) base + (int) m_RHSStartPtX - spiral_width;
			y[2] = y[1] - (int) perpendicular;
			x[3] = (int) base + (int) m_RHSStartPtX - spiral_width;
			y[3] = y[2] - dispHeight;
		}
		dispPolygon = new Polygon(x, y, 4);
		return dispPolygon;
	}

	private JButton makeNavigationButton(String imageName, String toolTipText) {
		ImageIcon imgIcon = null;
		// Loading the image.
		String imgLocation = "res/" + imageName + ".gif";

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
			String imgPath = "/res/" + imageName + ".gif";
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
		return button;
	}

	// m_DigitalAssetId m_CurrentPage m_NumberOfPages m_PDFPages
	// m_BindingOptions
	// m_DisplayPDFPages m_PDFPageWidth, m_PDFPageHeight m_ScreenWidth,
	// m_ScreenHeight
	// m_StartXRight = 0, m_StartXLeft = 0 m_StartY = 0 m_DisplayPolygon
	// m_AnimationSpeed
	// m_AnimationStep = 16 ; m_Theta=0 m_DisplayBuffer m_DisplayContentPanel
	// m_PDFPageInfoManager m_PDFView m_BindingWidth

	// This is valid for a Single Page Document
	public static int DISPLAY_SINGLE_PAGE = 1;
	// This is valid for a Two Page Document or for a Middle content in a
	// multi page document
	public static int DISPLAY_DOUBLE_PAGE = 2;
	// This is valid for a Multi Page Document when First Page has to be
	// Displayed
	public static int DISPLAY_RHS_PAGE_ONLY = 3;
	// This is valid for a Multi Page Document when Last Page has to be
	// Displayed
	public static int DISPLAY_LHS_PAGE_ONLY = 4;

	// Display Mode to activate appropriate display described above
	public int m_DisplayMode;

	// This method is called the very first time when the PDF to be shown as
	// EBook is loaded.
	Dimension m_DisplaySize;

	// Current Page Number if it is Single Page Document or the Display Mode
	// is DISPLAY_SINGLE_PAGE
	public int m_CurrentPageNum = -1;

	// LHS Page Number of a Multi Page Document
	public int m_LHSPageNum = -1;

	// RHS Page Number of a Multi Page Document
	public int m_RHSPageNum = -1;

	Dimension m_SplitPanelSize;
	Dimension m_VisibleDisplaySize;

	// Display CTM for LHS and RHS Page
	AffineTransform m_PageDisplayCTM = null;
	AffineTransform m_LHSPageDisplayCTM = null;
	AffineTransform m_RHSPageDisplayCTM = null;

	private void createDisplayParameters() {
		if (m_PDFPages == null)
			return;
		// This leaves the space in the Left and the Bottom.
		Rectangle visibleRect = this.getVisibleRect();
		if (visibleRect.getWidth() == 0 || visibleRect.getHeight() == 0)
			return;
		m_DisplaySize = new Dimension((int) visibleRect.getWidth() - 30,
				(int) visibleRect.getHeight() - 20);
		m_DisplayBuffer = this.createImage((int) m_DisplaySize.getWidth(),
				(int) m_DisplaySize.getHeight());
		m_SplitPanelSize = new Dimension((int) (m_DisplaySize.getWidth() / 2),
				(int) m_DisplaySize.getHeight());

		// Getting the Page Width and Height. Here it is assumed that all the
		// PDF
		// Page in the Book have the same Ht and Width
		BufferedImage pdfPage = (BufferedImage) m_PDFPages.get(new Integer(1));

		// Getting the first page if it is yet not retrieved
		if (pdfPage == null) {
			System.out.println("No PDF Page Populated");
			Vector pdfPages = new Vector();
			pdfPages.addElement(1);
			this.getNonCachedPages(pdfPages);
		}

		System.out.println("Page Width: " + pdfPage.getWidth() + " Page Ht: "
				+ pdfPage.getHeight());
		// Calculating the Affine Transformation and Start Positions for the
		// Split Panel

		m_LHSStartPtX = 20.0;
		m_StartPtY = 30.0;

		// Calculating the Affine for Left Page. This returns the Affine
		// Transformation
		m_PageDisplayCTM = calcDisplayCTM(ScalingFactor.FIT_PAGE,
				m_DisplaySize, pdfPage.getWidth(), pdfPage.getHeight(),
				m_LHSStartPtX, m_StartPtY);

		System.out.println("LHS CTM: " + m_LHSPageDisplayCTM);

		// Calculating the Affine for Left Page. This returns the Affine
		// Transformation
		m_LHSPageDisplayCTM = calcDisplayCTM(ScalingFactor.FIT_PAGE,
				m_SplitPanelSize, pdfPage.getWidth(), pdfPage.getHeight(),
				m_LHSStartPtX, m_StartPtY);

		System.out.println("LHS CTM: " + m_LHSPageDisplayCTM);

		// The Scale of Left Affine Transformation is used to calculate the
		// start positioning the RHS Page.
		m_RHSStartPtX = m_LHSStartPtX + pdfPage.getWidth()
				* m_LHSPageDisplayCTM.getScaleX();
		System.out.println("Inital RHS Start Pt: " + m_RHSStartPtX);
		// Calculating the Affine for Left Page. This returns the Affine
		// Transformation
		m_RHSPageDisplayCTM = calcDisplayCTM(ScalingFactor.FIT_PAGE,
				m_SplitPanelSize, pdfPage.getWidth(), pdfPage.getHeight(),
				m_RHSStartPtX, m_StartPtY);
		System.out.println("RHS CTM: " + m_RHSPageDisplayCTM);

		// Resetting the Start Points
		m_StartPtY = m_LHSPageDisplayCTM.getTranslateY();
		m_LHSStartPtX = m_LHSPageDisplayCTM.getTranslateX();
		m_RHSStartPtX = m_RHSPageDisplayCTM.getTranslateX();
	}

	public void displayEBook() {
		if (!m_PageRedraw)
			return;
		Vector nonCachedPages = null;
		Vector pdfPages = new Vector();
		System.out.println("Displaying LHS Pg Num: " + m_LHSPageNum
				+ " RHS Pg Num: " + m_RHSPageNum);
		// Setting the Page Redraw to false.
		m_PageRedraw = false;

		if (m_NumberOfPages == 1) {
			m_CurrentPageNum = 1;
			pdfPages.addElement(m_CurrentPageNum);
			this.getNonCachedPages(pdfPages);
			m_DisplayMode = DISPLAY_SINGLE_PAGE;
			m_PageNumField.setText(String.valueOf(m_CurrentPageNum));
			displaySinglePageMode();
			return;
		}
		// This condition occurs the Very First Time or when the First Page is
		// displayed.
		if (m_RHSPageNum == -1 || m_RHSPageNum == 1) {
			m_LHSPageNum = -1;
			m_RHSPageNum = 1;
			pdfPages.addElement(m_RHSPageNum);
			this.getNonCachedPages(pdfPages);
			m_DisplayMode = DISPLAY_RHS_PAGE_ONLY;
			m_PageNumField.setText(String.valueOf(m_RHSPageNum));
			displayDoublePageMode();
			return;
		}
		// This is to display Last Page if the m_NumberOfPages is even
		if (m_LHSPageNum == m_NumberOfPages) {
			// m_RHSPageNum = -1;
			System.out.println("Displaying Last LHS Pg Num: " + m_LHSPageNum
					+ " RHS Pg Num: " + m_RHSPageNum);
			pdfPages.addElement(m_LHSPageNum);
			this.getNonCachedPages(pdfPages);
			m_DisplayMode = DISPLAY_LHS_PAGE_ONLY;
			m_PageNumField.setText(String.valueOf(m_LHSPageNum));
			displayDoublePageMode();
			return;
		}
		// This is display inside Pages
		pdfPages.addElement(m_LHSPageNum);
		pdfPages.addElement(m_RHSPageNum);
		m_DisplayMode = DISPLAY_DOUBLE_PAGE;
		m_PageNumField.setText(String.valueOf(m_LHSPageNum) + "-"
				+ String.valueOf(m_RHSPageNum));
		int numOfPageDownload = 4;
		this.getNonCachedPages(pdfPages, numOfPageDownload);
		// this.getNonCachedPages(pdfPages);
		displayDoublePageMode();
	}

	// This method is called to retrieve the single Page that need to be
	// displayed.
	private void getNonCachedPages(Vector pdfPages) {
		this.getNonCachedPages(pdfPages, -1);
	}

	// This method is called to retrieve Pages that need to be displayed. The
	// numOfPages can be 1, 2 or 4
	private void getNonCachedPages(Vector pdfPages, int numOfPages) {
		Vector nonCachedPages = null;
		if (m_PDFPages == null) {
			m_PDFPages = new HashMap();
		}
		// This check is done if the page is already downloaded then no need to
		// wait
		// on Synchronized Thread
		System.out.println("\n\n******************************\n");
		System.out.println("Get Pages: " + pdfPages.toString());
		// numOfPages -1 means this is the last page. Calling getNextPages
		// method
		// to invoke next numOfPages that are not yet needed for display
		if (numOfPages != -1)
			this.getNextPages(numOfPages, pdfPages);

		// This retrieves the Page Nums that have not yet started loading
		nonCachedPages = this.getNonCachedPageNums(pdfPages);

		// If null all Pages are retrieved
		if (nonCachedPages == null)
			return;

		// This is retrieving pages that are needed for diaplay and are not yet
		// retrieved
		System.out.println("Retrieving Pages: " + nonCachedPages.toString());
		System.out.println("\n******************************\n\n");
		try {
			System.out.println("Finally retrieving Pages: "
					+ nonCachedPages.toString());
			HashMap nonCachedPDFPages = m_EBookPageHandler
					.getPages(nonCachedPages);
			System.out.println("Retrieved Pages: "
					+ nonCachedPDFPages.toString());
			m_PDFPages.putAll(nonCachedPDFPages);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// This retrieves the Page Nums that have not yet started loading
	private Vector getNonCachedPageNums(Vector pdfPages) {
		System.out.println("Request to retrieve Pages: " + pdfPages.toString());
		Vector nonCachedPages = null;
		for (int i = 0; i < pdfPages.size(); i++) {
			int pgNum = ((Integer) pdfPages.elementAt(i)).intValue();
			if (pgNum == -1 || pgNum == 0 || pgNum > m_NumberOfPages)
				continue;
			if (m_PDFPages.get(pgNum) != null)
				continue;
			boolean isLoaded = m_PDFPageLoadStatus.contains(pgNum);
			if (isLoaded) {
				try {
					while (m_PDFPages.get(pgNum) == null) {
						Thread.sleep(1000);
					}
					continue;
				} catch (Exception ex) {
				}
			}
			if (nonCachedPages == null)
				nonCachedPages = new Vector();
			nonCachedPages.addElement(pgNum);
			m_PDFPageLoadStatus.addElement(pgNum);
			// m_PDFPageLoadStatus.put(pgNum, true);
		}
		return nonCachedPages;
	}

	// This method is invoked to retrieve the next pages that are not yet loaded
	// or started loading in a background thread. This are the pages that are
	// not
	// yet required for display. This method is used to retrieve future pages in
	// anticipation in Background thread
	private void getNextPages(int numOfPages, Vector pdfPages) {
		Vector nextPages = new Vector();
		if (this.getNextPage(pdfPages, nextPages) == -1)
			return;
		for (int i = 0; i < numOfPages; i++) {
			int pgNum = this.getNextPage(pdfPages, nextPages);
			if (pgNum == -1)
				break;
			nextPages.addElement(pgNum);
		}
		if (nextPages.size() == 0)
			return;

		// Retrieve Pages using Background Thread
		HashMap requestData = new HashMap();
		RGPTThreadWorker threadWorker = null;
		requestData.put("RequestType", "GetPages");
		requestData.put("Pages", nextPages);
		threadWorker = new RGPTThreadWorker(Thread.MIN_PRIORITY, this,
				requestData);
		threadWorker.startThreadInvocation();
	}

	// This method is invoked to retrieve the next page that have not yet
	// started loading
	private int getNextPage(Vector currPages, Vector nextPages) {
		boolean pgLoadStatus = true, foundPgReq = false;
		// Object[] pages = m_PDFPageLoadStatus.keySet().toArray();
		for (int i = 0; i < m_NumberOfPages; i++) {
			// Resetting the foundPgReq;
			foundPgReq = false;
			int pgNum = i + 1;
			// m_PDFPageLoadStatus has pages that are currently uploaded
			if (m_PDFPageLoadStatus.contains(pgNum))
				continue;
			// currPages has pages that are to be immediately uploaded
			if (currPages.contains(pgNum))
				continue;
			// nextPages has pages that are to be uploaded in the background
			if (nextPages.contains(pgNum))
				continue;
			return pgNum;
		}
		return -1;
	}

	// Start Pt for LHS and RHS Panel. The PDF Page will be drawn from this
	// Point.
	double m_LHSStartPtX = 0.0, m_RHSStartPtX = 0.0;
	double m_StartPtY = 0.0;

	private void setRenderingHints(Graphics2D g2d) {
		RenderingHints rh = g2d.getRenderingHints();
		rh.put(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		rh.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHints(rh);
	}

	// This method is used
	public void displaySinglePageMode() {
		double scale = 1.0;
		BufferedImage pdfPage = null;
		if (m_DisplaySize == null || m_DisplayBuffer == null)
			createDisplayParameters();
		System.out.println("Disp Size: " + this.getSize().toString());
		System.out.println("Visible Size: " + this.getVisibleRect().toString());

		Graphics2D bufG = (Graphics2D) m_DisplayBuffer.getGraphics();
		bufG.clearRect(0, 0, (int) m_DisplaySize.getWidth(),
				(int) m_DisplaySize.getHeight());
		this.setRenderingHints(bufG);
		pdfPage = (BufferedImage) m_PDFPages.get(new Integer(m_CurrentPageNum));
		if (m_UseAffine4Display) {
			bufG.drawImage(pdfPage, m_PageDisplayCTM, null);
			return;
		}
		Rectangle visibleRect = this.getVisibleRect();
		if (visibleRect.getWidth() == 0 || visibleRect.getHeight() == 0)
			return;
		System.out
				.println("Visible EBook Rectangle: " + visibleRect.toString());
		m_DisplayBuffer = this.createImage((int) visibleRect.getWidth(),
				(int) visibleRect.getHeight());
		double startXPt = 30.0, startYPt = 10.0;
		Dimension dispDim = new Dimension((int) (visibleRect.getWidth()) - 30,
				(int) visibleRect.getHeight() - 100);
		System.out.println("EBook Disp Rectangle: " + dispDim.toString());
		BufferedImage scaleImg = ImageUtils.scaleImage(pdfPage, dispDim);
		System.out.println("Disp Image Size, Wt: " + scaleImg.getWidth()
				+ " Ht: " + scaleImg.getHeight());
		if (visibleRect.height > scaleImg.getHeight()) {
			startYPt = (visibleRect.height - scaleImg.getHeight()) / 2;
		}
		if (visibleRect.width > scaleImg.getWidth()) {
			startXPt = (visibleRect.width - scaleImg.getWidth()) / 2;
		}
		System.out.println("StartXPt: " + startXPt + " startYPt: " + startYPt);
		this.drawLHSBuffer(scaleImg, (int) startXPt, (int) startYPt);
	}

	// This method is called to Display Document in Two Page Mode. This is
	// called
	// display 2 Page Document. In Multi Page Document scenario, this methos is
	// called to Display First Page (i.e. RHS Page), Display Last Page (i.e. LHS
	// Page) or the Middle Content (i.e. Double Page Mode).
	// Here the Affien is not used and X, Y Pts are calculated

	public void displayDoublePageUsingPts() {
		BufferedImage lhsPage = null, rhsPage = null;
		BufferedImage lhsScaleImg = null, rhsScaleImg = null;
		double lhsStXPt = 0.0, lhsStYPt = 0.0, rhsStXPt = 0.0, rhsStYPt = 0.0;
		Dimension dispLHSDim = null, dispRHSDim = null;
		Rectangle visibleRect = this.getVisibleRect();
		if (visibleRect.getWidth() == 0 || visibleRect.getHeight() == 0)
			return;
		System.out
				.println("Visible EBook Rectangle: " + visibleRect.toString());
		m_DisplayBuffer = this.createImage((int) visibleRect.getWidth(),
				(int) visibleRect.getHeight());
		int xGap = 30, yGap = 100;
		Dimension dispDim = new Dimension(
				(int) (visibleRect.getWidth()) - xGap,
				(int) visibleRect.getHeight() - yGap);
		Dimension dispPageDim = new Dimension((int) dispDim.width / 2,
				dispDim.height);
		m_SplitPanelSize = dispPageDim;
		System.out.println("EBook Disp Rectangle: " + dispDim.toString());
		System.out.println("LHSPage: " + m_LHSPageNum + " RHSPage: "
				+ m_RHSPageNum);
		Point2D startPt = null;
		if (m_DisplayMode == DISPLAY_DOUBLE_PAGE
				|| m_DisplayMode == DISPLAY_LHS_PAGE_ONLY) {
			lhsPage = (BufferedImage) m_PDFPages.get(new Integer(m_LHSPageNum));
			lhsScaleImg = ImageUtils.scaleImage(lhsPage, dispPageDim);
			startPt = this.setDisplayPts(visibleRect, lhsScaleImg, xGap, yGap);
			lhsStXPt = startPt.getX();
			lhsStYPt = startPt.getY();
			System.out.println("LHSStartXPt: " + lhsStXPt + " LHSStartYPt: "
					+ lhsStYPt);
			System.out.println("Disp Image Size, Wt: " + lhsScaleImg.getWidth()
					+ " Ht: " + lhsScaleImg.getHeight());
		}

		if (m_RHSPageNum > 0) {
			rhsPage = (BufferedImage) m_PDFPages.get(new Integer(m_RHSPageNum));
			rhsScaleImg = ImageUtils.scaleImage(rhsPage, dispPageDim);
			if (startPt == null)
				startPt = this.setDisplayPts(visibleRect, rhsScaleImg, xGap,
						yGap);
			rhsStXPt = startPt.getX() + rhsScaleImg.getWidth();
			rhsStYPt = startPt.getY();
			System.out.println("RHSStartXPt: " + rhsStXPt + " RHSStartYPt: "
					+ rhsStYPt);
			System.out.println("Disp Image Size, Wt: " + rhsScaleImg.getWidth()
					+ " Ht: " + rhsScaleImg.getHeight());
		}
		this.drawDoublePageBuffer(lhsScaleImg, (int) lhsStXPt, (int) lhsStYPt,
				rhsScaleImg, (int) rhsStXPt, (int) rhsStYPt);
	}

	private Point2D setDisplayPts(Rectangle visibleRect,
			BufferedImage scaledImg, int xgap, int ygap) {
		double startXPt = 0.0, startYPt = 0.0;
		int totDispHt = scaledImg.getHeight();
		int totDispWt = 2 * scaledImg.getWidth();
		if (visibleRect.height > totDispHt) {
			startYPt = (visibleRect.height - totDispHt) / 2;
			if (startYPt < ygap)
				startYPt = (double) (ygap / 2);
		}
		if (visibleRect.width > totDispWt) {
			startXPt = (visibleRect.width - totDispWt) / 2;
			if (startXPt < xgap)
				startXPt = (double) (xgap / 2);
		}
		// Setting the RHS Start Pt X and Y used by Animation to calculate
		// the Polygon
		m_RHSStartPtX = startXPt + scaledImg.getWidth();
		m_StartPtY = startYPt;
		m_LHSStartPtX = (double) startXPt;
		m_DisplayPageHeight = scaledImg.getHeight();
		m_DisplayPageWidth = scaledImg.getWidth();
		return new Point2D.Double(startXPt, startYPt);
	}

	public void drawLHSBuffer(BufferedImage lhsImg, int startXPt, int startYPt) {
		this.drawBuffer(lhsImg, startXPt, startYPt, null, -1, -1, null, -1, -1,
				null);
	}

	public void drawDoublePageBuffer(BufferedImage lhsImg, int lhstXPt,
			int lhsYPt, BufferedImage rhsImg, int rhsXPt, int rhsYPt) {
		this.drawBuffer(lhsImg, lhstXPt, lhsYPt, null, -1, -1, rhsImg, rhsXPt,
				rhsYPt, null);
	}

	public void drawBuffer(BufferedImage lhsImg, int startx1, int starty1,
			BufferedImage animImg, int startx2, int starty2,
			BufferedImage rhsImg, int startx3, int starty3, Polygon dispPolygon) {
		this.drawBuffer(lhsImg, startx1, starty1, animImg, startx2, starty2,
				rhsImg, startx3, starty3, dispPolygon, false);
	}

	public void drawBuffer(BufferedImage lhsImg, int startx1, int starty1,
			BufferedImage animImg, int startx2, int starty2,
			BufferedImage rhsImg, int startx3, int starty3,
			Polygon dispPolygon, boolean testAnim) {
		Graphics2D bufG = (Graphics2D) m_DisplayBuffer.getGraphics();
		int wt = (int) m_DisplayBuffer.getWidth(null), ht = (int) m_DisplayBuffer
				.getHeight(null);
		bufG.clearRect(0, 0, wt, ht);
		this.setRenderingHints(bufG);
		bufG.setBackground(RGPTUIManager.PANEL_COLOR);
		bufG.setPaint(RGPTUIManager.BG_COLOR);
		bufG.fill(new Rectangle(wt, ht));
		if (lhsImg != null && !testAnim)
			bufG.drawImage(lhsImg, startx1, starty1, null);
		if (rhsImg != null && !testAnim)
			bufG.drawImage(rhsImg, startx3, starty3, null);
		if (dispPolygon != null)
			bufG.setClip(dispPolygon);
		if (animImg != null)
			bufG.drawImage(animImg, startx2, starty2, null);
		Graphics graphics = this.getGraphics();
		graphics.drawImage(m_DisplayBuffer, 0, 0, this);
	}

	public void displayDoublePageMode() {
		if (!m_UseAffine4Display) {
			// Here the Affien is not used and X, Y Pts are calculated
			this.displayDoublePageUsingPts();
			return;
		}
		double scale = 1.0;
		BufferedImage pdfPage = null;
		if (m_DisplaySize == null || m_DisplayBuffer == null)
			createDisplayParameters();
		System.out.println("Disp Size: " + this.getSize().toString());
		System.out.println("Visible Size: " + this.getVisibleRect().toString());

		Graphics2D bufG = (Graphics2D) m_DisplayBuffer.getGraphics();
		bufG.clearRect(0, 0, (int) m_DisplaySize.getWidth(),
				(int) m_DisplaySize.getHeight());

		this.setRenderingHints(bufG);
		// This is to Handle when the First Page of the Multi Page Document has
		// to
		// be displayed. In the Scenario the LHS Page Number is same as RHS Page
		// Number. The LHS Page is not displayed and is used only for
		// positioning
		// the RHS Position
		// if (m_LHSPageNum == -1) m_LHSPageNum = m_RHSPageNum;
		pdfPage = (BufferedImage) m_PDFPages.get(new Integer(m_LHSPageNum));

		// m_LHSStartPtX = 20.0;
		// m_StartPtY = 30.0;
		// The Scale is used in positioning the RHS Page accurately
		// scale = deriveDeviceCTM(ScalingFactor.FIT_PAGE, m_SplitPanelSize,
		// pdfPage.getWidth(), pdfPage.getHeight(),
		// m_LHSStartPtX, m_StartPtY, true);
		// This Page is Displayed only when the mode is Double Page or Display
		// LHS Page only which is the last page of the Document.
		if (m_DisplayMode == DISPLAY_DOUBLE_PAGE
				|| m_DisplayMode == DISPLAY_LHS_PAGE_ONLY) {
			bufG.drawImage(pdfPage, m_LHSPageDisplayCTM, null);
		}

		// No further processing is done if the Display Mode is LHS Only.
		if (m_DisplayMode == DISPLAY_LHS_PAGE_ONLY)
			return;

		// This means further processing is done if the Display Mode is RHS or
		// Double Page
		// m_RHSStartPtX = pdfPage.getWidth()*scale;
		pdfPage = (BufferedImage) m_PDFPages.get(new Integer(m_RHSPageNum));
		// deriveDeviceCTM(ScalingFactor.FIT_PAGE, m_SplitPanelSize,
		// pdfPage.getWidth(),
		// pdfPage.getHeight(), m_RHSStartPtX, m_StartPtY, true);
		bufG.drawImage(pdfPage, m_RHSPageDisplayCTM, null);
		repaint();
	}

	public void repaintContent() {
		this.repaint();
	}

	public void paint(Graphics g) {
		System.out.println("In Paint Method");
		if (m_PageRedraw)
			this.displayEBook();
		this.setRenderingHints((Graphics2D) g);
		g.drawImage(m_DisplayBuffer, 0, 0, null);
		m_PageNavPanel.repaint();
	}

	AffineTransform calcDisplayCTM(ScalingFactor scaleFactor,
			Dimension panelSize, int imgWidth, int imgHt, double startXPt,
			double startYPt) {
		AffineTransform devCloneCTM = null;
		AffineTransform finalDevCTM = null;
		AffineTransform pgCloneCTM = null;

		// Scaling Parameters
		double scale = 0.0, sx = 0.0, sy = 0.0;
		// Translation parameters
		double tx = 0.0, ty = 0.0;
		// Image Ht and Width after Scaling
		double newImgHt = 0.0, newImgWidth = 0.0;

		// Defining the Viewable Area

		double viewablePanelHt = (double) panelSize.getHeight();
		double viewablePanelWidth = (double) panelSize.getWidth();

		viewablePanelHt = viewablePanelHt - startYPt;
		if (ScalingFactor.FIT_HEIGHT.equals(scaleFactor)) {
			sy = viewablePanelHt / imgHt;
			if (sy > 1.0)
				sy = 1.0;
			scale = sy;
		} else if (ScalingFactor.FIT_PAGE.equals(scaleFactor)) {
			sy = viewablePanelHt / imgHt;
			sx = viewablePanelWidth / imgWidth;
			if (sx > sy)
				scale = sy;
			else
				scale = sx;
		} else if (ScalingFactor.FIT_WIDTH.equals(scaleFactor)) {
			sx = viewablePanelWidth / imgWidth;
			if (sx > 1.0)
				sx = 1.0;
			scale = sx;
		} else if (ScalingFactor.ZOOM_IN_OUT.equals(scaleFactor))
			scale = ((double) scaleFactor.m_ZoomValue) / 100;

		double scaledPDFPageWd = imgWidth * scale;
		double scaledPDFPageHt = imgHt * scale;

		System.out.println("Bfor Viewable Width: " + viewablePanelWidth
				+ " Viewable Ht: " + viewablePanelHt);

		// Calculating Translations in X and Y dirn.
		if (scaledPDFPageHt < viewablePanelHt) {
			// Devided by 2 to give equal padding on either side of the Image
			// Width
			ty = startYPt + (viewablePanelHt - scaledPDFPageHt) / 2;
			viewablePanelHt = scaledPDFPageHt;
		} else
			ty = startYPt;

		if (scaledPDFPageWd < viewablePanelWidth) {
			// Devided by 2 to give equal padding on either side of the Image
			// Width
			tx = startXPt + (viewablePanelWidth - scaledPDFPageWd) / 2;
			viewablePanelWidth = scaledPDFPageWd;
		} else
			tx = startXPt;
		m_VisibleDisplaySize = new Dimension((int) viewablePanelWidth,
				(int) viewablePanelHt);
		System.out.println("Scale: " + scale + " Tx: " + tx + " Ty: " + ty);
		System.out.println("Scaled Widtht: " + scaledPDFPageWd + " Scaled Ht: "
				+ scaledPDFPageHt);
		System.out.println("After Viewable Width: " + viewablePanelWidth
				+ " Viewable Ht: " + viewablePanelHt);
		AffineTransform displayCTM = null;
		displayCTM = new AffineTransform(scale, 0, 0, scale, tx, ty);
		return displayCTM;
	}

	// This is for Clean-up Activity. When called this releases the PDFPageInfo
	// Memory for every Page as well User VDP Data for every Page
	public void cleanUpMemory() {
	}
}
