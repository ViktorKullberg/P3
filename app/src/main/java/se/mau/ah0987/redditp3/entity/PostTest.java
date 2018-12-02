package se.mau.ah0987.redditp3.entity;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.Calendar;

import se.mau.ah0987.redditp3.DateConverter;

/**
 * Post Item used to display a tweet or reddit post
 * Contains information about content, user, date, title etc..
 * Authors:
 */
public class PostTest {
    private String content;
    private String subreddit;
    private String user;
    private Bitmap bitmap;
    private String url;
    private String platform;
    private String date;
    private String title;

    public PostTest(String title, String subreddit, String user,
                    Bitmap bitmap,String url, String date, String platform,String content){
        this.content = content;
        this.title = title;
        this.subreddit = subreddit;
        this.user = user;
        this.bitmap = bitmap;
        this.url = url;
        this.date = date;
        this.platform = platform;
        Log.d("CreatedPost", content+subreddit+user);
    }

    public String getContent() {
        return content;
    }

    public String getTitle(){
        return title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public String getPlatform(){
        return platform;
    }

    public boolean isImage() {
        if (bitmap == null) {
            return false;
        }
        return true;
    }
    public String getRawDate(){
        return date;
    }

    /**
     * Returns the date as a long.
     * Since reddit and twitter uses differens time formats
     * for posts we have to parse and convert
     */
    public long getDate(){
        if(platform.equals("Twitter")){
            return (new Long(date).longValue()/1000)+3600*8;
        }
        return DateConverter.convertDate(date);
    }

    /**
     * Format the time to displat how long ago it was. Due to time difference
     * we have to append 3600*8 for posts.
     */
    public String getDateFormatted(){
        Calendar rightNow = Calendar.getInstance();
        long currentTime = rightNow.getTimeInMillis();
        long currentTimeSeconds = currentTime/1000;
        Log.d("currentMillis", String.valueOf(currentTimeSeconds));
        Log.d("postTime", String.valueOf(getDate()));
        long timeSincePosted = currentTimeSeconds - getDate();
        timeSincePosted+=3600*8;
        String returnTime = "";
        if(timeSincePosted<3600){
            returnTime = String.valueOf(timeSincePosted/60)+ " minutes ago";
        }else{
            returnTime = String.valueOf(timeSincePosted/3600) + " hours ago";
        }
        return returnTime;
    }

    public boolean isGif(){
        if(url.endsWith("gif")){
            return true;
        }
        return false;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public String getUrl(){
        return url;
    }
}
