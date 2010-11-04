package com.pingidentity.efazendin.pingpong.sp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

public class PingPongSetIdpIdServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final Logger _logger = Logger.getLogger(PingPongSetIdpIdServlet.class);

	private static final String IDP_ID_COOKIE_NAME = "IdpId";
	private static int idpIdCookieLifeTimeInSecs = 5 * 365 * 24 * 3600;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String idpIdQueryParameter = req.getParameter(IDP_ID_COOKIE_NAME);
		
		if (idpIdQueryParameter != null) {
			String hexIdpId = new String(Hex.encodeHex(idpIdQueryParameter.getBytes()));
			Cookie idpId = new Cookie(IDP_ID_COOKIE_NAME, hexIdpId);
			idpId.setMaxAge(idpIdCookieLifeTimeInSecs);
			idpId.setPath("/");
		
			resp.addCookie(idpId);
		} else {
			_logger.warn("Missing 'IdpId' query parameter.");
		}
	}

}
