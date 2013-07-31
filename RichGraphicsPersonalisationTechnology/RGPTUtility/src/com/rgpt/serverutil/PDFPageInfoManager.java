package com.rgpt.serverutil;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.imageio.ImageIO;

import com.rgpt.imageutil.ImageUtils;
import com.rgpt.serverhandler.ApprovePDFRequest;
import com.rgpt.serverhandler.CustomerAssetRequest;
import com.rgpt.serverhandler.FontStreamRequest;
import com.rgpt.serverhandler.GetThemesRequest;
import com.rgpt.serverhandler.PDFPageImageRequest;
import com.rgpt.serverhandler.PDFPageInfoRequest;
import com.rgpt.serverhandler.PDFViewInterface;
import com.rgpt.serverhandler.PopulateVDPFieldRequest;
import com.rgpt.serverhandler.SaveImageRequest;
import com.rgpt.serverhandler.SaveVDPDataRequest;
import com.rgpt.serverhandler.ServerInterface;
import com.rgpt.serverhandler.ServerResponse;
import com.rgpt.templateutil.PDFPageHolder;
import com.rgpt.templateutil.PDFPageInfo;
import com.rgpt.templateutil.VDPImageFieldInfo;
import com.rgpt.templateutil.VDPTextFieldInfo;
import com.rgpt.util.AppletParameters;
import com.rgpt.util.BatchApprovalRequest;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTRectangle;
import com.rgpt.util.RGPTThreadWorker;
import com.rgpt.util.ThreadInvokerMethod;
// This classes are needed to make URL Connection and read the Stream

public class PDFPageInfoManager implements ThreadInvokerMethod {
	public static final int SERVER_MODE = 0;
	public static final int STANDALONE_MODE = 1;

	public static int m_Mode;

	// PDF Pages corresponding to every page
	public HashMap m_PDFPageInfo;

	public int m_PageCount;

	// This holds the Implementor of PDFView Interface
	PDFViewInterface m_PDFViewImpl;

	// Server Settings
	public static Properties m_ServerProperties;

	// Applet Parameters
	public AppletParameters m_AppletParameters;

	// Work In Progress Parameter. If Worj is Saved in between this
	// parameter is set to true
	public boolean m_IsWorkInProgress = false;

	ServerProxy m_Server;

	public PDFPageInfoManager(int pageCount, int mode) {
		m_Mode = mode;
		m_PageCount = pageCount;
		m_PDFPageInfo = new HashMap();

		boolean isServerMode = false;
		if (this.m_Mode == SERVER_MODE)
			isServerMode = true;
		m_ServerProperties = AppletParameters.createServerProperties(
				this.getClass(), isServerMode);
	}

	public void reInstateMemory() {
		m_PDFPageInfo = new HashMap();
	}

	// This is for Clean-up Activity. When called this releases the PDFPageInfo
	// Memory for every Page in the m_PDFPageInfo HashMap.
	public void cleanUpMemory() {
		if (m_PDFPageInfo == null)
			return;
		m_PDFPageInfo.clear();
		m_PDFPageInfo = null;
	}

	public void loadPDFViewInterface(PDFViewInterface pdfView) {
		m_PDFViewImpl = pdfView;
	}

	// This method is invoked by the Applet

	public void loadViewPDFRequest(AppletParameters appletParameters,
			PDFViewInterface pdfView) {
		m_AppletParameters = appletParameters;
		m_PDFViewImpl = pdfView;
	}

	public void loadSavedPDFPage(AppletParameters appletParameters,
			PDFViewInterface pdfView) {
		m_AppletParameters = appletParameters;
		m_PDFViewImpl = pdfView;
	}

	public void loadPDFPage(AppletParameters appletParameters,
			PDFViewInterface pdfView) throws Exception {
		m_AppletParameters = appletParameters;
		m_PDFViewImpl = pdfView;
		// this.getPDFPageFromServer(1);
	}

	public BufferedImage getPDFPageAsImage(int pageNum) throws Exception {
		PDFPageInfo pdfPageInfo = this.getPDFPage(pageNum);
		if (m_Mode != SERVER_MODE)
			return pdfPageInfo.m_BufferedImage;
		return m_PDFViewImpl.getPDFPage(PDFViewInterface.REGULAR_QUALITY_PDF,
				pageNum);
	}

