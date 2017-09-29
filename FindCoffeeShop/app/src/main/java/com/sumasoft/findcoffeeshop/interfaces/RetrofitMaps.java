package com.sumasoft.findcoffeeshop.interfaces;

import com.sumasoft.findcoffeeshop.model.CoffeeShopResponse;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by sumasoft on 25/09/17.
 */

public interface RetrofitMaps {
    /*
     * Retrofit get annotation with our URL
     * And our method that will return us details of places.
     */
    @GET("api/place/nearbysearch/json?sensor=true&key=AIzaSyBJIuT70ocgy52mYfrHiflx4nUCSSjDAjc")
    Call<CoffeeShopResponse> getNearbyPlaces(@Query("type") String type, @Query("location") String location, @Query("radius") int radius);
}
