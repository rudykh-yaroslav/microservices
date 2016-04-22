package com.rudykh.rirservice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by Yaroslav Rudykh on 23.04.2016.
 */
public interface ISPRepository extends JpaRepository<ISP, Long> {

    Collection<ISP> findByCompanyName(String companyName);

    Collection<ISP> findByWebsite(String website);

    Optional<ISP> findByEmail(String email);
}
