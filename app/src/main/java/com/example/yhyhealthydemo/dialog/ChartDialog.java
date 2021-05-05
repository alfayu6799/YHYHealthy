package com.example.yhyhealthydemo.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.yhyhealthydemo.R;
import com.example.yhyhealthydemo.datebase.TempDataApi;
import com.example.yhyhealthydemo.tools.DateUtil;
import com.example.yhyhealthydemo.datebase.Degree;
import com.example.yhyhealthydemo.tools.TargetZoneLineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;

/****************************
 * 藍芽體溫圖表顯示
 * 使用者,大頭照,溫度,開始時間,結束時間
 * Chart採用第三方庫MPAndroidChart
 * 最高溫度固定42
 * 最低體溫固定35.5以下
* *********************/

public class ChartDialog extends Dialog {

    private static final String TAG = "ChartDialog";

    //Chart畫面用
    private CircleImageView bleUserImage;  //大頭照
    private TextView bleUserName;          //使用者
    private TextView bleUserDegree;        //溫度
    private TextView firstDateTime;        //開始時間
    private TextView endDateTime;          //結束時間
    private ImageView closeDialog;         //結束此Dialog
    private TextView  nextMeasureTime;     //下次量測時間

    private TempDataApi.SuccessBean data;       //使用者DataBean
    private ArrayList<Degree> DataArray;        //體溫DataBean

    private String correctDate;
    private Double degree;

    //圖表:折線圖
    private TargetZoneLineChart bleLineChart;

    //建構子
    public ChartDialog(@NonNull Context context, TempDataApi.SuccessBean data) {
        super(context);
        this.data = data;
    }

