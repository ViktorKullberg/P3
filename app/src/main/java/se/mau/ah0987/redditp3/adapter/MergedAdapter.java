package se.mau.ah0987.redditp3.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import se.mau.ah0987.redditp3.Controller;
import se.mau.ah0987.redditp3.R;
import se.mau.ah0987.redditp3.entity.Post;


public class MergedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Post> content;
    private Controller controller;

    public MergedAdapter(List<Post> content) {
        this.content = content;
    }

    @Override
    public int getItemCount() {
        return content.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.merged_post_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        initLayout((ViewHolder) holder, position);
    }

    private void initLayout(ViewHolder holder, final int position) {
        holder.tvHeadline.setText(content.get(position).getHeadline());
        holder.tvUsername.setText(content.get(position).getUsername());
        holder.tvBody.setText(content.get(position).getBody());
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvHeadline;
        public TextView tvUsername;
        public TextView tvBody;

        public ViewHolder (View itemView) {
            super(itemView);
            tvHeadline = (TextView)itemView.findViewById(R.id.tvHeadline);
            tvUsername = (TextView)itemView.findViewById(R.id.tvUsername);
            tvBody = (TextView)itemView.findViewById(R.id.tvBody);
        }
    }
}
