package com.myvanitys.api.auth.application.adapter.input;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

  @GetMapping("/hello")
  public String helloWorldAuth() {
    return "Hello World from Auth!";
  }
}
