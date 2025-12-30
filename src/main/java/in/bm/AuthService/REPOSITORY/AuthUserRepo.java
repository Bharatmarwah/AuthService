package in.bm.AuthService.REPOSITORY;

import in.bm.AuthService.ENTITY.AuthUser;
import in.bm.AuthService.ENTITY.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthUserRepo extends JpaRepository<AuthUser, UUID> {
    Optional<AuthUser> findByPhoneNumber(String phoneNumber);

    Optional<AuthUser> findByProviderAndEmail(Provider provider, String email);
}
