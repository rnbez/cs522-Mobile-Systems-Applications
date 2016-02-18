package edu.stevens.cs522.bookstore.entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import edu.stevens.cs522.bookstore.contracts.BookContract;

public class Book implements Parcelable {

    // PRIVATE FIELDS
    protected int id;

    protected String title;

    protected Author[] authors;

    protected String isbn;

    protected double price;

    public Book() {
    }

    public void writeToProvider(ContentValues values) {
        BookContract.putId(values, this.id);
        BookContract.putTitle(values, this.title);
        BookContract.putIsbn(values, this.isbn);
        BookContract.putPrice(values, this.price);
    }

    public Book(int id, String title, Author[] author, String isbn, double price) {
        this.id = id;
        this.title = title;
        this.authors = author;
        this.isbn = isbn;
        this.price = price;
    }


    protected Book(Parcel in) {
        id = in.readInt();
        title = in.readString();
        authors = in.createTypedArray(Author.CREATOR);
        isbn = in.readString();
        price = in.readDouble();
    }

    public Book(Cursor in) {
        this.id = BookContract.getId(in);
        this.title = BookContract.getTitle(in);
        this.isbn = BookContract.getIsbn(in);
        this.price = BookContract.getPrice(in);
        this.authors = BookContract.getAuthors(in);
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeTypedArray(authors, flags);
        dest.writeString(isbn);
        dest.writeDouble(price);
    }

    @Override
    public String toString() {
        return this.title + " - $" + this.price;
    }

    //GETTERS AND SETTERS
    public int getId() {
        return id;
    }

    public void setId(int id) {
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
