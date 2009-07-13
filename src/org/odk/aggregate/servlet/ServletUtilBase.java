/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate.servlet;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.constants.HtmlConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.ServletConsts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base class for Servlets that contain useful utilities
 *
 */
@SuppressWarnings("serial")
public class ServletUtilBase extends HttpServlet {

  /**
   * Takes the request and displays request in plain text in the response
   * @param req
   *    The HTTP request received at the server
   * @param resp
   *    The HTTP response to be sent to client
   * @throws IOException
   */
  protected void printRequest(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    try {
      BufferedReader received = req.getReader();

      String line = received.readLine();
      while (line != null) {
        resp.getWriter().println(line);
        line = received.readLine();
      }
    } catch (Exception e) {
      e.printStackTrace(resp.getWriter());
    }
  }

  /**
   * Removes HTML sensitive characters and replaces them with the 
   * HTML versions that can be properly displayed.
   * For example: '<' becomes '&lt;'
   * 
   * @param htmlString
   *    string of HTML that will have its HTML sensitive characters removed
   * @return
   *    string of HTML that has been replace with proper HTML display characters
   */
  protected String formatHtmlString(String htmlString) {
    String formatted = htmlString;
    formatted = formatted.replace(BasicConsts.LESS_THAN, HtmlConsts.LESS_THAN);
    formatted = formatted.replace(BasicConsts.GREATER_THAN, HtmlConsts.GREATER_THAN);
    formatted = formatted.replace(BasicConsts.NEW_LINE, HtmlConsts.LINE_BREAK);
    formatted = formatted.replace(BasicConsts.TAB, HtmlConsts.TAB);
    formatted = formatted.replace(BasicConsts.SPACE, HtmlConsts.SPACE);
    return formatted;
  }

  /**
   * Takes request and verifies the user has logged in. If the user has not
   * logged in generates the appropriate text for response to user
   * 
   * @param req
   *    The HTTP request received at the server
   * @param resp
   *    The HTTP response to be sent to client
   * @return
   *    boolean value of whether the user is logged in
   * @throws IOException
   *    Throws IO Exception if problem occurs creating the login link in response
   */
  protected boolean verifyCredentials(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      beginBasicHtmlResponse("Login Required", resp, false);
      String returnUrl = req.getRequestURI() + ServletConsts.BEGIN_PARAM + req.getQueryString();
      String loginHtml = HtmlUtil.wrapWithHtmlTags(HtmlConsts.P, "Please " + HtmlUtil.createHref(userService.createLoginURL(returnUrl), "log in"));
      resp.getWriter().print(loginHtml);
      finishBasicHtmlResponse(resp);
      return false;
    }
    return true;
  }

  /**
   * Generate HTML header string for web responses.
   * NOTE: beginBasicHtmlResponse and finishBasicHtmlResponse 
   * are a paired set of functions. beginBasicHtmlResponse should be called 
   * first before adding other information to the http response. When response is finished
   * finishBasicHtmlResponse should be called.
   * 
   * @param pageName
   *    name that should appear on the top of the page 
   * @param resp 
   *    http response to have the information appended to
   * @param displayLinks 
   *    display links accross the top
   * @throws IOException 
   */
  protected void beginBasicHtmlResponse(String pageName, HttpServletResponse resp, boolean displayLinks) throws IOException {
    resp.setContentType(ServletConsts.RESP_TYPE_HTML);
    resp.setCharacterEncoding(ServletConsts.ENCODE_SCHEME);
    PrintWriter out = resp.getWriter();
    out.write(HtmlConsts.HTML_OPEN);
    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.HEAD, HtmlUtil.wrapWithHtmlTags(HtmlConsts.TITLE, BasicConsts.APPLICATION_NAME)));
    out.write(HtmlConsts.BODY_OPEN);
    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.H2, BasicConsts.APPLICATION_NAME)); 
    if(displayLinks) {
      UserService userService = UserServiceFactory.getUserService();
      out.write(generateNavigationInfo());
      out.write(HtmlConsts.TAB);
      out.write(HtmlUtil.createHref(userService.createLogoutURL("/"), "Log Out from " + userService.getCurrentUser().getNickname()));
    }
    out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.H1, pageName));
  }

  /**
   * Generate HTML footer string for web responses
   * @param resp 
   *    http response to have the information appended to
   * @throws IOException 
   */
  protected void finishBasicHtmlResponse(HttpServletResponse resp) throws IOException {
    resp.getWriter().write(HtmlConsts.BODY_CLOSE + HtmlConsts.HTML_CLOSE);
  }

  /**
   * Generate error response for ODK ID not found
   * 
   * @param resp
   *    The HTTP response to be sent to client
   * @throws IOException
   *    caused by problems writing error information to response
   */
  protected void odkIdNotFoundError(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_NOT_FOUND, ErrorConsts.ODKID_NOT_FOUND);
  }

  /**
   * Generate error response for missing the Key parameter
   * 
   * @param resp
   *    The HTTP response to be sent to client
   * @throws IOException
   *    caused by problems writing error information to response
   */
  protected void errorMissingKeyParam(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.ODK_KEY_PROBLEM);
  }
  
  /**
   * Generate error response for missing the Key parameter
   * 
   * @param resp
   *    The HTTP response to be sent to client
   * @throws IOException
   *    caused by problems writing error information to response
   */
  protected void errorRetreivingData(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.INCOMPLETE_DATA);
  }

  /**
   * Generate error response for missing parameters in request
   * 
   * @param resp
   *    The HTTP response to be sent to client
   * @throws IOException
   *    caused by problems writing error information to response
   */
  protected void sendErrorNotEnoughParams(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.INSUFFIECENT_PARAMS);
  }
  
  /**
   * Extract the parameter from HTTP request and return the decoded value. Returns null if 
   * parameter not present
   * 
   * @param req
   *    HTTP request that contains the parameter
   * @param parameterName
   *    the name of the parameter to be retrieved
   * @return
   *    Parameter's decoded value or null if not found
   * 
   * @throws UnsupportedEncodingException
   */
  protected String getParameter(HttpServletRequest req, String parameterName) throws UnsupportedEncodingException {
    String encodedParamter = req.getParameter(parameterName);
    String parameter = null;
  
    if (encodedParamter != null) {
      parameter = URLDecoder.decode(encodedParamter, ServletConsts.ENCODE_SCHEME);
    }
    return parameter;
  }


  /**
   * Generate common navigation links
   * @return
   *    a string with href links
   */
  public String generateNavigationInfo() {
    String html = HtmlUtil.createHref(FormsServlet.ADDR, ServletConsts.FORMS_LINK_TEXT);
    html += HtmlConsts.TAB;
    html += HtmlUtil.createHref(FormUploadServlet.ADDR, ServletConsts.UPLOAD_FORM_LINK_TEXT);
    html += HtmlConsts.TAB;
    html += HtmlUtil.createHref(ServletConsts.UPLOAD_SUBMISSION_ADDR, ServletConsts.UPLOAD_SUB_LINK_TEXT);
    return html + HtmlConsts.TAB;
  }

  protected void setDownloadFileName(HttpServletResponse resp, String filename) {
    resp.setHeader(ServletConsts.CONTENT_DISPOSITION, ServletConsts.ATTACHMENT_FILENAME_TXT + filename + BasicConsts.QUOTE + BasicConsts.SEMI_COLON);
  }  
  
}
