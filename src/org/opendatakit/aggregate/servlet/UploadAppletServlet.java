/*
 * Copyright (C) 2010 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.aggregate.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * The only purpose of this servlet is to insert Set-Cookie META tags into the
 * HEAD section of the Upload Applet page being vended.  UPLOAD_BODY is a
 * string representation of the HTML body for the Upload Applet.
 * 
 * Copied from BriefcaseServlet.java with a few changes.
 * 
 * @author the.dylan.price@gmail.com
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 * 
 */
public class UploadAppletServlet extends ServletUtilBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5121979982542017092L;

	/**
	 * URI from base
	 */
	public static final String ADDR = "UploadApplet";

	/**
	 * Title for generated webpage
	 */
	private static final String TITLE_INFO = "ODK Upload";
	
	/**
	 * Upload Applet body
	 */
	private static final String UPLOAD_BODY = 
"<h1>Upload data to ODK Aggregate</h1>" +
"\n<object type=\"application/x-java-applet\" height=\"300\" width=\"900\" >" +
"\n  <param name=\"jnlp_href\" value=\"upload/opendatakit-upload.jnlp\" />" +
"\n  <param name=\"mayscript\" value=\"true\" />" +
"\n</object>" +
"\n<script language=\"JavaScript\"><!--" +
"\n  document.write('<p>Cookies: '+document.cookie+'</p>')" +
"\n  document.write('<p>Location: '+location.href+'</p>');" +
"\n  //-->" +
"\n</script>";

	/**
	 * Handler for HTTP Get request to create blank page that is navigable
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		// verify user is logged in
		if (!verifyCredentials(req, resp)) {
			return;
		}

		String cookieSet = "";
		Cookie[] cookies = req.getCookies();
		if ( cookies != null ) {
			for ( Cookie c : cookies ) {
				String aDef = "<META HTTP-EQUIV=\"Set-Cookie\" CONTENT=\"" + 
								c.getName() + "=" + c.getValue() + "\" />\n";
				resp.addCookie(c);
				cookieSet += aDef;
				
			}
		}
		String headContent = cookieSet;
		beginBasicHtmlResponse(TITLE_INFO, headContent, resp, req, true); // header
	    PrintWriter out = resp.getWriter();
	    out.write(UPLOAD_BODY);
		finishBasicHtmlResponse(resp);
		resp.setStatus(200);
	}
}
