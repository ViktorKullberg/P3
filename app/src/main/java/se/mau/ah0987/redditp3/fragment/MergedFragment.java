package se.mau.ah0987.redditp3.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import se.mau.ah0987.redditp3.Controller;
import se.mau.ah0987.redditp3.MyItemDecoration;
import se.mau.ah0987.redditp3.R;
import se.mau.ah0987.redditp3.adapter.RedditAdapter;
import se.mau.ah0987.redditp3.entity.PostTest;


/**
 * Fragment to display the merged feed
 */
public class MergedFragment extends Fragment {
    private RecyclerView recyclerView;
    private RedditAdapter redditAdapter;
    private List<PostTest> content = new ArrayList<>();
    private Controller controller;
    private TextView tvReddit;
    private TextView tvTwitter;

    public MergedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_merged, container, false);
        tvTwitter = view.findViewById(R.id.tvTwitterInfo);
        tvReddit = view.findViewById(R.id.tvRedditInfo);
        tvReddit.setText(controller.checkRedditLogin());
        recyclerView = view.findViewById(R.id.rvMerged);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new MyItemDecoration(getContext()));
        redditAdapter = new RedditAdapter(getActivity(), content);
        recyclerView.setAdapter(redditAdapter);
        return view;
    }

    public void setContent(List<PostTest> content){
        this.content = content;
        redditAdapter.setContent(content);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    public void setController(Controller controller) {
        this.controller = controller;
    }
}
