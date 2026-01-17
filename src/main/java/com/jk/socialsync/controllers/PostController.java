package com.jk.socialsync.controllers;

import com.jk.socialsync.dtos.requests.PostAddRequestDto;
import com.jk.socialsync.dtos.responses.PostAddResponseDto;
import com.jk.socialsync.entities.PostEntity;
import com.jk.socialsync.services.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<PostAddResponseDto> createPost(
            @Valid @RequestBody PostAddRequestDto postAddRequest,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(postService.createPost(postAddRequest,request));
    }

    @GetMapping("/getAllByUserId/{userId}")
    public ResponseEntity<List<PostAddResponseDto>> getPostByUsername(@PathVariable Long userId) {
        return ResponseEntity.ok(postService.getAllByUserId(userId));
    }
}
