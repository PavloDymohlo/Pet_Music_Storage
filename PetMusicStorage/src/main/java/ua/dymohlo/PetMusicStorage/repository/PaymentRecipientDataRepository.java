package ua.dymohlo.PetMusicStorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.dymohlo.PetMusicStorage.entity.PaymentRecipientData;

@Repository
public interface PaymentRecipientDataRepository extends JpaRepository<PaymentRecipientData, Long> {
    PaymentRecipientData findByTransactionalNameIgnoreCase(String transactionalName);
}