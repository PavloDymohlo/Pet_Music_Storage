package ua.dymohlo.PetMusicStorage.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ua.dymohlo.PetMusicStorage.entity.PaymentRecipientData;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@DataJpaTest
public class PaymentRecipientDataRepositoryTest {

    @Mock
    private PaymentRecipientDataRepository paymentRecipientDataRepository;

    @BeforeEach
    void setUp() {
        PaymentRecipientData paymentRecipientData = PaymentRecipientData.builder()
                .transactionalName("transactionalName").build();

        when(paymentRecipientDataRepository.findByTransactionalNameIgnoreCase("transactionalName"))
                .thenReturn(paymentRecipientData);
    }

    @Test
    public void findByTransactionalName_success() {
        PaymentRecipientData paymentRecipientData = paymentRecipientDataRepository.findByTransactionalNameIgnoreCase("transactionalName");

        assertNotNull(paymentRecipientData);
    }

    @Test
    public void findByTransactionalName_notFound() {
        PaymentRecipientData paymentRecipientData = paymentRecipientDataRepository.findByTransactionalNameIgnoreCase("anotherTransactionalName");

        assertNull(paymentRecipientData);
    }
}
