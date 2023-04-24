package com.example.antigraffiti;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.List;

public class myAdapter extends FirebaseRecyclerAdapter<imageData, myAdapter.myViewHolder> {

    private Context context;
    public myAdapter(@NonNull FirebaseRecyclerOptions<imageData> options) {
        super(options);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull imageData model) {
        Glide.with(context).load(model.getImageUrl()).into(holder.img);
        holder.cat.setText(model.getCategory());
        holder.loc.setText(model.getAddress());

    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitems, parent, false);
context = parent.getContext();
return new myViewHolder(view);
    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        CardView cards;
        ImageView img;
        TextView cat,loc;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            cards = itemView.findViewById(R.id.card);
            img = itemView.findViewById(R.id.preview);
            cat = itemView.findViewById(R.id.categroy);
            loc = itemView.findViewById(R.id.address);

            loc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String address = loc.getText().toString();

                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + address);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");

                    PackageManager packageManager = v.getContext().getPackageManager();
                    List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, 0);
                    boolean isIntentSafe = ((List<?>) activities).size() > 0;

                    if (isIntentSafe) {
                        v.getContext().startActivity(mapIntent);
                    } else {
                        Toast.makeText(v.getContext(), "No app found to handle this action", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
