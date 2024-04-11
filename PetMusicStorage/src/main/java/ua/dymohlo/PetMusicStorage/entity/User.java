package ua.dymohlo.PetMusicStorage.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString(exclude = {"userBankCard", "subscription"})
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;
    @Column(name = "phone_number")
    private long phoneNumber;
    @Column(name = "password")
    private String password;
    @Column(name = "auto_renew")
    private String autoRenew;
    @Column(name = "end_time")
    private LocalDateTime endTime;
    @Column(name = "email")
    private String email;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "bank_card_number")
    private UserBankCard userBankCard;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "subscriptions_id")
    private Subscription subscription;
}
