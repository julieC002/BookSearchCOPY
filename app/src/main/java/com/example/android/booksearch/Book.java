package com.example.android.booksearch;

/**
 * A {@link Book} object contains information related to a single book.
 */
public class Book {

    /** Author of the book */
    private String mAuthor;

    /** Title of the book */
    private String mTitle;

    /**
     * Constructs a new {@link Book} object.
     *
     * @param author is the author of the book
     * @param title is the title of the book
     */
    public Book(String author, String title){
        mAuthor = author;
        mTitle = title;
    }

    /** Returns the author of the book */
    public String getAuthor(){
        return mAuthor;
    }

    /** Returns the title of the book */
    public String getTitle(){
        return mTitle;
    }

}