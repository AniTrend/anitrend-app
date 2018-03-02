package com.mxt.anitrend.model.entity.anilist;

import android.os.Parcel;
import android.text.TextUtils;

import com.mxt.anitrend.model.entity.base.SeriesBase;
import com.mxt.anitrend.model.entity.base.StaffBase;
import com.mxt.anitrend.model.entity.base.StudioBase;
import com.mxt.anitrend.model.entity.general.ExternalLink;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Maxwell on 10/2/2016.
 * This include Anime or Manga
 */
public class Series extends SeriesBase {

    private String description;
    private boolean favourite;
    private Map<String, Float> list_stats;
    private Map<String, Map<String, Float>> airing_stats;
    /*Relation Model*/
    private String relation_type;
    private int link_id;
    private List<StaffBase> staff;
    private List<Rank> rankings;
    private List<StudioBase> studio;
    private List<ExternalLink> external_links;
    private List<Series> relations;
    private List<Series> relations_manga;
    private List<Series> relations_anime;
    private List<Tag> tags;
    private List<Review> reviews;

    private HashMap<Integer, Float> score_distribution;

    protected Series(Parcel in) {
        super(in);
        description = in.readString();
        favourite = in.readByte() != 0;
        staff = in.createTypedArrayList(StaffBase.CREATOR);
        rankings = in.createTypedArrayList(Rank.CREATOR);
        studio = in.createTypedArrayList(StudioBase.CREATOR);
        external_links = in.createTypedArrayList(ExternalLink.CREATOR);
        relations = in.createTypedArrayList(Series.CREATOR);
        relations_manga = in.createTypedArrayList(Series.CREATOR);
        relations_anime = in.createTypedArrayList(Series.CREATOR);
        tags = in.createTypedArrayList(Tag.CREATOR);
        reviews = in.createTypedArrayList(Review.CREATOR);
        list_stats = in.readHashMap(HashMap.class.getClassLoader());
        airing_stats = in.readHashMap(HashMap.class.getClassLoader());
        score_distribution = in.readHashMap(HashMap.class.getClassLoader());
        relation_type = in.readString();
        link_id = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(description);
        dest.writeByte((byte) (favourite ? 1 : 0));
        dest.writeTypedList(staff);
        dest.writeTypedList(rankings);
        dest.writeTypedList(studio);
        dest.writeTypedList(external_links);
        dest.writeTypedList(relations);
        dest.writeTypedList(relations_manga);
        dest.writeTypedList(relations_anime);
        dest.writeTypedList(tags);
        dest.writeTypedList(reviews);
        dest.writeMap(list_stats);
        dest.writeMap(airing_stats);
        dest.writeMap(score_distribution);
        dest.writeString(relation_type);
        dest.writeInt(link_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Series> CREATOR = new Creator<Series>() {
        @Override
        public Series createFromParcel(Parcel in) {
            return new Series(in);
        }

        @Override
        public Series[] newArray(int size) {
            return new Series[size];
        }
    };

    public List<Series> getRelations_anime() {
        return relations_anime;
    }

    /**
     * Description of series.
     */
    public String getDescription() {
        return description;
    }

    /**ration between 10 - 100 and it's rating values*/
    public Map<Integer, Float> getScore_distribution() {
        return score_distribution;
    }

    /** airing, score and watching correlation 74 : {score: 68.6, watching: 1564} */
    public Map<String, Map<String, Float>> getAiring_stats() {
        return airing_stats;
    }

    /** @see com.mxt.anitrend.util.KeyUtils.UserListType */
    public Map<String, Float> getList_stats() {
        return list_stats;
    }

    public List<StaffBase> getStaff() {
        return staff;
    }

    public List<Rank> getRankings() {
        return rankings;
    }

    public List<StudioBase> getStudio() {
        return studio;
    }

    public List<ExternalLink> getExternal_links() {
        return external_links;
    }

    public List<Series> getRelations() {
        return relations;
    }

    public List<Series> getRelations_manga() {
        return relations_manga;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    /**
     * Only available in relations
     */
    public String getRelation_type() {
        if(!TextUtils.isEmpty(relation_type))
            return relation_type.substring(0, 1).toUpperCase() + relation_type.substring(1);
        return relation_type;
    }

    public void setRelation_type(String relation_type) {
        this.relation_type = relation_type;
    }

    /**
     * Only available in relations
     */
    public int getLink_id() {
        return link_id;
    }

    public void setLink_id(int link_id) {
        this.link_id = link_id;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}

