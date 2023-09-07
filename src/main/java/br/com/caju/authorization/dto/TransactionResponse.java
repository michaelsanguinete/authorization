package br.com.caju.authorization.dto;

import br.com.caju.authorization.entity.Transaction;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {

    private Long accountId;
    private BigDecimal amount;
    private String merchant;
    private Integer mcc;
    private ResultEnum result;
    private String rejectionCause;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime purchaseDate;

    public TransactionResponse(Transaction transaction){
        this.accountId = transaction.getAccount().getId();
        this.amount = transaction.getAmount();
        this.merchant = transaction.getMerchant();
        this.mcc = transaction.getMcc();
        this.result = transaction.getResult();
        this.rejectionCause = transaction.getRejectionCause();
        this.purchaseDate = transaction.getPurchaseDate();
    }
}
