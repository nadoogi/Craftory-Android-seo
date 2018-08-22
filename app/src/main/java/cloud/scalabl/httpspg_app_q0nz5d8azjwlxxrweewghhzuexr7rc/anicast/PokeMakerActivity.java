package cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.functions.FunctionBase;
import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.models.ArtistPost;
import in.myinnos.awesomeimagepicker.activities.AlbumSelectActivity;
import in.myinnos.awesomeimagepicker.helpers.ConstantsCustomGallery;
import in.myinnos.awesomeimagepicker.models.Image;
import me.gujun.android.taggroup.TagGroup;

public class PokeMakerActivity extends AppCompatActivity {

    private EditText body;
    private ImageView body_reset;
    private LinearLayout save_button;
    private ImageView preview;

    private ArrayList<Image> images;

    private TagGroup tagGroup;

    private ParseUser currentUser;

    private String imagePath;

    private String finalImage;

    private RequestManager requestManager;

    private File tempFile;


    private FunctionBase functionBase;

    private TextView file_status_text;

    private ArrayList<String> tagString;

    private EditText title;
    private ImageView title_reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poke_maker);

        requestManager = Glide.with(getApplicationContext());

        currentUser = ParseUser.getCurrentUser();

        body = (EditText) findViewById(R.id.post_body);
        body_reset = (ImageView) findViewById(R.id.body_reset);
        body_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                body.setText("");

            }
        });
        LinearLayout image_upload = (LinearLayout) findViewById(R.id.image_upload);
        preview = (ImageView) findViewById(R.id.preview);

        preview.setFocusableInTouchMode(true);
        preview.requestFocus();

        tagGroup = (TagGroup) findViewById(R.id.tag_group);
        save_button = (LinearLayout) findViewById(R.id.save_button);
        file_status_text = (TextView) findViewById(R.id.file_status_text);

        title = (EditText) findViewById(R.id.title);
        title_reset = (ImageView) findViewById(R.id.title_reset);



        LinearLayout back_button = (LinearLayout) findViewById(R.id.back_button);
        TextView back_button_text = (TextView) findViewById(R.id.back_button_text);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

        back_button_text.setText("이모티콘 만들기");

        finalImage = null;
        imagePath = null;
        images = new ArrayList<>();

        functionBase = new FunctionBase(this, getApplicationContext());


        image_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), AlbumSelectActivity.class);
                intent.putExtra(ConstantsCustomGallery.INTENT_EXTRA_LIMIT, 1); // set limit for image selection
                startActivityForResult(intent, ConstantsCustomGallery.REQUEST_CODE);

            }
        });

        title_reset.setVisibility(View.GONE);
        title_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                title.setText("");

            }
        });


        body.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if(hasFocus){

                    body_reset.setVisibility(View.VISIBLE);

                } else {

                    body_reset.setVisibility(View.GONE);

                }

            }
        });


        body_reset.setVisibility(View.GONE);

        body_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                body.setText("");

            }
        });



        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                save_button.setClickable(false);

                if(finalImage == null){

                    TastyToast.makeText(getApplicationContext(), "작품이 등록되지 않았습니다. 작품을 등록해 주세요!", TastyToast.LENGTH_LONG, TastyToast.ERROR);

                } else {


                    String[] taglist = tagGroup.getTags();
                    String searchString = "";

                    ArrayList<String> tagsArray = new ArrayList<>();

                    for(String tagItem : taglist){


                        if(tagItem.length() != 0){

                            tagsArray.add(tagItem);
                            searchString += tagItem + ",";

                        }

                    }

                    String fileType = "";

                    if(functionBase.gifChecker(finalImage)){

                        fileType = "gif";

                    } else {

                        fileType = "png";

                    }

                    String titleString = title.getText().toString();
                    String bodyString = body.getText().toString();


                    HashMap<String, Object> params = new HashMap<>();


                    if(tagsArray.size() != 0){

                        params.put("tag_array", tagsArray);

                    }

                    params.put("key", currentUser.getSessionToken());
                    params.put("title", titleString);
                    params.put("body", bodyString);
                    params.put("image_cdn", finalImage);
                    params.put("image_type", fileType);
                    params.put("search_string", searchString + bodyString);

                    Date uniqueIdDate = new Date();
                    String uniqueId = uniqueIdDate.toString();

                    params.put("uid", uniqueId);

                    ParseCloud.callFunctionInBackground("poke_image_save", params, new FunctionCallback<Map<String, Object>>() {

                        public void done(Map<String, Object> mapObject, ParseException e) {

                            if (e == null) {

                                Log.d("tag", mapObject.toString());

                                if(mapObject.get("status").toString().equals("true")){

                                    Log.d("step", "installation data check success");
                                    TastyToast.makeText(getApplicationContext(), "저장 완료", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                                    finish();


                                } else {

                                    Log.d("step", "installation data check fail");
                                    save_button.setClickable(true);


                                }

                            } else {


                                Log.d("step", "installation data check request fail");
                                e.printStackTrace();
                                save_button.setClickable(true);

                            }
                        }
                    });


                }


            }
        });

    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if (requestCode == ConstantsCustomGallery.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            //The array list has the image paths of the selected images

            file_status_text.setText("파일 선택 완료 대기 중..");

            images = data.getParcelableArrayListExtra(ConstantsCustomGallery.INTENT_EXTRA_IMAGES);

            imagePath = images.get(0).path;
            String imageName = images.get(0).name;

            UploadCallback callback = new UploadCallback() {
                @Override
                public void onStart(String requestId) {

                    file_status_text.setText("파일 업로드 시작");

                }

                @Override
                public void onProgress(String requestId, long bytes, long totalBytes) {

                    float progress = functionBase.makeProgress(Float.parseFloat(String.valueOf(bytes)), Float.parseFloat(String.valueOf(totalBytes)));
                    file_status_text.setText("업로드 중 : " + String.valueOf(bytes) + "/" + String.valueOf(totalBytes) + " || "  + String.valueOf(progress * 100) + "%");

                }

                @Override
                public void onSuccess(String requestId, Map resultData) {

                    file_status_text.setText("업로드 완료 || 이미지 미리보기 불러오는 중..");
                    Log.d("resultdata", resultData.get("secure_url").toString());

                    String uploadImg = resultData.get("secure_url").toString();
                    String[] splitString = uploadImg.split("/");
                    String uploadTarget = splitString[splitString.length-2] + "/" + splitString[splitString.length-1];
                    String finalUploadTaget = functionBase.imageUrlBase300 + uploadTarget;

                    finalImage = uploadTarget;

                    if(tempFile != null){

                        tempFile.delete();

                    }

                    requestManager
                            .load(finalUploadTaget)
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                            .transition(new DrawableTransitionOptions().crossFade())
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                    file_status_text.setText("업로드 완료 || 이미지 미리보기 실패");

                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                    file_status_text.setText("업로드 완료 || 이미지 미리보기 완료");

                                    return false;
                                }
                            })
                            .into(preview);

                }

                @Override
                public void onError(String requestId, ErrorInfo error) {

                    file_status_text.setText("업로드 실패");

                }

                @Override
                public void onReschedule(String requestId, ErrorInfo error) {

                    file_status_text.setText("업로드 실패");

                }
            };

            // 한글이 포함된 문자열
            Log.d("korean", "true");

            String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/craftory";

            File gifFile = new File(imagePath);
            tempFile = new File(sdPath);
            tempFile.mkdirs();

            tempFile = new File(sdPath, "temp.png");

            try {

                functionBase.copy(gifFile, tempFile);
                MediaManager.get().upload(sdPath+"/temp.png").unsigned(AppConfig.image_preset).callback(callback).dispatch();

            } catch (IOException e) {
                file_status_text.setText("업로드 실패");
                e.printStackTrace();
            }



        } else {

            file_status_text.setText("파일 선택 안됨");

        }



    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {

            finish();

        }

        //return super.onOptionsItemSelected(item);
        return true;
    }

    @Override
    public void onBackPressed() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);

        builder.title("확인");
        builder.content("이 페이지에서 나가시겠습니까?");
        builder.positiveText("예");
        builder.negativeText("아니요");
        builder.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {



            }
        });

        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                finish();

            }
        });
        builder.show();

    }


}
