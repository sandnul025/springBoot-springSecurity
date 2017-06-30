package com.sandnul.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * MOTTO: Rainbow comes after a storm.
 * AUTHOR: sandNul
 * DATE: 2017/6/28
 * TIME: 8:50
 */
@Controller
public class TestController {

    @GetMapping(value = {"/","index"})
    public String index(){

        return "index";
    }

    @GetMapping(value = "hello")
    public String hello(){

        return "hello";
    }

    @GetMapping(value = "login")
    public String login(){

        return "login";
    }


    @GetMapping(value = "admin")
    public String admin(Model model){

        model.addAttribute("title","标题");
        model.addAttribute("content","内容");
        model.addAttribute("extraInfo","你是admin");
        return "admin";
    }
}
