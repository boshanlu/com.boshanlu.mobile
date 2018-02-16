package com.boshanlu.mobile.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.boshanlu.mobile.utils.UrlUtils;

/**
 * Created by free2 on 16-3-11.
 * 单篇文章数据包括评论
 */
public class SingleArticleData implements Parcelable {

    public static final Parcelable.Creator<SingleArticleData> CREATOR = new Parcelable.Creator<SingleArticleData>() {
        @Override
        public SingleArticleData createFromParcel(Parcel source) {
            return new SingleArticleData(source);
        }

        @Override
        public SingleArticleData[] newArray(int size) {
            return new SingleArticleData[size];
        }
    };
    //用来标识是楼主还是内容还是loadmore
    public SingleType type;
    public String username;
    public String postTime;
    public String uid;
    public String pid;
    //楼层
    public String index;
    //回复链接
    public String replyUrlTitle;
    public String content;
    public String title;
    //投票
    public VoteData vote;

    public SingleArticleData(SingleType type, String title, String uid, String username, String postTime, String index, String replyUrl, String content, String pid) {
        this.type = type;
        this.username = username;
        this.postTime = postTime;
        this.index = index;
        this.replyUrlTitle = replyUrl;
        this.content = content;
        this.title = title;
        this.pid = pid;
        this.uid = uid;
    }

    protected SingleArticleData(Parcel in) {
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : SingleType.values()[tmpType];
        this.username = in.readString();
        this.postTime = in.readString();
        this.uid = in.readString();
        this.pid = in.readString();
        this.index = in.readString();
        this.replyUrlTitle = in.readString();
        this.content = in.readString();
        this.title = in.readString();
    }

    public String getImg() {
        return UrlUtils.getAvatarUrl(uid, "m");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeString(this.username);
        dest.writeString(this.postTime);
        dest.writeString(this.uid);
        dest.writeString(this.pid);
        dest.writeString(this.index);
        dest.writeString(this.replyUrlTitle);
        dest.writeString(this.content);
        dest.writeString(this.title);
    }

}
