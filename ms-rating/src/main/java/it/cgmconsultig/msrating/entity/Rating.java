package it.cgmconsultig.msrating.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class Rating {

    // un utente puo votare una volta sola al giorno
    // quindi in un anno un post può ottenere al max 365 voti per utente

    @EmbeddedId
    @EqualsAndHashCode.Include
    private RatingId ratingId;

    private byte rate;
}