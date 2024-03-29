package it.cgmconsulting.mspost.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter @Setter @NoArgsConstructor @ToString
public class Post{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 100, unique = true)
    private String title;
    @Column(nullable = false)
    private String overview;
    @Column(nullable = false, length = 20000)
//    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private long authorId;

    // Se null -> il caporedattore deve ancora approvare il post
    // data futura -> il caporedattore ha approvato il post ma questo verrà pubblicato in futuro
    // data passata -> post pubblicato e visibile
    private LocalDateTime publishedAt;

    @CreationTimestamp
    //in automatico si definisce un unica volta quando viene caricata per la prima volta sul db l'entità
    @Column(updatable = false)  // mi assicuro che non venga mai modificata
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Post(String title, String overview, String content, long authorId) {
        this.title = title;
        this.overview = overview;
        this.content = content;
        this.authorId = authorId;
    }

    public Post(long id) {
       this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return getId() == post.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
