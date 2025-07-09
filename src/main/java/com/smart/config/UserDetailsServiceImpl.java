package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.smart.dao.UserRepository;
import com.smart.entities.User;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		// Fetching user from database

		User user = userRepository.getUserByUserName(username);
		if(user==null) {
			throw new UsernameNotFoundException("could not username !!");
		}
		
		CustomUserDetails customUserDetails = new CustomUserDetails(user);
		return customUserDetails;
	}
	
//	@Service
//	public class UserDetailsServiceImpl implements UserDetailsService {
//
//	    @Autowired
//	    private UserRepository userRepository;
//
//	    @Override
//	    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//	        User user = userRepository.findByEmail(email)
//	            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
//	        return new CustomUserDetails(user);
//	    }
//	}

}
