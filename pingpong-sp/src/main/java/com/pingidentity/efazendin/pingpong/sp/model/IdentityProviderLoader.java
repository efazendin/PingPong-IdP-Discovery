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
        for(Object key : keys) {
        	entityId = ((String)key).split("\\.")[1];
        	entityIds.add(entityId);
        }
        
        _logger.debug("IdPs: " + entityIds.toString());
        
        IdentityProvider idp;
        for(String id : entityIds) {
        	idp = new IdentityProvider();

        	idp.setName(identityProviders.getProperty("idp." + id + ".name"));
        	idp.setEntityId(id);
        	idp.setFileName(identityProviders.getProperty("idp." + id + ".filename"));
        	idp.setPingHandlerUrl(identityProviders.getProperty("idp." + id + ".url"));
        	
        	String apps = identityProviders.getProperty("idp." + id + ".apps");
        	
        	if (apps != null) {
	        	String[] appArray = apps.split(",");
	        	for (String appId : appArray) {

	        		idp.addApp(appId.trim());
	        	}
        	}
        	
        	theMap.put(idp);
        }
        
		
		return theMap;
		
	}
	
}
