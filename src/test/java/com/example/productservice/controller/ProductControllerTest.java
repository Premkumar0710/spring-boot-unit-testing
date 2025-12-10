package com.example.productservice.controller;

import com.example.productservice.model.Product;
import com.example.productservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class) // Loads only the web layer (ProductController, Jackson (JSON), Validation)
class ProductControllerTest {

    @Autowired MockMvc mvc; // your fake HTTP client -> Simulates HTTP requests without starting a server. (curl /products/1/availability)
    @Autowired ObjectMapper mapper; // Converts Java object → JSON string

    @MockBean ProductService service; // Mocks the real productService

    @Test
    @DisplayName("Create API")
    void create_returns201() throws Exception {
        // what the client sends in the request
        Product request = new Product("n", "d", BigDecimal.TEN);

        // what the service returns after saving
        Product saved = new Product("n", "d", BigDecimal.TEN);
        saved.setId(1L);

        when(service.create(any())).thenReturn(saved);

        mvc.perform(
                        post("/api/products")
                                .contentType("application/json")
                                .content(mapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/products/1"));
    }

    @Test
    @DisplayName("Get API")
    void get_returns200() throws Exception {
        Product p = new Product("n","d", BigDecimal.ONE);
        when(service.getById(1L)).thenReturn(p);

        mvc.perform(get("/api/products/1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value("n")); // get the value of name field & validate it
    }

    @Test
    @DisplayName("Search API")
    void search_whenNoQuery_returns200AndEmpty() throws Exception {
        when(service.searchByName(null)).thenReturn(List.of());
        mvc.perform(get("/api/products/search"))
          .andExpect(status().isOk())
          .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("Update API")
    void updatePrice_returns200() throws Exception {
        Long id = 1L;
        BigDecimal price = BigDecimal.valueOf(1000);

        // Construct service response
        Product updated = new Product("PS-5", "Enjoy luxury games @home", price);
        updated.setId(id);

        when(service.updatePrice(id, price)).thenReturn(updated);

        mvc.perform(
                        patch("/api/products/{id}/{price}", id, price)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.price").value(1000));

        verify(service).updatePrice(id, price);
    }

    @Test
    @DisplayName("Delete API")
    void delete_returns204() throws Exception {
        Long id = 1L;

        doNothing().when(service).delete(id);
        mvc.perform(
                delete("/api/products/{id}", id)
        )
                .andExpect(status().isNoContent());

        // Can't write below statements because we won't recieve any content in the output
               /* .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(1));*/
    }

}