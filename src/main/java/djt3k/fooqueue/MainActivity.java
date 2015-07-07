package djt3k.fooqueue;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;

import java.io.InputStreamReader;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;

import java.io.OutputStream;
import java.io.FileOutputStream;

import android.widget.ListView;
import android.widget.TextView;
import android.os.StrictMode;
import java.util.regex.*;
import com.pubnub.api.*;
import org.json.*;
import org.json.JSONException;



public class MainActivity extends ActionBarActivity {

    public final static String EXTRA_MESSAGE = "djt3k.fooqueue.MESSAGE";
    public final static String EXTRA_MESSAGE2 = "djt3k.fooqueue.MESSAGE2";
    public final static String EXTRA_MESSAGE3 = "djt3k.fooqueue.MESSAGE3";
    public final static String EXTRA_MESSAGE4 = "djt3k.fooqueue.MESSAGE4";

    //public final static String

    public static final String PREFS_NAME = "FooQueuePrefs";

    ArrayList<String> searchResults = new ArrayList<String>();
    ArrayList<String> playlistResults = new ArrayList<String>();

    String searchData;
    boolean searchRes = false;

    int playlistAction = -1;
    String playlistString = "";

    // Create global buttons
    Button button;
    Button button2;
    Button button3;
    Button button4;
    Button button5;
    Button button6;
    Button button7;
    Button button8;
    Button button9;
    Button button10;
    Button button11;
    Button button12;
    Button button13;
    TextView currentTrack;

    // Global server instance..to be deprecaed
    //commServer serv;

