package br.com.caju.authorization.repository;

import br.com.caju.authorization.dto.ResultEnum;
import br.com.caju.authorization.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.xml.transform.Result;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {

    List<Transaction> findByMerchant(String merchant);
    List<Transaction> findByMerchantAndResult(String merchant, ResultEnum result);
    List<Transaction> findByAccountId(Long accountId);
    List<Transaction> findByAccountIdAndResult(Long accountId, ResultEnum result);
    List<Transaction> findByResult(ResultEnum result);
    List<Transaction> findByAccountIdAndMerchantAndAmountAndMccAndPurchaseDateBetween(Long accountId, String merchant, BigDecimal amount, Integer mcc, LocalDateTime startTime, LocalDateTime endTime);
}
