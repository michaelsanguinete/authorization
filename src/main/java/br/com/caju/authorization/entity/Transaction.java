package br.com.caju.authorization.entity;

import br.com.caju.authorization.dto.ResultEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id") // Nome da coluna que faz referência à conta
    private Account account;

    private BigDecimal amount;
    private String merchant;
    private Integer mcc;
    private ResultEnum result;
    private String rejectionCause;
}
