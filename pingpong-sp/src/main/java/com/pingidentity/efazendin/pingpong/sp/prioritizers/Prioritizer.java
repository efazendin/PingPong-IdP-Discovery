package com.pingidentity.efazendin.pingpong.sp.prioritizers;

import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pingidentity.efazendin.pingpong.sp.model.IdentityProvider;
import com.pingidentity.efazendin.pingpong.sp.model.IdentityProviderAppFilter;

/**
 * A Prioritizer sets the priority of IdPs to query.
 * 
 * @author efazendin
 *
 */
public interface Prioritizer {
	
	public void init(ServletContext cont);
	
	public List<IdentityProvider> prioritize(Set<IdentityProvider> identityProviderSet, HttpServletRequest req, HttpServletResponse resp);
	

}