    //Pubnub stuff..pull data from config settings
    Pubnub pubn;
    String channel, pubKey, subKey, origin;
    boolean subscribed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button)findViewById(R.id.butSearch);
        button2 = (Button)findViewById(R.id.butSub);
        button3 = (Button)findViewById(R.id.butUp);
        button4 = (Button)findViewById(R.id.butDown);
        button5 = (Button)findViewById(R.id.butMute);
        button6 = (Button)findViewById(R.id.butSettings);
        button7 = (Button)findViewById(R.id.butPlPlay);
        button8 = (Button)findViewById(R.id.butStop);
        button9 = (Button)findViewById(R.id.butPause);
        button10 = (Button)findViewById(R.id.butSkipLeft);
        button11 = (Button)findViewById(R.id.butSkipRight);
        button12 = (Button)findViewById(R.id.butClear);
        button13 = (Button)findViewById(R.id.butPlaylist);

        //ArrayAdapter playlistResults = new ArrayAdapter(this, R.layout.list_item);
        //lstPl.setAdapter(playlistResults);

        currentTrack = (TextView)findViewById(R.id.txtTrack);


        //need to do networking...telnet not supported
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                //.detectNetwork() // or .detectAll() for all detectable problems
                //.penaltyDialog()  //show a dialog
                .permitNetwork() //permit Network access
                .build());

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        channel = settings.getString("channel", "test_channel");
        pubKey = settings.getString("pubkey", "pub-c-a8e83541-1fef-4a25-8f45-44481226dff4");
        subKey = settings.getString("subkey", "sub-c-704f80ce-faa1-11e4-b360-02ee2ddab7fe");
        origin = settings.getString("origin", "http://pubsub.pubnub.com");
        //boolean silent = settings.getBoolean("silentMode", false);


        //create an instance of commServer...not best code practice
        //serv = new commServer();

        //Two different databases in use here. Master is music sorted by genre - artist - album - song
        //Singles is music stored by Year - Month - Genre - Song
        //String master_db_path = "master.db";
        //String singles_db_path = "singles.db";

        //masterDb = openDatabase(master_db_path, "Music");
        //singlesDb = openDatabase(singles_db_path, "Singles");

        /*
        pubn = new Pubnub(
                "pub-c-a8e83541-1fef-4a25-8f45-44481226dff4",  // PUBLISH_KEY   (Optional, supply "" to disable)
                "sub-c-704f80ce-faa1-11e4-b360-02ee2ddab7fe",  // SUBSCRIBE_KEY (Required)
                "",      // SECRET_KEY    (Optional, supply "" to disable)
                "",      // CIPHER_KEY    (Optional, supply "" to disable)
                false    // SSL_ON?
        );
        */
        //channel = "test_channel";

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //return from search view
        if (requestCode == 1)
        {
            if(resultCode == RESULT_OK)
            {
                //searchData = data.getStringArrayListExtra("selected");
                searchData = data.getStringExtra("selected");
                searchRes = true;
            }
            if (resultCode == RESULT_CANCELED)
            {
                searchRes = false;
                searchData = "";
                //Write your code if there's no result
            }
            searchResults.clear();
        }

        //return from settings view
        if (requestCode == 2)
        {
            if(resultCode == RESULT_OK)
            {
                //set globals and configs
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();

                editor.putString("channel", data.getStringExtra("channel"));
                channel = data.getStringExtra("channel");

                editor.putString("pubkey", data.getStringExtra("pubkey"));
                pubKey = data.getStringExtra("pubkey");

                editor.putString("subkey", data.getStringExtra("subkey"));
                subKey = data.getStringExtra("subkey");

                editor.putString("origin", data.getStringExtra("origin"));
                origin = data.getStringExtra("origin");

                editor.commit();

            }

            if (resultCode == RESULT_CANCELED)
            {
                //do nothing
            }
        }

        if (requestCode == 3)
        {
            String play = "";
            String remove = "";

            if(resultCode == RESULT_OK)
            {
                play = data.getStringExtra("play");
                remove = data.getStringExtra("remove");

                if (!play.isEmpty())
                {
                    //play request has come in
                    playlistString = play;
                    playlistAction = 0;
                }
                else if (!remove.isEmpty())
                {
                    //remove song has come in
                    playlistString = remove;
                    playlistAction = 1;
                }
                else
                {
                    //both are empty
                    playlistString = "";
                    playlistAction = -1;
                }
            }
            if (resultCode == RESULT_CANCELED)
            {
                playlistString = "";
                playlistAction = -1;
            }

            //clear playlist results list
            playlistResults.clear();
        }

    }

    protected void onResume()
    {
        super.onResume();
        //if(serv.connected && (searchRes == true))
        if (searchRes == true)
        {
            searchRes = false;
            pubNubSendRequest();
        }

        //playlist action -1 - nothing, 0 - play, 1 - remove
        if (playlistAction == -1)
        {
            //continue on
        }
        else if (playlistAction == 0)
        {
            //play a song in playlist
            playlistAction = -1;
            pubNubPlaySong();

        }
        else if (playlistAction == 1)
        {
            //remove a song from playlist
            playlistAction = -1;
            pubNubRemoveSong();

        }
    }

    /*
   protected void onStop()
   {
        //masterDb.close();

   }
   */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //search textbox
        final EditText mEdit = (EditText)findViewById(R.id.txtInput);

        // Search button listener
        button.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        String query;

                        query = mEdit.getText().toString();
                        query = query.toLowerCase();

                        if (subscribed)
                        {

                            if (query.isEmpty())
                            {
                                showAlert("Nothing entered!", 0);
                            }
                            else
                            {
                                //global variable
                                pubNubSearchQuery(query);

                                if (searchResults.isEmpty()) {
                                    mEdit.setText("");
                                    showAlert("No entry found!", 0);
                                } else {
                                    mEdit.setText("");
                                    searchMusic(v, query, searchResults);
                                }
                            }
                        }
                        else
                        {
                            showAlert("Not subscribed to an active channel!", 0);
                        }
                    }
                } );

        // Subscribe button listener
        button2.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        if (!subscribed) {
                            //create popup window and return result
                            //showPopup();
                            startPubNub();
                            if (pubnubSubscribe())
                            {
                                //do nothing
                                button2.setText("UNSUBSCRIBE");
                            }
                            else
                            {
                                //hmm
                            }
                        }
                        else
                        {
                            if (pubnubUnsubscribe()){
                                subscribed = false;
                                button2.setText("SUBSCRIBE");
                            }
                        }
                    }
                }
        );

        // Volume UP
        button3.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        if (subscribed)
                        {
                            pubNubVolumeControl(0);
                        }
                        else
                        {
                            //do nothing
                        }
                    }
                }
        );

        // Volume DOWN
        button4.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        if (subscribed)
                        {
                            pubNubVolumeControl(1);
                        }
                        else
                        {
                            //do nothing
                        }
                    }
                }
        );

        // Volume MUTE
        button5.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        if (subscribed)
                        {
                            pubNubVolumeControl(2);
                        }
                        else
                        {
                            //do nothing
                        }
                    }
                }
        );

        //Settings button
        button6.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        if (subscribed)
                        {
                            showAlert("Cannot change settings while subscribed to channel!", 0);
                        }
                        else
                        {
                            launchSettings(v);
                        }
                    }
                }
        );

        //Play button
        button7.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        if (subscribed)
                        {
                            pubNubPlayControl(0);

                        }
                        else
                        {
                            //not subscribed..do nothing
                        }
                    }
                }
        );

        //Stop button
        button8.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        if (subscribed)
                        {
                            pubNubPlayControl(1);
                        }
                        else
                        {
                            //not subscribed..do nothing
                        }
                    }
                }
        );

        //Pause button
        button9.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        if (subscribed)
                        {
                            pubNubPlayControl(2);
                        }
                        else
                        {
                            //not subscribed..do nothing
                        }
                    }
                }
        );

        //Skip left button
        button10.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        if (subscribed)
                        {
                            pubNubPlayControl(3);
                        }
                        else
                        {
                            //not subscribed..do nothing
                        }
                    }
                }
        );

        //Skip right button
        button11.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        if (subscribed)
                        {
                            pubNubPlayControl(4);
                        }
                        else
                        {
                            //not subscribed..do nothing
                        }
                    }
                }
        );

        //Clear button
        button12.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        mEdit.setText("");
                    }
                }
        );

        //Playlist button
        button13.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        if (subscribed)
                        {
                            if (pubNubGetPlaylist())
                            {
                                showPlaylist(v, playlistResults);
                            }
                            else
                            {

                            }

                        }
                        else
                        {
                            showAlert("Not subscribed to an active channel!", 0);
                        }
                    }
                }
        );
