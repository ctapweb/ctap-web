package com.ctapweb.web.server.user;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Response;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ctapweb.web.server.DBConnectionManager;
import com.ctapweb.web.server.Utils;
import com.ctapweb.web.shared.CorpusText;
import com.ctapweb.web.shared.exception.DatabaseException;
import com.ctapweb.web.shared.exception.UserNotLoggedInException;

public class ErrorHandlerServlet extends HttpServlet {

	private static final Logger logger = LogManager.getLogger();
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// Analyze the servlet exception       
		Throwable throwable = (Throwable)
				req.getAttribute("javax.servlet.error.exception");
		Integer statusCode = (Integer)
				req.getAttribute("javax.servlet.error.status_code");
		String servletName = (String)
				req.getAttribute("javax.servlet.error.servlet_name");
		if (servletName == null){
			servletName = "Unknown";
		}
		String requestUri = (String)
				req.getAttribute("javax.servlet.error.request_uri");
		if (requestUri == null){
			requestUri = "Unknown";
		}

		// Set response content type
		resp.setContentType("text/html");

		PrintWriter out = resp.getWriter();

		out.println("<!doctype html>");
		out.println("<html lang=\"en\">");
		out.println("<head>");
		out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1\">");
		out.println("<meta charset=\"utf-8\">");
		out.println("<link rel=\"icon\" href=\"../../favicon.ico\">");
		out.println("<title>Complexity</title>");
		out.println("<!-- FONT AWESOME-->");
		out.println("<link rel=\"stylesheet\" href=\"../../css/font-awesome.min.css\">");
		out.println("<link rel=\"stylesheet\" href=\"../../css/bootstrap.css\" id=\"bscss\">");
		out.println("<link rel=\"stylesheet\" href=\"../../css/app.css\" id=\"maincss\">");
		out.println("</head>");
		out.println("<body>");
		out.println("<div class=\"wrapper\">");
		out.println("<div class=\"abs-center wd-xxxl\">");
		out.println("	<div class=\"p text-center\">");
		out.println("		<i class=\"fa fa-times fa-5x text-danger\" aria-hidden=\"true\"></i>");
		out.println("	</div>");
		out.println("<!-- START panel -->");
		out.println("<div class=\"panel widget b0\">");
		out.println("	<div class=\"panel-body\">");
		out.println("	<h3 class=\"text-center text-danger\">Serious Error Occurred!</h3>");
		out.println("<div class=\"text-danger\">");
	//error message goes here
//		if (throwable == null && statusCode == null){
//			out.println("<h2>Error information is missing</h2>");
//			out.println("Please return to the <a href=\"" + 
//					resp.encodeURL("http://localhost:8080/") + 
//					"\">Home Page</a>.");
//		}else if (statusCode != null){
//			out.println("The status code : " + statusCode);
//		}else{
			out.println("<h2>Error information</h2>");
			out.println("Servlet Name : " + servletName + 
					"</br></br>");
			out.println("Exception Type : " + 
					throwable.getClass( ).getName( ) + 
					"</br></br>");
			out.println("The request URI: " + requestUri + 
					"<br><br>");
			out.println("The exception message: " + 
					throwable.getMessage( ));
//		}
		
		out.println("</div>");
		out.println("<!-- <div class=\"clearfix\"> -->");
		out.println("<div class=\"pull-left mt-sm\">");
		out.println("		<small>");
		out.println("		You are seeing this message mostly because you are using");
		out.println("			this");
		out.println("		Web site in an inappropriate way or when a serious problem");
		out.println("			has");
		out.println("			occurred. You may choose to report this problem to us by");
		out.println("			writing");
		out.println("			us an email about what you encountered.");
		out.println("		</small>");
		out.println("			<!-- </div> -->");
		out.println("	</div>");
		out.println("</div>");
		out.println("</div>");
		out.println("<!-- END panel -->");
		out.println("<div class=\"p-lg text-center\">");
		out.println("<span>&copy;</span>");
		out.println("<span>2016</span>");
		out.println("<span>-</span>");
		out.println("<span>Xiaobin</span>");
		out.println("<br />");
		out.println("<span>Text Complexity Tools</span>");
		out.println("</div>");
		out.println("</div>");
		out.println("</div>");
		out.println("</body>");
		out.println("</html>");
	}
}












