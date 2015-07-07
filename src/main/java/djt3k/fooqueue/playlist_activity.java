package djt3k.fooqueue;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


public class playlist_activity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_activity);

        ArrayList<String> playlist = new ArrayList<String>();

        Intent intent = getIntent();

        playlist = intent.getStringArrayListExtra(MainActivity.EXTRA_MESSAGE);

        final ListView lstPlaylist = (ListView)findViewById(R.id.lstPlaylist);

        final ArrayAdapter playlistResults = new ArrayAdapter(this, R.layout.list_item, playlist);

        lstPlaylist.setAdapter(playlistResults);
        lstPlaylist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        Button butBack = (Button)findViewById(R.id.butBack2);
        Button butPlay = (Button)findViewById(R.id.butPlPlay);
        Button butRemove = (Button)findViewById(R.id.butRemove);

        //Play button
        butPlay.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        String action = "";

                        SparseBooleanArray checked = lstPlaylist.getCheckedItemPositions();
                        if (checked.size() > 0) {
                            for (int i = 0; i < lstPlaylist.getAdapter().getCount(); i++) {
                                if (checked.get(i)) {
                                    action = lstPlaylist.getAdapter().getItem(i).toString();
                                }
                            }
                            goBackPlay(action);
                        }
                        else
                        {
                            showAlert("Nothing selected", 0);
                        }

                    }

                } );

        //Remove button
        butRemove.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        String action = "";

                        SparseBooleanArray checked = lstPlaylist.getCheckedItemPositions();
                        if (checked.size() > 0) {
                            for (int i = 0; i < lstPlaylist.getAdapter().getCount(); i++) {
                                if (checked.get(i)) {
                                    action = lstPlaylist.getAdapter().getItem(i).toString();
                                }
                            }
                            goBackRemove(action);
                        }
                        else
                        {
                            showAlert("Nothing selected", 0);
                        }

                    }

                } );

        //Back button
        butBack.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        goBack();
                    }
                } );

    }

    public void goBackPlay(String selected)
    {
        Intent output = new Intent(this, MainActivity.class);
        output.putExtra("play", selected);
        output.putExtra("remove", "");
        setResult(RESULT_OK, output);
        finish();
    }

    public void goBackRemove(String selected)
    {
        Intent output = new Intent(this, MainActivity.class);
        output.putExtra("remove", selected);
        output.putExtra("play", "");
        setResult(RESULT_OK, output);
        finish();
    }

    public void goBack()
    {
        Intent output = new Intent(this, MainActivity.class);
        setResult(RESULT_CANCELED, output);
        finish();
    }

    public void showAlert(String message, final int action){
        //actions: 0 - nothing, 1 - return to main screen, 2 -
        AlertDialog alertDialog = new AlertDialog.Builder(playlist_activity.this).create();
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
