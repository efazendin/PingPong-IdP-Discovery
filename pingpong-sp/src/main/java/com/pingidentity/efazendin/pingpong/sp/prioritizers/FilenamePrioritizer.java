package com.pingidentity.efazendin.pingpong.sp.prioritizers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.pingidentity.efazendin.pingpong.sp.model.IdentityProvider;
import com.pingidentity.efazendin.pingpong.sp.model.IdentityProviderAppFilter;

/**
 * A Prioritizer implementation that orders the IdPs by the names on the files of IdP templates.
 * 
 * @author efazendin
 *
 */
public class FilenamePrioritizer implements Prioritizer {

	private static final Logger _logger = Logger.getLogger(FilenamePrioritizer.class);

	public List<IdentityProvider> prioritize(Set<IdentityProvider> identityProviderSet, IdentityProviderAppFilter filter, ServletContext cont, HttpServletRequest req, HttpServletResponse resp) {
		
		if (filter != null)
			filter.filter(identityProviderSet);
		
		List<FilenameComparableIdentityProvider> prioritizedList = new ArrayList<FilenameComparableIdentityProvider>();
		
		for (IdentityProvider idp : identityProviderSet)
			prioritizedList.add(new FilenameComparableIdentityProvider(idp));
		
		Collections.sort(prioritizedList);
				
		List<IdentityProvider> castedList = new ArrayList<IdentityProvider>();
		for (IdentityProvider idp : prioritizedList)
			castedList.add(idp);

		return castedList;
	}
	
	
	private class FilenameComparableIdentityProvider extends IdentityProvider implements Comparable {

		public FilenameComparableIdentityProvider(IdentityProvider idp) {
			super(idp.getName(), idp.getEntityId(), idp.getFileName(), idp.getPingHandlerUrl(), idp.getStartSSOUrl());
			this.setApps(idp.getApps());
		}
		
		public int compareTo(Object o) {
			
			return this.getFileName().compareTo(((IdentityProvider)o).getFileName());
		}
		
	}

}
