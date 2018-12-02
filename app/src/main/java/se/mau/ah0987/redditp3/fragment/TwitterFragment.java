package se.mau.ah0987.redditp3.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hintdesk.core.util.StringUtil;

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

import se.mau.ah0987.redditp3.ConstantValues;
import se.mau.ah0987.redditp3.Controller;
import se.mau.ah0987.redditp3.MainActivity;
import se.mau.ah0987.redditp3.MyItemDecoration;
import se.mau.ah0987.redditp3.R;
import se.mau.ah0987.redditp3.TwitterUtil;
import se.mau.ah0987.redditp3.adapter.RedditAdapter;
import se.mau.ah0987.redditp3.entity.PostTest;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Fragment to display the twitter feed
 */
public class TwitterFragment extends Fragment {
    private OnOptionsItemSelectedListener listener;
    private Controller controller;
    private Button buttonUpdateStatus, buttonLogout;
    private EditText editTextStatus;
    private TextView textViewStatus, textViewUserName;
    private RecyclerView recyclerView;
    private RedditAdapter redditAdapter;
    private List<PostTest> list = new ArrayList<>();
    private SwipeRefreshLayout swipeContainer;


    public TwitterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_twitter, container, false);
        initializeComponent(view);
        initControl(view);

        return view;

    }


    public void getTweets(){
        new TwitterHomeTimelineTask().execute();
    }

    private void initControl(View view) {
        Uri uri = getActivity().getIntent().getData();
        if (uri != null && uri.toString().startsWith(ConstantValues.TWITTER_CALLBACK_URL)) {
            String verifier = uri.getQueryParameter(ConstantValues.URL_PARAMETER_TWITTER_OAUTH_VERIFIER);
            new TwitterGetAccessTokenTask().execute(verifier);
        } else
            new TwitterGetAccessTokenTask().execute("");
    }

    private void initializeComponent(View view) {
        buttonUpdateStatus = view.findViewById(R.id.buttonUpdateStatus);
        buttonLogout = view.findViewById(R.id.buttonLogout);
        editTextStatus =  view.findViewById(R.id.editTextStatus);
        textViewStatus = view.findViewById(R.id.textViewStatus);
        textViewUserName = view.findViewById(R.id.textViewUserName);
        recyclerView = view.findViewById(R.id.rvTwitter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new MyItemDecoration(getContext()));
        redditAdapter = new RedditAdapter(getActivity(), list);
        recyclerView.setAdapter(redditAdapter);
        buttonUpdateStatus.setOnClickListener(buttonUpdateStatusOnClickListener);
        buttonLogout.setOnClickListener(buttonLogoutOnClickListener);

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                getTweets();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }

    public void setRvContent(List<PostTest> list) {
        redditAdapter.setContent(list);
    }

    private View.OnClickListener buttonLogoutOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN, "");
            editor.putString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, "");
            editor.putBoolean(ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN, false);
            editor.commit();
            TwitterUtil.getInstance().reset();
            Intent intent = new Intent(getActivity().getApplication(), MainActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener buttonUpdateStatusOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            String status = editTextStatus.getText().toString();
            if (!StringUtil.isNullOrWhitespace(status)) {
                new TwitterUpdateStatusTask().execute(status);
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Please enter a status", Toast.LENGTH_SHORT).show();
            }

        }
    };

    class TwitterGetAccessTokenTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String userName) {
            textViewUserName.setText(Html.fromHtml("<b> Welcome " + userName + "</b>"));
        }

        @Override
        protected String doInBackground(String... params) {

            Twitter twitter = TwitterUtil.getInstance().getTwitter();
            RequestToken requestToken = TwitterUtil.getInstance().getRequestToken();
            if (!StringUtil.isNullOrWhitespace(params[0])) {
                try {
                    AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, params[0]);
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
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
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
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
                Toast.makeText(getActivity().getApplicationContext(), "Tweet successfully", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getActivity().getApplicationContext(), "Tweet failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
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
            swipeContainer.setRefreshing(false);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            try {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                String accessTokenString = sharedPreferences.getString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN, "");
                String accessTokenSecret = sharedPreferences.getString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, "");
                // gets Twitter instance with default credentials
                list.clear();
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
                        list.add(postTest);
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setRvContent(list);
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

    public List<PostTest> getTwitterList(){
        return this.list;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public static Date getTwitterDate(String date) throws ParseException {

        final String TWITTER="EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(TWITTER, Locale.ENGLISH);
        sf.setLenient(true);
        return sf.parse(date);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.actionbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return listener.onOptionsItemSelected(item);
    }

    public void setOnOptionsItemSelectedListener(OnOptionsItemSelectedListener listener) {
        this.listener = listener;
    }

    public interface OnOptionsItemSelectedListener {
        public boolean onOptionsItemSelected(MenuItem item);
    }
}