    @SuppressLint({"NewApi", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_ble_chart);

        //圖表
        bleLineChart = findViewById(R.id.lineChartBle);

        bleUserImage = findViewById(R.id.imgBleUserShot);
        bleUserName = findViewById(R.id.tvBleUserName);
        bleUserDegree = findViewById(R.id.tvUserDegree);
        firstDateTime = findViewById(R.id.tvStartDate);
        endDateTime = findViewById(R.id.tvEndDate);
        nextMeasureTime = findViewById(R.id.tvNextMeasureTime);

        closeDialog = findViewById(R.id.imgCloseDialog);
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataArray.clear();
                dismiss();
            }
        });

        //塞入相對的資料
        bleUserName.setText(data.getUserName());

        //base64解碼大頭貼
        byte[] imageByteArray = Base64.decode(data.getHeadShot(), Base64.DEFAULT);
        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageByteArray,0, imageByteArray.length);
        bleUserImage.setImageBitmap(decodedImage);

        bleUserDegree.setText(String.valueOf(data.getDegree())); //使用者目前體溫

        DataArray = new ArrayList<>();

        //避免沒有量測時按下圖表功能造成閃退
        if (data != null && data.getDegreeList().size() > 0){
            //開始時間
            String firstTime = data.getDegreeList().get(0).getDate();
            firstDateTime.setText(firstTime);

            //結束時間
            String lastTime = data.getDegreeList().get(data.getDegreeList().size()-1).getDate();
            endDateTime.setText(lastTime);
            DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd HH:mm");
            DateTime testDataTime = dtf.parseDateTime(lastTime).plusMinutes(3);
            nextMeasureTime.setText(getContext().getString(R.string.next_measure_time_is) + testDataTime.toString("HH:mm"));

            for (int i = 0; i < data.getDegreeList().size(); i++){
                correctDate = DateUtil.fromDateToTime(data.getDegreeList().get(i).getDate());
                degree = data.getDegreeList().get(i).getDegree();
                setChart();
            }
        }
    }

    private void setChart() {

        //將溫度與日期塞入DataArray
         DataArray.add(new Degree(degree, correctDate));

         ArrayList<String> label = new ArrayList<>();    //X軸(時間)
         ArrayList<Entry> entries = new ArrayList<>();   //Y軸(體溫)

         //將資料填入X(日期)與Y軸(溫度)
         for (int i = 0; i < DataArray.size(); i++ ){
             String xValues = DataArray.get(i).getDate();
             double yValues = DataArray.get(i).getDegree();
             entries.add(new Entry(i , (float)yValues));
             label.add(xValues);
         }

         LineDataSet lineDataSet = new LineDataSet(entries,"");
         lineDataSet.setColor(Color.RED);  //軸線顏色
         LineData data = new LineData(lineDataSet);
         bleLineChart.setData(data);

         XAxis xAxis = bleLineChart.getXAxis(); //取得X軸
         xAxis.setValueFormatter(new IndexAxisValueFormatter(label)); //x軸放入自定義的時間
         xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); //日期顯示在底層
         xAxis.setGranularity(1f);                      //x軸最小間隔
         xAxis.setLabelRotationAngle(-30);              //X軸傾斜30度
         xAxis.setLabelCount(label.size());             //X軸的數量來自資料集
         xAxis.setGridColor(Color.BLACK);               //設置X軸的網格線的顏色
         xAxis.setAxisLineColor(Color.BLACK);
         xAxis.enableGridDashedLine(10f,10f,0f); //X軸格線虛線

         YAxis rightAxis = bleLineChart.getAxisRight();         //獲取右側的Y軸
         rightAxis.setEnabled(false);                           //不顯示右側Y軸
         YAxis leftAxis = bleLineChart.getAxisLeft();           //獲取左側的Y軸線
         leftAxis.setDrawGridLines(false);                      //隱藏Y軸的格線

         leftAxis.setLabelCount(7);    //35-42七組數據
         leftAxis.setAxisMaximum(42);  //最高體溫
         leftAxis.setAxisMinimum(35);  //最低體溫

         //體溫微高
         float rangeHigh = 42f;
         float rangeLow = 37.5f;

         //正常體溫
         float rangeHigh1 = 37.5f;
         float rangeLow1 = 35.5f;

         //低體溫
         float rangeHigh2 = 35.5f;
         float rangeLow2 = 35.0f;

         //體溫微高背景顏色
         bleLineChart.addTargetZone(new TargetZoneLineChart.TargetZone(Color.parseColor("#f5c6cb"),rangeLow,rangeHigh));
         //正常體溫背景顏色
         bleLineChart.addTargetZone(new TargetZoneLineChart.TargetZone(Color.parseColor("#ffffff"),rangeLow1,rangeHigh1));
         //低體溫背景顏色
         bleLineChart.addTargetZone(new TargetZoneLineChart.TargetZone(Color.parseColor("#ffff35"),rangeLow2,rangeHigh2));

         bleLineChart.getLegend().setEnabled(false);            //隱藏圖例
         bleLineChart.getDescription().setEnabled(false);       //隱藏描述
         bleLineChart.invalidate();                             //重新刷圖表
    }

    //當藍芽的體溫值有變化時將會透過此方法將舊有的value更新
    @SuppressLint("SetTextI18n")
    public void update(TempDataApi.SuccessBean newMemberBean) {
        //當使用者與藍芽回來的資料是同一人才進行更新圖表 2021/05/03
        if(data.getUserName().equals(newMemberBean.getUserName())){

            //結束時間
            String lastTime = newMemberBean.getDegreeList().get(newMemberBean.getDegreeList().size()-1).getDate(); //取得最後一筆
            endDateTime.setText(lastTime);
            DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd HH:mm");
            DateTime testDataTime = dtf.parseDateTime(lastTime).plusMinutes(3);
            nextMeasureTime.setText(getContext().getString(R.string.next_measure_time_is) + testDataTime.toString("HH:mm"));

            bleUserDegree.setText(String.valueOf(newMemberBean.getDegree()));
            correctDate = DateUtil.fromDateToTime(newMemberBean.getDegreeList().get(newMemberBean.getDegreeList().size()-1).getDate());
            degree = newMemberBean.getDegree(); //取最新的溫度
            setChart(); //製作折線圖
        }
    }
}
