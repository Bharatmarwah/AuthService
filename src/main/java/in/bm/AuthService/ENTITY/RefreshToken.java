package in.bm.AuthService.ENTITY;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private AuthUser user;

    @Column(nullable = false)
    private boolean isUsed;

    @Column(unique = true,nullable = false)
    private String refreshTokenHash;

    @Column(nullable = false)
    private boolean isRevoked;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiryAt;



}
