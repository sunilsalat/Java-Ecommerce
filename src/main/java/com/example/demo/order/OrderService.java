package com.example.demo.order;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface OrderService {

    Order create(Order order);

    List<Order> getAll();

    Order getById(Long id);

    void delete(Long id);
}
