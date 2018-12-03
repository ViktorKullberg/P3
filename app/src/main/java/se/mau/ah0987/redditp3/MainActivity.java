package se.mau.ah0987.redditp3;

import android.content.Intent;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

/**
 * MainActivity, initiates the app and creates the controller which handles all the fragments
 * Authors:
 */
public class MainActivity extends AppCompatActivity {
    private Controller controller;
    private Toolbar actionToolbar;
    private static final String STATE = "MY_RANDOM_STRING_123456";
    private boolean redditlogin = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(actionToolbar);
        controller = new Controller(this, savedInstanceState);
    }

    public void setRedditLogin(boolean loginState){
        redditlogin = loginState;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(redditlogin && getIntent()!=null && getIntent().getAction()!=null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            Uri uri = getIntent().getData(); //ONLY reddit login will get here
            if(uri.getQueryParameter("error") != null) {
                String error = uri.getQueryParameter("error");
                Log.e("Error", "An error has occurred : " + error);
            } else {
                String state = uri.getQueryParameter("state");
                if (state != null && state.equals(STATE)) {
                    String code = uri.getQueryParameter("code");
                    controller.getAccessToken(code);
                }
            }
        }
        controller.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        controller.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
