package com.thoughtworks.adtd.springframework;

import com.google.common.collect.Multimap;
import com.thoughtworks.adtd.http.Request;
import com.thoughtworks.adtd.http.Response;
import com.thoughtworks.adtd.http.WebProxy;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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
        String method = adtdRequest.getMethod().toUpperCase();
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
        Multimap<String, String> params = adtdRequest.getParams();
        for (String param : params.keySet()) {
            for (String value : params.get(param)) {
                requestBuilder.param(param, value);
            }
        }
    }

    private void setHeaders(MockHttpServletRequestBuilder requestBuilder, Request adtdRequest) {
        Multimap<String, String> headers = adtdRequest.getHeaders();
        for (String header : headers.keySet()) {
            for (String value : headers.get(header)) {
                requestBuilder.header(header, value);
            }
        }
    }

    private void setSession(MockHttpServletRequestBuilder requestBuilder, Request adtdRequest) {
        if (session != null) {
            requestBuilder.session(session);
        }
    }
}
