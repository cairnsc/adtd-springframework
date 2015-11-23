package com.thoughtworks.adtd.springframework;

import com.thoughtworks.adtd.http.Response;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Adapter for adtd's Response interface to a MvcResult.
 */
public class MvcResultAdtdResponse implements Response {
    private final MvcResult mvcResult;

    public MvcResultAdtdResponse(MvcResult mvcResult) {
        this.mvcResult = mvcResult;
    }

    public int getStatus() {
        return mvcResult.getResponse().getStatus();
    }

    public String getHeader(String name) {
        return mvcResult.getResponse().getHeader(name);
    }

    public String getBody() throws Exception {
        return mvcResult.getResponse().getContentAsString();
    }
}
