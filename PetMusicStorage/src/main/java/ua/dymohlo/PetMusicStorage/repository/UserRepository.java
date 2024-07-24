package ua.dymohlo.PetMusicStorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.dymohlo.PetMusicStorage.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByPhoneNumber(long phoneNumber);

    boolean existsByPhoneNumber(long phoneNumber);

    boolean existsByEmailIgnoreCase(String email);

    User findByEmailIgnoreCase(String email);

    User findById(long userId);
    void deleteById(long id);
}
