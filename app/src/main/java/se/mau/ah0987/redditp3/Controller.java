package se.mau.ah0987.redditp3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import se.mau.ah0987.redditp3.dialog.RedditLoginDialog;
import se.mau.ah0987.redditp3.dialog.TwitterLoginDialog;
import se.mau.ah0987.redditp3.entity.Post;
import se.mau.ah0987.redditp3.entity.PostTest;
import se.mau.ah0987.redditp3.fragment.MergedFragment;
import se.mau.ah0987.redditp3.fragment.RedditFragment;
import se.mau.ah0987.redditp3.fragment.TwitterFragment;
import twitter4j.auth.RequestToken;

public class Controller {
    private MainActivity mainActivity;
    private BottomNavigationView bottomNavigation;
    private FragmentManager fragmentManager;
    private TwitterFragment twitterFragment;
    private MergedFragment mergedFragment;
    private RedditFragment redditFragment;
    private TwitterLoginDialog twitterLoginDialog;
    private RedditLoginDialog redditLoginDialog;
    private String currentFragmentTag;

    private static final String AUTH_URL =
            "https://www.reddit.com/api/v1/authorize.compact?api_key&client_id=%s" +
                    "&response_type=code&state=%s&redirect_uri=%s&" +
                    "duration=permanent&scope=identity+read+mysubreddits";

    private static final String CLIENT_ID = "7_UNbRy-oSBuog";

    private static final String REDIRECT_URI = "http://www.example.com/my_redirect";

    private static final String STATE = "MY_RANDOM_STRING_123456";

    private static final String ACCESS_TOKEN_URL =
            "https://www.reddit.com/api/v1/access_token";

    private String accessToken;
    private String refreshToken;
    private List<PostTest> posts = new ArrayList<PostTest>();


    public Controller(MainActivity mainActivity, Bundle savedInstanceState) {
        this.mainActivity = mainActivity;
        fragmentManager = mainActivity.getSupportFragmentManager();
        initializeBottomNavigation();
        initializeFragments(savedInstanceState);
        initializeDialogs();
    }

    public void onResume() {
        Fragment fragment = fragmentManager.findFragmentByTag(currentFragmentTag);
        if(fragment != null) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.show(fragmentManager.findFragmentByTag(currentFragmentTag));
            ft.commit();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putString("currentFragmentTag", currentFragmentTag);
    }

