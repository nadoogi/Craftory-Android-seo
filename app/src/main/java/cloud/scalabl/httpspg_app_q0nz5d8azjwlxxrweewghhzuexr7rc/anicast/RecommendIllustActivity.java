package cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.adapters.RecommendIllustAdapter;
import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.functions.FunctionBase;
import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.listeners.RecommendItemSelectListener;
import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.listeners.TagSelectListener;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class RecommendIllustActivity extends AppCompatActivity implements TagSelectListener {
    //레이아웃의 텍스트뷰 선언
    TextView tag_selected_1;
    TextView tag_selected_2;
    TextView tag_selected_3;
    TextView tag_selected_4;
    TextView tag_selected_5;
    TextView tag_count;
    TextView tag_message;
    //레이아웃의 라인레이아웃 선언
    LinearLayout tag1_button;
    LinearLayout tag2_button;
    LinearLayout tag3_button;
    LinearLayout tag4_button;
    LinearLayout tag5_button;

    //parse db 관련 명렁문(currentUser)
    ParseUser currentUser;
    //int tag count 는 5
    int inputTagCount = 5;
    //추후 선택을 하기 전까진 거짓 값 치환
    Boolean tagSelected1 = false;
    Boolean tagSelected2 = false;
    Boolean tagSelected3 = false;
    Boolean tagSelected4 = false;
    Boolean tagSelected5 = false;

    RelativeLayout next_button;

    String tag1;
    String tag2;
    String tag3;
    String tag4;
    String tag5;

    //리사이클 뷰 선언
    RecyclerView artwork_list;
    RequestManager requestManager;
    int pastVisibleItems, visibleItemCount, totalItemCount;
    RecommendIllustAdapter recommendIllustAdapter;

    private int selectedCount;
    private List<ParseObject> selectedTagItemList;

    private Boolean inputStatus;
    private FunctionBase functionBase;

    //onCreate // 해당 엑티비티의 뼈대 제작(
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend_illust);

        //tag selected와 tag button 의 기본 뼈대 제작
        tag_selected_1 = (TextView) findViewById(R.id.tag_selected_1);
        tag_selected_2 = (TextView) findViewById(R.id.tag_selected_2);
        tag_selected_3 = (TextView) findViewById(R.id.tag_selected_3);
        tag_selected_4 = (TextView) findViewById(R.id.tag_selected_4);
        tag_selected_5 = (TextView) findViewById(R.id.tag_selected_5);

        tag1_button = (LinearLayout) findViewById(R.id.tag1_button);
        tag2_button = (LinearLayout) findViewById(R.id.tag2_button);
        tag3_button = (LinearLayout) findViewById(R.id.tag3_button);
        tag4_button = (LinearLayout) findViewById(R.id.tag4_button);
        tag5_button = (LinearLayout) findViewById(R.id.tag5_button);

        next_button = (RelativeLayout) findViewById(R.id.next_button);
        next_button.setVisibility(View.GONE);


        artwork_list = (RecyclerView) findViewById(R.id.artwork_list);

        tag_count = (TextView) findViewById(R.id.tag_count);
        tag_message = (TextView) findViewById(R.id.tag_message);

        functionBase = new FunctionBase(this, getApplicationContext());
        currentUser = ParseUser.getCurrentUser();

        //배열 선언후 db currentuser 항목에 선택 되었을시 치환
        ArrayList<String> blankArray = new ArrayList<>();
        currentUser.put("tag_selected", blankArray);
        currentUser.saveInBackground(); // parse 명령어중 쓰레드에 저장하는 명령어

        //user tag status를 스트링 형태로 로그를 남기는 구문
        Log.d("currentUser tag status", String.valueOf(currentUser.getBoolean("tag_check")) );


        selectedTagItemList = new ArrayList<>();
        inputStatus = true;

        selectedCount = 0;

        requestManager = Glide.with(getApplicationContext());

        final LinearLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),3);

        artwork_list.setLayoutManager(layoutManager);
        artwork_list.setHasFixedSize(true);
        artwork_list.setNestedScrollingEnabled(false);

        recommendIllustAdapter = new RecommendIllustAdapter(getApplicationContext(), requestManager );
        recommendIllustAdapter.setTagSelectListener(this);

        artwork_list.setItemAnimator(new SlideInUpAnimator());
        artwork_list.setAdapter(recommendIllustAdapter);

        artwork_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);


                if(dy > 0){
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();


                    if ( (visibleItemCount + pastVisibleItems) >= totalItemCount) {

                        recommendIllustAdapter.loadNextPage();

                    }

                }

            }



        });


        //다음 버튼 관련 구문
        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //다음 버튼 숨김
                next_button.setClickable(false);
                //선택 수가 5개와 같을때
                if(selectedCount == 5){

                    ArrayList<String> tagList = new ArrayList<>();

                    //parse db에 selectedTagItemList 사이즈가 1보다 커질때까지
                    for(int i=0;selectedTagItemList.size()>i;i++){

                        // db에 selectedTagItemList에서 for문 i에 맞는 값을 tag list 배열값에 json 형태로 치환
                        ArrayList<String> tag_list = functionBase.jsonArrayToArrayList(selectedTagItemList.get(i).getJSONArray("tag_array"));


                        for(int j=0;tag_list.size()>j;j++){

                            tagList.add(tag_list.get(j));
                            //for문이 돌아갈동안 각각에 맞는 항목을 tagLogob db 항목에 치환
                            ParseObject tagLogOb = new ParseObject("TagLog");
                            tagLogOb.put("add", true);
                            tagLogOb.put("place", "recommend_tag");
                            tagLogOb.put("user", currentUser);
                            tagLogOb.put("status", true);
                            tagLogOb.put("tag", tag_list.get(j));
                            tagLogOb.put("type", "recommend");
                            tagLogOb.saveInBackground();

                        }
                    }

                    //currentUser에 tags 값과 tag_check 값을 치환
                    currentUser.put("tags", tagList);
                    currentUser.put("tag_check", true);

                    //저장 하기전 에러를 체크하는 부분인데 파악이 힘듬
                    currentUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                            if(e==null){

                                Intent intent = new Intent(getApplicationContext(), RecommendCreatorActivity.class);
                                startActivity(intent);

                                finish();

                            } else {
                                // 에러가 없을때 다음 버튼 팝업
                                e.printStackTrace();
                                next_button.setClickable(true);
                            }

                        }
                    });



                } else {
                    //에러가 없지만 태그 선택을 하지않았을때 다음버튼은 팝업 되지만 선택을 완료해달라는 토스트 메세지 팝업
                    next_button.setClickable(true);

                    TastyToast.makeText(getApplicationContext(), "취향 선택을 완료해주세요.", TastyToast.LENGTH_LONG, TastyToast.ERROR);

                }

            }
        });

        progressCheck(selectedCount);


        // 스트링 mkdirpath에 크래프토리 어플의 절대경로를 치환
        String mkdirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/craftory";
        //폴더 생성
        File mkDir = new File(mkdirPath);
        mkDir.mkdir();

        JSONArray initData = new JSONArray();
        initData.put("init");

        //어플리케이션 절대경로에 제작된 폴더에 temp.json 파일을 생성후 데이터 치환
        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/craftory/temp.json";
        functionBase.fileSave(sdPath, initData);


        //File obname = new File(getApplicationContext().getFilesDir(),"craft");
        //if(!dir.exists())

            //dir.mkdirs();
        //obname.mkdir();

        //JSONArray obdata = new JSONArray();
        //initData.put("init");




    }


    private void progressCheck(int counter){

        if(counter == 5 || counter > 5){

            inputStatus = false;

        } else {

            inputStatus = true;

        }
        // 클릭 했을때 이미지변경 하는 구문
        switch (counter){

            case 0:

                tag1_button.setBackgroundResource(R.drawable.button_radius_5dp_point_color);
                tag2_button.setBackgroundResource(R.drawable.button_radius_5dp_point_color);
                tag3_button.setBackgroundResource(R.drawable.button_radius_5dp_point_color);
                tag4_button.setBackgroundResource(R.drawable.button_radius_5dp_point_color);
                tag5_button.setBackgroundResource(R.drawable.button_radius_5dp_point_color);

                next_button.setVisibility(View.GONE);

                break;

            case 1:

                tag1_button.setBackgroundResource(R.drawable.button_radius_5dp_point_fullcolor);
                tag2_button.setBackgroundResource(R.drawable.button_radius_5dp_point_color);
                tag3_button.setBackgroundResource(R.drawable.button_radius_5dp_point_color);
                tag4_button.setBackgroundResource(R.drawable.button_radius_5dp_point_color);
                tag5_button.setBackgroundResource(R.drawable.button_radius_5dp_point_color);

                next_button.setVisibility(View.GONE);

                break;

            case 2:

                tag1_button.setBackgroundResource(R.drawable.button_radius_5dp_point_fullcolor);
                tag2_button.setBackgroundResource(R.drawable.button_radius_5dp_point_fullcolor);
                tag3_button.setBackgroundResource(R.drawable.button_radius_5dp_point_color);
                tag4_button.setBackgroundResource(R.drawable.button_radius_5dp_point_color);
                tag5_button.setBackgroundResource(R.drawable.button_radius_5dp_point_color);

                next_button.setVisibility(View.GONE);

                break;

            case 3:

                tag1_button.setBackgroundResource(R.drawable.button_radius_5dp_point_fullcolor);
                tag2_button.setBackgroundResource(R.drawable.button_radius_5dp_point_fullcolor);
                tag3_button.setBackgroundResource(R.drawable.button_radius_5dp_point_fullcolor);
                tag4_button.setBackgroundResource(R.drawable.button_radius_5dp_point_color);
                tag5_button.setBackgroundResource(R.drawable.button_radius_5dp_point_color);

                next_button.setVisibility(View.GONE);

                break;

            case 4:

                tag1_button.setBackgroundResource(R.drawable.button_radius_5dp_point_fullcolor);
                tag2_button.setBackgroundResource(R.drawable.button_radius_5dp_point_fullcolor);
                tag3_button.setBackgroundResource(R.drawable.button_radius_5dp_point_fullcolor);
                tag4_button.setBackgroundResource(R.drawable.button_radius_5dp_point_fullcolor);
                tag5_button.setBackgroundResource(R.drawable.button_radius_5dp_point_color);

                next_button.setVisibility(View.GONE);

                break;

            case 5:

                tag1_button.setBackgroundResource(R.drawable.button_radius_5dp_point_fullcolor);
                tag2_button.setBackgroundResource(R.drawable.button_radius_5dp_point_fullcolor);
                tag3_button.setBackgroundResource(R.drawable.button_radius_5dp_point_fullcolor);
                tag4_button.setBackgroundResource(R.drawable.button_radius_5dp_point_fullcolor);
                tag5_button.setBackgroundResource(R.drawable.button_radius_5dp_point_fullcolor);

                next_button.setVisibility(View.VISIBLE);

                break;

        }

    }




    @Override
    public void onSelectedItem(ParseObject itemOb, Boolean selected) {

        Log.d("selected", itemOb.getObjectId());
        Log.d("selected", selected.toString());
        Log.d("selected", inputStatus.toString());

        Boolean itemSelected;
        //selectedtagitemlist가 itemob 항목과 contains으로 비교 하는 이프문
        if(selectedTagItemList.contains(itemOb)){

            itemSelected = true;

        } else {

            itemSelected = false;

        }
        //현재의 inputstatus의 상태에 따라
        if(inputStatus){
            // 두 항목이 모두 참 일 경우
            if(selected && itemSelected){

                int indexNum = 0;

                for(int i=0; selectedTagItemList.size()>i;i++ ){
                    //...itemlist에 for문 i 에 해당 하는 정보가 itemob와 같으면
                    if(itemOb.getObjectId().equals( selectedTagItemList.get(i).getObjectId() )){
                        //인덱스 ??
                        indexNum = i;

                    }

                }

                Log.d("index", String.valueOf(indexNum) );
                Log.d("before", selectedTagItemList.toString());

                selectedTagItemList.remove(indexNum);

                Log.d("after", selectedTagItemList.toString());
                selectedCount = selectedTagItemList.size();

                progressCheck(selectedCount);

                Log.d("seletedTagItemList1", selectedTagItemList.toString());

                //recommendIllustAdapter.loadObjects(0);
                recommendIllustAdapter.notifyDataSetChanged();

                // 두 항목이 참값이 아닐경우
            } else if(!selected && !itemSelected) {
                //누락된 데이터값을 치환하고 프로세스 체크 하는 구문인거 같은데 파악이 힘듭니다
                selectedTagItemList.add(itemOb);

                selectedCount = selectedTagItemList.size();

                progressCheck(selectedCount);

                Log.d("seletedTagItemList2", selectedTagItemList.toString());

                //recommendIllustAdapter.loadObjects(0);
                recommendIllustAdapter.notifyDataSetChanged();

            } else if(selected && !itemSelected){

                int indexNum = 0;

                for(int i=0; selectedTagItemList.size()>i;i++ ){

                    if(itemOb.getObjectId().equals( selectedTagItemList.get(i).getObjectId() )){

                        indexNum = i;

                    }

                }

                Log.d("index", String.valueOf(indexNum) );
                Log.d("before", selectedTagItemList.toString());

                if(selectedTagItemList.size() != 0){

                    selectedTagItemList.remove(indexNum);

                    Log.d("after", selectedTagItemList.toString());
                    selectedCount = selectedTagItemList.size();

                    progressCheck(selectedCount);

                    //recommendIllustAdapter.loadObjects(0);
                    recommendIllustAdapter.notifyDataSetChanged();

                }

            } else if(!selected && itemSelected){

                Log.d("seletedTagItemList4", selectedTagItemList.toString());

                //recommendIllustAdapter.loadObjects(0);
                recommendIllustAdapter.notifyDataSetChanged();

            }

        } else {

            TastyToast.makeText(getApplicationContext(), "선택이 완료 되었습니다.", TastyToast.LENGTH_LONG, TastyToast.INFO);

        }


    }


}
