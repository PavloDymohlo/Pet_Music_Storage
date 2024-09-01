package ua.dymohlo.PetMusicStorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.dymohlo.PetMusicStorage.entity.BankTransactionData;

@Repository
public interface BankTransactionDataRepository extends JpaRepository<BankTransactionData, Long> {
    boolean existsByBankNameIgnoreCase(String bankTransactionDataName);

    boolean existsByBankUrlTransactionIgnoreCase(String bankUrlTransaction);
}