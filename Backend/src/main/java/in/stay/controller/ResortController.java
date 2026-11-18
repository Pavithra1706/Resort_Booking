package in.stay.controller;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import in.stay.dto.ResortDto;
import in.stay.entity.Resort;
import in.stay.entity.Users;
import in.stay.exception.ResortException;
import in.stay.exception.UserException;
import in.stay.repo.ResortRepo;
import in.stay.request.AddResortRequest;
import in.stay.response.ApiResponse;
import in.stay.service.CustomUserDetails;
import in.stay.service.ICloudinaryService;
import in.stay.service.IresortService;
import jakarta.validation.Valid;
@RestController
@RequestMapping("stay/resort")
public class ResortController {
	
	@Autowired
	private IresortService rService;
	
	//--------------------------add resort
	
	@PostMapping("/add")
	@PreAuthorize("hasRole('RESORT_OWNER')")
	ResponseEntity<?> createResort(@AuthenticationPrincipal CustomUserDetails userDetails,
			@Valid @RequestBody AddResortRequest resort,
			BindingResult bindingResult){
		if (bindingResult.hasErrors()) {
			throw new UserException("Invalid resort Input", HttpStatus.BAD_REQUEST);
		}
		if(userDetails==null) {
			throw new UserException("Login first", HttpStatus.NOT_FOUND);
		}
		
		Resort result=rService.createResort(resort, userDetails.getUser());
		
		Map<String, Object> response = new HashMap<>();
		response.put("status", "success");
		response.put("message", "Resort added Successfully");
		response.put("resort", result);
		return new ResponseEntity<>(response, HttpStatus.OK);	
		
	}
	
	//------------------to upload resort imgs 
	
	@PutMapping(value = "/upload/{resortId}" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
	@PreAuthorize("hasRole('RESORT_OWNER')")
	public ResponseEntity<ApiResponse<?>> updateResort(
	        @AuthenticationPrincipal CustomUserDetails customUserDetails,
	        @RequestParam(name = "img" ,required = false) MultipartFile imageFile,
	        @RequestParam String isChanged,
	        @PathVariable Integer resortId // Users user

	){
		 if (customUserDetails.getUser()==null) {
		        throw new UserException("Unauthorized", HttpStatus.UNAUTHORIZED);
		    }
		    
		 //findResort.getUser().getUserId()
//		 if(resortId!= customUserDetails.getUser().getUserId()) {
//				throw new UserException("You can only update your Resort Data", HttpStatus.BAD_REQUEST);
//			}
		 
		rService.uploadResortImage(resortId, customUserDetails.getUser(), imageFile, isChanged);
		    
		return ResponseEntity.ok(new ApiResponse<>("success", "Resort img Uploaded Successfully", null));
	}	
	
	//---------------------to update the resort data
	
	@PutMapping("/updateResort/{id}")
	@PreAuthorize("hasRole('RESORT_OWNER')")
	ResponseEntity<ApiResponse<?>> updateResort(
			@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@PathVariable Integer id,
			@Valid @RequestBody AddResortRequest updateResort,
			BindingResult bindingResult){  
		if(bindingResult.hasErrors()) {
			throw new ResortException("Invalid Data", HttpStatus.BAD_REQUEST);
		}
		if (customUserDetails.getUser()==null) {
	        throw new UserException("Unauthorized", HttpStatus.UNAUTHORIZED);
	    }
		rService.updateResortDetails(id, updateResort, customUserDetails);
		
		return ResponseEntity.ok(new ApiResponse<>("success","resort updated successfully",null));
	}
	
	//to delete resort 
	@DeleteMapping("/delete/{id}")
	@PreAuthorize("hasRole('RESORT_OWNER')")
	ResponseEntity<ApiResponse<?>> deleteResort(@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@PathVariable Integer id){
		if (customUserDetails.getUser()==null) {
	        throw new UserException("Unauthorized", HttpStatus.UNAUTHORIZED);
	    }
		
		rService.deleteResortByOwner(id, customUserDetails);

		
		return ResponseEntity.ok(new ApiResponse<>("Success","Resort Deleted Successfully",null));
	}
	
	//get particular resort
	@GetMapping("/getResort/{id}")
	ResponseEntity<ApiResponse<?>> getResortById(@PathVariable Integer id,
			@RequestParam(required = false, defaultValue = "1") Integer page,
			@RequestParam(required = false, defaultValue = "10") Integer limit){
		
		ResortDto resResortDTO = rService.getResortDto(id);
			
		return ResponseEntity.ok(new ApiResponse<>("success", "Resort found", resResortDTO));
	}
	
	//get all resorts
	@GetMapping("/allResort")
	ResponseEntity<ApiResponse<?>> getAllResort(@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@RequestParam(required = false, defaultValue = "") String location,
			@RequestParam(required = false, defaultValue = "1") Integer page,
			@RequestParam(required = false, defaultValue = "10") Integer limit){
		Pageable pageable = PageRequest.of(page, limit, Sort.by("resortId").descending());
		
		 Page<ResortDto> resortResponse = rService.findAllResortDtos(location, pageable);
			
		return ResponseEntity.ok(new ApiResponse<>("success", "Resorts data", resortResponse));
	}
	
	
	//taking admin id to fetch the resorts belongs to that owner
	@GetMapping("/adminResorts/{id}")
	@PreAuthorize("hasRole('RESORT_OWNER')")
	ResponseEntity<ApiResponse<?>> getResortsOfOwner(@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@PathVariable Integer id){
		
		    if (customUserDetails.getUser()==null) {
	            throw new UserException("Unauthorized", HttpStatus.UNAUTHORIZED);
	        }
			if(customUserDetails.getUser().getUserId()!=id) {
				throw new UserException("you can only see your resorts", HttpStatus.BAD_REQUEST);
			}
			List<ResortDto> resortDtos = rService.findResortDtosByUserId(id);
			
		    return ResponseEntity.ok(new ApiResponse<>("success", "Resorts data", resortDtos));
		
	}
}
