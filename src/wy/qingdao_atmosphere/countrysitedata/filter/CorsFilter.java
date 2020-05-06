package wy.qingdao_atmosphere.countrysitedata.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

public class CorsFilter implements Filter{

	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
			FilterChain filterChain) throws IOException, ServletException {
		 HttpServletResponse response = (HttpServletResponse) servletResponse;
	        HttpServletRequest request = (HttpServletRequest) servletRequest;
	// 指定允许其他域名访问
	        response.setHeader("Access-Control-Allow-Origin", "*");
	// 响应类型
	        response.setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, OPTIONS, DELETE");
	// 响应头设置
	        response.setHeader("Access-Control-Allow-Headers", "*");
//	        response.setHeader("Access-Control-Allow-Headers", "*");
	        if ("OPTIONS".equals(request.getMethod())){
	            response.setStatus(HttpStatus.SC_NO_CONTENT);
	        }
	        filterChain.doFilter(servletRequest, servletResponse);
		
	}

	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

}
