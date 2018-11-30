package se.mau.ah0987.redditp3;

import android.content.Intent;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;


public class MainActivity extends AppCompatActivity {
    private Controller controller;
    private Toolbar actionToolbar;
    private static final String STATE = "MY_RANDOM_STRING_123456";
    private boolean redditlogin = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ONCREATE", "ACTIVITY" + redditlogin);
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
        Log.d("TESTING", "HELLO" + redditlogin);
        if(redditlogin && getIntent()!=null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            Log.d("TESTING", "YES");
            Uri uri = getIntent().getData();
            if(uri.getQueryParameter("error") != null) {
                String error = uri.getQueryParameter("error");
                Log.e("LOL", "An error has occurred : " + error);
            } else {
                String state = uri.getQueryParameter("state");
                if(state.equals(STATE)) {
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
        outState.putBoolean("redditlogin", redditlogin);
        Log.d("OnsavedState", "test " + redditlogin);
        controller.onSaveInstanceState(outState);
    }
}
