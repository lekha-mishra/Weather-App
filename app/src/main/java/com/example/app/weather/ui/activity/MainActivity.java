package com.example.app.weather.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.app.weather.adapter.FiveDaysWeatherAdapter;
import com.example.app.weather.adapter.WeatherAdapter;
import com.example.app.weather.model.CityInfo;
import com.example.app.weather.model.common.HourlyModel;
import com.example.app.weather.model.common.Wind;
import com.example.app.weather.model.currentweather.CurrentWeatherResponse;
import com.example.app.weather.model.currentweather.Main;
import com.example.app.weather.model.daysweather.ListItem;
import com.example.app.weather.model.daysweather.MultipleDaysWeatherResponse;
import com.example.app.weather.model.db.CurrentWeather;
import com.example.app.weather.model.db.FiveDayWeather;
import com.example.app.weather.model.db.ItemHourlyDB;
import com.example.app.weather.model.fivedayweather.FiveDayResponse;
import com.example.app.weather.model.fivedayweather.ItemHourly;
import com.example.app.weather.service.ApiService;
import com.example.app.weather.utils.ApiClient;
import com.example.app.weather.utils.AppUtil;
import com.example.app.weather.utils.Constants;
import com.example.app.weather.utils.DbUtil;
import com.example.app.weather.utils.Loader;
import com.example.app.weather.utils.MyApplication;
import com.example.app.weather.utils.SnackbarUtil;
import com.example.app.weather.utils.TextViewFactory;


