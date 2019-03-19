package main.android.com.popularmoviesapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;

import main.android.com.popularmoviesapp.parcels.Movie;
import main.android.com.popularmoviesapp.utilities.NetworkUtils;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class MainActivity extends AppCompatActivity implements PopularMoviesAdapter.OnRecyclerViewClickListener {

    private RecyclerView mRecyclerView;
    private PopularMoviesAdapter mAdapter;
    int NUMBER_COLUMN_IN_GRID = 2;
    JSONArray moviesArray;
    int len;
    public TextView mContent;
    public ArrayList MoviePojosArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null && savedInstanceState.containsKey("MoviePojosArrayListParcel")) {
            MoviePojosArrayList = savedInstanceState.getParcelableArrayList("MoviePojosArrayListParcel");
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        MoviePojosArrayList = new ArrayList<Movie>();
        mRecyclerView = (RecyclerView) findViewById(R.id.mRecyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, NUMBER_COLUMN_IN_GRID, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        // use this setting to improve performance if you know that changes
        //in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        URL moviesUrl = NetworkUtils.buildUrl();
        //Checking if we got an internet connection prior to making network call
        if (isOnline())
            fetchMoviesUrl(moviesUrl);
    }

    /**
     * Got this code from here: https://developer.android.com/training/monitoring-device-state/connectivity-monitoring
     *
     * @return a boolean true if we're connected
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnected();
    }

    public void fetchMoviesUrl(URL url) {
        new DownloadMovieUrlsAsyncTask().execute(url);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList("MoviePojosArrayListParcel", MoviePojosArrayList);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("MoviePojosArrayListParcel", MoviePojosArrayList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onclickListener(int itemClicked) {
        Toast.makeText(getApplicationContext(), "Item click was: " + itemClicked, Toast.LENGTH_SHORT).show();
    }

    private class DownloadMovieUrlsAsyncTask extends AsyncTask<URL, Void, JSONArray>
            implements PopularMoviesAdapter.OnRecyclerViewClickListener {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONArray doInBackground(URL... urls) {
            URL url = urls[0];
            JSONArray allMoviesJsonArray = new JSONArray();
            try {
                JSONObject allMoviesJsonObject = NetworkUtils.getResponseFromHttpUrl(url);
                allMoviesJsonArray = allMoviesJsonObject.getJSONArray("results");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return allMoviesJsonArray;
        }

        @Override
        protected void onPostExecute(JSONArray allMoviesJsonArray) {
            moviesArray = allMoviesJsonArray;
            len = allMoviesJsonArray.length();
            for (int i = 0; i < allMoviesJsonArray.length(); i++) {
                JSONObject singleMovieJsonObject = null;
                try {
                    singleMovieJsonObject = allMoviesJsonArray.getJSONObject(i);
                    String movieTitle = singleMovieJsonObject.getString("title");
                    String movieReleaseDate = singleMovieJsonObject.getString("release_date");
                    String movieOverview = singleMovieJsonObject.getString("overview");
                    String posterPath = singleMovieJsonObject.getString("poster_path").split("/")[1];
                    String fullPosterPath = NetworkUtils.buildPosterPathUrl(posterPath).toString();
                    String movieVoteAverage = singleMovieJsonObject.getString("vote_average");
                    Movie aMovie = new Movie(movieTitle, movieReleaseDate, movieOverview, fullPosterPath, movieVoteAverage);
                    MoviePojosArrayList.add(aMovie);
                    Log.v("fullPosterPath", fullPosterPath.toString());
                    Log.v("ArrayListLength", String.valueOf(MoviePojosArrayList.size()));
                    //Instantiating our adapter class
                    mAdapter = new PopularMoviesAdapter(MoviePojosArrayList, this);
                    mRecyclerView.setAdapter(mAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }//end loop
            super.onPostExecute(allMoviesJsonArray);
        }

        @Override
        public void onclickListener(int itemClicked) {
            //Toast.makeText(getApplicationContext(), "Item click was: "+itemClicked, Toast.LENGTH_SHORT).show();
            Intent movieDetailsIntent = new Intent(MainActivity.this, MovieDetails.class);
            Movie movieClickedOn = (Movie) MoviePojosArrayList.get(itemClicked);
            movieDetailsIntent.putExtra("movieTitle", movieClickedOn.getMovieTitle());
            movieDetailsIntent.putExtra("movieReleaseDate", movieClickedOn.getMovieReleaseDate());
            movieDetailsIntent.putExtra("movieOverview", movieClickedOn.getMovieOverview());
            movieDetailsIntent.putExtra("movieFullPosterPath", movieClickedOn.getMovieFullPosterPath());
            movieDetailsIntent.putExtra("movieVoteAverage", movieClickedOn.getMovieVoteAverage());
            startActivity(movieDetailsIntent);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