	public BufferedImage getPDFPageFromServer(int pageNum, int qualityMode)
			throws Exception {
		// Get PDF Page from the Server.
		ServerInterface serverReq = null;
		serverReq = new ServerInterface(m_AppletParameters.m_ServiceIdentifier,
				m_AppletParameters.m_ServerName,
				ServerInterface.PDF_PAGE_IMAGE_REQUEST);

		serverReq.m_ServerRequest = new PDFPageImageRequest();
		PDFPageImageRequest imgReq = (PDFPageImageRequest) serverReq.m_ServerRequest;
		imgReq.m_QualityMode = qualityMode;
		imgReq.m_PageNum = pageNum;
		imgReq.m_TemplateId = m_AppletParameters.getIntVal("template_id");

		// Load the PDF Page from the Server Response
		InputStream connIPStream = makeServerRequest(serverReq);
		BufferedImage pdfPage = ImageIO.read(connIPStream);
		connIPStream.close();
		return pdfPage;
	}

	public PDFPageInfo firstPage() {
		// Retrieving from the HaspMap
		int pageNum = 1;
		return this.getPDFPage(pageNum);
	}

	public PDFPageInfo prevPage(int currPageNum) {
		// Retrieving from the HaspMap
		int pageNum = currPageNum - 1;
		return this.getPDFPage(pageNum);
	}

	public PDFPageInfo nextPage(int currPageNum) {
		// Retrieving from the HaspMap
		int pageNum = currPageNum + 1;
		return this.getPDFPage(pageNum);
	}

	public PDFPageInfo lastPage() {
		// Retrieving from the HaspMap
		int pageNum = m_PDFPageInfo.size();
		return this.getPDFPage(pageNum);
	}

	public boolean isVDPDefinedInPDF() {
		PDFPageInfo pdfPageInfo = null;
		Object[] keys = m_PDFPageInfo.keySet().toArray();
		for (int i = 0; i < keys.length; i++) {
			pdfPageInfo = (PDFPageInfo) m_PDFPageInfo.get((Integer) keys[i]);
			if (pdfPageInfo.m_IsVDPFieldDefined)
				return true;
		}
		RGPTLogger.logToFile("No VDP Fields is defined for this document");
		return false;
	}

	public PDFPageInfo getPDFPage(int pageNum) {
		// if (m_PageCount == -1) return null;
		PDFPageInfo pdfPageInfo = null;
		if (pageNum <= 0)
			pageNum = 1;
		else if (pageNum > m_PageCount)
			pageNum = m_PageCount;
		pdfPageInfo = (PDFPageInfo) m_PDFPageInfo.get(new Integer(pageNum));
		if (pdfPageInfo != null)
			return pdfPageInfo;
		try {
			if (m_Mode == SERVER_MODE)
				pdfPageInfo = this.getPDFPageFromServer(pageNum);
		} catch (Exception ex) {
			throw new RuntimeException("Unable to retrieve Page from Server: "
					+ ex.getMessage());
		}
		return pdfPageInfo;
	}

	public int m_IsPDFApproved = ApprovePDFRequest.PDF_NO_ACTION_TAKEN;

	public void setPDFApproval(boolean isApproved) {
		if (isApproved)
			m_IsPDFApproved = ApprovePDFRequest.PDF_APPROVED;
		else
			m_IsPDFApproved = ApprovePDFRequest.PDF_DISAPPROVED;
		if (this.m_Mode != SERVER_MODE)
			return;

		// Populating the Server Requeste
		try {
			ServerInterface serverReq = null;
			serverReq = new ServerInterface(
					m_AppletParameters.m_ServiceIdentifier,
					m_AppletParameters.m_ServerName,
					ServerInterface.APPROVE_PDF_REQUEST);
			serverReq.m_ServerRequest = new ApprovePDFRequest();
			ApprovePDFRequest approvePDFReq = (ApprovePDFRequest) serverReq.m_ServerRequest;
			approvePDFReq.m_CustomerId = m_AppletParameters
					.getIntVal("customer_id");
			approvePDFReq.m_CustomerBasketId = m_AppletParameters
					.getIntVal("customer_basket_id");
			approvePDFReq.m_PDFApprovalFlag = m_IsPDFApproved;
			RGPTLogger.logToFile("Making Server Call for Customer: "
					+ m_AppletParameters.getIntVal("customer_id"));

			// Making the Server Request
			ObjectInputStream dataStr = new ObjectInputStream(
					makeServerRequest(serverReq));
			ServerResponse servResp = (ServerResponse) ServerResponse
					.load(dataStr);
			dataStr.close();
			if (!servResp.m_IsSuccess)
				throw new RuntimeException(
						"Unable to set Approval Flag in the Server");
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(
					"Unable to set Approval Flag in the Server");
		}
	}

