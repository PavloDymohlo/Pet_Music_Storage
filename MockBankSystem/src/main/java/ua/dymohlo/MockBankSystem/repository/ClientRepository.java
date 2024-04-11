package ua.dymohlo.MockBankSystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.dymohlo.MockBankSystem.entity.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsByCardNumber(long cardNumber);

    Client findByCardNumber(long cardNumber);
}
