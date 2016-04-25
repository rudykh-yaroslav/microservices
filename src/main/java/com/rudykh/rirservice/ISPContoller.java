package com.rudykh.rirservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Created by Yaroslav Rudykh on 23.04.2016.
 */
@RestController
@RequestMapping("/isp")
public class ISPContoller {

    private static final Sort SORT_BY_COMPANY_NAME = new Sort(new Sort.Order("companyName").ignoreCase(), new Sort.Order("id"));
    private static final Sort SORT_BY_ID = new Sort("id");
    private static final Logger logger = LoggerFactory.getLogger(ISPContoller.class);

    private final ISPRepository ispRepository;

    @Autowired
    ISPContoller(ISPRepository ispRepository) {
        this.ispRepository = ispRepository;
    }

    @RequestMapping(value = "/register", method = POST)
    ResponseEntity<?> register(@RequestBody ISP input) {
        logger.info("register[ISP={}]", input);
        ISP savedISP;
        try {
            savedISP = ispRepository.save(input);
            logger.info("... Success: {}", savedISP);
        } catch(DataIntegrityViolationException e) {
            logger.warn("... Failure", e);
            throw new ISPRegisterException("Could not create " + input + " as it is already exist", e);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest()
                .buildAndExpand(savedISP.getId())
                .toUri());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{ispId}", method = GET)
    ISP getISP(@PathVariable Long ispId) {
        logger.info("getISP[id={}]", ispId);
        ISP isp = ispRepository.findOne(ispId);
        if(isp == null) {
            logger.info("... ISP[id={}] not found", ispId);
            throw new ISPNotFoundException("Could not find ISP with id '" + ispId + "'.");
        }
        logger.info("... ISP[id={}] found: {}", ispId, isp);
        return isp;
    }

    @RequestMapping(value = "/list", method = GET)
    Collection<ISP> getAllISPs() {
        logger.info("getAllISPs");
        Collection<ISP> isps = ispRepository.findAll(SORT_BY_COMPANY_NAME);
        if(isps.isEmpty()) {
            logger.info("... No ISPs found");
            throw new ISPNotFoundException("Could not find ISPs.");
        }
        logger.info("... Found {} ISPs", isps.size());
        return isps;
    }

    @RequestMapping(value = "", method = GET, params = "companyName")
    Collection<ISP> findByCompanyName(@RequestParam("companyName") String companyName) {
        logger.info("findByCompanyName[companyName='{}']", companyName);
        Collection<ISP> isps = ispRepository.findByCompanyName(companyName, SORT_BY_ID);
        if(isps.isEmpty()) {
            logger.info("... ISP[companyName='{}'] not found", companyName);
            throw new ISPNotFoundException("Could not find ISPs with companyName '" + companyName + "'.");
        }
        logger.info("... Found {} ISPs", isps.size());
        return isps;
    }

    @RequestMapping(value = "/search", method = GET, params = "companyName")
    Collection<ISP> searchByCompanyName(@RequestParam("companyName") String companyName) {
        logger.info("searchByCompanyName[companyName like %{}%]", companyName);
        Collection<ISP> isps = ispRepository.searchByCompanyName(companyName);
        if(isps.isEmpty()) {
            logger.info("... ISP[companyName like %{}%'] not found", companyName);
            throw new ISPNotFoundException("Could not find ISPs with companyName like %" + companyName + "%.");
        }
        logger.info("... Found {} ISPs", isps.size());
        return isps;
    }

    @RequestMapping(value = "", method = GET, params = "website")
    Collection<ISP> findByWebsite(@RequestParam("website") String website) {
        logger.info("findByWebsite[website='{}']", website);
        Collection<ISP> isps = ispRepository.findByWebsite(website, SORT_BY_COMPANY_NAME);
        if(isps.isEmpty()) {
            logger.info("... ISP[website='{}'] not found", website);
            throw new ISPNotFoundException("Could not find ISPs with website '" + website + "'.");
        }
        logger.info("... Found {} ISPs", isps.size());
        return isps;
    }

    @RequestMapping(value = "/search", method = GET, params = "website")
    Collection<ISP> searchByWebsite(@RequestParam("website") String website) {
        logger.info("searchByWebsite[website like %{}%]", website);
        Collection<ISP> isps = ispRepository.searchByWebsite(website);
        if(isps.isEmpty()) {
            logger.info("... ISP[website like %{}%'] not found", website);
            throw new ISPNotFoundException("Could not find ISPs with website like %" + website + "%.");
        }
        logger.info("... Found {} ISPs", isps.size());
        return isps;
    }

    @RequestMapping(value = "", method = GET, params = "email")
    ISP findByEmail(@RequestParam("email") String email) {
        logger.info("findByEmail[email='{}']", email);
        return ispRepository.findByEmail(email)
                .map((value) -> {
                    logger.info("... ISP[email='{}'] found: {}", email, value);
                    return value;
                })
                .orElseThrow(() -> {
                    logger.info("... ISP[email='{}'] not found", email);
                    return new ISPNotFoundException("Could not find ISP with email '" + email + "'.");
                });
    }

    @RequestMapping(value = "/search", method = GET, params = "email")
    Collection<ISP> searchByEmail(@RequestParam("email") String email) {
        logger.info("searchByEmail[email like %{}%]", email);
        Collection<ISP> isps = ispRepository.searchByEmail(email);
        if(isps.isEmpty()) {
            logger.info("... ISP[email like %{}%'] not found", email);
            throw new ISPNotFoundException("Could not find ISPs with email like %" + email + "%.");
        }
        logger.info("... Found {} ISPs", isps.size());
        return isps;
    }
}
