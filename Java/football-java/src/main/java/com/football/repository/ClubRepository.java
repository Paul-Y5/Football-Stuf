package com.football.repository;

import com.football.model.Club;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Club entity operations.
 */
@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

    /**
     * Find club by name (case-insensitive).
     */
    Optional<Club> findByNameIgnoreCase(String name);

    /**
     * Find all clubs from a specific country.
     */
    List<Club> findByCountryOrderByRankingAsc(String country);

    /**
     * Find clubs by country with pagination.
     */
    Page<Club> findByCountry(String country, Pageable pageable);

    /**
     * Find top N clubs globally.
     */
    List<Club> findTop100ByOrderByRankingAsc();

    /**
     * Search clubs by name or country (case-insensitive).
     */
    @Query("SELECT c FROM Club c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.country) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Club> searchByNameOrCountry(@Param("search") String search);

    /**
     * Find clubs with points in range.
     */
    List<Club> findByPointsBetweenOrderByPointsDesc(int minPoints, int maxPoints);

    /**
     * Count clubs by country.
     */
    long countByCountry(String country);

    /**
     * Get distinct countries.
     */
    @Query("SELECT DISTINCT c.country FROM Club c ORDER BY c.country")
    List<String> findDistinctCountries();

    /**
     * Find clubs that improved (trend UP).
     */
    @Query("SELECT c FROM Club c WHERE c.trend = 'UP' ORDER BY c.ranking ASC")
    List<Club> findImprovedClubs();

    /**
     * Find clubs that declined (trend DOWN).
     */
    @Query("SELECT c FROM Club c WHERE c.trend = 'DOWN' ORDER BY c.ranking ASC")
    List<Club> findDeclinedClubs();

    /**
     * Check if club exists by name.
     */
    boolean existsByNameIgnoreCase(String name);
}
