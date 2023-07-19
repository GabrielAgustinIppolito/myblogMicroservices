package it.cgmconsulting.mscategory.service;

import it.cgmconsulting.mscategory.entity.Category;
import it.cgmconsulting.mscategory.entity.CategoryPosts;
import it.cgmconsulting.mscategory.exception.ResourceNotFoundException;
import it.cgmconsulting.mscategory.payload.request.CategoryRequest;
import it.cgmconsulting.mscategory.payload.request.PostCategoryAssociationRequest;
import it.cgmconsulting.mscategory.payload.response.CategoriesResponse;
import it.cgmconsulting.mscategory.repository.CategoryPostsRepository;
import it.cgmconsulting.mscategory.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryPostsRepository categoryPostsRepository;


    public ResponseEntity<?> createCategory(String categoryName) {
        categoryName = categoryName.toUpperCase().trim();
        if(categoryRepository.existsByCategoryName(categoryName))
            return new ResponseEntity<>("Category alredy present", HttpStatus.BAD_REQUEST);
        categoryRepository.save(new Category(categoryName));
        return new ResponseEntity<>("New category has been added", HttpStatus.OK);
    }

    // Aggiornamento cateoria relativamente al nome ed alla visibilità
    @Transactional
    public ResponseEntity<?> updateCategory(CategoryRequest request){
        String newCategoryName = request.getNewCategoryName().toUpperCase().trim();
        if(categoryRepository.existsByCategoryNameAndIdNot(newCategoryName, request.getCategoryId()) )
            return new ResponseEntity<>("Category alredy present", HttpStatus.BAD_REQUEST);
        Category cat = findCategory(request.getCategoryId());
        cat.setCategoryName(newCategoryName);
        cat.setVisible(request.isNewVisibility());
        return new ResponseEntity<>("Category updated succesfully", HttpStatus.OK);
    }

    protected Category findCategory(long cagetforyId){
        Category c = categoryRepository.findById(cagetforyId).orElseThrow(
                () -> new ResourceNotFoundException("Category", "id", cagetforyId)
        );
        return c;
    }

    public ResponseEntity<?> getAllCategoriesVisibleCategories() {
        return new ResponseEntity<>(categoryRepository.getAllVisibleCategories(), HttpStatus.OK);
    }

    public ResponseEntity<?> getAllCategories() {
        return new ResponseEntity<>( categoryRepository.getAllCategories(), HttpStatus.OK);
    }

    public ResponseEntity<?> postCategoryAssociation(PostCategoryAssociationRequest request) {
//        List<Category> categories2 = categoryRepository.findAllByIdInAndVisibleTrue(request.getCategoriesId());
//        return new ResponseEntity<>(categories + " " + categories2, HttpStatus.OK);
        List<Category> categories = categoryRepository.getCategoriesByIds(request.getCategoriesId());
        long postId = request.getPostId();

        categoryPostsRepository.deleteCategoryPost(postId);

        for (Category cat : categories) {
            categoryPostsRepository.insertCategoryPost(postId, cat.getId());
        }

        return new ResponseEntity<>(categories, HttpStatus.OK);

    }

    public Set<String> getCategoriesByPost(long postId) {
        return categoryPostsRepository.getCategoriesByPost(postId);
    }

    public Set<Long> findPostsByCategory(String categoryName) {
        return categoryPostsRepository.getPostsByCategory(categoryName);
    }

    public List<CategoriesResponse> findAllCategoriesByPosts(){
        List<CategoryPosts> list = categoryPostsRepository.findAllCategoriesByPosts();
        List<CategoriesResponse> cr = new ArrayList<>();
        Set<String> categories = new HashSet<>();

        if(list.isEmpty())
            return new ArrayList<>();

        Set<Long> postIds = list.stream().map(p -> p.getCategoryId().getPostId()).collect(Collectors.toSet());
        for(long postId : postIds){
            for(CategoryPosts c : list){
                if(c.getCategoryId().getPostId() == postId)
                    categories.add(c.getCategoryId().getCategoryId().getCategoryName());
            }
            cr.add(new CategoriesResponse(postId, categories));
            categories = new HashSet<>();
        }
        return cr;

    }

//    public List<CategoriesResponse> findAllCategoriesByPosts(){
//        List<CategoryPosts> list = categoryPostsRepository.findAllCategoriesByPosts();
//        List<CategoriesResponse> cr = new ArrayList<>();
//
//        if(list.isEmpty())
//            return new ArrayList<>();
//
//        cr.add(new CategoriesResponse(list.get(0).getCategoryId().getPostId(),
//                new HashSet<>(Arrays.asList(list.get(0).getCategoryId().getCategoryId().getCategoryName()))));
//        for (int i = 1; i < list.size(); i++){
//            if(list.get(i).getCategoryId().getPostId() == cr.get(cr.size()-1).getPostId()){
//                cr.get(cr.size()-1).addCategory(
//                        list.get(i).getCategoryId().getCategoryId().getCategoryName()
//                );
//            } else {
//                cr.add(new CategoriesResponse(list.get(i).getCategoryId().getPostId(),
//                        new HashSet<>(Arrays.asList(list.get(i).getCategoryId().getCategoryId().getCategoryName()))));
//            }
//        }
//        return cr;
//
//    }

//    public List<CategoriesResponse> findAllCategoriesByPosts(){
//        List<CategoryPosts> list = categoryPostsRepository.findAllCategoriesByPosts();
//        List<CategoriesResponse> cr = new ArrayList<>();
//
//        if(list.isEmpty())
//            return new ArrayList<>();
//
//        Map<Long, List<CategoryPosts>> l2
//                = list.stream()
//                .collect(Collectors.groupingBy(c -> c.getCategoryId().getPostId()));
//
//        l2.keySet().forEach(l -> cr.add(new CategoriesResponse(l,
//                l2.get(l)
//                .stream().map(cat -> cat.getCategoryId().getCategoryId().getCategoryName()).collect(Collectors.toSet())
//                )
//        ));
//
//        return cr;
//
//    }

//    public List<CategoriesResponse> findAllCategoriesByPosts(){ NON FUNZIONA
////        Map<Set<Long>, Set<String>> mappa = new;
//        List<Object[]> ol = categoryPostsRepository.findAllCategoriesByPosts2();
//        List<CategoriesResponse> cr = new ArrayList<>();
//
//        if(ol.isEmpty())
//            return new ArrayList<>();
//
//        // Place results in map
//        for (Object[] o : ol) {
//            cr.add(new CategoriesResponse((long)o[0], (Set<String>)o[1]));
////            results.put((String)o[0], (String)o[1]);
//        }
//
//        return cr;
//
//    }



}
