package br.com.caju.authorization.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "account")
    private List<Transaction> transactions;

    private BigDecimal foodBalance;
    private BigDecimal mealBalance;
    private BigDecimal cashBalance;
}
