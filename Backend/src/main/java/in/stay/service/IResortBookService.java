package in.stay.service;

import java.util.List;

import in.stay.dto.ResortBookDto;
import in.stay.entity.BookResort;
import in.stay.entity.ResortReview;
import in.stay.entity.Users;
import in.stay.request.ResortBookRequest;

public interface IResortBookService {
	
	public BookResort bookResort(ResortBookRequest request);
	
	public List<ResortBookDto> getMyBookingDtos(Users user);
	
	public void cancelBookingWithEmail(Integer bookingId,Users actor);
	
	List<ResortBookDto> getAdminBookingDtos(Integer adminId);
	
	public void stayCompletedWithEmail(Integer bookingId, Users owner);
	  
	
	
}
