package ua.dymohlo.PetMusicStorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.dymohlo.PetMusicStorage.entity.BankTransactionData;

@Repository
public interface BankTransactionDataRepository extends JpaRepository<BankTransactionData, Long> {
    BankTransactionData findByBankName(String bankTransactionDataName);
    boolean existsByBankName(String bankTransactionDataName);
    boolean existsByBankUrlTransaction(String bankUrlTransaction);
}
