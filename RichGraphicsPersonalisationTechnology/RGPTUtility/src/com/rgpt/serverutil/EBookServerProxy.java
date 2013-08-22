// RGPT PACKAGES
package com.rgpt.serverutil;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import com.rgpt.imageutil.ImageUtils;
import com.rgpt.serverhandler.CustomerAssetRequest;
import com.rgpt.serverhandler.EBookPageHandler;
import com.rgpt.serverhandler.ServerInterface;
import com.rgpt.serverhandler.ServerResponse;
import com.rgpt.templateutil.PDFPageHolder;
import com.rgpt.util.AppletParameters;
import com.rgpt.util.BatchApprovalRequest;
import com.rgpt.util.RGPTLogger;
import com.rgpt.util.RGPTParams;

public class EBookServerProxy implements EBookPageHandler {
	public ServerProxy m_Server;
	public AppletParameters m_AppletParameters;
	public static Properties m_ServerProperties = null;

	public EBookServerProxy(AppletParameters params) {
		m_AppletParameters = params;
	}

	// This function is called to retrieve Pages corresponding to the Page
	// Numbers from the Server
	public HashMap getPages(Vector pageNums) throws Exception {
		if (m_ServerProperties == null)
			m_ServerProperties = RGPTParams.createServerProperties(
					this.getClass(), true);
		BufferedImage pdfPage = null;
		PDFPageHolder pgHldr = null;
		HashMap pdfPages = null, pdfServerPages = null;
		pdfServerPages = this.getPDFPagesAsImage(pageNums);
		pdfPages = new HashMap();
		System.out.println("PDF Server Pages: " + pdfServerPages.toString());
		for (int i = 0; i < pageNums.size(); i++) {
			int pgNum = ((Integer) pageNums.elementAt(i)).intValue();
			pgHldr = (PDFPageHolder) pdfServerPages.get(new Integer(pgNum));
			pdfPages.put(pgNum, ImageUtils.getBufferedImage(pgHldr.m_ImgStr));
		}
		return pdfPages;
	}

	public HashMap getPDFDocApprovals(int totalPgCnt, int pdfPgCnt,
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
		// Populating the Server Requeste
		try {
			ServerInterface serverReq = null;
			AppletParameters params = m_AppletParameters;
			serverReq = new ServerInterface(params.m_ServiceIdentifier,
					params.m_ServerName, ServerInterface.BATCH_APPROVAL_REQUEST);
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

	// This retrieves the pdf asset from the server for viewing purpose.
	public HashMap getPDFPagesAsImage(Vector pdfPages) throws Exception {
		// Populating the Server Request
		ServerInterface serverReq = null;
		serverReq = new ServerInterface(m_AppletParameters.m_ServiceIdentifier,
				m_AppletParameters.m_ServerName,
				ServerInterface.CUSTOMER_ASSET_REQUEST);
		serverReq.m_ServerRequest = new CustomerAssetRequest();
		CustomerAssetRequest custAssetReq = null;
		custAssetReq = (CustomerAssetRequest) serverReq.m_ServerRequest;
		custAssetReq.m_CustomerId = m_AppletParameters.getIntVal("customer_id");
		custAssetReq.m_AssetType = CustomerAssetRequest.PDF_ASSET_TYPE;
		custAssetReq.m_AssetId = m_AppletParameters.m_DigitalAssetId;
		custAssetReq.m_PDFPages = pdfPages;
		custAssetReq.m_ImageSizeType = CustomerAssetRequest.REGULAR_IMAGE;

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
		if (m_AppletParameters.m_AppletURL != null)
			return ServerProxy.makeServerRequest(
					m_AppletParameters.m_AppletURL, "VDPServer", serverReq);
		else {
			String url = m_ServerProperties.getProperty("URL");
			return ServerProxy.makeServerRequest(url, serverReq);
		}
	}

}