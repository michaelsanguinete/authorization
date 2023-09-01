package br.com.caju.authorization.dto;

import br.com.caju.authorization.entity.Transaction;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
public class TransactionResponse {

    private Long accountId;
    private BigDecimal amount;
    private String merchant;
    private Integer mcc;
    private ResultEnum result;
    private String rejectionCause;

    public TransactionResponse(Transaction transaction){
        this.accountId = transaction.getAccount().getId();
        this.amount = transaction.getAmount();
        this.merchant = transaction.getMerchant();
        this.mcc = transaction.getMcc();
        this.result = transaction.getResult();
        this.rejectionCause = transaction.getRejectionCause();
    }
}
