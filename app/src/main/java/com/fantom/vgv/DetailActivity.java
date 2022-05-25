package com.fantom.vgv;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

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

        // get file permissions
        try {
            getFilePermissions();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // write to microservice input text file
        try {
            microserviceInput(game.getTitle());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Write to in-pipe.txt failed", Toast.LENGTH_SHORT).show();
        }

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

        // read from microservice output text file
        try {
            String wiki = microserviceOutput();
            tvWiki.setText(String.format("Wikipedia page: %s", wiki));
        } catch (IOException e) {
            e.printStackTrace();
            tvWiki.setText(String.format("Wikipedia page: %s", "Not found!"));
        }
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

    private void getFilePermissions() throws IOException {
        if (ActivityCompat.shouldShowRequestPermissionRationale(DetailActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(DetailActivity.this, "Write External Storage permission allows us to create files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(DetailActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }

    }

    private void microserviceInput(String title) throws IOException {
        File myFile = new File(Environment.getExternalStorageDirectory(), "Download/in-pipe.txt");
        FileOutputStream fOut = new FileOutputStream(myFile);

        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

        myOutWriter.append(title);
        myOutWriter.close();
        fOut.close();
    }

    private String microserviceOutput() throws IOException {
        File myFile = new File(Environment.getExternalStorageDirectory(), "Download/out-pipe.txt");
        myFile.createNewFile();
        FileInputStream fIn = new FileInputStream(myFile);
        BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));

        String wiki = myReader.readLine();

        myReader.close();
        return wiki;
    }
}
