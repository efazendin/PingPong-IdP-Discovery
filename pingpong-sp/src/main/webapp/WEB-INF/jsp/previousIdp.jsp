<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Previous IdP</title>

<link href="style.css" rel="stylesheet" type="text/css" />
</head>
<body>


<div class="mainContainer">

	<p>
		SSO again from your previous Identity Provider:
	</p>

	<%=request.getAttribute("startSSOHtml") %>

	
	<p>
		Or, <a href="<%=request.getAttribute("pingPongIdpDiscoveryUrl") %>">click here</a> to search for other Identity Providers.
	</p>
</div>

</body>
</html>