	public static HashMap getPDFPageApprovals(int totalPgCnt, int pdfPgCnt,
			HashMap pdfDocApprovals) {
		int numOfPDFDoc = totalPgCnt / pdfPgCnt;
		int pdfDocCounter = 0;
		RGPTLogger.logToFile("Total Pg Count: " + totalPgCnt + " PDFPg Count: "
				+ pdfPgCnt + " Num Docs: " + numOfPDFDoc);
		HashMap pdfPageApprovals = new HashMap();
		boolean isPDFApproved = true;
		int pgCounter = 0;
		for (int i = 0; i < pdfDocApprovals.size(); i++) {
			pdfDocCounter++;
			isPDFApproved = ((Boolean) pdfDocApprovals.get(pdfDocCounter))
					.booleanValue();
			for (int j = 0; j < pdfPgCnt; j++) {
				pgCounter++;
				// int pdfPage = pdfDocCounter*(j+1);
				pdfPageApprovals.put(pgCounter, isPDFApproved);
			}
		}
		RGPTLogger.logToFile("PDF Pages: " + pdfPageApprovals.size());
		return pdfPageApprovals;
	}

	public static HashMap getPDFDocApprovals(int totalPgCnt, int pdfPgCnt,
			HashMap pgApprStatus) {
		int numOfPDFDoc = totalPgCnt / pdfPgCnt;
		int pdfDocCounter = 0;
		HashMap pdfDocApprStatus = new HashMap();
		boolean isPDFApproved = true, isPDFPgApproved = true;
		int pgCounter = 0;
		for (int i = 0; i < numOfPDFDoc; i++) {
			pdfDocCounter++;
			for (int j = 0; j < pdfPgCnt; j++) {
				pgCounter++;
				// int pdfPage = pdfDocCounter*(j+1);
				isPDFPgApproved = ((Boolean) pgApprStatus.get(pgCounter))
						.booleanValue();
				if (!isPDFPgApproved)
					isPDFApproved = false;
			}
			pdfDocApprStatus.put(pdfDocCounter, isPDFApproved);
			isPDFApproved = true;
		}
		return pdfDocApprStatus;
	}

	public void setPDFBatchApproval(HashMap pdfPgApproveStatus) {
		if (this.m_Mode != SERVER_MODE)
			return;

		// Populating the Server Requeste
		try {
			ServerInterface serverReq = null;
			AppletParameters params = m_AppletParameters;
			serverReq = new ServerInterface(
					m_AppletParameters.m_ServiceIdentifier,
					m_AppletParameters.m_ServerName,
					ServerInterface.BATCH_APPROVAL_REQUEST);
			serverReq.m_ServerRequest = new BatchApprovalRequest();
			BatchApprovalRequest pdfBatchApproveReq = null;
			pdfBatchApproveReq = (BatchApprovalRequest) serverReq.m_ServerRequest;
			pdfBatchApproveReq.m_AssetId = params.m_DigitalAssetId;
			pdfBatchApproveReq.m_BatchId = params.getIntVal("batch_id");
			pdfBatchApproveReq.m_TemplateId = params.getIntVal("template_id");
			pdfBatchApproveReq.m_TemplatePageCount = params
					.getIntVal("template_page_count");
			pdfBatchApproveReq.m_TotalPageCount = params
					.getIntVal("page_count");
			pdfBatchApproveReq.m_PDFPageApprovalStatus = pdfPgApproveStatus;
			RGPTLogger.logToFile("Making Server Call for Batch Approval: "
					+ pdfBatchApproveReq.toString());

			// Making the Server Request
			ObjectInputStream dataStr = new ObjectInputStream(
					makeServerRequest(serverReq));
			ServerResponse servResp = (ServerResponse) ServerResponse
					.load(dataStr);
			dataStr.close();
			if (!servResp.m_IsSuccess)
				throw new RuntimeException(
						"Unable to set Approval Flag in the Server");
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(
					"Unable to set Approval Flag in the Server");
		}
	}

