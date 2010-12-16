package com.pingidentity.efazendin.pingpong.sp;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.pingidentity.efazendin.pingpong.sp.model.IdentityProvider;
import com.pingidentity.efazendin.pingpong.sp.model.IdentityProviderPager;
import com.pingidentity.efazendin.pingpong.sp.prioritizers.NamePrioritizer;
import com.pingidentity.efazendin.pingpong.sp.prioritizers.Prioritizer;
import com.pingidentity.efazendin.pingpong.sp.util.VelocityEngineFactory;

/**
 * This servlet is called via ajax once the {@link PongStatusServlet} has
 * indicated that either one or more IdPs were found for this user or PingPong
 * has completed.
 * 
 * @author efazendin
 * 
 */
public class PingPongGetIdpsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger _logger = Logger.getLogger(PingPongGetIdpsServlet.class);
	
	private static final String VELOCITY_TEMPLATE_PATH = "/WEB-INF/velocity";
	private static final String V_START_SSO_PARAM = "startSSOUrl";
	private static final String V_ALL_IDPS_TEMPLATE = "all-idps.vm";
	
	private VelocityEngine ve;

    public PingPongGetIdpsServlet() {
        super();

    }

	public void init() throws ServletException {
		super.init();
		
		// Initialize a local velocity engine
    	String velocityTemplatePath = this.getServletContext().getRealPath(VELOCITY_TEMPLATE_PATH);
    	ve = VelocityEngineFactory.getNewVelocityEngine(velocityTemplatePath);

	}

	/**
	 * If any IdP responded and indicated a user in this browser context has
	 * authenticated with them previously, this will first create the html to be
	 * displayed to the user showing these IdPs. Afterwards, it will generate
	 * the html that diplays all possible IdPs.
	 * 
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		IdentityProviderPager idpPager = (IdentityProviderPager)req.getSession().getAttribute(IdentityProviderPager.IDP_PAGER);
		
		VelocityContext vContext = new VelocityContext();
		String htmlEncodedIdpsStartSSOUrl;

		// Set up the list of links for IdPs that have authenticated the user
		if (idpPager.haveAnyAuthnedUser()) {
			List<IdentityProvider> idps = idpPager.getUsersIdps();
			
			for (IdentityProvider idp : idps) {
				htmlEncodedIdpsStartSSOUrl = StringEscapeUtils.escapeHtml(idp.getStartSSOUrl());
				vContext.put(V_START_SSO_PARAM, htmlEncodedIdpsStartSSOUrl);
				
				_logger.debug("startSSOUrl for user's IdP: " + htmlEncodedIdpsStartSSOUrl);

		        try {
		        	// Write velocity transformed HTML to HTTP response.
					ve.mergeTemplate(idp.getFileName(), "utf-8", vContext, resp.getWriter());
				} catch (Exception e) {
					_logger.error("There was an error processing the IdP link template", e);
				}	
			}
			resp.getWriter().write("<p>Or select your Identity Provider from the list below.</p>");
			
			_logger.debug("PingPong found " + idps.size() + " IdP(s) have previously authenticated the user.");
		} else {
			resp.getWriter().write("<p>Select your Identity Provider from the list below.</p>");
		}


		// Set up the dropdown for all IdPs
		Prioritizer idpPrioritizer = new NamePrioritizer();
    	List<IdentityProvider> nameOrderedIdps = idpPrioritizer.prioritize(idpPager.getAllIdentityProviders(), req, resp);
    	
		for (IdentityProvider idp : nameOrderedIdps) {
			htmlEncodedIdpsStartSSOUrl = StringEscapeUtils.escapeHtml(idp.getStartSSOUrl());
			idp.setStartSSOUrl(htmlEncodedIdpsStartSSOUrl);
			
			_logger.debug("startSSOUrl for dropdown: " + htmlEncodedIdpsStartSSOUrl);
		}
		vContext = new VelocityContext();
		vContext.put("idpList", nameOrderedIdps);

		try {
			ve.mergeTemplate(V_ALL_IDPS_TEMPLATE, "utf-8", vContext, resp.getWriter());
		} catch (Exception e) {
			_logger.error("There was an error processing the all IdPs select dropdown template", e);
		}

		resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		resp.setHeader("Pragma", "no-cache");
	}

}
