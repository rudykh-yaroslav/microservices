package com.rudykh.rirservice;

import org.springframework.beans.factory.annotation.Autowired;
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
public class ISPContoller {

    private final ISPRepository ispRepository;

    @Autowired
    ISPContoller(ISPRepository ispRepository) {
        this.ispRepository = ispRepository;
    }

    @RequestMapping(value = "/register", method = POST)
    ResponseEntity<?> register(@RequestBody ISP input) {
        ISP savedISP = ispRepository.save(input);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest()
                .buildAndExpand(savedISP.getId())
                .toUri());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{ispId}", method = RequestMethod.GET)
    ISP getISP(@PathVariable Long ispId) {
        return ispRepository.findOne(ispId);
    }

//    @RequestMapping(value = "/list", method = RequestMethod.GET)
//    Collection<ISP> getISP() {
//        return this.bookmarkRepository.findByAccountUsername(userId);
//    }
}
