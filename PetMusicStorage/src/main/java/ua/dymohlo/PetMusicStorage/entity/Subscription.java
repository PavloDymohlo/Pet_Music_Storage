package ua.dymohlo.PetMusicStorage.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString(exclude = {"musicFiles", "users"})
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "subscription_name")
    private String subscriptionName;
    @Column(name = "subscription_price")
    private BigDecimal subscriptionPrice;
    @Column(name = "subscription_duration_time")
    private int subscriptionDurationTime;
    @JsonIgnore
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    private List<MusicFile> musicFiles;
    @JsonIgnore
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    private List<User> users = new ArrayList<>();
}
