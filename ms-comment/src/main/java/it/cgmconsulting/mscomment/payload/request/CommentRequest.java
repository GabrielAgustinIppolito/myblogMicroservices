package it.cgmconsulting.mscomment.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class CommentRequest {

    @NotBlank
    @Size(max = 250)
    private String comment;
    @Min(1)
    private long postId;

}
