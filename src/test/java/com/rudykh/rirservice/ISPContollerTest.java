package com.rudykh.rirservice;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;


/**
 * Created by Yaroslav Rudykh on 24.04.2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class ISPContollerTest {

    private MediaType jsonContentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private MediaType xmlContentType = new MediaType(MediaType.APPLICATION_XML.getType(),
            MediaType.APPLICATION_XML.getSubtype(),
            Charset.forName("utf8"));

    private MockMvc mockMvc;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;
    private HttpMessageConverter mappingJackson2XmlHttpMessageConverter;

    private List<ISP> ispList = new ArrayList<>();

    @Autowired
    private ISPRepository ispRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(
                hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().get();

        Assert.assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);

        this.mappingJackson2XmlHttpMessageConverter = Arrays.asList(converters).stream().filter(
                hmc -> hmc instanceof MappingJackson2XmlHttpMessageConverter).findAny().get();

        Assert.assertNotNull("the XML message converter must not be null", this.mappingJackson2XmlHttpMessageConverter);
    }

    @Before
    public void setup() throws Exception {
        mockMvc = webAppContextSetup(webApplicationContext).build();

        ispRepository.deleteAllInBatch();

        ispList.add(ispRepository.save(new ISP("e-BO Enterprises B.V.", "https://www.ripe.net/membership/indices/data/be.ebo-enterprises.html", "noc.ebo-enterprises@com")));
        ispList.add(ispRepository.save(new ISP("Some ISP", "https://www.ripe.net/", "mail1@some.isp")));
        ispList.add(ispRepository.save(new ISP("Some ISP", "https://www.ripe.net/", "mail2@some.isp")));
        ispList.add(ispRepository.save(new ISP("Some ISP", "", "mail3@some.isp")));
        ispList.add(ispRepository.save(new ISP("1Some ISP", "https://www.ripe.net/", "mail4@some.isp")));
    }

    @Test
    public void ispNotFoundByIdJSON() throws Exception {
        mockMvc.perform(get("/isp/100000")
                .accept(jsonContentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void ispNotFoundByIdXML() throws Exception {
        mockMvc.perform(get("/isp/100000")
                .accept(xmlContentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void ispFoundByIdJSON() throws Exception {
        mockMvc.perform(get("/isp/" + ispList.get(0).getId())
                .accept(jsonContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(jsonContentType))
                .andExpect(jsonPath("$.id", is(ispList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.companyName", is(ispList.get(0).getCompanyName())))
                .andExpect(jsonPath("$.website", is(ispList.get(0).getWebsite())))
                .andExpect(jsonPath("$.email", is(ispList.get(0).getEmail())));
    }

    @Test
    public void ispFoundByIdXML() throws Exception {
        mockMvc.perform(get("/isp/" + ispList.get(0).getId())
                .accept(xmlContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(xmlContentType))
                .andExpect(xpath("count(//ISP)").number(1.0))
                .andExpect(xpath("//ISP/id[.=" + ispList.get(0).getId() + "]").exists())
                .andExpect(xpath("//ISP/companyName[.=\"" + ispList.get(0).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//ISP/website[.=\"" + ispList.get(0).getWebsite() + "\"]").exists())
                .andExpect(xpath("//ISP/email[.=\"" + ispList.get(0).getEmail() + "\"]").exists());
    }

    @Test
    public void ispNotFoundByCompanyNameJSON() throws Exception {
        mockMvc.perform(get("/isp").param("companyName", "Not Existing Name")
                .accept(jsonContentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void ispNotFoundByCompanyNameXML() throws Exception {
        mockMvc.perform(get("/isp").param("companyName", "Not Existing Name")
                .accept(xmlContentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void singleISPFoundByCompanyNameJSON() throws Exception {
        mockMvc.perform(get("/isp").param("companyName", ispList.get(0).getCompanyName())
                .accept(jsonContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(jsonContentType))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id", is(ispList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.[0].companyName", is(ispList.get(0).getCompanyName())))
                .andExpect(jsonPath("$.[0].website", is(ispList.get(0).getWebsite())))
                .andExpect(jsonPath("$.[0].email", is(ispList.get(0).getEmail())));
    }

    @Test
    public void singleISPFoundByCompanyNameXML() throws Exception {
        mockMvc.perform(get("/isp").param("companyName", ispList.get(0).getCompanyName())
                .accept(xmlContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(xmlContentType))
                .andExpect(xpath("count(//Collection)").number(1.0))
                .andExpect(xpath("count(//Collection/item)").number(1.0))
                .andExpect(xpath("//Collection/item/id[.=" + ispList.get(0).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item/companyName[.=\"" + ispList.get(0).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item/website[.=\"" + ispList.get(0).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item/email[.=\"" + ispList.get(0).getEmail() + "\"]").exists());
    }

    @Test
    public void multipleISPsFoundByCompanyNameJSON() throws Exception {
        mockMvc.perform(get("/isp").param("companyName", ispList.get(1).getCompanyName())
                .accept(jsonContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(jsonContentType))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$.[0].id", is(ispList.get(1).getId().intValue())))
                .andExpect(jsonPath("$.[0].companyName", is(ispList.get(1).getCompanyName())))
                .andExpect(jsonPath("$.[0].website", is(ispList.get(1).getWebsite())))
                .andExpect(jsonPath("$.[0].email", is(ispList.get(1).getEmail())))
                .andExpect(jsonPath("$.[1].id", is(ispList.get(2).getId().intValue())))
                .andExpect(jsonPath("$.[1].companyName", is(ispList.get(2).getCompanyName())))
                .andExpect(jsonPath("$.[1].website", is(ispList.get(2).getWebsite())))
                .andExpect(jsonPath("$.[1].email", is(ispList.get(2).getEmail())))
                .andExpect(jsonPath("$.[2].id", is(ispList.get(3).getId().intValue())))
                .andExpect(jsonPath("$.[2].companyName", is(ispList.get(3).getCompanyName())))
                .andExpect(jsonPath("$.[2].website", is(ispList.get(3).getWebsite())))
                .andExpect(jsonPath("$.[2].email", is(ispList.get(3).getEmail())));
    }

    @Test
    public void multipleISPsFoundByCompanyNameXML() throws Exception {
        mockMvc.perform(get("/isp").param("companyName", ispList.get(1).getCompanyName())
                .accept(xmlContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(xmlContentType))
                .andExpect(xpath("count(//Collection)").number(1.0))
                .andExpect(xpath("count(//Collection/item)").number(3.0))
                .andExpect(xpath("//Collection/item[1]/id[.=" + ispList.get(1).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[1]/companyName[.=\"" + ispList.get(1).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[1]/website[.=\"" + ispList.get(1).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[1]/email[.=\"" + ispList.get(1).getEmail() + "\"]").exists())
                .andExpect(xpath("//Collection/item[2]/id[.=" + ispList.get(2).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[2]/companyName[.=\"" + ispList.get(2).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[2]/website[.=\"" + ispList.get(2).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[2]/email[.=\"" + ispList.get(2).getEmail() + "\"]").exists())
                .andExpect(xpath("//Collection/item[3]/id[.=" + ispList.get(3).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[3]/companyName[.=\"" + ispList.get(3).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[3]/website[.=\"" + ispList.get(3).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[3]/email[.=\"" + ispList.get(3).getEmail() + "\"]").exists());
    }

    @Test
    public void ispNotSearchedByCompanyNameJSON() throws Exception {
        mockMvc.perform(get("/isp/search").param("companyName", "Not Existing Name")
                .accept(jsonContentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void ispNotSearchedByCompanyNameXML() throws Exception {
        mockMvc.perform(get("/isp/search").param("companyName", "Not Existing Name")
                .accept(xmlContentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void singleISPSearchedByCompanyNameJSON() throws Exception {
        mockMvc.perform(get("/isp/search").param("companyName", ispList.get(0).getCompanyName().substring(5, 15))
                .accept(jsonContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(jsonContentType))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id", is(ispList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.[0].companyName", is(ispList.get(0).getCompanyName())))
                .andExpect(jsonPath("$.[0].website", is(ispList.get(0).getWebsite())))
                .andExpect(jsonPath("$.[0].email", is(ispList.get(0).getEmail())));
    }

    @Test
    public void singleISPSearchedByCompanyNameXML() throws Exception {
        mockMvc.perform(get("/isp/search").param("companyName", ispList.get(0).getCompanyName().substring(5, 15))
                .accept(xmlContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(xmlContentType))
                .andExpect(xpath("count(//Collection)").number(1.0))
                .andExpect(xpath("count(//Collection/item)").number(1.0))
                .andExpect(xpath("//Collection/item/id[.=" + ispList.get(0).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item/companyName[.=\"" + ispList.get(0).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item/website[.=\"" + ispList.get(0).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item/email[.=\"" + ispList.get(0).getEmail() + "\"]").exists());
    }

    @Test
    public void multipleISPsSearchedByCompanyNameJSON() throws Exception {
        mockMvc.perform(get("/isp/search").param("companyName", ispList.get(1).getCompanyName().substring(0, 4))
                .accept(jsonContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(jsonContentType))
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$.[0].id", is(ispList.get(4).getId().intValue())))
                .andExpect(jsonPath("$.[0].companyName", is(ispList.get(4).getCompanyName())))
                .andExpect(jsonPath("$.[0].website", is(ispList.get(4).getWebsite())))
                .andExpect(jsonPath("$.[0].email", is(ispList.get(4).getEmail())))
                .andExpect(jsonPath("$.[1].id", is(ispList.get(1).getId().intValue())))
                .andExpect(jsonPath("$.[1].companyName", is(ispList.get(1).getCompanyName())))
                .andExpect(jsonPath("$.[1].website", is(ispList.get(1).getWebsite())))
                .andExpect(jsonPath("$.[1].email", is(ispList.get(1).getEmail())))
                .andExpect(jsonPath("$.[2].id", is(ispList.get(2).getId().intValue())))
                .andExpect(jsonPath("$.[2].companyName", is(ispList.get(2).getCompanyName())))
                .andExpect(jsonPath("$.[2].website", is(ispList.get(2).getWebsite())))
                .andExpect(jsonPath("$.[2].email", is(ispList.get(2).getEmail())))
                .andExpect(jsonPath("$.[3].id", is(ispList.get(3).getId().intValue())))
                .andExpect(jsonPath("$.[3].companyName", is(ispList.get(3).getCompanyName())))
                .andExpect(jsonPath("$.[3].website", is(ispList.get(3).getWebsite())))
                .andExpect(jsonPath("$.[3].email", is(ispList.get(3).getEmail())));
    }

    @Test
    public void multipleISPsSearchedByCompanyNameXML() throws Exception {
        mockMvc.perform(get("/isp/search").param("companyName", ispList.get(1).getCompanyName().substring(0, 4))
                .accept(xmlContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(xmlContentType))
                .andExpect(xpath("count(//Collection)").number(1.0))
                .andExpect(xpath("count(//Collection/item)").number(4.0))
                .andExpect(xpath("//Collection/item[1]/id[.=" + ispList.get(4).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[1]/companyName[.=\"" + ispList.get(4).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[1]/website[.=\"" + ispList.get(4).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[1]/email[.=\"" + ispList.get(4).getEmail() + "\"]").exists())
                .andExpect(xpath("//Collection/item[2]/id[.=" + ispList.get(1).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[2]/companyName[.=\"" + ispList.get(1).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[2]/website[.=\"" + ispList.get(1).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[2]/email[.=\"" + ispList.get(1).getEmail() + "\"]").exists())
                .andExpect(xpath("//Collection/item[3]/id[.=" + ispList.get(2).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[3]/companyName[.=\"" + ispList.get(2).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[3]/website[.=\"" + ispList.get(2).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[3]/email[.=\"" + ispList.get(2).getEmail() + "\"]").exists())
                .andExpect(xpath("//Collection/item[4]/id[.=" + ispList.get(3).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[4]/companyName[.=\"" + ispList.get(3).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[4]/website[.=\"" + ispList.get(3).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[4]/email[.=\"" + ispList.get(3).getEmail() + "\"]").exists());
    }

    @Test
    public void ispNotFoundByWebsiteJSON() throws Exception {
        mockMvc.perform(get("/isp").param("website", "not.existing.site")
                .accept(jsonContentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void ispNotFoundByWebsiteXML() throws Exception {
        mockMvc.perform(get("/isp").param("website", "not.existing.site")
                .accept(xmlContentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void singleISPFoundByWebsiteJSON() throws Exception {
        mockMvc.perform(get("/isp").param("website", ispList.get(0).getWebsite())
                .accept(jsonContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(jsonContentType))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id", is(ispList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.[0].companyName", is(ispList.get(0).getCompanyName())))
                .andExpect(jsonPath("$.[0].website", is(ispList.get(0).getWebsite())))
                .andExpect(jsonPath("$.[0].email", is(ispList.get(0).getEmail())));
    }

    @Test
    public void singleISPFoundByWebsiteXML() throws Exception {
        mockMvc.perform(get("/isp").param("website", ispList.get(0).getWebsite())
                .accept(xmlContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(xmlContentType))
                .andExpect(xpath("count(//Collection)").number(1.0))
                .andExpect(xpath("count(//Collection/item)").number(1.0))
                .andExpect(xpath("//Collection/item/id[.=" + ispList.get(0).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item/companyName[.=\"" + ispList.get(0).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item/website[.=\"" + ispList.get(0).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item/email[.=\"" + ispList.get(0).getEmail() + "\"]").exists());
    }

    @Test
    public void multipleISPsFoundByWebsiteJSON() throws Exception {
        mockMvc.perform(get("/isp").param("website", ispList.get(1).getWebsite())
                .accept(jsonContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(jsonContentType))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$.[0].id", is(ispList.get(4).getId().intValue())))
                .andExpect(jsonPath("$.[0].companyName", is(ispList.get(4).getCompanyName())))
                .andExpect(jsonPath("$.[0].website", is(ispList.get(4).getWebsite())))
                .andExpect(jsonPath("$.[0].email", is(ispList.get(4).getEmail())))
                .andExpect(jsonPath("$.[1].id", is(ispList.get(1).getId().intValue())))
                .andExpect(jsonPath("$.[1].companyName", is(ispList.get(1).getCompanyName())))
                .andExpect(jsonPath("$.[1].website", is(ispList.get(1).getWebsite())))
                .andExpect(jsonPath("$.[1].email", is(ispList.get(1).getEmail())))
                .andExpect(jsonPath("$.[2].id", is(ispList.get(2).getId().intValue())))
                .andExpect(jsonPath("$.[2].companyName", is(ispList.get(2).getCompanyName())))
                .andExpect(jsonPath("$.[2].website", is(ispList.get(2).getWebsite())))
                .andExpect(jsonPath("$.[2].email", is(ispList.get(2).getEmail())));
    }

    @Test
    public void multipleISPsFoundByWebsiteXML() throws Exception {
        mockMvc.perform(get("/isp").param("website", ispList.get(1).getWebsite())
                .accept(xmlContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(xmlContentType))
                .andExpect(xpath("count(//Collection)").number(1.0))
                .andExpect(xpath("count(//Collection/item)").number(3.0))
                .andExpect(xpath("//Collection/item[1]/id[.=" + ispList.get(4).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[1]/companyName[.=\"" + ispList.get(4).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[1]/website[.=\"" + ispList.get(4).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[1]/email[.=\"" + ispList.get(4).getEmail() + "\"]").exists())
                .andExpect(xpath("//Collection/item[2]/id[.=" + ispList.get(1).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[2]/companyName[.=\"" + ispList.get(1).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[2]/website[.=\"" + ispList.get(1).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[2]/email[.=\"" + ispList.get(1).getEmail() + "\"]").exists())
                .andExpect(xpath("//Collection/item[3]/id[.=" + ispList.get(2).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[3]/companyName[.=\"" + ispList.get(2).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[3]/website[.=\"" + ispList.get(2).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[3]/email[.=\"" + ispList.get(2).getEmail() + "\"]").exists());
    }

    @Test
    public void ispNotSearchedByWebsiteJSON() throws Exception {
        mockMvc.perform(get("/isp/search").param("website", "not.existing.site")
                .accept(jsonContentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void ispNotSearchedByWebsiteXML() throws Exception {
        mockMvc.perform(get("/isp/search").param("website", "not.existing.site")
                .accept(xmlContentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void singleISPSearchedByWebsiteJSON() throws Exception {
        mockMvc.perform(get("/isp/search").param("website", ispList.get(0).getWebsite().substring(0, 22))
                .accept(jsonContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(jsonContentType))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id", is(ispList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.[0].companyName", is(ispList.get(0).getCompanyName())))
                .andExpect(jsonPath("$.[0].website", is(ispList.get(0).getWebsite())))
                .andExpect(jsonPath("$.[0].email", is(ispList.get(0).getEmail())));
    }

    @Test
    public void singleISPSearchedByWebsiteXML() throws Exception {
        mockMvc.perform(get("/isp/search").param("website", ispList.get(0).getWebsite().substring(0, 22))
                .accept(xmlContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(xmlContentType))
                .andExpect(xpath("count(//Collection)").number(1.0))
                .andExpect(xpath("count(//Collection/item)").number(1.0))
                .andExpect(xpath("//Collection/item/id[.=" + ispList.get(0).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item/companyName[.=\"" + ispList.get(0).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item/website[.=\"" + ispList.get(0).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item/email[.=\"" + ispList.get(0).getEmail() + "\"]").exists());
    }

    @Test
    public void multipleISPsSearchedByWebsiteJSON() throws Exception {
        mockMvc.perform(get("/isp/search").param("website", ispList.get(0).getWebsite().substring(0, 21))
                .accept(jsonContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(jsonContentType))
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$.[0].id", is(ispList.get(4).getId().intValue())))
                .andExpect(jsonPath("$.[0].companyName", is(ispList.get(4).getCompanyName())))
                .andExpect(jsonPath("$.[0].website", is(ispList.get(4).getWebsite())))
                .andExpect(jsonPath("$.[0].email", is(ispList.get(4).getEmail())))
                .andExpect(jsonPath("$.[1].id", is(ispList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.[1].companyName", is(ispList.get(0).getCompanyName())))
                .andExpect(jsonPath("$.[1].website", is(ispList.get(0).getWebsite())))
                .andExpect(jsonPath("$.[1].email", is(ispList.get(0).getEmail())))
                .andExpect(jsonPath("$.[2].id", is(ispList.get(1).getId().intValue())))
                .andExpect(jsonPath("$.[2].companyName", is(ispList.get(1).getCompanyName())))
                .andExpect(jsonPath("$.[2].website", is(ispList.get(1).getWebsite())))
                .andExpect(jsonPath("$.[2].email", is(ispList.get(1).getEmail())))
                .andExpect(jsonPath("$.[3].id", is(ispList.get(2).getId().intValue())))
                .andExpect(jsonPath("$.[3].companyName", is(ispList.get(2).getCompanyName())))
                .andExpect(jsonPath("$.[3].website", is(ispList.get(2).getWebsite())))
                .andExpect(jsonPath("$.[3].email", is(ispList.get(2).getEmail())));
    }

    @Test
    public void multipleISPsSearchedByWebsiteXML() throws Exception {
        mockMvc.perform(get("/isp/search").param("website", ispList.get(0).getWebsite().substring(0, 21))
                .accept(xmlContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(xmlContentType))
                .andExpect(xpath("count(//Collection)").number(1.0))
                .andExpect(xpath("count(//Collection/item)").number(4.0))
                .andExpect(xpath("//Collection/item[1]/id[.=" + ispList.get(4).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[1]/companyName[.=\"" + ispList.get(4).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[1]/website[.=\"" + ispList.get(4).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[1]/email[.=\"" + ispList.get(4).getEmail() + "\"]").exists())
                .andExpect(xpath("//Collection/item[2]/id[.=" + ispList.get(0).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[2]/companyName[.=\"" + ispList.get(0).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[2]/website[.=\"" + ispList.get(0).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[2]/email[.=\"" + ispList.get(0).getEmail() + "\"]").exists())
                .andExpect(xpath("//Collection/item[3]/id[.=" + ispList.get(1).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[3]/companyName[.=\"" + ispList.get(1).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[3]/website[.=\"" + ispList.get(1).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[3]/email[.=\"" + ispList.get(1).getEmail() + "\"]").exists())
                .andExpect(xpath("//Collection/item[4]/id[.=" + ispList.get(2).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[4]/companyName[.=\"" + ispList.get(2).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[4]/website[.=\"" + ispList.get(2).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[4]/email[.=\"" + ispList.get(2).getEmail() + "\"]").exists());
    }

    @Test
    public void singleISPFoundByEmptyWebsiteJSON() throws Exception {
        mockMvc.perform(get("/isp").param("website", "")
                .accept(jsonContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(jsonContentType))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id", is(ispList.get(3).getId().intValue())))
                .andExpect(jsonPath("$.[0].companyName", is(ispList.get(3).getCompanyName())))
                .andExpect(jsonPath("$.[0].website", is(ispList.get(3).getWebsite())))
                .andExpect(jsonPath("$.[0].email", is(ispList.get(3).getEmail())));
    }

    @Test
    public void singleISPFoundByEmptyWebsiteXML() throws Exception {
        mockMvc.perform(get("/isp").param("website", "")
                .accept(xmlContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(xmlContentType))
                .andExpect(xpath("count(//Collection)").number(1.0))
                .andExpect(xpath("count(//Collection/item)").number(1.0))
                .andExpect(xpath("//Collection/item/id[.=" + ispList.get(3).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item/companyName[.=\"" + ispList.get(3).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item/website[.=\"" + ispList.get(3).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item/email[.=\"" + ispList.get(3).getEmail() + "\"]").exists());
    }

    @Test
    public void ispNotFoundByEmailJSON() throws Exception {
        mockMvc.perform(get("/isp").param("email", "not@existing.email")
                .contentType(jsonContentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void ispNotFoundByEmailXML() throws Exception {
        mockMvc.perform(get("/isp").param("email", "not@existing.email")
                .contentType(xmlContentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void ispFoundByEmailJSON() throws Exception {
        mockMvc.perform(get("/isp").param("email", ispList.get(0).getEmail())
                .accept(jsonContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(jsonContentType))
                .andExpect(jsonPath("$.id", is(ispList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.companyName", is(ispList.get(0).getCompanyName())))
                .andExpect(jsonPath("$.website", is(ispList.get(0).getWebsite())))
                .andExpect(jsonPath("$.email", is(ispList.get(0).getEmail())));
    }

    @Test
    public void ispFoundByEmailXML() throws Exception {
        mockMvc.perform(get("/isp").param("email", ispList.get(0).getEmail())
                .accept(xmlContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(xmlContentType))
                .andExpect(xpath("count(//ISP)").number(1.0))
                .andExpect(xpath("//ISP/id[.=" + ispList.get(0).getId().intValue() + "]").exists())
                .andExpect(xpath("//ISP/companyName[.=\"" + ispList.get(0).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//ISP/website[.=\"" + ispList.get(0).getWebsite() + "\"]").exists())
                .andExpect(xpath("//ISP/email[.=\"" + ispList.get(0).getEmail() + "\"]").exists());
    }

    @Test
    public void ispNotSearchedByEmailJSON() throws Exception {
        mockMvc.perform(get("/isp/search").param("email", "not@existing.email")
                .accept(jsonContentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void ispNotSearchedByEmailXML() throws Exception {
        mockMvc.perform(get("/isp/search").param("email", "not@existing.email")
                .accept(xmlContentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void singleISPSearchedByEmailJSON() throws Exception {
        mockMvc.perform(get("/isp/search").param("email", ispList.get(0).getEmail().substring(4, 19))
                .accept(jsonContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(jsonContentType))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id", is(ispList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.[0].companyName", is(ispList.get(0).getCompanyName())))
                .andExpect(jsonPath("$.[0].website", is(ispList.get(0).getWebsite())))
                .andExpect(jsonPath("$.[0].email", is(ispList.get(0).getEmail())));
    }

    @Test
    public void singleISPSearchedByEmailXML() throws Exception {
        mockMvc.perform(get("/isp/search").param("email", ispList.get(0).getEmail().substring(4, 19))
                .accept(xmlContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(xmlContentType))
                .andExpect(xpath("count(//Collection)").number(1.0))
                .andExpect(xpath("count(//Collection/item)").number(1.0))
                .andExpect(xpath("//Collection/item/id[.=" + ispList.get(0).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item/companyName[.=\"" + ispList.get(0).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item/website[.=\"" + ispList.get(0).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item/email[.=\"" + ispList.get(0).getEmail() + "\"]").exists());
    }

    @Test
    public void multipleISPsSearchedByEmailJSON() throws Exception {
        mockMvc.perform(get("/isp/search").param("email", ispList.get(1).getEmail().substring(0, 4))
                .accept(jsonContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(jsonContentType))
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$.[0].id", is(ispList.get(4).getId().intValue())))
                .andExpect(jsonPath("$.[0].companyName", is(ispList.get(4).getCompanyName())))
                .andExpect(jsonPath("$.[0].website", is(ispList.get(4).getWebsite())))
                .andExpect(jsonPath("$.[0].email", is(ispList.get(4).getEmail())))
                .andExpect(jsonPath("$.[1].id", is(ispList.get(1).getId().intValue())))
                .andExpect(jsonPath("$.[1].companyName", is(ispList.get(1).getCompanyName())))
                .andExpect(jsonPath("$.[1].website", is(ispList.get(1).getWebsite())))
                .andExpect(jsonPath("$.[1].email", is(ispList.get(1).getEmail())))
                .andExpect(jsonPath("$.[2].id", is(ispList.get(2).getId().intValue())))
                .andExpect(jsonPath("$.[2].companyName", is(ispList.get(2).getCompanyName())))
                .andExpect(jsonPath("$.[2].website", is(ispList.get(2).getWebsite())))
                .andExpect(jsonPath("$.[2].email", is(ispList.get(2).getEmail())))
                .andExpect(jsonPath("$.[3].id", is(ispList.get(3).getId().intValue())))
                .andExpect(jsonPath("$.[3].companyName", is(ispList.get(3).getCompanyName())))
                .andExpect(jsonPath("$.[3].website", is(ispList.get(3).getWebsite())))
                .andExpect(jsonPath("$.[3].email", is(ispList.get(3).getEmail())))
        ;
    }

    @Test
    public void multipleISPsSearchedByEmailXML() throws Exception {
        mockMvc.perform(get("/isp/search").param("email", ispList.get(1).getEmail().substring(0, 4))
                .accept(xmlContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(xmlContentType))
                .andExpect(xpath("count(//Collection)").number(1.0))
                .andExpect(xpath("count(//Collection/item)").number(4.0))
                .andExpect(xpath("//Collection/item[1]/id[.=" + ispList.get(4).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[1]/companyName[.=\"" + ispList.get(4).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[1]/website[.=\"" + ispList.get(4).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[1]/email[.=\"" + ispList.get(4).getEmail() + "\"]").exists())
                .andExpect(xpath("//Collection/item[2]/id[.=" + ispList.get(1).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[2]/companyName[.=\"" + ispList.get(1).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[2]/website[.=\"" + ispList.get(1).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[2]/email[.=\"" + ispList.get(1).getEmail() + "\"]").exists())
                .andExpect(xpath("//Collection/item[3]/id[.=" + ispList.get(2).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[3]/companyName[.=\"" + ispList.get(2).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[3]/website[.=\"" + ispList.get(2).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[3]/email[.=\"" + ispList.get(2).getEmail() + "\"]").exists())
                .andExpect(xpath("//Collection/item[4]/id[.=" + ispList.get(3).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[4]/companyName[.=\"" + ispList.get(3).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[4]/website[.=\"" + ispList.get(3).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[4]/email[.=\"" + ispList.get(3).getEmail() + "\"]").exists());
    }

    @Test
    public void ispListJSON() throws Exception {
        mockMvc.perform(get("/isp/list")
                .accept(jsonContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(jsonContentType))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$.[0].id", is(ispList.get(4).getId().intValue())))
                .andExpect(jsonPath("$.[0].companyName", is(ispList.get(4).getCompanyName())))
                .andExpect(jsonPath("$.[0].website", is(ispList.get(4).getWebsite())))
                .andExpect(jsonPath("$.[0].email", is(ispList.get(4).getEmail())))
                .andExpect(jsonPath("$.[1].id", is(ispList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.[1].companyName", is(ispList.get(0).getCompanyName())))
                .andExpect(jsonPath("$.[1].website", is(ispList.get(0).getWebsite())))
                .andExpect(jsonPath("$.[1].email", is(ispList.get(0).getEmail())))
                .andExpect(jsonPath("$.[2].id", is(ispList.get(1).getId().intValue())))
                .andExpect(jsonPath("$.[2].companyName", is(ispList.get(1).getCompanyName())))
                .andExpect(jsonPath("$.[2].website", is(ispList.get(1).getWebsite())))
                .andExpect(jsonPath("$.[2].email", is(ispList.get(1).getEmail())))
                .andExpect(jsonPath("$.[3].id", is(ispList.get(2).getId().intValue())))
                .andExpect(jsonPath("$.[3].companyName", is(ispList.get(2).getCompanyName())))
                .andExpect(jsonPath("$.[3].website", is(ispList.get(2).getWebsite())))
                .andExpect(jsonPath("$.[3].email", is(ispList.get(2).getEmail())))
                .andExpect(jsonPath("$.[4].id", is(ispList.get(3).getId().intValue())))
                .andExpect(jsonPath("$.[4].companyName", is(ispList.get(3).getCompanyName())))
                .andExpect(jsonPath("$.[4].website", is(ispList.get(3).getWebsite())))
                .andExpect(jsonPath("$.[4].email", is(ispList.get(3).getEmail())));
    }

    @Test
    public void ispListXML() throws Exception {
        mockMvc.perform(get("/isp/list")
                .accept(xmlContentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(xmlContentType))
                .andExpect(xpath("count(//Collection)").number(1.0))
                .andExpect(xpath("count(//Collection/item)").number(5.0))
                .andExpect(xpath("//Collection/item[1]/id[.=" + ispList.get(4).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[1]/companyName[.=\"" + ispList.get(4).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[1]/website[.=\"" + ispList.get(4).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[1]/email[.=\"" + ispList.get(4).getEmail() + "\"]").exists())
                .andExpect(xpath("//Collection/item[2]/id[.=" + ispList.get(0).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[2]/companyName[.=\"" + ispList.get(0).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[2]/website[.=\"" + ispList.get(0).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[2]/email[.=\"" + ispList.get(0).getEmail() + "\"]").exists())
                .andExpect(xpath("//Collection/item[3]/id[.=" + ispList.get(1).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[3]/companyName[.=\"" + ispList.get(1).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[3]/website[.=\"" + ispList.get(1).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[3]/email[.=\"" + ispList.get(1).getEmail() + "\"]").exists())
                .andExpect(xpath("//Collection/item[4]/id[.=" + ispList.get(2).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[4]/companyName[.=\"" + ispList.get(2).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[4]/website[.=\"" + ispList.get(2).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[4]/email[.=\"" + ispList.get(2).getEmail() + "\"]").exists())
                .andExpect(xpath("//Collection/item[5]/id[.=" + ispList.get(3).getId().intValue() + "]").exists())
                .andExpect(xpath("//Collection/item[5]/companyName[.=\"" + ispList.get(3).getCompanyName() + "\"]").exists())
                .andExpect(xpath("//Collection/item[5]/website[.=\"" + ispList.get(3).getWebsite() + "\"]").exists())
                .andExpect(xpath("//Collection/item[5]/email[.=\"" + ispList.get(3).getEmail() + "\"]").exists());
    }

    @Test
    public void createISPSuccessJSON() throws Exception {
        String ispJson = toJson(new ISP("New ISP", "http://wibsite.com", "new@isp.email"));
        this.mockMvc.perform(post("/isp/register")
                .contentType(jsonContentType)
                .content(ispJson))
                .andExpect(status().isCreated());
    }

    @Test
    public void createISPSuccessXML() throws Exception {
        String ispXml = toXml(new ISP("New ISP", "http://wibsite.com", "new@isp.email"));
        this.mockMvc.perform(post("/isp/register")
                .contentType(xmlContentType)
                .content(ispXml))
                .andExpect(status().isCreated());
    }

    @Test
    public void createISPFailureJSON() throws Exception {
        String ispJson = toJson(new ISP("New ISP", "http://wibsite.com", "noc.ebo-enterprises@com"));
        this.mockMvc.perform(post("/isp/register")
                .contentType(jsonContentType)
                .content(ispJson))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void createISPFailureXML() throws Exception {
        String ispXml = toXml(new ISP("New ISP", "http://wibsite.com", "noc.ebo-enterprises@com"));
        this.mockMvc.perform(post("/isp/register")
                .contentType(xmlContentType)
                .content(ispXml))
                .andExpect(status().isUnprocessableEntity());
    }

    private String toJson(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

    private String toXml(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2XmlHttpMessageConverter.write(o, MediaType.APPLICATION_XML, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
