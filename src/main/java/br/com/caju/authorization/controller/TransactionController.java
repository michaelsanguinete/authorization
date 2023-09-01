package br.com.caju.authorization.controller;

import br.com.caju.authorization.dto.TransactionRequest;
import br.com.caju.authorization.dto.TransactionResponse;
import br.com.caju.authorization.entity.Transaction;
import br.com.caju.authorization.service.TransactionService;
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
    public ResponseEntity realizaTransacao(@RequestBody TransactionRequest request){
        service.realizaTransacao(request);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @GetMapping("/{merchant}")
    public ResponseEntity<List<TransactionResponse>> findByMerchant (@PathVariable String merchant){
        return ResponseEntity.ok(service.findByMerchant(merchant));
    }
}
