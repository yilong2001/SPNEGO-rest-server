package com.spengo.bdap.auth.filter;

/**
 * Created by yilong on 2017/10/19.
 */
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.spengo.bdap.auth.kbr.KbrAuthHandler;
import com.spengo.bdap.exception.RestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.security.authentication.client.KerberosAuthenticator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class HttpAuthFilter implements Filter {
    private Logger LOG = LoggerFactory.getLogger(HttpAuthFilter.class);

    KbrAuthHandler kbrAuthHandler;

    public HttpAuthFilter(KbrAuthHandler kbrAuthHandler) {
        this.kbrAuthHandler = kbrAuthHandler;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String url = ((HttpServletRequest) servletRequest).getRequestURI();
        //System.out.println(url);

        //the url is cloudera manager rest api: for example : /api/v13/cluster
        if (url.startsWith("/api/")) {
            if (doBasicAuth(servletRequest, servletResponse)) {
                filterChain.doFilter(servletRequest, servletResponse);
            }

            return;
        }

        //the url is hbase rest api : for example : /testtable/regions
        if (doKbrAuth(servletRequest, servletResponse)) {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {

    }

    private String checkHTTPAuthorize(ServletRequest request) throws RestException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String auth = httpRequest.getHeader("Authorization");
        return kbrAuthHandler.authenticate(request.getServerName(), auth);
    }

    private boolean doKbrAuth(ServletRequest request, ServletResponse servletResponse) throws IOException {

        try {
            String token = checkHTTPAuthorize(request);
            return true;
        } catch (RestException e) {
            HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

            if (e.getErrCode() == RestException.ERR_CODE_BAD_HEADER) {
                httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                httpResponse.setHeader(KerberosAuthenticator.WWW_AUTHENTICATE, KerberosAuthenticator.NEGOTIATE);
            } else {
                httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
            }

            ObjectMapper mapper = new ObjectMapper();
            httpResponse.getWriter().write(mapper.writeValueAsString(e));
        }

        return false;
    }

    private boolean doBasicAuth(ServletRequest request, ServletResponse servletResponse) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        Base64 base64 = new Base64();
        String authheader = httpRequest.getHeader("Authorization");
        if (authheader == null) {
            httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpResponse.setHeader(KerberosAuthenticator.WWW_AUTHENTICATE, "Basic");
            return false;
        }

        String auth = authheader.substring("Basic".length()).trim();
        LOG.error("basic auth header : " + auth);
        String userpws[] = new String(base64.decode(auth)).split(":");

        if (userpws.length < 2 || !userpws[0].equals("admin") || !userpws[1].equals("admin") ) {
            httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }

        return true;
    }
}
