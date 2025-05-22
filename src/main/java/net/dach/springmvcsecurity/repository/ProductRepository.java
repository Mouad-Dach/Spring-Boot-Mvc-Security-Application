package net.dach.springmvcsecurity.repository;

import net.dach.springmvcsecurity.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContains(String keyword);

    Page<Product> findAll(Pageable pageable);

    Page<Product> findByNameContains(String keyword, Pageable pageable);

    @Query("SELECT DISTINCT p.name FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY p.name ASC")
    List<String> findSuggestions(@Param("query") String query);

    @Query("SELECT DISTINCT p.name FROM Product p ORDER BY p.name ASC")
    List<String> findAllProductNames();

    @Query(value = "SELECT name FROM (SELECT DISTINCT p.name, RANDOM() as rand FROM Product p ORDER BY rand LIMIT :limit)", nativeQuery = true)
    List<String> findRandomProductNames(@Param("limit") int limit);
}
