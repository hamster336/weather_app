package com.hamster.weatherapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.hamster.weatherapp.databinding.ActivityMainBinding;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// dac08d38859e5f580d1a679a8439464a -> API key

public class MainActivity extends AppCompatActivity {
    // Declare ViewBinding as a member variable
    private ActivityMainBinding binding;

    private static final String TAG = "WeatherService";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // preventing the app from being hidden status bar and navigation buttons
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View decorView = getWindow().getDecorView();
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), decorView);

        // Ensure content adapts to system bars
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);



        // Initialize the binding (equivalent to lazy initialization in Kotlin)
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        // Set the content view to the root of the binding
        setContentView(binding.getRoot());
        // call the method to fetch data
        fetchWeatherData("Mahendranagar");
        searchCity();
    }

    private void searchCity(){
        SearchView searchView = binding.searchView;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchWeatherData(query);  // Pass the searched city name

                searchView.clearFocus();  // Clear focus to ensure the keyboard disappears

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Optional: handle text change here
                return false;
            }
        });
    }

    private void fetchWeatherData(String cityName) {
        Retrofit retrofit = new Retrofit. Builder()
                .addConverterFactory(GsonConverterFactory. create())
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .build();

        // Create API interface
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);

        // Make the API call
        Call<WeatherApp> response = apiInterface.getWeatherData(cityName, "dac08d38859e5f580d1a679a8439464a", "metric");

        // Enqueue the request to handle asynchronously
        response.enqueue(new Callback<WeatherApp>() {

            @Override
            public void onResponse(@NonNull Call<WeatherApp> call, @NonNull Response<WeatherApp> response) {
                WeatherApp responseBody = response.body();
                if (response.isSuccessful() && responseBody != null) {
                    // Get temperature and log it
                    String temperature = String.valueOf(responseBody.getMain().getTemp());

                    double tempDouble = Double.parseDouble(temperature);

                    int tempMax = (int) responseBody.getMain().getTempMax();
                    int tempMin = (int) responseBody.getMain().getTempMin();

                    binding.temperature.setText(String.valueOf((int)tempDouble));
//                    binding.temperature.setText(String.valueOf(Integer.parseInt(temperature)));   temp value here is a decimal alue. Since Integer.parseInt() does not support decimal values, it throws a NumberFormatException.
//                    binding.temperature.setText(Integer.parseInt(temperature));   Using this crashes the app because the setText() method expects either a string or a resrc id.When you use int directly, it is interpreted as res id which causes the crash
//                    Log.d("TAG", "MAX Temp: " + tempMax);
//                    Log.d("TAG", "MIN Temp: " + tempMin);

                    binding.tempRange.setText(tempMin + "/" + tempMax + " °C");

                    binding.cityName.setText(cityName);
                    binding.weekDay.setText(dayName());
                    binding.date.setText(date());

                    String condition = "unknown";
                    Weather firstWeather = responseBody.getWeather().get(0); // Get the first element
                    if (firstWeather != null && firstWeather.getMain() != null) {
                        condition = firstWeather.getMain();     // Assign the 'main' value if it's not null
                    }

                    long sunriseUnix = responseBody.getSys().getSunrise();
                    long sunsetUnix = responseBody.getSys().getSunset();

                    Date sunriseTime = new Date(sunriseUnix * 1000L);
                    Date sunsetTime = new Date(sunsetUnix * 1000L);
                    Date currentTime = new Date();

                    setWeatherAnimation(condition, sunriseTime, sunsetTime, currentTime);
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherApp> call, @NonNull Throwable t) {
                // Handle failure case
                Log.e("TAG", "onFailure: Network request failed", t);
            }
        });

    }

    private void setWeatherAnimation(String condition, Date sunriseTime, Date sunsetTime, Date currentTime) {
        switch(condition){
            case "Sunny" : // case "Clear Sky" : case "Clear" :
                        binding.lottieAnimationView2.setAnimation(R.raw.sun);
                        break;

            case "Clouds" : // case "scattered cloudy" : case "Haze" : case "overcast" :
                        binding.lottieAnimationView2.setAnimation(R.raw.cloud);
                        break;

            case "Rain" : // case "heavy rain" : case "moderate rain" : case "light rain" :
                        binding.lottieAnimationView2.setAnimation(R.raw.rain);
                        break;

            case "Snow" :
                        binding.lottieAnimationView2.setAnimation(R.raw.snow);

            default:
                        // Compare the current time with sunrise and sunset times
                        if (currentTime.after(sunriseTime) && currentTime.before(sunsetTime)) {
                            // Show sun animation
                            binding.lottieAnimationView2.setAnimation(R.raw.sun);
                        } else {
                            // Show moon animation
                            binding.lottieAnimationView2.setAnimation(R.raw.moon);
                        }
        }
        binding.lottieAnimationView2.playAnimation();
    }

    public static String date(){
        LocalDate currentDate = LocalDate.now();

        // Format the date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        return currentDate.format(formatter);
    }

    public static String dayName(){
        LocalDate currentDay = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE");
        return currentDay.format(formatter);
    }
}