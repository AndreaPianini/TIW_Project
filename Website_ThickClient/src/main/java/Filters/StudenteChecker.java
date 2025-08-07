package Filters;

import java.io.IOException;

import BEANS.Utente;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class StudenteChecker implements Filter {

	public void init(FilterConfig fConfig) throws ServletException {
	}
	
	public void destroy() {
	}

	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		System.out.print("Studente filter executing \n");
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String loginpath = req.getServletContext().getContextPath() + "/Login.html";
		HttpSession s = req.getSession();
		Utente u = null;
		// check if the user is a student
		u = (Utente) s.getAttribute("user");
		if (!u.getRole().equals("STUDENTE")) {
			System.out.print("Studente checker FAILED...\n");
			res.setStatus(403);
			res.setHeader("Location", loginpath);
			res.sendRedirect(loginpath);
			return;
		}
		// pass the request along the filter chain
		chain.doFilter(request, response);
	}

}
