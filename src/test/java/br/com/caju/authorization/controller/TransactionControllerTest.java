package br.com.caju.authorization.controller;

import br.com.caju.authorization.dto.ResultEnum;
import br.com.caju.authorization.dto.TransactionApproved;
import br.com.caju.authorization.dto.TransactionRequest;
import br.com.caju.authorization.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
class TransactionControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<TransactionRequest> transactionRequestJson;

    @Autowired
    private JacksonTester<TransactionApproved> transactionApprovedJson;

    @MockBean
    private TransactionService service;;

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
}