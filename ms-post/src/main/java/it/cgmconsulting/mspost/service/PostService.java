package it.cgmconsulting.mspost.service;

import it.cgmconsulting.mspost.entity.Post;
import it.cgmconsulting.mspost.exception.ResourceNotFoundException;
import it.cgmconsulting.mspost.payload.request.PostRequest;
import it.cgmconsulting.mspost.payload.response.UserResponse;
import it.cgmconsulting.mspost.payload.response.PostResponse;
import it.cgmconsulting.mspost.repository.PostRepository;
import it.cgmconsulting.mspost.utils.EndPoints;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    final private PostRepository repo;

    public ResponseEntity<?> createPost(PostRequest request, long authorId) {
        if(repo.existsByTitle(request.getTitle()))
            return new ResponseEntity<>("Title already in use", HttpStatus.BAD_REQUEST);
        Post p = fromRequestToEntity(request, authorId);
        return new ResponseEntity<>(repo.save(p), HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity<?> updatePost(PostRequest request, long authorId, long postId){
        // verificare titolo,
        if(repo.existsByTitleAndIdNot(request.getTitle(), postId))
            return new ResponseEntity<>("Title already in use", HttpStatus.BAD_REQUEST);
//        Optional<Post> p = repo.findById(postId);
//        if(!p.isPresent())
//            return new ResponseEntity<>("Post not found", HttpStatus.NOT_FOUND);
        Post p = findPost(postId);
        p.setTitle(request.getTitle());
        p.setOverview(request.getOverview());
        p.setContent(request.getContent());
        p.setAuthorId(authorId);
        p.setPublishedAt(null);
        // gestire i valori aggiornati
      return new ResponseEntity<>(p, HttpStatus.OK);
    }


    protected Post findPost(long postId){
        Post p = repo.findById(postId).orElseThrow(
                () -> new ResourceNotFoundException("Post", "id", postId)
        );
        return p;
    }


    private Post fromRequestToEntity(PostRequest request, long authorId){
        return new Post(request.getTitle(), request.getOverview(), request.getContent(), authorId);
    }

    @Transactional
    public ResponseEntity<?> publishPost(long postId, LocalDateTime publishedAt) {
        if(publishedAt != null)
            if(publishedAt.isBefore(LocalDateTime.now()))
                return new ResponseEntity<>("You cannot publish the post in the past", HttpStatus.BAD_REQUEST);

        if(publishedAt == null)
            publishedAt = LocalDateTime.now();
        Post p = findPost(postId);
        p.setPublishedAt(publishedAt);
        return new ResponseEntity<>("Post " + p.getTitle() + " will be published at " + publishedAt, HttpStatus.OK);
    }

    public ResponseEntity<?> getPost(long postId) {
        Optional<PostResponse> p = repo.getPost(postId, LocalDateTime.now());
        if(!p.isPresent())
            return new ResponseEntity<>("Post not found", HttpStatus.NOT_FOUND);
        String author = getAuthor(p.get().getAuthorId());
        p.get().setAuthor(author);
        return new ResponseEntity<>(p, HttpStatus.OK);
    }

    public String getAuthor(long authorId){
        RestTemplate restTemplate = new RestTemplate();
        String resourceUrl = EndPoints.GATEWAY + EndPoints.MS_AUTH +"/v0/" + authorId;
        try {
            return restTemplate.getForObject(resourceUrl, String.class);
        } catch (RestClientException e){
            log.error(e.getMessage());
            return null;
        }
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("v-int", "ok");
//        HttpEntity<String> httpEntity = new HttpEntity<>("some body", headers);
//        restTemplate.exchange(resourceUrl, HttpMethod.GET, httpEntity, String.class);
//        return username;
    }

    public ResponseEntity<?> getAllPublishedPost() {
        List<UserResponse> users =
                Arrays.stream(getUsersByRole("ROLE_WRITER")).toList();
        List<PostResponse> publishedPosts = repo.getAllPublishedPost(LocalDateTime.now());
        System.out.println(users);
        publishedPosts.forEach(p -> {
//            users.forEach(u -> {
//                if (u.getId() == p.getAuthorId()) p.setAuthor(u.getUsername());
//            }); //qua non si interrompe appena trovato, dovrei usare un doppio for
//
//            for (UserResponse u : users) {
//                if (u.getId() == p.getAuthorId()){
//                    p.setAuthor(u.getUsername());
//                    break;
//                }
//            }

            users.stream()
                    .filter(u -> u.getId() == p.getAuthorId())
                    .findFirst()
                    .ifPresent(u -> p.setAuthor(u.getUsername()));
        });  //così dovrebbe essere un po meglio
        return new ResponseEntity<>(publishedPosts, HttpStatus.OK);
    }

    public UserResponse[] getUsersByRole(String role){
        RestTemplate restTemplate = new RestTemplate();
        String resourceUrl = EndPoints.GATEWAY + EndPoints.MS_AUTH +"/v0/get-users-by-role/" + role;
        try {
            ResponseEntity<UserResponse[]> response =
                    restTemplate.getForEntity(
                            resourceUrl,
                            UserResponse[].class);
            return response.getBody();
        } catch (RestClientException e){
            log.error(e.getMessage());
            return null;
        }
    }

}




