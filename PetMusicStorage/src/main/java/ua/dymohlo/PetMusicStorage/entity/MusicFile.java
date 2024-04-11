package ua.dymohlo.PetMusicStorage.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString(exclude = "subscription")
@Table(name = "music_files")
public class MusicFile {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;
    @Column(name = "music_file_name")
    private String musicFileName;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "subscriptions_id")
    private Subscription subscription;
}
