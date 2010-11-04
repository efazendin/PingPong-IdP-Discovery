package com.pingidentity.efazendin.pingpong.sp;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
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
import com.pingidentity.efazendin.pingpong.sp.prioritizers.FilenamePrioritizer;
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
	private static IdentityProviderMap idpMap;
	
	private static final String VELOCITY_TEMPLATE_PATH = "/WEB-INF/velocity";
	private static final String V_START_SSO_PARAM = "startSSOUrl";
	
	private VelocityEngine ve;
	
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
	        //_logger.debug("Properties from " + IDP_PROPS_FILE + ": " + props);
        } else {
        	_logger.warn("There are no IdPs defined in " + IDP_PROPS_FILE);
        }

        idpMap = IdentityProviderLoader.load(props);
        //_logger.debug("idpMap.size(): " + idpMap.size());
        
        
		// Initialize a local velocity engine
    	String velocityTemplatePath = this.getServletContext().getRealPath(VELOCITY_TEMPLATE_PATH);
    	ve = VelocityEngineFactory.getNewVelocityEngine(velocityTemplatePath);
	}


	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	
		_logger.debug("HTTP REQUEST: \n" + req);
		
		String encodedStartSSOParams = req.getParameter(START_SSO_PARAM);

    	
    	String appId = req.getParameter(APP_ID_PARAM);
    	
    	// Clone the possible IdPs to prevent changes made to IdP object fields from affecting others.
    	IdentityProviderMap requestorsIdpMap; 
    	try {
			requestorsIdpMap = idpMap.clone();
		} catch (CloneNotSupportedException e) {
			_logger.error("Error initializing the list of IdPs to Ping", e);
			requestorsIdpMap = new IdentityProviderMap();
		}
		
		// Set up startSSO URLs for each of the requestor's IdPs
		String baseStartSSOUrl = this.getServletContext().getInitParameter(START_SSO_URL_PARAM);
		requestorsIdpMap.setStartSSOUrl(baseStartSSOUrl, encodedStartSSOParams);
		
		//  TODO It seems like this really should be passing in a prioritizer to an IdpPager or IdpMap...
		
		// Figure out what's going to be the prioritized list of IdPs for this
		// given request. This currently doesn't do it, but the instantiated
		// type of prioritizer should be configurable. This one just sorts
		// alphabetically but others may want to take runtime data into account
		// when prioritizing IdPs. For example, the user's IP address may
		// be associated with an IdP such as Comcast or Qwest. 
    	Prioritizer idpPrioritizer = new FilenamePrioritizer();
    	List<IdentityProvider> prioritizedIdps;
    	if (appId == null)
    		prioritizedIdps = idpPrioritizer.prioritize(requestorsIdpMap.getValueSet(), this.getServletContext(), req, resp);
    	else
    		prioritizedIdps = idpPrioritizer.prioritize(requestorsIdpMap.getValueSet(), new IdentityProviderAppFilter(appId), this.getServletContext(), req, resp);

    	int pageSize = Integer.valueOf(this.getServletContext().getInitParameter(PAGE_SIZE_PARAM));
    	long pageExpiration = Long.valueOf(this.getServletContext().getInitParameter(PAGE_EXP_PARAM));
    	IdentityProviderPager pager = new IdentityProviderPager();
    	pager.setPrioritizedIdentityProviders(prioritizedIdps);
    	pager.setPageSize(pageSize);
    	pager.setPageExpirationPeriod(pageExpiration);
    	
        req.getSession().setAttribute(IdentityProviderPager.IDP_PAGER, pager);
        
        boolean ignoreIdpIdCookie = Boolean.valueOf(req.getParameter(IGNORE_IDP_ID_COOKIE_PARAM));
        String idpIdCookieValue = null;
        
        if (!ignoreIdpIdCookie) {
			// Check to see if IdpId cookie exists and matches a possible IdP
			String idpIdCookieName = this.getServletContext().getInitParameter(IDP_ID_COOKIE_NAME);
			
			Cookie[] allCookies = req.getCookies();
			if (allCookies != null) {
				for (Cookie cookie : allCookies) {
					if (cookie.getName().equals(idpIdCookieName)) {
						try {
							idpIdCookieValue = new String(Hex.decodeHex(cookie.getValue().toCharArray()));
						} catch (DecoderException e) {
							_logger.error("Error decoding IdpId cookie value.", e);
						}
						_logger.debug("Found an IdpId cookie with value: " + idpIdCookieValue);
					}

				}
			}
        }
        
        ServletContext context = getServletContext();
        if ((!ignoreIdpIdCookie) && (idpIdCookieValue != null) && (pager.contiansIdp(idpIdCookieValue))) {
			// Display previousIdp.jsp
        	// Build startSSO HTML and the link to ignore the IdpId cookie

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
			if (req.getQueryString() != null) {
				// TODO Small bug here, if ignoreIdpId=false is already in query string it persists even though an ignoreIdpId=true is going to be added a couple of lines down from here.  ignoreIdpId is then treated as false, resulting in showing the previousIdp.jsp page.
				relativeUrl = "PingPongIdpDiscovery?" + req.getQueryString();
			}
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
