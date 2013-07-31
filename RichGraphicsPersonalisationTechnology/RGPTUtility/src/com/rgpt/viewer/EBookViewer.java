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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
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
import com.rgpt.serverhandler.PDFViewInterface;
import com.rgpt.serverutil.PDFPageInfoManager;
import com.rgpt.util.AppletParameters;
import com.rgpt.util.ScalingFactor;

public class EBookViewer extends JPanel implements ComponentListener,
		ApprovalHandler {
	// Digital Asset currently being Displayed
	int m_DigitalAssetId;

	// Current Page Number. This is is always the right hand page. Set initally
	// to arbitary value -1, indicating not yet assigned.
	int m_CurrentPage = -1;

	// Number of Pages to be displayed in the Document
	int m_NumberOfPages;

	// This maintains HashMap of PDF Page to the corresponding PDF Page.
	HashMap m_PDFPages;

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

	// This holds all the PDF Page Information
	PDFPageInfoManager m_PDFPageInfoManager;

	// Container having this Panel as a Content Pane
	Container m_PDFViewContainer;

	// PDFViewInterface to get PDF Page as Image
	PDFViewInterface m_PDFView;

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

	public EBookViewer(int assetId, int numOfPages, Container pdfViewContainer,
			PDFViewInterface pdfView, PDFPageInfoManager pgInfoMgr,
			boolean enablePgAnim, int animSpeed, int animStep,
			boolean enablePDFAppr) {
		m_PDFView = pdfView;
		m_DigitalAssetId = assetId;
		m_NumberOfPages = numOfPages;
		m_AnimationStep = animStep;
		m_AnimationSpeed = animSpeed;
		m_EnableAnimation = enablePgAnim;
		m_EnablePDFApproval = enablePDFAppr;
		m_PDFPageInfoManager = pgInfoMgr;
		m_PDFViewContainer = pdfViewContainer;

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
				int pgNum = -1;
				int pgCounter = 0;
				// This setting is used to set the number of page to be
				// downloaded every
				// time from server
				int numOfPageDownload = 4;
				Vector pdfPages = new Vector();
				// Loading the First Page initially and then the remaining
				for (int i = 0; i < m_NumberOfPages; i++) {
					pgNum = i + 1;
					pdfPages.addElement(pgNum);
					if (pgNum == 1) {
						getNonCachedPages(pdfPages);
						pdfPages.clear();
						continue;
					}
					pgCounter++;
					if (pgCounter == numOfPageDownload) {
						getNonCachedPages(pdfPages);
						pdfPages.clear();
						pgCounter = 0;
					}
				}
				// This downloads the last page that was not downloaded
				if (pdfPages.size() > 0)
					getNonCachedPages(pdfPages);
			}
		});
	}

	AppletParameters m_AppletParameters = null;

	public void setAppletParameters(AppletParameters params) {
		m_AppletParameters = params;
	}

	private void closeEBookViewer() {
		if (m_PageApprovalStatus == null)
			return;
		m_PDFPageInfoManager.setPDFBatchApproval(m_PageApprovalStatus);
	}

	// Warning PDF Page Count and Template Page Count not specified
	public int m_PDFPageCount = -1, m_TemplatePageCount = -1;

	private String getApprovalMesg(boolean showMesg) {
		if (m_PageApprovalStatus == null || m_AppletParameters == null)
			return "";
		System.out
				.println("Page Approvals: " + m_PageApprovalStatus.toString());
		AppletParameters params = m_AppletParameters;
		HashMap pdfDocApprStatus = m_PDFPageInfoManager.getPDFDocApprovals(
				m_PDFPageCount, m_TemplatePageCount, m_PageApprovalStatus);
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
		m_DisplayPolygons.clear();
		repaint();
	}

	// Invoked when the component has been made visible.
	public void componentShown(ComponentEvent e) {
	}

	public void populateContentPane(JPanel contentPanel) {

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
		this.getNonCachedPages(pdfPages);

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
			// System.out.println("StartY: " + m_StartPtY +
			// " :AnimateStartY: " + animatedStartPtY);
			// System.out.println("Display Page Ordering: " +
			// dispPDFPageOrdering.toString());
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
	public void displayAnimatedPage(Vector displayPages, Polygon dispPolygon,
			int polygonHt, int animatedStartPtX, int animatedStartPtY) {
		double scale = 1.0;
		BufferedImage pdfPage = null;
		Graphics2D bufG = (Graphics2D) m_DisplayBuffer.getGraphics();
		bufG.clearRect(0, 0, (int) m_DisplaySize.getWidth(),
				(int) m_DisplaySize.getHeight());

		// First Drawing the LHS Page into the Buffer
		int leftPage = ((Integer) displayPages.elementAt(0)).intValue();
		pdfPage = (BufferedImage) m_PDFPages.get(new Integer(leftPage));
		if (pdfPage != null) {
			// deriveDeviceCTM(ScalingFactor.FIT_PAGE, m_SplitPanelSize,
			// pdfPage.getWidth(), pdfPage.getHeight(),
			// m_LHSStartPtX, m_StartPtY, true);
			bufG.drawImage(pdfPage, m_LHSPageDisplayCTM, null);
		}

		// Second Drawing the RHS Page into the Buffer
		int rightPage = ((Integer) displayPages.elementAt(1)).intValue();
		pdfPage = (BufferedImage) m_PDFPages.get(new Integer(rightPage));
		if (pdfPage != null) {
			// deriveDeviceCTM(ScalingFactor.FIT_PAGE, m_SplitPanelSize,
			// pdfPage.getWidth(), pdfPage.getHeight(),
			// m_RHSStartPtX, m_StartPtY, true);
			bufG.drawImage(pdfPage, m_RHSPageDisplayCTM, null);
		}

		// Finally Drawing the Animated Page
		Dimension animatedSize = new Dimension(
				(int) m_SplitPanelSize.getWidth(), polygonHt);
		// System.out.println("Panel Size: " + m_SplitPanelSize.toString());
		// System.out.println("Animated Size: " + animatedSize.toString());
		int animatedPage = ((Integer) displayPages.elementAt(2)).intValue();
		pdfPage = (BufferedImage) m_PDFPages.get(new Integer(animatedPage));
		BufferedImage scaleImg = null;
		if (pdfPage != null) {
			scaleImg = ImageUtils.ScaleToSize(pdfPage,
					(int) m_SplitPanelSize.getWidth(), polygonHt);
			// deriveDeviceCTM(ScalingFactor.FIT_PAGE, animatedSize,
			// pdfPage.getWidth(), pdfPage.getHeight(),
			// animatedStartPtX, animatedStartPtY, true);
			bufG.setClip(dispPolygon);
			// bufG.drawPolygon(dispPolygon);
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
	double m_PolygonLimitingFactor = 0.5;
	HashMap m_DisplayPolygons = new HashMap();

	private Polygon createDisplayPolygon(double theta) {
		Polygon dispPolygon = null;
		dispPolygon = (Polygon) m_DisplayPolygons.get(new Double(theta));
		if (dispPolygon != null)
			return dispPolygon;

		int[] x = { 0, 0, 0, 0 };
		int[] y = { 0, 0, 0, 0 };
		// int dispWidth = (int) m_SplitPanelSize.getWidth();
		int dispWidth = (int) m_VisibleDisplaySize.getWidth();
		// m_LHSPageDisplayCTM.getScaleX());
		// int dispHeight = (int) m_SplitPanelSize.getHeight();
		int dispHeight = (int) m_VisibleDisplaySize.getHeight();
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
		m_DisplayPolygons.put(new Double(theta), dispPolygon);
		return dispPolygon;
	}

	private JButton makeNavigationButton(String imageName, String toolTipText) {
		ImageIcon imgIcon = null;
		// Loading the image.
		String imgLocation = "res/" + imageName + ".gif";

		// Create and initialize the button.
		JButton button = new JButton();
		button.setToolTipText(toolTipText);
		try {
			imgIcon = new ImageIcon(imgLocation);
		} catch (Throwable th) {
			String imgPath = "/res/" + imageName + ".gif";
			byte[] buf = ImageUtils.loadImage(imgPath, this.getClass());
			imgIcon = new ImageIcon(Toolkit.getDefaultToolkit()
					.createImage(buf));
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
		this.getNonCachedPages(pdfPages);
		displayDoublePageMode();
	}

	private Vector getNonCachedPageNums(Vector pdfPages) {
		Vector nonCachedPages = null;
		for (int i = 0; i < pdfPages.size(); i++) {
			int pgNum = ((Integer) pdfPages.elementAt(i)).intValue();
			if (pgNum == -1 || pgNum == 0 || pgNum > m_NumberOfPages)
				continue;
			if (m_PDFPages.get(pgNum) != null)
				continue;
			if (nonCachedPages == null)
				nonCachedPages = new Vector();
			nonCachedPages.addElement(pgNum);
		}
		return nonCachedPages;
	}

	private void getNonCachedPages(Vector pdfPages) {
		Vector nonCachedPages = null;
		if (m_PDFPages == null)
			m_PDFPages = new HashMap();
		// This check is done if the page is already downloaded then no need to
		// wait
		// on Synchronized Thread
		nonCachedPages = this.getNonCachedPageNums(pdfPages);
		if (nonCachedPages == null)
			return;
		// Adding non cached page to the Memory
		synchronized (m_PDFView) {
			try {
				// Checking again if the Page is retrived by the earlier threads
				nonCachedPages = this.getNonCachedPageNums(pdfPages);
				if (nonCachedPages == null)
					return;
				HashMap nonCachedPDFPages = m_PDFView
						.getPDFPages(nonCachedPages);
				m_PDFPages.putAll(nonCachedPDFPages);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
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
		bufG.drawImage(pdfPage, m_PageDisplayCTM, null);
	}

	// This method is called to Display Document in Two Page Mode. This is
	// called
	// display 2 Page Document. In Multi Page Document scenario, this methos is
	// called to Display First Page (i.e. RHS Page), Display Last Page (i.e. LHS
	// Page) or the Middle Content (i.e. Double Page Mode).
	public void displayDoublePageMode() {
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
