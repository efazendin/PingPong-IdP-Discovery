package com.pingidentity.efazendin.pingpong.idp.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class HttpUtil {
	
	private static final Logger _logger = Logger.getLogger(HttpUtil.class);

	private static final String COOKIE_NAME = "loggedInHere";
	
	public static boolean hasLoggedInHere(HttpServletRequest req) {
		
		boolean result = false;
		
		Cookie[] cookies = req.getCookies();
		
		if (cookies != null) {
			for(Cookie cookie : cookies) {
				
				if ((COOKIE_NAME.equals(cookie.getName())) && (Boolean.TRUE.toString().equals(cookie.getValue()))) {
					result = true;
				}
			}
		}

		return result;
	}
	
	public static void writeLoggedInHereCookie(HttpServletResponse resp) {
		Cookie cookie = new Cookie(COOKIE_NAME, Boolean.TRUE.toString());
		//Persist the cookie for 5 years
		cookie.setMaxAge(60 * 60 * 24 * 365 * 5);
		resp.addCookie(cookie);
	}

	public static String addParameterToUrl(String url, String paramName, String paramValue) {
		
		if (url.contains("?")) {
			
			if ((url.lastIndexOf("?") == url.length()) || (url.lastIndexOf("&") == url.length())) {
				url = url + paramName + "=" + paramValue;
			} else {
				url = url + "&" + paramName + "=" + paramValue;
			}
			
			
		} else {
			url = url + "?" + paramName + "=" + paramValue;
		}
		
		return url;
	}
}