	// FontStreamRequest
	private static HashMap m_FontStreamHolder;
	private boolean m_DownloadingFontStream = false;

	public HashMap getFontStreamHolder() {
		if (m_FontStreamHolder != null)
			return m_FontStreamHolder;
		for (int i = 0; i < m_PageCount; i++) {
			int pageNum = i + 1;
			PDFPageInfo pdfPage = (PDFPageInfo) m_PDFPageInfo.get(new Integer(
					pageNum));
			if (pdfPage == null)
				continue;
			if (pdfPage.m_VDPTextFieldInfo.size() == 0)
				continue;
			VDPTextFieldInfo vdpText = (VDPTextFieldInfo) pdfPage.m_VDPTextFieldInfo
					.elementAt(0);
			if (vdpText.m_FontStreamHolder != null) {
				this.m_FontStreamHolder = vdpText.m_FontStreamHolder;
				return m_FontStreamHolder;
			}
		}
		return null;
	}

	// Loading the Font Stream in a new Thread
	public void loadFontStream() {
		RGPTThreadWorker threadWorker = null;
		HashMap requestData = new HashMap();
		requestData.put("RequestType", "LoadFontStream");
		threadWorker = new RGPTThreadWorker(Thread.MIN_PRIORITY, this,
				requestData);
		threadWorker.startThreadInvocation();
	}

	// Processing the Thread Request to load Font Streams
	public void processThreadRequest(HashMap requestData) throws Exception {
		String reqType = (String) requestData.get("RequestType");
		if (reqType.equals("LoadFontStream"))
			this.loadServerFontStreams();
	}

	// Making Server Call to load Font Stream
	public void loadServerFontStreams() {
		if (m_FontStreamHolder != null || m_DownloadingFontStream
				|| m_Mode == STANDALONE_MODE)
			return;

		// Making Server Call to download Font Stream Object
		ServerInterface serverReq = null;
		serverReq = new ServerInterface(m_AppletParameters.m_ServiceIdentifier,
				m_AppletParameters.m_ServerName,
				ServerInterface.FONT_STREAM_REQUEST);
		serverReq.m_ServerRequest = new FontStreamRequest();
		FontStreamRequest fontStreamReq = (FontStreamRequest) serverReq.m_ServerRequest;
		fontStreamReq.m_TemplateId = m_AppletParameters
				.getIntVal("template_id");
		System.out.println("Server Request: " + serverReq.toString());

		// Load the Font Stream from the Server Response
		boolean raiseEx = false;
		ServerResponse servResp = null;
		try {
			m_DownloadingFontStream = true;
			ObjectInputStream dataStr = new ObjectInputStream(
					makeServerRequest(serverReq));
			servResp = (ServerResponse) ServerResponse.load(dataStr);
			dataStr.close();
		} catch (Throwable th) {
			th.printStackTrace();
			raiseEx = true;
		}

		if ((servResp != null && !servResp.m_IsSuccess) || raiseEx) {
			m_DownloadingFontStream = false;
			throw new RuntimeException("No Font Stream Returned from Server");
		}

		m_FontStreamHolder = (HashMap) servResp.m_ResultValues
				.get("FontStreamHolder");

		// If Font Stream Hashmap is null or size is o then there are no VDPText
		// in this Template
		if (m_FontStreamHolder == null || m_FontStreamHolder.size() == 0)
			return;

		// Setting the Font Stream
		for (int i = 0; i < m_PageCount; i++) {
			int pageNum = i + 1;
			PDFPageInfo pdfPage = (PDFPageInfo) m_PDFPageInfo.get(new Integer(
					pageNum));
			if (pdfPage == null)
				continue;
			if (pdfPage.m_VDPTextFieldInfo.size() == 0)
				continue;
			VDPTextFieldInfo vdpText = (VDPTextFieldInfo) pdfPage.m_VDPTextFieldInfo
					.elementAt(0);
			if (vdpText.m_FontStreamHolder != null)
				continue;
			vdpText.m_FontStreamHolder = this.m_FontStreamHolder;
			pdfPage.buildFontStream();
		}
	}

