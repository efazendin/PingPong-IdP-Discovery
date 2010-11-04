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


public class PingPongGetIdpsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger _logger = Logger.getLogger(PingPongGetIdpsServlet.class);
	
	private static final String VELOCITY_TEMPLATE_PATH = "/WEB-INF/velocity";
	private static final String V_START_SSO_PARAM = "startSSOUrl";
	private static final String V_ALL_IDPS_TEMPLATE = "all-idps.vm";
	
	// TODO Will there be any threading issues?
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

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		List<IdentityProvider> idps;
		IdentityProviderPager idpPager = (IdentityProviderPager)req.getSession().getAttribute(IdentityProviderPager.IDP_PAGER);
		
		
		// TODO I noticed that the logos are being displayed in reverse prioritized order.  Figure out why.
		VelocityContext context = new VelocityContext();
		String htmlEncodedIdpsStartSSOUrl;

		
		// Set up the list of links for IdPs that have authenticated the user
		boolean anyAuthnedUser = idpPager.haveAnyAuthnedUser();
		
		if (anyAuthnedUser) {
			idps = idpPager.getUsersIdps();
			
			_logger.debug("PingPong found " + idps.size() + " IdP(s) have previously authenticated the user.");
			
			for (IdentityProvider idp : idps) {
				htmlEncodedIdpsStartSSOUrl = StringEscapeUtils.escapeHtml(idp.getStartSSOUrl());
				context.put(V_START_SSO_PARAM, htmlEncodedIdpsStartSSOUrl);
				_logger.debug("startSSOUrl: " + htmlEncodedIdpsStartSSOUrl);

		        try {
		        	// Write velocity transformed HTML to HTTP response.
					ve.mergeTemplate(idp.getFileName(), "utf-8", context, resp.getWriter());
				} catch (Exception e) {
					_logger.error("There was an error processing the IdP link template", e);
				}	
			}
		}


		if (anyAuthnedUser)
			resp.getWriter().write("<p>Or select your Identity Provider from the list below.</p>");
		else
			resp.getWriter().write("<p>Select your Identity Provider from the list below.</p>");


		// Set up the dropdown for all IdPs		
		Prioritizer idpPrioritizer = new NamePrioritizer();
    	List<IdentityProvider> nameOrderedIdps;
    	nameOrderedIdps = idpPrioritizer.prioritize(idpPager.getAllIdentityProviders(), this.getServletContext(), req, resp);

		for (IdentityProvider idp : nameOrderedIdps) {
			htmlEncodedIdpsStartSSOUrl = StringEscapeUtils.escapeHtml(idp.getStartSSOUrl());
			_logger.debug("startSSOUrl: " + htmlEncodedIdpsStartSSOUrl);
			idp.setStartSSOUrl(htmlEncodedIdpsStartSSOUrl);
		}
		context = new VelocityContext();
		context.put("idpList", nameOrderedIdps);

		try {
			ve.mergeTemplate(V_ALL_IDPS_TEMPLATE, "utf-8", context, resp.getWriter());
		} catch (Exception e) {
			_logger.error("There was an error processing the all IdPs select dropdown template", e);
		}

		resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		resp.setHeader("Pragma", "no-cache");
	}

}
