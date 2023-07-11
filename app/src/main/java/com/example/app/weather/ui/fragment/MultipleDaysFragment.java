package com.example.app.weather.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.app.weather.adapter.FiveDaysWeatherAdapter;
import com.example.app.weather.adapter.MultipleDaysWeatherAdapter;
import com.example.app.weather.adapter.NextDaysAdapter;
import com.example.app.weather.model.CityInfo;
import com.example.app.weather.model.common.NextDaysModel;
import com.example.app.weather.model.daysweather.ListItem;
import com.example.app.weather.model.daysweather.MultipleDaysWeatherResponse;
import com.example.app.weather.model.db.MultipleDaysWeather;
import com.example.app.weather.model.fivedayweather.FiveDayResponse;
import com.example.app.weather.model.fivedayweather.ItemHourly;
import com.example.app.weather.service.ApiService;
import com.example.app.weather.ui.activity.MainActivity;
import com.example.app.weather.utils.ApiClient;
import com.example.app.weather.utils.AppUtil;
import com.example.app.weather.utils.Constants;
import com.example.app.weather.utils.DbUtil;
import com.example.app.weather.utils.MyApplication;
import com.example.app.weather.R;
import com.example.app.weather.databinding.FragmentMultipleDaysBinding;
import com.github.pwittchen.prefser.library.rx2.Prefser;
import com.google.gson.Gson;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.Query;
import io.objectbox.reactive.DataObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MultipleDaysFragment extends DialogFragment {
  private String defaultLang = "en";
  private CompositeDisposable disposable = new CompositeDisposable();
  private FastAdapter<MultipleDaysWeather> mFastAdapter;
  private ItemAdapter<MultipleDaysWeather> mItemAdapter;
  private Activity activity;
  private Box<MultipleDaysWeather> multipleDaysWeatherBox;
  private Prefser prefser;
  private String apiKey;
  private FragmentMultipleDaysBinding binding;
  ArrayList<NextDaysModel> nextDaysList =new ArrayList<>();
  MultipleDaysWeatherAdapter multipleDaysWeatherAdapter;
  ProgressDialog progressDialog;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    binding = FragmentMultipleDaysBinding.inflate(inflater, container, false);
    View view = binding.getRoot();
    progressDialog= new ProgressDialog(getActivity());
    initVariables();
    initSwipeView();
    initRecyclerView();
    showStoredMultipleDaysWeather();
    checkTimePass();
    nextDaysList.add(new NextDaysModel(1,"7°",R.drawable.ic_shower_rain,"H:10°","L:9°","Thursday, 23 March 2023","Mid Rain"));
    nextDaysList.add(new NextDaysModel(1,"9°",R.drawable.ic_rainy_weather,"H:10°","L:9°","Friday, 24 March 2023","Mid Rain"));
    nextDaysList.add(new NextDaysModel(1,"10°",R.drawable.ic_cloudy_weather,"H:10°","L:9°","Saturday, 25 March 2023","Windy"));
    nextDaysList.add(new NextDaysModel(1,"12°",R.drawable.ic_clear_day,"H:10°","L:9°","Sunday, 26 March 2023","Mid Rain"));
    nextDaysList.add(new NextDaysModel(1,"14°",R.drawable.ic_shower_rain,"H:10°","L:9°","Monday, 27 March 2023","Mid Rain"));
    nextDaysList.add(new NextDaysModel(1,"7°",R.drawable.ic_shower_rain,"H:10°","L:9°","Tuesday, 28 March 2023","Mid Rain"));
    nextDaysList.add(new NextDaysModel(1,"7°",R.drawable.ic_shower_rain,"H:10°","L:9°","Wednesday, 29 March 2023","Mid Rain"));

  //  fetchNextDaysData();
    return view;
  }

  public  void  fetchNextDaysData(){
    NextDaysAdapter nextDaysAdapter = new NextDaysAdapter(getActivity(),nextDaysList);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
    linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
    binding.recyclerView.setLayoutManager(linearLayoutManager);
    binding.recyclerView.setAdapter(nextDaysAdapter);
  }

  private void initVariables() {
    activity = getActivity();
    prefser = new Prefser(activity);
    BoxStore boxStore = MyApplication.getBoxStore();
    multipleDaysWeatherBox = boxStore.boxFor(MultipleDaysWeather.class);
    binding.closeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
        if (getFragmentManager() != null) {
          getFragmentManager().popBackStack();
        }
      }
    });
  }

  private void initSwipeView() {
    binding.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
        android.R.color.holo_green_light,
        android.R.color.holo_orange_light,
        android.R.color.holo_red_light);
    binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

      @Override
      public void onRefresh() {
        requestWeather();
      }

    });
  }

  private void requestWeather() {
    long lastUpdate = prefser.get(Constants.LAST_STORED_MULTIPLE_DAYS, Long.class, 0L);
    if (AppUtil.isTimePass(lastUpdate)) {
      checkCityInfoExist();
    } else {
      binding.swipeContainer.setRefreshing(false);
    }
  }

  private void initRecyclerView() {
    LinearLayoutManager layoutManager
        = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
    binding.recyclerView.setLayoutManager(layoutManager);
    mItemAdapter = new ItemAdapter<>();
    mFastAdapter = FastAdapter.with(mItemAdapter);
    binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
    binding.recyclerView.setAdapter(mFastAdapter);
  }

  private void showStoredMultipleDaysWeather() {
    Query<MultipleDaysWeather> query = DbUtil.getMultipleDaysWeatherQuery(multipleDaysWeatherBox);
    query.subscribe().on(AndroidScheduler.mainThread())
        .observer(new DataObserver<List<MultipleDaysWeather>>() {
          @Override
          public void onData(@NonNull List<MultipleDaysWeather> data) {
            if (data.size() > 0) {
              final Handler handler = new Handler();
              handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                  data.remove(0);
                  mItemAdapter.clear();
                  mItemAdapter.add(data);
                }
              }, 500);
            }
          }
        });
  }

  private void checkTimePass() {
    apiKey = getResources().getString(R.string.open_weather_map_api);
    if (prefser.contains(Constants.LAST_STORED_MULTIPLE_DAYS)) {
      requestWeather();
    } else {
      checkCityInfoExist();
    }
  }

  private void checkCityInfoExist() {
    CityInfo cityInfo = prefser.get(Constants.CITY_INFO, CityInfo.class, null);
    if (cityInfo != null) {
      if (AppUtil.isNetworkConnected()) {
        requestWeathers(cityInfo.getName());
      } else {
        Toast.makeText(activity, getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show();
        binding.swipeContainer.setRefreshing(false);
      }
    }
  }
  private void setAdapter(List<ItemHourly> itemHourlyList){
    multipleDaysWeatherAdapter = new MultipleDaysWeatherAdapter(getActivity(),itemHourlyList);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
    linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
    binding.recyclerView.setAdapter(multipleDaysWeatherAdapter);
    binding.recyclerView.setLayoutManager(linearLayoutManager);
  }

  private void requestWeathers(String cityName) {
    AppUtil.showDialog(progressDialog);
    ApiService apiService = ApiClient.getClient().create(ApiService.class);
    disposable.add(
        apiService.getMultipleDaysWeathers(
            cityName, Constants.UNITS, defaultLang, 40, Constants.API_KEY)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new DisposableSingleObserver<FiveDayResponse>() {
              @Override
              public void onSuccess(FiveDayResponse response) {
              //  handleMultipleDaysResponse(response);
                setAdapter(response.getList());
                Log.d("requestweather","weather 15 Days" + new Gson().toJson(response));
                binding.swipeContainer.setRefreshing(false);
                AppUtil.exitDialog(progressDialog);
              }

              @Override
              public void onError(Throwable e) {
                binding.swipeContainer.setRefreshing(false);
                Log.d("requestweatherfail", "onError: " + e.getLocalizedMessage());
                AppUtil.exitDialog(progressDialog);
              }
            })
    );
  }

  private void handleMultipleDaysResponse(MultipleDaysWeatherResponse response) {
    multipleDaysWeatherBox.removeAll();
    List<ListItem> listItems = response.getList();
    for (ListItem listItem : listItems) {
      MultipleDaysWeather multipleDaysWeather = new MultipleDaysWeather();
      multipleDaysWeather.setDt(listItem.getDt());
      multipleDaysWeather.setMaxTemp(listItem.getTemp().getMax());
      multipleDaysWeather.setMinTemp(listItem.getTemp().getMin());
      multipleDaysWeather.setWeatherId(listItem.getWeather().get(0).getId());
      multipleDaysWeatherBox.put(multipleDaysWeather);
    }
    prefser.put(Constants.LAST_STORED_MULTIPLE_DAYS, System.currentTimeMillis());
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setCancelable(true);
    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
    lp.copyFrom(dialog.getWindow().getAttributes());
    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
    lp.height = WindowManager.LayoutParams.MATCH_PARENT;
    dialog.getWindow().setAttributes(lp);
    return dialog;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    disposable.dispose();
  }
}