	public HashMap getThemeData(int reqType, Vector<Integer> themeIdSet) {
		ServerInterface serverReq = null;
		int serviceProvId = m_AppletParameters.m_ServiceIdentifier;
		String serverName = m_AppletParameters.m_ServerName;
		serverReq = new ServerInterface(serviceProvId, serverName,
				ServerInterface.GET_THEME_REQUEST);
		GetThemesRequest getThemeReq = null;
		serverReq.m_ServerRequest = new GetThemesRequest();
		getThemeReq = (GetThemesRequest) serverReq.m_ServerRequest;
		getThemeReq.m_ThemeIds = themeIdSet;
		getThemeReq.m_ThemeRequestType = reqType;
		ObjectInputStream dataStr = null;
		RGPTLogger
				.logToFile("Server Request is: " + serverReq.toString(), true);
		try {
			dataStr = new ObjectInputStream(makeServerRequest(serverReq));
			ServerResponse servResp = (ServerResponse) ServerResponse
					.load(dataStr);
			RGPTLogger.logToFile("Server Response is: " + servResp.toString(),
					true);
			if (!servResp.m_IsSuccess)
				return null;
			return servResp.m_ResultValues;
		} catch (Exception ex) {
			String errMsg = "Exception getThemeData Request: "
					+ ex.getMessage();
			RGPTLogger.logToFile(errMsg);
			ex.printStackTrace();
			throw new RuntimeException(errMsg);
		} finally {
			try {
				if (dataStr != null)
					dataStr.close();
			} catch (Exception e) {
			}
		}
		// return ((Map<Integer, HashMap>)
		// servResp.m_ResultValues.get("Themes"));
	}

	private PDFPageInfo getPDFPageFromServer(int pageNum) throws Exception {
		// Get PDF Page from the Server.
		ServerInterface serverReq = null;
		serverReq = new ServerInterface(m_AppletParameters.m_ServiceIdentifier,
				m_AppletParameters.m_ServerName,
				ServerInterface.PDF_PAGE_INFO_REQUEST);
		serverReq.m_ServerRequest = new PDFPageInfoRequest();
		PDFPageInfoRequest pgReq = (PDFPageInfoRequest) serverReq.m_ServerRequest;
		String reqType = m_AppletParameters.m_RequestType;
		if (reqType.equals(m_AppletParameters.EDIT_PDF_TEMPLATE_REQUEST)) {
			pgReq.setEditPDFRequest(
					m_AppletParameters.getIntVal("template_id"), pageNum,
					m_AppletParameters.m_SrcDir,
					m_AppletParameters.m_SerializedFileName);
		} else if (reqType.equals(m_AppletParameters.APPROVE_PDF_REQUEST)) {
			pgReq.setApprovePDFRequest(m_AppletParameters.m_DigitalAssetId,
					pageNum, m_AppletParameters.m_SrcDir,
					m_AppletParameters.m_SerializedFileName);
		} else if (reqType.equals(m_AppletParameters.VIEW_PDF_REQUEST)) {
			pgReq.setViewPDFRequest(m_AppletParameters.m_DigitalAssetId,
					pageNum, m_AppletParameters.m_SrcDir,
					m_AppletParameters.m_SerializedFileName);
		}

		System.out.println("Server Request: " + serverReq.toString());

		// If Font Stream for VDP text is not loaded then initating the Load in
		// a new Thread
		if (m_FontStreamHolder == null || !m_DownloadingFontStream)
			this.loadFontStream();

		// Load the PDF Page from the Server Response
		ObjectInputStream dataStr = new ObjectInputStream(
				makeServerRequest(serverReq));
		ServerResponse servResp = (ServerResponse) ServerResponse.load(dataStr);
		dataStr.close();
		if (!servResp.m_IsSuccess)
			throw new RuntimeException("No Asset Returned from Server");

		PDFPageHolder pgHldr = null;
		pgHldr = (PDFPageHolder) servResp.m_ResultValues.get("PDFPageHolder");
		// m_PDFPageInfo.clear();

		ObjectInputStream objInputStr = null;
		objInputStr = new ObjectInputStream(new ByteArrayInputStream(
				pgHldr.m_PageStr));
		PDFPageInfo pdfPageInfo = (PDFPageInfo) PDFPageInfo.load(objInputStr);
		objInputStr.close();

		pdfPageInfo.m_BufferedImage = ImageIO.read(new ByteArrayInputStream(
				pgHldr.m_ImgStr));
		if (pdfPageInfo.m_VDPTextFieldInfo.size() != 0
				&& m_FontStreamHolder != null) {
			VDPTextFieldInfo vdpText = (VDPTextFieldInfo) pdfPageInfo.m_VDPTextFieldInfo
					.elementAt(0);
			vdpText.m_FontStreamHolder = this.m_FontStreamHolder;
		}
		pdfPageInfo.buildProperFields();
		m_PDFPageInfo.put(new Integer(pageNum), pdfPageInfo);

		// Setting the Applet Parameters for future calls
		m_AppletParameters.m_SrcDir = pgHldr.m_SrcDir;
		m_AppletParameters.m_SerializedFileName = pgHldr.m_SerializedFileName;
		RGPTLogger.logToFile("PDFPageInfo Object is retrieved: "
				+ pdfPageInfo.toString());
		return pdfPageInfo;
	}

