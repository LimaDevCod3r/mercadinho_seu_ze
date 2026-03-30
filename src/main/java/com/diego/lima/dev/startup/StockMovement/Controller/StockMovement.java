package com.diego.lima.dev.startup.StockMovement.Controller;

import com.diego.lima.dev.startup.StockMovement.Dto.Request.StockMovementRequestDTO;
import com.diego.lima.dev.startup.StockMovement.Dto.Response.StockMovementResponseDTO;
import com.diego.lima.dev.startup.StockMovement.Service.StockMovementService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stock-movements")
public class StockMovement {

    private final StockMovementService stockMovementService;

    public StockMovement(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @PostMapping
    public ResponseEntity<StockMovementResponseDTO> createMovement(@Valid @RequestBody StockMovementRequestDTO request) {
        var response = stockMovementService.createMovement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<StockMovementResponseDTO>> findAllMovement(Pageable pageable) {
        var response = stockMovementService.findAllStockMovement(pageable);
        return ResponseEntity.ok(response);
    }
}
