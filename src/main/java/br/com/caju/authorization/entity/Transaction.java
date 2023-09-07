package br.com.caju.authorization.entity;

import br.com.caju.authorization.dto.ResultEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id") // Nome da coluna que faz referência à conta
    private Account account;

    @DecimalMin("0.01")
    private BigDecimal amount;
    private String merchant;
    private Integer mcc;
    @Enumerated
    private ResultEnum result;
    private String rejectionCause;
    private LocalDateTime purchaseDate = LocalDateTime.now();
}
