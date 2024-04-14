package ua.dymohlo.PetMusicStorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.dymohlo.PetMusicStorage.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    User findByPhoneNumber(long phoneNumber);
}
