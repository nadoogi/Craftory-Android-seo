package cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.R;
import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.adapters.CommercialWensdayAdapter;


/**
 * Created by ssamkyu on 2017. 8. 7..
 */

public class ArtworkWensdayFragment extends Fragment {

    private static CommercialWensdayAdapter commercialWensAdapter;

    int pastVisibleItems, visibleItemCount, totalItemCount;
    private boolean loading = true;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_artwork_home, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView tues_list = (RecyclerView) view.findViewById(R.id.artwork_home_list);

        RequestManager requestManager = Glide.with(getActivity());

        //final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false);
        final LinearLayoutManager layoutManager = new GridLayoutManager(getActivity(),2);


        tues_list.setLayoutManager(layoutManager);
        tues_list.setHasFixedSize(true);
        tues_list.setNestedScrollingEnabled(false);

        //artworkHomeRecyclerAdapter = new ArtworkHomeRecyclerAdapter(getActivity(), requestManager );

        //commercialMondayAdapter = new CommercialMondayAdapter(getActivity(), requestManager);
        commercialWensAdapter = new CommercialWensdayAdapter(getActivity(), requestManager);
        tues_list.setAdapter(commercialWensAdapter);

        tues_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);


                if(dy > 0) //check for scroll down
                {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();


                    if(loading){

                        if ( (visibleItemCount + pastVisibleItems) >= totalItemCount) {

                            loading = false;

                            commercialWensAdapter.loadNextPage();

                            loading = true;

                        }

                    }



                }

            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();

    }


}
