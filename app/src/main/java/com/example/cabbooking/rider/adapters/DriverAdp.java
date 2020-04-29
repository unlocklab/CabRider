package com.example.cabbooking.rider.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.example.cabbooking.rider.R;
import com.example.cabbooking.rider.dto.CatDto;
import com.example.cabbooking.rider.dto.PriceDto;
import com.example.cabbooking.rider.dto.UserDto;
import com.example.cabbooking.rider.intr.AdpListner;
import com.example.cabbooking.rider.other.MapConst;

import org.json.JSONObject;

import java.util.List;


public class DriverAdp extends RecyclerView.Adapter<DriverAdp.MyViewHolder> {

    private List<UserDto> l1;
    private Activity activity;
    private AdpListner adpListner;
    private JSONObject ride_data;
    private PriceDto priceDto;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView txt1,txt2;
        public LinearLayout rl1;

        public MyViewHolder(View view) {
            super(view);
            txt1 = view.findViewById(R.id.txt1);
            txt2 = view.findViewById(R.id.txt2);
            rl1 = view.findViewById(R.id.rl1);
        }
    }


    public DriverAdp(Activity activity, List<UserDto> l1,JSONObject ride_data,PriceDto priceDto) {
        this.l1 = l1;
        this.activity = activity;
        this.ride_data = ride_data;
        this.priceDto = priceDto;
        adpListner = (AdpListner) activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        try {
            holder.txt1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adpListner.click1(holder.getAdapterPosition());
                }
            });

            holder.txt1.setText(l1.get(holder.getAdapterPosition()).getFname()+" "+l1.get(holder.getAdapterPosition()).getLname());

            holder.rl1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adpListner.click3(holder.getAdapterPosition());
                }
            });

            int distance_val = (int) Math.round(ride_data.getJSONObject(MapConst.distance).getLong(MapConst.value) / 1000);

            double fair = distance_val * Double.parseDouble(priceDto.getPrice_pkm());

            double dis = Double.parseDouble(priceDto.getDiscount());

            if (dis > 0) {
                fair = fair - ((fair * dis) / 100);
            }

            holder.txt2.setText("$" + String.format("%.2f", fair));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public int getItemCount() {
        return l1.size();
    }

}