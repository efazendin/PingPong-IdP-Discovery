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
 * A Prioritizer implementation that orders the IdPs by the filenames of the corresponding velocity template.
 * 
 * @author efazendin
 *
 */
public class FilenamePrioritizer implements Prioritizer {

	private static final Logger _logger = Logger.getLogger(FilenamePrioritizer.class);

	public void init(ServletContext cont) {
		//ignore
	}
	
	public List<IdentityProvider> prioritize(Set<IdentityProvider> identityProviderSet, HttpServletRequest req, HttpServletResponse resp) {
		
		List<IdentityProvider> sortedList = new ArrayList<IdentityProvider>(identityProviderSet);
		Collections.sort(sortedList, new FilenameComparator());
		return sortedList;
	}
	
	private class FilenameComparator implements Comparator<IdentityProvider> {

		public int compare(IdentityProvider idp0, IdentityProvider idp1) {
			return idp0.getFileName().compareTo(idp1.getFileName());
		}
		
	}
}
