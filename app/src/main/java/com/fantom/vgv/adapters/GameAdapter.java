package com.fantom.vgv.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fantom.vgv.DetailActivity;
import com.fantom.vgv.R;
import com.fantom.vgv.models.Game;

import org.parceler.Parcels;

import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.ViewHolder> {

    Context context;
    List<Game> games;

    public GameAdapter(Context context, List<Game> games) {
        this.context = context;
        this.games = games;
    }

    // Inflates a layout from XML and returns holder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("MovieAdapter", "onCreateViewHolder");
        View movieView = LayoutInflater.from(context).inflate(R.layout.item_game, parent, false);
        return new ViewHolder(movieView);
    }

    // Populates data into the item through holder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("MovieAdapter", "onBindViewHolder " + position);
        // get movie at the passed in position
        Game game = games.get(position);
        // bind movie data into viewHolder
        holder.bind(game);
    }

    // Return total count of items in list
    @Override
    public int getItemCount() {
        return games.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout container;
        TextView tvTitle;
        TextView tvOverview;
        ImageView ivPoster;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvOverview = itemView.findViewById(R.id.tvOverview);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            container = itemView.findViewById(R.id.container);
        }

        public void bind(Game game) {
            tvTitle.setText(game.getTitle());
            tvOverview.setText(game.getOverview());
            Glide.with(context).load(game.getPosterPath()).into(ivPoster);

            // 1. Register click listener on entire row
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 2. Navigate to a new activity on tap
                    Intent i = new Intent(context, DetailActivity.class);
                    i.putExtra("movie", Parcels.wrap(game));
                    context.startActivity(i);
                }
            });
        }
    }
}
