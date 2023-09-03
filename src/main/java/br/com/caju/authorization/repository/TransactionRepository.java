package br.com.caju.authorization.repository;

import br.com.caju.authorization.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {

    List<Transaction> findByMerchant(String merchant);
    List<Transaction> findByAccountId(Long accountId);

    List<Transaction> findByAccountIdAndMerchantAndAmountAndPurchaseDateBetween(Long accountId, String merchant, BigDecimal amount, LocalDateTime startTime, LocalDateTime endTime);
}
