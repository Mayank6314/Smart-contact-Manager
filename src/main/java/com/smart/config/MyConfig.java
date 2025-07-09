package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@Configuration
@EnableWebSecurity
public class MyConfig {

	@Autowired
	public UserDetailsServiceImpl userDetailsService;
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public DaoAuthenticationProvider daoAuthenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
		
	}
	
	/// configure method..
	@Bean
	public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration configuration) throws Exception{
		return configuration.getAuthenticationManager();
	}
	
	
    @Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    	  http
              .authorizeHttpRequests((authz) -> authz
              .requestMatchers("/admin/**").hasRole("ADMIN")
              .requestMatchers("/user/**").hasRole("USER")
              .requestMatchers("/**").permitAll()
              .anyRequest().authenticated()
          )
              .authenticationProvider(daoAuthenticationProvider()) // Register the provider
              .formLogin(form -> form
                      .loginPage("/signin") // Your custom login page URL
                      .loginProcessingUrl("/signin") // Form action URL
                      .defaultSuccessUrl("/user/index", true) // Redirect after successful login
                      .failureUrl("/signin?error=true") // Redirect on login failure
                      .permitAll()
                  )
              .logout(logout -> logout
            		    .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET")) // Allow GET logout
            		    .logoutSuccessUrl("/signin?logout=true")
            		    .invalidateHttpSession(true)
            		    .clearAuthentication(true)
            		    .deleteCookies("JSESSIONID")
            		    .permitAll()
            		);

		
		return http.build();
	}
    

}
