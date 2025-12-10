package com.example.productservice.service;

import com.example.productservice.exception.NotFoundException;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) { this.repo = repo; }

    public Product create(Product p){
        if(p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Price must be non-negative");
        return repo.save(p);
    }

    public Product getById(Long id){
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Product not found"));
    }

    public List<Product> searchByName(String q){
        if(q == null || q.isBlank()) return List.of();
        return repo.findByNameContainingIgnoreCase(q);
    }

    public Product updatePrice(Long id, BigDecimal newPrice){
        if(newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Price must be non-negative");
        Product p = getById(id);
        p.setPrice(newPrice);
        return repo.save(p);
    }

    public void delete(Long id){
        if(!repo.existsById(id)) throw new NotFoundException("Product not found");
        repo.deleteById(id);
    }

    // Below implementations are unit tested & covered by myself
    public Product renameProduct(Long id, String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        Product p = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        p.setName(newName);
        return repo.save(p);
    }

    public Product updateStock(Long id, int delta) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        if (delta == 0) {
            throw new IllegalArgumentException("delta cannot be zero");
        }

        Product p = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        int newStock = p.getStock() + delta;

        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot go negative");
        }

        p.setStock(newStock);
        return repo.save(p);
    }

    public Product updateProductName(Long id, String newName) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("name cannot be empty");
        }

        Product p = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (newName.equals(p.getName())) {
            return p; // no change, do NOT save
        }

        p.setName(newName);

        return repo.save(p);
    }

    public Product toggleProductAvailability(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }

        Product p = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        boolean newStatus = !p.isAvailable();
        p.setAvailable(newStatus);

        return repo.save(p);
    }




}