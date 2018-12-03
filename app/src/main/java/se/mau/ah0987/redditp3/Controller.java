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
import android.widget.Toast;

import com.hintdesk.core.util.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import se.mau.ah0987.redditp3.dialog.RedditLoginDialog;
import se.mau.ah0987.redditp3.dialog.TwitterLoginDialog;
import se.mau.ah0987.redditp3.entity.PostTest;
import se.mau.ah0987.redditp3.fragment.MergedFragment;
import se.mau.ah0987.redditp3.fragment.RedditFragment;
import se.mau.ah0987.redditp3.fragment.TwitterFragment;
import twitter4j.MediaEntity;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Controller class which controlls the flow of the app.
 * Authors:
 */
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

    private static final String REDIRECT_URI = "myapp://auth";

    private static final String STATE = "MY_RANDOM_STRING_123456";

    private static final String ACCESS_TOKEN_URL =
            "https://www.reddit.com/api/v1/access_token";

    private String accessToken = null;
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
                        mergedFragment.setContent(createMergedFeed());
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
                        loginReddit();
                        return true;
                }
                return false;
            }
        });
    }

    public void showFragment(String tag) {
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

    /**
     * Start login to reddit using OAUTH 2.0
     */
    public void loginReddit(){
        String url = String.format(AUTH_URL, CLIENT_ID, STATE, REDIRECT_URI);
        mainActivity.setRedditLogin(true);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        mainActivity.startActivity(intent);
    }

    /**
     * Start login twitter using OAUTH 2.0
     */
    public void loginTwitter(){
        mainActivity.setRedditLogin(false);
        logIn();
    }

    public List<PostTest> createMergedFeed(){
        ArrayList<PostTest> reddit = (ArrayList<PostTest>)redditFragment.getRedditList();
        ArrayList<PostTest> twitter = (ArrayList<PostTest>)twitterFragment.getTwitterList();
        ArrayList<PostTest> mergedList = new ArrayList<>();
        Log.d("CREATEMERGED", "redditsize: " + reddit.size());

        for(int i = 0; i < reddit.size(); i++){
            mergedList.add(i, reddit.get(i));
        }

        for(int i = 0; i < twitter.size(); i ++) {
            int j = 0;
            while (j < mergedList.size() && twitter.get(i).getDate() < mergedList.get(j).getDate()) {
                j++;
            }
            mergedList.add(j, twitter.get(i));
        }

        return mergedList;
    }


    public String checkRedditLogin(){
        if(accessToken==null){
            return "Log into Reddit to create the merged feed";
        }else{
            return "Reddit feed";
        }
    }

    /**
     * Initiats the http request to Reddit form the accestokens
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
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Failed HTTPrequest", "ERROR: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                JSONObject data = null;
                try {
                    data = new JSONObject(json);
                    accessToken = data.optString("access_token");
                    refreshToken = data.optString("refresh_token");
                    getLastestPosts(); //fetch the data
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getLastestPosts(){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder() //builds the request with acceesstoken
                .url("https://oauth.reddit.com/new?count=0")
                .addHeader("Authorization", "bearer "+ accessToken)
                .get().build();
        Log.v("accestoken",request.toString());
        client.newCall(request).enqueue(new Callback(){
            @Override
            public void onFailure(Call call, IOException e) {
                Log.v("Failure", "Error" + e.toString());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                JSONObject data = null;
                try {
                    posts.clear();
                    data = new JSONObject(json);
                    JSONObject dataObject = data.getJSONObject("data");
                    JSONArray dataChildrenArray = dataObject.getJSONArray("children");
                    dataChildrenArray.length();
                    for(int i = 0; i < dataChildrenArray.length(); i ++){
                        JSONObject childrenData = dataChildrenArray.getJSONObject(i).getJSONObject("data");
                        String title = childrenData.getString("title");
                        String subreddit = childrenData.getString("subreddit");
                        String user = childrenData.getString("author");
                        String url2 = childrenData.getString("url");
                        String date = childrenData.getString("created");
                        String platform = "Reddit";
                        Bitmap image = null;
                        if(url2.contains(".png")|| url2.contains(".jpg")) {
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
                            redditFragment.setContent(posts);
                            redditFragment.setSwipeContainer(false);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * Twitter login, if we have a accestoken we will use that
     */
    private void logIn() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        if (!sharedPreferences.getBoolean(ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN,false))
        {
            new TwitterAuthenticateTask().execute();
        }
        else
        {
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

    private static Date getTwitterDate(String date) throws ParseException {

        final String TWITTER="EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(TWITTER, Locale.ENGLISH);
        sf.setLenient(true);
        return sf.parse(date);
    }

    private void setRvContent(List<PostTest> list) {
        twitterFragment.setContent(list);
    }

    public void twitterAccessToken(String string) {
        new TwitterGetAccessTokenTask().execute(string);
    }

    public void twitterUpdateStatus(String status) {
        new TwitterUpdateStatusTask().execute(status);
    }

    public void twitterHomeTimeline() {
        new TwitterHomeTimelineTask().execute();
    }

    class TwitterGetAccessTokenTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String userName) {
            twitterFragment.setTextViewUserName(userName);
        }

        @Override
        protected String doInBackground(String... params) {

            Twitter twitter = TwitterUtil.getInstance().getTwitter();
            RequestToken requestToken = TwitterUtil.getInstance().getRequestToken();
            if (!StringUtil.isNullOrWhitespace(params[0])) {
                try {
                    AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, params[0]);
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity.getApplicationContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN, accessToken.getToken());
                    editor.putString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, accessToken.getTokenSecret());
                    editor.putBoolean(ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN, true);
                    editor.commit();
                    return twitter.showUser(accessToken.getUserId()).getName();
                } catch (TwitterException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            } else {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity.getApplicationContext());
                String accessTokenString = sharedPreferences.getString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN, "");
                String accessTokenSecret = sharedPreferences.getString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, "");
                AccessToken accessToken = new AccessToken(accessTokenString, accessTokenSecret);
                try {
                    TwitterUtil.getInstance().setTwitterFactory(accessToken);
                    return TwitterUtil.getInstance().getTwitter().showUser(accessToken.getUserId()).getName();
                } catch (TwitterException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    class TwitterUpdateStatusTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPostExecute(Boolean result) {
            if (result)
                Toast.makeText(mainActivity.getApplicationContext(), "Tweet successfully", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(mainActivity.getApplicationContext(), "Tweet failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity.getApplicationContext());
                String accessTokenString = sharedPreferences.getString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN, "");
                String accessTokenSecret = sharedPreferences.getString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, "");

                if (!StringUtil.isNullOrWhitespace(accessTokenString) && !StringUtil.isNullOrWhitespace(accessTokenSecret)) {
                    AccessToken accessToken = new AccessToken(accessTokenString, accessTokenSecret);
                    twitter4j.Status status = TwitterUtil.getInstance().getTwitterFactory().getInstance(accessToken).updateStatus(params[0]);
                    return true;
                }

            } catch (TwitterException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return false;  //To change body of implemented methods use File | Settings | File Templates.

        }
    }

    class TwitterHomeTimelineTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPostExecute(Boolean result) {
            twitterFragment.setSwipeContainer(false);
            mergedFragment.setSwipeContainer(false);
            mergedFragment.setContent(createMergedFeed());
            //createMergedFeed();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            try {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity.getApplicationContext());
                String accessTokenString = sharedPreferences.getString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN, "");
                String accessTokenSecret = sharedPreferences.getString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, "");
                // gets Twitter instance with default credentials
                twitterFragment.clearList();
                if (!StringUtil.isNullOrWhitespace(accessTokenString) && !StringUtil.isNullOrWhitespace(accessTokenSecret)) {
                    AccessToken accessToken = new AccessToken(accessTokenString, accessTokenSecret);
                    List<twitter4j.Status> statuses = TwitterUtil.getInstance().getTwitterFactory().getInstance(accessToken).getHomeTimeline();
                    //twitter4j.Status status = TwitterUtil.getInstance().getTwitterFactory().getInstance(accessToken).updateStatus(params[0]);
                    User user = TwitterUtil.getInstance().getTwitterFactory().getInstance(accessToken).verifyCredentials();
                    Log.d("user", "Showing @" + user.getScreenName() + "'s home timeline.");
                    for(twitter4j.Status status : statuses) {
                        Date date = getTwitterDate(status.getCreatedAt().toString());
                        date.getTime();
                        Log.d("TEST", String.valueOf(date.getTime()));
                        //Date date2 = status.getCreatedAt().getTime();
                        //status.getCreatedAt().getTime();
                        Log.d("statuses", "@" + status.getUser().getScreenName() + " - " + status.getText() + String.valueOf(date.getTime()));
                        MediaEntity[] media = status.getMediaEntities(); //get the media entities from the status
                        String url = "";
                        Bitmap bitmap = null;
                        for(MediaEntity m : media){ //search trough your entities
                            //Log.d("links", Arrays.toString(m.getVideoVariants())); //get your url!

                            MediaEntity.Variant[] videos = m.getVideoVariants();
                            if(videos.length!=0) {
                                for (MediaEntity.Variant video : videos) {
                                    if (video.getContentType().equals("video/mp4")) {
                                        Log.d("video", video.getUrl());
                                        url = video.getUrl();
                                        url = url.substring(0, url.length() - 6); //removes tag at end
                                        break; //only want first
                                    }
                                }
                            }else{
                                url = m.getMediaURL();
                                if(url.contains(".png")|| url.contains(".jpg")) { //NOT VERY GUD CODE GÖR MED ASYNC TASK
                                    URL urlURL = new URL(url);
                                    InputStream stream = urlURL.openConnection().getInputStream();
                                    bitmap = BitmapFactory.decodeStream(stream);
                                    stream.close();
                                }
                            }
                        }
                        PostTest postTest = new PostTest(status.getText(), "", status.getUser().getScreenName(), bitmap, url, String.valueOf(date.getTime()), "Twitter", "");
                        twitterFragment.addList(postTest);
                    }
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setRvContent(twitterFragment.getTwitterList());
                        }
                    });
                    return true;
                }
            } catch (TwitterException te) {
                te.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;  //To change body of implemented methods use File | Settings | File Templates.

        }
    }
}
