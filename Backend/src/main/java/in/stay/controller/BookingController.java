package in.stay.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.stay.exception.ResortBookingException;
import in.stay.exception.ResortException;
import in.stay.exception.UserException;
import in.stay.repo.ResortRepo;
import in.stay.repo.UserRepo;
import in.stay.request.ResortBookRequest;
import in.stay.response.ApiResponse;
import in.stay.service.CustomUserDetails;
import in.stay.service.EmailService;
import in.stay.service.IResortBookService;
import in.stay.dto.ResortBookDto;
import in.stay.emuns.Status;
import in.stay.entity.BookResort;
import in.stay.entity.Resort;
import in.stay.entity.Users;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/stay/booking")
public class BookingController {
	
	@Autowired
	private IResortBookService bookService;
	
	@PostMapping("/bookResort")
	@PreAuthorize("hasRole('CUSTOMER')")         //@AuthenticationPrincipal -> Get the currently authenticated user from Spring Security.
	ResponseEntity<ApiResponse<?>> BookResort(                                 // not directly from the URL or request parameter.
	        @AuthenticationPrincipal CustomUserDetails customUserDetails,
	        @Valid @RequestBody ResortBookRequest request,
	        BindingResult bindingResult) {

	    if (bindingResult.hasErrors()) {
	        throw new UserException("Invalid UserInput", HttpStatus.BAD_REQUEST);
	    }

	    if (customUserDetails == null) {
	        throw new UserException("Login First To Book Resort", HttpStatus.BAD_REQUEST);
	    }

	    BookResort booked = bookService.bookResort(request);
	    
	    return ResponseEntity.ok(new ApiResponse<>("success", "Resort Booked Successfully", booked)
	    );
	}

	
	//get booking data of each user
	@GetMapping("/mybookings")
	@PreAuthorize("hasRole('CUSTOMER')")
	ResponseEntity<ApiResponse<?>> getMyBookkings(@AuthenticationPrincipal CustomUserDetails customUserDetails){
		if(customUserDetails==null) {
			throw new UserException("Login First To get Your Booking data", HttpStatus.BAD_REQUEST);
		}
		//if logined user data is present in CustomerUserDetails so getting user data
		Users user=customUserDetails.getUser();
		
		List<ResortBookDto> bookingDtos =
	            bookService.getMyBookingDtos(user);

		return ResponseEntity.ok(new ApiResponse<>("success", "Your Booking Data", bookingDtos));
	}
	
	//cancel the booking by both user and resort owner
	@PutMapping("/cancel/{bookingId}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'RESORT_OWNER')")
	ResponseEntity<ApiResponse<?>> cancelBooking(@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@PathVariable Integer bookingId){
		if(customUserDetails==null) {
			throw new UserException("Login First cancel booking", HttpStatus.BAD_REQUEST);
		}
		bookService.cancelBookingWithEmail(bookingId,customUserDetails.getUser());

		return ResponseEntity.ok(new ApiResponse<>("sucess", "Booking cancelled", null));
	}
	
	
	//to get booking data in resort-owner page
	@GetMapping("/getBookedInfo")
	@PreAuthorize("hasRole('RESORT_OWNER')")
	ResponseEntity<ApiResponse<?>> getBookedResorts(@AuthenticationPrincipal CustomUserDetails customUserDetails){
		if(customUserDetails==null) {
			throw new UserException("Login First to get booking data", HttpStatus.BAD_REQUEST);
		}
		Users user=customUserDetails.getUser();
		List<ResortBookDto> bookingDtos =
	            bookService.getAdminBookingDtos(user.getUserId());
		
		return ResponseEntity.ok(new ApiResponse<>("Sucess", "Booking data fetched", bookingDtos));
	}
	
	
	
	//change to completed after user checkout 
	@PutMapping("/staycompleted/{bookingId}")
	@PreAuthorize("hasRole('RESORT_OWNER')")
	ResponseEntity<ApiResponse<?>> markCompleted(@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@PathVariable Integer bookingId){
		if(customUserDetails==null) {
			throw new UserException("Login First to make changes", HttpStatus.BAD_REQUEST);
		}
		
		

	    bookService.stayCompletedWithEmail(bookingId,customUserDetails.getUser());
	    
		return ResponseEntity.ok(new ApiResponse<>("Sucess","Stay Marked as Completed", null));
	}
	
	

}
