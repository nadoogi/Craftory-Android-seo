package cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.HashMap;
import java.util.Map;

import cloud.scalabl.httpspg_app_q0nz5d8azjwlxxrweewghhzuexr7rc.anicast.functions.FunctionBase;

public class PermissionActivity extends AppCompatActivity {

    private ParseInstallation parseInstallation;
    private FunctionBase functionBase;
    private ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        parseInstallation = ParseInstallation.getCurrentInstallation();
        currentUser = ParseUser.getCurrentUser();
        functionBase = new FunctionBase(this, getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // ...
            }

            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        }



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {

            if (grantResults.length > 0) {

                for (int i=0; i<grantResults.length; ++i) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        // 하나라도 거부한다면.
                        new AlertDialog.Builder(this).setTitle("알림").setMessage("권한을 허용해주셔야 앱을 이용할 수 있습니다.")
                                .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                }).setNegativeButton("권한 설정", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                getApplicationContext().startActivity(intent);
                            }
                        }).setCancelable(false).show();

                        return;
                    }
                }

                if(parseInstallation.get("firstTime") == null){

                    Log.d("check_error", "1");
                    Intent intent = new Intent(getApplicationContext(), SignFirstTimeActivity.class);
                    startActivity(intent);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    finish();


                } else {

                    Log.d("check_error", "2");

                    if(currentUser != null){

                        HashMap<String, Object> params = new HashMap<>();

                        params.put("key", currentUser.getSessionToken());

                        ParseCloud.callFunctionInBackground("mytag_reset", params, new FunctionCallback<Map<String, Object>>() {

                            public void done(Map<String, Object> mapObject, ParseException e) {

                                if (e == null) {

                                    Log.d("result", mapObject.toString());

                                    if(mapObject.get("status").toString().equals("true")){

                                        try {

                                            Log.d("check_error", "3");

                                            ParseUser fetchedUser = currentUser.fetch();

                                            functionBase.enteranceWithGuideCheckFunction(PermissionActivity.this, fetchedUser, parseInstallation);

                                        } catch (ParseException e1) {

                                            Log.d("check_error", "4");

                                            TastyToast.makeText(getApplicationContext(), "입장하는데 실패 했습니다. 다시 시도해주세요!!", TastyToast.LENGTH_LONG, TastyToast.ERROR);
                                            e1.printStackTrace();

                                        }

                                    } else {

                                        TastyToast.makeText(getApplicationContext(), "댓글 입력이 실패 했습니다.", TastyToast.LENGTH_LONG, TastyToast.ERROR);

                                    }

                                } else {

                                    e.printStackTrace();

                                }
                            }
                        });




                    } else {

                        Log.d("check_error", "5");

                        Intent intent = new Intent(getApplicationContext(), MainBoardActivity.class);
                        startActivity(intent);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        finish();

                    }

                }


            }
        }
    }


}
