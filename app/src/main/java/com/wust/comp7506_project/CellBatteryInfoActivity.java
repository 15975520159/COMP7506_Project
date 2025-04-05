package com.wust.comp7506_project;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CellBatteryInfoActivity extends AppCompatActivity {
    public static String exception;
    public static double MaxCellV;
    public static double MinCellV;

    public static int MaxCellVNo;
    public static int MinCellVNo;
    public static double CellAverV;
    public static double CellMaxDiffV;

    public static int CellNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            // 设置状态栏透明，允许我们自定义背景
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // 将状态栏背景设置为渐变色的 Drawable
            window.setStatusBarColor(Color.TRANSPARENT); // 先设置透明背景
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            // 使用渐变背景（自定义背景）
            View statusBarView = new View(this);
            statusBarView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight()));
            statusBarView.setBackgroundResource(R.drawable.gradient);  // 设置渐变背景
            ((ViewGroup) window.getDecorView()).addView(statusBarView);
            // 设置导航栏颜色为指定颜色
            window.setNavigationBarColor(Color.parseColor("#F3F3F3"));
            // 让内容延伸至状态栏下方
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.cell_battery_info);

        ImageView loading = findViewById(R.id.loading_gif);
        loading.setVisibility(View.VISIBLE);

        Bundle extras = getIntent().getExtras();
        exception = extras.getString("exception");
        if (exception.equals("no battery table")) {
            loading.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "此电池暂无数据信息", Toast.LENGTH_LONG).show();
        }

        FrameLayout backButtonLayout = findViewById(R.id.backButtonLayout);
        backButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CellBatteryInfoActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(R.xml.slide_in_left, R.xml.slide_out_right);
                finish();
            }
        });
        FrameLayout mapButtonLayout = findViewById(R.id.mapButtonLayout);
        mapButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建 Intent 跳转至 MapActivity
                Intent intent = new Intent(CellBatteryInfoActivity.this, MapActivity.class);
                // 将 battery_id 添加到 Intent 的 extras 中
                String batteryId = getIntent().getStringExtra("battery_id");
                intent.putExtra("battery_id", batteryId);
                intent.putExtra("exception", exception);
                startActivity(intent);
                overridePendingTransition(R.xml.slide_in_right, R.xml.slide_out_left);
            }
        });
        FrameLayout wholeBatteryInfo = findViewById(R.id.wholeBatteryButtonLayout);
        wholeBatteryInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CellBatteryInfoActivity.this, BatteryInfoActivity.class);
                String batteryId = getIntent().getStringExtra("battery_id");
                intent.putExtra("battery_id", batteryId);
                intent.putExtra("exception", exception);
                startActivity(intent);
                overridePendingTransition(R.xml.slide_in_left, R.xml.slide_out_right);
                finish();
            }
        });

        if (extras != null) {
            // 从 Bundle 中提取数据
            String batteryId = extras.getString("battery_id");
            TextView Battery_Id = findViewById(R.id.battery_id_2);
            Battery_Id.setText("ID: " + batteryId);
            // 启动异步任务，传递 batteryId
            new LoadBatteryInfoTask().execute(batteryId);
        }

    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    // 异步任务，用来从后台加载电池信息
    private class LoadBatteryInfoTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String batteryId = params[0];
            String urlString = "http://www.dgthreeteam.com/api/get_APP_request_data_list_by_battery_id/" + batteryId;
            try {
                // 发起 GET 请求并获取响应
                return getJsonResponse(urlString);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            // 在UI线程中处理网络响应
            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    CellNumber = jsonObject.optInt("Cell_Number", 0);

                    // 获取 last_result 数据
                    JSONObject lastResult = jsonObject.getJSONObject("battery_last_data").getJSONObject("last_result");

                    List<Integer> vList = new ArrayList<>();
                    for (int i = 1; i <= CellNumber; i++) {
                        String cellKey = "cell" + i + "_v";  // cell1_v, cell2_v, ...
                        if (lastResult.has(cellKey)) {
                            int socValue = lastResult.optInt(cellKey, 0); // 获取电池电压值（SOC 数据）
                            vList.add(socValue);  // 添加到列表中
                        }
                    }

                    List<Float> tempList = new ArrayList<>();
                    int tempCount = 0;  // 用于统计 temp 数据的数量
                    int i = 1;

                    String tempKey = "temp" + i;
                    while(lastResult.has(tempKey)){
                        float tempValue = (float) lastResult.optDouble(tempKey, 0.0); // 获取温度值
                        tempList.add(tempValue);  // 添加到列表中
                        tempCount++;  // 增加 temp 数据的数量
                        i++;
                        tempKey = "temp" + i;
                    }

                    MaxCellV = jsonObject.getJSONObject("battery_last_data").getJSONObject("last_result").getInt("cell_high_v");
                    MinCellV = jsonObject.getJSONObject("battery_last_data").getJSONObject("last_result").getInt("cell_low_v");
                    MaxCellVNo = jsonObject.getJSONObject("battery_last_data").getJSONObject("last_result").getInt("cell_high_no");
                    MinCellVNo = jsonObject.getJSONObject("battery_last_data").getJSONObject("last_result").getInt("cell_low_no");
                    CellAverV = jsonObject.getJSONObject("battery_last_data").getJSONObject("last_result").getInt("cell_v_avg");

                    CellMaxDiffV = MaxCellV - MinCellV;

                    TextView Cell_Max_V = findViewById(R.id.cellMaxV);
                    TextView Cell_Min_V = findViewById(R.id.cellMinV);
                    TextView Cell_Aver_V = findViewById(R.id.cellAverV);
                    TextView Max_V_Diff = findViewById(R.id.maxVDiff);

                    Cell_Max_V.setText(String.valueOf(Math.round(MaxCellV)) + "mV (Cell " + MaxCellVNo + ")");
                    Cell_Min_V.setText(String.valueOf(Math.round(MinCellV)) + "mV (Cell " + MinCellVNo + ")");
                    Cell_Aver_V.setText(String.valueOf(Math.round(CellAverV)) + "mV");
                    Max_V_Diff.setText(String.valueOf(Math.round(CellMaxDiffV)) + "mV");

                    RecyclerView recyclerView_v = findViewById(R.id.recyclerView_v);

                    // 使用 Activity 上下文来设置 GridLayoutManager
                    GridLayoutManager layoutManager_v = new GridLayoutManager(recyclerView_v.getContext(), 3);  // 每行 4 个
                    recyclerView_v.setLayoutManager(layoutManager_v);

                    // 设置适配器
                    CellBatteryCardAdapter cellBatteryCardAdapter = new CellBatteryCardAdapter(CellNumber, vList);
                    recyclerView_v.setAdapter(cellBatteryCardAdapter);

                    RecyclerView recyclerView_temp = findViewById(R.id.recyclerView_temp);

                    // 使用 Activity 上下文来设置 GridLayoutManager
                    GridLayoutManager layoutManager_temp = new GridLayoutManager(recyclerView_temp.getContext(), 3);  // 每行 4 个
                    recyclerView_temp.setLayoutManager(layoutManager_temp);

                    // 设置适配器
                    CellTempCardAdapter cellTempCardAdapter  = new CellTempCardAdapter(tempCount, tempList);
                    recyclerView_temp.setAdapter(cellTempCardAdapter);

                    ImageView loading = findViewById(R.id.loading_gif);
                    loading.setVisibility(View.INVISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 发送 GET 请求并获取响应的 JSON 字符串
    public static String getJsonResponse(String urlString) throws Exception {
        // 创建 URL 对象
        URL url = new URL(urlString);
        // 打开连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);  // 设置连接超时为5秒
        connection.setReadTimeout(5000);     // 设置读取超时为5秒
        // 获取响应
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        // 读取响应内容
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();  // 返回响应内容
    }
}
