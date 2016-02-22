package edu.stevens.cs522.bookstore.entities;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import edu.stevens.cs522.bookstore.contracts.AuthorContract;

public class Author implements Parcelable {

    // DONE: Modify this to implement the Parcelable interface.

    // NOTE: middleInitial may be NULL!

    protected long id;
    protected String firstName;
    protected String middleInitial;
    protected String lastName;
    protected long bookID;


    public Author(String firstName, String middleInitial, String lastName) {
        this.firstName = firstName;
        if (middleInitial != null || !middleInitial.isEmpty()) {
            this.middleInitial = " " + middleInitial + " ";
        } else {
            this.middleInitial = " ";
        }
        this.lastName = lastName;
    }

    public Author(String fullName){
        String[] authorNameArr = fullName.trim().split(" ");

        if(authorNameArr.length < 2) throw new IllegalArgumentException("invalid full name: " + fullName);

        this.firstName = authorNameArr[0].trim();
        this.lastName = authorNameArr[authorNameArr.length - 1].trim();

        if(authorNameArr.length > 2 ){
            this.middleInitial = " " + authorNameArr[1].trim() + " ";
        }
        else{
            this.middleInitial = " ";
        }
    }

    protected Author(Parcel in) {
        id = in.readLong();
        firstName = in.readString();
        middleInitial = in.readString();
        lastName = in.readString();
        bookID = in.readLong();
    }

    public static final Creator<Author> CREATOR = new Creator<Author>() {
        @Override
        public Author createFromParcel(Parcel in) {
            return new Author(in);
        }

        @Override
        public Author[] newArray(int size) {
            return new Author[size];
        }
    };

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(firstName);
        dest.writeString(middleInitial);
        dest.writeString(lastName);
        dest.writeLong(bookID);
    }


    public void writeToProvider(ContentValues values) {
//        BookContract.putId(values, this.id);
        AuthorContract.putFirstName(values, firstName);
        AuthorContract.putMiddleName(values, middleInitial);
        AuthorContract.putLastName(values, lastName);
        AuthorContract.putBookId(values, bookID);
    }

    @Override
    public String toString() {
        return this.firstName + this.middleInitial + this.lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleInitial() {
        return middleInitial;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBookID() {
        return bookID;
    }

    public void setBookID(long bookID) {
        this.bookID = bookID;
    }
}
