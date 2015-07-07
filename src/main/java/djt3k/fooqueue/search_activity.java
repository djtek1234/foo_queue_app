package djt3k.fooqueue;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import android.widget.ArrayAdapter;
import android.view.View;
import android.util.SparseBooleanArray;
import android.widget.TextView;


public class search_activity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ArrayList<String> music = new ArrayList<String>();
        String query;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        Intent intent = getIntent();

        music = intent.getStringArrayListExtra(MainActivity.EXTRA_MESSAGE);
        query = intent.getStringExtra(MainActivity.EXTRA_MESSAGE2);

        final ListView lstMusic = (ListView)findViewById(R.id.lstResults);
        final TextView musicText = (TextView)findViewById(R.id.txtTitle);

        musicText.setText("Results for " + "\"" + query + "\"" + " in Music:");

        final ArrayAdapter musicResults = new ArrayAdapter(this,
                R.layout.list_item, music);

        lstMusic.setAdapter(musicResults);
        lstMusic.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        Button butBack = (Button)findViewById(R.id.butBack);
        Button butReq = (Button)findViewById(R.id.butReq);

        butReq.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        String selected = "";

                        SparseBooleanArray checked = lstMusic.getCheckedItemPositions();
                        if (checked.size() > 0) {
                            for (int i = 0; i < lstMusic.getAdapter().getCount(); i++) {
                                if (checked.get(i)) {
                                    selected = lstMusic.getAdapter().getItem(i).toString();
                                }
                            }
                            goBackRequest(selected);
                        }
                        else
                        {
                            showAlert("Nothing selected", 0);
                        }

                    }

                } );

        butBack.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        goBack();
                    }
                } );

    }

    public void showAlert(String message, final int action){
        //actions: 0 - nothing, 1 - return to main screen, 2 -
        AlertDialog alertDialog = new AlertDialog.Builder(search_activity.this).create();
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

    public void goBack()
    {
        Intent output = new Intent(this, MainActivity.class);
        setResult(RESULT_CANCELED, output);
        finish();
    }

    public void goBackRequest(String selected)
    {
        Intent output = new Intent(this, MainActivity.class);
        output.putExtra("selected", selected);
        setResult(RESULT_OK, output);
        finish();
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity2, menu);


        return true;
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
