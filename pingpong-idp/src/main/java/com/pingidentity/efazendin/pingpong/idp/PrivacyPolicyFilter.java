package com.pingidentity.efazendin.pingpong.idp;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class PrivacyPolicyFilter implements Filter {

    public PrivacyPolicyFilter() {

    }

	public void destroy() {

	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		((HttpServletResponse)response).setHeader("P3P", "policyref=\"/idp/privacy_policy/p3p.xml\", CP=\"NOI CURa ADMa DEVa OUR IND COM NAV STA\"");

		chain.doFilter(request, response);
	}

	public void init(FilterConfig fConfig) throws ServletException {

	}

}
