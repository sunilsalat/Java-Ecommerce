package com.example.demo.product;

import java.util.List;

public interface ProductService {

    Product create(Product product);

    List<Product> getAll();

    Product getById(Long id);

    void delete(Long id);
}
