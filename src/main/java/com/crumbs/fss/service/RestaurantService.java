package com.crumbs.fss.service;

import com.crumbs.fss.DTO.addRestaurantDTO;
import com.crumbs.fss.entity.*;
import com.crumbs.fss.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public List<Restaurant> getRestaurants(){
        return restaurantRepository.findAll();
    }

    public List<MenuItem> getMenuItems(){
        return menuItemRepository.findAll();
    }

    public List<Restaurant> getRestaurantsByQuery(String query){
        ExampleMatcher customExampleMatcher = ExampleMatcher.matchingAny()
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withIgnorePaths("address");

        Example<Restaurant> example = Example.of(Restaurant.builder().name(query).build(),
                customExampleMatcher);

        return restaurantRepository.findAll(example);
    }

    public List<MenuItem> getMenuItemsByQuery(String query){
        ExampleMatcher customExampleMatcher = ExampleMatcher.matchingAny()
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withIgnorePaths("price", "quantity", "description");

        Example<MenuItem> example = Example.of(MenuItem.builder().name(query).build(),
                customExampleMatcher);
 
        return menuItemRepository.findAll(example);
    }
    public Restaurant addRestaurant(addRestaurantDTO a) throws Exception {
        if(a.getOwnerFirstName()==null) {
            throw new Exception();
        }
        else System.out.println(a.getOwnerFirstName());

        UserDetail userDetail = UserDetail.builder()
                .firstName(a.getOwnerFirstName())
                .lastName(a.getOwnerLastName())
                .email(a.getOwnerEmail())
                .build();

        RestaurantOwner restaurantOwner = RestaurantOwner.builder()
                .userDetail(userDetail)
                .build();

        Location location = Location.builder()
                .street(a.getStreet())
                .city(a.getCity())
                .zipCode(a.getZip())
                .state(a.getState())
                .build();

        Restaurant restaurant = new Restaurant();
        restaurant.setName(a.getName());
        restaurant.setPriceRating(a.getPriceRating());
        restaurant.setLocation(location);
        restaurant.setRestaurantOwner(restaurantOwner);

        userDetailRepository.save(userDetail);
        restaurantOwnerRepository.save(restaurantOwner);
        locationRepository.save(location);

        return restaurantRepository.save(restaurant);

    }
}
