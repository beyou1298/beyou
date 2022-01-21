package com.beyou.category;

import java.util.List;

import com.beyou.common.entity.Category;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<Category, Integer> {
    
    @Query("SELECT c FROM Category c WHERE c.enabled = true ORDER BY c.name ASC")
    public List<Category> findAllEnabled();

    @Query("SELECT c FROM Category c WHERE c.enabled = true AND c.alias = ?1")
    public Category findByAliasEnabled(String alias);;
}