    private void initializeBottomNavigation() {
        bottomNavigation = (BottomNavigationView) mainActivity.findViewById(R.id.navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation1_merged:
                        showFragment("MergedFragment");
                        return true;
                    case R.id.navigation2_twitter:
                        showFragment("TwitterFragment");
                        twitterFragment.getTweets();
                        return true;
                    case R.id.navigation3_reddit:
                        showFragment("RedditFragment");
                        return true;
                }
                return false;
            }
        });
    }

    private void initializeFragments(Bundle savedInstanceState) {
        if(savedInstanceState == null) {
            mergedFragment = new MergedFragment();
            twitterFragment = new TwitterFragment();
            redditFragment = new RedditFragment();
            currentFragmentTag = "MergedFragment";

            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.add(R.id.fragment_container, mergedFragment, "MergedFragment");
            ft.add(R.id.fragment_container, twitterFragment, "TwitterFragment");
            ft.add(R.id.fragment_container, redditFragment, "RedditFragment");
            ft.commit();
        } else {
            mergedFragment = (MergedFragment)fragmentManager.findFragmentByTag("MergedFragment");
            twitterFragment = (TwitterFragment)fragmentManager.findFragmentByTag("TwitterFragment");
            redditFragment = (RedditFragment)fragmentManager.findFragmentByTag("RedditFragment");
            currentFragmentTag = savedInstanceState.getString("currentFragmentTag");
        }
        fragmentManager.beginTransaction().hide(mergedFragment).hide(twitterFragment).hide(redditFragment).commit();
        mergedFragment.setController(this);
        twitterFragment.setController(this);
        redditFragment.setController(this);
        twitterFragment.setOnOptionsItemSelectedListener(new TwitterFragment.OnOptionsItemSelectedListener() {
            @Override
            public boolean onOptionsItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.login :
                        //showDialog(twitterLoginDialog, "TwitterLoginDialog");
                        loginTwitter();
                        return true;
                }
                return false;
            }
        });
        redditFragment.setOnOptionsItemSelectedListener(new RedditFragment.OnOptionsItemSelectedListener() {
            @Override
            public boolean onOptionsItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.login :
                        //showDialog(redditLoginDialog, "RedditLoginDialog");
                        loginReddit();
                        return true;
                }
                return false;
            }
        });
    }

    private void showFragment(String tag) {
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        Fragment currentFragment = fragmentManager.findFragmentByTag(currentFragmentTag);
        if(fragment != null) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            if(currentFragment != null) {
                ft.hide(currentFragment);
            }
            ft.show(fragment);
            ft.commit();
            currentFragmentTag = tag;
        }
    }

    private void refreshFragment(String tag) {
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment != null) {
            fragmentManager.beginTransaction().detach(fragment).commitNowAllowingStateLoss();
            fragmentManager.beginTransaction().attach(fragment).commitAllowingStateLoss();
        }
    }

    private void initializeDialogs() {
        twitterLoginDialog = (TwitterLoginDialog)fragmentManager.findFragmentByTag("TwitterLoginDialog");
        redditLoginDialog = (RedditLoginDialog)fragmentManager.findFragmentByTag("RedditLoginDialog");
        if(twitterLoginDialog == null) { twitterLoginDialog = new TwitterLoginDialog(); }
        if(redditLoginDialog == null) { redditLoginDialog = new RedditLoginDialog(); }
        twitterLoginDialog.setHeader(mainActivity.getString(R.string.twitter_login));
        twitterLoginDialog.setController(this);
        redditLoginDialog.setHeader(mainActivity.getString(R.string.reddit_login));
        redditLoginDialog.setController(this);
    }

    private void showDialog(DialogFragment newDialog, String tag) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        newDialog.show(ft, tag);
    }

    public void twitterLoginDialog(String username, String password) {
        mergedFragment.setContent(new Post("tweet test", username, password));
        //TODO Twitter login
    }

    public void redditLoginDialog(String username, String password) {
        mergedFragment.setContent(new Post("reddit test", username, password));
        //TODO Reddit login
    }

    public void loginReddit(){
        String url = String.format(AUTH_URL, CLIENT_ID, STATE, REDIRECT_URI);
        Log.v("LOL", url);
        mainActivity.setRedditLogin(true);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        mainActivity.startActivity(intent);
    }

    public void loginTwitter(){
        mainActivity.setRedditLogin(false);
        logIn();
    }

    /**
     * Initiats the http request to Reddit form the accestokens
     * @param code
     */
    public void getAccessToken(String code) {
        OkHttpClient client = new OkHttpClient();
        final String authString = CLIENT_ID + ":";
        String encodedAuthString = Base64.encodeToString(authString.getBytes(),
                Base64.NO_WRAP);
        Request request = new Request.Builder()
                .addHeader("User-Agent", "Sample App")
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url(ACCESS_TOKEN_URL)
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                        "grant_type=authorization_code&code=" + code +
                                "&redirect_uri=" + REDIRECT_URI))
                .build();
        Log.v("accestoken",request.toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //mainActivity.setRedditLogin(false);
                Log.e("LOL", "ERROR: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //mainActivity.setRedditLogin(false);
                String json = response.body().string();

                JSONObject data = null;
                try {
                    data = new JSONObject(json);
                    accessToken = data.optString("access_token");
                    //this.accessToken = data.optString("access_token");
                    refreshToken = data.optString("refresh_token");

                    Log.d("LOL", "Access Token = " + accessToken);
                    Log.d("LOL", "Refresh Token = " + refreshToken);
                    test(); //fetch the data RENAME??
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void test(){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                //.url("https://oauth.reddit.com/subreddits/mine/subscriber?count=0")
                .url("https://oauth.reddit.com?count=0")
                //.addHeader("User-Agent", "Sample App")
                .addHeader("Authorization", "bearer "+ accessToken)
                .get().build();
        //.method("GET",null).build();
        //.url("https://oauth.reddit.com/api/v1/me/karma").build();
        Log.v("accestoken",request.toString());
        client.newCall(request).enqueue(new Callback(){

            @Override
            public void onFailure(Call call, IOException e) {
                Log.v("fel", "fel");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                JSONObject data = null;

                Log.d("test1", json);
                try {
                    data = new JSONObject(json);
                    JSONObject test = data.getJSONObject("data");
                    JSONArray test2 = test.getJSONArray("children");
                    test2.length();
                    //String test4 = test2.getJSONObject(0).getJSONObject("display_name").toString();
                    //Log.d("test3", test4);
                    Log.d("test3", String.valueOf(test2.length()));
                    for(int i = 0; i < test2.length(); i ++){
                        //String user = test2.getJSONObject(i).getJSONObject("data").getString("display_name");
                        String title = test2.getJSONObject(i).getJSONObject("data").getString("title");
                        String subreddit = test2.getJSONObject(i).getJSONObject("data").getString("subreddit");
                        String user = test2.getJSONObject(i).getJSONObject("data").getString("author");
                        String url2 = test2.getJSONObject(i).getJSONObject("data").getString("url");
                        String date = test2.getJSONObject(i).getJSONObject("data").getString("created");
                        String platform = "Reddit";
                        Log.d("testingURL", url2);
                        Log.d("date", date);
                        Bitmap image = null;
                        if(url2.contains(".png")|| url2.contains(".jpg")) { //NOT VERY GUD CODE GÖR MED ASYNC TASK
                            URL url = new URL(url2);
                            InputStream stream = url.openConnection().getInputStream();
                            image = BitmapFactory.decodeStream(stream);
                            stream.close();
                        }
                        PostTest post = new PostTest(title, subreddit, user, image, url2, date, platform, "");
                        posts.add(post);
                    }
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("test", String.valueOf(posts.size()));
                            redditFragment.setContent(posts);
                        }
                    });

                    //this.accessToken = data.optString("access_token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //String test = response.toString();
                //String test = response.headers().toString();
                //Log.v("TEST", json);
                //Log.v("TEST", json);
            }
        });
    }

    private void logIn() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        if (!sharedPreferences.getBoolean(ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN,false))
        {
            new TwitterAuthenticateTask().execute();
        }
        else
        {
            /*FragmentManager fragmentManager = getSupportFragmentManager();
            TwitterFragment twitterFragment = (TwitterFragment)fragmentManager.findFragmentById(R.id.fragment);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.commit();
            *//*Intent intent = new Intent(this, TwitterFragment.class);
            startActivity(intent);*/
        }
    }

    class TwitterAuthenticateTask extends AsyncTask<String, String, RequestToken> {

        @Override
        protected void onPostExecute(RequestToken requestToken) {
            if (requestToken!=null)
            {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL()));
                mainActivity.startActivity(intent);
                /*if (!isUseWebViewForAuthentication)
                {

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL()));
                    startActivity(intent);
                }
                else
                {
                    Intent intent = new Intent(getApplicationContext(), OAuthActivity.class);
                    intent.putExtra(ConstantValues.STRING_EXTRA_AUTHENCATION_URL,requestToken.getAuthenticationURL());
                    startActivity(intent);
                }*/
            }
        }

        @Override
        protected RequestToken doInBackground(String... params) {
            return TwitterUtil.getInstance().getRequestToken();
        }
    }
}
