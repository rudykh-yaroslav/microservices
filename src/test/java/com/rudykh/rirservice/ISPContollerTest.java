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

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private MediaType xmlContentType = new MediaType(MediaType.APPLICATION_XML.getType(),
            MediaType.APPLICATION_XML.getSubtype(),
            Charset.forName("utf8"));

    private MockMvc mockMvc;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

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
    public void ispNotFoundById() throws Exception {
        mockMvc.perform(get("/isp/100000")
                .accept(contentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void ispFoundById() throws Exception {
        mockMvc.perform(get("/isp/" + ispList.get(0).getId())
                .accept(contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.id", is(ispList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.companyName", is(ispList.get(0).getCompanyName())))
                .andExpect(jsonPath("$.website", is(ispList.get(0).getWebsite())))
                .andExpect(jsonPath("$.email", is(ispList.get(0).getEmail())));
    }

    @Test
    public void ispNotFoundByCompanyName() throws Exception {
        mockMvc.perform(get("/isp").param("companyName", "Not Existing Name")
                .accept(contentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void singleISPFoundByCompanyName() throws Exception {
        mockMvc.perform(get("/isp").param("companyName", ispList.get(0).getCompanyName())
                .accept(contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id", is(ispList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.[0].companyName", is(ispList.get(0).getCompanyName())))
                .andExpect(jsonPath("$.[0].website", is(ispList.get(0).getWebsite())))
                .andExpect(jsonPath("$.[0].email", is(ispList.get(0).getEmail())));
    }

    @Test
    public void multipleISPsFoundByCompanyName() throws Exception {
        mockMvc.perform(get("/isp").param("companyName", ispList.get(1).getCompanyName())
                .accept(contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
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
    public void ispNotSearchedByCompanyName() throws Exception {
        mockMvc.perform(get("/isp/search").param("companyName", "Not Existing Name")
                .accept(contentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void singleISPSearchedByCompanyName() throws Exception {
        mockMvc.perform(get("/isp/search").param("companyName", ispList.get(0).getCompanyName().substring(5, 15))
                .accept(contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id", is(ispList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.[0].companyName", is(ispList.get(0).getCompanyName())))
                .andExpect(jsonPath("$.[0].website", is(ispList.get(0).getWebsite())))
                .andExpect(jsonPath("$.[0].email", is(ispList.get(0).getEmail())));
    }

    @Test
    public void multipleISPsSearchedByCompanyName() throws Exception {
        mockMvc.perform(get("/isp/search").param("companyName", ispList.get(1).getCompanyName().substring(0, 4))
                .accept(contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
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
    public void ispNotFoundByWebsite() throws Exception {
        mockMvc.perform(get("/isp").param("website", "not.existing.site")
                .accept(contentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void singleISPFoundByWebsite() throws Exception {
        mockMvc.perform(get("/isp").param("website", ispList.get(0).getWebsite())
                .accept(contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id", is(ispList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.[0].companyName", is(ispList.get(0).getCompanyName())))
                .andExpect(jsonPath("$.[0].website", is(ispList.get(0).getWebsite())))
                .andExpect(jsonPath("$.[0].email", is(ispList.get(0).getEmail())));
    }

    @Test
    public void multipleISPsFoundByWebsite() throws Exception {
        mockMvc.perform(get("/isp").param("website", ispList.get(1).getWebsite())
                .accept(contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
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
    public void ispNotSearchedByWebsite() throws Exception {
        mockMvc.perform(get("/isp/search").param("website", "not.existing.site")
                .accept(contentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void singleISPSearchedByWebsite() throws Exception {
        mockMvc.perform(get("/isp/search").param("website", ispList.get(0).getWebsite().substring(0, 22))
                .accept(contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id", is(ispList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.[0].companyName", is(ispList.get(0).getCompanyName())))
                .andExpect(jsonPath("$.[0].website", is(ispList.get(0).getWebsite())))
                .andExpect(jsonPath("$.[0].email", is(ispList.get(0).getEmail())));
    }

    @Test
    public void multipleISPsSearchedByWebsite() throws Exception {
        mockMvc.perform(get("/isp/search").param("website", ispList.get(0).getWebsite().substring(0, 21))
                .accept(contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
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
    public void singleISPFoundByEmptyWebsite() throws Exception {
        mockMvc.perform(get("/isp").param("website", "")
                .accept(contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id", is(ispList.get(3).getId().intValue())))
                .andExpect(jsonPath("$.[0].companyName", is(ispList.get(3).getCompanyName())))
                .andExpect(jsonPath("$.[0].website", is(ispList.get(3).getWebsite())))
                .andExpect(jsonPath("$.[0].email", is(ispList.get(3).getEmail())));
    }

    @Test
    public void ispNotFoundByEmail() throws Exception {
        mockMvc.perform(get("/isp").param("email", "not@existing.email")
                .contentType(contentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void ispFoundByEmail() throws Exception {
        mockMvc.perform(get("/isp").param("email", ispList.get(0).getEmail())
                .accept(contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.id", is(ispList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.companyName", is(ispList.get(0).getCompanyName())))
                .andExpect(jsonPath("$.website", is(ispList.get(0).getWebsite())))
                .andExpect(jsonPath("$.email", is(ispList.get(0).getEmail())));
    }

    @Test
    public void ispNotSearchedByEmail() throws Exception {
        mockMvc.perform(get("/isp/search").param("email", "not@existing.email")
                .accept(contentType))
                .andExpect(status().isNotFound());
    }

    @Test
    public void singleISPSearchedByEmail() throws Exception {
        mockMvc.perform(get("/isp/search").param("email", ispList.get(0).getEmail().substring(4, 19))
                .accept(contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id", is(ispList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.[0].companyName", is(ispList.get(0).getCompanyName())))
                .andExpect(jsonPath("$.[0].website", is(ispList.get(0).getWebsite())))
                .andExpect(jsonPath("$.[0].email", is(ispList.get(0).getEmail())));
    }

    @Test
    public void multipleISPsSearchedByEmail() throws Exception {
        mockMvc.perform(get("/isp/search").param("email", ispList.get(1).getEmail().substring(0, 4))
                .accept(contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
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
                .andExpect(jsonPath("$.[2].email", is(ispList.get(2).getEmail())));
    }

    @Test
    public void ispList() throws Exception {
        mockMvc.perform(get("/isp/list")
                .accept(contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
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
    public void createISPSuccess() throws Exception {
        String ispJson = toJson(new ISP("New ISP", "http://wibsite.com", "new@isp.email"));
        this.mockMvc.perform(post("/isp/register")
                .contentType(contentType)
                .content(ispJson))
                .andExpect(status().isCreated());
    }

    @Test
    public void createISPFailure() throws Exception {
        String ispJson = toJson(new ISP("New ISP", "http://wibsite.com", "noc.ebo-enterprises@com"));
        this.mockMvc.perform(post("/isp/register")
                .contentType(contentType)
                .content(ispJson))
                .andExpect(status().isUnprocessableEntity());
    }

    private String toJson(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
