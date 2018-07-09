package com.example.bertogonz3000.simplechat;

import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    static final int MAX_CHAT_MESSAGES_TO_SHOW = 50;
    static final String USER_ID_KEY = "userId";
    static final String BODY_KEY = "body";
    RecyclerView rvChat;
    ArrayList<Message> mMessages;
    ChatAdapter mAdapter;
    // Keep track of initial load to scroll to the bottom of the ListView
    boolean mFirstLoad;


    EditText etMessage;
    Button btSend;


    static final String TAG = ChatActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // User login
        if (ParseUser.getCurrentUser() != null) { // start with existing user
            startWithCurrentUser();
        } else { // If not logged in, login as a new anonymous user
            login();
        }

        refreshMessages();

    }

    // Get the userId from the cached currentUser object
    void startWithCurrentUser() {
        setupMessagePosting();

    }

    void setupMessagePosting() {

        // Find the text field and button
        final EditText etMessage;
        etMessage = (EditText) findViewById(R.id.etMessage);
        btSend = (Button) findViewById(R.id.btSend);
        rvChat = (RecyclerView) findViewById(R.id.rvChat);
        mMessages = new ArrayList<>();
        mFirstLoad = true;
        final String userId = ParseUser.getCurrentUser().getObjectId();
        mAdapter = new ChatAdapter(ChatActivity.this, userId, mMessages);
        rvChat.setAdapter(mAdapter);

        // associate the LayoutManager with the RecylcerView
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        rvChat.setLayoutManager(linearLayoutManager);


        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = etMessage.getText().toString();

                //ParseObject message = ParseObject.create("Message");
                //message.put(USER_ID_KEY, ParseUser.getCurrentUser().getObjectId());
                //message.put(BODY_KEY, data);
                //message.saveInBackground(new SaveCallback() {

                Message message = new Message();
                message.setBody(data);
                message.setUserId(ParseUser.getCurrentUser().getObjectId());
                message.saveInBackground(new SaveCallback() {
                @Override
                    public void done(com.parse.ParseException e) {
                        if(e==null){
                            Toast.makeText(ChatActivity.this, "Successfully created message on Parse", Toast.LENGTH_SHORT).show();
                            refreshMessages();
                        }
                        else{
                            Log.e(TAG, "Failed to save message", e);
                        }
                    }
                });
                etMessage.setText(null);
            }
        });
    }

    // Query messages from Parse so we can load them into the chat adapter
    void refreshMessages() {
        // Construct query to execute
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class);
        // Configure limit and sort order
        query.setLimit(MAX_CHAT_MESSAGES_TO_SHOW);

        // get the latest 50 messages, order will show up newest to oldest of this group
        query.orderByDescending("createdAt");
        // Execute query to fetch all messages from Parse asynchronously
        // This is equivalent to a SELECT query with SQL
        query.findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> messages, com.parse.ParseException e) {
                if(e==null){
                    mMessages.clear();
                    mMessages.addAll(messages);
                    mAdapter.notifyDataSetChanged();
                    if(mFirstLoad){
                        rvChat.scrollToPosition(0);
                        mFirstLoad = false;
                    }
                }
                else{
                    Log.e("message", "Error Loading Messages" + e);
                }
            }
        });
    }


    // Create an anonymous user using ParseAnonymousUtils and set sUserId
    void login() {
        ParseAnonymousUtils.logIn(new LogInCallback() {
            @Override
            public void done(ParseUser user, com.parse.ParseException e) {

                if (e != null) {
                    Log.e(TAG, "Anonymous login failed: ", e);
                } else {
                    startWithCurrentUser();
                }

            }
        });
    }


}
