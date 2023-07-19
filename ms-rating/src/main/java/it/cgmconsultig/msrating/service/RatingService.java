package it.cgmconsultig.msrating.service;

import it.cgmconsultig.msrating.entity.Rating;
import it.cgmconsultig.msrating.entity.RatingId;
import it.cgmconsultig.msrating.payload.response.AvgPosts;
import it.cgmconsultig.msrating.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository repo;

    public ResponseEntity<?> addRate(long postId, long userId, byte rate) {
            RatingId ratingId = new RatingId(postId,userId);
            if(repo.existsById(ratingId))
                return new ResponseEntity<>("User already voted today", HttpStatus.FORBIDDEN);

            Rating rating = new Rating(ratingId, rate);
            repo.save(rating);

            return new ResponseEntity<>("Rating added successfully", HttpStatus.OK);

    }

    public double getAvgByPost(long postId) {
        double avg = repo.getAvgByPost(postId);
        return avg;
    }

    public List<AvgPosts> getAvgPosts(){
        return repo.getAvgPosts();
    }
}
