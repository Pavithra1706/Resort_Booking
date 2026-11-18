package in.stay.utills;
import java.io.IOException;
import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import in.stay.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
	private final JwtUtil jwtUtil;
	private final CustomUserDetailsService customUserDetailsService;

	public JwtRequestFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.customUserDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		final String authorizationHeader = request.getHeader("Authorization");
		// String path = request.getRequestURI();
//		 if (path.startsWith("/stay/resort/allResort") || 
//			        path.startsWith("/api/stay/public") || 
//			        path.startsWith("/swagger") ||
//			        path.startsWith("/v3/api-docs")) {
//			        chain.doFilter(request, response);
//			        return;
//			    }

		if (authorizationHeader != null) {

			String email = null;
			String jwt = null;                            //Bearer : - The client is presenting this token as its authentication credential.

			if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
				jwt = authorizationHeader.substring(7);
				email = jwtUtil.extractSubject(jwt);
				String role=jwtUtil.extractClaims(jwt).get("role").toString();//getting role from authorizationHeader
			    System.out.println(role);
			}

			if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

				UserDetails userDetails = null;

				userDetails = customUserDetailsService.loadUserByUsername(email);

				Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities() != null
						? userDetails.getAuthorities()
						: null;
                //..............Authentication created...................
				var authToken = new UsernamePasswordAuthenticationToken(
						userDetails, null, authorities); //This creates a Spring Security authentication object.
				                                         //User = Pavithra
				                                         //Role = ROLE_CUSTOMER
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); //This adds information about the current request to the authentication object.
				SecurityContextHolder.getContext().setAuthentication(authToken);   
			}
		}
                      //This request is authenticated as this user, with these authorities."

               //This is the point where the JWT has effectively been converted into a Spring Security authenticated user.
		chain.doFilter(request, response);
	}
}

  //SecurityContextHolder -> Current authenticated user
//Principal = the user who is currently authenticated.
//userDetails ->becomes the principal.
