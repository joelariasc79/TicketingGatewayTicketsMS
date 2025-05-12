
package com.ticketing.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	UserDetailsService userDetailsService;
	

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
//	@Bean
//	public UserDetailsService userDetailsService() {
//		UserDetails user =
//			 User.withDefaultPasswordEncoder()
//				.username("user")
//				.password("password")
//				.roles("USER")
//				.build();
//
//		return new InMemoryUserDetailsManager(user);
//	}
	


//	@Bean 
//	public SecurityFilterChain apiFilterChain2(HttpSecurity http) throws Exception {
//		http
//			.apply(MyCustomDsl.customDsl())
//			.flag(true).and()
//			.authorizeRequests()
//				.requestMatchers("/", "/home").permitAll().and()
//			      .exceptionHandling().accessDeniedPage("/accessDeniedPage").and()
//			.authorizeRequests()
//				.requestMatchers("/userForm", "/submitUser", "/form","/submitForm").hasAnyAuthority("ADMIN").and()
//		.formLogin()
//			.loginPage("/login")
//			.defaultSuccessUrl("/userForm").permitAll().and()
//		.logout()
//		.logoutSuccessUrl("/")
//        .invalidateHttpSession(true)
//        .deleteCookies("JSESSIONID")
//        .permitAll();
//		
//		return http.build();
//	}
	
	
//	@Bean
//	public SecurityFilterChain apiFilterChain2(HttpSecurity http) throws Exception {	
//	    http
//	         .apply(MyCustomDsl.customDsl())
//	         .flag(true).and()
//		    .authorizeRequests()
////		    	.requestMatchers("/**", "/home","/create", "/admin/**","/html/**", "/static/**", "/error").permitAll().anyRequest().authenticated()
////			    .requestMatchers("/**","/home","/html/**", "/admin/roles/all", "/edit/**", "/error").permitAll().anyRequest().authenticated()
////		        .requestMatchers("/home", "/create", "/edit", "/edit/*", "/edit/*/data" ,"/html/**", "/admin/roles/all", "/error").permitAll().anyRequest().authenticated()
////		    	.requestMatchers("/home", "/create", "/edit/**", "/html/**", "/html/signup.html/1", "/admin/roles/all", "/error", "/save").permitAll().anyRequest().authenticated()
////		    	.requestMatchers("/**","/home", "/create", "/edit/**", "/html/**", "/admin/roles/all", "/error", "/save").permitAll().anyRequest().authenticated() // Keep existing static path
//		    	.requestMatchers("/**","/home", "/create", "/admin/roles/all", "/error", "/save").permitAll().anyRequest().authenticated() 
//		    	.and()
//			    .exceptionHandling().accessDeniedPage("/accessDeniedPage")
//			    
//			    .and()
//	        .formLogin()
//	            .loginPage("/login.html")
//	            .defaultSuccessUrl("/userForm").permitAll()
//	            .and()
//	        .logout()
//	            .logoutSuccessUrl("/")
//	            .invalidateHttpSession(true)
//	            .deleteCookies("JSESSIONID")
//	            .permitAll();
//
//	    return http.build();
//	}	
	
	
	@Bean
	public SecurityFilterChain apiFilterChain2(HttpSecurity http) throws Exception {	
	    http
	         .apply(MyCustomDsl.customDsl())
	         .flag(true).and()
		    .authorizeRequests()
		    	.requestMatchers("/**", "/test","/home", "/create", "/admin/users", "/admin/users/list" ,"/admin/managers/all", "/admin/roles/all", "/admin/departments/all", "/admin/projects/all", "/error", "/save").permitAll().anyRequest().authenticated() 
		    	.and()
			    .exceptionHandling().accessDeniedPage("/accessDeniedPage")
			    
			    .and()
	        .formLogin()
	            .loginPage("/login")
	            .defaultSuccessUrl("/admin/users").permitAll()
	            .and()
	        .logout()
	            .logoutSuccessUrl("/")
	            .invalidateHttpSession(true)
	            .deleteCookies("JSESSIONID")
	            .permitAll();

	    return http.build();
	}	
	
	
//	@Bean
//	public SecurityFilterChain apiFilterChain2(HttpSecurity http) throws Exception {
//	    http
//	        .authorizeRequests()
//	        .requestMatchers("/**").permitAll()
//	        .anyRequest().authenticated()
//	        .and()
//	        .formLogin().loginPage("/html/login.html").permitAll()
//	        .defaultSuccessUrl("/userForm").permitAll()
//	        .and()
//	        .logout().permitAll();
//	    return http.build();
//	}

}
