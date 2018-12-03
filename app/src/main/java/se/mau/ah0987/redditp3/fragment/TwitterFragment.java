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
        controller.twitterHomeTimeline();
    }

    private void initControl(View view) {
        Uri uri = getActivity().getIntent().getData();
        if (uri != null && uri.toString().startsWith(ConstantValues.TWITTER_CALLBACK_URL)) {
            String verifier = uri.getQueryParameter(ConstantValues.URL_PARAMETER_TWITTER_OAUTH_VERIFIER);
            controller.twitterAccessToken(verifier);
        } else
            controller.twitterAccessToken("");
    }

    public void setTextViewUserName(String textViewUserName) {
        this.textViewUserName.setText(Html.fromHtml("<b> Welcome " + textViewUserName + "</b>"));
    }

    public void setSwipeContainer(Boolean bool) {
        swipeContainer.setRefreshing(bool);
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

    public void setContent(List<PostTest> content){
        this.list = content;
        redditAdapter.setContent(content);
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
                controller.twitterUpdateStatus(status);
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Please enter a status", Toast.LENGTH_SHORT).show();
            }

        }
    };



    public List<PostTest> getTwitterList(){
        return this.list;
    }

    public void clearList() {
        list.clear();
    }

    public void addList(PostTest postTest) {
        list.add(postTest);
    }

    public void setController(Controller controller) {
        this.controller = controller;
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
