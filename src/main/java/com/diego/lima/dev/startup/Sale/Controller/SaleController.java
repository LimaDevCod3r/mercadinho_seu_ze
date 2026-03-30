package com.diego.lima.dev.startup.Sale.Controller;

import com.diego.lima.dev.startup.Sale.Dto.Request.SaleRequestDTO;
import com.diego.lima.dev.startup.Sale.Dto.Response.SaleResponseDTO;
import com.diego.lima.dev.startup.Sale.Service.SalesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sales")
public class SaleController {

    private final SalesService salesService;

    public SaleController(SalesService salesService) {
        this.salesService = salesService;
    }

    @PostMapping
    public ResponseEntity<SaleResponseDTO> createSale(@Valid @RequestBody SaleRequestDTO request){
        var response = salesService.createSale(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
