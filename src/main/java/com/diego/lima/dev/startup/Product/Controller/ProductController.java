package com.diego.lima.dev.startup.Product.Controller;

import com.diego.lima.dev.startup.Product.Dtos.Request.CreateProductDTO;
import com.diego.lima.dev.startup.Product.Dtos.Response.ProductResponse;
import com.diego.lima.dev.startup.Product.Service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductDTO request) {
        var response = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