	public Map<String, HashMap> populateMappedFields() throws Exception {
		// No pre-population is done if not in Server Mode
		if (m_Mode != SERVER_MODE)
			return null;

		Vector<String> vdpTxtFields = VDPTextFieldInfo.m_VDPPrepopulatedFields;
		Vector<String> vdpImageFields = VDPImageFieldInfo.m_VDPPrepopulatedFields;
		// Pre-polated VDP Fields from the Server.
		ServerInterface serverReq = null;
		serverReq = new ServerInterface(m_AppletParameters.m_ServiceIdentifier,
				m_AppletParameters.m_ServerName,
				ServerInterface.POPULATE_VDP_FIELDS_REQUEST);
		int tempId = m_AppletParameters.getIntVal("template_id");
		int custId = m_AppletParameters.getIntVal("customer_id");
		String dataSrcName = m_AppletParameters.getVal("data_source_name");
		PopulateVDPFieldRequest popVdpFldReq = null;
		popVdpFldReq = new PopulateVDPFieldRequest(tempId, custId, dataSrcName,
				vdpTxtFields, vdpImageFields);
		serverReq.m_ServerRequest = popVdpFldReq;
		// Load the Prepopulated Fields from the Server Response
		ObjectInputStream dataStr = new ObjectInputStream(
				makeServerRequest(serverReq));
		ServerResponse servResp = (ServerResponse) ServerResponse.load(dataStr);
		dataStr.close();
		if (!servResp.m_IsSuccess)
			throw new RuntimeException("No Asset Returned from Server");
		return (Map<String, HashMap>) servResp.m_ResultValues
				.get("VDPMapFieldValues");
	}

	// This function is used to save the User Uploaded Image to Server
	public String saveUserUploadedImages(int pageNum, String fldName,
			String fileName, String imgPath) throws Exception {
		if (this.m_Mode != SERVER_MODE)
			return imgPath;

		// Loading the Image
		BufferedImage image = ImageUtils.LoadImage(imgPath);

		// Populating the Server Request
		ServerInterface serverReq = null;
		serverReq = new ServerInterface(m_AppletParameters.m_ServiceIdentifier,
				m_AppletParameters.m_ServerName,
				ServerInterface.SAVE_IMAGE_REQUEST);
		serverReq.m_ServerRequest = new SaveImageRequest();
		SaveImageRequest svImgReq = (SaveImageRequest) serverReq.m_ServerRequest;
		svImgReq.m_PageNum = pageNum;
		svImgReq.m_TemplateId = m_AppletParameters.getIntVal("template_id");
		svImgReq.m_CustomerId = m_AppletParameters.getIntVal("customer_id");
		svImgReq.m_FieldName = fldName;
		svImgReq.m_ImageFileName = fileName;
		svImgReq.m_VDPImage = image;

		// Making the Server Request
		DataInputStream dataStr = new DataInputStream(
				makeServerRequest(serverReq));
		byte[] pathByte = new byte[dataStr.available()];
		dataStr.read(pathByte);
		String serverPath = new String(pathByte);
		return serverPath;
	}

