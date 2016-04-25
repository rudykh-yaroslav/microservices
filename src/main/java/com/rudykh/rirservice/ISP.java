package com.rudykh.rirservice;

import javax.persistence.*;

/**
 * Created by Yaroslav Rudykh on 23.04.2016.
 */
@Entity
public class ISP {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String website;

    @Column(unique = true, nullable = false)
    private String email;

    ISP() {
    }

    public ISP(String companyName, String website, String email) {
        this.companyName = companyName;
        this.website = website;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getWebsite() {
        return website;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "ISP{" +
                "companyName='" + companyName + '\'' +
                ", website='" + website + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
