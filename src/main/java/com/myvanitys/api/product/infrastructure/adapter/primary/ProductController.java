package com.myvanitys.api.product.infrastructure.adapter.primary;

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