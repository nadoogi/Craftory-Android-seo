package cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ui.widget.ParseQueryAdapter;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.R;
import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.functions.FunctionBase;
import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.functions.FunctionComment;
import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.viewHolders.CommentHeaderViewHolder;
import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.viewHolders.CommentViewHolder;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by ssamkyu on 2017. 5. 8..
 */

public class CommentDefaultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public  interface OnQueryLoadListener<ParseObject> {
        public void onLoading();
        public void onLoaded(List<ParseObject> objects, Exception e);
    }

    private List<OnQueryLoadListener<ParseObject>> onQueryLoadListeners = new ArrayList<>();
    private ArrayList<ParseObject> items;
    private ParseQueryAdapter.QueryFactory<ParseObject> queryFactory;
    private List<List<ParseObject>> objectPages;
    private int objectsPerPage = 25;
    private boolean paginationEnabled = true;
    private boolean hasNextPage = true;
    private int currentPage = 0;
    private RequestManager requestManager;
    protected Context context;
    protected ParseUser currentUser;
    private ParseObject parentObject;
    private int HeaderViewCode = 1000;
    private FunctionBase functionBase;


    public CommentDefaultAdapter(Context context, RequestManager requestManager, ParseObject parentOb) {

        this.requestManager = requestManager;
        this.context = context;
        this.currentUser = ParseUser.getCurrentUser();
        this.items = new ArrayList<>();
        this.objectPages = new ArrayList<>();
        this.parentObject = parentOb;
        this.functionBase = new FunctionBase(context);
        Log.d("reply objectId", parentOb.getObjectId());

        this.queryFactory = new ParseQueryAdapter.QueryFactory<ParseObject>() {
            @Override
            public ParseQuery<ParseObject> create() {

                ParseQuery<ParseObject> query = ParseQuery.getQuery("Reply");
                //query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
                query.whereEqualTo("status", true);
                query.whereEqualTo("parentOb", parentObject);
                query.include("user");
                query.include("poke_item");
                query.include("parentOb");
                query.orderByDescending("createdAt");

                return query;
            }
        };

        loadObjects(currentPage);



    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == HeaderViewCode){

            View headerView;
            headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_comment_header, parent, false);

            return new CommentHeaderViewHolder(headerView);

        } else {

            View commentView;

            commentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_post_comment_reply, parent, false);

            return new CommentViewHolder(commentView);

        }



    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if(holder instanceof CommentHeaderViewHolder){

            LinearLayout comment_layout = ((CommentHeaderViewHolder)holder).getCommentLayout();
            CircleImageView comment_photo = ((CommentHeaderViewHolder)holder).getCommentPhoto();
            TextView comment_name = ((CommentHeaderViewHolder)holder).getCommentName();
            TextView comment_updated = ((CommentHeaderViewHolder)holder).getCommentUpdated();
            TextView comment_body = ((CommentHeaderViewHolder)holder).getCommentBody();
            LinearLayout comment_button = ((CommentHeaderViewHolder)holder).getCommentButton();
            ImageView comment_icon = ((CommentHeaderViewHolder)holder).getCommentIcon();
            TextView comment_count = ((CommentHeaderViewHolder)holder).getCommentCount();

            LinearLayout like_button = ((CommentHeaderViewHolder)holder).getLikeButton();
            ImageView like_icon = ((CommentHeaderViewHolder)holder).getLikeIcon();
            TextView like_count = ((CommentHeaderViewHolder)holder).getLikeCount();

            LinearLayout best_layout = ((CommentHeaderViewHolder)holder).getBestLayout();
            LinearLayout post_image_layout = ((CommentHeaderViewHolder)holder).getPostImageLayout();
            ImageView comment_image = ((CommentHeaderViewHolder)holder).getCommentImage();

            if(best_layout != null){

                best_layout.setVisibility(View.GONE);
            }

            ParseObject userOb = parentObject.getParseObject("user");
            ParseObject pokeItemOb = parentObject.getParseObject("poke_item");

            if(userOb != null){

                functionBase.profileImageLoading(comment_photo, userOb, requestManager);
                functionBase.profileNameLoading(comment_name, userOb);

                comment_updated.setText( functionBase.dateToString( parentObject.getCreatedAt() ) );
                comment_count.setText( String.valueOf( parentObject.getInt("comment_count") )  );
                like_count.setText( String.valueOf(parentObject.getInt("like_count"))  );

                if(pokeItemOb != null){

                    comment_image.setVisibility(View.VISIBLE);
                    comment_body.setVisibility(View.GONE);

                    functionBase.generalImageLoading(comment_image, pokeItemOb, requestManager);

                } else {

                    comment_image.setVisibility(View.GONE);
                    comment_body.setVisibility(View.VISIBLE);
                    comment_body.setText( parentObject.getString("body") );

                }

            }


        } else if(holder instanceof CommentViewHolder){

            final ParseObject commentOb = getItem(position);

            if(commentOb != null){

                Log.d("commentOb", commentOb.getObjectId());

            }

            FunctionComment functionComment = new FunctionComment(context);
            functionComment.CommentHeaderAdapterBuilder(commentOb, holder, parentObject, this, "reply", requestManager);

        }

    }

    @Override
    public int getItemViewType(int position) {

        if(position == 0){

            return HeaderViewCode;

        } else {

            return position;
        }
    }


    @Override
    public int getItemCount() {
        return items.size()+1;
    }


    public ParseObject getItem(int position) {
        return items.get(position-1);
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
