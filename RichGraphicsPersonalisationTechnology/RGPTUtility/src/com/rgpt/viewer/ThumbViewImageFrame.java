// RGPT PACKAGES
package com.rgpt.viewer;

//import javax.swing.JInternalFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;

import com.rgpt.imageutil.ImageHolder;
import com.rgpt.imageutil.ImageUtils;
import com.rgpt.imageutil.SelectedImageHandler;
import com.rgpt.serverhandler.CustomerAssetRequest;
import com.rgpt.serverhandler.ImageHandlerInterface;
import com.rgpt.serverhandler.ImageUploadInterface;
import com.rgpt.serverutil.PDFPageInfoManager;
import com.rgpt.util.RGPTRectangle;
import com.rgpt.util.ThumbViewImageHandler;

public class ThumbViewImageFrame extends JDialog implements MouseListener,
		MouseMotionListener, WindowFocusListener, WindowStateListener,
		ComponentListener, ImageHandlerInterface, ThumbViewImageHandler

{
	static int openFrameCount = 0;
	static final int xOffset = 30, yOffset = 30;

	// This are Initial set of Images that are shown in the Toolbar
	HashMap m_InitImageAssets;

	// This component display the full image
	private JLabel m_DisplayLabel;
	private JToolBar m_ThumbviewToolBar;

	// This is used to retrieve the enlarged Image from the Server. If Image
	// Holder
	// contains Enlarged Image which is true in Standalone Mode then null can be
	// a
	// valid argument in the constructor.
	PDFPageInfoManager m_PDFPageInfoManager;

	// If further calls are needed to retrieve complete image assets, this is
	// set
	// to false otherwise if the Image Holder has all the assets enlerged, thumb
	// view image, etc this is set to true.
	boolean m_UseImageAssets;

	// This identifies the selected Image Object
	ThumbnailAction m_SelectedImageObject;

	// This Object is used for further processing of Selected Images.
	SelectedImageHandler m_SelectedImageHandler;

	// Windows Toolkit
	Toolkit m_Toolkit;

	// Image Uploader Interface. This is used to loads Images for the user
	ImageUploadInterface m_ImageUploader;

	// This indicates the download mode (either desktop/server) for innitial set
	// of Images.
	int m_InitImageFromMode;

	// This indicates the Current Image From Mode choosen by the User
	int m_CurrImageFromMode;

	// This Vector holds all the Components in the Toolbar
	Vector m_ToolBarComponents;

	// This maintains the Themes supported in this Template;
	Map<Integer, HashMap> m_TemplateThemeInfo;

	// As explained above null can be a valid argument if Image Assets are
	// completely populated. In this case useImageAssets is set to true
	public ThumbViewImageFrame(SelectedImageHandler imgHdlr,
			HashMap imageAssets, ImageUploadInterface imgUploader,
			int downloadMode, Map<Integer, HashMap> templateThemeInfo,
			PDFPageInfoManager pdfPageInfoManager) {
		super(new java.awt.Frame(), "Image Selection Box");
		this.setModal(true);

		// ...Then set the window size or call pack...
		setSize(500, 500);

		m_InitImageAssets = imageAssets;
		m_ImageUploader = imgUploader;
		m_SelectedImageHandler = imgHdlr;
		m_InitImageFromMode = downloadMode;
		m_CurrImageFromMode = m_InitImageFromMode;
		m_PDFPageInfoManager = pdfPageInfoManager;
		m_TemplateThemeInfo = templateThemeInfo;

		m_DisplayLabel = new JLabel();
		m_ThumbviewToolBar = new JToolBar();
		m_ToolBarComponents = new Vector();

		// Set the window's location.
		// setLocation(xOffset*openFrameCount, yOffset*openFrameCount);

		// A label for displaying the pictures
		// JPanel displayPanel = new JPanel();
		m_DisplayLabel.setVerticalTextPosition(JLabel.BOTTOM);
		m_DisplayLabel.setHorizontalTextPosition(JLabel.CENTER);
		m_DisplayLabel.setVerticalAlignment(JLabel.TOP);
		m_DisplayLabel.setHorizontalAlignment(JLabel.LEFT);
		m_DisplayLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		setContentPane(this.createContentPane());
		// We add two glue components. Later in process() we will add thumbnail
		// buttons
		// to the toolbar inbetween thease glue compoents. This will center the
		// buttons in the toolbar.
		m_ThumbviewToolBar.add(Box.createGlue());
		m_ThumbviewToolBar.add(Box.createGlue());

		// Inital Setting to No Selection. This means neither Image Selection
		// mode
		// is set nor the Clip Selection is set
		m_SelectionCriteria = NO_SELECTION;

		addWindowFocusListener(this);
		addWindowStateListener(this);
		addComponentListener(this);

		int res = 0;
		Dimension screenSize = null;
		try {
			m_Toolkit = Toolkit.getDefaultToolkit();
			screenSize = m_Toolkit.getScreenSize();
			res = m_Toolkit.getScreenResolution();
			System.out.println("Res: " + res + " Screen Size: "
					+ screenSize.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// This is popup code to view zoomed Image
		this.initializeZoomWindow();

		// this centers the frame on the screen
		// setLocationRelativeTo(null);

		// start the image loading SwingWorker in a background thread
		m_BackgroundImageLoader.execute();
	}

	public Container getThumbViewUI() {
		return this;
	}

	public void setVisibility(boolean isVisible) {
		this.setVisible(isVisible);
	}

	// This is to show Direction Icon to move Images Horizontally or Vertically
	public BufferedImage getDirectionIcon() {
		return null;
	}

	// This is get the UI Visible
	public boolean getVisibility() {
		return this.isVisible();
	}

	// This is to add Image Files
	public void addImageFile() {
	}

	double m_PrintAspectRatio;
	int m_MinImageWidth, m_MinImageHeight;
	RGPTRectangle m_ImageBBox;

	public void setImageBBox(RGPTRectangle imgBBox) {
		m_ImageBBox = imgBBox;
		HashMap reqImgSize = ImageUtils.getImageSize4PPI(300,
				m_ImageBBox.width / 72, m_ImageBBox.height / 72);
		m_MinImageWidth = ((Integer) reqImgSize.get("Width")).intValue();
		m_MinImageHeight = ((Integer) reqImgSize.get("Height")).intValue();
		m_PrintAspectRatio = m_ImageBBox.height / m_ImageBBox.width;
		if (m_SelectedImageObject == null) {
			showError("Please Upload Images", "No Images Found");
			return;
		}
		if (m_SelectedImageObject.m_DisplayImage == null)
			m_SelectedImageObject.selectDefaultImage();
		System.out.println("Image BBox: " + m_ImageBBox.toString());
		System.out.println("Min Print Wt for 300 PPI: " + m_MinImageWidth
				+ " Ht: " + m_MinImageHeight + " Print Aspect Ratio: "
				+ m_PrintAspectRatio);
	}

	JToolBar m_ImageSelToolPanel;

	public JPanel createContentPane() {
		JPanel contentPane = new JPanel();

		// Set the Layout to Border Layout
		contentPane.setLayout(new BorderLayout());

		// Adding Image Selection Criteria to the Content Pane
		m_ImageSelToolPanel = new JToolBar(JToolBar.VERTICAL);
		m_ImageSelToolPanel.setFloatable(false);
		this.createImageSelectionPanel();
		contentPane.add(m_ImageSelToolPanel, BorderLayout.EAST);

		// Adding Scroll Pane to view Image and laying it in the Center
		// imageScroller = new JScrollPane(m_DisplayLabel,
		// JScrollPane.VERTICAL_SCROLLBAR_NEVER,
		// JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		// imageScroller.setPreferredSize(new Dimension(200, 200));
		// imageScroller.setViewportBorder(BorderFactory.createLineBorder(Color.black));
		// JViewport viewPort = new JViewport();
		// imageScroller.setViewport(viewPort);
		// viewPort.setView(m_DisplayLabel);
		// imageScroller.getVerticalScrollBar().setUnitIncrement(10);
		// contentPane.add(imageScroller, BorderLayout.CENTER);
		contentPane.add(m_DisplayLabel, BorderLayout.CENTER);
		// contentPane.add(m_DisplayLabel, BorderLayout.CENTER);

		// Adding Thumvbiew Images to the Bottom
		JScrollPane imageScroller = null;
		m_ThumbviewToolBar.setMinimumSize(new Dimension(100, 100));
		imageScroller = new JScrollPane(m_ThumbviewToolBar,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		imageScroller.setViewportBorder(BorderFactory
				.createLineBorder(Color.black));
		contentPane.add(imageScroller, BorderLayout.SOUTH);

		// contentPane.add(m_ThumbviewToolBar, BorderLayout.SOUTH);

		m_DisplayLabel.addMouseListener(this);
		m_DisplayLabel.addMouseMotionListener(this);

		System.out.println("The Component Count in ContentPane: "
				+ contentPane.getComponentCount());

		return contentPane;
	}

	JFrame m_ZoomWindow;
	JLabel m_ZoomContent;
	JButton m_SelectClip;

	private void initializeZoomWindow() {
		m_ZoomWindow = new JFrame("Zoom");
		// m_ZoomWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		m_ZoomWindow.setLocation(600, 100);
		// m_ZoomWindow.setSize(new Dimension(40, 40));
		m_ZoomContent = new JLabel();
		m_SelectClip = new JButton("Select Clip Image");
		m_SelectClip.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				selectClip();
			}
		});
		m_SelectClip.setEnabled(false);
		m_ZoomWindow.getContentPane().add(m_SelectClip, BorderLayout.NORTH);
		m_ZoomWindow.getContentPane().add(m_ZoomContent, BorderLayout.CENTER);
		// m_ZoomWindow.setContentPane(m_ZoomContent);
		m_ZoomWindow.setVisible(false);
	}

	private void showZoomContent(BufferedImage img) {
		// Dimension dim = new Dimension(width, height);
		Dimension dim = new Dimension((int) (1.05 * img.getWidth()),
				(int) (img.getHeight() + 50));
		m_ZoomWindow.setSize(dim);
		// m_ZoomContent.setSize(dim);
		ImageIcon iicon = new ImageIcon((java.awt.Image) img);
		m_ZoomContent.setIcon(iicon);
		m_ZoomWindow.setVisible(true);
		// Graphics2D g2d = (Graphics2D) m_ZoomWindow.getGraphics();
		// Graphics g = m_ZoomWindow.getContentPane().getGraphics();
		// if (g2d != null)
		// g2d.drawImage(img, 0, 0, null);
	}

	// Invoked when the component has been made invisible.
	public void componentHidden(ComponentEvent e) {
		System.out.println("Windows Hidden");
		m_ZoomWindow.setVisible(false);
	}

	// Invoked when the component's position changes.
	public void componentMoved(ComponentEvent e) {
		// System.out.println("Windows Moved");
	}

	// Invoked when the component's size changes.
	public void componentResized(ComponentEvent e) {
		System.out.println("Windows Resized");
		if (m_SelectedImageObject == null)
			return;
		m_SelectedImageObject.displayImage();
		System.out.println("Display Visible Bounds: "
				+ m_DisplayLabel.getVisibleRect().toString());
		System.out.println("Display Bounds: "
				+ m_DisplayLabel.getBounds().toString());
		m_DisplayLabel.getBounds(m_DisplaySize);
		// m_DisplaySize.translate((int) m_DisplaySize.getX(), (int)
		// m_DisplaySize.getY());
	}

	// Invoked when the component has been made visible.
	public void componentShown(ComponentEvent e) {
		System.out.println("Windows Shown");
		// this.setModal(true);
	}

	// When the user presses the mouse, record the location of the top-left
	// corner of rectangle.
	public int m_ImageSelStartX, m_ImageSelStartY;
	public int m_ImageSelEndX, m_ImageSelEndY;
	public int m_ImageSelWidth = 0, m_ImageSelHeight = 0;

	Rectangle m_DisplaySize = new Rectangle();

	public void windowStateChanged(WindowEvent e) {
		System.out.println("Windows State Chenged");
	}

	public void windowGainedFocus(WindowEvent e) {
		System.out.println("Focus Gained");
		if (m_SelectedImageObject == null) {
			System.out.println("Selected Image Object is Null");
			return;
		}
		m_SelectedImageObject.displayImage();
		m_DisplayLabel.getBounds(m_DisplaySize);
		// m_DisplaySize.translate((int) m_DisplaySize.getX(), (int)
		// m_DisplaySize.getY());
		m_FocusGained = true;
		// this.setVisible(true);
		repaint();
	}

	boolean m_FocusGained = false;

	public void windowLostFocus(WindowEvent e) {
		// m_MouseExited = true;
		// m_FocusLost = true;
		System.out.println("Focus Lost");
		// showError("Focus is getting Lost", "Focus Lost");
	}

	// This is used to view zoomed or cliped image on the Zoomed Image Panel
	public void mouseMoved(MouseEvent event) {
		BufferedImage origImg = null, selImg = null, dispimg = null;
		if (m_MouseExited) {
			m_MouseExited = false;
			return;
		}
		if (m_SelectionCriteria == NO_SELECTION)
			return;
		if (m_SelectionCriteria == ZOOM_SELECTION && m_RectZoomSelected) {
			System.out.println("Mouse Moved: " + m_SelectionCriteria);
			int x = event.getX();
			int y = event.getY();
			// System.out.println("Display Size: " + m_DisplaySize.toString());
			// System.out.println("x: " + x + " y: " + y);

			// If X, Y points is outside Display Rectangle, nothing is done
			if (!m_DisplaySize.contains(x, y)
					|| !m_SelectedImageObject.m_DisplayRect.contains(x, y))
				return;

			int startx = x - m_ImageSelWidth;
			int starty = y - m_ImageSelHeight;
			// System.out.println("startx: " + startx + " :stry: " + starty);
			// System.out.println("width: " + m_ImageSelWidth + " :Ht: " +
			// m_ImageSelHeight);
			// Nothing is done if the Rectangular Bounds of Zoomed Rectangle is
			// not within the Display Size
			if (!m_DisplaySize.contains(startx, starty)
					|| !m_SelectedImageObject.m_DisplayRect.contains(startx,
							starty))
				return;

			// The zoom rectangle is contained within the Display Area
			// Calculating the Image Coordinates
			// x = x - (int) m_DisplaySize.getX();
			// y = y - (int) m_DisplaySize.getY();
			// startx = x - m_ImageSelWidth;
			// starty = y - m_ImageSelHeight;

			// Hide the Old Recatngle by setting the XORMode
			// Graphics g = getGraphics();
			Graphics g = m_DisplayLabel.getGraphics();
			// If this window regains the focus then there is no old rectangle
			// to erase
			if (!m_FocusGained) {
				g.setXORMode(Color.lightGray);
				drawRectangle(g, m_ImageSelStartX, m_ImageSelStartY,
						m_ImageSelEndX, m_ImageSelEndY);

			} else {
				m_FocusGained = false;
				System.out
						.println("Mouse Moved Inside: " + m_SelectionCriteria);
			}
			// g.drawRect(m_ImageSelStartX, m_ImageSelStartY,
			// m_ImageSelWidth, m_ImageSelHeight);

			// Drawing the New Rectangle
			m_ImageSelStartX = startx;
			m_ImageSelStartY = starty;
			m_ImageSelEndX = x;
			m_ImageSelEndY = y;

			// Drawing the New Zoom Rectangle to the new Moved Position
			g.setXORMode(Color.lightGray);
			drawRectangle(g, m_ImageSelStartX, m_ImageSelStartY,
					m_ImageSelEndX, m_ImageSelEndY);
			// System.out.println("startx: " + m_ImageSelStartX + " starty: " +
			// m_ImageSelStartY +
			// ":endx: " + m_ImageSelEndX + " endy: " + m_ImageSelEndY);

			// selImg = m_SelectedImageObject.getBufferedImage();
			origImg = m_SelectedImageObject.m_DisplayImage;
			selImg = m_SelectedImageObject.m_ScaledImage;
			int left = m_SelectedImageObject.m_LeftMargin;
			int top = m_SelectedImageObject.m_TopMargin;
			// m_TopMargin m_LeftMargin m_DisplayRect
			dispimg = ImageUtils.getSelectedImage(origImg, selImg,
					m_ImageSelStartX, m_ImageSelStartY, m_ImageSelEndX,
					m_ImageSelEndY, left, top, m_ClippedRectangle);
			System.out.println("Clip Rect: " + m_ClippedRectangle.toString());
			// dispimg = ImageUtils.getSelectedImage(selImg, m_ImageSelStartX,
			// m_ImageSelStartY, m_ImageSelEndX,
			// m_ImageSelEndY, (int) m_DisplaySize.getX(),
			// (int) m_DisplaySize.getY());
			if (dispimg == null)
				return;
			m_ClippedImage = dispimg;
			BufferedImage scalImg = ImageUtils.ScaleToSize(dispimg,
					(int) (1.5 * m_ImageSelWidth),
					(int) (1.5 * m_ImageSelHeight));
			if (scalImg != null)
				showZoomContent(scalImg);
			// showZoomContent(scalImg, 40, 40);
		}
	}

	// This draws a rubberband rectangle, from the location where the mouse was
	// first clicked to the location where the mouse is dragged.
	public void mouseDragged(MouseEvent event) {
		if (m_SelectionCriteria == NO_SELECTION)
			return;
		if ((m_SelectionCriteria == ZOOM_SELECTION || m_SelectionCriteria == CLIP_SELECTION)
				&& !m_RectZoomSelected) {
			System.out.println("Mouse Dragged: " + m_SelectionCriteria);
			int x = event.getX();
			int y = event.getY();
			System.out.println("Display Size: " + m_DisplaySize.toString());
			System.out.println("Sel Image Size: "
					+ m_SelectedImageObject.m_DisplayRect.toString());
			System.out.println("Raw X, Y Pts are: " + x + " " + y);
			// If X, Y points is outside Display Rectangle, nothing is done
			if (!m_DisplaySize.contains(x, y)
					|| !m_SelectedImageObject.m_DisplayRect.contains(x, y))
				return;
			Graphics g = m_DisplayLabel.getGraphics();
			// Graphics g = getGraphics();
			g.setXORMode(Color.lightGray);
			// Drawing Rectangle twice gives the Rubberband Effect
			System.out
					.println("Rubbing the old Rect at StartX: "
							+ m_ImageSelStartX + " :StartY: "
							+ m_ImageSelStartY + " :EndX: " + m_ImageSelEndX
							+ " :EndY: " + m_ImageSelEndY);
			drawRectangle(g, m_ImageSelStartX, m_ImageSelStartY,
					m_ImageSelEndX, m_ImageSelEndY);

			// New code to Drag along the Aspect Ratio
			int w = x - m_ImageSelStartX;
			int h = (int) (w * m_PrintAspectRatio);
			y = m_ImageSelStartY + h;
			// if (x > m_SelectedImageObject.m_ScaledImage.getWidth() ||
			// y > m_SelectedImageObject.m_ScaledImage.getHeight())
			if (!m_SelectedImageObject.m_DisplayRect.contains(x, y)) {
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
		}
		if (m_SelectionCriteria == CLIP_SELECTION)
			return;
	}

	// Recording initial start points
	public void mousePressed(MouseEvent event) {
		// If no Image is Selected No processing is done
		if (m_SelectedImageObject.m_DisplayImage == null)
			return;
		if (m_SelectionCriteria == NO_SELECTION)
			return;
		if ((m_SelectionCriteria == ZOOM_SELECTION || m_SelectionCriteria == CLIP_SELECTION)
				&& !m_RectZoomSelected) {
			System.out.println("Mouse Pressed: " + m_SelectionCriteria);
			int x = event.getX();
			int y = event.getY();
			// System.out.println("Mouse Pressed x: " + x + " :y: " + y);
			// If X, Y points is outside Display Rectangle, nothing is done
			if (!m_DisplaySize.contains(x, y))
				return;

			// Image Coordinate System
			m_ImageSelStartX = x;
			m_ImageSelStartY = y;
			m_ImageSelEndX = m_ImageSelStartX;
			m_ImageSelEndY = m_ImageSelStartY;
			// System.out.println("Mouse Pressed Img Start x: " +
			// m_ImageSelStartX +
			// " :Img Start y: " + m_ImageSelStartY);
		}
		if (m_SelectionCriteria == CLIP_SELECTION)
			return;
	}

	// Erase the last rectangle when the user releases the mouse.
	public void mouseReleased(MouseEvent event) {
		BufferedImage origImg = null;
		BufferedImage selImg = null;
		BufferedImage dispimg = null;
		// If no Image is Selected No processing is done
		if (m_SelectedImageObject.m_DisplayImage == null)
			return;
		if (m_SelectionCriteria == NO_SELECTION)
			return;
		if ((m_SelectionCriteria == ZOOM_SELECTION || m_SelectionCriteria == CLIP_SELECTION)
				&& !m_RectZoomSelected) {
			System.out.println("Mouse Released: " + m_SelectionCriteria);
			m_ImageSelWidth = Math.abs(m_ImageSelStartX - m_ImageSelEndX);
			m_ImageSelHeight = Math.abs(m_ImageSelStartY - m_ImageSelEndY);
			// m_OrigImageWidth m_OrigImageHeight
			if (m_ImageSelWidth < 50 || m_ImageSelHeight < 50) {
				showError("Please Select Larger Zoom Size",
						"Zoom Selection Error");
				// showError("Please Select Minimum Width: " + m_MinImageWidth +
				// " And Height: " + m_MinImageHeight, "Zoom Selection Error");
				return;
			}

			// Re-Calculating the Aspect Ratio based on Ht and Width and
			// readjusting
			// the End Position
			/*
			 * Graphics g = m_DisplayLabel.getGraphics();
			 * g.setXORMode(Color.lightGray); drawRectangle(g, m_ImageSelStartX,
			 * m_ImageSelStartY, m_ImageSelEndX, m_ImageSelEndY);
			 * g.setXORMode(Color.lightGray); m_ImageSelHeight = (int)
			 * (m_ImageSelWidth * m_PrintAspectRatio);
			 * //m_SelectedImageObject.m_AspectRatio); m_ImageSelEndY =
			 * m_ImageSelStartY + m_ImageSelHeight; drawRectangle(g,
			 * m_ImageSelStartX, m_ImageSelStartY, m_ImageSelEndX,
			 * m_ImageSelEndY);
			 */
			m_FocusGained = false;
			m_RectZoomSelected = true;
			// System.out.println("MR: startx: " + m_ImageSelStartX +
			// " starty: " + m_ImageSelStartY +
			// ":endx: " + m_ImageSelEndX + " endy: " + m_ImageSelEndY);
			// selImg = m_SelectedImageObject.getBufferedImage();
			selImg = m_SelectedImageObject.m_ScaledImage;
			origImg = m_SelectedImageObject.m_DisplayImage;
			m_ClippedRectangle = new Rectangle();
			int left = m_SelectedImageObject.m_LeftMargin;
			int top = m_SelectedImageObject.m_TopMargin;
			// m_TopMargin m_LeftMargin m_DisplayRect
			System.out.println("Calling Clip Rect: startx: " + m_ImageSelStartX
					+ " starty: " + m_ImageSelStartY + ":endx: "
					+ m_ImageSelEndX + " endy: " + m_ImageSelEndY);
			dispimg = ImageUtils.getSelectedImage(origImg, selImg,
					m_ImageSelStartX, m_ImageSelStartY, m_ImageSelEndX,
					m_ImageSelEndY, left, top, m_ClippedRectangle);
			System.out.println("Clip Rect: " + m_ClippedRectangle.toString());
			if (dispimg == null) {
				showError("Reselect Image within the Image Area",
						"Image Selection Error");
				return;
			}
			m_ClippedImage = dispimg;
			BufferedImage scalImg = ImageUtils.ScaleToSize(dispimg,
					(int) (1.5 * m_ImageSelWidth),
					(int) (1.5 * m_ImageSelHeight));
			// m_ClippedImage = scalImg;
			if (scalImg != null)
				showZoomContent(scalImg);

			// If Selection Criterial is Clip the the Selected Rectangle is
			// Clipped
			if (m_SelectionCriteria == CLIP_SELECTION) {
				m_ClipButton.setEnabled(true);
				m_SelectClip.setEnabled(true);
			} else {
				m_ClipButton.setEnabled(false);
				m_SelectClip.setEnabled(false);
			}
			return;
		}
	}

	public void mouseEntered(MouseEvent event) {
	}

	public void mouseExited(MouseEvent event) {
		System.out.println("Mouse Exited: " + m_SelectionCriteria);
		m_MouseExited = true;
		// m_SelectionCriteria = NO_SELECTION;
	}

	public void mouseClicked(MouseEvent event) {
		System.out.println("Mouse Clicked: " + m_SelectionCriteria);
		System.out.println("Mouse Button: " + event.getButton());
		// If no Image is Selected No processing is done
		if (m_SelectedImageObject.m_DisplayImage == null)
			return;
		if (m_SelectionCriteria == NO_SELECTION) {
			if (event.getClickCount() == 2) {
				processSelectedImage();
			}
			return;
		}
		if ((event.getButton() == 3)
				&& (m_SelectionCriteria == ZOOM_SELECTION || m_SelectionCriteria == CLIP_SELECTION)) {
			selectClip();
			return;
		}
		// m_MouseClicked = true;
		/*
		 * if ((m_SelectionCriteria == ZOOM_SELECTION || m_SelectionCriteria ==
		 * CLIP_SELECTION) && !m_RectZoomSelected) { int x = event.getX(); int y
		 * = event.getY(); System.out.println("Mouse Pressed x: " + x + " :y: "
		 * + y); // If X, Y points is outside Display Rectangle, nothing is done
		 * if (!m_DisplaySize.contains(x, y)) return;
		 * 
		 * // Image Coordinate System m_ImageSelStartX = x; m_ImageSelStartY =
		 * y; m_ImageSelEndX = m_ImageSelStartX; m_ImageSelEndY =
		 * m_ImageSelStartY; System.out.println("Mouse Pressed Img x: " +
		 * m_ImageSelStartX + " :Imgy: " + m_ImageSelStartY); return; }
		 */
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

	public void repaintContent() {
		this.repaint();
	}

	public void showBusyCursor() {
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
	}

	protected void setDefaultCursor() {
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public static final int NO_SELECTION = 0;
	public static final int ZOOM_SELECTION = 1;
	public static final int CLIP_SELECTION = 2;
	public int m_SelectionCriteria;
	public BufferedImage m_ClippedImage;
	public Rectangle m_ClippedRectangle;
	boolean m_MouseExited = false;
	boolean m_RectZoomSelected = false;
	JButton m_ClipButton;

	private void createImageSelectionPanel() {
		int hgap = 10, vgap = 5;
		// System.out.println("Create Image Selection Panel");
		// JButton button = new JButton("Select Zoom Area");
		// button.setToolTipText("Select Zoom Rectangle");
		m_ImageSrcButtons = new HashMap<Integer, JButton>();
		JButton button = makeNavigationButton("fileopen", "Open");
		m_ImageSrcButtons.put(ImageHandlerInterface.UPLOAD_DESKTOP_IMAGES,
				button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					System.out.println("Is Model Bfor: "
							+ ThumbViewImageFrame.this.isModal());
					ThumbViewImageFrame.this.setModal(false);
					System.out.println("Is Model after: "
							+ ThumbViewImageFrame.this.isModal());
					m_ImageUploader
							.addImageFile(ThumbViewImageFrame.this, true);
				} catch (Exception ex) {
					ex.printStackTrace();
					showError("Unable to Add File", "Upload File Error");
				}
			}
		});
		m_ImageSelToolPanel.add(button);

		button = makeNavigationButton("zoom", "Select Zoom Rectangle");
		m_ImageSelToolPanel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				m_SelectionCriteria = ZOOM_SELECTION;
				m_RectZoomSelected = false;
				m_ImageSelStartX = 0;
				m_ImageSelStartY = 0;
				m_ImageSelEndX = 0;
				m_ImageSelEndY = 0;
				m_ImageSelWidth = 0;
				m_ImageSelHeight = 0;
				m_ZoomWindow.setVisible(false);
				setResizable(false);
				repaintContent();
			}
		});

		button = makeNavigationButton("clip", "Select Clip Rectangle");
		m_ImageSelToolPanel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				m_SelectionCriteria = CLIP_SELECTION;
				m_RectZoomSelected = false;
				m_ImageSelStartX = 0;
				m_ImageSelStartY = 0;
				m_ImageSelEndX = 0;
				m_ImageSelEndY = 0;
				m_ImageSelWidth = 0;
				m_ImageSelHeight = 0;
				m_ZoomWindow.setVisible(false);
				setResizable(true);
				repaintContent();
			}
		});

		button = makeNavigationButton("unselect", "Remove all Selection");
		m_ImageSelToolPanel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				m_SelectionCriteria = NO_SELECTION;
				m_ZoomWindow.setVisible(false);
				setResizable(true);
				repaintContent();
			}
		});

		m_ClipButton = makeNavigationButton("selClip", "Select Clip Image");
		m_ClipButton.setEnabled(false);
		m_ImageSelToolPanel.add(m_ClipButton);
		m_ClipButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				selectClip();
			}
		});

		button = makeNavigationButton("desktop", "Show Desktop Images");
		m_ImageSelToolPanel.add(button);
		m_ImageSrcButtons.put(ImageHandlerInterface.DESKTOP_IMAGES, button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Setting the Visiblity of Toolbar Components to Show Desktop
				// Images
				m_CurrImageFromMode = ImageHandlerInterface.DESKTOP_IMAGES;
				resetToolBarComponents();
			}
		});

		// The Server Mode and Theme Mode is disabled if the Init Mode is the
		// Desktop Mode
		if (m_InitImageFromMode == ImageHandlerInterface.DESKTOP_IMAGES)
			return;

		button = makeNavigationButton("server", "Show Server Images");
		m_ImageSrcButtons.put(ImageHandlerInterface.SERVER_IMAGES, button);
		m_ImageSelToolPanel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Setting the Visiblity of Toolbar Components to Show Desktop
				// Images
				m_CurrImageFromMode = ImageHandlerInterface.SERVER_IMAGES;
				resetToolBarComponents();
			}
		});

		if (m_TemplateThemeInfo == null)
			return;

		HashMap themes = null;
		Integer[] themeIds = m_TemplateThemeInfo.keySet().toArray(
				new Integer[0]);
		for (int i = 0; i < themeIds.length; i++) {
			themes = m_TemplateThemeInfo.get(themeIds[i]);
			String toolTipText = (String) themes.get("ThemeName");
			button = makeNavigationButton("server", toolTipText);
			int imageSrcId = this.getThemeImageSrcId(themeIds[i]);
			m_ImageSrcButtons.put(imageSrcId, button);
			m_ImageSelToolPanel.add(button);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					// Setting the Visiblity of Toolbar Components to Show
					// Desktop Images
					m_CurrImageFromMode = getSelectedImageSource((JButton) arg0
							.getSource());
					resetToolBarComponents();
				}
			});
		}
	}

	Map<Integer, JButton> m_ImageSrcButtons; // m_ImageSrcButtons

	// This method is to Activate or Deactivate the other Image Sources based on
	// the Theme Selected and settings of the VDP Image Field
	public void manageImageSrcActivation(int themeId, boolean allowImgUpld) {
		JButton imgSrcButton = null;
		int selImageSrcId = -1;
		if (themeId != -1)
			selImageSrcId = this.getThemeImageSrcId(themeId);
		Integer[] imgSrcIds = m_ImageSrcButtons.keySet()
				.toArray(new Integer[0]);
		for (int i = 0; i < imgSrcIds.length; i++) {
			int imgSrcId = imgSrcIds[i].intValue();
			imgSrcButton = m_ImageSrcButtons.get(imgSrcIds[i]);
			// Unabling all by default
			imgSrcButton.setEnabled(true);
			if (themeId == -1)
				continue;
			if (selImageSrcId == imgSrcId)
				continue;
			// If Other Sources are allowed then only Themes that are not used
			// for
			// the current selected Image are deactivated. Else all the Source
			// except
			// the theme selected is deactivated.
			if (allowImgUpld) {
				// if (imgSrcId < ImageHandlerInterface.THEME_IMAGES)
				continue;
			}
			imgSrcButton.setEnabled(false);
		}
	}

	public int getSelectedImageSource(JButton selButton) {
		Integer[] imgSrcIds = m_ImageSrcButtons.keySet()
				.toArray(new Integer[0]);
		for (int i = 0; i < imgSrcIds.length; i++) {
			int imgSrcId = imgSrcIds[i].intValue();
			if (m_ImageSrcButtons.get(imgSrcIds[i]) == selButton)
				return imgSrcId;
		}
		throw new RuntimeException("Invalid Image Source Selection");
	}

	public int getThemeImageSrcId(int themeId) {
		return ImageHandlerInterface.THEME_IMAGES + themeId;
	}

	public int getThemeIdFromImageSrc(int imgSrcId) {
		// Returns -1 for Images that are not Theme Images
		if (imgSrcId < ImageHandlerInterface.THEME_IMAGES)
			return -1;
		return imgSrcId - ImageHandlerInterface.THEME_IMAGES;
	}

	private void resetToolBarComponents() {
		Vector toolBarDispComps = null;
		toolBarDispComps = setToolBarCompsVisibility(m_CurrImageFromMode, true);
		resetToolBarDisplayComp(toolBarDispComps);
		// Will show only the Images based on cyrrent image from setting
		m_ThumbviewToolBar.updateUI();
		repaintContent();
	}

	private void selectClip() {
		m_SelectionCriteria = CLIP_SELECTION;
		m_ZoomWindow.setVisible(false);
		processSelectedImage();
		repaintContent();
	}

	private JButton makeNavigationButton(String imageName, String toolTipText) {
		boolean useJarResource = true;
		ImageIcon imgIcon = null;
		// Loading the image.
		String imgLocation = "res/" + imageName + ".gif";
		// System.out.println("In ThumbviewImage makeNavigationButton: " +
		// imgLocation);

		// Create and initialize the button.
		JButton button = new JButton();
		button.setToolTipText(toolTipText);
		try {
			imgIcon = new ImageIcon(imgLocation);
			Image img = imgIcon.getImage();
			if (img.getWidth(null) <= 0 || img.getHeight(null) <= 0)
				useJarResource = true;
			else
				useJarResource = false;
		} catch (Throwable th) {
			useJarResource = true;
			// System.out.println("Exception Thrown: " + th.toString());
			th.printStackTrace();
		}
		if (useJarResource) {
			String imgPath = "/res/" + imageName + ".gif";
			// System.out.println("Image Path: " + imgPath);
			byte[] buf = ImageUtils.loadImage(imgPath, this.getClass());
			// byte[] buf = ImageUtils.loadImage(imgPath, m_ResClass);
			// System.out.println("Image Size of Button Icon: " + buf.length);
			imgIcon = new ImageIcon(Toolkit.getDefaultToolkit()
					.createImage(buf));
			// System.out.println("Image Icon: " + imgIcon.toString());
		}
		if (imgIcon != null)
			button.setIcon(imgIcon);
		else
			button.setName(imageName);
		return button;
	}

	// This function is called to handle Images downloaded for various themes.
	public void handleThemeImages(Map<Integer, Vector> themeImages)
			throws Exception {
		Vector<ImageHolder> themeImageHldrs = null;
		Integer[] themeIds = themeImages.keySet().toArray(new Integer[0]);
		for (int i = 0; i < themeIds.length; i++) {
			themeImageHldrs = themeImages.get(themeIds[i]);
			int imageSrc = this.getThemeImageSrcId(themeIds[i]);
			this.handleImagesFromSource(themeImageHldrs, imageSrc, false);
		}
	}

	// This function is called by the ImageUploadImterface to handle the new
	// images uploaded from the user machine.
	public void handleImagesUploaded(Vector imageHolders, int imageDownloadMode)
			throws Exception {
		this.handleImagesFromSource(imageHolders, imageDownloadMode, true);
		this.setDefaultCursor();
	}

	// This function is called by the ImageUploadImterface to handle the new
	// images uploaded from the user machine.
	private void handleImagesFromSource(Vector imageHolders, int imageSrc,
			boolean isDesktoImage) throws Exception {
		ImageHolder imgHldr = null;
		ImageIcon thumbnailIcon = null;
		ThumbnailAction thumbAction = null;

		System.out.println("Number of New Images Uploaded are: "
				+ imageHolders.size());
		// Resetting all the Toolbar Components to False to Show the Newly Added
		// Components
		this.setToolBarCompsVisibility(-1, false);

		Vector toolBarDispComps = new Vector();
		// Adding the Newly Added Components
		for (int i = 0; i < imageHolders.size(); i++) {
			imgHldr = (ImageHolder) imageHolders.elementAt(i);
			System.out.println("Image Uploaded: " + imgHldr.toString());
			if (isDesktoImage)
				thumbnailIcon = new ImageIcon(imgHldr.m_ThumbviewImage,
						imgHldr.m_ImgCaption);
			else
				thumbnailIcon = new ImageIcon(ImageUtils.loadImage(
						imgHldr.m_ImgStr, 100), imgHldr.m_ImgCaption);
			thumbAction = new ThumbnailAction(imgHldr, thumbnailIcon, imageSrc);
			addThumbButton(thumbAction);
			toolBarDispComps.addElement(thumbAction);
			m_ToolBarComponents.addElement(thumbAction);
		}
		// Will show only the newly Uploaded Images
		System.out.println("The Number of Uploaded Images to be displayed is: "
				+ toolBarDispComps.size());
		this.resetToolBarDisplayComp(toolBarDispComps);
		System.out.println("Reset ToolBar Display Component Successfully");
		this.setModal(true);
		m_ThumbviewToolBar.updateUI();
		repaintContent();
	}

	private void resetToolBarDisplayComp(Vector toolBarDispComps) {
		ThumbnailAction thumbAction = null, defaultThumbIcon = null;
		m_ThumbviewToolBar.removeAll();
		System.out.println("Toolbar Component count after Removing All: "
				+ m_ThumbviewToolBar.getComponentCount());
		for (int i = 0; i < toolBarDispComps.size(); i++) {
			thumbAction = (ThumbnailAction) toolBarDispComps.elementAt(i);
			if (i == 0)
				defaultThumbIcon = thumbAction;
			thumbAction.m_ImageButton.setVisible(true);
			m_ThumbviewToolBar.add(thumbAction.m_ImageButton,
					m_ThumbviewToolBar.getComponentCount() - 1);
		}
		if (defaultThumbIcon != null)
			defaultThumbIcon.selectDefaultImage();
		System.out
				.println("Toolbar Component After Adding the Necessary Comps: "
						+ m_ThumbviewToolBar.getComponentCount());
	}

	// If ImgFromMode is set to -1 then all the ToolBar Components are reset.
	private Vector setToolBarCompsVisibility(int imgFromMode, boolean visibility) {
		ThumbnailAction thumbAction = null;
		Vector toolBarDispComps = new Vector();
		for (int i = 0; i < m_ToolBarComponents.size(); i++) {
			thumbAction = (ThumbnailAction) m_ToolBarComponents.elementAt(i);
			if (imgFromMode == -1 || imgFromMode == thumbAction.m_ImageFromMode) {
				thumbAction.m_ImageButton.setVisible(visibility);
			} else {
				// Setting the Visibility to otherwise
				if (visibility)
					thumbAction.m_ImageButton.setVisible(false);
				else
					thumbAction.m_ImageButton.setVisible(true);
			}
			if (thumbAction.m_ImageButton.isVisible())
				toolBarDispComps.addElement(thumbAction);
		}
		return toolBarDispComps;
	}

	/**
	 * SwingWorker class that loads the images a background thread and calls
	 * publish when a new one is ready to be displayed.
	 * 
	 * We use Void as the first SwingWroker param as we do not need to return
	 * anything from doInBackground().
	 */
	private SwingWorker<Void, ThumbnailAction> m_BackgroundImageLoader = new SwingWorker<Void, ThumbnailAction>() {

		// This method is invoked when execute methos id called on SwingWorker
		// Object
		// Creates thumbnail versions of the target image files.
		@Override
		protected Void doInBackground() throws Exception {
			if (m_InitImageAssets == null || m_InitImageAssets.size() == 0)
				return null;
			Object[] assetIds = m_InitImageAssets.keySet().toArray();
			System.out.println("Total Initial Images to be loaded: "
					+ m_InitImageAssets.size());
			ThumbnailAction defaultThumbIcon = null;
			for (int i = 0; i < assetIds.length; i++) {
				int assetId = ((Integer) assetIds[i]).intValue();
				ThumbnailAction thumbAction;
				System.out.println("Image Id to be added: " + assetId);
				ImageHolder imgHldr = (ImageHolder) m_InitImageAssets
						.get(assetId);
				System.out.println("Image Holder to be added: " + imgHldr);
				ImageIcon thumbnailIcon = null;
				// if (imgHldr.m_UseCachedImages)
				if (m_InitImageFromMode == ImageHandlerInterface.DESKTOP_IMAGES)
					thumbnailIcon = new ImageIcon(imgHldr.m_ThumbviewImage,
							imgHldr.m_ImgCaption);
				else
					thumbnailIcon = new ImageIcon(ImageUtils.loadImage(
							imgHldr.m_ImgStr, 100), imgHldr.m_ImgCaption);
				thumbAction = new ThumbnailAction(imgHldr, thumbnailIcon,
						m_InitImageFromMode);
				m_ToolBarComponents.addElement(thumbAction);
				if (i == 0)
					defaultThumbIcon = thumbAction;
				// This incalls the process method on this object
				publish(thumbAction);
			}
			if (defaultThumbIcon != null)
				defaultThumbIcon.selectDefaultImage();
			m_ThumbviewToolBar.updateUI();
			repaintContent();
			return null;
		}

		/**
		 * Process all loaded images.
		 */
		@Override
		protected void process(List<ThumbnailAction> chunks) {
			for (ThumbnailAction thumbAction : chunks) {
				addThumbButton(thumbAction);
			}
		}
	};

	public void addThumbButton(ThumbnailAction thumbAction) {
		// System.out.println("Adding thumbAction: \n" +
		// thumbAction.m_ImgHldr.toString());
		JButton thumbButton = new JButton(thumbAction);
		thumbAction.addImageButton(thumbButton);
		thumbButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					// System.out.println(" double click" );
					processSelectedImage();
				}
			}
		});

		// add the new button BEFORE the last glue
		// this centers the buttons in the toolbar
		// System.out.println("Adding Image to Toolbar: " +
		// thumbAction.m_ImgHldr.m_AssetId);
		// System.out.println("The Component Count: " +
		// m_ThumbviewToolBar.getComponentCount());
		m_ThumbviewToolBar.add(thumbButton,
				m_ThumbviewToolBar.getComponentCount() - 1);
	}

	public boolean m_DoAutoCrop = false;

	// public String m_SelectedImagePath;
	private void processSelectedImage() {
		m_ZoomWindow.setVisible(false);
		if (m_SelectedImageObject.m_DisplayImage == null)
			return;
		BufferedImage dispimg = null;
		BufferedImage selectedImage = m_SelectedImageObject.m_DisplayImage;
		BufferedImage origImg = selectedImage;
		boolean isClipped = false;
		// m_SelectedImageObject.m_ImgHldr.m_UseCachedImages
		if (m_SelectionCriteria == CLIP_SELECTION && m_ClippedImage != null) {
			selectedImage = m_ClippedImage;
			isClipped = true;
			int ppi = ImageUtils.FindResolution(selectedImage,
					m_ImageBBox.width / 72, m_ImageBBox.height / 72);
			if (ppi < 300) {
				showError("The Image Resolution is: " + ppi + " vs: 300."
						+ " Select Higher Image Size", "Low Resolution");
			}
			double scale = m_SelectedImageObject.m_Scale;
			// Resetting the Clip Rectangle based on High Res Image
			int x = (int) (m_ClippedRectangle.getX() / scale);
			int y = (int) (m_ClippedRectangle.getY() / scale);
			int w = (int) (m_ClippedRectangle.getWidth() / scale);
			int h = (int) (m_ClippedRectangle.getHeight() / scale);
			m_ClippedRectangle = new Rectangle(x, y, w, h);
			System.out.println("Resetting Clip Rect: "
					+ m_ClippedRectangle.toString());
		}
		// Auto Cropping
		double percARError = m_SelectedImageObject.m_AspectRatio
				/ m_PrintAspectRatio;
		System.out.println("AR Error 4 Image: "
				+ m_SelectedImageObject.m_AspectRatio + " and Print: "
				+ m_PrintAspectRatio + " is " + percARError);
		if (!isClipped && (percARError < 0.8 || percARError > 1.2)) {
			String mesg = "The Selected Image will be distorted. Use Crop "
					+ "Capability for better Print.";
			String title = "Aspect Ratio Issue";
			int mesgType = JOptionPane.INFORMATION_MESSAGE;
			Object[] possibleValues = { "Cancel", "Continue", "Auto Crop" };
			Object selectedValue = null;
			selectedValue = JOptionPane.showInputDialog(this, mesg, title,
					mesgType, null, possibleValues, possibleValues[0]);
			String selVal = (String) selectedValue;
			if (selVal.equals("Cancel"))
				return;
			if (selVal.equals("Auto Crop"))
				selectedImage = this.performAutoCrop();
			// Auto Cropping
			// m_MinImageWidth m_MinImageHeight
		}
		// Clipped Rectangle is null if the Image is not Clipped
		int imgFromMode = m_SelectedImageObject.m_ImageFromMode;
		ImageHolder imgHldr = m_SelectedImageObject.m_ImgHldr;
		Rectangle clipRect = m_ClippedRectangle;

		// Populating Image Holder with essential data
		imgHldr.m_IsClipped = isClipped;
		imgHldr.m_ImageBBox = m_ImageBBox;
		imgHldr.m_ImageFromMode = imgFromMode;
		imgHldr.m_ClipRectangle = m_ClippedRectangle;
		try {
			if (imgFromMode == ImageHandlerInterface.DESKTOP_IMAGES
					&& imgHldr.m_UseCachedImages) {
				BufferedImage buffImg = null;
				buffImg = ImageUtils
						.getPrintableImage(imgHldr, IMAGE_PRINT_DPI);
				imgHldr.m_ImgStr = ImageUtils.getImageStream(buffImg, "PNG");
				System.out.println("Selected Image Length: "
						+ imgHldr.m_ImgStr.length);
			}
			m_SelectedImageHandler.updateImageData(selectedImage, imgHldr);
		} catch (Exception ex) {
			ex.printStackTrace();
			showError("Unable to Load Image", "Image Loading Error");
		}
		return;
	}

	private BufferedImage performAutoCrop() {
		BufferedImage dispimg = null;
		int startx = 0, starty = 0, offset = 0, endx = 0, endy = 0, wt = 0, ht = 0;
		boolean useWt = false, useHt = false;
		m_ClippedRectangle = new Rectangle();
		BufferedImage origImg = m_SelectedImageObject.m_DisplayImage;
		int origwd = origImg.getWidth();
		int oright = origImg.getHeight();
		if (m_SelectedImageObject.m_ImagePPI >= 300) {
			wt = (int) (m_MinImageWidth);
			ht = (int) (wt * m_SelectedImageObject.m_AspectRatio);
			startx = (int) (origwd - wt) / 2;
			starty = (int) (oright - ht) / 2;
		} else {
			System.out.println("Calc Wt and Ht for PPI: "
					+ m_SelectedImageObject.m_ImagePPI);
			wt = (int) (m_SelectedImageObject.m_MinImageWidth * m_SelectedImageObject.m_Scale);
			ht = (int) (wt * m_SelectedImageObject.m_AspectRatio);
			startx = (int) (origwd - wt) / 2;
			starty = (int) (oright - ht) / 2;
		}
		System.out.println("New startx=" + startx + " starty=" + starty
				+ " Wt=" + wt + " Ht=" + ht);
		if (startx < 0)
			startx = 0;
		if (starty < 0)
			starty = 0;
		// NARAYAN TBD Write New Function to crop Image
		endx = startx + wt;
		endy = starty + ht;
		double scale = m_SelectedImageObject.m_Scale;
		dispimg = ImageUtils.getSelectedImage(origImg, startx, starty, endx,
				endy, scale, scale, m_ClippedRectangle);
		System.out.println("Clip Rect: " + m_ClippedRectangle.toString());
		return dispimg;
	}

	/**
	 * Action class that shows the image specified in it's constructor.
	 */
	private class ThumbnailAction extends AbstractAction {
		// The icon if the full image we want to display.
		// public ImageIcon m_DisplayImage;
		public String m_ImageCaption;
		public BufferedImage m_DisplayImage;
		public BufferedImage m_ScaledImage;

		// Original Image Size, PPI, and Minimum Image Size that can be
		// displayed
		public int m_ImagePPI;
		public int m_OrigImageWidth;
		public int m_OrigImageHeight;
		public int m_MinImageWidth;
		public int m_MinImageHeight;
		public double m_Scale;
		public double m_AspectRatio;
		public String m_SelectedImagePath;

		// The Image is positioned in the center. This defines the Top and Left
		// Margin left to draw the Image
		public int m_TopMargin;
		public int m_LeftMargin;

		// This is the Rectangular Bounds of the Image Displayed
		Rectangle m_DisplayRect;

		// ImageHolder Objects which holds the Thumbview Image and location of
		// Image in Server
		public ImageHolder m_ImgHldr;

		// This Element is the Button which displays the ImageIcon on the
		// TolBar.
		// The Button is used to set Visble on/off based on Display mode
		public JButton m_ImageButton;

		// This indicates the Images are from Server, Desktop, etc.
		public int m_ImageFromMode;

		/**
		 * @param Icon
		 *            - The full size photo to show in the button.
		 * @param Icon
		 *            - The thumbnail to show in the button.
		 * @param String
		 *            - The descriptioon of the icon.
		 */
		public ThumbnailAction(ImageHolder imgHldr, Icon thumb, int imgFromMode) {
			m_ImgHldr = imgHldr;
			m_DisplayImage = null;
			m_ImageFromMode = imgFromMode;

			// The short description becomes the tooltip of a button.
			putValue(SHORT_DESCRIPTION, m_ImgHldr.m_ImgCaption);

			// The LARGE_ICON_KEY is the key for setting the
			// icon when an Action is applied to a button.
			putValue(LARGE_ICON_KEY, thumb);
			// System.out.println("Image Added: \n" + m_ImgHldr.toString());
		}

		public void addImageButton(JButton thumbButton) {
			m_ImageButton = thumbButton;
		}

		/*
		 * public BufferedImage getBufferedImage(int width, int ht) {
		 * BufferedImage selImg = null; if (m_DisplayImage == null ||
		 * m_DisplayImage.getImage() == null) return null; selImg =
		 * ImageUtils.ScaleToSize(m_DisplayImage.getImage(), width, ht); return
		 * selImg; }
		 */
		// Shows the full image in the main area and sets the application title.
		public void actionPerformed(ActionEvent e) {
			if (m_DisplayImage == null) {
				m_DisplayImage = loadImage(m_ImgHldr);
			}
			m_SelectedImageObject = this;
			displayImage();
			m_SelectionCriteria = NO_SELECTION;
			m_ZoomWindow.setVisible(false);
		}

		public void selectDefaultImage() {
			// System.out.println("Image to be Displayed: \n" +
			// m_ImgHldr.toString());
			if (m_DisplayImage == null) {
				m_DisplayImage = loadImage(m_ImgHldr);
			}
			m_SelectedImageObject = this;
			if (m_DisplayImage != null)
				displayImage();
			m_SelectionCriteria = NO_SELECTION;
			m_ZoomWindow.setVisible(false);
		}

		public void displayImage() {
			Rectangle rect = m_DisplayLabel.getBounds();
			System.out.println("Display Size: " + rect.toString());
			Dimension toolBarSize = m_ThumbviewToolBar.getSize(null);
			System.out.println("ThumbviewToolBar Size: "
					+ toolBarSize.toString());
			if (m_DisplayImage == null || toolBarSize.getWidth() == 0
					|| toolBarSize.getHeight() == 0)
				return;

			int h = 0, w = 0;
			int dispH = (int) m_DisplayImage.getHeight();
			int dispW = (int) m_DisplayImage.getWidth();
			double aspect = ((double) dispH) / ((double) dispW);
			System.out.println("Img Ht=" + dispH + " Wt=" + dispW + " Aspect="
					+ aspect);
			boolean useHt = false;
			if (dispW > dispH) {
				w = (int) rect.getWidth();
				h = (int) (w * aspect);
				if (h > rect.getHeight()) {
					useHt = true;
				}
			}
			System.out.println("First Loop Scaled Image Width: " + w + " Ht: "
					+ h + " useHt= " + useHt);
			if (dispH >= dispW || useHt) {
				h = (int) rect.getHeight() - (int) rect.getY();
				w = (int) (h / aspect);
			}
			System.out.println("Scaled Image Width: " + w + " and Ht: " + h);
			// m_ScaledImage = ImageUtils.ScaleToSize(m_DisplayImage, (int)
			// rect.getWidth(),
			// ((int) rect.getHeight() -
			// (int) rect.getY()));
			m_ScaledImage = ImageUtils.ScaleToSize(m_DisplayImage, w, h);
			ImageIcon imgIcon = new ImageIcon(m_ScaledImage, m_ImageCaption);
			m_TopMargin = (int) ((rect.getHeight() - m_ScaledImage.getHeight()) / 2);
			m_LeftMargin = (int) ((rect.getWidth() - m_ScaledImage.getWidth()) / 2);
			// System.out.println("Get Bounds: " + rect.toString());
			// System.out.println("Display Visible Bounds: " +
			// m_DisplayLabel.getVisibleRect().toString());
			int bottom = m_TopMargin, right = m_LeftMargin;
			m_DisplayLabel.setBorder(BorderFactory.createEmptyBorder(
					m_TopMargin, m_LeftMargin, bottom, right));
			m_DisplayRect = new Rectangle(m_LeftMargin, m_TopMargin,
					m_ScaledImage.getWidth(), m_ScaledImage.getHeight());
			m_DisplayLabel.setIcon(imgIcon);
			// setTitle("Select Image " +
			// getValue(SHORT_DESCRIPTION).toString());
			repaintContent();
		}

		private BufferedImage loadImage(ImageHolder imgHldr) {
			BufferedImage dispImg = null, actImg = null;
			ImageHolder fullImgHldr = null;
			if (m_ImageBBox == null) {
				System.out.println("Image BBox is Null");
				return null;
			}
			double w = m_ImageBBox.width / 72;
			double h = m_ImageBBox.height / 72;
			HashMap imageAssets = null;
			try {
				// This will retrive all Thumbview Images
				int assetId = imgHldr.m_AssetId;
				// if (m_ImgHldr.m_UseCachedImages)
				if (m_ImageFromMode == ImageHandlerInterface.DESKTOP_IMAGES) {
					fullImgHldr = imgHldr;
					String imgSrcDir = imgHldr.m_SrcDir + imgHldr.m_FileName;
					dispImg = ImageUtils.LoadImage(imgSrcDir);
					System.out.println("Actual Display Image Ht: "
							+ dispImg.getHeight() + " Wt: "
							+ dispImg.getWidth());
					int dpi = ImageUtils.FindResolution(dispImg, w, h);
					System.out.println("DPI of the Image: " + dpi);
					// if the Image dpi is less then IMAGE_DIAPLAY_DPI then the
					// image
					// will not be down sampled
					if (dpi > IMAGE_DIAPLAY_DPI)
						dispImg = ImageUtils.DownSampleImage(dispImg, w, h,
								IMAGE_DIAPLAY_DPI);
					fullImgHldr.m_EnlargedImage = dispImg;
				} else {
					// This functions returns -1 for non Theme Image Sources
					int themeId = getThemeIdFromImageSrc(m_ImageFromMode);
					String assetType = CustomerAssetRequest.IMAGE_ASSET_TYPE;
					imageAssets = m_PDFPageInfoManager.getCustomerAssets(
							themeId, assetType, assetId,
							CustomerAssetRequest.REGULAR_IMAGE, m_ImageBBox);
					fullImgHldr = (ImageHolder) imageAssets.get(assetId);
					dispImg = ImageUtils.getBufferedImage(fullImgHldr.m_ImgStr);
					m_ImgHldr = fullImgHldr;
				}
				m_ImageCaption = fullImgHldr.m_ImgCaption;
				m_OrigImageWidth = fullImgHldr.m_OrigImageWidth;
				m_OrigImageHeight = fullImgHldr.m_OrigImageHeight;
				// All the Images are in Java/PDF user space and hence no
				// conversion
				// is needed //m_OrigImageWidth m_OrigImageHeight
				m_ImagePPI = ImageUtils.FindResolution(m_OrigImageWidth,
						m_OrigImageHeight, m_ImageBBox.width / 72,
						m_ImageBBox.height / 72);
				if (m_ImagePPI < 300) {
					showError("The Image has lower resolution: " + m_ImagePPI,
							"Image Resolution Warning");
				}
				HashMap reqImgSize = ImageUtils.getImageSize4PPI(m_ImagePPI,
						m_ImageBBox.width / 72, m_ImageBBox.height / 72);
				m_MinImageWidth = ((Integer) reqImgSize.get("Width"))
						.intValue();
				m_MinImageHeight = ((Integer) reqImgSize.get("Height"))
						.intValue();
				m_AspectRatio = ((double) m_OrigImageHeight)
						/ ((double) m_OrigImageWidth);
				// int calcHt = (int) (m_MinImageWidth*m_AspectRatio);
				m_SelectedImagePath = fullImgHldr.m_SrcDir
						+ fullImgHldr.m_FileName;

				if (dispImg != null) {
					System.out.println("Display Image Ht: "
							+ dispImg.getHeight() + " Wt: "
							+ dispImg.getWidth());
					double sy = ((double) dispImg.getHeight())
							/ ((double) m_OrigImageHeight);
					double sx = ((double) dispImg.getWidth())
							/ ((double) m_OrigImageWidth);
					if (sx > sy)
						m_Scale = sy;
					else
						m_Scale = sx;
					System.out.println("sx: " + sx + " sy: " + sy + " Scale: "
							+ m_Scale);
				}
				System.out.println("Display Image Obj: " + toString());
				return dispImg;
			} catch (Exception ex) {
				showError("Unable to Load Image from Server",
						"Load Image Error");
			}
			return null;
		}

		public String toString() {
			StringBuffer mesg = new StringBuffer(
					"ThumbnailAction Object Info: ");
			mesg.append("Aspect Ratio=" + m_AspectRatio + " PPI=" + m_ImagePPI);
			mesg.append(" Orig Img Wt=" + m_OrigImageWidth + " And Ht="
					+ m_OrigImageHeight);
			mesg.append(" Minimum Img Wt=" + m_MinImageWidth + " And Ht="
					+ m_MinImageHeight);
			mesg.append("\nScale of Orig 2 Down Sampled Img=" + m_Scale);
			mesg.append("\nImage Abs Path=" + m_SelectedImagePath);
			mesg.append("\nAsset ID=" + m_ImgHldr.m_AssetId);
			return mesg.toString();
		}

	}

	// This is for Clean-up Activity.
	public void cleanUpMemory() {
		// System.out.println("In Clean-up Memory");
		if (m_InitImageAssets != null) {
			m_InitImageAssets.clear();
			m_InitImageAssets = null;
		}
		this.dispose();
	}

	public void showError(String mesg, String title) {
		m_SelectionCriteria = NO_SELECTION;
		m_ZoomWindow.setVisible(false);
		JOptionPane.showMessageDialog(this, mesg, title,
				JOptionPane.ERROR_MESSAGE);
	}
}
