package com.example.productservice.controller;

import com.example.productservice.model.Product;
import com.example.productservice.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;
    public ProductController(ProductService service){ this.service = service; }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product p){
        Product saved = service.create(p);
        return ResponseEntity.created(URI.create("/api/products/" + saved.getId())).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> get(@PathVariable Long id){
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> search(@RequestParam(required=false) String q){
        return ResponseEntity.ok(service.searchByName(q));
    }

    @PatchMapping("/{id}/{price}")
    public ResponseEntity<Product> updatePrice(@PathVariable Long id, @PathVariable BigDecimal price){
        return ResponseEntity.ok(service.updatePrice(id, price));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}