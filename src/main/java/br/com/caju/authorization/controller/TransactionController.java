package br.com.caju.authorization.controller;

import br.com.caju.authorization.dto.ResultEnum;
import br.com.caju.authorization.dto.TransactionApproved;
import br.com.caju.authorization.dto.TransactionRequest;
import br.com.caju.authorization.dto.TransactionResponse;
import br.com.caju.authorization.entity.Transaction;
import br.com.caju.authorization.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/transaction")
public class TransactionController {

    private final TransactionService service;

    @PostMapping
    @Transactional
    @Operation(summary = "Cadastra uma nova transação")
    public ResponseEntity<TransactionApproved> realizaTransacao(@RequestBody @Valid TransactionRequest request){
        TransactionApproved transactionApproved = service.realizaTransacao(request);
        return new ResponseEntity<>(transactionApproved, HttpStatus.CREATED);
    }

    @GetMapping("/merchant/result/{merchant}")
    @Operation(summary = "Busca transações pelo nome do estabelecimento")
    public ResponseEntity<List<TransactionResponse>> findByMerchantAndResult (@PathVariable String merchant, @RequestParam(required = false) ResultEnum result){
        return ResponseEntity.ok(service.findByMerchantAndResult(merchant, result));
    }

    @GetMapping("/accountId/result/{accountId}")
    @Operation(summary = "Busca transações pelo ID da conta")
    public ResponseEntity<List<TransactionResponse>> findByAccountIdAndResult(@PathVariable Long accountId, @RequestParam(required = false) ResultEnum result){
        return ResponseEntity.ok(service.findByAccountIdAndResult(accountId, result));
    }

    @GetMapping("/result/{result}")
    @Operation(summary = "Busca transações pelo status da compra")
    public ResponseEntity<List<TransactionResponse>> findByResult(@PathVariable ResultEnum result){
        return ResponseEntity.ok(service.findByResult(result));
    }
}
