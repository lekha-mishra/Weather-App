package com.example.app.weather.adapter;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.app.weather.R;
import com.example.app.weather.model.common.HourlyModel;
import com.example.app.weather.model.fivedayweather.ItemHourly;
import com.example.app.weather.utils.AppUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.Format;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FiveDaysWeatherAdapter extends RecyclerView.Adapter<FiveDaysWeatherAdapter.holder> {
    Context context;
    private List<ItemHourly> itemHourlyList;
    BottomSheetDialog sheetDialog;
    private  int row_index;
    private int testindex;

    public FiveDaysWeatherAdapter(Context context, List<ItemHourly> itemHourlyList) {
        this.context = context;
        this.itemHourlyList = itemHourlyList;
    }

    @NonNull
    @Override
    public FiveDaysWeatherAdapter.holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_items,parent,false);
        return new holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FiveDaysWeatherAdapter.holder holder, int position) {
         ItemHourly model = itemHourlyList.get(position);
        holder.timingHourly.setText(getTimeString(model.getDt(),"IST") );
        holder.rainPercent.setText(String.valueOf(model.getClouds().getAll()) + "%");
        holder.tempHourly.setText(String.valueOf(Math.round(model.getMain().getTemp()))+ "°");
        AppUtil.setWeatherIcons(context, holder.rainStatus,model.getWeather().get(0).getId());

        holder.layoutbg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                row_index =position;
                notifyDataSetChanged();
                testindex=1;
                Log.d("itemclickbg","item is clicked");
                sheetDialog = new BottomSheetDialog(context,R.style.BottomSheetStyle);
                sheetDialog.setContentView(R.layout.bottom_sheet_hourly_layout);
                sheetDialog.setCanceledOnTouchOutside(true);
                sheetDialog.show();
                String str = model.getDtTxt();
                str  = str.replaceAll("\\s.*","");

                ImageView cancelIcon = sheetDialog.findViewById(R.id.cancelIcon);
                AppCompatTextView avgTemp = sheetDialog.findViewById(R.id.avgTemp);
                TextView realFeelValue = sheetDialog.findViewById(R.id.realFeelValue);
                TextView RFeelSValue = sheetDialog.findViewById(R.id.RFeelSValue);
                TextView HumidityValue = sheetDialog.findViewById(R.id.HumidityValue);
                TextView IndoorHumValue = sheetDialog.findViewById(R.id.IndoorHumValue);
                TextView bottomTimeDay = sheetDialog.findViewById(R.id.bottomTimeDay);
                TextView cloudCoverValue = sheetDialog.findViewById(R.id.cloudCoverValue);
                TextView windValue = sheetDialog.findViewById(R.id.windValue);
                TextView windGustValue = sheetDialog.findViewById(R.id.windGustValue);
                TextView visibilityValue = sheetDialog.findViewById(R.id.visibilityValue);
                TextView rainType = sheetDialog.findViewById(R.id.rainType);
                TextView lowTemp = sheetDialog.findViewById(R.id.lowTemp);
                LottieAnimationView rainTypeImg = sheetDialog.findViewById(R.id.rainTypeImg);

                rainTypeImg.setAnimation(AppUtil.getWeatherAnimation(model.getWeather().get(0).getId()));
                avgTemp.setText(String.valueOf(Math.round(model.getMain().getTemp()))+ "°");
                realFeelValue.setText(String.valueOf(Math.round(model.getMain().getFeelLike()))+ "°");
                RFeelSValue.setText(String.valueOf(Math.round(model.getMain().getFeelLike()))+ "°");
                bottomTimeDay.setText(getTimeString(model.getDt(),"IST") +", "+AppUtil.dateToDay(str));
                cloudCoverValue.setText(model.getClouds().getAll() + "%");
                windValue.setText(Math.round(model.getWind().getSpeed()*3.6)  + " "+context.getResources().getString(R.string.wind_speed));
                windGustValue.setText(Math.round(model.getWind().getGust()*3.6)  +" mph");
                visibilityValue.setText(model.getVisibility()/1000+" "+context.getResources().getString(R.string.km));
                HumidityValue.setText(String.valueOf(model.getMain().getHumidity())+ "%");
                IndoorHumValue.setText(String.valueOf(model.getMain().getHumidity())+ "%");
                rainType.setText(model.getWeather().get(0).getDescription());
                AppUtil.setWeatherIcons(context, rainTypeImg,model.getWeather().get(0).getId());
                lowTemp.setText(context.getResources().getString(R.string.atmospheric_pressure) +" : "+model.getMain().getGrndLevel() +" hPa");


                cancelIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sheetDialog.dismiss();
                    }
                });
            }
        });

        if(row_index==position & testindex==1 ){
            holder.layoutbg.setBackgroundColor(R.color.white);
            holder.layoutbg.setRadius(50);

        }else{
            holder.layoutbg.setBackgroundColor(R.color.material_blue);
            holder.layoutbg.setRadius(50);
        }
    }

    @Override
    public int getItemCount() {
        return itemHourlyList.size();
    }
    static String getTimeString(long epochSecond, String strTz) { ZoneId zoneId = ZoneId.of(strTz);
        Instant instant = Instant.ofEpochSecond(epochSecond);
        ZonedDateTime zdt = instant.atZone(zoneId);
        // DateTimeFormatter dtf = DateTimeFormatter.ofPattern("h:m:s a", Locale.ENGLISH);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("h:ma", Locale.ENGLISH);
        Log.d("timeformat","time is :" + zdt.format(dtf));
        return zdt.format(dtf);
    }

    public static class holder extends RecyclerView.ViewHolder{
        ImageView rainStatus;
        TextView timingHourly, rainPercent, tempHourly,claim_dedate;
        ConstraintLayout itemdata;
        CardView layoutbg;

        public holder(@NonNull View itemView) {
            super(itemView);
            timingHourly = itemView.findViewById(R.id.timingHourly);
            rainStatus = itemView.findViewById(R.id.rainStatus);
            layoutbg = itemView.findViewById(R.id.layoutbg);
            rainPercent = itemView.findViewById(R.id.rainPercent);
            tempHourly = itemView.findViewById(R.id.tempHourly);
        }
    }
}
