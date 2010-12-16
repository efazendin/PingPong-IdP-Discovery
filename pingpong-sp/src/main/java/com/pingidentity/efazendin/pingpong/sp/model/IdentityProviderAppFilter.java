package com.pingidentity.efazendin.pingpong.sp.model;

import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

public class IdentityProviderAppFilter {

	private static final Logger _logger = Logger.getLogger(IdentityProviderAppFilter.class);

	private String appId;

	public IdentityProviderAppFilter(String appId) {
		this.appId = appId;
	}

	public Set<IdentityProvider> filter(Set<IdentityProvider> idps) {

		_logger.debug("appId: " + appId);

		IdentityProvider idp;
		Iterator<IdentityProvider> iterator = idps.iterator();
		while (iterator.hasNext()) {
			idp = iterator.next();

			if (idp.getAppsSize() > 0) {
				boolean idpContainsApp = idp.containsApp(appId);
				_logger.debug(idp.getEntityId() + " contains app: " + idpContainsApp);
				if (!idpContainsApp)
					iterator.remove();
			}
		}

		return idps;
	}
}
