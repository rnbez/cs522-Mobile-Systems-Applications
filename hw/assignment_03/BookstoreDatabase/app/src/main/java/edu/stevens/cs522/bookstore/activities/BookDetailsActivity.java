package edu.stevens.cs522.bookstore.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.entities.Author;
import edu.stevens.cs522.bookstore.entities.Book;

public class BookDetailsActivity extends Activity {

    Book book;
    TextView details_title, details_isbn, details_authors, details_price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        Intent callingIntent = getIntent();
        book = getIntent().getParcelableExtra(BookStoreActivity.BOOK_DETAILS_KEY);

        details_title = (TextView) findViewById(R.id.details_title);
        details_isbn = (TextView) findViewById(R.id.details_isbn);
        details_authors = (TextView) findViewById(R.id.details_authors);
        details_price = (TextView) findViewById(R.id.details_price);

        details_title.setText((CharSequence) "Title: " + book.getTitle());
        details_isbn.setText((CharSequence) "ISBN: " + book.getIsbn());

        details_price.setText((CharSequence) "Price: $" + String.valueOf(book.getPrice()));
        Author[] authors = book.getAuthors();
        StringBuilder authorsSb = new StringBuilder("Authors: ");
        authorsSb.append(authors[0].toString());
        if (authors.length > 1) {
            for (int i = 1; i < authors.length; i++) {
                authorsSb.append(", ")
                        .append(authors.toString());
            }
        }
        details_authors.setText((CharSequence) authorsSb.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // TODO display ORDER and CANCEL options.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bookstore_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.cancel: {
                Intent resultData = new Intent();
                setResult(Activity.RESULT_CANCELED, resultData);
                finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
