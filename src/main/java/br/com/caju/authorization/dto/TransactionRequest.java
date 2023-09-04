package br.com.caju.authorization.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
