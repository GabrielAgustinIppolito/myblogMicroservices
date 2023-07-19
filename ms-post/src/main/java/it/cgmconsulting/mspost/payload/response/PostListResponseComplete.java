package it.cgmconsulting.mspost.payload.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter @Setter @NoArgsConstructor
public class PostListResponseComplete extends PostListResponse {
    private Set<String> categories;
    private String author;
    private long authorId;

    public PostListResponseComplete(long id, String title, String overview, Set<String> categories, long authorId) {
        super(id, title, overview);
        this.categories = categories;
        this.authorId = authorId;
    }

    public PostListResponseComplete(long id, String title, String overview, long authorId) {
        super(id, title, overview);
        this.authorId = authorId;
    }
}
