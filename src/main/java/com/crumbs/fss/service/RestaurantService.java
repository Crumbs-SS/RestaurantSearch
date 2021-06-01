package com.crumbs.fss.service;

import com.crumbs.fss.DTO.addRestaurantDTO;
import com.crumbs.fss.DTO.updateRestaurantDTO;
import com.crumbs.fss.ExceptionHandling.DuplicateEmailException;
import com.crumbs.fss.ExceptionHandling.DuplicateFieldException;
import com.crumbs.fss.ExceptionHandling.DuplicateLocationException;
import com.crumbs.fss.entity.*;
import com.crumbs.fss.entity.MenuItem;
import com.crumbs.fss.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(rollbackFor = { Exception.class })
public class RestaurantService {

    @Autowired RestaurantRepository restaurantRepository;
    @Autowired MenuItemRepository menuItemRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired LocationRepository locationRepository;
    @Autowired RestaurantCategoryRepository restaurantCategoryRepository;
    @Autowired RestaurantOwnerRepository restaurantOwnerRepository;
    @Autowired UserDetailRepository userDetailRepository;

    public List<Restaurant> getAllRestaurants(){
        return restaurantRepository.findAll();
    }

    public List<Restaurant> getRestaurantOwnerRestaurants(Long id){
        return restaurantRepository.findRestaurantByOwnerID(id);
    }

    public Restaurant addRestaurant(addRestaurantDTO a) {

        String duplicates="";
         if(userDetailRepository.findUserByEmail(a.getEmail())!=null)
            duplicates+="email";

        if(locationRepository.findLocationByStreet(a.getStreet())!=null)
            duplicates+="location";
        if(duplicates!="")
            throw new DuplicateFieldException(duplicates);

        UserDetail userDetail = UserDetail.builder()
                .firstName(a.getFirstName())
                .lastName(a.getLastName())
                .email(a.getEmail())
                .build();

        userDetailRepository.save(userDetail);

        RestaurantOwner restaurantOwner = RestaurantOwner.builder()
                .userDetail(userDetail)
                .build();

        restaurantOwnerRepository.save(restaurantOwner);

        Location location = Location.builder()
                .street(a.getStreet())
                .city(a.getCity())
                .zipCode(a.getZip())
                .state(a.getState())
                .build();

        locationRepository.save(location);

        Restaurant temp = Restaurant.builder()
                .name(a.getName())
                .priceRating(a.getPriceRating())
                .location(location)
                .restaurantOwner(restaurantOwner)
                .build();

        Restaurant restaurant = restaurantRepository.save(temp);
        Long restaurantID = restaurant.getId();

        restaurantCategoryRepository.deleteByRestaurantID(restaurantID);

        List<Category> categories = a.getCategories();
        List<RestaurantCategory> restaurantCategories = new ArrayList<>();

        if(categories!= null && !categories.isEmpty()) {
            categories.forEach(category -> {

                RestaurantCategoryID resCatID = RestaurantCategoryID.builder()
                        .restaurantId(restaurantID)
                        .categoryId(category.getName())
                        .build();

                //create restaurant category
                RestaurantCategory resCat = RestaurantCategory.builder()
                        .id(resCatID)
                        .restaurant(restaurant)
                        .category(category)
                        .build();

                restaurantCategories.add(resCat);
            });
            restaurant.setCategories(restaurantCategories);
        }
        return restaurantRepository.save(temp);
    }
    public Restaurant deleteRestaurant(Long id){

        Restaurant temp = restaurantRepository.findById(id).orElseThrow(() -> new EntityNotFoundException());
        restaurantRepository.deleteById(id);
        locationRepository.deleteById(temp.getLocation().getId());
        restaurantOwnerRepository.deleteById(temp.getRestaurantOwner().getId());
        userDetailRepository.deleteById(temp.getRestaurantOwner().getUserDetail().getId());
        if(menuItemRepository.findById(id).isPresent())
            menuItemRepository.deleteById(id);

        return temp;
    }
    public Restaurant updateRestaurant(Long id, updateRestaurantDTO updateRestaurantDTO){

        Restaurant temp = restaurantRepository.findById(id).orElseThrow(() -> new EntityNotFoundException());

        String duplicates="";
        if(userDetailRepository.findUserByEmail(updateRestaurantDTO.getEmail())!=null)
            duplicates+="email";

        if(locationRepository.findLocationByStreet(updateRestaurantDTO.getStreet())!=null)
            duplicates+="location";
        if(duplicates!="")
            throw new DuplicateFieldException(duplicates);

        // Update User Details
        String firstName = updateRestaurantDTO.getFirstName();
        if(firstName!= null && !firstName.isEmpty())
            temp.getRestaurantOwner().getUserDetail().setFirstName(firstName);

        String lastName = updateRestaurantDTO.getLastName();
        if(firstName!= null && !firstName.isEmpty())
            temp.getRestaurantOwner().getUserDetail().setLastName(lastName);

        String email = updateRestaurantDTO.getEmail();
        if(email!= null && !email.isEmpty())
            temp.getRestaurantOwner().getUserDetail().setEmail(email);

        //Update Restaurant Location
        String street = updateRestaurantDTO.getStreet();
        if(street!= null && !street.isEmpty())
            temp.getLocation().setStreet(street);

        String city = updateRestaurantDTO.getCity();
        if(city!= null && !city.isEmpty())
            temp.getLocation().setCity(city);

        Integer zip = updateRestaurantDTO.getZip();
        if(zip!= null)
            temp.getLocation().setZipCode(zip);

        String state = updateRestaurantDTO.getState();
        if(state!= null && !state.isEmpty())
            temp.getLocation().setState(state);

        //Update Restaurant Details
        String name = updateRestaurantDTO.getName();
        if(name!= null && !name.isEmpty())
            temp.setName(name);

        Integer priceRating = updateRestaurantDTO.getPriceRating();
        if(priceRating!= null)
            temp.setPriceRating(priceRating);

        //delete old categories
        if(!temp.getCategories().isEmpty())
            restaurantCategoryRepository.deleteByRestaurantID(id);

        //replace with new ones
        List<Category> newCategories = updateRestaurantDTO.getCategories();
        List<RestaurantCategory> restaurantCategories = new ArrayList<>();

        if(newCategories!= null && !newCategories.isEmpty()) {
            newCategories.forEach(category -> {
                restaurantCategoryRepository.insertRestaurantCategory(category.getName(),temp.getId());

                RestaurantCategoryID resCatID = RestaurantCategoryID.builder()
                        .restaurantId(temp.getId())
                        .categoryId(category.getName())
                        .build();

                //create restaurant category
                RestaurantCategory resCat = RestaurantCategory.builder()
                        .id(resCatID)
                        .restaurant(temp)
                        .category(category)
                        .build();

                restaurantCategories.add(resCat);
            });
            temp.setCategories(restaurantCategories);
        }

        return restaurantRepository.save(temp);
    }


}
