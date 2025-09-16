package somsomcore.zipcheck.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import somsomcore.zipcheck.domain.user.entity.User;
import somsomcore.zipcheck.domain.user.entity.enums.OauthType;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndOauthType(String email, OauthType oauthType);
}