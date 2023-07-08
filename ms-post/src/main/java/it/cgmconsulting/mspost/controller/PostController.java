package it.cgmconsulting.mspost.controller;

import it.cgmconsulting.mspost.payload.request.PostRequest;
import it.cgmconsulting.mspost.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping("v2")
    public ResponseEntity<?> createPost(@RequestBody @Valid PostRequest request,
                                        @RequestHeader("userId") long authorId){
        return postService.createPost(request, authorId);
    }
    @PostMapping("v2/{postId}")
    public ResponseEntity<?> createPost(@RequestBody @Valid PostRequest request,
                                        @RequestHeader("userId") long authorId,
                                        @PathVariable("postId") long postId){
        return postService.updatePost(request, authorId, postId);
    }
    @PutMapping("v1/publish/{postId}")
    public ResponseEntity<?> createPost(@PathVariable("postId") long postId,
                                        @RequestParam(required = false) LocalDateTime publishedAt){
        return postService.publishPost(postId, publishedAt);
    }

    @GetMapping("v0/{postId}")
    public ResponseEntity<?> getPost(@PathVariable long postId){
        return postService.getPost(postId);
    }

    @GetMapping("v0/posts")
    public ResponseEntity<?> getPosts(){
        return postService.getAllPublishedPost();
    }



}
