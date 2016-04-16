package edu.stevens.cs522.chat.oneway.server.activities.fragments;


import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import edu.stevens.cs522.chat.oneway.server.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewChatFragment extends DialogFragment {

    public static final String DIALOG_KEY = "dialog_id";

    public static void launch(Activity context, int dialogId, String tag) {
        NewChatFragment dialog = new NewChatFragment();
        Resources res = context.getResources();
        Bundle args = new Bundle();
        args.putInt(DIALOG_KEY, dialogId);
        dialog.setArguments(args);
        FragmentManager fm = ((FragmentActivity) context).getSupportFragmentManager();
        dialog.show(fm, tag);

    }

    private INewChatFragmentListener listener;

    private int dialogId;

    public interface INewChatFragmentListener {
        public void newChatAcknowledge(Dialog dialog, boolean confirm, String inputText);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof INewChatFragmentListener)) {
            throw new IllegalStateException("Activity must implement INewChatFragmentListener");
        }
        listener = (INewChatFragmentListener) activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialogId = getArguments().getInt(DIALOG_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag__new_chat, container, false);

        final EditText inputText = (EditText) view.findViewById(R.id.frag__new_chat_input_text);
        Button confirmButton = (Button) view.findViewById(R.id.frag__new_chat_confirm);
        Button cancelButton = (Button) view.findViewById(R.id.frag__new_chat_cancel);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.newChatAcknowledge(NewChatFragment.this.getDialog(), true, inputText.getText().toString());

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.newChatAcknowledge(NewChatFragment.this.getDialog(), false, null);
            }
        });


        return view;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
