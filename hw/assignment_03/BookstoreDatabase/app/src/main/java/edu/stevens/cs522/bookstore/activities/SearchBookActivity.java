package edu.stevens.cs522.bookstore.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;

import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.entities.Author;
import edu.stevens.cs522.bookstore.entities.Book;

public class SearchBookActivity extends Activity {

    // Use this as the key to return the book details as a Parcelable extra in the result intent.
    public static final String BOOK_RESULT_KEY = "book_result";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_book);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // TODO provide SEARCH and CANCEL options
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bookstore_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // TODO
        switch (item.getItemId()) {
            case R.id.add: {
                Intent resultData = new Intent();
                resultData.putExtra(BOOK_RESULT_KEY, searchBook());
                setResult(Activity.RESULT_OK, resultData);
                finish();
                return true;
            }
            case R.id.cancel: {
                Intent resultData = new Intent();
                setResult(Activity.RESULT_CANCELED, resultData);
                finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
        // SEARCH: return the book details to the BookStore activity

        // CANCEL: cancel the search request
    }

    public Book searchBook() {
        String title = ((TextView) findViewById(R.id.search_title)).getText().toString();
        String isbn = ((TextView) findViewById(R.id.search_isbn)).getText().toString();
        double price = Double.parseDouble(((TextView) findViewById(R.id.search_price)).getText().toString());
        return new Book(1, title, getSplittedAuthors(), isbn, price);
    }

    private Author[] getSplittedAuthors() {
        String authorsConcatedList = ((TextView) findViewById(R.id.search_author)).getText().toString();
        String[] stringAuthors = authorsConcatedList.split(",");
        Author[] result = new Author[stringAuthors.length];

        for (int i = 0; i < stringAuthors.length; i++) {
            String[] authorNameArr = stringAuthors[i].split(" ");
            Author author = new Author(authorNameArr[0].trim(), "", authorNameArr[authorNameArr.length - 1].trim());
            author.setMiddleInitial(authorNameArr.length > 2 ? authorNameArr[1].trim() : "");
            result[i] = author;
        }

        return result;
    }

}