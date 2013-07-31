// RGPT PACKAGES
package com.rgpt.viewer;

//import javax.swing.JInternalFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import com.rgpt.serverhandler.PDFPageHandler;
import com.rgpt.serverutil.PDFPageInfoManager;
import com.rgpt.util.RGPTRectangle;
import com.rgpt.util.ThumbViewImageHandler;

public class ThumbViewImagePanel extends JPanel implements MouseListener,
		ImageHandlerInterface, ThumbViewImageHandler {
	// This are Initial set of Images that are shown in the Toolbar
	HashMap m_InitImageAssets;

	// This component display the full image
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
	public ThumbViewImagePanel(SelectedImageHandler imgHdlr,
			HashMap imageAssets, ImageUploadInterface imgUploader,
			int downloadMode, Map<Integer, HashMap> templateThemeInfo,
			PDFPageInfoManager pdfPageInfoManager) {
		m_InitImageAssets = imageAssets;
		m_ImageUploader = imgUploader;
		m_SelectedImageHandler = imgHdlr;
		m_InitImageFromMode = downloadMode;
		m_CurrImageFromMode = m_InitImageFromMode;
		m_PDFPageInfoManager = pdfPageInfoManager;
		m_TemplateThemeInfo = templateThemeInfo;

		m_ThumbviewToolBar = new JToolBar();
		m_ToolBarComponents = new Vector();

		// Set the window's location.
		// setLocation(xOffset*openFrameCount, yOffset*openFrameCount);

		// A label for displaying the pictures
		// JPanel displayPanel = new JPanel();
		this.setPreferredSize(new Dimension(0, 120));
		this.createContentPane();
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

	// This is get the UI Visible
	public boolean getVisibility() {
		return this.isVisible();
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

	public void createContentPane() {
		// Set the Layout to Border Layout
		this.setLayout(new BorderLayout());

		// Adding Image Selection Criteria to the Content Pane
		m_ImageSelToolPanel = new JToolBar(JToolBar.VERTICAL);
		m_ImageSelToolPanel.setFloatable(false);
		this.createImageSelectionPanel();
		this.add(m_ImageSelToolPanel, BorderLayout.WEST);

		// Adding Thumvbiew Images to the Bottom
		JScrollPane imageScroller = null;
		m_ThumbviewToolBar.setMinimumSize(new Dimension(100, 100));
		imageScroller = new JScrollPane(m_ThumbviewToolBar,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		imageScroller.setViewportBorder(BorderFactory
				.createLineBorder(Color.black));
		this.add(imageScroller, BorderLayout.CENTER);

		System.out.println("The Component Count in ContentPane: "
				+ this.getComponentCount());

	}

	// Recording initial start points
	public void mousePressed(MouseEvent event) {
	}

	// Erase the last rectangle when the user releases the mouse.
	public void mouseReleased(MouseEvent event) {
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
	}

	public void repaintContent() {
		this.repaint();
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

	// This is to add Image Files
	public void addImageFile() {
		try {
			JButton button = m_ImageSrcButtons
					.get(ImageHandlerInterface.UPLOAD_DESKTOP_IMAGES);
			if (button.isEnabled())
				m_ImageUploader.addImageFile(ThumbViewImagePanel.this, false);
		} catch (Exception ex) {
			ex.printStackTrace();
			showError("No Image Files Selected", "Image Selection Error");
		}
	}

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
				addImageFile();
			}
		});
		m_ImageSelToolPanel.add(button);

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
		if (imageHolders == null && imageHolders.size() == 0)
			return;
		boolean defImageSel = this.handleImagesFromSource(imageHolders,
				imageDownloadMode, true);
		if (defImageSel)
			processSelectedImage();
		this.setDefaultCursor();
		m_SelectedImageHandler.setDefaultCursor();
	}

	// This function is called by the ImageUploadImterface to handle the new
	// images uploaded from the user machine.
	private boolean handleImagesFromSource(Vector imageHolders, int imageSrc,
			boolean isDesktoImage) throws Exception {
		ImageHolder imgHldr = null;
		ImageIcon thumbnailIcon = null;
		ThumbnailAction thumbAction = null;
		boolean defImageSel = false;

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
			if (i == 0 && isDesktoImage) {
				try {
					if (!m_SelectedImageHandler.containsUserSelImage()) {
						m_SelectedImageObject = thumbAction;
						defImageSel = true;
					}
				} catch (Exception ex) {
				}
			}
			addThumbButton(thumbAction);
			toolBarDispComps.addElement(thumbAction);
			m_ToolBarComponents.addElement(thumbAction);
		}
		// Will show only the newly Uploaded Images
		System.out.println("The Number of Uploaded Images to be displayed is: "
				+ toolBarDispComps.size());
		this.resetToolBarDisplayComp(toolBarDispComps);
		System.out.println("Reset ToolBar Display Component Successfully");
		m_ThumbviewToolBar.updateUI();
		repaintContent();
		return defImageSel;
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
		thumbButton.addMouseListener(thumbAction);
		/*
		 * thumbButton.addMouseListener(new MouseAdapter(){ public void
		 * mousePressed(MouseEvent mp) { JButton thumbViewBut = (JButton)
		 * mp.getSource(); setCursorImage(thumbViewBut); } public void
		 * mouseClicked(MouseEvent e){ if (e.getClickCount() == 2){
		 * System.out.println(" double click" ); processSelectedImage(); } }
		 * public void mouseReleased(MouseEvent me) { if
		 * (m_SelectedImageHandler.containsVDPImage(me)) processSelectedImage();
		 * setDefaultCursor(); } } );
		 */
		// add the new button BEFORE the last glue
		// this centers the buttons in the toolbar
		// System.out.println("Adding Image to Toolbar: " +
		// thumbAction.m_ImgHldr.m_AssetId);
		// System.out.println("The Component Count: " +
		// m_ThumbviewToolBar.getComponentCount());
		m_ThumbviewToolBar.add(thumbButton,
				m_ThumbviewToolBar.getComponentCount() - 1);
	}

	// This is to show Direction Icon to move Images Horizontally or Vertically
	BufferedImage m_DirectionIcon = null;

	public BufferedImage getDirectionIcon() {
		if (m_DirectionIcon != null)
			return m_DirectionIcon;
		try {
			String imgLocation = PDFPageHandler.IMAGE_PATH + "direction.png";
			m_DirectionIcon = ImageUtils.getBufferedImage(imgLocation,
					this.getClass());
			// ImageUtils.displayImage(m_DirectionIcon, "Direction Image");
			return m_DirectionIcon;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public void showBusyCursor() {
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		m_SelectedImageHandler.showBusyCursor();
	}

	protected void setDefaultCursor() {
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	protected void setCursorImage(JButton jb) {
		Icon newicon = jb.getIcon();
		Image selectedimg = ((ImageIcon) newicon).getImage();
		// Image selectedimg = m_SelectedImageObject.m_DisplayImage;
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Cursor c = toolkit.createCustomCursor(selectedimg, new Point(16, 15),
				"img");
		this.setCursor(c);
	}

	public boolean m_DoAutoCrop = false;

	// public String m_SelectedImagePath;
	private void processSelectedImage() {
		System.out.println("In Process Selected Image");
		if (m_SelectedImageObject.m_DisplayImage == null)
			m_SelectedImageObject.loadImage();

		System.out.println("Selected Image Object is: "
				+ m_SelectedImageObject.toString());
		BufferedImage dispimg = null;
		BufferedImage selDispImage = m_SelectedImageObject.m_DisplayImage;
		BufferedImage selectedImage = selDispImage;
		// selectedImage = ImageUtils.scaleImage(selDispImage, (int)
		// m_ImageBBox.width,
		// (int) m_ImageBBox.height, true);

		int imgFromMode = m_SelectedImageObject.m_ImageFromMode;
		ImageHolder imgHldr = m_SelectedImageObject.m_ImgHldr;
		if (selectedImage == null)
			return;
		System.out.println("Selected Image is: " + selectedImage.toString()
				+ " Image Holder Object is: " + imgHldr.toString());

		// Populating Image Holder with essential data
		imgHldr.m_IsClipped = false;
		imgHldr.m_ImageBBox = m_ImageBBox;
		imgHldr.m_ImageFromMode = imgFromMode;
		imgHldr.m_ClipRectangle = null;
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

	/**
	 * Action class that shows the image specified in it's constructor.
	 */
	private class ThumbnailAction extends AbstractAction implements
			MouseListener {
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

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mousePressed(MouseEvent mp) {
			JButton thumbViewBut = (JButton) mp.getSource();
			setCursorImage(m_ImageButton);
			m_SelectedImageObject = this;
			m_SelectionCriteria = NO_SELECTION;
		}

		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				System.out.println(" double click");
				// processSelectedImage();
			}
		}

		public void mouseReleased(MouseEvent me) {
			if (m_SelectedImageHandler.containsVDPImage(me))
				processSelectedImage();
			setDefaultCursor();
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
				loadImage();
			}
			m_SelectedImageObject = this;
			m_SelectionCriteria = NO_SELECTION;
		}

		public void selectDefaultImage() {
			// System.out.println("Image to be Displayed: \n" +
			// m_ImgHldr.toString());
			if (m_DisplayImage == null) {
				loadImage();
			}
			m_SelectedImageObject = this;
			m_SelectionCriteria = NO_SELECTION;
		}

		private void loadImage() {
			m_DisplayImage = loadImage(m_ImgHldr);
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
		if (m_InitImageAssets != null)
			m_InitImageAssets.clear();
		m_InitImageAssets = null;
		if (m_ToolBarComponents != null)
			m_ToolBarComponents.removeAllElements();
		m_ToolBarComponents = null;
		if (m_TemplateThemeInfo != null)
			m_TemplateThemeInfo.clear();
		m_TemplateThemeInfo = null;
	}

	public void showError(String mesg, String title) {
		m_SelectionCriteria = NO_SELECTION;
		JOptionPane.showMessageDialog(this, mesg, title,
				JOptionPane.ERROR_MESSAGE);
	}
}
