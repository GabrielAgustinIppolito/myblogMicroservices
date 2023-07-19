package it.cgmconsultig.msrating.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RatingId implements Serializable {

    private long postId;
    private long userId;
    @Column(updatable = false, nullable = false)
    private LocalDate votedAt = LocalDate.now();

    public RatingId(long postId, long userId) {
        this.postId = postId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RatingId ratingId = (RatingId) o;
        return postId == ratingId.postId && userId == ratingId.userId && Objects.equals(votedAt, ratingId.votedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, userId, votedAt);
    }
}