	public boolean saveUserVDPData(HashMap userPageData) throws Exception {
		if (this.m_Mode != SERVER_MODE)
			throw new RuntimeException(
					"THIS METHOD CAN BE INVOKED ONLY IN SERVER_MODE");

		// Populating the Server Requeste
		ServerInterface serverReq = null;
		serverReq = new ServerInterface(m_AppletParameters.m_ServiceIdentifier,
				m_AppletParameters.m_ServerName,
				ServerInterface.SAVE_VDP_DATA_REQUEST);
		serverReq.m_ServerRequest = new SaveVDPDataRequest();
		SaveVDPDataRequest svVDPReq = (SaveVDPDataRequest) serverReq.m_ServerRequest;
		svVDPReq.m_TemplateId = m_AppletParameters.getIntVal("template_id");
		svVDPReq.m_CustomerId = m_AppletParameters.getIntVal("customer_id");
		svVDPReq.m_CustomerBasketId = m_AppletParameters
				.getIntVal("customer_basket_id");
		svVDPReq.m_UserPageData = userPageData;
		svVDPReq.m_IsWorkInProgress = m_IsWorkInProgress;
		System.out.println("Making Server Call for Customer: "
				+ svVDPReq.m_CustomerId);

		// Making the Server Request
		RGPTLogger.logToFile("Applet Params in saveUserVDPData: "
				+ m_AppletParameters.toString());
		ObjectInputStream dataStr = new ObjectInputStream(
				makeServerRequest(serverReq));
		ServerResponse servResp = (ServerResponse) ServerResponse.load(dataStr);
		dataStr.close();
		if (!servResp.m_IsSuccess)
			throw new RuntimeException("No Asset Returned from Server");
		PDFPageHolder pgHldr = null;
		pgHldr = (PDFPageHolder) servResp.m_ResultValues.get("PDFPageHolder");
		System.out.println("PDFPageHolder: " + pgHldr.toString());
		m_PDFPageInfo.clear();
		ObjectInputStream objInputStr = null;
		objInputStr = new ObjectInputStream(new ByteArrayInputStream(
				pgHldr.m_PageStr));
		PDFPageInfo pdfPageInfo = (PDFPageInfo) PDFPageInfo.load(objInputStr);
		objInputStr.close();

		pdfPageInfo.m_BufferedImage = ImageIO.read(new ByteArrayInputStream(
				pgHldr.m_ImgStr));
		pdfPageInfo.buildProperFields();
		m_PDFPageInfo.put(new Integer(pgHldr.m_PageNum), pdfPageInfo);

		// Setting the Applet Parameters for future calls
		m_AppletParameters.m_RequestType = m_AppletParameters.APPROVE_PDF_REQUEST;
		m_AppletParameters.m_DigitalAssetId = pgHldr.m_AssetId;
		m_AppletParameters.m_SrcDir = pgHldr.m_SrcDir;
		m_AppletParameters.m_SerializedFileName = pgHldr.m_SerializedFileName;
		// boolean result = dataStr.readBoolean();
		return servResp.m_IsSuccess;
	}

	// This retrieves the asset from the server. If the asset id is not know,
	// then assign -1. This api can be used to retrieve PDF or Image Asset
	// Type. For Image Asset Type, specify Image Asset Type to retrive
	// regular or thumbview image. Else specify -1.
	// This will return HashMap. The caller function can retrive Bytes to
	// create asset of appropriate type

	public HashMap getCustomerAssets(String assetType, int assetId,
			int imageSizeType) throws Exception {
		return this.getCustomerAssets(-1, -1, assetType, assetId,
				imageSizeType, null);
	}

	// This method is called from ThumbviewImageFrame (line 1400) to retrieve
	// regular Image
	public HashMap getCustomerAssets(int themeId, String assetType,
			int assetId, int imageSizeType, RGPTRectangle imgBBox)
			throws Exception {
		return this.getCustomerAssets(-1, themeId, assetType, assetId,
				imageSizeType, imgBBox);
	}

	// This is called from PDFPageViewHdlr (line#970) to retrive Customer
	// Thumbview
	// Images from Server
	public HashMap getCustomerAssets(int templateId, String assetType,
			int assetId, int imageSizeType) throws Exception {
		return this.getCustomerAssets(templateId, -1, assetType, assetId,
				imageSizeType, null);
	}

