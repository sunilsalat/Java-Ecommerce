package com.example.demo.product;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.auth.CurrentUserService;
import com.example.demo.auth.User;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;

    public ProductServiceImpl(ProductRepository productRepository, CurrentUserService currentUserService) {
        this.productRepository = productRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    public Product create(Product product) {
        User currentUser = currentUserService.getCurrentUser();
        product.setOwnerId(currentUser);
        return productRepository.save(product);
    }

    @Override
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    @Override
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

}
