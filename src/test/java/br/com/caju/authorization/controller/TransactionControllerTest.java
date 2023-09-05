package br.com.caju.authorization.controller;

import br.com.caju.authorization.dto.ResultEnum;
import br.com.caju.authorization.dto.TransactionApproved;
import br.com.caju.authorization.dto.TransactionRequest;
import br.com.caju.authorization.dto.TransactionResponse;
import br.com.caju.authorization.entity.Account;
import br.com.caju.authorization.entity.Transaction;
import br.com.caju.authorization.repository.AccountRepository;
import br.com.caju.authorization.repository.TransactionRepository;
import br.com.caju.authorization.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
class TransactionControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<TransactionRequest> transactionRequestJson;

    @Autowired
    private JacksonTester<List<TransactionResponse>> transactionResponseJson;

    @Autowired
    private JacksonTester<TransactionApproved> transactionApprovedJson;

    @MockBean
    private TransactionService service;

    @Mock
    private Account account;

    @Mock
    private TransactionRepository repository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ModelMapper mapper;

    private TransactionService service2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        service2 = new TransactionService(repository, accountRepository, mapper);
    }



    @Test
    @DisplayName("Teste que deve retornar 400, pois a requisição está sendo enviada sem os campos")
    void realizarTransacaoErro400() throws Exception {
        var response = mvc.perform(post("/transaction"))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Teste que deve retornar 400, pois a requisição está sendo enviada com o valor do amount = 0")
    void realizarTransacaoValorIgualAZero() throws Exception {
        var response = mvc.perform(post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionRequestJson.write(new TransactionRequest(1L, BigDecimal.ZERO,"Padaria do Fulano",5812))
                                .getJson()))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Teste que deve retornar 201, pois a requisição está sendo enviada corretamente")
    void realizarTransacao() throws Exception {

        var transactionApproved = new TransactionApproved(ResultEnum.APROVADA,null);
        when(service.realizaTransacao(any())).thenReturn(transactionApproved);

        var response = mvc.perform(post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transactionRequestJson.write(new TransactionRequest(1L, BigDecimal.valueOf(100),"Padaria do Fulano",5812))
                                .getJson()))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        var jsonRetorno = transactionApprovedJson.write(transactionApproved).getJson();

        assertThat(response.getContentAsString()).isEqualTo(jsonRetorno);
    }

    @Test
    @DisplayName("Teste findByMerchant que deve retornar 200 com lista de transações não vazia")
    void findByMerchant() throws Exception {
        String merchantName = "Padaria do Fulano";

        // Mock do serviço para retornar uma lista de transações não vazia
        List<TransactionResponse> transactions = new ArrayList<>();
        transactions.add(new TransactionResponse(/* dados da transação */));
        when(service.findByMerchant(anyString())).thenReturn(transactions);

        var response = mvc.perform(get("/transaction/merchant/{merchant}", merchantName))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // Verifica se o JSON retornado corresponde à lista de transações
        var jsonRetorno = transactionResponseJson.write(transactions).getJson();
        assertThat(response.getContentAsString()).isEqualTo(jsonRetorno);
    }

    @Test
    @DisplayName("Teste findByAccountId que deve retornar 200 com lista de transações não vazia")
    void findByAccountId() throws Exception {
        Long accountId = 12345L;

        // Mock do serviço para retornar uma lista de transações não vazia
        List<TransactionResponse> transactions = new ArrayList<>();
        transactions.add(new TransactionResponse(/* dados da transação */));
        when(service.findByAccountId(anyLong())).thenReturn(transactions);

        var response = mvc.perform(get("/transaction/accountId/{accountId}", accountId))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // Verifica se o JSON retornado corresponde à lista de transações
        var jsonRetorno = transactionResponseJson.write(transactions).getJson();
        assertThat(response.getContentAsString()).isEqualTo(jsonRetorno);
    }

    @Test
    @DisplayName("Teste do método verificaSaldo com saldo suficiente")
    void testVerificaSaldoComSaldoSuficiente() {
        BigDecimal amount = BigDecimal.valueOf(100);
        BigDecimal balance = BigDecimal.valueOf(200);

        boolean result = service2.verificaSaldo(amount, balance);

        assertTrue(result);
    }

    @Test
    @DisplayName("Teste do método verificaSaldo com saldo insuficiente")
    void testVerificaSaldoComSaldoInsuficiente() {
        BigDecimal amount = BigDecimal.valueOf(200);
        BigDecimal balance = BigDecimal.valueOf(100);

        boolean result = service2.verificaSaldo(amount, balance);

        assertFalse(result);
    }

    @Test
    @DisplayName("Teste do método defineTipoSaldo com MCC 5411")
    void testDefineTipoSaldoComMCC5411() {
        int mcc = 5411;
        when(account.getFoodBalance()).thenReturn(BigDecimal.valueOf(100));

        BigDecimal result = service2.defineTipoSaldo(mcc, account);

        assertEquals(BigDecimal.valueOf(100), result);
    }

    @Test
    @DisplayName("Teste do método defineTipoSaldo com MCC 9999 (default)")
    void testDefineTipoSaldoComDefaultMCC() {
        int mcc = 9999;
        when(account.getCashBalance()).thenReturn(BigDecimal.valueOf(200));

        BigDecimal result = service2.defineTipoSaldo(mcc, account);

        assertEquals(BigDecimal.valueOf(200), result);
    }

//    @Test
//    @DisplayName("Teste do método atualizaSaldo com MCC 5411 e transação aprovada")
//    void testAtualizaSaldoComMCC5411ETransacaoAprovada() {
//        int mcc = 5411;
//        BigDecimal amount = BigDecimal.valueOf(50);
//        BigDecimal balance = account.getFoodBalance();
//        when(account.getFoodBalance()).thenReturn(BigDecimal.valueOf(100));
//        when(account.setFoodBalance(any())).thenAnswer(invocation -> {
//            BigDecimal newBalance = invocation.getArgument(0);
//            when(account.getFoodBalance()).thenReturn(newBalance);
//            return null;
//        });
//
//        service.atualizaSaldo(account, mcc, amount, true);
//
//        verify(account, times(1)).setFoodBalance(BigDecimal.valueOf(50));
//    }

    @Test
    @DisplayName("Teste do método checkDuplicateTransaction com transação duplicada")
    void testCheckDuplicateTransactionComTransacaoDuplicada() {
        Account account1 = new Account();
        TransactionRequest request = new TransactionRequest(1L,BigDecimal.valueOf(100),"Padaria do Fulano",5812);
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime startTime = currentTime.minusSeconds(60);
        List<Transaction> similarTransactions = new ArrayList<>();
        similarTransactions.add(new Transaction(1L,account1,BigDecimal.valueOf(100),"Padaria do Fulano",5812,ResultEnum.APROVADA,null,LocalDateTime.now()));
        when(repository.findByAccountIdAndMerchantAndAmountAndMccAndPurchaseDateBetween(
                request.getAccountId(), request.getMerchant(), request.getAmount(), request.getMcc(), startTime, currentTime))
                .thenReturn(similarTransactions);

        boolean result = service2.checkDuplicateTransaction(request);

        assertTrue(result);
        verify(repository, times(1)).save(any(Transaction.class));
    }
}