import com.example.app.weather.R;
import com.example.app.weather.databinding.ActivityMainBinding;
import com.example.app.weather.ui.fragment.AboutFragment;
import com.example.app.weather.ui.fragment.MultipleDaysFragment;
import com.github.pwittchen.prefser.library.rx2.Prefser;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.Query;
import io.objectbox.reactive.DataObserver;
import io.objectbox.reactive.DataSubscriptionList;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class MainActivity extends BaseActivity {

  private FastAdapter<FiveDayWeather> mFastAdapter;
  private ItemAdapter<FiveDayWeather> mItemAdapter;
  private CompositeDisposable disposable = new CompositeDisposable();
  private String defaultLang = "en";
  private List<FiveDayWeather> fiveDayWeathers;
  private ApiService apiService;
  private FiveDayWeather todayFiveDayWeather;
  private Prefser prefser;
  private Box<CurrentWeather> currentWeatherBox;
  private Box<FiveDayWeather> fiveDayWeatherBox;
  private Box<ItemHourlyDB> itemHourlyDBBox;
  private DataSubscriptionList subscriptions = new DataSubscriptionList();
  private boolean isLoad = false;
  private CityInfo cityInfo;
  private String apiKey;
  private Typeface typeface;
  private ActivityMainBinding binding;
  private int[] colors;
  private int[] colorsAlpha;
  ArrayList<HourlyModel> hourlytime = new ArrayList<>();
  BottomSheetDialog sheetDialog;
  FiveDaysWeatherAdapter fiveDaysWeatherAdapter;
  Loader loader;
  ProgressDialog progressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    setSupportActionBar(binding.toolbarLayout.toolbar);
    progressDialog =new ProgressDialog(this);
    Window  window = getWindow();
    window.setStatusBarColor(ContextCompat.getColor(MainActivity.this,R.color.appcolor));
    window.setNavigationBarColor(ContextCompat.getColor(MainActivity.this,R.color.appcolor));
    initSearchView();
    initValues();
    setupTextSwitchers();
    initRecyclerView();
    showStoredCurrentWeather();
    showStoredFiveDayWeather();
    checkLastUpdate();

    hourlytime.add(new HourlyModel(1,"12PM",R.drawable.ic_cloudy_weather,"20%","8°"));
    hourlytime.add(new HourlyModel(2,"1PM",R.drawable.ic_rainy_weather,"30%","8°"));
    hourlytime.add(new HourlyModel(3,"2PM",R.drawable.ic_rainy_weather,"10%","8°"));
    hourlytime.add(new HourlyModel(4,"3PM",R.drawable.ic_snow_weather,"70%","8°"));
    hourlytime.add(new HourlyModel(5,"4PM",R.drawable.ic_storm_weather,"90%","8°"));
    hourlytime.add(new HourlyModel(6,"5PM",R.drawable.ic_storm_weather,"70%","18°"));

  //  fetchHourlyStatus();

  //  cityInfo = prefser.get(Constants.CITY_INFO, CityInfo.class, null);
   // requestWeather(cityInfo.getName(), false);

   // getFiveDaysHourlyWeather("Etawah");
    binding.contentEmptyLayout.searchTextView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        binding.toolbarLayout.searchView.showSearch();
      }
    });
  }

  private void initSearchView() {
    binding.toolbarLayout.searchView.setVoiceSearch(false);
    binding.toolbarLayout.searchView.setHint(getString(R.string.search_label_toolbar));
    binding.toolbarLayout.searchView.setCursorDrawable(R.drawable.custom_curosr);
    binding.toolbarLayout.searchView.setEllipsize(true);
    binding.toolbarLayout.searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        requestWeather(query, true);
        return false;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        return false;
      }
    });
    binding.toolbarLayout.searchView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        binding.toolbarLayout.searchView.showSearch();
      }
    });

  }

  private void initValues() {
    colors = getResources().getIntArray(R.array.mdcolor_500);
    colorsAlpha = getResources().getIntArray(R.array.mdcolor_500_alpha);
    prefser = new Prefser(this);
    apiService = ApiClient.getClient().create(ApiService.class);
    BoxStore boxStore = MyApplication.getBoxStore();
    currentWeatherBox = boxStore.boxFor(CurrentWeather.class);
    fiveDayWeatherBox = boxStore.boxFor(FiveDayWeather.class);
    itemHourlyDBBox = boxStore.boxFor(ItemHourlyDB.class);
    binding.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
        android.R.color.holo_green_light,
        android.R.color.holo_orange_light,
        android.R.color.holo_red_light);
    binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

      @Override
      public void onRefresh() {
        cityInfo = prefser.get(Constants.CITY_INFO, CityInfo.class, null);
        if (cityInfo != null) {
          long lastStored = prefser.get(Constants.LAST_STORED_CURRENT, Long.class, 0L);
          if (AppUtil.isTimePass(lastStored)) {
            requestWeather(cityInfo.getName(), false);
          } else {
            binding.swipeContainer.setRefreshing(false);
          }
        } else {
          binding.swipeContainer.setRefreshing(false);
        }
      }

    });
    binding.bar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showAboutFragment();
      }
    });
    typeface = Typeface.createFromAsset(getAssets(), "fonts/Vazir.ttf");
    binding.contentMainLayout.weeklyForecast.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        AppUtil.showFragment(new MultipleDaysFragment(), getSupportFragmentManager(), true);
      }
    });
    binding.nextDaysButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {


        AppUtil.showFragment(new MultipleDaysFragment(), getSupportFragmentManager(), true);
      }
    });
    binding.contentMainLayout.todayMaterialCard.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (todayFiveDayWeather != null) {
          Intent intent = new Intent(MainActivity.this, HourlyActivity.class);
          intent.putExtra(Constants.FIVE_DAY_WEATHER_ITEM, todayFiveDayWeather);
          startActivity(intent);
        }
      }
    });
  }

  private void setupTextSwitchers() {
    binding.contentMainLayout.tempTextView.setFactory(new TextViewFactory(MainActivity.this, R.style.TempTextView, true, typeface));
    binding.contentMainLayout.tempTextView.setInAnimation(MainActivity.this, R.anim.slide_in_right);
    binding.contentMainLayout.tempTextView.setOutAnimation(MainActivity.this, R.anim.slide_out_left);
   // binding.contentMainLayout.descriptionTextView.setFactory(new TextViewFactory(MainActivity.this, R.style.DescriptionTextView, true, typeface));
   // binding.contentMainLayout.descriptionTextView.setInAnimation(MainActivity.this, R.anim.slide_in_right);
  //  binding.contentMainLayout.descriptionTextView.setOutAnimation(MainActivity.this, R.anim.slide_out_left);
    binding.contentMainLayout.humidityTextView.setFactory(new TextViewFactory(MainActivity.this, R.style.HumidityTextView, false, typeface));
    binding.contentMainLayout.humidityTextView.setInAnimation(MainActivity.this, R.anim.slide_in_bottom);
    binding.contentMainLayout.humidityTextView.setOutAnimation(MainActivity.this, R.anim.slide_out_top);
    binding.contentMainLayout.windTextView.setFactory(new TextViewFactory(MainActivity.this, R.style.WindSpeedTextView, false, typeface));
    binding.contentMainLayout.windTextView.setInAnimation(MainActivity.this, R.anim.slide_in_bottom);
    binding.contentMainLayout.windTextView.setOutAnimation(MainActivity.this, R.anim.slide_out_top);
  }

  private void fetchHourlyStatus() {

    WeatherAdapter weatherAdapter = new WeatherAdapter(MainActivity.this, hourlytime);
    //  GridLayoutManager manager = new GridLayoutManager(Hardware_CAtegory_Activity.this, 2, GridLayoutManager.VERTICAL, false);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
    linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
    binding.contentMainLayout.recyclerView.setLayoutManager(linearLayoutManager);
    binding.contentMainLayout.recyclerView.setAdapter(weatherAdapter);


  }



  private void initRecyclerView() {
    LinearLayoutManager layoutManager
        = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
    binding.contentMainLayout.recyclerView.setLayoutManager(layoutManager);
    mItemAdapter = new ItemAdapter<>();
    mFastAdapter = FastAdapter.with(mItemAdapter);
    binding.contentMainLayout.recyclerView.setItemAnimator(new DefaultItemAnimator());
    binding.contentMainLayout.recyclerView.setAdapter(mFastAdapter);
    binding.contentMainLayout.recyclerView.setFocusable(false);
    mFastAdapter.withOnClickListener(new OnClickListener<FiveDayWeather>() {
      @Override
      public boolean onClick(@Nullable View v, @NonNull IAdapter<FiveDayWeather> adapter, @NonNull FiveDayWeather item, int position) {
        Intent intent = new Intent(MainActivity.this, HourlyActivity.class);
        intent.putExtra(Constants.FIVE_DAY_WEATHER_ITEM, item);
        startActivity(intent);
        return true;
      }
    });
  }
  static String getTimeString(long epochSecond, String strTz) { ZoneId zoneId = ZoneId.of(strTz);
    Instant instant = Instant.ofEpochSecond(epochSecond);
    ZonedDateTime zdt = instant.atZone(zoneId);
    // DateTimeFormatter dtf = DateTimeFormatter.ofPattern("h:m:s a", Locale.ENGLISH);
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("h:m a", Locale.ENGLISH);
    Log.d("timeformat","time is :" + zdt.format(dtf));
    return zdt.format(dtf);
  }

  private void showStoredCurrentWeather() {
    Query<CurrentWeather> query = DbUtil.getCurrentWeatherQuery(currentWeatherBox);
    query.subscribe(subscriptions).on(AndroidScheduler.mainThread())
        .observer(new DataObserver<List<CurrentWeather>>() {
          @Override
          public void onData(@NonNull List<CurrentWeather> data) {
            if (data.size() > 0) {
              hideEmptyLayout();
              CurrentWeather currentWeather = data.get(0);
              if (isLoad) {
                binding.contentMainLayout.tempTextView.setText(String.format(Locale.getDefault(), "%.0f°", currentWeather.getTemp()));
                AppUtil.getWeatherStatus(currentWeather.getWeatherId(),binding.contentMainLayout.descriptionTextView);
                binding.contentMainLayout.humidityTextView.setText(String.format(Locale.getDefault(), "%d%%", currentWeather.getHumidity()));
                binding.contentMainLayout.windTextView.setText(String.format(Locale.getDefault(), getResources().getString(R.string.wind_unit_label), currentWeather.getWindSpeed()));
              } else {
                binding.contentMainLayout.tempTextView.setCurrentText(String.format(Locale.getDefault(), "%.0f°", currentWeather.getTemp()));
                AppUtil.getWeatherStatus(currentWeather.getWeatherId(),binding.contentMainLayout.descriptionTextView);
               // binding.contentMainLayout.descriptionTextView.setCurrentText(AppUtil.getWeatherStatus(currentWeather.getWeatherId(), AppUtil.isRTL(MainActivity.this)));
                binding.contentMainLayout.humidityTextView.setCurrentText(String.format(Locale.getDefault(), "%d%%", currentWeather.getHumidity()));
                binding.contentMainLayout.windTextView.setCurrentText(String.format(Locale.getDefault(), getResources().getString(R.string.wind_unit_label), currentWeather.getWindSpeed()));
              }
              binding.contentMainLayout.animationView.setAnimation(AppUtil.getWeatherAnimation(currentWeather.getWeatherId()));
              binding.contentMainLayout.animationView.playAnimation();
            }
          }
        });
  }

  private void showStoredFiveDayWeather() {
    Query<FiveDayWeather> query = DbUtil.getFiveDayWeatherQuery(fiveDayWeatherBox);
    query.subscribe(subscriptions).on(AndroidScheduler.mainThread())
        .observer(new DataObserver<List<FiveDayWeather>>() {
          @Override
          public void onData(@NonNull List<FiveDayWeather> data) {
            if (data.size() > 0) {
              todayFiveDayWeather = data.remove(0);
              mItemAdapter.clear();
              mItemAdapter.add(data);
            }
          }
        });
  }

  private void checkLastUpdate() {
    cityInfo = prefser.get(Constants.CITY_INFO, CityInfo.class, null);
    if (cityInfo != null) {
      binding.toolbarLayout.cityNameTextView.setText(String.format("%s, %s", cityInfo.getName(), cityInfo.getCountry()));
      if (prefser.contains(Constants.LAST_STORED_CURRENT)) {
        long lastStored = prefser.get(Constants.LAST_STORED_CURRENT, Long.class, 0L);
        if (AppUtil.isTimePass(lastStored)) {
          Log.d("weatherdatahourlycheckif","last stored");
          requestWeather(cityInfo.getName(), false);
        }
      } else {
        requestWeather(cityInfo.getName(), false);
        Log.d("weatherdatahourlycheckelse","last stored");
      }
      requestWeather(cityInfo.getName(), false);
    } else {
      showEmptyLayout();
    }

  }


  private void requestWeather(String cityName, boolean isSearch) {
    if (AppUtil.isNetworkConnected()) {
      getCurrentWeather(cityName, isSearch);
      getFiveDaysWeather(cityName);

      getFiveDaysHourlyWeather(cityName);
    } else {
      SnackbarUtil
          .with(binding.swipeContainer)
          .setMessage(getString(R.string.no_internet_message))
          .setDuration(SnackbarUtil.LENGTH_LONG)
          .showError();
      binding.swipeContainer.setRefreshing(false);
    }
  }

  private void getCurrentWeather(String cityName, boolean isSearch) {
     apiKey = getResources().getString(R.string.open_weather_map_api);
 //   apiKey = "873743bfc6e3b1297ca7b6232dba9690";
    disposable.add(
        apiService.getCurrentWeather(
            cityName, Constants.UNITS, defaultLang, apiKey)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new DisposableSingleObserver<CurrentWeatherResponse>() {
              @Override
              public void onSuccess(CurrentWeatherResponse currentWeatherResponse) {
                Log.d("weatherres","weather response: "+ new Gson().toJson(currentWeatherResponse));
                isLoad = true;
                storeCurrentWeather(currentWeatherResponse);
                storeCityInfo(currentWeatherResponse);
                String sunrise= String.valueOf(currentWeatherResponse.getSys().getSunrise());
                String sunset= String.valueOf(currentWeatherResponse.getSys().getSunset());
                String visibility= String.valueOf(currentWeatherResponse.getVisibility()/1000);
                String windSpeed= String.valueOf(Math.round(currentWeatherResponse.getWind().getSpeed()*3.6) );
                String windDegree= String.valueOf(currentWeatherResponse.getWind().getDeg());
                getTimeString(Long.parseLong(sunrise),"IST");
                binding.contentMainLayout.txtSunrise.setText(getTimeString(Long.parseLong(sunrise),"IST"));
                binding.contentMainLayout.txtSunset.setText(getResources().getString(R.string.sunset)+" :  "+getTimeString(Long.parseLong(sunset),"IST"));
                binding.contentMainLayout.txtVisibility.setText(visibility+" "+getResources().getString(R.string.km));
                binding.contentMainLayout.txtWind.setText(windSpeed + " "+ getResources().getString(R.string.wind_speed));
                binding.contentMainLayout.txtwindDegree.setText(getResources().getString(R.string.wind_direction)+" : "+windDegree + "°");
                binding.contentMainLayout.txtrealFeel.setText(getResources().getString(R.string.lowhealth) +" :  "+String.valueOf(Math.round(currentWeatherResponse.getMain().getFeels_like()))+"°" );
                binding.contentMainLayout.txtPressure.setText(currentWeatherResponse.getMain().getPressure()+" hPa");
                AppUtil.setPressure(MainActivity.this,binding.contentMainLayout.txtPressure,binding.contentMainLayout.txtPressureText,binding.contentMainLayout.idtxtPressure,Math.round(currentWeatherResponse.getMain().getPressure()) );
                AppUtil.setVisibility(MainActivity.this,binding.contentMainLayout.txtVisibilityValue,binding.contentMainLayout.bgVisibility,currentWeatherResponse.getVisibility());
                AppUtil.setTemp(MainActivity.this,binding.contentMainLayout.txtrealFeel,binding.contentMainLayout.txtTemptext,binding.contentMainLayout.idrealFeel, Integer.parseInt(String.valueOf(Math.round(currentWeatherResponse.getMain().getFeels_like()) )));
                Log.d("weatherressunrise",getTimeString(Long.parseLong(sunrise),"IST")+","+getTimeString(Long.parseLong(sunset),"IST"));
                binding.swipeContainer.setRefreshing(false);
                if (isSearch) {
                  prefser.remove(Constants.LAST_STORED_MULTIPLE_DAYS);
                }
              }

              @Override
              public void onError(Throwable e) {
                binding.swipeContainer.setRefreshing(false);
                try {
                  HttpException error = (HttpException) e;
                  handleErrorCode(error);
                } catch (Exception exception) {
                  e.printStackTrace();
                }
              }
            })

    );
  }

  private void handleErrorCode(HttpException error) {
    if (error.code() == 404) {
      SnackbarUtil
          .with(binding.swipeContainer)
          .setMessage(getString(R.string.no_city_found_message))
          .setDuration(SnackbarUtil.LENGTH_INDEFINITE)
          .setAction(getResources().getString(R.string.search_label), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              binding.toolbarLayout.searchView.showSearch();
            }
          })
          .showWarning();

    } else if (error.code() == 401) {
      SnackbarUtil
          .with(binding.swipeContainer)
          .setMessage(getString(R.string.invalid_api_key_message))
          .setDuration(SnackbarUtil.LENGTH_INDEFINITE)
          .setAction(getString(R.string.ok_label), new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
          })
          .showError();

    } else {
      SnackbarUtil
          .with(binding.swipeContainer)
          .setMessage(getString(R.string.network_exception_message))
          .setDuration(SnackbarUtil.LENGTH_LONG)
          .setAction(getResources().getString(R.string.retry_label), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if (cityInfo != null) {
                requestWeather(cityInfo.getName(), false);
              } else {
                binding.toolbarLayout.searchView.showSearch();
              }
            }
          })
          .showWarning();
    }
  }

  private void showEmptyLayout() {
    Glide.with(MainActivity.this).load(R.drawable.cityone).into(binding.contentEmptyLayout.noCityImageView);
    binding.contentEmptyLayout.emptyLayout.setVisibility(View.VISIBLE);
    binding.contentMainLayout.nestedScrollView.setVisibility(View.GONE);
    binding.contentEmptyLayout.animationView.setAnimation(R.raw.cityscape);
  }

  private void hideEmptyLayout() {
    binding.contentEmptyLayout.emptyLayout.setVisibility(View.GONE);
    binding.contentMainLayout.nestedScrollView.setVisibility(View.VISIBLE);
  }


  private void storeCurrentWeather(CurrentWeatherResponse response) {
    CurrentWeather currentWeather = new CurrentWeather();
    currentWeather.setTemp(response.getMain().getTemp());
    currentWeather.setHumidity(response.getMain().getHumidity());
    currentWeather.setDescription(response.getWeather().get(0).getDescription());
    currentWeather.setMain(response.getWeather().get(0).getMain());
    currentWeather.setWeatherId(response.getWeather().get(0).getId());
    currentWeather.setWindDeg(response.getWind().getDeg());
    currentWeather.setWindSpeed(response.getWind().getSpeed());
    currentWeather.setStoreTimestamp(System.currentTimeMillis());
    prefser.put(Constants.LAST_STORED_CURRENT, System.currentTimeMillis());
    if (!currentWeatherBox.isEmpty()) {
      currentWeatherBox.removeAll();
      currentWeatherBox.put(currentWeather);
    } else {
      currentWeatherBox.put(currentWeather);
    }
  }

  private void storeCityInfo(CurrentWeatherResponse response) {
    CityInfo cityInfo = new CityInfo();
    cityInfo.setCountry(response.getSys().getCountry());
    cityInfo.setId(response.getId());
    cityInfo.setName(response.getName());
    prefser.put(Constants.CITY_INFO, cityInfo);
    binding.toolbarLayout.cityNameTextView.setText(String.format("%s, %s", cityInfo.getName(), cityInfo.getCountry()));
  }

  private void getFiveDaysWeather(String cityName) {
    disposable.add(
        apiService.getMultipleDaysWeather(
            cityName, Constants.UNITS, defaultLang, 40, apiKey)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new DisposableSingleObserver<MultipleDaysWeatherResponse>() {
              @Override
              public void onSuccess(MultipleDaysWeatherResponse response) {
                Log.d("weatherdatafive","weather data 5" + new Gson().toJson(response));
                handleFiveDayResponse(response, cityName);
              }

              @Override
              public void onError(Throwable e) {
                e.printStackTrace();
                Log.d("weatherdatafivefail","weather data 5" + e.getLocalizedMessage());
              }
            })
    );
  }

  private void handleFiveDayResponse(MultipleDaysWeatherResponse response, String cityName) {
    fiveDayWeathers = new ArrayList<>();
    List<ListItem> list = response.getList();
    int day = 0;
    for (ListItem item : list) {
      int color = colors[day];
      int colorAlpha = colorsAlpha[day];
      Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
      Calendar newCalendar = AppUtil.addDays(calendar, day);
      FiveDayWeather fiveDayWeather = new FiveDayWeather();
      fiveDayWeather.setWeatherId(item.getWeather().get(0).getId());
      fiveDayWeather.setDt(item.getDt());
      fiveDayWeather.setMaxTemp(item.getTemp().getMax());
      fiveDayWeather.setMinTemp(item.getTemp().getMin());
      fiveDayWeather.setTemp(item.getTemp().getDay());
      fiveDayWeather.setColor(color);
      fiveDayWeather.setColorAlpha(colorAlpha);
      fiveDayWeather.setTimestampStart(AppUtil.getStartOfDayTimestamp(newCalendar));
      fiveDayWeather.setTimestampEnd(AppUtil.getEndOfDayTimestamp(newCalendar));
      fiveDayWeathers.add(fiveDayWeather);
      day++;
    }
    getFiveDaysHourlyWeather(cityName);
  }

  private void setAdapter(List<ItemHourly> itemHourlyList){
    fiveDaysWeatherAdapter = new FiveDaysWeatherAdapter(MainActivity.this,itemHourlyList);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
    linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
    binding.contentMainLayout.recyclerView.setAdapter(fiveDaysWeatherAdapter);
    binding.contentMainLayout.recyclerView.setLayoutManager(linearLayoutManager);
  }
  private void getFiveDaysHourlyWeather(String cityName) {
     AppUtil.showDialog(progressDialog);
    disposable.add(
        apiService.getFiveDaysWeather(
            cityName, Constants.UNITS, defaultLang, Constants.API_KEY)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new DisposableSingleObserver<FiveDayResponse>() {
              @Override
              public void onSuccess(FiveDayResponse response) {
               // handleFiveDayHourlyResponse(response);
                setAdapter(response.getList());
                Log.d("weatherdatahourly","data : " + response.getList());
                AppUtil.exitDialog(progressDialog);
               // loader.dismiss();
              }

              @Override
              public void onError(Throwable e) {
                e.printStackTrace();
                Log.d("weatherdatahourlyfail","data : " + e.getLocalizedMessage());
                AppUtil.exitDialog(progressDialog);
              }
            })

    );
  }




  private void handleFiveDayHourlyResponse(FiveDayResponse response) {
    if (!fiveDayWeatherBox.isEmpty()) {
      fiveDayWeatherBox.removeAll();
    }
    if (!itemHourlyDBBox.isEmpty()) {
      itemHourlyDBBox.removeAll();
    }
    for (FiveDayWeather fiveDayWeather : fiveDayWeathers) {
      long fiveDayWeatherId = fiveDayWeatherBox.put(fiveDayWeather);
      ArrayList<ItemHourly> listItemHourlies = new ArrayList<>(response.getList());
      for (ItemHourly itemHourly : listItemHourlies) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTimeInMillis(itemHourly.getDt() * 1000L);
        if (calendar.getTimeInMillis()
            <= fiveDayWeather.getTimestampEnd()
            && calendar.getTimeInMillis()
            > fiveDayWeather.getTimestampStart()) {
          ItemHourlyDB itemHourlyDB = new ItemHourlyDB();
          itemHourlyDB.setDt(itemHourly.getDt());
          itemHourlyDB.setFiveDayWeatherId(fiveDayWeatherId);
          itemHourlyDB.setTemp(itemHourly.getMain().getTemp());
          itemHourlyDB.setWeatherCode(itemHourly.getWeather().get(0).getId());
          itemHourlyDBBox.put(itemHourlyDB);
        }
      }
    }
  }


  @Override
  protected void onDestroy() {
    super.onDestroy();
    disposable.dispose();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    MenuItem item = menu.findItem(R.id.action_search);
    binding.toolbarLayout.searchView.setMenuItem(item);
    return true;
  }

  public void showAboutFragment() {
    AppUtil.showFragment(new AboutFragment(), getSupportFragmentManager(), true);
  }

  @Override
  public void onBackPressed() {
    if (binding.toolbarLayout.searchView.isSearchOpen()) {
      binding.toolbarLayout.searchView.closeSearch();
    } else {
      super.onBackPressed();
    }
  }
}
