package com.pingidentity.efazendin.pingpong.sp.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

public class HttpUtil {
	private static final Logger _logger = Logger.getLogger(HttpUtil.class);
		
	public static String getStartSSOUrlWithoutPartnerIdpId(String startSSOUrl, String startSSOQueryParams) {
		
		if ((startSSOQueryParams != null) && (!startSSOQueryParams.trim().equals(""))) {
			String[] queryArray = startSSOQueryParams.split("&");
			String[] aParamArray;
			for (String param : queryArray) {
				_logger.debug("param: " + param);
				aParamArray = param.split("=");
				if (aParamArray.length == 2) {
					_logger.debug(aParamArray[0] + ": " + aParamArray[1]);
					startSSOUrl = addParameterToUrl(startSSOUrl, aParamArray[0], aParamArray[1]);
				}
			}
		}
		
		return startSSOUrl;
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
	
	public static String getCookieValue(String name, Cookie[] cookies) {
		String result = null;
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					result = cookie.getValue();
				}

			}
		}
		return result;
	}
}
