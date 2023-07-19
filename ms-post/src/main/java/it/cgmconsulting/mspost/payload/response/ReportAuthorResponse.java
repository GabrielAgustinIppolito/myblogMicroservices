package it.cgmconsulting.mspost.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReportAuthorResponse {

    // From ms-post
    private long userId;
    private long totPosts;
    // from ms-auth
    private String username;
    // from ms-rating
    private double totAVG;

    public ReportAuthorResponse(long userId, long totPosts) {
        this.userId = userId;
        this.totPosts = totPosts;
    }
}
