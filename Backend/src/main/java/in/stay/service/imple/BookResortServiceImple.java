package in.stay.service.imple;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import in.stay.dto.ResortBookDto;
import in.stay.emuns.Status;
import in.stay.entity.BookResort;
import in.stay.entity.Resort;
import in.stay.entity.Users;
import in.stay.exception.ResortBookingException;
import in.stay.exception.ResortException;
import in.stay.exception.UserException;
import in.stay.repo.BookResortRepo;
import in.stay.repo.ResortRepo;
import in.stay.repo.UserRepo;
import in.stay.request.ResortBookRequest;
import in.stay.service.EmailService;
import in.stay.service.IResortBookService;
@Service
public class BookResortServiceImple implements IResortBookService{
	
	@Autowired
	private BookResortRepo bookingRepo;
	
	@Autowired
	private ResortRepo resortRepo;

	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private ModelMapper mapper;

	@Autowired
	private EmailService emailService;
	
	@Override
	public BookResort bookResort(ResortBookRequest request) {
	  Resort resort = resortRepo.findById(request.getResortId())
	            .orElseThrow(() -> new ResortException("Resort Not Found", HttpStatus.BAD_REQUEST));

	    Users user = userRepo.findById(request.getUserId())
	            .orElseThrow(() -> new UserException("User Not Found", HttpStatus.BAD_REQUEST));

	    BookResort booked = mapper.map(request, BookResort.class);
	    LocalDate checkIn = request.getCheckInDate();
	    Long days = request.getNoOfDays();
	    
	    //get->checkOut date by using checkIn and NumOfdays
	    LocalDate checkOut = checkIn.plusDays(days);

	    booked.setCheckInDate(checkIn);
	    booked.setCheckOutDate(checkOut);
	    booked.setNoOfDays(days);
	    booked.setBookedAt(LocalDateTime.now());
	    booked.setResort(resort);//which resort booked
	    booked.setUser(user);//who booked

	    //calculate total amount
	    long totalAmount =(long) resort.getAmount() *request.getNumOfPersons() *request.getNoOfDays();

	    booked.setTotalAmount(totalAmount);
	    booked.setStatus(Status.BOOKED);//by default BOOKED
	    BookResort savedBooking = bookingRepo.save(booked);

	    try {
	    	emailService.sendMail(
	    		    request.getEmail(),
	    		    "Resort Booking Confirmed",
	    		    "Hi " + user.getName() +
	    		    "\n\nYour booking at " + resort.getName() +
	    		    " is confirmed from " + checkIn +
	    		    " to " + checkOut +
	    		    "\nTotal Amount: ₹" + totalAmount +
	    		    "\n\nPlease note: Checkout time is 10:00 AM on your checkout day." +
	    		    "\n\nThank you!"
	    		);

	    } catch (Exception e) {
	        System.out.println("Mail not sent");
	    }
	    return savedBooking;
	}

	@Override
	public List<ResortBookDto> getMyBookingDtos(Users user) {
List<BookResort> bookings=bookingRepo.findByUser(user);

		List<ResortBookDto> bookingDtos=bookings.stream().
				map(resort->mapper.map(resort, ResortBookDto.class)).toList();
		return bookingDtos;
	}
	
	@Override
	public void cancelBookingWithEmail(Integer bookingId, Users actor) {
		BookResort bookedresort=bookingRepo.findById(bookingId).get();
		
		//if already cancelled or completed we can't cancel again
		if(bookedresort.getStatus()==Status.CANCELLED) {
			throw new ResortBookingException("Booking already canceled", HttpStatus.CONFLICT);
		}
		if(bookedresort.getStatus()==Status.COMPLETED) {
			throw new ResortBookingException("Stay Completed cant cancel now", HttpStatus.CONFLICT);
		}
		if(resortRepo.findById(bookedresort.getResort().getResortId()) == null) {
			throw new ResortException("Resort Not Found", HttpStatus.NOT_FOUND);
		}
		// CUSTOMER: only cancel their own booking.
	    if (actor.getRole() == in.stay.emuns.Role.CUSTOMER) {

	        if (bookedresort.getUser() == null ||
	        		bookedresort.getUser().getUserId() != actor.getUserId()) {

	            throw new UserException(
	                "You can only cancel your own booking",
	                HttpStatus.FORBIDDEN
	            );
	        }

	    // RESORT_OWNER: only reject bookings for their own resort.
	    } else if (actor.getRole() == in.stay.emuns.Role.RESORT_OWNER) {

	        if (bookedresort.getResort() == null ||
	        		bookedresort.getResort().getUser() == null ||
	        				bookedresort.getResort().getUser().getUserId()
	                != actor.getUserId()) {

	            throw new UserException(
	                "You can only reject bookings for your own resorts",
	                HttpStatus.FORBIDDEN
	            );
	        }

	    } else {
	        throw new UserException(
	            "You are not allowed to cancel this booking",
	            HttpStatus.FORBIDDEN
	        );
	    }

		bookingRepo.ChangeBookingstatus(bookingId, Status.CANCELLED);	
        
        //after cancel send mail to user
        String subject = "Resort Booking Cancelled";
        String text = "Hi " + bookedresort.getUser().getName() + ",\n\n" +
                      "Your booking for " + bookedresort.getResort().getName() + " has been cancelled successfully.";
        emailService.sendMail(bookedresort.getEmail(), subject, text);	
		
	}

	@Override
	public List<ResortBookDto> getAdminBookingDtos(Integer adminId) {
List<BookResort> bookedData=bookingRepo.getAdminBookings(adminId);
		
		List<ResortBookDto> bookingDtos=bookedData.stream().
				map(resort->mapper.map(resort, ResortBookDto.class)).toList();
		return bookingDtos;
	}
	
	@Override
	public void stayCompletedWithEmail(Integer bookingId,Users owner) {
		BookResort bookedResort=bookingRepo.findById(bookingId).get();
		  // Check whether the booking belongs to this owner's resort
	    if (bookedResort.getResort() == null ||
	        bookedResort.getResort().getUser() == null ||
	        		bookedResort.getResort().getUser().getUserId()
	        	    != owner.getUserId()) {

	        throw new UserException(
	            "You can only manage bookings for your own resorts",
	            HttpStatus.FORBIDDEN
	        );
	    }
		
		//if booking cancelled or completed we can't make complete again
		if(bookedResort.getStatus()==Status.CANCELLED) {
			throw new UserException("already Booking canceled You cant change to Completed", HttpStatus.NOT_FOUND);
		}
		if(bookedResort.getStatus()==Status.COMPLETED) {
			throw new UserException("Stay Completed already", HttpStatus.BAD_REQUEST);
		}
		bookingRepo.ChangeBookingstatus(bookingId, Status.COMPLETED);
	    
	    //after completed send mail to give review
	    String subject = "Stay Completed";
	    String text = "Hi " + bookedResort.getUser().getName() + ",\n\n" +
	                  "Your stay at " + bookedResort.getResort().getName() + " has been completed.\n" +
	                  "Please share your experience by giving a review.\n\nThank you!";
	    emailService.sendMail(bookedResort.getUser().getEmail(), subject, text);
	}

	

	
	

}
