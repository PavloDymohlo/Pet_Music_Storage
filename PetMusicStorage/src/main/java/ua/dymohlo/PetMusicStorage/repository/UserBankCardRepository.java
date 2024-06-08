package ua.dymohlo.PetMusicStorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;

@Repository
public interface UserBankCardRepository extends JpaRepository<UserBankCard, Long> {
    UserBankCard findByCardNumber(long cardNumber);
    void deleteByCardNumber(long cardNumber);
}
