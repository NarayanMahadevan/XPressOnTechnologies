// RGPT PACKAGES
package com.rgpt.fileuploader;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Vector;
import java.util.zip.ZipEntry;

import com.rgpt.serverhandler.ServerInterface;
import com.rgpt.serverhandler.UploadAssetRequest;
import com.rgpt.serverutil.HTTPDocumentUpload;
import com.rgpt.util.AppletParameters;
import com.rgpt.util.NotificationHandlerInterface;
// This classes are needed to make URL Connection and read the Stream

public class HTTPFileUpload implements FileUploadInterface,
		NotificationHandlerInterface {
	FileUploadViewer m_FileUploadViewer;

	public void registerFileUploadViewer(FileUploadViewer fuViewer) {
		m_FileUploadViewer = fuViewer;
	}

	// This returns Any Action the Caller Object should Perform
	public int getProgressAction() {
		if (m_FileUploadViewer.m_PauseUpload)
			return NotificationHandlerInterface.ACTION_PAUSE;
		else
			return NotificationHandlerInterface.ACTION_CONTINUE;
	}

	// The status is notified by the Caller Object to the Call Back Object
	public void notifyProgress(String source, int status, HashMap result) {
		System.out.println("Current Status: "
				+ HTTPDocumentUpload.m_NotificationStatus.get(status)
				+ "\n Result Data: " + result.toString());
	}

	// This method needs to gather up each of the files the user has selected,
	// zip those files together, and send that zip file up to the server.
	public HashMap uploadFile(AppletParameters params, Vector files)
			throws Exception {
		ZipEntry entry = null;
		HashMap result = null;
		HttpURLConnection conn = null;
		ServerInterface serverReq = null;
		if (files.size() == 0)
			return null;

		HTTPDocumentUpload httpDocUpldr = new HTTPDocumentUpload();
		httpDocUpldr.registerNotificationHandler(this);
		HashMap connInfo = new HashMap();
		connInfo.put("URLCodeBase", params.m_AppletURL.toString());
		connInfo.put("FileUpldServletName", "FileUploadServer");
		connInfo.put("FileUpldInfoServletName", "FileUploadInfoServer");
		int fileUpldId = httpDocUpldr.getFileUploadId(connInfo);

		// Creating Server Request for Uploading Files
		serverReq = this.makeServerRequest(params, fileUpldId, files.size());
		UploadAssetRequest uploadAssetReq = null;
		uploadAssetReq = (UploadAssetRequest) serverReq.m_ServerRequest;
		// This check is done to upload File Completly or upload only the
		// Network
		// Path. If true then only the Path is uploaded
		if (params.getBoolVal("upload_file_path")) {
			uploadAssetReq.m_IsUploadFilePath = true;
			uploadAssetReq.m_UploadFilePaths = new Vector<String>();
			File f = null;
			for (int i = 0; i < files.size(); i++) {
				// For each file we will create a new entry in the ZIP archive
				// and
				// stream the file into that entry.
				f = (File) files.elementAt(i);
				if (i == 0) {
					uploadAssetReq.m_UploadFileName = f.getName();
				}
				uploadAssetReq.m_UploadFilePaths.add(f.getAbsolutePath());
			}
			files.removeAllElements();
		}
		httpDocUpldr.uploadDocument(connInfo, serverReq, fileUpldId, files,
				uploadAssetReq.m_IsUploadFilePath);
		m_FileUploadViewer.finishedUpload(files);
		return result;
	}

	private ServerInterface makeServerRequest(AppletParameters params,
			int fileUpldId, int numOfFiles) throws Exception {
		UploadAssetRequest uploadAssetReq = null;
		ServerInterface serverReq = null;
		// int pspid = params.m_ServiceIdentifier;
		int pspid = params.getIntVal("psp_id");
		String serverName = params.getVal("server_name");
		int serverReqIndicator = ServerInterface.UPLOAD_ASSET_REQUEST;
		serverReq = new ServerInterface(pspid, serverName, serverReqIndicator);
		serverReq.m_ServerRequest = new UploadAssetRequest();
		uploadAssetReq = (UploadAssetRequest) serverReq.m_ServerRequest;
		uploadAssetReq.m_FileUploaderId = fileUpldId;
		uploadAssetReq.m_CustomerId = params.getIntVal("customer_id");
		uploadAssetReq.m_CustomerAccountId = params
				.getIntVal("customer_account_id");
		uploadAssetReq.m_TemplateId = params.getIntVal("template_id");
		uploadAssetReq.m_NumberOfUploadedFiles = numOfFiles;
		uploadAssetReq.m_FileType = params.getVal("file_type");
		if (params.m_RequestType.equals(AppletParameters.UPLOAD_PSP_ASSET)) {
			uploadAssetReq.m_UploadMode = UploadAssetRequest.UPLOAD_PSP_ASSET;
			uploadAssetReq.m_SessionId = params.getVal("session_id");
			String assetType = params.getVal("asset_type");
			if (assetType.equals(AppletParameters.PDF_TEMPLATE_ASSET))
				uploadAssetReq.m_AssetType = UploadAssetRequest.PDF_TEMPLATE_ASSET;
			else if (assetType.equals(AppletParameters.FAST_SHOPPING_ASSET))
				uploadAssetReq.m_AssetType = UploadAssetRequest.FAST_SHOPPING_ASSET;
		} else if (params.m_RequestType
				.equals(AppletParameters.UPLOAD_BATCH_FILE))
			uploadAssetReq.m_UploadMode = UploadAssetRequest.UPLOAD_BATCH_FILE;
		else if (params.m_RequestType
				.equals(AppletParameters.UPLOAD_DIGITAL_ASSETS))
			uploadAssetReq.m_UploadMode = UploadAssetRequest.UPLOAD_DIGITAL_ASSET;
		else if (params.m_RequestType
				.equals(AppletParameters.UPLOAD_UPDATED_ASSET)) {
			uploadAssetReq.m_UploadMode = UploadAssetRequest.UPLOAD_UPDATED_ASSET;
			uploadAssetReq.m_DigitalAssetId = params.getIntVal("orig_asset_id");
			uploadAssetReq.m_OrderId = params.getIntVal("order_id");
			uploadAssetReq.m_OrderJobId = params.getIntVal("order_job_id");
		} else
			throw new RuntimeException("Uploade Mode Not Recognized");
		System.out.println("Upload Req: " + uploadAssetReq.toString());
		return serverReq;
	}
}