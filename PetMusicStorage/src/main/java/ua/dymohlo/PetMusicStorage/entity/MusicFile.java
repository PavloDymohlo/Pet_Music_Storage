package ua.dymohlo.PetMusicStorage.entity;


import lombok.*;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString(exclude = "subscription")
@Table(name = "music_files")
public class MusicFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "music_file_name")
    private String musicFileName;
    @Column(name = "file_path")
    private String filePath;
    @ManyToOne
    @JoinColumn(name = "subscriptions_id")
    private Subscription subscription;
}