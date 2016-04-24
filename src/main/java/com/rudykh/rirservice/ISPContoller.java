package com.rudykh.rirservice;

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

    private static final Sort SORT_BY_COMPANY_NAME = new Sort(new Sort.Order("companyName").ignoreCase());
    private static final Sort SORT_BY_ID = new Sort("id");
    private final ISPRepository ispRepository;

    @Autowired
    ISPContoller(ISPRepository ispRepository) {
        this.ispRepository = ispRepository;
    }

    @RequestMapping(value = "/register", method = POST)
    ResponseEntity<?> register(@RequestBody ISP input) {
        ISP savedISP;
        try {
            savedISP = ispRepository.save(input);
        } catch(DataIntegrityViolationException e) {
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
        ISP isp = ispRepository.findOne(ispId);
        if(isp == null) {
            throw new ISPNotFoundException("Could not find ISP with id '" + ispId + "'.");
        }
        return isp;
    }

    @RequestMapping(value = "/list", method = GET)
    Collection<ISP> getISP() {
        Collection<ISP> isps = ispRepository.findAll(SORT_BY_COMPANY_NAME);
        if(isps.isEmpty()) {
            throw new ISPNotFoundException("Could not find ISPs.");
        }
        return isps;
    }

    @RequestMapping(value = "", method = GET, params = "companyName")
    Collection<ISP> findByCompanyName(@RequestParam("companyName") String companyName) {
        Collection<ISP> isps = ispRepository.findByCompanyName(companyName, SORT_BY_ID);
        if(isps.isEmpty()) {
            throw new ISPNotFoundException("Could not find ISPs with companyName '" + companyName + "'.");
        }
        return isps;
    }

    @RequestMapping(value = "", method = GET, params = "website")
    Collection<ISP> findByWebsite(@RequestParam("website") String website) {
        Collection<ISP> isps = ispRepository.findByWebsite(website, SORT_BY_COMPANY_NAME);
        if(isps.isEmpty()) {
            throw new ISPNotFoundException("Could not find ISPs with website '" + website + "'.");
        }
        return isps;
    }

    @RequestMapping(value = "", method = GET, params = "email")
    ISP findByEmail(@RequestParam("email") String email) {
        return ispRepository.findByEmail(email)
                .orElseThrow(() -> new ISPNotFoundException("Could not find ISP with email '" + email + "'."));
    }
}
