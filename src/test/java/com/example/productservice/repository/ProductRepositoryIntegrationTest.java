package com.example.productservice.repository;

import com.example.productservice.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProductRepositoryIntegrationTest {

    @Autowired
    ProductRepository repo;

    @Test
    void findByNameContainingIgnoreCase_works(){
        Product p1 = new Product("Apple phone", "d", BigDecimal.TEN);
        Product p2 = new Product("Apple watch", "d", BigDecimal.ONE);
        repo.save(p1);
        repo.save(p2);

        List<Product> res = repo.findByNameContainingIgnoreCase("apple");
        assertEquals(2, res.size());
    }
}