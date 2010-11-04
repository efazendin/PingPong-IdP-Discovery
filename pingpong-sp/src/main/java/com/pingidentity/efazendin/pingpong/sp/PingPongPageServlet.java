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
import com.pingidentity.efazendin.pingpong.sp.util.HttpUtil;
import com.pingidentity.efazendin.pingpong.sp.util.VelocityEngineFactory;


public class PingPongPageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger _logger = Logger.getLogger(PingPongPageServlet.class);
	
	private static final String IDP_PARAM = "idp";
	private static final String PONG_HANDLER_PARAM = "pongHandler";
	private static final String PONG_HANDLER_URL_PARAM = "pong.handler.url";
	private static String pongHandlerUrl;
	
	private static final String V_PING_HANDLER_PARAM = "pingHandlerUrl";
	private static final String V_PINGPONG_IFRAME_TEMPLATE = "pingpong-iframe.vm";
	private static final String VELOCITY_TEMPLATE_PATH = "/WEB-INF/velocity";
	
	private VelocityEngine ve;
	
	@Override
	public void init() throws ServletException {
		
		// Initialize a local velocity engine
    	String velocityTemplatePath = this.getServletContext().getRealPath(VELOCITY_TEMPLATE_PATH);
    	ve = VelocityEngineFactory.getNewVelocityEngine(velocityTemplatePath);

		pongHandlerUrl = this.getServletContext().getInitParameter(PONG_HANDLER_URL_PARAM);
	}


	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		_logger.debug("HTTP REQUEST:\n" + req);
				
		IdentityProviderPager idpPager = (IdentityProviderPager)req.getSession().getAttribute(IdentityProviderPager.IDP_PAGER);
		List<IdentityProvider> idps = idpPager.getNextPage();
		

		VelocityContext context = new VelocityContext();
		for (IdentityProvider idp : idps) {
			context.put(V_PING_HANDLER_PARAM, getPingHandlerUrl(idp.getEntityId(), idp.getPingHandlerUrl(), pongHandlerUrl));
			
			try {
				ve.mergeTemplate(V_PINGPONG_IFRAME_TEMPLATE, "utf-8", context, resp.getWriter());
			} catch (Exception e) {
				_logger.error("There was an error creating the PingPong iframe.", e);
			}
		}

		resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		resp.setHeader("Pragma", "no-cache");
	}


	private String getPingHandlerUrl(String idpEntityId, String pingHandlerUrl, String pongHandlerUrl) {

		String pongHandlerWithParams = HttpUtil.addParameterToUrl(pongHandlerUrl, IDP_PARAM, idpEntityId);
		String pingWithPongHandlerUrl = HttpUtil.addParameterToUrl(pingHandlerUrl, PONG_HANDLER_PARAM, pongHandlerWithParams);
		String htmlEncodedPingHandlerUrl = StringEscapeUtils.escapeHtml(pingWithPongHandlerUrl);
		
		_logger.debug("Ping handler URL: " + htmlEncodedPingHandlerUrl);
		
		return htmlEncodedPingHandlerUrl;
	}

}
