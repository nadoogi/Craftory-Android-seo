package cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.RequestManager;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ui.widget.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;
import java.util.List;

import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.LoginActivity;
import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.R;
import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.functions.FunctionBase;
import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.functions.FunctionPost;
import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.viewHolders.ArtistIllustHeaderViewHolder;
import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.viewHolders.TimelineItemViewHolder;
import me.gujun.android.taggroup.TagGroup;


/**
 * Created by ssamkyu on 2017. 5. 8..
 */

public class ArtistIllustAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public  interface OnQueryLoadListener<ParseObject> {
        public void onLoading();
        public void onLoaded(List<ParseObject> objects, Exception e);
    }

    private List<OnQueryLoadListener<ParseObject>> onQueryLoadListeners;
    private ArrayList<ParseObject> items;
    private ParseQueryAdapter.QueryFactory<ParseObject> queryFactory;
    private List<List<ParseObject>> objectPages;
    private int objectsPerPage = 30;
    private boolean paginationEnabled;
    private boolean hasNextPage;
    private int currentPage;
    private RequestManager requestManager;
    protected Context context;
    protected ParseUser currentUser;

    private final int patronType = 0;
    private final int postType = 1;
    private Boolean recommendFlag;
    private FunctionBase functionBase;
    private FunctionPost functionPost;


    public ArtistIllustAdapter(Context context, RequestManager requestManager, Boolean recommend, FunctionBase functionBase, FunctionPost functionPost) {

        String TAG = "ArtistIllustAdatper";
        Log.d(TAG, "Welcome");

        this.requestManager = requestManager;
        this.context = context;
        this.currentUser = ParseUser.getCurrentUser();
        this.items = new ArrayList<>();
        this.objectPages = new ArrayList<>();
        this.currentPage = 0;
        this.paginationEnabled = true;
        this.hasNextPage = true;
        this.onQueryLoadListeners = new ArrayList<>();
        this.recommendFlag = recommend;
        this.functionBase = functionBase;
        this.functionPost = functionPost;

        final ArrayList<String> queryList = new ArrayList<>();
        queryList.add("post");
        queryList.add("album");

        if(currentUser != null){

            if(recommend){

                final ArrayList<String> myTagArray = functionBase.jsonArrayToArrayList(currentUser.getJSONArray("tags"));

                this.queryFactory = new ParseQueryAdapter.QueryFactory<ParseObject>() {
                    @Override
                    public ParseQuery<ParseObject> create() {

                        ParseQuery<ParseObject> query = ParseQuery.getQuery("ArtistPost");
                        query.whereEqualTo("status", true);
                        query.whereEqualTo("open_flag", true);
                        query.whereContainedIn("post_type", queryList);
                        query.whereContainedIn("tag_array", myTagArray);
                        query.whereNotEqualTo("objectId", currentUser.getObjectId());
                        query.include("user");
                        query.orderByDescending("like_count");


                        return query;
                    }
                };

                loadObjects(currentPage);

            } else {

                this.queryFactory = new ParseQueryAdapter.QueryFactory<ParseObject>() {
                    @Override
                    public ParseQuery<ParseObject> create() {

                        ParseQuery<ParseObject> query = ParseQuery.getQuery("ArtistPost");
                        query.whereEqualTo("status", true);
                        query.whereContainedIn("post_type", queryList);
                        query.include("user");

                        query.orderByDescending("createdAt");

                        return query;
                    }
                };

                loadObjects(currentPage);

            }

        } else {

            this.queryFactory = new ParseQueryAdapter.QueryFactory<ParseObject>() {
                @Override
                public ParseQuery<ParseObject> create() {

                    ParseQuery<ParseObject> query = ParseQuery.getQuery("ArtistPost");
                    query.whereEqualTo("status", true);
                    query.whereEqualTo("post_type", "post");
                    query.include("user");

                    if(recommendFlag){

                        query.orderByDescending("like_count");

                    } else {

                        query.orderByDescending("createdAt");

                    }

                    return query;
                }
            };

            loadObjects(currentPage);

        }



    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == 0){

            View headerView;

            headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_artistillust_header, parent, false);

            return new ArtistIllustHeaderViewHolder(headerView);

        } else {

            View timelineView;

            timelineView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_timeline_item2, parent, false);

            return new TimelineItemViewHolder(timelineView);

        }

    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if(position == 0){

            final TagGroup tag_group = ((ArtistIllustHeaderViewHolder)holder).getTagGroup();
            RecyclerView best_illust_list = ((ArtistIllustHeaderViewHolder)holder).getBestIllustList();
            RecyclerView weekly_best_list = ((ArtistIllustHeaderViewHolder)holder).getWeeklyBestList();
            LinearLayout recommend_button = ((ArtistIllustHeaderViewHolder)holder).getRecommendButton();
            LinearLayout recent_button = ((ArtistIllustHeaderViewHolder)holder).getRecentButton();
            LinearLayout weekly_best_layout =((ArtistIllustHeaderViewHolder)holder).getWeeklyBestLayout();

            weekly_best_layout.setVisibility(View.GONE);

            final LinearLayoutManager illustRankingLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false);
            best_illust_list.setLayoutManager(illustRankingLayoutManager);
            best_illust_list.setHasFixedSize(true);
            best_illust_list.setNestedScrollingEnabled(false);
            final ArtworkIllustBestAdapter artworkIllustBestAdapter = new ArtworkIllustBestAdapter(context, requestManager);
            best_illust_list.setAdapter(artworkIllustBestAdapter);


            ParseQuery query = ParseQuery.getQuery("TagLog");
            query.whereEqualTo("status", true);
            query.whereEqualTo("add", true);
            query.setLimit(100);
            query.orderByDescending("createdAt");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> tagObs, ParseException e) {

                    if(e==null){

                        String[] tagArray = functionBase.tagMaker(tagObs);
                        tag_group.setTags( tagArray );

                        if(currentUser != null){

                            tag_group.setOnTagClickListener(new TagGroup.OnTagClickListener() {
                                @Override
                                public void onTagClick(final String tag) {

                                    ParseObject tagLog = new ParseObject("TagLog");
                                    tagLog.put("tag", tag);
                                    tagLog.put("user", currentUser);
                                    tagLog.put("type", "tagAdd");
                                    tagLog.put("place", "ArtistIllustFragment");
                                    tagLog.put("status", true);
                                    tagLog.put("add", true);
                                    tagLog.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {

                                            if(e==null){

                                                currentUser.addUnique("tags", tag);
                                                currentUser.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {

                                                        if(e==null){

                                                            TastyToast.makeText(context, "태그가 추가 되었습니다.", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                                                            loadObjects(0);

                                                        } else {



                                                        }

                                                    }
                                                });

                                            } else {

                                                e.printStackTrace();
                                            }

                                        }
                                    });

                                }
                            });

                        }



                    } else {

                        e.printStackTrace();
                    }

                }

            });

        } else {

            final ParseObject timelineOb = getItem(position);

            functionPost.TimelineArtistPostAdapterBuilder( timelineOb, holder, requestManager, "small");

        }

    }


    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);

        if(holder instanceof ArtistIllustHeaderViewHolder){


        } else {

            ImageView post_image = ((TimelineItemViewHolder)holder).getPostImage();
            ImageView writter_image = ((TimelineItemViewHolder)holder).getWritterPhoto();
            requestManager.clear(post_image);
            requestManager.clear(writter_image);

        }



    }


    @Override
    public int getItemViewType(int position) {

        return position;

    }




    @Override
    public int getItemCount() {

        return items.size()+1;
    }


    public ParseObject getItem(int position) {
        return items.get( position -1 );
    }



    public void loadObjects(final int page) {

        final ParseQuery<ParseObject> query = this.queryFactory.create();

        if (this.objectsPerPage > 0 && this.paginationEnabled) {
            this.setPageOnQuery(page, query);
        }

        this.notifyOnLoadingListeners();

        if (page >= objectPages.size()) {
            objectPages.add(page, new ArrayList<ParseObject>());
        }


        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> foundObjects, ParseException e) {

                if ((e != null) && ((e.getCode() == ParseException.CONNECTION_FAILED) || (e.getCode() != ParseException.CACHE_MISS))) {

                    hasNextPage = true;

                } else if (foundObjects != null) {

                    // Only advance the page, this prevents second call back from CACHE_THEN_NETWORK to
                    // reset the page.
                    if (page >= currentPage) {
                        currentPage = page;

                        // since we set limit == objectsPerPage + 1
                        hasNextPage = (foundObjects.size() > objectsPerPage);
                    }

                    if (paginationEnabled && foundObjects.size() > objectsPerPage) {
                        // Remove the last object, fetched in order to tell us whether there was a "next page"
                        foundObjects.remove(objectsPerPage);
                    }

                    List<ParseObject> currentPage = objectPages.get(page);
                    currentPage.clear();
                    currentPage.addAll(foundObjects);

                    syncObjectsWithPages();

                    // executes on the UI thread
                    notifyDataSetChanged();
                }

                notifyOnLoadedListeners(foundObjects, e);

            }
        });
    }


    public void loadNextPage() {


        if (items.size() == 0) {

            loadObjects(0);

        } else {

            loadObjects(currentPage + 1);

        }
    }


    public void setObjectsPerPage(int objectsPerPage) {
        this.objectsPerPage = objectsPerPage;
    }

    private void syncObjectsWithPages() {
        items.clear();
        for (List<ParseObject> pageOfObjects : objectPages) {
            items.addAll(pageOfObjects);
        }
    }



    protected void setPageOnQuery(int page, ParseQuery<ParseObject> query) {
        query.setLimit(this.objectsPerPage);
        query.setSkip(page * this.objectsPerPage);
    }

    public void addOnQueryLoadListener(OnQueryLoadListener<ParseObject> listener) {
        this.onQueryLoadListeners.add(listener);
    }

    public void removeOnQueryLoadListener(OnQueryLoadListener<ParseObject> listener) {
        this.onQueryLoadListeners.remove(listener);
    }

    private void notifyOnLoadingListeners() {
        for (OnQueryLoadListener<ParseObject> listener : this.onQueryLoadListeners) {
            listener.onLoading();
        }
    }

    private void notifyOnLoadedListeners(List<ParseObject> objects, Exception e) {
        for (OnQueryLoadListener<ParseObject> listener : this.onQueryLoadListeners) {
            listener.onLoaded(objects, e);
        }
    }





}
