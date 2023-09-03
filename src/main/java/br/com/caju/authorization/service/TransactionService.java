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
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository repository;
    private final AccountRepository accountRepository;
    private final ModelMapper mapper;

    public List<TransactionResponse> findByMerchant(String merchant) {
        List<Transaction> transactions = repository.findByMerchant(merchant);
        List<TransactionResponse> responses = new ArrayList<>();
        transactions.forEach(de -> responses.add(new TransactionResponse(de)));
        return responses;
    }

    public List<TransactionResponse> findByAccountId(Long accountId){
        List<Transaction> transactions = repository.findByAccountId(accountId);
        List<TransactionResponse> responses = new ArrayList<>();
        transactions.forEach(de -> responses.add(new TransactionResponse(de)));
        return responses;
    }

    public TransactionApproved realizaTransacao(TransactionRequest request) {
        BigDecimal amount = request.getAmount();
        Account account = accountRepository.getReferenceById(request.getAccountId());

        //Verifica se a transação já não foi feita no mesmo estabelecimento, com o mesmo valor e o mesmo mcc no tempo definido
        if (checkDuplicateTransaction(request.getAccountId(), request.getMerchant(), amount, 60)) {
            Transaction transaction = mapper.map(request, Transaction.class);
            transaction.setRejectionCause("Transação duplicada!");
            transaction.setResult(ResultEnum.REJEITADA);
            transaction.setId(null);
            repository.save(transaction);

            return new TransactionApproved(ResultEnum.REJEITADA, "Transação já realizada, verifique o seu extrato ou aguarde alguns instantes e tente novamente");
        }

        //Define qual tipo de saldo será debitado na transação
        BigDecimal balance = defineTipoSaldo(request.getMcc(), account);

        //Verifica se existe saldo e aprova ou rejeita a transação
        ResultEnum result = verificaSaldo(amount, balance) ? ResultEnum.APROVADA : ResultEnum.REJEITADA;
        String rejectionCause = verificaSaldo(amount, balance) ? null : "Saldo insuficiente!";

        atualizaSaldo(account, request.getMcc(), amount, verificaSaldo(amount, balance));

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

    private boolean checkDuplicateTransaction(Long accountId, String merchant, BigDecimal amount, int seconds) {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startTime = currentTime.minusSeconds(seconds);

        List<Transaction> similarTransactions = repository.findByAccountIdAndMerchantAndAmountAndPurchaseDateBetween(accountId, merchant, amount, startTime, currentTime);
        return (!similarTransactions.isEmpty());
    }

}
