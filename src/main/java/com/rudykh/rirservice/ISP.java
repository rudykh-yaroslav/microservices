package com.rudykh.rirservice;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Yaroslav Rudykh on 23.04.2016.
 */
@Entity
@XmlRootElement(name = "isp")
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

    @XmlElement
    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    @XmlElement
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getWebsite() {
        return website;
    }

    @XmlElement
    public void setWebsite(String website) {
        this.website = website;
    }

    public String getEmail() {
        return email;
    }

    @XmlElement
    public void setEmail(String email) {
        this.email = email;
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
