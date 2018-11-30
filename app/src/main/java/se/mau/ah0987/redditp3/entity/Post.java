package se.mau.ah0987.redditp3.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Post implements Parcelable {
    private String headline;
    private String username;
    private String body;

    public Post(String headline, String username, String body){
        this.headline = headline;
        this.username = username;
        this.body = body;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    protected Post(Parcel in) {
        headline = in.readString();
        username = in.readString();
        body = in.readString();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(headline);
        dest.writeString(username);
        dest.writeString(body);
    }
}
