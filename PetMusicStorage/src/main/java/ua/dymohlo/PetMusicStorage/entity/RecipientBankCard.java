package ua.dymohlo.PetMusicStorage.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
@Table(name = "recipient_bank_card")
public class RecipientBankCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "bank_card_name")
    private String bankCardName;
    @Column(name = "bank_card_number")
    private long bankCardNumber;
}
