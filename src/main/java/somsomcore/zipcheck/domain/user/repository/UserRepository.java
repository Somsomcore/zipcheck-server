package somsomcore.zipcheck.domain.user.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import somsomcore.zipcheck.domain.user.entity.User;
import somsomcore.zipcheck.domain.user.entity.enums.OauthType;
import somsomcore.zipcheck.domain.user.entity.enums.Role;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndOauthType(String email, OauthType oauthType);
    List<User> findAllByRole(Role role);
}
