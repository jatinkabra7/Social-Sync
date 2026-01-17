package com.jk.socialsync.services;

import com.jk.socialsync.dtos.requests.PostAddRequestDto;
import com.jk.socialsync.dtos.responses.PostAddResponseDto;
import com.jk.socialsync.dtos.responses.common.UserInfoDto;
import com.jk.socialsync.entities.PostEntity;
import com.jk.socialsync.entities.UserEntity;
import com.jk.socialsync.repositories.PostRepository;
import com.jk.socialsync.repositories.UserRepository;
import com.jk.socialsync.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;

    public PostAddResponseDto createPost(PostAddRequestDto postAddRequest, HttpServletRequest request) {
        // first extract the access token
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Unauthorized");
        }

        String accessToken = authHeader.substring(7);

        String username = jwtUtil.getUsernameFromToken(accessToken);

        Optional<UserEntity> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }

        UserEntity user = optionalUser.get();

        PostEntity post = PostEntity.builder()
                .mediaUrl(postAddRequest.getMediaUrl())
                .mediaType(postAddRequest.getMediaType())
                .caption(postAddRequest.getCaption())
                .user(user)
                .build();

        PostEntity savedPost = postRepository.save(post);

        UserInfoDto author = UserInfoDto.builder()
                .id(savedPost.getUser().getId())
                .username(savedPost.getUser().getUsername())
                .profilePictureUrl(savedPost.getUser().getProfilePictureUrl())
                .build();

        return PostAddResponseDto.builder()
                .id(savedPost.getId())
                .mediaUrl(savedPost.getMediaUrl())
                .mediaType(savedPost.getMediaType())
                .caption(savedPost.getCaption())
                .uploadedAt(savedPost.getUploadedAt())
                .author(author)
                .build();
    }

    public List<PostAddResponseDto> getAllByUserId(Long userId) {
        List<PostEntity> posts = postRepository.findAllByUser_Id(userId);
        return posts.stream().map((element) -> modelMapper.map(element, PostAddResponseDto.class)).collect(Collectors.toList());
    }
}
