package com.diego.lima.dev.startup.Stock.Controller;

import com.diego.lima.dev.startup.Stock.Dto.Request.CreateStockDTO;
import com.diego.lima.dev.startup.Stock.Dto.Response.StockResponse;
import com.diego.lima.dev.startup.Stock.Service.StockService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stocks")
public class StockController {

    private final StockService stockService;


    public StockController(StockService stockService) {
        this.stockService = stockService;
    }


    @PostMapping
    public ResponseEntity<StockResponse> createStock(@Valid @RequestBody CreateStockDTO request) {
        var response = stockService.createStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockResponse> findStockById(@PathVariable Long id) {
        var response = stockService.findStockById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<StockResponse>> findAllStocks(Pageable pageable) {
        var response = stockService.findAllStocks(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<StockResponse>findStockByProductId(@PathVariable Long id){
        var response = stockService.findStockByProductId(id);
        return ResponseEntity.ok(response);
    }
}
