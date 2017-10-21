package com.cebbank.bdap.v1.controller;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by yilong on 2017/10/19.
 */
@RestController
@RequestMapping("/api/v13")
public class CMCtrl {
    private Logger LOG = LoggerFactory.getLogger(CMCtrl.class);

    @RequestMapping("/clusters/{clusterName}/services/{serviceName}/roles")
    public String roles(HttpServletRequest request, HttpServletResponse response,
                        @PathVariable String clusterName,
                        @PathVariable String serviceName) {
        return "success:"+clusterName+":"+serviceName;
    }
}
