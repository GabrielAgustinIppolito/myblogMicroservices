package it.cgmconsulting.mscomment.controller;

import it.cgmconsulting.mscomment.payload.request.CommentRequest;
import it.cgmconsulting.mscomment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
public class CommentController {
    private final CommentService commentService;

    @PostMapping("v3")
    public ResponseEntity<?> publicComment(@RequestBody @Valid CommentRequest request, @RequestHeader("userId") long authorId){
        return commentService.createComment(request, authorId);
    }

   @GetMapping("v0/{postId}")
    public ResponseEntity<?> getComments(@PathVariable long postId){
        return commentService.getComments(postId);
   }

}