	// This the Internal Method Encapsulating all the above getCustAssets
	// Methods
	private HashMap getCustomerAssets(int templateId, int themeId,
			String assetType, int assetId, int imageSizeType,
			RGPTRectangle imgBBox) throws Exception {
		if (this.m_Mode != SERVER_MODE)
			throw new RuntimeException(
					"THIS METHOD CAN BE INVOKED ONLY IN SERVER_MODE");

		// Populating the Server Request
		ServerInterface serverReq = null;
		serverReq = new ServerInterface(m_AppletParameters.m_ServiceIdentifier,
				m_AppletParameters.m_ServerName,
				ServerInterface.CUSTOMER_ASSET_REQUEST);
		CustomerAssetRequest custAssetReq = null;
		serverReq.m_ServerRequest = new CustomerAssetRequest();
		custAssetReq = (CustomerAssetRequest) serverReq.m_ServerRequest;
		custAssetReq.m_CustomerId = m_AppletParameters.getIntVal("customer_id");
		custAssetReq.m_AssetType = assetType;
		custAssetReq.m_AssetId = assetId;
		custAssetReq.m_TemplateId = templateId;
		custAssetReq.m_ThemeId = themeId;
		custAssetReq.m_ImageBBox = imgBBox;
		custAssetReq.m_ImageSizeType = imageSizeType;

		// Making the Server Request
		ObjectInputStream dataStr = new ObjectInputStream(
				makeServerRequest(serverReq));
		ServerResponse servResp = (ServerResponse) ServerResponse.load(dataStr);
		dataStr.close();
		if (!servResp.m_IsSuccess)
			throw new RuntimeException("No Asset Returned from Server");
		return servResp.m_ResultValues;
	}

	// This retrieves the pdf asset from the server for viewing purpose.
	public HashMap getPDFPagesAsImage(Vector pdfPages) throws Exception {
		boolean usePDFTemplateAsset = false;
		return this.getPDFPagesAsImage(usePDFTemplateAsset, pdfPages,
				CustomerAssetRequest.REGULAR_IMAGE);
	}

	public HashMap getPDFPagesAsImage(boolean usePDFTemplateAsset,
			Vector pdfPages, int imgSizeType) throws Exception {
		if (this.m_Mode != SERVER_MODE)
			throw new RuntimeException(
					"THIS METHOD CAN BE INVOKED ONLY IN SERVER_MODE");

		// Populating the Server Request
		ServerInterface serverReq = null;
		serverReq = new ServerInterface(m_AppletParameters.m_ServiceIdentifier,
				m_AppletParameters.m_ServerName,
				ServerInterface.CUSTOMER_ASSET_REQUEST);
		CustomerAssetRequest custAssetReq = null;
		serverReq.m_ServerRequest = new CustomerAssetRequest();
		custAssetReq = (CustomerAssetRequest) serverReq.m_ServerRequest;
		custAssetReq.m_CustomerId = m_AppletParameters.getIntVal("customer_id");
		custAssetReq.m_AssetType = CustomerAssetRequest.PDF_ASSET_TYPE;
		if (usePDFTemplateAsset)
			custAssetReq.m_TemplateId = m_AppletParameters
					.getIntVal("template_id");
		else
			custAssetReq.m_AssetId = m_AppletParameters.m_DigitalAssetId;
		custAssetReq.m_PDFPages = pdfPages;
		custAssetReq.m_ImageSizeType = imgSizeType;

		// Making the Server Request
		ObjectInputStream dataStr = new ObjectInputStream(
				makeServerRequest(serverReq));
		ServerResponse servResp = (ServerResponse) ServerResponse.load(dataStr);
		dataStr.close();
		if (!servResp.m_IsSuccess)
			throw new RuntimeException("No Asset Returned from Server");
		return servResp.m_ResultValues;
	}

	private InputStream makeServerRequest(ServerInterface serverReq) {
		boolean isServerMode = false;
		if (this.m_Mode == SERVER_MODE)
			isServerMode = true;
		if (m_ServerProperties == null)
			m_ServerProperties = AppletParameters.createServerProperties(
					this.getClass(), isServerMode);
		// RGPTLogger.logToFile("Applet Params: " +
		// m_AppletParameters.toString());
		if (m_AppletParameters != null
				&& m_AppletParameters.m_AppletURL != null)
			return ServerProxy.makeServerRequest(
					m_AppletParameters.m_AppletURL, "VDPServer", serverReq);
		else if (AppletParameters.getVal("CodeBase") != null) {
			String url = AppletParameters.getVal("CodeBase");
			String servletName = AppletParameters.getVal("ServletName");
			RGPTLogger.logToFile("URL: " + url + " Servlet Name: "
					+ servletName, true);
			return ServerProxy.makeServerRequest(url, servletName, serverReq);
		} else {
			String url = m_ServerProperties.getProperty("URL");
			return ServerProxy.makeServerRequest(url, serverReq);
		}
	}
}