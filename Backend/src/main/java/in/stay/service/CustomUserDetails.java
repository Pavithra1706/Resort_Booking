package in.stay.service;
/* Spring Security expects user information in a particular format called: UserDetails  
 * But your database has its own entity:User
 * These are two different things.
our project therefore creates:  CustomUserDetails   -> to connect them.

Spring Security doesn't directly work with your custom Users entity. It expects an object implementing: UserDetails

My CustomUserDetails class will provide the user information Spring Security needs."

CustomUserDetails
       │
       └── user → Users
                    │
                    ├── userId
                    ├── name
                    ├── email
                    ├── role
                    └── ...
 * 
 */
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import in.stay.entity.Users;
public class CustomUserDetails implements UserDetails {

	private static final long serialVersionUID = 1L;
	
	
	private Users user; // you are storing your database user inside CustomUserDetails.
	
	public CustomUserDetails(Users user)
	{
		this.user = user;           //Store the Users object received by the constructor inside this class.
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole())); //creates a Spring Security authority object.
	}                       //This method tells Spring Security:

                                        //What role does this user have?

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public Users getUser()
	{
		return user;
	}

}

