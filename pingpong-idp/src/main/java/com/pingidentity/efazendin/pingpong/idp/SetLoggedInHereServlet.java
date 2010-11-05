package com.pingidentity.efazendin.pingpong.idp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pingidentity.efazendin.pingpong.idp.util.HttpUtil;

public class SetLoggedInHereServlet extends HttpServlet {

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		HttpUtil.writeLoggedInHereCookie(resp);
		
	}

}
