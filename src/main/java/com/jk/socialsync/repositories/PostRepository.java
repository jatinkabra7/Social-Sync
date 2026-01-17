package com.jk.socialsync.repositories;

import com.jk.socialsync.entities.PostEntity;
import com.jk.socialsync.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    List<PostEntity> findAllByUser_Id(Long userId);

    Long user(UserEntity user);
}
