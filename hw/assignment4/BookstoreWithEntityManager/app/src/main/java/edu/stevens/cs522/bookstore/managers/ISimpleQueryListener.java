package edu.stevens.cs522.bookstore.managers;

import java.util.List;

/**
 * Created by Rafael on 2/21/2016.
 */
public interface ISimpleQueryListener<T> {
    public void handleResults(List<T> results);
}
