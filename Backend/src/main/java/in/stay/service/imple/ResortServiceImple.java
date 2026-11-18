package in.stay.service.imple;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import in.stay.dto.ResortDto;
import in.stay.entity.Resort;
import in.stay.entity.Users;
import in.stay.exception.ResortException;
import in.stay.exception.UserException;
import in.stay.repo.ResortRepo;
import in.stay.request.AddResortRequest;
import in.stay.service.CustomUserDetails;
import in.stay.service.ICloudinaryService;
import in.stay.service.IresortService;
@Service
public class ResortServiceImple implements IresortService{
	@Autowired
	private ResortRepo resortRepo;
	@Autowired
	private ResortRepo rRepo;
	
	@Autowired
	private ModelMapper mapper;
	
	
	
	@Autowired
	private ICloudinaryService cloudinaryService;

	@Override
	public Resort createResort(AddResortRequest request, Users user) {
		Resort r=rRepo.findByName(request.getName());
		//if we hv resort with same name ->exception
		if(r!=null) {
			throw new UserException("Resort name Already Exists", HttpStatus.CONFLICT);
		}
		
		Resort resortEntity=mapper.map(request, Resort.class);
		resortEntity.setCreatedDate(LocalDateTime.now());
		resortEntity.setUser(user);
		
		return rRepo.save(resortEntity);
	}


	@Override
	public void uploadResortImage(Integer resortId, Users user, MultipartFile imageFile, String isChanged) {
		
		Resort findResort=resortRepo.findById(resortId).get();
	    if (findResort == null) {
	        throw new ResortException("Resort not found", HttpStatus.NOT_FOUND);
	    }

	    // Verify that this resort belongs to the logged-in owner
	    if (findResort.getUser() == null ||
	        findResort.getUser().getUserId()!=(user.getUserId())) {

	        throw new UserException(
	            "You can only update your own resort",
	            HttpStatus.FORBIDDEN
	        );
	    }
		

	    if (imageFile != null && imageFile.getSize() > 2 * 1024 * 1024) {
	        throw new ResortException("Image size must be less than 2MB", HttpStatus.BAD_REQUEST);
	    }

	    if(isChanged.equalsIgnoreCase("true"))
	    {
	    		 if (findResort.getResortImgUrlPublicId() != null) {
		 	        cloudinaryService.deleteFile(findResort.getResortImgUrlPublicId());
		 	        findResort.setResortImgUrl(null);
		 	        findResort.setResortImgUrlPublicId(null);
		 	    }
	    		 
	    		 if(imageFile != null)
	    		 {
	    			Map uploadResult = cloudinaryService.uploadFile(imageFile);
	 		        findResort.setResortImgUrl((String) uploadResult.get("secure_url"));
	 		        findResort.setResortImgUrlPublicId((String) uploadResult.get("public_id"));
	    			   
	    		 }        
	    }
	    resortRepo.save(findResort);	
	 
		
	}

	@Override
	public void updateResortDetails(Integer id, AddResortRequest updateResort, CustomUserDetails customUserDetails) {
		Resort resort=resortRepo.findById(id).get();
		if(resort==null) {
			throw new ResortException("resort not found", HttpStatus.NOT_FOUND);
		}
		
		if(resort.getUser().getUserId()!= customUserDetails.getUser().getUserId()) {
			throw new UserException("You can only update your Resort Data", HttpStatus.BAD_REQUEST);
		}
		
		resort.setName(updateResort.getName());
		resort.setFacilities(updateResort.getFacilities());
		resort.setAmount(updateResort.getAmount());
		resort.setDescription(updateResort.getDescription());
		resort.setLocation(updateResort.getLocation());
		resortRepo.save(resort);	
		
	}

	@Override
	public void deleteResortByOwner(Integer id, CustomUserDetails customUserDetails) {
         Resort resort=resortRepo.findById(id).get();
		
		if(resort==null) {
			throw new ResortException("Resort not found", HttpStatus.NOT_FOUND);
		}

		if(resort.getUser().getUserId()!= customUserDetails.getUser().getUserId()) {
			throw new UserException("You can only delete your Resort Data", HttpStatus.BAD_REQUEST);
		}
		resortRepo.deleteById(id);

		
	}

	@Override
	public ResortDto getResortDto(Integer id) {
		Resort resort=resortRepo.findById(id).get();
		if(resort==null) {
			throw new ResortException("Resort Not Found", HttpStatus.NOT_FOUND);
		}		
		ResortDto resResortDTO=mapper.map(resort, ResortDto.class);
		
		return resResortDTO;
	}

	@Override
	public Page<ResortDto> findAllResortDtos(String location, Pageable pageable) {
		Page<Resort> resorts =null;
		if(location==null||location.isEmpty()) {
			resorts =resortRepo.findAll(pageable);
		}else {
				resorts = resortRepo.findByLocation(location, pageable);
		}
		
		//Page<Resort> resorts//= findAll(location,pageable);
		Page<ResortDto> resortResponse=resorts.map(resort->{
			ResortDto resortDto=new ResortDto();
			resortDto.setResortId(resort.getResortId());
			resortDto.setName(resort.getName());
			resortDto.setResortImgUrl(resort.getResortImgUrl());
			resortDto.setResortImgUrlPublicId(resort.getResortImgUrlPublicId());
			resortDto.setCreatedDate(resort.getCreatedDate());
			resortDto.setAmount(resort.getAmount());
			resortDto.setDescription(resort.getDescription());
			resortDto.setFacilities(resort.getFacilities());
			resortDto.setLocation(resort.getLocation());
			return resortDto;
			
		});
		
		return resortResponse;
	}

	@Override
	public List<ResortDto> findResortDtosByUserId(int id) {
		List<Resort> ownerResorts= resortRepo.getResortsByUserId(id);
		
		List<ResortDto> resortDtos = ownerResorts.stream()
		        .map(resort -> mapper.map(resort, ResortDto.class))
		        .toList();
		return resortDtos;
	}

	
	
//	@Override
//	public Page<Resort> findAll(int page, int limit) {
//		Pageable pageable = PageRequest.of(page - 1, limit);
//	    return resortRepo.findAll(pageable);
//	}


}
