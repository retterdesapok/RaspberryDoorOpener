package de.retterdesapok.jettydooropener;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OpenDoorServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	public static String PARAM_USERNAME = "username";
	public static String PARAM_PASSWORD = "password";
	
	@Override 
	  public void doGet(HttpServletRequest request, HttpServletResponse response) 
	      throws IOException
	  {

		String username = request.getParameter(PARAM_USERNAME);
		String password = request.getParameter(PARAM_PASSWORD);
		
		if(username == null || password == null) {
			response.getOutputStream().println("Invalid arguments sent. Expected username and password");
			response.setStatus(400);
			return;
		}

		response.getOutputStream().println("Authenticating...");
		String passwordmd5 = null;
		
		if(password != null && password.length() > 0) {
			try {
				passwordmd5 = Utilities.getMD5(password);
			} catch (NoSuchAlgorithmException e) {
				response.getOutputStream().println(e.getLocalizedMessage());
				response.getOutputStream().println(e.getStackTrace().toString());
				response.setStatus(500);
				return;
			}
		}
		
		if(passwordmd5 != null) {
			try {
				User user = DatabaseHelper.loginUserWithNameAndPasswordHash(username, passwordmd5);
				if(user == null || user.getFailedLoginCount() > 3) {
					response.getOutputStream().println("Wrong username or password.");
					response.setStatus(403);
					return;
				} else if (user.getRemainingLogins() <= 0) {
					response.getOutputStream().println("No logins left...");
					response.setStatus(403);
					return;
				} else if(user.getFailedLoginCount() <= 3 && user.getRemainingLogins() > 0) {
					response.getOutputStream().println("Activating relay...");
					response.setStatus(200);
				}
			} catch (SQLException e) {
				response.getOutputStream().println(e.getLocalizedMessage());
				response.getOutputStream().println(e.getStackTrace().toString());
				response.setStatus(500);
				return;
			}
		}
	  }
	
	
}
