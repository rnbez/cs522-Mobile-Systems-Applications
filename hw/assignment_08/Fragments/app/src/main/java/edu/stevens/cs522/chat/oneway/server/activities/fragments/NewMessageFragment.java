package edu.stevens.cs522.chat.oneway.server.activities.fragments;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.stevens.cs522.chat.oneway.server.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewMessageFragment extends DialogFragment {


    public NewMessageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag__new_message, container, false);
    }

}