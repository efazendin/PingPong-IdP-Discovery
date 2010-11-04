<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
	<title>PingPong IdP Discovery</title>
	
	
	<script type="text/javascript" src="jquery-1.4.2.min.js"></script>
	
	<link href="style.css" rel="stylesheet" type="text/css" />

</head>
<body>

	<div class="mainContainer">
		<!-- You could add some branding here -->
		<% /* 
		<div class="logo">
			<h1></h1>
		</div>
		*/ %>
		
		<div class="animatedContainer">
			
			<h3>Please wait while we find your provider.</h3>
			
			<div id="div1" class="animated"></div><div id="div2" class="animated"></div><div id="div3" class="animated"></div><div id="div4" class="animated"></div><div id="div5" class="animated"></div>
		</div>
		
		<div class="idps">
		
		</div>
	</div>
	
	<!-- This is where PingPong happens. -->
	<div class="idpList">
		
	</div>
	
	
	<script type="text/javascript">
	
		var pongStatusFrequency = 500;
		var stopTheAnimation = false;
		
		function animateTheBox() {
			$("#div1").fadeIn("slow", function () { if (!stopTheAnimation) 
				$("#div2").fadeIn("slow", function () { if (!stopTheAnimation) 
					$("#div3").fadeIn("slow", function () { if (!stopTheAnimation) 
						$("#div4").fadeIn("slow", function () { if (!stopTheAnimation) 
							$("#div5").fadeIn("slow", function () {
								$("#div1").fadeOut("fast");
								$("#div2").fadeOut("fast");
								$("#div3").fadeOut("fast");
								$("#div4").fadeOut("fast");
								$("#div5").fadeOut("fast", function () {
									if (!stopTheAnimation) animateTheBox();
								});
							});
						});
					});
				});
			});
		}
		
		function getPage() {
		
			$.ajax({
			  url: "PingPongPage",
			  data: "",
			  success: function(data) {parsePage(data);}
			});
			
		}
		
		function parsePage(data) {
			$('.idpList').html(data);
		}
		
		function getIdps() {
			$.ajax({
			  url: "PingPongGetIdps",
			  success: function(data) {parseIdps(data);}
			});
		}
		
		function parseIdps(data) {
			$('.idps').html(data);
		}
		
		function getStatus() {
		
			$.ajax({
				url: "PongStatus",
				success: function(data) {parseStatus(data);}
			});
		}
		
		function parseStatus(data) {
		
			if (data == "next page") {
				
				getPage();
		
				setTimeout("getStatus()", pongStatusFrequency);
				
			} else if (data == "check back") {
				setTimeout("getStatus()", pongStatusFrequency);
			} else {
		
				stopTheAnimation = true;
		
				$(".animatedContainer").fadeOut("fast", function () {
					getIdps();
				});
			}
		}
		
		
		
		animateTheBox();
		getPage();
		setTimeout("getStatus()", pongStatusFrequency);
	
	
	</script>


</body>
</html>