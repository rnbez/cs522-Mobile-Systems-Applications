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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

import edu.stevens.cs522.chat.oneway.server.R;
import edu.stevens.cs522.chat.oneway.server.entities.Chatroom;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewMessageFragment extends DialogFragment {

    public static final String DIALOG_KEY = "dialog_id";
    public static final String CHATROOM_LIST_KEY = "chatroom_list";
    public static final String CHATROOM_SELECTED_KEY = "chatroom_selected";

    private INewMessageFragmentListener listener;
    private ArrayAdapter<String> arrayAdapter;
    private int dialogId;
    private ArrayList<String> chatroomList;


    public static void launch(Activity context, int dialogId, String tag, ArrayList<Chatroom> chatrooms, int selected) {
        NewMessageFragment dialog = new NewMessageFragment();
        Resources res = context.getResources();
        Bundle args = new Bundle();
        args.putInt(DIALOG_KEY, dialogId);
        args.putParcelableArrayList(CHATROOM_LIST_KEY, chatrooms);
        args.putInt(CHATROOM_SELECTED_KEY, selected);
        dialog.setArguments(args);
        FragmentManager fm = ((FragmentActivity) context).getSupportFragmentManager();
        dialog.show(fm, tag);

    }

    public interface INewMessageFragmentListener {
        public void newMessageAcknowledge(Dialog dialog, boolean confirm, String inputText, Chatroom selectedChatroom);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof INewMessageFragmentListener)) {
            throw new IllegalStateException("Activity must implement INewChatFragmentListener");
        }
        listener = (INewMessageFragmentListener) activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialogId = getArguments().getInt(DIALOG_KEY);
        ArrayList<Chatroom> list = getArguments().getParcelableArrayList(CHATROOM_LIST_KEY);
        chatroomList = new ArrayList<>();
        for (Chatroom c :
                list) {
            chatroomList.add(c.getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag__new_message, container, false);

        final Spinner chatroomSpinner = (Spinner) view.findViewById(R.id.frag__new_message_chatroom);
        final EditText inputText = (EditText) view.findViewById(R.id.frag__new_message_input_text);
        Button confirmButton = (Button) view.findViewById(R.id.frag__new_message_confirm);
        Button cancelButton = (Button) view.findViewById(R.id.frag__new_message_cancel);

        arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, chatroomList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chatroomSpinner.setAdapter(arrayAdapter);
        chatroomSpinner.setSelection(getArguments().getInt(CHATROOM_SELECTED_KEY));

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Chatroom> list = getArguments().getParcelableArrayList(CHATROOM_LIST_KEY);
                Chatroom chat = list.get(chatroomSpinner.getSelectedItemPosition());
                String text = inputText.getText().toString();
                listener.newMessageAcknowledge(NewMessageFragment.this.getDialog(), true, text, chat);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.newMessageAcknowledge(NewMessageFragment.this.getDialog(), false, null, null);
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
