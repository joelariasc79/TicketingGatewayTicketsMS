
package com.ticketing.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ticketing.domain.Role;
import com.ticketing.domain.User;

// Does this populate User and user_roles; tables?

@Service
public class UserDetailServiceImpl implements UserDetailsService {

	@Autowired
	UserService userService;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userService.findByUserName(username);
		if(user == null) {
			throw new UsernameNotFoundException(username);
		}
		Set<GrantedAuthority> ga = new HashSet<>();
		Set<Role> roles = user.getRoles();
		for (Role role : roles) {
			System.out.println("role.getRoleName()" + role.getRoleName());
			ga.add(new SimpleGrantedAuthority(role.getRoleName().toString()));
		}

		return new org.springframework.security.core.userdetails.User(user.getUserName(), user.getUserPassword(), ga);
	}

}
