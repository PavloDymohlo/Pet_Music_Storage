package ua.dymohlo.PetMusicStorage.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
@Table(name = "bank_transaction_data")
public class BankTransactionData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "bank_name")
    private String bankName;
    @Column(name = "bank_url_transaction")
    private String bankUrlTransaction;
}
