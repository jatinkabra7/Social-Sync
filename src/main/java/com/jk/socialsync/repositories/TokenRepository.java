package com.jk.socialsync.repositories;

import com.jk.socialsync.entities.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<TokenEntity, Long> {

    @Query("""
    select t 
    from TokenEntity t
    join UserEntity u on u.userId = t.user.userId
    where t.user.userId = :userId
    and t.isLoggedOut = false 
    """)
    List<TokenEntity> findAllTokensByUser(Long userId);
    Optional<TokenEntity> findByTokenHash(String tokenHash);
}
