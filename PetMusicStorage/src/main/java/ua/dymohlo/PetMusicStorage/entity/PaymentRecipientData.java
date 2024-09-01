package ua.dymohlo.PetMusicStorage.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString(exclude = {"recipientBankCard", "bankTransactionData"})
@Table(name = "payment_recipient_data")
public class PaymentRecipientData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "transactional_name", unique = true, nullable = false)
    private String transactionalName;
    @OneToOne
    @JoinColumn(name = "bank_card_id", nullable = false)
    private RecipientBankCard recipientBankCard;
    @OneToOne
    @JoinColumn(name = "bank_name_id", nullable = false)
    private BankTransactionData bankTransactionData;
}