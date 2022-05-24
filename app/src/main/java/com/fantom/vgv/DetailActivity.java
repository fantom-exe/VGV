package com.fantom.vgv;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.fantom.vgv.models.Game;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;

import okhttp3.Headers;

public class DetailActivity extends YouTubeBaseActivity {
    private static final String YOUTUBE_API_KEY = "AIzaSyBXCu68NKFHQp6-sP6EZ_skVyCp6qXEU8A";
    public static final String VIDEOS_URL = "https://api.themoviedb.org/3/movie/%d/videos?api_key=a07e22bc18f5cb106bfe4cc1f83ad8ed";

    YouTubePlayerView youTubePlayerView;
    TextView tvTitle;
    TextView tvOverview;
    RatingBar ratingBar;
    TextView tvWiki;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        youTubePlayerView = findViewById(R.id.player);
        tvTitle = findViewById(R.id.tvTitle);
        tvOverview = findViewById(R.id.tvOverview);
        ratingBar = findViewById(R.id.ratingBar);
        tvWiki = findViewById(R.id.tvWiki);

        Game game = Parcels.unwrap(getIntent().getParcelableExtra("movie"));
        tvTitle.setText(game.getTitle());
        tvOverview.setText(game.getOverview());
        ratingBar.setRating((float) game.getRating());

        try {
            writeToFile(game.getTitle());
        } catch (IOException e) {
            e.printStackTrace();
        }

        tvWiki.setText(String.format("Wikipedia page: %s", game.getTitle()));

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(String.format(VIDEOS_URL, game.getGameId()), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Headers headers, JSON json) {
                try {
                    JSONArray results = json.jsonObject.getJSONArray("results");
                    if(results.length() == 0) {
                        return;
                    }
                    String youtubekey = results.getJSONObject(0).getString("key");
                    Log.d("DetailActivity", youtubekey);
                    initalizeYoutube(youtubekey);
                } catch (JSONException e) {
                    Log.e("DetailActivity", "Failed to parse JSON", e);
                }
            }

            @Override
            public void onFailure(int i, Headers headers, String s, Throwable throwable) {

            }
        });

    }

    private void initalizeYoutube(final String youtubekey) {
        youTubePlayerView.initialize(YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                Log.d("DetailActivity", "onInitializationSuccess");
                youTubePlayer.cueVideo(youtubekey);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.d("DetailActivity", "onInitializationFailure");
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void writeToFile(String title) throws IOException {
//        RandomAccessFile writer = new RandomAccessFile("./wiki_image/in-pipe.txt", "rw");
//        writer.writeUTF(title);
//        writer.close();

        Context context = getApplicationContext();
        String folder = context.getFilesDir().getAbsolutePath();
        Log.d("debug", folder);

        Files.write(Paths.get(folder + "/wiki_image/in-pipe.txt"), title.getBytes());

    }
}