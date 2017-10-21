package com.cebbank.bdap.v1.controller;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by yilong on 2017/10/19.
 */
@RestController
@RequestMapping("/testtable")
public class HBaseRestCtrl {
    private Logger LOG = LoggerFactory.getLogger(HBaseRestCtrl.class);

    @RequestMapping("/regions")
    public String regions(HttpServletRequest request, HttpServletResponse response) {
        return "success";
    }
}
