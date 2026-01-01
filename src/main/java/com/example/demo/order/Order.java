package com.example.demo.order;

import java.time.Instant;
import java.util.Map;
import com.example.demo.product.Product;
import com.example.demo.auth.User;

import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@Entity
@Table(name = "orders")
public class Order {
    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // foreign key column
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id") // foreign key column
    @NotBlank
    private Product product;

    @NotBlank
    @NotNull
    private Instant orderDate;

    @NotBlank
    private Integer quantity;

    @NotBlank
    private Double totalPrice;

    @NotBlank
    private OrderStatus status;

    @NotBlank
    @Type(value = JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> shippingAddress;

}
