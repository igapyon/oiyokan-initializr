package com.example;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.oiyokan.OiyokanOdata4RegisterImpl;
import jp.oiyokan.common.OiyoInfo;

@RestController
public class DemoOData4Register {
    private static final String ODATA_ROOTPATH = "/odata4.svc";

    @RequestMapping(ODATA_ROOTPATH + "/*")
    public void serv(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException {
        final OiyoInfo oiyoInfo = new OiyoInfo();
        OiyokanOdata4RegisterImpl.serv(oiyoInfo, req, resp, ODATA_ROOTPATH);
    }
}