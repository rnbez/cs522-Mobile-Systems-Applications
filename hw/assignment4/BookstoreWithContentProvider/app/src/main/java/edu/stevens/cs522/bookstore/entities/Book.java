package edu.stevens.cs522.bookstore.entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import edu.stevens.cs522.bookstore.contracts.BookContract;

public class Book implements Parcelable {

    // PRIVATE FIELDS
    protected long id;

    protected String title;

    protected Author[] authors;

    protected String isbn;

    protected double price;

    public Book() {
    }

    public Book(long id, String title, Author[] authors, String isbn, double price) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.isbn = isbn;
        this.price = price;
    }

    public Book(Parcel in) {
        id = in.readLong();
        title = in.readString();
        authors = in.createTypedArray(Author.CREATOR);
        isbn = in.readString();
        price = in.readDouble();
    }

    public Book(Cursor in) {
        id = BookContract.getId(in);
        title = BookContract.getTitle(in);
        authors = BookContract.getAuthors(in);
        isbn = BookContract.getIsbn(in);
        price = BookContract.getPrice(in);
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    @Override
    public String toString() {
        return this.title + " - $" + this.price;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeTypedArray(authors, flags);
        dest.writeString(isbn);
        dest.writeDouble(price);
    }

    public void writeToProvider(ContentValues values) {
//        BookContract.putId(values, this.id);
        BookContract.putTitle(values, this.title);
        BookContract.putIsbn(values, this.isbn);
        BookContract.putPrice(values, this.price);
        BookContract.putAuthors(values, this.authors);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Author[] getAuthors() {
        return authors;
    }

    public void setAuthors(Author[] authors) {
        this.authors = authors;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
