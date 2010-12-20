package com.pingidentity.efazendin.pingpong.sp.prioritizers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pingidentity.efazendin.pingpong.sp.model.IdentityProvider;

/**
 * A Prioritizer implementation that orders the IdPs by the names on the files of IdP templates.
 * 
 * @author efazendin
 *
 */
public class NamePrioritizer implements Prioritizer {

	private static final Logger _logger = Logger.getLogger(NamePrioritizer.class);

	public void init(ServletContext cont) {
		//ignore
	}
	
	public List<IdentityProvider> prioritize(Set<IdentityProvider> identityProviderSet, HttpServletRequest req, HttpServletResponse resp) {
		List<IdentityProvider> sortedList = new ArrayList<IdentityProvider>(identityProviderSet);
		Collections.sort(sortedList, new NameComparator());
		return sortedList;
	}
	
	private class NameComparator implements Comparator<IdentityProvider> {

		public int compare(IdentityProvider idp0, IdentityProvider idp1) {
			return idp0.getName().compareTo(idp1.getName());
		}
		
	}
}
