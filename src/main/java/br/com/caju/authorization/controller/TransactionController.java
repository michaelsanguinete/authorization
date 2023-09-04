package br.com.caju.authorization.controller;

import br.com.caju.authorization.dto.TransactionApproved;
import br.com.caju.authorization.dto.TransactionRequest;
import br.com.caju.authorization.dto.TransactionResponse;
import br.com.caju.authorization.entity.Transaction;
import br.com.caju.authorization.service.TransactionService;
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
    public ResponseEntity realizaTransacao(@RequestBody @Valid TransactionRequest request){
        TransactionApproved transactionApproved = service.realizaTransacao(request);
        return new ResponseEntity<>(transactionApproved, HttpStatus.CREATED);
    }

    @GetMapping("/merchant/{merchant}")
    public ResponseEntity<List<TransactionResponse>> findByMerchant (@PathVariable String merchant){
        return ResponseEntity.ok(service.findByMerchant(merchant));
    }

    @GetMapping("/accountId/{accountId}")
    public ResponseEntity<List<TransactionResponse>> findByAccountId(@PathVariable Long accountId){
        return ResponseEntity.ok(service.findByAccountId(accountId));
    }
}
