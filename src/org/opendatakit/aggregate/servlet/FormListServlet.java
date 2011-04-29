/*
 * Copyright (C) 2009 Google Inc. 
 * Copyright (C) 2010 University of Washington.
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.format.form.FormXmlTable;
import org.opendatakit.aggregate.format.form.XFormsXmlTable;
import org.opendatakit.aggregate.query.QueryFormList;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * Servlet to generate the XML list of forms to be presented as the API for
 * forms for computers
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FormListServlet extends ServletUtilBase {
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 13236849409070038L;

  /**
   * URI from base
   */
  public static final String ADDR = "formList";

  /**
   * Handler for HTTP Get request that responds with an XML list of forms to
   * download
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	CallingContext cc = ContextFactory.getCallingContext(this, req);

	if ( getOpenRosaVersion(req) != null ) {
		// OpenRosa implementation
	  	addOpenRosaHeaders(resp);
	    try {
	      QueryFormList formsList = new QueryFormList(false, cc);
	      XFormsXmlTable formFormatter = new XFormsXmlTable(formsList, cc.getServerURL());

	      resp.setContentType(HtmlConsts.RESP_TYPE_XML);
	      formFormatter.generateXmlListOfForms(resp.getWriter());
	    } catch (ODKDatastoreException e) {
	      errorRetreivingData(resp);
	    } catch (ODKIncompleteSubmissionData e) {
		  // TODO Auto-generated catch block
		  e.printStackTrace();
		  errorRetreivingData(resp);
		}
	} else {
		// Collect 1.1.5 legacy app
	    try {
	      QueryFormList formsList = new QueryFormList(false, cc);
	      FormXmlTable formFormatter = new FormXmlTable(formsList, cc.getServerURL());
	
	      resp.setContentType(HtmlConsts.RESP_TYPE_XML);
	      resp.getWriter().print(formFormatter.generateXmlListOfForms());
	    } catch (ODKDatastoreException e) {
	      errorRetreivingData(resp);
	    } catch (ODKIncompleteSubmissionData e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		      errorRetreivingData(resp);
		}
	}
  }

}
