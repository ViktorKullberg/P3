package se.mau.ah0987.redditp3.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.AnimateGifMode;

import java.util.ArrayList;
import java.util.List;

import se.mau.ah0987.redditp3.R;
import se.mau.ah0987.redditp3.entity.Post;
import se.mau.ah0987.redditp3.entity.PostTest;

/**
 * Adapter for all our recyclerviews.
 * Enables the feed to diaplays gifs, videos and show images.
 * Authors:
 */
public class RedditAdapter extends RecyclerView.Adapter<RedditAdapter.Holder> {
    private List<PostTest> content = new ArrayList<PostTest>();
    private LayoutInflater inflater;
    private Context context;

    public RedditAdapter(Context context){
        this(context, new ArrayList<PostTest>());
    }

    public RedditAdapter(Context context, List<PostTest> content){
        this.inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        this.content = content;
        this.context = context;
    }

    public void setContent(List<PostTest> content){
        this.content = content;
        super.notifyDataSetChanged();
    }

    public List<PostTest> getList(){
        return this.content;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_layout, parent, false);
        return new Holder(view);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

    }

    @Override
    public void onViewDetachedFromWindow(@NonNull Holder holder) {
        holder.videoView.stopPlayback(); //stop animation on detach
        holder.videoView.clearAnimation();
        holder.videoView.suspend();
        holder.videoView.setVideoURI(null); //had problems with leaking data between recycleritems
        super.onViewDetachedFromWindow(holder);
        //holder.videoView.suspend();
        //holder.videoView.setVideoURI(null);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull Holder holder) {
        if(holder.url!=null && holder.url.endsWith("mp4")){ //we have a video
            holder.startAnimation();  //start it
        }
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder,int position) {
        holder.tvContent.setText(content.get(position).getContent());
        holder.tvSubreddit.setText(content.get(position).getSubreddit());
        holder.tvUser.setText(content.get(position).getUser());
        holder.tvPlatform.setText(content.get(position).getPlatform());
        holder.tvTitle.setText(content.get(position).getTitle());
        holder.tvDate.setText(String.valueOf(content.get(position).getDateFormatted()));
        holder.url = null;
        if(content.get(position).isImage()){
            holder.imageView.setVisibility(View.VISIBLE);
            holder.imageView.setImageBitmap(content.get(position).getBitmap());
            holder.videoView.setVideoURI(null);
            holder.videoView.suspend();
            holder.videoView.setVisibility(View.GONE);
        }else if(content.get(position).isGif()){
            //content.get(position).getBitmap().recycle();
            holder.imageView.setVisibility(View.VISIBLE);
            Ion.with(holder.imageView)
                    .animateGif(AnimateGifMode.ANIMATE)
                    .load(content.get(position).getUrl());
            holder.videoView.setVideoURI(null);
            holder.videoView.suspend();
            holder.videoView.setVisibility(View.GONE);
        }else if(content.get(position).getUrl().endsWith("gifv")) { //"video" imgur has this ending on gifs
            holder.imageView.setImageBitmap(null);
            holder.imageView.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.VISIBLE);
            String oldUrl = content.get(position).getUrl();
            String newUrl = oldUrl.replace("gifv", "mp4");
            holder.url = newUrl;
        } else if(content.get(position).getUrl().endsWith("mp4")){
            holder.imageView.setImageBitmap(null);
            holder.imageView.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.VISIBLE);
            holder.url = content.get(position).getUrl();
        }
        else{
            holder.imageView.setImageBitmap(null);
            holder.imageView.setVisibility(View.GONE);
            holder.videoView.setVideoURI(null);
            holder.videoView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return content.size();
    }


    public class Holder extends RecyclerView.ViewHolder implements OnClickListener {
        private TextView tvContent;
        private TextView tvSubreddit;
        private TextView tvUser;
        private TextView tvTitle;
        private TextView tvPlatform;
        private TextView tvDate;
        private ImageView imageView;
        private VideoView videoView;
        private MediaController mc;
        private String url= null;

        public Holder(@NonNull View itemView) {
            super(itemView);
            mc = new MediaController(context);
            videoView = itemView.findViewById(R.id.videoInList);
            this.videoView.setMediaController(this.mc);
            mc.setVisibility(View.INVISIBLE);
            videoView.setVideoURI(null);
            videoView.setOnClickListener(new TouchListener()); //play on touch
            tvContent = itemView.findViewById(R.id.tvContent);
            tvSubreddit = itemView.findViewById(R.id.tvSubreddit);
            tvUser = itemView.findViewById(R.id.tvUser);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPlatform = itemView.findViewById(R.id.tvPlatform);
            imageView = itemView.findViewById(R.id.imageTest);
        }

        public void startAnimation(){
            if(url!=null && url.contains("mp4")) {
                Uri video = Uri.parse(url);
                this.videoView.setVideoURI(video);
                this.mc.setMediaPlayer(this.videoView);
                this.mc.setAnchorView(this.videoView);
            }
        }
        @Override
        public void onClick(View v) {
            //videoView.start();
        }

        public class TouchListener implements View.OnClickListener{
            @Override
            public void onClick(View v) {
                if(videoView.isPlaying()){
                    videoView.pause();
                }else{
                    videoView.start();
                }
            }
        }
    }
}
