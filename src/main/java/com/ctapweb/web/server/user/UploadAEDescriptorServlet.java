package com.ctapweb.web.server.user;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ctapweb.web.server.admin.AdminServiceImpl;
import com.ctapweb.web.server.admin.ImportAE;
import com.ctapweb.web.server.logging.LogMarker;
import com.ctapweb.web.server.logging.ServiceRequestReceivedMessage;
import com.ctapweb.web.shared.AnalysisEngine.AEType;

public class UploadAEDescriptorServlet extends HttpServlet implements Servlet {

	private long FILE_SIZE_LIMIT = 100 * 1024 * 1024; // 100 MiB
	private static final Logger logger = LogManager.getLogger();
	private AdminServiceImpl adminServiceImpl = new AdminServiceImpl();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws ServletException, IOException {
		String serviceName = "uploadAEDescriptor";
		logger.info(LogMarker.CTAP_SERVER_MARKER, 
				new ServiceRequestReceivedMessage(serviceName)); 

		try {
			//check if user admin
			if(!adminServiceImpl.isUserAdmin(req)) {
				resp.sendError(HttpServletResponse.SC_FORBIDDEN, 
						"User not logged in or non admin. Service refused.");
				return;
			}

			//check passes
			DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
			ServletFileUpload fileUpload = new ServletFileUpload(fileItemFactory);
			fileUpload.setSizeMax(FILE_SIZE_LIMIT);

			List<FileItem> items = fileUpload.parseRequest(req);

			// get the file and populate it into the DB 
			for (FileItem item : items) {
				if (!item.isFormField()) {
					// check size
					if (item.getSize() > FILE_SIZE_LIMIT) {
						resp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, 
								"File size exceeds limit.");
						return;
					}

					//save the uploaded file in temp directory
					String fileName = item.getName();
					String aeType = fileName.endsWith(AEType.ANNOTATOR) ? 
							AEType.ANNOTATOR : AEType.FEATURE_EXTRACTOR;
					File uploadedFile =new File(FileUtils.getTempDirectory(), fileName);
					item.write(uploadedFile);

					// save ae info into database
					ImportAE importAE = new ImportAE();
					importAE.importDescriptorFile(uploadedFile, aeType);
					
					//remove temp file
					FileUtils.deleteQuietly(uploadedFile);

					if (!item.isInMemory())
						item.delete();
				}
			}
		} catch (Exception e) {
			resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED,
					"Exceptions occurred during file upload. Check the system log for more details.");
			throw logger.throwing(new ServletException(e));
		}

	}
}