package ua.dymohlo.PetMusicStorage.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString(exclude = "paymentRecipientData")
@Table(name = "recipient_bank_card")
public class RecipientBankCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "bank_card_name", unique = true, nullable = false)
    private String bankCardName;

    @Column(name = "bank_card_number", unique = true, nullable = false)
    private long bankCardNumber;

    @OneToOne(mappedBy = "recipientBankCard")
    @JsonIgnore
    private PaymentRecipientData paymentRecipientData;
}

