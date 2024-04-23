package ua.dymohlo.MockBankSystem.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.dymohlo.MockBankSystem.entity.Client;
import ua.dymohlo.MockBankSystem.repository.ClientRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;

    @Transactional
    public void transactionMoney(long outputCardNumber, long targetCardNumber, int sum, String cardExpirationDate, short cvv) {
        if (validCardData(outputCardNumber, cardExpirationDate, cvv)) {
            Client payer = clientRepository.findByCardNumber(outputCardNumber);
            Client recipient = clientRepository.findByCardNumber(targetCardNumber);
            if (payer.getBalance() >= sum) {
                int newPayerBalance = payer.getBalance() - sum;
                payer.setBalance(newPayerBalance);
                clientRepository.save(payer);

                int newRecipientBalance = recipient.getBalance() + sum;
                recipient.setBalance(newRecipientBalance);
                clientRepository.save(recipient);
                System.out.println("Transaction successful!");
            } else {
                throw new IllegalArgumentException("Insufficient funds!");
            }
        } else {
            throw new IllegalArgumentException("Invalid card data!");
        }
    }

    private boolean validCardData(long cardNumber, String cardExpirationDate, short cvv) {
        Date currentDate = new Date();
        if (clientRepository.existsByCardNumber(cardNumber)) {
            Client client = clientRepository.findByCardNumber(cardNumber);
            if (cardExpirationDate(cardExpirationDate).after(currentDate)
                && client.getCardExpirationDate().equals(cardExpirationDate)) {
                if (client.getCvv() == cvv) {
                    return true;
                }
                throw new IllegalArgumentException("CVV is not correct!");
            }
            throw new IllegalArgumentException("The card has expired, or not correct!");
        }
        throw new IllegalArgumentException("Card with this number not found! ");
    }

    private Date cardExpirationDate(String cardExpirationDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/yy");
        Date expirationDate;
        try {
            expirationDate = simpleDateFormat.parse(cardExpirationDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return expirationDate;
    }
}
