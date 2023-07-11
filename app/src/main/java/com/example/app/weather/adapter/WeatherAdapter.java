package com.example.app.weather.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.app.weather.R;
import com.example.app.weather.model.common.HourlyModel;
import com.example.app.weather.ui.activity.MainActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;


public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.holder> {


    Context context;
    private List<HourlyModel> hourlyModels;
    BottomSheetDialog sheetDialog;
    private  int row_index;
    private int testindex;
    public WeatherAdapter(Context context, List<HourlyModel> hourlyModels) {
        this.context = context;
        this.hourlyModels = hourlyModels;
    }



    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_items, parent, false);
        return new holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull holder holder, int position) {
        HourlyModel model =hourlyModels.get(position);
        holder.timingHourly.setText(model.getTiminghourly());
        holder.rainPercent.setText(model.getRainpercent());
        holder.tempHourly.setText(model.getRaintemp());


        Glide.with(context)
                .load(model.getRainstatus())
                .into(holder.rainStatus);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("itemclick","item is clicked");
            }
        });

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
    public int getItemCount() {  return hourlyModels.size();

    }

    public static class holder extends RecyclerView.ViewHolder {

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