/*
        //Quit button
        button13.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        mEdit.setText("");
                    }
                }
        );
*/
        return true;
    }

    public boolean pubnubUnsubscribe()
    {
        JSONObject msg = new JSONObject();
        try
        {
            msg.put("UNSUBSCRIBE", "FooQueue Unsubscribing from channel");
        }
        catch (JSONException e){ return false;}

        pubn.unsubscribe(channel);

        return true;
    }

    public void startPubNub()
    {
        pubn = new Pubnub(
                pubKey,  // PUBLISH_KEY   (Optional, supply "" to disable)
                subKey,  // SUBSCRIBE_KEY (Required)
                "",      // SECRET_KEY    (Optional, supply "" to disable)
                "",      // CIPHER_KEY    (Optional, supply "" to disable)
                false    // SSL_ON?
        );
    }

    public void stopPubNub()
    {
        //pubn.shutdown();
    }

    public boolean pubnubSubscribe()
    {
        JSONObject msg = new JSONObject();
        try
        {
            msg.put("SUBSCRIBE", "FooQueue Subscribing to channel");
        }
        catch (JSONException e){ return false;}

        try {
            pubn.subscribe(channel, new Callback() {

                @Override
                public void connectCallback(String channel, Object message) {
                    Log.d("djt3k.fooqueue", "SUBSCRIBE : CONNECT on channel:" + channel + " : " + message.getClass() + " : " + message.toString());
                    subscribed = true;
                    //button2.setText("UNSUBSCRIBE");
                }

                @Override
                public void disconnectCallback(String channel, Object message) {
                    Log.d("djt3k.fooqueue", "SUBSCRIBE : DISCONNECT on channel:" + channel + " : " + message.getClass() + " : " + message.toString());
                    subscribed = false;
                    button2.setText("SUBSCRIBE");
                }

                public void reconnectCallback(String channel, Object message) {
                    Log.d("djt3k.fooqueue", "SUBSCRIBE : RECONNECT on channel:" + channel + " : " + message.getClass() + " : " + message.toString());
                }

                @Override
                public void successCallback(String channel, Object message) {
                    JSONObject resp = (JSONObject) message;
                    //String track;


                    try {
                        //Library search results array
                        if (resp.toString().contains("LIBRARY"))
                        {
                            ArrayList<String> results = new ArrayList<String>();
                            JSONArray arr = resp.getJSONArray("LIBRARY");

                            for (int i = 0; i < arr.length(); i++) {
                                results.add(arr.get(i).toString().replace("file://",""));
                                //searchResults.add(array.getJSONObject(i).getString("interestKey"));
                            }

                            searchResults = results;
                        }
                        //Current track playing
                        else if (resp.toString().contains("CURRENT_TRACK"))
                        {
                            final String track = resp.optString("CURRENT_TRACK", "");

                            //call into main thread to update the textview
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run()
                                {
                                    currentTrack.setText(track);
                                }
                            });

                        }
                        //Whole playlist
                        else if (resp.toString().contains("PLAYLIST"))
                        {
                            JSONArray arr = resp.getJSONArray("PLAYLIST");
                            //ArrayList<String> playlistResults = new ArrayList<String>();

                            for (int i = 0; i < arr.length(); i++)
                            {
                                playlistResults.add(arr.get(i).toString());
                            }

                            //final ArrayAdapter playlistAdapter = new ArrayAdapter(MainActivity.this, R.layout.list_item, playlistResults);

                            /*
                            //call into main thread to update the listview
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run()
                                {
                                    lstPl.setAdapter(playlistAdapter);
                                }
                            });
                            */

                        }
                        else if (resp.toString().contains("ERROR"))
                        {
                            final String error = resp.optString("ERROR", "");

                            //call into main thread to update the textview
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run()
                                {
                                    //showAlert(error, 0);
                                }
                            });

                        }
                        //return searchResults;
                    } catch (JSONException e) {
                    }

                    Log.d("djt3k.fooqueue", "SUBSCRIBE : " + channel + " : " + message.getClass() + " : " + message.toString());
                    //showAlert("Subscribed to channel " + channel, 0);
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    Log.d("djt3k.fooqueue", "SUBSCRIBE : ERROR on channel " + channel + " : " + error.toString());
                }
            });
        }
        catch (PubnubException e)
        {
            System.out.println(e.toString());
            return false;
        }

        Callback callback = new Callback() {
            public void successCallback(String channel, Object response) {
                Log.d("FooQueue",response.toString());
            }
            public void errorCallback(String channel, PubnubError error) {
                Log.d("FooQueue",error.toString());
            }
        };

        pubn.publish(channel, msg, callback);
        pubNubUpdateTrack();

        //showAlert("Subscribed to channel " + channel, 0);
        return true;

    }

    public boolean pubNubGetPlaylist()
    {
        JSONObject msg = new JSONObject();

        try
        {
            msg.put("GET_PLAYLIST", "Current Playlist");
        }
        catch (JSONException e){ return false; }

        Callback callback = new Callback() {
            public void successCallback(String channel, Object response)
            {
                //Log.d("FooQueue",response.toString());
            }
            public void errorCallback(String channel, PubnubError error)
            {
                //Log.d("FooQueue",error.toString());
            }
        };

        pubn.publish(channel, msg, callback);

        //prevent race condition
        try
        {
            Thread.sleep(500);                 //1000 milliseconds is one second.
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }


        return true;

    }

    public void pubNubUpdateTrack()
    {
        JSONObject msg = new JSONObject();

        try
        {
            msg.put("GET_TRACK", "Current Playing");
        }
        catch (JSONException e){}

        Callback callback = new Callback() {
            public void successCallback(String channel, Object response)
            {
                //Log.d("FooQueue",response.toString());
            }
            public void errorCallback(String channel, PubnubError error)
            {
                //Log.d("FooQueue",error.toString());
            }
        };

        pubn.publish(channel, msg , callback);

        //prevent race condition
        try
        {
            Thread.sleep(500);                 //1000 milliseconds is one second.
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
        //return searchResults;
    }

    //Send song request to Foobar2k
    public void pubNubSendRequest()
    {
        JSONObject msg = new JSONObject();

        try
        {
            msg.put("REQUEST", searchData);
        }
        catch (JSONException e){}

        Callback callback = new Callback() {
            public void successCallback(String channel, Object response)
            {
                //Log.d("FooQueue",response.toString());
            }
            public void errorCallback(String channel, PubnubError error)
            {
                //Log.d("FooQueue",error.toString());
            }
        };

        pubn.publish(channel, msg , callback);

        //prevent race condition
        try
        {
            Thread.sleep(500);                 //1000 milliseconds is one second.
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

    //play a song in the playlist
    public void pubNubPlaySong()
    {
        JSONObject msg = new JSONObject();

        try
        {
            msg.put("QUEUE_TRACK", playlistString);
        }
        catch (JSONException e){}

        playlistString = "";

        Callback callback = new Callback() {
            public void successCallback(String channel, Object response)
            {
                //Log.d("FooQueue",response.toString());
            }
            public void errorCallback(String channel, PubnubError error)
            {
                //Log.d("FooQueue",error.toString());
            }
        };

        pubn.publish(channel, msg , callback);

        //prevent race condition
        try
        {
            Thread.sleep(500);                 //1000 milliseconds is one second.
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

    //Remove a song from the playlist
    public void pubNubRemoveSong()
    {
        JSONObject msg = new JSONObject();

        try
        {
            msg.put("REMOVE_TRACK", playlistString);
        }
        catch (JSONException e){}

        playlistString = "";

        Callback callback = new Callback() {
            public void successCallback(String channel, Object response)
            {
                //Log.d("FooQueue",response.toString());
            }
            public void errorCallback(String channel, PubnubError error)
            {
                //Log.d("FooQueue",error.toString());
            }
        };

        pubn.publish(channel, msg , callback);

        //prevent race condition
        try
        {
            Thread.sleep(500);                 //1000 milliseconds is one second.
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

    public void pubNubPlayControl(int action)
    {
        //0 - Play, 1 - Stop, 2 - Pause, 3 - skip left, 4 - skip right
        JSONObject msg = new JSONObject();
        String query = "";

        try
        {
            switch (action)
            {
                case 0: query = "PLAY"; break;
                case 1: query = "STOP"; break;
                case 2: query = "PAUSE"; break;
                case 3: query = "SKIP_LEFT"; break;
                case 4: query = "SKIP_RIGHT"; break;
                default: query = "";
            }
            msg.put("CONTROL", query);
        }
        catch (JSONException e){}

        Callback callback = new Callback() {
            public void successCallback(String channel, Object response)
            {
                //Log.d("FooQueue",response.toString());
            }
            public void errorCallback(String channel, PubnubError error)
            {
                //Log.d("FooQueue",error.toString());
            }
        };

        pubn.publish(channel, msg , callback);

        try
        {
            Thread.sleep(500);                 //1000 milliseconds is one second.
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }

    }


    public void pubNubVolumeControl(int action)
    {
        //0 - up, 1 - down, 2 - mute

        JSONObject msg = new JSONObject();
        String query = "";

        try
        {
            switch (action)
            {
                case 0: query = "UP"; break;
                case 1: query = "DOWN"; break;
                case 2: query = "MUTE"; break;
                default: query = "";
            }
            msg.put("VOLUME", query);
        }
        catch (JSONException e){}

        Callback callback = new Callback() {
            public void successCallback(String channel, Object response)
            {
                //Log.d("FooQueue",response.toString());
            }
            public void errorCallback(String channel, PubnubError error)
            {
                //Log.d("FooQueue",error.toString());
            }
        };

        pubn.publish(channel, msg , callback);

        try
        {
            Thread.sleep(500);                 //1000 milliseconds is one second.
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }


    public void pubNubSearchQuery(String query)
    {
        JSONObject msg = new JSONObject();

        try
        {
            msg.put("SEARCH_QUERY", query);
        }
        catch (JSONException e){}

        Callback callback = new Callback() {
            public void successCallback(String channel, Object response)
            {
                //Log.d("FooQueue",response.toString());
            }
            public void errorCallback(String channel, PubnubError error)
            {
                //Log.d("FooQueue",error.toString());
            }
        };

        pubn.publish(channel, msg , callback);

        //prevent race condition
        try
        {
            Thread.sleep(500);                 //1000 milliseconds is one second.
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
        //return searchResults;
    }

    public int getSearchKey() {
        //searchData = type - key - artist - song
        String sub;

        sub = searchData.substring(searchData.indexOf("-") + 1, searchData.length() - 1);
        sub = sub.substring(0, searchData.indexOf("-") - 1).replace("-", "");
        //sub = searchData.substring(0, searchData.indexOf("-") - 1);

        return Integer.parseInt(sub.replace(" ", ""));
    }

    public String getSearchType() {
        //searchData = type - key - artist  song
        String sub;

        sub = searchData.substring(0, searchData.indexOf("-") - 1);

        return sub;
    }


    public void showAlert(String message, final int action){
        //actions: 0 - nothing, 1 - return to main screen, 2 -
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(message);

        alertDialog.setButton("Continue..", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //if (action == 1) goBack();
                //if (action == 1) goBackRequest(path);
            }
        });
        alertDialog.show();

    }


    /*
    private void showPopup() {
        LayoutInflater inflater = (LayoutInflater)
                MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View popupView = inflater.inflate(R.layout.server_popup, null, false);

        final PopupWindow pw = new PopupWindow(
                popupView,
                700,
                600,
                true);

        pw.setTouchable(true);
        pw.setFocusable(true);
        pw.showAtLocation(findViewById(R.id.app), Gravity.CENTER, 0, 0);

        Button close = (Button)popupView.findViewById(R.id.butClosePW);
        Button start = (Button)popupView.findViewById(R.id.butContPW);
        final EditText ipInput = (EditText)popupView.findViewById(R.id.editText);
        final EditText portInput = (EditText)popupView.findViewById(R.id.editText2);


        close.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        connectStatus = false;
                        pw.dismiss();
                    }
                }
        );

        start.setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        final String ip = ipInput.getText().toString();
                        final String port = portInput.getText().toString();

                        if (verifyIP(ip) && verifyPort(port)){
                            connectStatus = true;
                            serverIP = ip;
                            serverPort = Integer.parseInt(port);

                            int serverStatus = serv.startServer(serverIP, serverPort);

                            switch (serverStatus){
                                case SERVER_DOWN: serv.connected = false; showAlert("Server is not responding to requests.", 0); break;
                                case SERVER_UP: serv.connected = true; button4.setText("DISCONNECT"); changeStatus(true); break;
                                case SERVER_BUSY: serv.connected = false; showAlert("The server appears to be busy.", 0); break;
                                case SERVER_FULL: serv.connected = false; showAlert("There are too many connections on the server.", 0); break;
                                default: showAlert("Unexpected error returned.",0); serv.connected = false; break;
                            }
                            pw.dismiss();
                        }
                        else{
                            showAlert("IP or Port is not valid!", 0);
                        }
                    }
                }
        );

        //return connectStatus;
    }
    */

    /*
    public void changeStatus(boolean status){
        if (status){
            txtStatus.setText("CONNECTED @ " + serverIP);
            txtStatus.setTextColor(Color.GREEN);
        }
        else{
            txtStatus.setText("NOT RUNNING");
            txtStatus.setTextColor(Color.RED);
        }
    }
    */

    //Verify that the IP is valid
    public boolean verifyIP(String ip){
    //start with IP
        //Thanks to StackOverflow for the IP address validation code :)
        Pattern IP_ADDRESS
                = Pattern.compile(
                "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                        + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                        + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                        + "|[1-9][0-9]|[0-9]))");

        Matcher matcher = IP_ADDRESS.matcher(ip);
        if (matcher.matches()) {
            return true;
        }
        else
        {
            return false;
        }

    }

    //Verify that the port is valid
    public boolean verifyPort(String port){
        int sPort = 0;
        try {
            sPort = Integer.parseInt(port);
        }
        catch(Exception e) {
            sPort = 0;
        }

        if (sPort > 0 && sPort < 65535){
            return true;
        }
        else{
            return false;
        }

    }

    //database is local..for now
    //Todo: Remote database connection
    public SQLiteDatabase openDatabase(String path, String name){

        String assetPath = "/data/data/" + getPackageName() + "/";
        String dbPath = "/data/data/" + getPackageName() + "/" + path;

        copyDataBase(assetPath, path);

        try {
            SQLiteDatabase db;

            db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
            return db;
        }
        catch(SQLiteAbortException e){
            return null;
        }

    }

    public void copyDataBase(String assPath, String path){
        try {
            InputStream externalDbStream = getResources().getAssets().open(path);
            String outFileName = assPath + path;

            //Now create a stream for writing the database byte by byte
            OutputStream localDbStream = new FileOutputStream(outFileName);

            //Copying the database
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = externalDbStream.read(buffer)) > 0) {
                localDbStream.write(buffer, 0, bytesRead);
            }

            localDbStream.close();
            externalDbStream.close();

        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
    // Parse the music database
    public ArrayList parseMusic(String search, String field) {
        // 0 - key, 1 - genre, 2 - artist, 3 - album, 4 - song, 5 - path
        ArrayList<String> subList = new ArrayList<String>();

        String query = "";

        if (field.contains("Artist"))
            query = "SELECT * FROM music WHERE artist LIKE '%" + search + "%'";

        if (field.contains("Song"))
            query = "SELECT * FROM music WHERE song LIKE '%" + search + "%'";

        query = "SELECT * FROM music WHERE path LIKE '%" + search + "%'";

        try {
            Cursor c = masterDb.rawQuery(query, null);

            if (c.moveToFirst()) {
                do {
                    //subList.add(c.getString(1));
                    subList.add(c.getString(0) + " - " + c.getString(2) + " - " + c.getString(4));
                } while (c.moveToNext());
            }

            c.close();
        }
        catch (SQLiteAbortException e) {}

        return subList;
    }

    // Parse the singles database
    public ArrayList parseSingles(String search) {
        // 0 - key, 1 - year, 2 - month, 3 - genre, 4 - song
        ArrayList<String> subList = new ArrayList<String>();

        String query = "";

        query = "SELECT * FROM singles WHERE song LIKE '%" + search + "%'";

        try {
            Cursor c = singlesDb.rawQuery(query, null);

            if (c.moveToFirst()) {
                do {
                    //subList.add(c.getString(1));
                    subList.add(c.getString(0) + " - " + c.getString(4));
                } while (c.moveToNext());
            }

            c.close();
        }
        catch (SQLiteAbortException e) { //handle //
        }

        return subList;
    }
    */

    // This function deprecated. String clean operation performed in python script
    public static ArrayList cleanString(ArrayList<String> lst){

        String str = "";
        int p = 0;

        for (int i = 0; i < lst.size(); i++){
            str = lst.get(i).toString();
            str = str.replaceAll("_"," ");
            p = str.lastIndexOf('\\');
            if (p < 0) continue;
            lst.set(i, str.substring(p+1, str.length()));
        }
        return lst;

    }

    // This function deprecated. No longer reading file path.
    public ArrayList readFileAsList(String filePath){

        ArrayList<String> list = new ArrayList<String>();


        try {
            InputStream is = getResources().getAssets().open(filePath);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                list.add(line);
            }

            is.close();
            bufferedReader.close();

        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    // This function deprecated. No longer reading text files.
    public static StringBuffer readFileAsString(String filePath) {

        StringBuffer stringBuff = new StringBuffer();
        File file = new File(filePath);

        try {
            File fl = new File(filePath);
            FileReader fileReader = new FileReader(fl);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuff.append(line);
                stringBuff.append("\n");
            }
            fileReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuff;
    }

    public void searchMusic(View view, String query, ArrayList<String> music)
    {
        Intent intent = new Intent( getBaseContext() , search_activity.class );
        intent.putExtra(EXTRA_MESSAGE, music);
        //intent.putExtra(EXTRA_MESSAGE2, singles);
        intent.putExtra(EXTRA_MESSAGE2, query);
        startActivityForResult(intent, 1);
    }

    public void launchSettings(View view)
    {
        //pubkey, subkey, channel, origin - all globals
        Intent intent = new Intent( getBaseContext() , settings_activity.class );
        intent.putExtra(EXTRA_MESSAGE, pubKey);
        intent.putExtra(EXTRA_MESSAGE2, subKey);
        intent.putExtra(EXTRA_MESSAGE3, channel);
        intent.putExtra(EXTRA_MESSAGE4, origin);
        startActivityForResult(intent, 2);
    }

    public void showPlaylist(View view, ArrayList<String> playlist)
    {
        Intent intent = new Intent( getBaseContext() , playlist_activity.class );
        intent.putExtra(EXTRA_MESSAGE, playlist);
        startActivityForResult(intent, 3);
    }

    /*
    public void browseMusic(View view)
    {
        Intent intent = new Intent(this, MainActivity3.class);
        startActivity(intent);
    }

    public void browseSingles(View view)
    {
        Intent intent = new Intent(this, MainActivity4.class);
        startActivity(intent);
    }
    */

    /*
    public void serverButton(View view)
    {

    }
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}

