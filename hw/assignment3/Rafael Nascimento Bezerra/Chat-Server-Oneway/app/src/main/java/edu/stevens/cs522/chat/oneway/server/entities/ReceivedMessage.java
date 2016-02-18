package edu.stevens.cs522.chat.oneway.server.entities;

/**
 * Created by Rafael on 2/12/2016.
 */
public class ReceivedMessage
{
    private String author;
    private String message;

    public ReceivedMessage() {
    }

    public ReceivedMessage(String author, String message) {
        this.author = author;
        this.message = message;
    }

    public ReceivedMessage(String message) {
        String[] splitted = message.split("#");
        this.author = splitted[0];
        this.message = splitted[1];
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
