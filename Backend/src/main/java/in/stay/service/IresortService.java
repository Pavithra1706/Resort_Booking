package in.stay.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import in.stay.dto.ResortDto;
import in.stay.entity.Resort;
import in.stay.entity.Users;
import in.stay.request.AddResortRequest;

public interface IresortService {
	public Resort createResort(AddResortRequest request, Users user);

	public void uploadResortImage(
	        Integer resortId,
	        Users user,
	        MultipartFile imageFile,
	        String isChanged
	);
	
	public void updateResortDetails(
	        Integer id,
	        AddResortRequest updateResort,
	        CustomUserDetails customUserDetails);
	
	public void deleteResortByOwner(Integer id, CustomUserDetails customUserDetails);
	
	public ResortDto getResortDto(Integer id);
	
	public Page<ResortDto> findAllResortDtos(String location, Pageable pageable);
	
	public List<ResortDto> findResortDtosByUserId(int id);
}
