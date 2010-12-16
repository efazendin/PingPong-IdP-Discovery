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
		/*
		List<NameComparableIdentityProvider> prioritizedList = new ArrayList<NameComparableIdentityProvider>();
		
		for (IdentityProvider idp : identityProviderSet)
			prioritizedList.add(new NameComparableIdentityProvider(idp));
		
		Collections.sort(prioritizedList);
				
		List<IdentityProvider> castedList = new ArrayList<IdentityProvider>();
		for (IdentityProvider idp : prioritizedList)
			castedList.add(idp);

		return castedList;
		*/
		List<IdentityProvider> sortedList = new ArrayList<IdentityProvider>(identityProviderSet);
		Collections.sort(sortedList, new NameComparator());
		return sortedList;
	}
	
	private class NameComparator implements Comparator {

		public int compare(Object arg0, Object arg1) {
			return ((IdentityProvider)arg0).getName().compareTo(((IdentityProvider)arg1).getName());
		}
		
	}
	
	/*
	private class NameComparableIdentityProvider extends IdentityProvider implements Comparable {

		public NameComparableIdentityProvider(IdentityProvider idp) {
			super(idp.getName(), idp.getEntityId(), idp.getFileName(), idp.getPingHandlerUrl(), idp.getStartSSOUrl());
			this.setApps(idp.getApps());
		}
		
		public int compareTo(Object o) {
			
			return this.getName().compareTo(((IdentityProvider)o).getName());
		}
		
	}
	*/
}
