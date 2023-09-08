package br.com.caju.authorization.service;

import br.com.caju.authorization.entity.Account;
import br.com.caju.authorization.repository.AccountRepository;
import br.com.caju.authorization.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class TransactionServiceTest {

    @Mock
    private Account account;

    private TransactionService service;

    @Mock
    private TransactionRepository repository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ModelMapper mapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TransactionService(repository, accountRepository, mapper);
    }

    @Test
    @DisplayName("Teste do método verificaSaldo com saldo suficiente")
    void testVerificaSaldoComSaldoSuficiente() {
        BigDecimal amount = BigDecimal.valueOf(100);
        BigDecimal balance = BigDecimal.valueOf(200);

        boolean result = service.verificaSaldo(amount, balance);

        assertTrue(result);
    }

    @Test
    @DisplayName("Teste do método verificaSaldo com saldo insuficiente")
    void testVerificaSaldoComSaldoInsuficiente() {
        BigDecimal amount = BigDecimal.valueOf(200);
        BigDecimal balance = BigDecimal.valueOf(100);

        boolean result = service.verificaSaldo(amount, balance);

        assertFalse(result);
    }

    @Test
    @DisplayName("Teste do método defineTipoSaldo com MCC 5411")
    void testDefineTipoSaldoComMCC5411() {
        int mcc = 5411;
        when(account.getFoodBalance()).thenReturn(BigDecimal.valueOf(100));

        BigDecimal result = service.defineTipoSaldo(mcc, account);

        assertEquals(BigDecimal.valueOf(100), result);
    }

    @Test
    @DisplayName("Teste do método defineTipoSaldo com MCC 9999 (default)")
    void testDefineTipoSaldoComDefaultMCC() {
        int mcc = 9999;
        when(account.getCashBalance()).thenReturn(BigDecimal.valueOf(200));

        BigDecimal result = service.defineTipoSaldo(mcc, account);

        assertEquals(BigDecimal.valueOf(200), result);
    }
}
