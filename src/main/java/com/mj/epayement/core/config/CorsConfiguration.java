package com.mj.epayement.core.config;


import com.estifeda.ec2.epayment.core.properties.ApplicationProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsConfiguration implements Filter {
    private final ApplicationProperties properties;
    public CorsConfiguration(ApplicationProperties properties) {
        super();
        this.properties = properties;
    }

    @Override
    public final void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
        final HttpServletResponse response = (HttpServletResponse) res;
        if(this.properties.getCustomCors().getAllowedOrigin() != null){
            response.setHeader("Access-Control-Allow-Origin", this.properties.getCustomCors().getAllowedOrigin());
        }
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "X-Requested-With, Authorization, Origin, Content-Type, Version");
        response.setHeader("Access-Control-Expose-Headers", "X-Requested-With, Authorization, Origin, Content-Type");
        HttpServletRequest request = (HttpServletRequest) req;
        if (!request.getMethod().equals("OPTIONS")) {
            chain.doFilter(req, res);
        }
    }

    @Override
    public void destroy() {
        //not used for moment
    }

    @Override
    public void init(FilterConfig filterConfig) {
        //not used for moment
    }
}
