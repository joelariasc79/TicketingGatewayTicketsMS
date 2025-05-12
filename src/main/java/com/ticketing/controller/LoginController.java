package com.ticketing.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.SessionAttributes;

//
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@SessionAttributes("/api/user")
public class LoginController {

   
    @GetMapping(value = "/login")
    public String login(@RequestParam(required = false) String logout, @RequestParam(required = false) String error,
                        HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Model model) {
        String message = "";
        if (error != null) {
            message = "Invalid Credentials";
        }
        if (logout != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(httpServletRequest, httpServletResponse, auth);
            }
            message = "Logout";
            return "login"; // Return the name of the Thymeleaf template
        }
        model.addAttribute("Message", message);
        return "login"; // Return the name of the Thymeleaf template
    }
    

}