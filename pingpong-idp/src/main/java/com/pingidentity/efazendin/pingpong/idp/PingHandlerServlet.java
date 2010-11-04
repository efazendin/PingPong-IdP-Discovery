package com.pingidentity.efazendin.pingpong.idp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pingidentity.efazendin.pingpong.idp.util.HttpUtil;

public class PingHandlerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger _logger = Logger.getLogger(PingHandlerServlet.class);

	private static final String PONG_HANDLER_PARAM = "pongHandler";
	private static final String AUTHNED_USER_PARAM = "authnedUser";

	private static final String WHITELIST_FILE = "WEB-INF/classes/pong-whitelist.txt";
	private Set<String> whitelist;
	
    public PingHandlerServlet() {
    
    }

    
	public void init() throws ServletException {
		super.init();
		
		whitelist = new HashSet<String>();
		
		String filePath = this.getServletContext().getRealPath(WHITELIST_FILE);
		try {
			BufferedReader input =  new BufferedReader(new FileReader(filePath));
			try {
				String line = null;
				while (( line = input.readLine()) != null){
					if ((!line.trim().startsWith("#")) && (!line.trim().equals("")))
						whitelist.add(line.trim());
				}
			}
			finally {
				input.close();
			}
		}
		catch (IOException e){
			_logger.error("There was an error reading in the sp-whitelist.txt file", e);
		}
	}


	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		//_logger.debug("HTTP REQUEST:\n" + req);
		
		String redirectUrl = req.getParameter(PONG_HANDLER_PARAM);
		
		
		if (HttpUtil.hasLoggedInHere(req)) {
			redirectUrl = HttpUtil.addParameterToUrl(redirectUrl, AUTHNED_USER_PARAM, "true");
		} else {
			redirectUrl = HttpUtil.addParameterToUrl(redirectUrl, AUTHNED_USER_PARAM, "false");
		}
		

		// Check whitelist
		if (whitelistApproved(redirectUrl))
			resp.sendRedirect(redirectUrl);
	}

	
	private boolean whitelistApproved(String redirectUrl) {
		boolean result = false;
		
		if (whitelist.isEmpty())
			result = true;
		else {
			URL theUrl;
			try {
				theUrl = new URL(redirectUrl);
				if (whitelist.contains(theUrl.getHost())) {
					result = true;
				} else {
					_logger.warn("Pong URL not in pong-whitelist.txt.");
					_logger.warn("Pong URL: " + redirectUrl);
					_logger.warn("Whitelist: " + whitelist);
				}
			} catch (MalformedURLException e) {
				_logger.error("Error parsing Pong URL: " + redirectUrl, e);
			}
		}
				
		
		
		return result;
	}

}
