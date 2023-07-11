package com.example.app.weather.service;

import com.example.app.weather.model.currentweather.CurrentWeatherResponse;
import com.example.app.weather.model.daysweather.MultipleDaysWeatherResponse;
import com.example.app.weather.model.fivedayweather.FiveDayResponse;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {


  @GET("forecast")
  Call<FiveDayResponse> getFiveDaysWeathers(
          @Query("q") String q,
          @Query("units") String units,
          @Query("lang") String lang,
          @Query("appid") String appId
  );

  /**
   * Get current weather of city
   *
   * @param q     String name of city
   * @param units String units of response
   * @param lang  String language of response
   * @param appId String api key
   * @return instance of {@link CurrentWeatherResponse}
   */
  @GET("weather")
  Single<CurrentWeatherResponse> getCurrentWeather(
      @Query("q") String q,
      @Query("units") String units,
      @Query("lang") String lang,
      @Query("appid") String appId
  );

  /**
   * Get five days weather forecast.
   *
   * @param q     String name of city
   * @param units String units of response
   * @param lang  String language of response
   * @param appId String api key
   * @return instance of {@link FiveDayResponse}
   */
  @GET("forecast")
  Single<FiveDayResponse> getFiveDaysWeather(
      @Query("q") String q,
      @Query("units") String units,
      @Query("lang") String lang,
      @Query("appid") String appId
  );

  /**
   * Get multiple days weather
   *
   * @param q     String name of city
   * @param units String units of response
   * @param lang  String language of response
   * @param appId String api key
   * @return instance of {@link MultipleDaysWeatherResponse}
   */
  // @GET("forecast/daily")
  @GET("forecast")
  Single<MultipleDaysWeatherResponse> getMultipleDaysWeather(
      @Query("q") String q,
      @Query("units") String units,
      @Query("lang") String lang,
      @Query("cnt") int dayCount,
      @Query("appid") String appId
  );

  @GET("forecast")
  Single<FiveDayResponse> getMultipleDaysWeathers(
          @Query("q") String q,
          @Query("units") String units,
          @Query("lang") String lang,
          @Query("cnt") int dayCount,
          @Query("appid") String appId
  );

//  @GET("forecast")
//  Single<FiveDayResponse> getMultipleDaysWeather(
//          @Query("q") String q,
//          @Query("units") String units,
//          @Query("lang") String lang,
//          @Query("cnt") int dayCount,
//          @Query("appid") String appId
//  );
}
