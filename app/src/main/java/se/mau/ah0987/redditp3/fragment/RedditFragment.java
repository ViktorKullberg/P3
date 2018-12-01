package se.mau.ah0987.redditp3.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import se.mau.ah0987.redditp3.Controller;
import se.mau.ah0987.redditp3.R;
import se.mau.ah0987.redditp3.adapter.RedditAdapter;
import se.mau.ah0987.redditp3.entity.Post;
import se.mau.ah0987.redditp3.entity.PostTest;


public class RedditFragment extends Fragment {
    private OnOptionsItemSelectedListener listener;
    private Controller controller;
    private Button logInReddit;
    private RedditAdapter redditAdapter;
    private RecyclerView recyclerView;
    private List<PostTest> posts = new ArrayList<PostTest>();

    public RedditFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("ONCREATE REDDIT", "CREATED");
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_reddit, container, false);
        logInReddit = view.findViewById(R.id.btnLoginReddit);
        recyclerView = view.findViewById(R.id.rlistReddit);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        redditAdapter = new RedditAdapter(getActivity(), posts);
        recyclerView.setAdapter(redditAdapter);
        logInReddit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.loginReddit();
            }
        });
        return view;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void setContent(List<PostTest> content){
        this.posts = content;
        redditAdapter.setContent(content);
    }

    public List<PostTest> getRedditList(){
        return this.redditAdapter.getList();
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
