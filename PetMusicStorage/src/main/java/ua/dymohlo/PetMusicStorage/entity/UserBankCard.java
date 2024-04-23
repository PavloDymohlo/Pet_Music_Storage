package ua.dymohlo.PetMusicStorage.entity;


import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString(exclude = "users")
@Table(name = "users_bank_cards")
public class UserBankCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "card_number")
    private long cardNumber;
    @Column(name = "cvv")
    private short cvv;
    @Column(name = "card_expiration_date")
    private String cardExpirationDate;
    @OneToMany(mappedBy = "userBankCard", cascade = CascadeType.ALL)
    private List<User> users;
}
