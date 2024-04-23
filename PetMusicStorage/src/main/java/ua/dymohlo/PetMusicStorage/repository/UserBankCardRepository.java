package ua.dymohlo.PetMusicStorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;

@Repository
public interface UserBankCardRepository extends JpaRepository<UserBankCard, Long> {
    UserBankCard findByCardNumber(long cardNumber);
}
