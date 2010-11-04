package com.pingidentity.efazendin.pingpong.sp.util;

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
	
	// This method is duplicated on the IdP side
	public static String addParameterToUrl(String url, String paramName, String paramValue) {
		
		/*
		try {
			paramValue = URLEncoder.encode(paramValue, "utf-8");
		} catch (UnsupportedEncodingException e) {
			_logger.error("There was an error url encoding the " + paramName + " query parameter.", e);
		}
		*/
		
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
