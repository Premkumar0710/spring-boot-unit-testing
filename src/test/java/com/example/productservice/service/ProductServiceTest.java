package com.example.productservice.service;

import com.example.productservice.exception.NotFoundException;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository repo;

    @InjectMocks
    ProductService service;

    @BeforeAll
    static void log_startTestSuite(){
        System.out.println("Unit test Execution started for the Service class");
    }

    @AfterAll
    static void log_endTestSuite(){
        System.out.println("Unit test Execution terminated for the Service class");
    }

    @BeforeEach
    void logging_StartTestExecution(){
        String methodName =
                StackWalker.getInstance()
                        .walk(frames -> frames.findFirst().get().getMethodName());
        System.out.println("Execution started for the method : "+ methodName);
    }

    @AfterEach
    void logging_EndTestExecution(){
        String methodName =
                StackWalker.getInstance()
                        .walk(frames -> frames.findFirst().get().getMethodName());
        System.out.println("Execution ended for the method : "+ methodName);
    }

    // Test cases starts here
    @Test
    @DisplayName("Validating exception by passing negative price value")
    void create_withNegativePrice_throws(){
        Product p = new Product("name","d", BigDecimal.valueOf(-1));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.create(p));
        assertEquals("Price must be non-negative", ex.getMessage());
        verifyNoInteractions(repo);
    }

    @Test
    @DisplayName("Validating exception by passing null price value")
    void create_withNullPrice_throws(){
        Product p = new Product("name","d", null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.create(p));
        assertEquals("Price must be non-negative", ex.getMessage());
        verifyNoInteractions(repo);
    }

    @Test
    void create_withValidProduct_callsSave(){
        Product p = new Product("name","d", BigDecimal.valueOf(10));
        when(repo.save(p)).thenReturn(p);
        Product saved = service.create(p);
        assertSame(p, saved);
        verify(repo).save(p);
    }

    @Test
    void getById_whenExists_returns(){
        Product p = new Product("n","d", BigDecimal.ONE);
        when(repo.findById(1L)).thenReturn(Optional.of(p));
        assertEquals(p, service.getById(1L));
    }

    @Test
    void getById_whenMissing_throwsNotFound(){
        when(repo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getById(1L));
    }

    @Test
    void searchByName_emptyQuery_returnsEmptyList(){
        assertTrue(service.searchByName(null).isEmpty());
        assertTrue(service.searchByName("").isEmpty());
        verifyNoInteractions(repo);
    }

    @Test
    void searchByName_validCallsRepo(){
        when(repo.findByNameContainingIgnoreCase("x")).thenReturn(List.of(new Product("x","d", BigDecimal.ONE)));
        List<Product> res = service.searchByName("x");
        assertEquals(1, res.size());
        verify(repo).findByNameContainingIgnoreCase("x");
    }

    @Test
    void updatePrice_NegativePricing_throws(){
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, ()-> service.updatePrice(19L,BigDecimal.valueOf(-100L)));
        assertEquals("Price must be non-negative",ex.getMessage());
        verifyNoInteractions(repo);
    }

    @Test
    void updatePrice_NullPricing_throws(){
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, ()-> service.updatePrice(19L,null));
        assertEquals("Price must be non-negative",ex.getMessage());
        verifyNoInteractions(repo);
    }

    @Test
    void updatePrice_PositiveScenario(){
        // Stubbing
        Product currentProduct = new Product("Iphone", "17 pro max", BigDecimal.valueOf(100000L));
        when(repo.findById(7L)).thenReturn(Optional.of(currentProduct));

        Product updatedPrice = new Product();
        updatedPrice.setPrice(BigDecimal.valueOf(1000L));
        when(repo.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service.updatePrice(7L,updatedPrice.getPrice());

        assertEquals(BigDecimal.valueOf(1000L),updatedPrice.getPrice());
    }


    @Test
    void delete_nonExisting_throws(){
        when(repo.existsById(1L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> service.delete(1L));

        // uncovered lines of code in service
        when(repo.existsById(10L)).thenReturn(true);
        Long id = 10L;
        service.delete(id);
        verify(repo).existsById(id);
        verify(repo).deleteById(id);
    }

    // Below TC's are 6written by me
    @Test
    void renameProduct_blankName_throws(){
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, ()-> service.renameProduct(1l,""));
        assertEquals("Name cannot be empty",ex.getMessage());
        verifyNoInteractions(repo);
    }

    @Test()
    void renameProduct_missingProduct_throwsNotFound(){
        // stubbing data (if id 7 is empty we need to throw the exception & we validated that as well)
        when(repo.findById(7L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, ()->service.renameProduct(7L,"NewName"));
    }

    @Test
    void renameProduct_valid_updatesAndSaves() {

        // Arrange
        Product existingProduct = new Product("Iphone", "17 pro max", BigDecimal.valueOf(100000L));
        existingProduct.setId(17L);

        when(repo.findById(17L)).thenReturn(Optional.of(existingProduct));

        // simulate repo.save returning the passed product
        when(repo.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Product result = service.renameProduct(17L, "Iphone air");

        // Assert actual returned product
        assertEquals("Iphone air", result.getName());
    }

    @Test
    void updateStock_positive_addsStockAndSaves(){
        Long id = 7L;
        int delta = 4;
        Product product = new Product("T-Shirts","Clothing",BigDecimal.valueOf(1000L));
        product.setStock(7);
        // stubbing
        when(repo.findById(id)).thenReturn(Optional.of(product));
        when(repo.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product newStock = service.updateStock(id,delta);
        assertEquals(11,id+delta);

    }

    @Test
    void updateStock_NullIdAndDeltaZero_Throw(){
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, ()-> service.updateStock(null,4));
        assertEquals("id cannot be null",ex1.getMessage());

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, ()-> service.updateStock(7L,0));
        assertEquals("delta cannot be zero",ex2.getMessage());
    }

    @Test
    void updateStock_NegativeStock_Throw(){
        Product p = new Product();
        p.setStock(5);

        when(repo.findById(1L)).thenReturn(Optional.of(p));

        assertThrows(IllegalArgumentException.class, ()-> service.updateStock(1L,-10));
        verify(repo).findById(1L);
    }

    @Test
    void updateStock_positive_updatesAndSaves() {
        Product p = new Product();
        p.setStock(5);

        when(repo.findById(1L)).thenReturn(Optional.of(p));
        when(repo.save(any())).thenReturn(p); // simplest possible save stub

        Product result = service.updateStock(1L, 3); // 5 + 3 = 8

        assertEquals(8, result.getStock()); // ensures setStock happened
        verify(repo).save(p);               // ensures save() was called
    }

    @Test
    void updateProductName_handleIllegalArgumentException_Throws(){

       IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, ()-> service.updateProductName(null,"Cycles"));
       assertEquals("id cannot be null",ex1.getMessage());

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, ()-> service.updateProductName(1L," "));
        assertEquals("name cannot be empty",ex2.getMessage());

        verifyNoInteractions(repo);
    }

    @Test
    void updateProductName_handleNotFoundException_Throws(){

        when(repo.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,()-> service.updateProductName(1L,"Cycle"));
        assertEquals("Product not found",ex.getMessage());

    }

    @Test
    void updateProductName_PositiveScenario() {

        Long id = 1L;

        Product product = new Product();
        product.setId(id);
        product.setName("OldName");   // IMPORTANT: name must be different

        when(repo.findById(id)).thenReturn(Optional.of(product));
        when(repo.save(any())).thenReturn(product);  // simplest save stub

        Product result = service.updateProductName(id, "NewName");

        assertEquals("NewName", result.getName());
    }

    @Test
    void toggleProductAvailability_handleExceptions_Throws(){

        // Stubbing
        when(repo.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,()->service.toggleProductAvailability(null));
        assertEquals("id cannot be null",ex.getMessage());

        NotFoundException nfe = assertThrows(NotFoundException.class, ()->service.toggleProductAvailability(1L));
        assertEquals("Product not found",nfe.getMessage());
    }

    @Test
    void toggleProductAvailability_whenAvailable_becomesUnavailable() {
        Long id = 1L;

        Product p = new Product();
        p.setId(id);
        p.setAvailable(true); // initial state

        when(repo.findById(id)).thenReturn(Optional.of(p));
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = service.toggleProductAvailability(id);

        assertFalse(result.isAvailable());  // should be toggled to false
    }



}