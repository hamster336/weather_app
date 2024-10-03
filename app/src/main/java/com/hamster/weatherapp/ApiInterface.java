package com.hamster.weatherapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    // Define the GET request method for weather data
    @GET("weather")
    Call<WeatherApp> getWeatherData(
            @Query("q") String city,       // City parameter
            @Query("appid") String appid,  // API key
            @Query("units") String units   // Units of measurement (metric, imperial, etc.)
    );
}
