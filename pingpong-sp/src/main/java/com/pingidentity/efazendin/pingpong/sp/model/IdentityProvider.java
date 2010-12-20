package com.pingidentity.efazendin.pingpong.sp.model;

import java.util.ArrayList;
import java.util.List;

public class IdentityProvider implements Cloneable {

	private String name = null;
	private String entityId = null;
	private String fileName = null;
	private String pingHandlerUrl = null;
	private List<String> apps;
	private boolean hasPonged = false;
	private boolean hasAuthenticatedUser = false;
	private String startSSOUrl = null;
	
	
	public IdentityProvider() {
		this.setApps(new ArrayList<String>());
	}
	
	public IdentityProvider(String name, String entityId, String fileName, String pingHandlerUrl, String startSSOUrl) {
		this();
		this.name = name;
		this.entityId = entityId;
		this.fileName = fileName;
		this.pingHandlerUrl = pingHandlerUrl;
		this.startSSOUrl = startSSOUrl;
	}
	
	
	public IdentityProvider clone() throws CloneNotSupportedException{
		IdentityProvider clonedCopy = (IdentityProvider)super.clone();
		clonedCopy.setApps(new ArrayList<String>());
		for(String appId : apps)
			clonedCopy.addApp(appId);
		
		return clonedCopy;
	}
	

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getPingHandlerUrl() {
		return pingHandlerUrl;
	}

	public void setPingHandlerUrl(String pingHandlerUrl) {
		this.pingHandlerUrl = pingHandlerUrl;
	}

	public void setApps(List<String> apps) {
		this.apps = apps;
	}

	public List<String> getApps() {
		return apps;
	}
	
	public void addApp(String appId) {
		this.apps.add(appId);
	}
	
	public int getAppsSize() {
		int size = 0;
		if (this.apps != null)
			size = this.apps.size();
		
		return size;
	}

	public boolean containsApp(String appId) {
		if (this.apps != null)
			return apps.contains(appId);
		else
			return false;
	}

	public void setHasAuthenticatedUser(boolean hasAuthenticatedUser) {
		this.hasAuthenticatedUser = hasAuthenticatedUser;
		this.hasPonged = true;
	}
	
	public boolean getHasAuthenticatedUser() {
		return this.hasAuthenticatedUser;
	}

	public void setHasPonged(boolean hasPonged) {
		this.hasPonged = hasPonged;
	}

	public boolean getHasPonged() {
		return hasPonged;
	}

	public void setStartSSOUrl(String startSSOUrl) {
		this.startSSOUrl = startSSOUrl;
	}

	public String getStartSSOUrl() {
		return startSSOUrl;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
