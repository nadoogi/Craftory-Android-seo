package cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.R;

/**
 * Created by ssamkyu on 2017. 7. 10..
 */

public class ArtworkDashboardWebnovelRankingViewHolder extends RecyclerView.ViewHolder {

    ImageView seriese_image;
    TextView title;
    TextView nickname;
    LinearLayout profile_tag;
    TextView description;

    public ArtworkDashboardWebnovelRankingViewHolder(View itemView) {
        super(itemView);

        seriese_image = (ImageView) itemView.findViewById(R.id.seriese_image);
        title = (TextView) itemView.findViewById(R.id.title);
        nickname = (TextView) itemView.findViewById(R.id.nickname);
        profile_tag = (LinearLayout) itemView.findViewById(R.id.profile_tag);
        description = (TextView) itemView.findViewById(R.id.description);

    }

    public ImageView getSerieseImage(){return seriese_image;}
    public TextView getTitle(){return title;}
    public TextView getNickname(){return nickname;}
    public LinearLayout getProfileTag(){return profile_tag;}
    public TextView getDescription(){return description;}

}
