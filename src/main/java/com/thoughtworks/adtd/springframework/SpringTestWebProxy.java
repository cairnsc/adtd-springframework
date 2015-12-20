package com.thoughtworks.adtd.springframework;

import com.google.common.collect.Multimap;
import com.thoughtworks.adtd.http.*;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Adapter for adtd's WebProxy in Spring MVC Test Framework.
 */
public class SpringTestWebProxy implements WebProxy {
    private final MockMvc mockMvc;
    private MockHttpSession session;
    private boolean print;

    public SpringTestWebProxy(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        session = null;
        print = false;
    }

    public SpringTestWebProxy withSession(MockHttpSession session) {
        this.session = session;
        return this;
    }

    /**
     * Print request execution details to System.out.
     * @return this
     */
    public SpringTestWebProxy doPrint() {
        print = true;
        return this;
    }

    public Response execute(Request adtdRequest) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = buildRequest(adtdRequest);
        ResultActions result = mockMvc.perform(requestBuilder);
        if (print) {
            result.andDo(print());
        }
        return new MvcResultAdtdResponse(result.andReturn());
    }

    private MockHttpServletRequestBuilder buildRequest(Request adtdRequest) {
        MockHttpServletRequestBuilder requestBuilder = getRequestBuilder(adtdRequest);
        setParams(requestBuilder, adtdRequest);
        setHeaders(requestBuilder, adtdRequest);
        setSession(requestBuilder, adtdRequest);
        return requestBuilder;
    }

    private MockHttpServletRequestBuilder getRequestBuilder(Request adtdRequest) {
        Assert.notNull(adtdRequest.getMethod(), "Request method must be set");
        String method = adtdRequest.getMethod().toUpperCase();
        Assert.notNull(adtdRequest.getUri(), "Request URI must be set");
        String uri = adtdRequest.getUri();
        MockHttpServletRequestBuilder builder;

        if (method.equals("GET")) {
            builder = MockMvcRequestBuilders.get(uri);
        } else if (method.equals("POST")) {
            builder = MockMvcRequestBuilders.post(uri);
        } else if (method.equals("PUT")) {
            builder = MockMvcRequestBuilders.put(uri);
        } else if (method.equals("PATCH")) {
            builder = MockMvcRequestBuilders.patch(uri);
        } else if (method.equals("DELETE")) {
            builder = MockMvcRequestBuilders.delete(uri);
        } else if (method.equals("OPTIONS")) {
            builder = MockMvcRequestBuilders.options(uri);
        } else if (method.equals("HEAD")) {
            builder = MockMvcRequestBuilders.get(uri);
        } else {
            // we could alternatively create a RequestPostProcessor to set the method string in the MockHttpServletRequest
            throw new IllegalArgumentException("Method \"" + method + "\" not supported");
        }
        return builder;
    }

    private void setParams(MockHttpServletRequestBuilder requestBuilder, Request adtdRequest) {
        Collection<RequestParameter> params = adtdRequest.getParams().getParams().values();
        for (RequestParameter param  : params) {
            List<String> values = param.getValues();
            requestBuilder.param(param.getName(), values.toArray(new String[values.size()]));
        }
    }

    private void setHeaders(MockHttpServletRequestBuilder requestBuilder, Request adtdRequest) {
        Collection<Header> headers = adtdRequest.getHeaders().values();
        for (Header header : headers) {
            requestBuilder.header(header.getName(), header.getValue());
        }
    }

    private void setSession(MockHttpServletRequestBuilder requestBuilder, Request adtdRequest) {
        if (session != null) {
            requestBuilder.session(session);
        }
    }
}
