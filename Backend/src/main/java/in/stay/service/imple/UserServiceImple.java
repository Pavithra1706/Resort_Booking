package in.stay.service.imple;

import java.time.LocalDateTime;
import java.util.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import in.stay.dto.UserDto;
import in.stay.entity.Users;
import in.stay.exception.UserException;
import in.stay.repo.UserRepo;
import in.stay.request.UserSigninRequest;
import in.stay.request.UserSignupRequest;
import in.stay.service.EmailService;
import in.stay.service.ICloudinaryService;
import in.stay.service.IuserService;
import in.stay.utills.JwtUtil;

@Service
public class UserServiceImple implements IuserService {

    @Autowired
    private UserRepo urepo;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ICloudinaryService cloudinaryService;

    @Override
    public Users update(Users user) {
        return urepo.save(user);
    }

    @Override
    public Map<String, Object> signUp(UserSignupRequest signupRequest) {
        Users existingUser = urepo.findByEmail(signupRequest.getEmail());
        if (existingUser != null) throw new UserException("Email Already Exists", HttpStatus.CONFLICT);

        Users user = mapper.map(signupRequest, Users.class);
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        
        user = urepo.save(user);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userid", user.getUserId());
        claims.put("role", user.getRole());

        String token = jwtUtil.generateToken(user.getEmail(), claims);

        try {
            emailService.sendMail(user.getEmail(), "Welcome to Resort Booking",
                "Hello " + user.getName() + ",\n\nYour account has been created successfully."
                + "\n\nRegards,\nResort Team");
        } catch (Exception e) {
            System.err.println("Welcome email failed: " + e.getMessage());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("email", user.getEmail());
        data.put("role", user.getRole());
        return data;
    }

    
    
    @Override
    public Map<String, Object> signIn(UserSigninRequest signinRequest) {
        Users user = urepo.findByEmail(signinRequest.getEmail());
        if (user == null) throw new UserException("User not found", HttpStatus.NOT_FOUND);

        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword()))
            throw new UserException("Invalid password", HttpStatus.UNAUTHORIZED);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userid", user.getUserId());
        claims.put("role", user.getRole());

        String token = jwtUtil.generateToken(user.getEmail(), claims);
        UserDto userDto = mapper.map(user, UserDto.class);
        //userDto.setPassword(null);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", userDto);
        System.out.println(data);
        return data;
    }

    
    
    @Override
    public UserDto getUserProfile(Integer id) {
        Users user = urepo.findById(id)
            .orElseThrow(() -> new UserException("User not found", HttpStatus.NOT_FOUND));

        UserDto userDTO = mapper.map(user, UserDto.class);
        userDTO.setPassword(null);
        return userDTO;
    }

    
    
    @Override
    public void updateUserProfile(Integer userId, String name, String phoneNumber,
                                  String isChanged, MultipartFile imageFile) {
        Users user = urepo.findById(userId)
            .orElseThrow(() -> new UserException("User not found", HttpStatus.NOT_FOUND));

        if (imageFile != null && imageFile.getSize() > 2 * 1024 * 1024)
            throw new UserException("Image size must be less than 2MB", HttpStatus.BAD_REQUEST);

        if ("true".equalsIgnoreCase(isChanged)) {
            if (user.getProfileImgUrlPublicId() != null) {
                cloudinaryService.deleteFile(user.getProfileImgUrlPublicId());
                user.setProfileImgUrl(null);
                user.setProfileImgUrlPublicId(null);
            }

            if (imageFile != null && !imageFile.isEmpty()) {
                Map<String, Object> uploadResult = cloudinaryService.uploadFile(imageFile);
                user.setProfileImgUrl((String) uploadResult.get("secure_url"));
                user.setProfileImgUrlPublicId((String) uploadResult.get("public_id"));
            }
        }

        user.setName(name);
        user.setPhoneNumber(phoneNumber);
        urepo.save(user);
    }
}