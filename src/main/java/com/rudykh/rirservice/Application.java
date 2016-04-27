package com.rudykh.rirservice;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * Created by Yaroslav Rudykh on 23.04.2016.
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

    @Bean
    CommandLineRunner init(ISPRepository ispRepository) {
        return new CommandLineRunner() {
            @Override
            public void run(String... strings) throws Exception {
                ispRepository.save(new ISP("0 loss B.V.", "https://www.ripe.net/membership/indices/data/nl.0loss.html", "ripe@0loss.com"));
                ispRepository.save(new ISP("e-BO Enterprises B.V.", "https://www.ripe.net/membership/indices/data/be.ebo-enterprises.html", "noc.ebo-enterprises@com"));
                ispRepository.save(new ISP("Data Comfort BV", "https://www.ripe.net/membership/indices/data/nl.datacomfort.html", "arjan.kunstman@datacomfort.nl"));
                ispRepository.save(new ISP("ABN AMRO Bank N.V.", "https://www.ripe.net/membership/indices/data/nl.abn-amro.html", "sdm.infra.netwerk@nl.abnamro.com"));
                ispRepository.save(new ISP("baten-lastendienst Logius", "https://www.ripe.net/membership/indices/data/nl.logius.html", "ripe-admin@logius.nl"));
                ispRepository.save(new ISP("Some ISP", "https://www.ripe.net/", "mail1@some.isp"));
                ispRepository.save(new ISP("Some ISP", "https://www.ripe.net/", "mail2@some.isp"));
                ispRepository.save(new ISP("Some ISP", "", "mail3@some.isp"));
                ispRepository.save(new ISP("1Some ISP", "https://www.ripe.net/", "mail4@some.isp"));
            }
        };
    }

    @Bean(destroyMethod = "close")
    DataSource dataSource(Environment env) throws SQLException {
        HikariConfig dataSourceConfig = new HikariConfig();
        dataSourceConfig.setDriverClassName(env.getRequiredProperty("db.driver"));
        dataSourceConfig.setJdbcUrl(env.getRequiredProperty("db.url"));
        dataSourceConfig.setUsername(env.getRequiredProperty("db.username"));
        dataSourceConfig.setPassword(env.getRequiredProperty("db.password"));

        return new HikariDataSource(dataSourceConfig);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
