package it.cgmconsulting.mscategory.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class PostCategoryAssociationRequest {
    private long postId;
    private Set<Long> categoriesId;
}
