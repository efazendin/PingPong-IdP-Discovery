package com.pingidentity.efazendin.pingpong.sp.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.pingidentity.efazendin.pingpong.sp.util.HttpUtil;

public class IdentityProviderMap implements Cloneable {

	private static final Logger _logger = Logger.getLogger(IdentityProviderMap.class);
	
	private static final String PARTNER_IDP_ID = "PartnerIdpId";
	
	private Map<String, IdentityProvider> identityProviders;
	
	public IdentityProviderMap() {
		identityProviders = new HashMap<String, IdentityProvider>();
	}
	

	public IdentityProviderMap clone() throws CloneNotSupportedException {
		IdentityProviderMap clonedMap = new IdentityProviderMap();
		
		for (String entityId : identityProviders.keySet()) {
			clonedMap.put(identityProviders.get(entityId).clone());
		}
		
		return clonedMap;
	}

	
	public void put(IdentityProvider idp) {
		identityProviders.put(idp.getEntityId(), idp);
	}
	
	public IdentityProvider get(String entityId) {
		return identityProviders.get(entityId);
	}
	
	public int size() {
		return identityProviders.size();
	}
	
	public Set<IdentityProvider> getValueSet() {
		Set<IdentityProvider> theSet = new HashSet<IdentityProvider>();
		for (IdentityProvider idp : identityProviders.values())
			theSet.add(idp);
		return theSet;
	}


	public void setStartSSOUrl(String baseStartSSOUrl, String encodedStartSSOParams) {
		
		baseStartSSOUrl = HttpUtil.getStartSSOUrlWithoutPartnerIdpId(baseStartSSOUrl, encodedStartSSOParams);
		String startSSOUrl;
		
		for (String id : identityProviders.keySet()) {
			startSSOUrl = HttpUtil.addParameterToUrl(baseStartSSOUrl, PARTNER_IDP_ID, id);
			identityProviders.get(id).setStartSSOUrl(startSSOUrl);
		}
		
	}
	
	
}
