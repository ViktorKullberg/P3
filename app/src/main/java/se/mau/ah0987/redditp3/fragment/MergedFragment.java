package se.mau.ah0987.redditp3.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import se.mau.ah0987.redditp3.Controller;
import se.mau.ah0987.redditp3.R;
import se.mau.ah0987.redditp3.adapter.MergedAdapter;
import se.mau.ah0987.redditp3.entity.Post;

public class MergedFragment extends Fragment {
    private RecyclerView rvMerged;
    private MergedAdapter mergedAdapter;
    private List<Post> content = new ArrayList<>();
    private Controller controller;

    public MergedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_merged, container, false);
        Log.d("ONCREATE TWITTER", "MERGED");
        rvMerged = view.findViewById(R.id.rvMerged);
        rvMerged.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (savedInstanceState != null) {
            content = savedInstanceState.getParcelableArrayList("postList");
        }
        mergedAdapter = new MergedAdapter(content);
        mergedAdapter.setController(controller);
        rvMerged.setAdapter(mergedAdapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mergedAdapter != null)
            mergedAdapter.setController(controller);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("postList", (ArrayList<Post>) content);
    }

    public void setContent(List<Post> content) {
        this.content = content;
    }

    public void setContent(Post post) {
        this.content.add(0, post);
        mergedAdapter.notifyDataSetChanged();
    }

    public void setController(Controller controller) {
        this.controller = controller;
        if(mergedAdapter !=null)
            mergedAdapter.setController(controller);
    }
}
