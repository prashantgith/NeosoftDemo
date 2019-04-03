package com.prashant.neosoftdemo.Adapter;

import android.graphics.Movie;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.prashant.neosoftdemo.Pojjo.Place;
import com.prashant.neosoftdemo.R;

import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.MyViewHolder> {

    private List<Place> placeList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView area, temp, hum;

        public MyViewHolder(View view) {
            super(view);
            area = (TextView) view.findViewById(R.id.txt_area);
            temp = (TextView) view.findViewById(R.id.txt_temp);
            hum = (TextView) view.findViewById(R.id.txt_hum);
        }
    }


    public WeatherAdapter(List<Place> placeList) {
        this.placeList = placeList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Place place = placeList.get(position);
        holder.temp.setText("Temp.- "+place.getTemp());
        holder.area.setText(""+place.getName());
        holder.hum.setText("Humd.- "+place.getHumidity());
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }
}
