package com.golan.amit.iwallet;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class WalletActionAdapter extends ArrayAdapter<WalletAction> {

    Context context;
    List<WalletAction> objects;

    public WalletActionAdapter(Context context, int resource, List<WalletAction> objects) {
        super(context, resource, objects);
        this.context = context;
        this.objects = objects;
    }

    public WalletAction getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        return super.getView(position, convertView, parent);

        View view = convertView;
        Holder holder;

        if(view == null) {
//            LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.custom_row, parent, false);

            holder = new Holder();

            holder.depositView = view.findViewById(R.id.tvDepositId);
            holder.drawView = view.findViewById(R.id.tvDrawId);
            holder.dateView = view.findViewById(R.id.tvDateId);
            holder.imageView = view.findViewById(R.id.iv);
            view.setTag(holder);
        } else {
            holder = (Holder)view.getTag();
        }

        WalletAction temp = objects.get(position);

        holder.imageView.setOnClickListener(popUpListenetr);
        Integer rowPosition = position;
        holder.imageView.setTag(rowPosition);

        holder.depositView.setText(String.valueOf(temp.getDeposit()));
        holder.drawView.setText(String.valueOf(temp.getDraw()));
        holder.dateView.setText(temp.getCurr_datetime());

        if (temp.getDraw() == 0) {
            holder.imageView.setImageResource(R.mipmap.green_ok);
            holder.drawView.setText("---");
        } else {
            holder.imageView.setImageResource(R.mipmap.red_notok);
            holder.depositView.setText("---");
        }
        return view;
    }

    View.OnClickListener popUpListenetr = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Integer viewPosition = (Integer)v.getTag();
            WalletAction wa = objects.get(viewPosition);
            if(wa != null) {
                int id = wa.getId();    //  if we want to delete / show / update from db
//                Toast.makeText(v.getContext(), "clicked: " + wa.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    public static class Holder {
        ImageView imageView;
        TextView depositView;
        TextView drawView;
        TextView dateView;
    }

}
