package com.example.android.booksearch;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    /**
     * URL to query the Google Books dataset for book information
     */
    private static final String GOOGLE_BOOKS_API_URL = "https://www.googleapis.com/books/v1/volumes?q=";

    private EditText searchText;
    private Button searchButton;
    private ListView listView;
    private ArrayList<Book> bookArrayList;
    private BookAdapter adapter;
    private TextView emptyText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the EditText, TextView, and Button
        searchButton = (Button) findViewById(R.id.search_button);
        emptyText = (TextView) findViewById(R.id.no_data_msg);

        // Find the ListView
        listView = (ListView) findViewById(R.id.list);
        bookArrayList = new ArrayList<Book>();
        adapter = new BookAdapter(this, bookArrayList);
        listView.setAdapter(adapter);

        // Set click listener on EditText field
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.clear();
                searchText = (EditText) findViewById(R.id.search_field);
                String userInput = searchText.getText().toString().replace(" ", "+");
                if (userInput.isEmpty()) {
                    Context context = getApplicationContext();
                    CharSequence text = "You did not enter anything";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                // Kick off an {@link AsyncTask} to perform the network request
                BookAsyncTask task = new BookAsyncTask();
                task.execute(userInput);
            }
        });
    }
    // Update the screen to display the information for the given {@link Book}
    private void updateUi(List<Book> book) {
        if (book.isEmpty()) {
            emptyText.setText("No results available. Try another search.");
        } else {
            emptyText.setText("");
            adapter.addAll(book);
            adapter.notifyDataSetChanged();
        }
    }

    // {@link AsyncTask} to perform the network request on a background thread, and then update
    // the UI with the first book in the response.

    private class BookAsyncTask extends AsyncTask<String, Void, List<Book>> {

        @Override
        protected List<Book> doInBackground(String... strings) {
            // Create URL object
            URL url = createUrl(GOOGLE_BOOKS_API_URL + strings[0] + "&maxResults=10");

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Extract relevant fields from the JSON response and create an {@link Book} object
            List<Book> book = extractFeatureFromJson(jsonResponse);

            // Return the {@link Book} object as the result of the {@link BookAsyncTask}
            return book;
        }

        // Update screen with given book (result of {@link BookAsyncTask}
        @Override
        protected void onPostExecute(List<Book> books) {
            if (books == null) {
                return;
            }
            updateUi(books);
        }


        // Return new URL object from the given string URL
        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        // Make an HTTP request to the given URL and return a String as the response.
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";

            // If the URL is null, then return early.
            if (url == null) {
                return jsonResponse;
            }

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();


                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                } else {
                    Log.e(LOG_TAG, "Error" + urlConnection.getResponseCode());
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        // Convert the (@link InputStream} into a String which contains the
        // whole JSON response from the server.
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        // Return an {@link Book} object by parsing out information
        // about the first book from the input bookJSON string.
        private List<Book> extractFeatureFromJson(String bookJSON) {
            List<Book> bookList = new ArrayList<>();
            // If the JSON string is empty or null, then return early.
            if (TextUtils.isEmpty(bookJSON)) {
                return null;
            }
            try {
                JSONObject baseJsonResponse = new JSONObject(bookJSON);
                JSONArray itemsArray = baseJsonResponse.getJSONArray("items");

                for (int i = 0; i < itemsArray.length(); i++) {
                    // Extract out first book item
                    JSONObject responseObject = itemsArray.getJSONObject(i);
                    JSONObject volumeInfo = responseObject.getJSONObject("volumeInfo");
                    // Extract out author and title values
                    String actualAuthor = "N/A";
                    if (volumeInfo.has("authors")) {
                        JSONArray authorsArray = volumeInfo.getJSONArray("authors");
                        actualAuthor = authorsArray.getString(0);
                    }
                    String bookTitle = volumeInfo.getString("title");
                    // Create a new {@link Book} object
                    Book book = new Book(actualAuthor, bookTitle);
                    Log.d(LOG_TAG, "extractFeatureFromJson " + book.toString());
                    bookList.add(book);
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the book JSON results", e);
            }
            return bookList;
        }
    }
}
