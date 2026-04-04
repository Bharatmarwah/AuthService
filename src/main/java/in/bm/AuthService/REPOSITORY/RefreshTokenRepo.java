package in.bm.AuthService.REPOSITORY;

import in.bm.AuthService.ENTITY.AuthUser;
import in.bm.AuthService.ENTITY.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByRefreshTokenHash(String hash);

    List<RefreshToken> findAllByUser(AuthUser user);

    @Modifying
    @Query("UPDATE RefreshToken t SET t.isRevoked = true WHERE t.user = :user")
    void revokeAll(@Param("user") AuthUser user);
}
