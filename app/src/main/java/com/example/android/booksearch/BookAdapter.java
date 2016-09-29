package com.example.android.booksearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * An {@link BookAdapter} knows how to create a list item layout for each book in the data source
 * (a list of {@link Book} objects).
 *
 * These list item layouts will be provided an adapter view like listView to be displayed to the user.
 */
public class BookAdapter extends ArrayAdapter<Book> {

    /**
     * Constructs a new {@link BookAdapter}
     *
     * @param context of the app
     * @param books   is the list of books, which is the data source of the adapter
     */
    public BookAdapter(Context context, ArrayList<Book> books) {
        super(context, 0, books);
    }

    /**
     * Returns a list item view that displays information about the book at the given position
     * in the list of books
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if there is an existing list item view (called convertView) that we can reuse,
        // otherwise, if convertView is null, then inflate a new list item layout.
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.book_list_item, parent, false);

        }
        // Find the book at the given position in the list of books
        Book currentBook = getItem(position);

        // Find the TextView with the view ID author
        TextView authorView = (TextView) listItemView.findViewById(R.id.author);
        // Display the author of the current book in that TextView
        authorView.setText(currentBook.getAuthor());

        // Find the TextView with the view ID title
        TextView titleView = (TextView) listItemView.findViewById(R.id.title);
        // Display the title of the current book in that TextView
        titleView.setText(currentBook.getTitle());

        // Return the list item view that is now showing the appropriate data
        return listItemView;
    }
}