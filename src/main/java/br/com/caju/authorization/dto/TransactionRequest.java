package br.com.caju.authorization.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionRequest {

    @NotNull
    private Long accountId;

    @NotNull @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private String merchant;

    @NotNull
    private Integer mcc;
}
