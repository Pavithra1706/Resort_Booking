package in.stay.service.imple;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import in.stay.dto.ResortReviewDto;
import in.stay.entity.BookResort;
import in.stay.entity.Resort;
import in.stay.entity.ResortReview;
import in.stay.exception.ResortException;
import in.stay.repo.BookResortRepo;
import in.stay.repo.ResortRepo;
import in.stay.repo.ResortReviewRepo;
import in.stay.repo.UserRepo;
import in.stay.request.AddReviewRequest;
import in.stay.service.IResortReviewService;
import in.stay.service.IresortService;
@Service
public class ResortReviewImple implements IResortReviewService{
	
	@Autowired
	private ResortReviewRepo reviewRepo;
	
	@Autowired
	private ResortRepo resortRepo;

	@Autowired
	private UserRepo userRepo;
	
	
	@Autowired
	private IresortService resortService;

	@Autowired
	private BookResortRepo bookingRepo;
	
	@Autowired
	private ModelMapper mapper;




	@Override
	public ResortReview addReview(AddReviewRequest reviewrequest) {
        Resort resort=resortRepo.findById(reviewrequest.getResortId()).get();
        		         //for this resort we r reviewing
		
		if(resort==null) {
			throw new ResortException("Resort not found", HttpStatus.NOT_FOUND);
		}
		
		ResortReview resortReview=new ResortReview();
		resortReview.setRating(reviewrequest.getRating());
		resortReview.setReview(reviewrequest.getReview());
		resortReview.setResort(resortRepo.findById(reviewrequest.getResortId()).get());
		resortReview.setUser(userRepo.findById(reviewrequest.getUserId()).get());
		resortReview.setCreatedAt(LocalDateTime.now());
		
		ResortReview res= reviewRepo.save(resortReview);  // storing in resort_review Table
		//after review added now i need to update booking table because as of now review is null there
		int bookingId=reviewrequest.getBookingId();
        BookResort bookedResort=bookingRepo.findById(bookingId).get();
        if(bookedResort!=null) {
        	bookedResort.setReview(res);    // storing review id in book_resort table 
        }
        bookingRepo.save(bookedResort);
        
		return res;
	}

	@Override
	public void editReview(Integer reviewId, AddReviewRequest editRequest) {
		ResortReview resortReview=reviewRepo.findById(reviewId).get();
        //( Resort_review table details)  
         if(resortReview==null) {
	   throw new ResortException("Review not found", HttpStatus.NOT_FOUND);
        }
        Resort resort=resortRepo.findById(editRequest.getResortId()).get();
           if(resort==null) {
	          throw new ResortException("Resort Not Found", HttpStatus.NOT_FOUND);
           }

	resortReview.setRating(editRequest.getRating());
	resortReview.setReview(editRequest.getReview());
	resortReview.setResort(resort);
	resortReview.setUser(userRepo.findById(editRequest.getUserId()).get());
	resortReview.setCreatedAt(LocalDateTime.now());
	
	reviewRepo.save(resortReview);	
			
	}

	@Override
	public ResortReview getReviewById(Integer reviewId) {
		ResortReview review =reviewRepo.findById(reviewId).get();
		if(review==null) {
			throw new ResortException("review not found", HttpStatus.NOT_FOUND);
		}
		return review;
	}

	@Override
	public void deleteReviewWithCheck(Integer reviewId) {
		ResortReview review=reviewRepo.findById(reviewId).get();
		if(review==null) {
			throw new ResortException("Review not found to delete", HttpStatus.NOT_FOUND);
		}
		
		//getting resort info which user wants to delete review from
		Resort resort= resortRepo.findById(review.getResort().getResortId()).get(); // ? Doubt can't we use only getResortId() instead of already written in code 
		
		if(resort==null) {
			throw new ResortException("resort not found", HttpStatus.NOT_FOUND);
		}
		reviewRepo.deleteById(reviewId);	
		
	}

	@Override
	public List<ResortReviewDto> getResortReviewDtosByOwnerId(Integer ownerId) {
List<ResortReview> resortreviews=  reviewRepo.getReviewByOwnerId(ownerId);
		
		if(resortreviews==null) {
			throw new ResortException("No review for your resorts", HttpStatus.NOT_FOUND);
		}
		List<ResortReviewDto> reviewsDtos=resortreviews.stream()
				.map(review->mapper.map(review, ResortReviewDto.class)).toList();
		return reviewsDtos;
	}

	
}
