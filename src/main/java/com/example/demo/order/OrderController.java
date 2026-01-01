package com.example.demo.order;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public Order createOrder(@RequestBody Order order) {
        return service.create(order);
    }

    @GetMapping("/list")
    public List<Order> getAllOrders() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Long id) {
        service.delete(id);
    }
}