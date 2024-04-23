package ua.dymohlo.PetMusicStorage.entity;


import lombok.*;
import ua.dymohlo.PetMusicStorage.Enum.AutoRenewStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString(exclude = {"subscription"})
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "phone_number")
    private long phoneNumber;
    @Column(name = "password")
    private String password;
    @Column(name = "auto_renew")
    @Enumerated(EnumType.STRING)
    private AutoRenewStatus autoRenew;
    @Column(name = "end_time")
    private LocalDateTime endTime;
    @Column(name = "email")
    private String email;
    @ManyToOne
    @JoinColumn(name = "bank_card_id")
    private UserBankCard userBankCard;
    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;
}
