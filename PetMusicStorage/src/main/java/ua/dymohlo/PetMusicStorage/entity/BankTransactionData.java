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
@Table(name = "bank_transaction_data")
public class BankTransactionData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "bank_name", nullable = false)
    private String bankName;
    @Column(name = "bank_url_transaction", unique = true, nullable = false)
    private String bankUrlTransaction;
    @OneToOne(mappedBy = "bankTransactionData")
    @JsonIgnore
    private PaymentRecipientData paymentRecipientData;
}

