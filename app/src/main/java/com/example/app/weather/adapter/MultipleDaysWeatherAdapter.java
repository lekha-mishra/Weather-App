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
import com.example.app.weather.model.fivedayweather.ItemHourly;
import com.example.app.weather.utils.AppUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;

import java.text.Format;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;


public class MultipleDaysWeatherAdapter extends RecyclerView.Adapter<MultipleDaysWeatherAdapter.holder> {
    Context context;
    private List<ItemHourly> itemHourlyList;
    BottomSheetDialog sheetDialog;
    private  int row_index;
    private int testindex;

    public MultipleDaysWeatherAdapter(Context context, List<ItemHourly> itemHourlyList) {
        this.context = context;
        this.itemHourlyList = itemHourlyList;
    }

    @NonNull
    @Override
    public MultipleDaysWeatherAdapter.holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.next_days_items,parent,false);
        return new holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MultipleDaysWeatherAdapter.holder holder, int position) {
        ItemHourly model = itemHourlyList.get(position);

        holder.avgTemp.setText(String.valueOf(Math.round(model.getMain().getTemp()))+ "°");
        holder.highTemp.setText("H-"+String.valueOf(Math.round(model.getMain().getTempMax()))+ "°");
        holder.lowTemp.setText("L-"+String.valueOf(Math.round(model.getMain().getTempMin()))+ "°");
        holder.rainType.setText(model.getWeather().get(0).getDescription());
      //  AppUtil.setWeatherIcons(context, holder.rainTypeImg,model.getWeather().get(0).getId());
        holder.animation_view.setAnimation(AppUtil.getWeatherAnimation(model.getWeather().get(0).getId()));

        String str = model.getDtTxt();
        str  = str.replaceAll("\\s.*","");

        String time = model.getDtTxt().substring(11);
        Log.d("12hr","format :" + AppUtil.convertYourTime(time) + "," +time);
        holder.dayandDate.setText(str +", "+AppUtil.dateToDay(str));
        holder.dateCurrent.setText(getTimeString(model.getDt(),"IST"));
        List<ItemHourly> newHourly;
        newHourly =AppUtil.removeDuplicates(itemHourlyList);
        Log.d("skipElement"," Eelements are :" + new Gson().toJson(newHourly));
         for(int i=1;i<=itemHourlyList.size();i++){

             if(str.equals("2023-03-00")){
                 Log.d("skipElementnew"," Eelements are :" );
             holder.nextdaylayout.setVisibility(View.GONE);
               // notifyDataSetChanged();
             }

         }


         String year = str.substring(0,str.indexOf("-"));
         String date = str.substring(8);
         String months =str.substring(5);
         String month= months.substring(0,months.length()-3);

         Date d1 = (new GregorianCalendar( Integer.parseInt(year) ,AppUtil.getMonth(month),Integer.parseInt(date)).getTime());
        Format f = new SimpleDateFormat("EEEE");
        String finalday = f.format(d1);

        Log.d("datetoday","date is :" + str + " Day is :" + AppUtil.dateToDay(str));
        Log.d("requestweatherdate","date is :" + str + "," +finalday  + "  d : " + d1 + " year "+year  +" date " +date +" month:  "+ month);
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
                ImageView rainTypeImg = sheetDialog.findViewById(R.id.rainTypeImg);

                avgTemp.setText(String.valueOf(Math.round(model.getMain().getTemp()))+ "°");
                realFeelValue.setText(String.valueOf(Math.round(model.getMain().getFeelLike()))+ "°");
                RFeelSValue.setText(String.valueOf(Math.round(model.getMain().getFeelLike()))+ "°");
                bottomTimeDay.setText(getTimeString(model.getDt(),"IST") +", "+finalday);
                cloudCoverValue.setText(model.getClouds().getAll() + "%");
                windValue.setText(Math.round(model.getWind().getSpeed()*3.6)  + " "+context.getResources().getString(R.string.wind_speed));
                windGustValue.setText(Math.round(model.getWind().getGust()*3.6) +" mph");
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

//        if(row_index==position & testindex==1 ){
//            holder.layoutbg.setBackgroundColor(R.color.white);
//            holder.layoutbg.setRadius(50);
//
//        }else{
//            holder.layoutbg.setBackgroundColor(R.color.material_blue);
//            holder.layoutbg.setRadius(50);
//        }
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
        ImageView rainTypeImg;
        TextView avgTemp, highTemp, lowTemp,dayandDate,rainType,dateCurrent;
        ConstraintLayout nextdaylayout;
        CardView layoutbg;
        LottieAnimationView animation_view;


        public holder(@NonNull View itemView) {
            super(itemView);
            avgTemp = itemView.findViewById(R.id.avgTemp);
            highTemp = itemView.findViewById(R.id.highTemp);
            layoutbg = itemView.findViewById(R.id.layoutbg);
            lowTemp = itemView.findViewById(R.id.lowTemp);
            dayandDate = itemView.findViewById(R.id.dayandDate);
            rainType = itemView.findViewById(R.id.rainType);
            rainTypeImg = itemView.findViewById(R.id.rainTypeImg);
            animation_view = itemView.findViewById(R.id.animation_view);
            nextdaylayout = itemView.findViewById(R.id.nextdaylayout);
            dateCurrent = itemView.findViewById(R.id.dateCurrent);
        }
    }
}
