package ua.dymohlo.MockBankSystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "clients")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;
    @Column(name = "client_full_name")
    private String clientFullName;
    @Column(name = "card_number")
    private long cardNumber;
    @Column(name = "card_expiration_date")
    private String cardExpirationDate;
    @Column(name = "cvv")
    private short cvv;
    @Column(name = "balance")
    private int balance;
}
