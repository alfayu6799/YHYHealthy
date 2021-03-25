package com.example.yhyhealthydemo.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthydemo.R;
import com.example.yhyhealthydemo.TemperatureActivity;
import com.example.yhyhealthydemo.module.ApiProxy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

import static android.R.layout.simple_list_item_1;
import static com.example.yhyhealthydemo.module.ApiProxy.REMOTE_USER_LIST;

public class RemoteAccountDialog extends Dialog {

    private static final String TAG = "RemoteAccountDialog";

    private Context context;

    private RemoteAccountDialog.PriorityListener listener;

    private ApiProxy proxy;

    private ArrayList<String> accountNo;
    private ListView listView;
    private ListAdapter listAdapter;
    private TextView textView;
    public ArrayList<Map<String, Object>> list = new ArrayList<Map<String,Object>>();


    /**
     * 自定義 Dialog listener
     * **/
    public interface PriorityListener{
        void setActivity(String name, String gender, String birthday, String weight, String height);
    }

    public RemoteAccountDialog(Context context, int theme, RemoteAccountDialog.PriorityListener listener){
        super(context, theme);
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_remote_account, null);
        setContentView(view);

        listView = findViewById(R.id.listView);

        initData();
    }

    private void initData() {
        proxy = ApiProxy.getInstance();
        proxy.buildPOST(REMOTE_USER_LIST, "" , requestListener);
    }

    private ApiProxy.OnApiListener requestListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            Log.d(TAG, "onSuccess: " + result.toString());
            new Thread(){
                @Override
                public void run() {
                    ArrayList<String> items = new ArrayList<String>();
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0){
                            JSONArray array = object.getJSONArray("success");
                            for (int i = 0; i < array.length(); i++){
                                items.add(String.valueOf(array.get(i)));
                            }
                            listAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_expandable_list_item_1, items);
                            listView.setAdapter(listAdapter);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        @Override
        public void onFailure(String message) {

        }

        @Override
        public void onPostExecute() {

        }
    };

    private void parserJson(JSONObject result) {


        //將資料塞到ListView
        listAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_expandable_list_item_1, accountNo);
        listView.setAdapter(listAdapter);

    }
}
