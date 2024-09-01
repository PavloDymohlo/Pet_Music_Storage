package ua.dymohlo.PetMusicStorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.dymohlo.PetMusicStorage.entity.RecipientBankCard;

@Repository
public interface RecipientBankCardRepository extends JpaRepository<RecipientBankCard, Long> {
    RecipientBankCard findByBankCardNameIgnoreCase(String bankCardName);
}