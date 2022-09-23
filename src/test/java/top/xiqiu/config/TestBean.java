package top.xiqiu.config;

import top.xiqiu.north.annotation.Bean;
import top.xiqiu.north.annotation.Configuration;

@Configuration
public class TestBean {

    @Bean
    public User userBean() {
        User user = new User();
        user.name = "tom north";

        return user;
    }

}
