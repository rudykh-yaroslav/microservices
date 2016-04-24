package com.rudykh.rirservice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Author: Yaroslav Rudykh (slavan.it2me@gmail.com) Date: 24.04.2016
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class ISPRegisterException extends RuntimeException {

    public ISPRegisterException(String message, Exception e) {
        super(message, e);
    }
}