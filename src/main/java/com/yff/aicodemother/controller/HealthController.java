package com.yff.aicodemother.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yff
 * @date 2026-02-01 16:27:10
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/")
    public String checkHealth() {
        return "OK";
    }

}
