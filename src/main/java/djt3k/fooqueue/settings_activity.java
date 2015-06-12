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
import android.widget.Button;
import android.widget.TextView;


public class settings_activity extends ActionBarActivity {
    String pubKey, subKey, channel, origin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        Intent intent = getIntent();

        pubKey = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        subKey = intent.getStringExtra(MainActivity.EXTRA_MESSAGE2);
        channel = intent.getStringExtra(MainActivity.EXTRA_MESSAGE3);
        origin = intent.getStringExtra(MainActivity.EXTRA_MESSAGE4);

        Button butOkay = (Button)findViewById(R.id.butBack2);
        Button butCancel = (Button)findViewById(R.id.butCancel2);

        final TextView txtPub = (TextView)findViewById(R.id.txtPubKey);
        txtPub.setText(pubKey);

        final TextView txtSub = (TextView)findViewById(R.id.txtSubKey);
        txtSub.setText(subKey);

        final TextView txtChann = (TextView)findViewById(R.id.txtChannel);
        txtChann.setText(channel);

        final TextView txtOrig = (TextView)findViewById(R.id.txtOrigin);
        txtOrig.setText(origin);


        butOkay.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v)
                    {
                        pubKey = txtPub.getText().toString();
                        subKey = txtSub.getText().toString();
                        channel = txtChann.getText().toString();
                        origin = txtOrig.getText().toString();

                        if ((pubKey.length() > 0) && (subKey.length() > 0) && (channel.length() > 0) && (origin.length() > 0))
                        {
                            goBack();
                        } else {
                            showAlert("Empty fields are not allowed!", 0);
                        }

                    }

                });

        butCancel.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        goBackCancel();
                    }
                } );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity5, menu);
        return true;
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

    public void showAlert(String message, final int action){
        //actions: 0 - nothing, 1 - return to main screen, 2 -
        AlertDialog alertDialog = new AlertDialog.Builder(settings_activity.this).create();
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
        output.putExtra("pubkey", pubKey);
        output.putExtra("subkey", subKey);
        output.putExtra("channel", channel);
        output.putExtra("origin", origin);
        setResult(RESULT_OK, output);
        finish();
    }

    public void goBackCancel()
    {
        Intent output = new Intent(this, MainActivity.class);
        setResult(RESULT_CANCELED, output);
        finish();
    }
}
