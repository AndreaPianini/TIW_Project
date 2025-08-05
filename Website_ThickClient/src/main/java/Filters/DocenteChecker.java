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


public class DocenteChecker implements Filter {

	public void init(FilterConfig fConfig) throws ServletException {
	}
	
	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		//System.out.print("Docente filter executing ..\n");
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String loginpath = req.getServletContext().getContextPath() + "/Login.html";
		// check if the client is an admin
		HttpSession s = req.getSession();
		Utente u = null;
		u = (Utente) s.getAttribute("user");
		if (!u.getRole().equals("Docente")) {
			res.setStatus(403);
			res.setHeader("Location", loginpath);
			System.out.print("Docente checker FAILED...\n");
			return;
		}
		// pass the request along the filter chain
		chain.doFilter(request, response);
		
	}

}
