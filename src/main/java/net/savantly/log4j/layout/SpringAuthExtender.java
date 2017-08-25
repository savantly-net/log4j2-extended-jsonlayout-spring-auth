package net.savantly.log4j.layout;

import org.apache.logging.log4j.core.layout.ExtendedJsonAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SpringAuthExtender extends ExtendedJsonAdapter {
	
	private static final String AUTHENTICATION_KEY = "authentication";

	public SpringAuthExtender() {
		super();
		getMixedFields().put(AUTHENTICATION_KEY, getAuthentication());
	}

	/**
	 * Override this in your subclass to customize the authentication object in the log event
	 * @return
	 */
	protected Object getAuthentication() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null){
			return null;
		} else {
			return new AuthenticationPojo(authentication);
		}
	}
	
	private class AuthenticationPojo {
		
		private String username;

		public AuthenticationPojo(Authentication authentication) {
			username = authentication.getName();
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}
	}
}
