/*
 * Copyright 2021 Toshiki Iga
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.oiyokan.initializr.authn;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * `/initializr` 以下に BASIC認証を設定.
 */
@EnableWebSecurity
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OiyokanWebAuthnAdapter extends WebSecurityConfigurerAdapter {
    private static final String USER = "admin";
    private static final String PASSWD = "passwd123";
    private static final String ROLES = "USER";

    private static final String PATH = "/initializr";
    private static final String REALMNAME = "REST API Server";

    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser(USER).password(PASSWD).roles(ROLES);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.regexMatcher("^" + PATH + ".*");
        http.authorizeRequests().anyRequest().authenticated();

        BasicAuthenticationEntryPoint authenticationEntryPoint = entryPoint();
        http.addFilter(authnFilter()).exceptionHandling().authenticationEntryPoint(authenticationEntryPoint);
    }

    private BasicAuthenticationEntryPoint entryPoint() {
        BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName(REALMNAME);
        return entryPoint;
    }

    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public UserDetailsService userDetailsServiceBean() throws Exception {
        return super.userDetailsServiceBean();
    }

    @Override
    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Primary
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManager();
    }

    private BasicAuthenticationFilter authnFilter() throws Exception {
        return new BasicAuthenticationFilter(authenticationManagerBean());
    }
}
