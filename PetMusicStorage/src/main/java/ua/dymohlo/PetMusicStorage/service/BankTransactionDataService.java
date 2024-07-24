package ua.dymohlo.PetMusicStorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.dymohlo.PetMusicStorage.dto.NewBankTransactionDataDTO;
import ua.dymohlo.PetMusicStorage.entity.BankTransactionData;
import ua.dymohlo.PetMusicStorage.repository.BankTransactionDataRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankTransactionDataService {
    private final BankTransactionDataRepository bankTransactionDataRepository;

    public void addBankTransactionData(NewBankTransactionDataDTO newBankTransactionDataDTO) {
        String bankName = newBankTransactionDataDTO.getBankName();
        if (bankTransactionDataRepository.existsByBankNameIgnoreCase(bankName)) {
            throw new IllegalArgumentException("Bank with bank's name " + bankName + " already exists");
        }
        String bankUrlTransaction = newBankTransactionDataDTO.getBankUrlTransaction();
        if (bankTransactionDataRepository.existsByBankUrlTransactionIgnoreCase(bankUrlTransaction)) {
            throw new IllegalArgumentException("Bank with bank's url " + bankUrlTransaction + " already exists");
        }
        BankTransactionData bankTransactionData = BankTransactionData.builder()
                .bankName(bankName)
                .bankUrlTransaction(bankUrlTransaction).build();
        bankTransactionDataRepository.save(bankTransactionData);
    }

    public BankTransactionData findByBankTransactionDataName(String bankTransactionDataName) {
        return bankTransactionDataRepository.findByBankNameIgnoreCase(bankTransactionDataName);
    }
}
