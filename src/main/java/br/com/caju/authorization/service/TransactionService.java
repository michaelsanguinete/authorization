package br.com.caju.authorization.service;

import br.com.caju.authorization.dto.ResultEnum;
import br.com.caju.authorization.dto.TransactionApproved;
import br.com.caju.authorization.dto.TransactionRequest;
import br.com.caju.authorization.dto.TransactionResponse;
import br.com.caju.authorization.entity.Account;
import br.com.caju.authorization.entity.Transaction;
import br.com.caju.authorization.repository.AccountRepository;
import br.com.caju.authorization.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository repository;
    private final AccountRepository accountRepository;
    private final ModelMapper mapper;

    public List<TransactionResponse> findByMerchant(String merchant) {
        List<Transaction> transactions = repository.findByMerchant(merchant);
        return transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> findByAccountId(Long accountId) {
        List<Transaction> transactions = repository.findByAccountId(accountId);
        return transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }

    public TransactionApproved realizaTransacao(TransactionRequest request) {
        BigDecimal amount = request.getAmount();
        Account account = accountRepository.getReferenceById(request.getAccountId());

        //Verifica se a transação já não foi feita no mesmo estabelecimento, com o mesmo valor e o mesmo mcc no tempo definido
        if (checkDuplicateTransaction(request)) {
            return new TransactionApproved(ResultEnum.REJEITADA, "Transação já realizada, verifique o seu extrato ou aguarde alguns instantes e tente novamente");
        }

        //Define qual tipo de saldo será debitado na transação
        BigDecimal balance = defineTipoSaldo(request.getMcc(), account);

        //Verifica se existe saldo e aprova ou rejeita a transação
        boolean isApproved = verificaSaldo(amount, balance);
        ResultEnum result = isApproved ? ResultEnum.APROVADA : ResultEnum.REJEITADA;
        String rejectionCause = isApproved ? null : "Saldo insuficiente!";

        atualizaSaldo(account, request.getMcc(), amount, isApproved);

        Transaction transaction = mapper.map(request, Transaction.class);
        transaction.setRejectionCause(rejectionCause);
        transaction.setResult(result);
        transaction.setId(null);

        repository.save(transaction);
        return new TransactionApproved(result, rejectionCause);
    }

    private boolean verificaSaldo(BigDecimal amount, BigDecimal balance) {
        return balance.compareTo(amount) >= 0;
    }

    private BigDecimal defineTipoSaldo(Integer mcc, Account account) {
        return switch (mcc) {
            case 5411, 5412 -> account.getFoodBalance();
            case 5811, 5812 -> account.getMealBalance();
            default -> account.getCashBalance();
        };
    }

    private void atualizaSaldo(Account account, Integer mcc, BigDecimal amount, boolean isApproved) {
        BigDecimal newBalance = isApproved ? defineTipoSaldo(mcc, account).subtract(amount) : defineTipoSaldo(mcc, account);
        switch (mcc) {
            case 5411, 5412 -> account.setFoodBalance(newBalance);
            case 5811, 5812 -> account.setMealBalance(newBalance);
            default -> account.setCashBalance(newBalance);
        }
    }

    private boolean checkDuplicateTransaction(TransactionRequest request) {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startTime = currentTime.minusSeconds(60);
        List<Transaction> similarTransactions = repository.findByAccountIdAndMerchantAndAmountAndMccAndPurchaseDateBetween(request.getAccountId(), request.getMerchant(), request.getAmount(), request.getMcc(), startTime, currentTime);
        if (!similarTransactions.isEmpty()) {
            Transaction transaction = mapper.map(request, Transaction.class);
            transaction.setRejectionCause("Transação duplicada!");
            transaction.setResult(ResultEnum.REJEITADA);
            transaction.setId(null);
            repository.save(transaction);
            return true;
        }
        return false;
    }
}
