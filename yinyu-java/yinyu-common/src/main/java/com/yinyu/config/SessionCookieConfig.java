package com.yinyu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class SessionCookieConfig {

    @Bean
    public DefaultCookieSerializer defaultCookieSerializer(
            @Value("${server.servlet.session.cookie.name:YINYU_SESSION}") String cookieName,
            @Value("${server.servlet.session.cookie.max-age:604800}") int maxAgeSeconds
    ) {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName(cookieName);
        serializer.setCookiePath("/");
        serializer.setCookieMaxAge(maxAgeSeconds);
        serializer.setSameSite("Lax");
        return serializer;
    }
}
