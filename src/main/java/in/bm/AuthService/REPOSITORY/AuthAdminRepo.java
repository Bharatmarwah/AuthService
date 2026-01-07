package in.bm.AuthService.REPOSITORY;

import in.bm.AuthService.ENTITY.AuthAdmin;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthAdminRepo extends JpaRepository<AuthAdmin,UUID> {
    Optional<AuthAdmin> findByUsername(@NotBlank String username);

}
