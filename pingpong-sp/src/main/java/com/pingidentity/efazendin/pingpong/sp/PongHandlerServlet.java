package com.pingidentity.efazendin.pingpong.sp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pingidentity.efazendin.pingpong.sp.model.IdentityProviderPager;

public class PongHandlerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger _logger = Logger.getLogger(PongHandlerServlet.class);
    
	private static final String AUTHNED_USER_PARAM = "authnedUser";
	private static final String IDP_PARAM = "idp";

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String idp = req.getParameter(IDP_PARAM);
		String authnedUser = req.getParameter(AUTHNED_USER_PARAM);

		IdentityProviderPager idpPager = (IdentityProviderPager)req.getSession().getAttribute(IdentityProviderPager.IDP_PAGER);
		
		if ((idp != null) && (authnedUser != null) && (idpPager != null))
			idpPager.updateHasAuthenticatedUser(idp, Boolean.valueOf(authnedUser));
		
		// TODO We don't really need to print this out.  This was just for debugging.
		resp.getOutputStream().println(idp);
		resp.getOutputStream().println("\n");
		resp.getOutputStream().println("AuthNedUser: " + authnedUser);
		
	}

}
