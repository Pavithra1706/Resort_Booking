package in.stay.service;

import java.util.List;

import in.stay.dto.ResortReviewDto;
import in.stay.entity.ResortReview;
import in.stay.request.AddReviewRequest;

public interface IResortReviewService {

	//public List<ResortReview> getAllReviewsByResort(Integer id); -not found
	
	public ResortReview addReview(AddReviewRequest reviewrequest);
	
	public void editReview(Integer reviewId, AddReviewRequest editRequest);
	
	public ResortReview getReviewById(Integer reviewId);
	
	public void deleteReviewWithCheck(Integer reviewId);
	
	public List<ResortReviewDto> getResortReviewDtosByOwnerId(Integer ownerId);
}
