package in.stay.controller;

import java.util.Map;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import in.stay.dto.UserDto;
import in.stay.exception.UserException;
import in.stay.request.UserSigninRequest;
import in.stay.request.UserSignupRequest;
import in.stay.response.ApiResponse;
import in.stay.service.CustomUserDetails;
import in.stay.service.IuserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("stay/auth")
public class AuthController {

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private IuserService uservice;

    //------------------------------SignUp---------------------
    
    @PostMapping("/user/signup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> userSignUp(
            @Valid @RequestBody UserSignupRequest signupRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors())
            throw new UserException("Invalid User Input", HttpStatus.BAD_REQUEST);

        Map<String, Object> data = uservice.signUp(signupRequest);
        
        ApiResponse<Map<String, Object>> response =
                new ApiResponse<>("success", "Account created successfully", data);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    //------------------------------SignIn---------------------
    
    @PostMapping("/user/signin")
    public ResponseEntity<?> userSignin(
            @Valid @RequestBody UserSigninRequest signinRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors())
            throw new UserException("Invalid User Input", HttpStatus.BAD_REQUEST);

        Map<String, Object> data = uservice.signIn(signinRequest);
        ApiResponse<Map<String, Object>> response =
                new ApiResponse<>("success", "login successfully", data);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    
    //------------------------------GetProfile---------------------
    
    @GetMapping("/user/profile/{id}")
    public ResponseEntity<ApiResponse<?>> getUserProfile(@PathVariable Integer id) {

        UserDto userDTO = uservice.getUserProfile(id);
        return ResponseEntity.ok(new ApiResponse<>("success", "Profile Data", userDTO));
    }

    //------------------------------Update Profile---------------------
    
    @PutMapping(value = "/user/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> updateUserProfile(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(name = "img", required = false) MultipartFile imageFile,
            @RequestParam String name, @RequestParam String phoneNumber,
            @RequestParam String isChanged, @RequestParam Integer userId) {

        if (customUserDetails == null)
            throw new UserException("UNAUTHORIZED", HttpStatus.UNAUTHORIZED);

        uservice.updateUserProfile(userId, name, phoneNumber, isChanged, imageFile);

        return ResponseEntity.ok(
                new ApiResponse<>("success", "Profile Updated Successfully", null));
    }
    
}