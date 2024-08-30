package ua.dymohlo.PetMusicStorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.dymohlo.PetMusicStorage.entity.UserBankCard;
import ua.dymohlo.PetMusicStorage.repository.UserBankCardRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBankCardService {
    private final UserBankCardRepository userBankCardRepository;

    public boolean validateBankCard(UserBankCard userBankCard) {
        UserBankCard existingCard = userBankCardRepository.findByCardNumber(userBankCard.getCardNumber());
        if (existingCard == null) {
            return false;
        }
        if (!existingCard.getCardExpirationDate().equals(userBankCard.getCardExpirationDate())) {
            throw new IllegalArgumentException("Bank card with number " + existingCard.getCardNumber() +
                    " already exists, but card expiration date is invalid");
        } else if (existingCard.getCvv() != userBankCard.getCvv()) {
            throw new IllegalArgumentException("Bank card with number " + existingCard.getCardNumber() +
                    " already exists, but card cvv is invalid");
        }
        return true;
    }

    public void deleteBankCard(long userBankCard) {
         userBankCardRepository.deleteByCardNumber(userBankCard);
    }

    public int checkBankCardUsers(long userBankCard) {
        UserBankCard findUserBankCard = userBankCardRepository.findByCardNumber(userBankCard);
        log.info("Bank card with number {} has {} users", findUserBankCard.getCardNumber(), findUserBankCard.getUsers().size());
        return findUserBankCard.getUsers().size();
    }
}