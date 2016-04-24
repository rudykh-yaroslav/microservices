package com.rudykh.rirservice;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by Yaroslav Rudykh on 23.04.2016.
 */
public interface ISPRepository extends JpaRepository<ISP, Long> {

    Collection<ISP> findByCompanyName(String companyName, Sort sort);

    Collection<ISP> findByWebsite(String website, Sort sort);

    Optional<ISP> findByEmail(String email);

    @Query("SELECT t FROM ISP t WHERE t.companyName LIKE %:searchTerm% ORDER BY LOWER(t.companyName), t.id")
    public Collection<ISP> searchByCompanyName(@Param("searchTerm") String searchTerm);

    @Query("SELECT t FROM ISP t WHERE t.website LIKE %:searchTerm% ORDER BY LOWER(t.companyName), t.id")
    public Collection<ISP> searchByWebsite(@Param("searchTerm") String searchTerm);

    @Query("SELECT t FROM ISP t WHERE t.email LIKE %:searchTerm% ORDER BY LOWER(t.companyName), t.id")
    public Collection<ISP> searchByEmail(@Param("searchTerm") String searchTerm);
}
