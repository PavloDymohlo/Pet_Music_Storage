package ua.dymohlo.PetMusicStorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.dymohlo.PetMusicStorage.entity.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByPhoneNumber(long phoneNumber);

    boolean existsByPhoneNumber(long phoneNumber);

    boolean existsByEmail(String email);

    User findByEmail(String email);

    User findById(long userId);
}
