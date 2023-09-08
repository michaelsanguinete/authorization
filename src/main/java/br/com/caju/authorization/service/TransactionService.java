package br.com.caju.authorization.service;

import br.com.caju.authorization.dto.ResultEnum;
import br.com.caju.authorization.dto.TransactionApproved;
import br.com.caju.authorization.dto.TransactionRequest;
import br.com.caju.authorization.dto.TransactionResponse;
import br.com.caju.authorization.entity.Account;
import br.com.caju.authorization.entity.Transaction;
import br.com.caju.authorization.repository.AccountRepository;
import br.com.caju.authorization.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository repository;
    private final AccountRepository accountRepository;
    private final ModelMapper mapper;

    public List<TransactionResponse> findByMerchantAndResult(String merchant, ResultEnum result) {
        List<Transaction> transactions;
        if (result == null) transactions = repository.findByMerchant(merchant);
        else transactions = repository.findByMerchantAndResult(merchant, result);
        if(transactions.isEmpty()) throw new EntityNotFoundException();
        return transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse>findByAccountIdAndResult(Long accountId, ResultEnum result) {
        List<Transaction> transactions;
        if (result == null) transactions = repository.findByAccountId(accountId);
        else transactions = repository.findByAccountIdAndResult(accountId, result);
        if(transactions.isEmpty()) throw new EntityNotFoundException();
        return transactions.stream()
                .map(TransactionResponse::new)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse>findByResult(ResultEnum result) {
        List<Transaction> transactions = repository.findByResult(result);
        if(transactions.isEmpty()) throw new EntityNotFoundException();
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

    public boolean verificaSaldo(BigDecimal amount, BigDecimal balance) {
        return balance.compareTo(amount) >= 0;
    }

    public BigDecimal defineTipoSaldo(Integer mcc, Account account) {
        return switch (mcc) {
            case 5411, 5412 -> account.getFoodBalance();
            case 5811, 5812 -> account.getMealBalance();
            default -> account.getCashBalance();
        };
    }

    public void atualizaSaldo(Account account, Integer mcc, BigDecimal amount, boolean isApproved) {
        BigDecimal newBalance = isApproved ? defineTipoSaldo(mcc, account).subtract(amount) : defineTipoSaldo(mcc, account);
        switch (mcc) {
            case 5411, 5412 -> account.setFoodBalance(newBalance);
            case 5811, 5812 -> account.setMealBalance(newBalance);
            default -> account.setCashBalance(newBalance);
        }
    }

    public boolean checkDuplicateTransaction(TransactionRequest request) {
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
