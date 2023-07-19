package it.cgmconsulting.mspost.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import it.cgmconsulting.mspost.entity.Post;
import it.cgmconsulting.mspost.exception.ResourceNotFoundException;
import it.cgmconsulting.mspost.payload.request.PostRequest;
import it.cgmconsulting.mspost.payload.response.*;
import it.cgmconsulting.mspost.repository.PostRepository;
import it.cgmconsulting.mspost.utils.EndPoints;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

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
        Optional<PostResponse> p = getPostById(postId);
        if(!p.isPresent())
            return new ResponseEntity<>("Post not found", HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(p.get(), HttpStatus.OK);
    }

    public Optional<PostResponse> getPostById(long postId) {
        Optional<PostResponse> p = repo.getPost(postId, LocalDateTime.now());
        if(p.isPresent()) {
            String author = getAuthor(p.get().getAuthorId());
            p.get().setAuthor(author);
            Set<String> categories = getCategories(postId);
            p.get().setCategories(categories);
            double avg = getRatingAvg(postId);
            p.get().setAvgRating(avg);
        }
        return p;
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

    @CircuitBreaker(name="a-tentativi", fallbackMethod = "fallbackMethodATentativi")
    public ResponseEntity<?> getAllPublishedPost() {
//        List<UserResponse> users =
//                Arrays.stream(getUsersByRole("ROLE_WRITER")).toList();
//        List<?> users = getUsersByRole("ROLE_WRITER");
        List<PostListResponseComplete> publishedPosts = repo.getAllPublishedPost(LocalDateTime.now());
//        System.out.println(users);
//        System.out.println(users.getClass());
//        System.out.println(users.get(0).getClass());
//        List<LinkedHashMap> v = new ArrayList<>();
//        for (int i = 0; i < users.size(); i++){
//            v.add((LinkedHashMap) users.get(i));
//        }
//        var v = (LinkedHashMap) users.get(0);
        List<UserResponse> users = getUsersByRole("ROLE_WRITER");
        List<CategoriesResponse> categories = getAllCategoriesByPost();
        publishedPosts.forEach(p -> {
//            users.forEach(u -> {
//                if (u.getId() == p.getAuthorId()) p.setAuthor(u.getUsername());
//            }); //qua non si interrompe appena trovato, dovrei usare un doppio for
//
//            for (LinkedHashMap u : v) {
//                if (Long.valueOf(u.get("id").toString()) == p.getAuthorId()){
//                    p.setAuthor(u.get("username").toString());
//                     break;
//                }
//            }
            users.stream()
                    .filter(u -> u.getId() == p.getAuthorId())
                    .findFirst()
                    .ifPresent(u -> p.setAuthor(u.getUsername()));
            categories.stream()
                    .filter(c -> c.getPostId() == p.getId())
                    .findFirst()
                    .ifPresent(c -> p.setCategories(c.getCategories()));
        });  //così dovrebbe essere un po meglio

        return new ResponseEntity<>(publishedPosts, HttpStatus.OK);
    }

    @CircuitBreaker(name="a-tentativi", fallbackMethod = "fallbackMethodATentativi")
    public ResponseEntity<?> findPostsByCategory(String categoryName) {
        // Ordinati per data di publicazione DESC
        Set<Long> postIds = getPostIdsFromCategoryName(categoryName);
        Set<PostListResponse> plr = repo.getPostsByCategory(postIds, LocalDateTime.now());
        return ResponseEntity.ok(plr);
    }

    public List<UserResponse> getUsersByRole(String role){
        RestTemplate restTemplate = new RestTemplate();
        String resourceUrl = EndPoints.GATEWAY + EndPoints.MS_AUTH +"/v0/get-users-by-role/" + role;
        /*
        try {
            ResponseEntity<UserResponse[]> response =
                    restTemplate.getForEntity(
                            resourceUrl,
                            UserResponse[].class);
            return response.getBody();
        } catch (RestClientException e){
            log.error(e.getMessage());
            return null;
        }*/
        try {
            ParameterizedTypeReference<List<UserResponse>> type = new ParameterizedTypeReference<List<UserResponse>>() {};
            ResponseEntity<List<UserResponse>> listResponseEntity =
                    restTemplate.exchange(resourceUrl, HttpMethod.GET, null, type);
            return listResponseEntity.getBody();
        } catch (RestClientException e){
            log.error(e.getMessage());
            return null;
        }
    }
//    public List<?> getUsersByRole(String role){
//        RestTemplate restTemplate = new RestTemplate();
//        String resourceUrl = EndPoints.GATEWAY+EndPoints.MS_AUTH+"/v0/get-users-by-role/"+role;
//        try {
//            return restTemplate.getForObject(resourceUrl, List.class);
//        } catch (RestClientException e){
//            log.error(e.getMessage());
//            return null;
//        }

//    }

    public ResponseEntity<?> fallbackMethodATentativi(Exception e){
       return new ResponseEntity<>("Something went wrong with authors", HttpStatus.SERVICE_UNAVAILABLE);
    }

    public Set<String> getCategories(long postId){
        RestTemplate restTemplate = new RestTemplate();
        String resourceUrl = EndPoints.GATEWAY + EndPoints.MS_CATEGORY +"/v0/get-categories-by-post/" + postId;
        try{
            return restTemplate.getForObject(resourceUrl, HashSet.class);
        } catch (RestClientException e){
            log.error(e.getMessage());
            return null;
        }
    }

    public Set<Long> getPostIdsFromCategoryName(String categoryName){
        String cn = categoryName.toUpperCase().trim();
        RestTemplate restTemplate = new RestTemplate();
        String resourceUrl = EndPoints.GATEWAY + EndPoints.MS_CATEGORY +"/v0/find-post-by-category/" + cn;
        try{
            ParameterizedTypeReference<Set<Long>> type = new ParameterizedTypeReference<Set<Long>>() {};
            ResponseEntity<Set<Long>> listResponseEntity =
                    restTemplate.exchange(resourceUrl, HttpMethod.GET, null, type);
            return listResponseEntity.getBody();
//            return restTemplate.getForObject(resourceUrl, HashSet.class);
        } catch (RestClientException e){
            log.error(e.getMessage());
            return null;
        }
    }

    private double getRatingAvg(long postId) {
        RestTemplate restTemplate = new RestTemplate();
        String resourceUrl = EndPoints.GATEWAY + EndPoints.MS_RATING +"/v0/get-avg-by-post/" + postId;
        try{
            Double avg = restTemplate.getForObject(resourceUrl, Double.class);
            return avg != null ? avg : 0;
        } catch (RestClientException e){
            log.error(e.getMessage());
            return 0;
        }
    }

    private List<CategoriesResponse> getAllCategoriesByPost() {
        RestTemplate restTemplate = new RestTemplate();
        String resourceUrl = EndPoints.GATEWAY + EndPoints.MS_CATEGORY +"/v0/find-all-categories-by-posts";
        try{
            ParameterizedTypeReference<List<CategoriesResponse>> type = new ParameterizedTypeReference<>() {};
            ResponseEntity<List<CategoriesResponse>> listResponseEntity =
                    restTemplate.exchange(resourceUrl, HttpMethod.GET, null, type);
            return listResponseEntity.getBody();
        } catch (RestClientException e){
            log.error(e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<UserPostResponse> userPost(){
        return repo.countPostsByAuthor();
    }

    public List<AvgPosts> getAvgPosts(){
        RestTemplate restTemplate = new RestTemplate();
        String resourceUrl = EndPoints.GATEWAY + EndPoints.MS_RATING +"/v0/get-all-avg";
        try{
            ParameterizedTypeReference<List<AvgPosts>> type = new ParameterizedTypeReference<>() {};
            ResponseEntity<List<AvgPosts>> listResponseEntity =
                    restTemplate.exchange(resourceUrl, HttpMethod.GET, null, type);
            return listResponseEntity.getBody();
        } catch (RestClientException e){
            log.error(e.getMessage());
            return new ArrayList<>();
        }
    }


/*
    public static String getClientIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
            log.info("------- Proxy-Client-IP: "+ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
            log.info("------- WL-Proxy-Client-IP: "+ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
            log.info("------- HTTP_CLIENT_IP: "+ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            log.info("------- HTTP_X_FORWARDED_FOR: "+ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            log.info("------- unknown: "+ip);
        }
        return ip;
    }
    */
}
