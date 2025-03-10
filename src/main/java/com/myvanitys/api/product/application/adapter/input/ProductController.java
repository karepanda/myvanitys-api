package com.myvanitys.api.product.application.adapter.input;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product")
public class ProductController {

  @GetMapping("/hello")
  public String helloWorldProduct() {
    return "Hello World from Product!";
  }
}