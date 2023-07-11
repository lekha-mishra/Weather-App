package com.example.app.weather.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.helper.widget.Layer;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.weather.R;
import com.example.app.weather.model.common.NextDaysModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class NextDaysAdapter  extends RecyclerView.Adapter<NextDaysAdapter.holder> {
    Context context;
    private List<NextDaysModel>  nextDaysModels;
    private int row_index;

    public NextDaysAdapter(Context context, List<NextDaysModel> nextDaysModels) {
        this.context = context;
        this.nextDaysModels = nextDaysModels;
    }

    @NonNull
    @Override
    public NextDaysAdapter.holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.next_days_items,parent,false);
        return new holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NextDaysAdapter.holder holder, int position) {
       NextDaysModel model =nextDaysModels.get(position);
         holder.avgTemp.setText(model.getAvgTemp());
         holder.lowTemp.setText(model.getLowTemp());
         holder.highTemp.setText(model.getHighTemp());
         holder.dayandDate.setText(model.getDayandDate());
         holder.rainType.setText(model.getRainType());

         holder.itemView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {

             }
         });
    }

    @Override
    public int getItemCount() {
        return nextDaysModels.size();
    }

    public  static class  holder extends RecyclerView.ViewHolder{
            ImageView  rainTypeImg;
            TextView   avgTemp,highTemp,lowTemp,dayandDate,rainType;
            MaterialCardView layoutbg;
        public holder(@NonNull View itemView) {
            super(itemView);
           rainTypeImg =itemView.findViewById(R.id.rainTypeImg);
            avgTemp     =itemView.findViewById(R.id.avgTemp);
            highTemp    =itemView.findViewById(R.id.highTemp);
            lowTemp     =itemView.findViewById(R.id.lowTemp);
            dayandDate  =itemView.findViewById(R.id.dayandDate);
            rainType    =itemView.findViewById(R.id.rainType);
            layoutbg    =itemView.findViewById(R.id.layoutbg);
        }
    }
}
