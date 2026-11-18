package in.stay.service;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import in.stay.dto.UserDto;
import in.stay.entity.Users;
import in.stay.request.UserSigninRequest;
import in.stay.request.UserSignupRequest;

public interface IuserService {
	
    public Map<String, Object> signUp(UserSignupRequest signupRequest);
    public Map<String, Object> signIn(UserSigninRequest signinRequest);
		
    public UserDto getUserProfile(Integer id);
    public void updateUserProfile(Integer userId,String name,String phoneNumber,String isChanged,MultipartFile imageFile);
	public Users update(Users user);
	
}