package com.pingidentity.efazendin.pingpong.sp.model;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

public class IdentityProviderLoader {

	private static final Logger _logger = Logger.getLogger(IdentityProviderLoader.class);

	public static IdentityProviderMap load(Properties identityProviders) {

		IdentityProviderMap theMap = new IdentityProviderMap();

		Set<Object> keys = identityProviders.keySet();

		Set<String> entityIds = new HashSet<String>();
		String entityId;
		String[] keyArray;
		for(Object key : keys) {
			keyArray = ((String)key).split("\\.");
			if (keyArray.length == 3) {
				entityId = keyArray[1];
				entityIds.add(entityId);
			} else
				_logger.warn("IdP Property Key of '" + key + "' is invalid.");
		}

		_logger.debug("IdPs: " + entityIds.toString());

		IdentityProvider idp;
		String name;
		String filename;
		String url;
		String apps;
		for(String id : entityIds) {
			name = identityProviders.getProperty("idp." + id + ".name");
			filename = identityProviders.getProperty("idp." + id + ".filename");
			url = identityProviders.getProperty("idp." + id + ".url");
			apps = identityProviders.getProperty("idp." + id + ".apps");

			if (name != null && filename != null && url != null) {

				idp = new IdentityProvider();

				idp.setName(name);
				idp.setEntityId(id);
				idp.setFileName(filename);
				idp.setPingHandlerUrl(url);

				if (apps != null) {
					String[] appArray = apps.split(",");
					for (String appId : appArray) {

						idp.addApp(appId.trim());
					}
				}

				theMap.put(idp);
			} else
				_logger.warn("Ignoring IdP " + id + " because it is missing required properties (name, filename, url).");
		}


		return theMap;

	}

}
