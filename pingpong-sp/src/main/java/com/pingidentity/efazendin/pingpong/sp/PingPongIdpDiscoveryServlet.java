package com.pingidentity.efazendin.pingpong.sp;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.pingidentity.efazendin.pingpong.sp.model.IdentityProvider;
import com.pingidentity.efazendin.pingpong.sp.model.IdentityProviderAppFilter;
import com.pingidentity.efazendin.pingpong.sp.model.IdentityProviderLoader;
import com.pingidentity.efazendin.pingpong.sp.model.IdentityProviderMap;
import com.pingidentity.efazendin.pingpong.sp.model.IdentityProviderPager;
import com.pingidentity.efazendin.pingpong.sp.prioritizers.NamePrioritizer;
import com.pingidentity.efazendin.pingpong.sp.prioritizers.Prioritizer;
import com.pingidentity.efazendin.pingpong.sp.util.HttpUtil;
import com.pingidentity.efazendin.pingpong.sp.util.VelocityEngineFactory;

public class PingPongIdpDiscoveryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final Logger _logger = Logger.getLogger(PingPongIdpDiscoveryServlet.class);
	
	private static final String IDP_DISCOVERY_JSP_PATH = "/WEB-INF/jsp/idpDiscovery.jsp";
	private static final String PREVIOUS_IDP_JSP_PATH = "/WEB-INF/jsp/previousIdp.jsp";

	private static final String START_SSO_URL_PARAM = "startsso.url";
	private static final String START_SSO_PARAM = "startSSOParams";
	private static final String IDP_PROPS_FILE = "identity-providers.properties";
	private static final String PAGE_SIZE_PARAM = "page.size";
	private static final String PAGE_EXP_PARAM = "page.expiration";
	private static final String APP_ID_PARAM = "appId";
	private static final String IGNORE_IDP_ID_COOKIE_PARAM = "ignoreIdpId";
	private static final String IDP_ID_COOKIE_NAME = "idp.id.cookie.name";
	private static final String IDP_PRIORITIZER = "idp.prioritizer";
	private static IdentityProviderMap idpMap;
	
	private static final String VELOCITY_TEMPLATE_PATH = "/WEB-INF/velocity";
	private static final String V_START_SSO_PARAM = "startSSOUrl";
	
	private VelocityEngine ve;
	private Class prioritizerClass;
	
    public PingPongIdpDiscoveryServlet() {
        super();
    }

    
	public void init() throws ServletException {
		super.init();

		// Load all possible IdPs from a properties file
        Properties props = new Properties();
        InputStream idpProperties = Thread.currentThread().getContextClassLoader().getResourceAsStream(IDP_PROPS_FILE);
        
        if (idpProperties != null) {
	        try {
				props.load(idpProperties);
			} catch (IOException e) {
				_logger.error("Failed to load IdPs.", e);
			}
        } else {
        	_logger.warn("There are no IdPs defined in " + IDP_PROPS_FILE);
        }

        idpMap = IdentityProviderLoader.load(props);
        
        
		// Initialize a local velocity engine
    	String velocityTemplatePath = this.getServletContext().getRealPath(VELOCITY_TEMPLATE_PATH);
    	ve = VelocityEngineFactory.getNewVelocityEngine(velocityTemplatePath);
    	
    	
    	// Initialize prioritizer
    	try {
			prioritizerClass = Class.forName(this.getServletContext().getInitParameter(IDP_PRIORITIZER));
		} catch (ClassNotFoundException e) {
			_logger.error("Prioritizer class not found: " + this.getServletContext().getInitParameter(IDP_PRIORITIZER), e);
		}
	}


	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		ServletContext context = getServletContext();
		String encodedStartSSOParams = req.getParameter(START_SSO_PARAM);
		String appId = req.getParameter(APP_ID_PARAM);
		String baseStartSSOUrl = context.getInitParameter(START_SSO_URL_PARAM);
		int pageSize = Integer.valueOf(context.getInitParameter(PAGE_SIZE_PARAM));
    	long pageExpiration = Long.valueOf(context.getInitParameter(PAGE_EXP_PARAM));
    	boolean ignoreIdpIdCookie = Boolean.valueOf(req.getParameter(IGNORE_IDP_ID_COOKIE_PARAM));
    	String idpIdCookieName = context.getInitParameter(IDP_ID_COOKIE_NAME);
    	
		
		_logger.debug("HTTP REQUEST: \n" + req);
    	
    	// Clone the possible IdPs to prevent changes made to IdP object fields from affecting others.
    	IdentityProviderMap requestorsIdpMap; 
    	try {
			requestorsIdpMap = idpMap.clone();
		} catch (CloneNotSupportedException e) {
			_logger.error("Error initializing the list of IdPs to Ping", e);
			requestorsIdpMap = new IdentityProviderMap();
		}
		
		
		// Set up startSSO URLs for each of the requestor's IdPs
		requestorsIdpMap.setStartSSOUrl(baseStartSSOUrl, encodedStartSSOParams);
		
		
		// Filter IdPs by App ID if needed
		IdentityProviderAppFilter idpAppFilter = null;
		if (appId != null)
			idpAppFilter = new IdentityProviderAppFilter(appId);
		
		
		// Prioritize IdPs based on the prioritizer specified in web.xml
		Prioritizer idpPrioritizer;
		try {
			if (prioritizerClass == null)
				throw new Exception("Prioritizer class not initialized.");

			idpPrioritizer = (Prioritizer)prioritizerClass.newInstance();
		} catch (Exception e) {
			idpPrioritizer = new NamePrioritizer();
			
			_logger.error(e);
			_logger.warn("Setting prioritizer to: " + NamePrioritizer.class.getCanonicalName());
		}
    	List<IdentityProvider> prioritizedIdps = idpPrioritizer.prioritize(requestorsIdpMap.getValueSet(), idpAppFilter, this.getServletContext(), req, resp);

    	
    	// Initialize pager
    	IdentityProviderPager pager = new IdentityProviderPager(prioritizedIdps, pageSize, pageExpiration);
        req.getSession().setAttribute(IdentityProviderPager.IDP_PAGER, pager);
        
        
        // Get IdP Cookie value if it exists
        String idpIdCookieValue = null;
        if (!ignoreIdpIdCookie) {
        	try {
        		String cookieValue = HttpUtil.getCookieValue(idpIdCookieName, req.getCookies());
        		if (cookieValue != null)
        			idpIdCookieValue = new String(Hex.decodeHex(cookieValue.toCharArray()));
			} catch (DecoderException e) {
				_logger.error("Error decoding IdpId cookie value.", e);
			}
			_logger.debug("Found an IdpId cookie with value: " + idpIdCookieValue);
        }


		// If user previously SSO'ed from an IdP, which is valid given filtered
		// IdPs, and they haven't explicitly requested to rerun PingPong, show a
		// link to the previous IdP
        if ((!ignoreIdpIdCookie) && (idpIdCookieValue != null) && (pager.contiansIdp(idpIdCookieValue))) {

        	// Get startSSO URL
    		String htmlEncodedStartSSOUrl = StringEscapeUtils.escapeHtml(pager.getIdentityProvider(idpIdCookieValue).getStartSSOUrl());
    		
    		// Set velocity up with startSSO URL
    		VelocityContext vContext = new VelocityContext();
    		vContext.put(V_START_SSO_PARAM, htmlEncodedStartSSOUrl);
    		StringWriter ssoWriter = new StringWriter();
    		try {
				ve.mergeTemplate(pager.getFileName(idpIdCookieValue), "utf-8", vContext, ssoWriter);
			} catch (Exception e) {
				_logger.error("There was an error generating velocity template for " + idpIdCookieValue, e);
			}
			req.setAttribute("startSSOHtml", ssoWriter.toString());

			
			// Build PingPongIdpDiscovery URL to ignore IdpId cookie
			String relativeUrl;
			if (req.getQueryString() != null)
				relativeUrl = "PingPongIdpDiscovery?" + req.getQueryString();
			else
				relativeUrl = "PingPongIdpDiscovery";
			relativeUrl = HttpUtil.addParameterToUrl(relativeUrl, IGNORE_IDP_ID_COOKIE_PARAM, "true");
			String htmlEncodedUrl = StringEscapeUtils.escapeHtml(relativeUrl);
			req.setAttribute("pingPongIdpDiscoveryUrl", htmlEncodedUrl);

			
        	RequestDispatcher dispatcher = context.getRequestDispatcher(PREVIOUS_IDP_JSP_PATH);
			dispatcher.forward(req, resp);			
		} else {
			
			// Display idpDiscovery.jsp
			RequestDispatcher dispatcher = context.getRequestDispatcher(IDP_DISCOVERY_JSP_PATH);
			dispatcher.forward(req, resp);
		}
	}

}
