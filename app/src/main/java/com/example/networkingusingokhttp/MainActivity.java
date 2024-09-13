package com.example.networkingusingokhttp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "NetworkingUsingOkHttp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button button = findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    updateTextView();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void updateTextView() throws IOException {

        // Make the network call here and update the TextView with the response

        // we can't directly make call on the network on main thread
        // because for any time consuming task, if we make call on main thread,
        // then for that time, screen will be frozen/slow/unresponsive
        // so we will make async call so creating class like below


//        NetworkTask networkTask = new NetworkTask();
        EditText editTextText = findViewById(R.id.editTextText);
        String query = "https://api.github.com/search/users?q=" + editTextText.getText().toString();
//        networkTask.execute("https://api.github.com/search/users?q=" + query);

        makeNetworkCall(query);
    }

    ArrayList<GithubUser> parseJson(String s) throws JSONException {
        ArrayList<GithubUser> githubUsers = new ArrayList<>();

        // parse the json
        try {
            JSONObject root = new JSONObject(s);
            JSONArray items = root.getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                JSONObject object = items.getJSONObject(i);
                String login = object.getString("login");
                Integer id = object.getInt("id");
                String avatar_url = object.getString("avatar_url");
                Double score = object.getDouble("score");
                String html = object.getString("html_url");
                String avatar = object.getString("avatar_url");
                GithubUser githubUser = new GithubUser(login, id, html, score, avatar);
                githubUsers.add(githubUser);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return githubUsers;
    }

    // using okhttp
    void makeNetworkCall(String url) throws IOException {
        // client
        OkHttpClient client = new OkHttpClient();

        // create request
        Request request = new Request.Builder()
                .url(url)
                .build();

        // create response
        // if used execute() it is synchronous call -> network request not allowed on main thread = NetworkMainThreadException
        // if used enqueue() it is asynchronous call
//        Response response = client.newCall(request).execute();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // below code DOES NOT run on main thread
                String result = response.body().string();

                Gson gson = new Gson();
                ApiResult apiResult = gson.fromJson(result, ApiResult.class);
//                ArrayList<GithubUser>

                final GithubUserAdapter githubUserAdapter = new GithubUserAdapter(apiResult.getItems());
//                ArrayList<GithubUser> users = null;
//                try {
//                    users = parseJson(result);
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }
//                Log.d(TAG, "onResponse : " + users);

//            Log.e("MainActivity", "onPostExecute: " + users.size());
//            Log.e("MainActivity", "onPostExecute: " + users.get(7).getHtml_url());
//                GithubUserAdapter githubUserAdapter = new GithubUserAdapter(users);
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // below code runs on main thread
                        RecyclerView recyclerView = findViewById(R.id.rvUsers);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                        recyclerView.setAdapter(githubUserAdapter);

                    }
                });
            }
        });
    }

    // 1st parameter is String because we will provide URL which is in String format
    // 2nd parameter is Void because we don't need/show progress update via API Call
    // 3rd parameter is String because we will get response in String format
    class NetworkTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String stringUrl = strings[0];
            try {
                URL url = new URL(stringUrl);
                url.openConnection();

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();

                Scanner scanner = new Scanner(inputStream);
                // this delimiter allows me to read whole input stream in one go
                scanner.useDelimiter("\\A");
                if (scanner.hasNext()) {
                    String s = scanner.next();
                    return s;
                }
            } catch (MalformedURLException e) {
                Log.d("NetworkTask", "doInBackground: Not Valid URL");
                throw new RuntimeException(e);
            } catch (IOException e) {
                Log.d("NetworkTask", "doInBackground: Not able to make Network call");
                throw new RuntimeException(e);
            }

            return "Failed to load";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            ArrayList<GithubUser> users = null;
//            try {
//                users = parseJson(s);
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            }
//
////            Log.e("MainActivity", "onPostExecute: " + users.size());
////            Log.e("MainActivity", "onPostExecute: " + users.get(7).getHtml_url());
//            GithubUserAdapter githubUserAdapter = new GithubUserAdapter(users);
//            RecyclerView recyclerView = findViewById(R.id.rvUsers);
//            recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
//            recyclerView.setAdapter(githubUserAdapter);

//            TextView textView = findViewById(R.id.textView);
//            textView.setText(s);
        }
    }
}

