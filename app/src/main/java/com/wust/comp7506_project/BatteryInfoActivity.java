package com.wust.comp7506_project;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BatteryInfoActivity extends AppCompatActivity {
    public static String exception;
    public static String Battery_Type = "";         // 电池类型
    public static String Cell_Number = "";              // 电池单元数
    public static String Ambient_Temp = "";          // 环境温度
    public static String Balance_State = "";         // 平衡状态
    public static String Discharge_mos_State = "";   // 放电MOS状态
    public static String Charge_mos_State = "";      // 充电MOS状态
    public static String Cycle_Number =  "";          // 循环次数
    public static String Capacity;
    public static String Create_At;
    public static int Online_State;
    public static boolean Mos_Over_Temp = false;     // MOS超温
    public static boolean Charge_Over_Current = false; // 充电过流
    public static boolean Discharge_Over_Current1 = false; // 放电过流1
    public static boolean Discharge_Over_Current2 = false; // 放电过流2
    public static boolean Cell_OverVoltage = false;  // 电池过压
    public static boolean Short_Circuit = false;     // 短路
    public static boolean Cell_Low_Voltage = false;  // 电池低压
    public static boolean Discharge_Low_Temp = false; // 放电低温
    public static boolean Charge_Over_Temp = false;  // 充电过温
    public static boolean Charge_Low_Temp = false;   // 充电低温
    public static double MaxTemp;
    public static double MinTemp;
    public static double MaxCellV;
    public static double MinCellV;
    public static double V;
    public static double I;
    public static double soc;
    public static double Mos_Temp;

    private Handler handler = new Handler();
    private Runnable refreshRunnable;
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
        setContentView(R.layout.battery_info);

        ImageView loading = findViewById(R.id.loading_gif);
        loading.setVisibility(View.VISIBLE);
        // 获取传递的 Bundle
        Bundle extras = getIntent().getExtras();

        exception = extras.getString("exception");
        if (exception.equals("no battery table")) {
            loading.setVisibility(View.INVISIBLE);
//            Toast.makeText(this, "此电池暂无数据信息", Toast.LENGTH_LONG).show();

            new AlertDialog.Builder(this)
                    .setTitle("Info")
                    .setMessage("There is no data available for the current battery") // 显示异常信息
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss(); // 关闭弹窗
                            Intent intent = new Intent(BatteryInfoActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent);
                            overridePendingTransition(R.xml.slide_in_left, R.xml.slide_out_right);
                        }
                    })
                    .setCancelable(false) // 防止用户点击外部取消
                    .show();
        }

        FrameLayout backButtonLayout = findViewById(R.id.backButtonLayout);
        backButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BatteryInfoActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(R.xml.slide_in_left, R.xml.slide_out_right);
            }
        });
        FrameLayout mapButtonLayout = findViewById(R.id.mapButtonLayout);
        mapButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建 Intent 跳转至 MapActivity
                Intent intent = new Intent(BatteryInfoActivity.this, MapActivity.class);
                // 将 battery_id 添加到 Intent 的 extras 中
                String batteryId = getIntent().getStringExtra("battery_id");
                intent.putExtra("battery_id", batteryId);
                intent.putExtra("exception", exception);
                startActivity(intent);
                overridePendingTransition(R.xml.slide_in_right, R.xml.slide_out_left);
            }
        });
        FrameLayout cellBatteryInfo = findViewById(R.id.cellBatteryButtonLayout);
        cellBatteryInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BatteryInfoActivity.this, CellBatteryInfoActivity.class);
                String batteryId = getIntent().getStringExtra("battery_id");
                intent.putExtra("battery_id", batteryId);
                intent.putExtra("exception", exception);
                startActivity(intent);
                overridePendingTransition(R.xml.slide_in_right, R.xml.slide_out_left);
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

        // 初始化定时任务
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                // 获取电池ID（假设batteryId是通过Intent传递过来的）
                String batteryId = extras.getString("battery_id");
                // 启动异步任务，获取电池信息
                new LoadBatteryInfoTask().execute(batteryId);
                // 每隔1分钟执行一次任务
                handler.postDelayed(this, 60000);  // 60000ms = 1 minute
            }
        };

        // 启动定时任务，初次延迟 0 秒后开始执行
        handler.post(refreshRunnable);
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 活动销毁时移除定时任务，避免内存泄漏
        handler.removeCallbacks(refreshRunnable);
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
                    // 解析数据
                    Battery_Type = jsonObject.optString("Battery_Type", "");
                    Cell_Number = jsonObject.optString("Cell_Number", "");
                    Ambient_Temp = jsonObject.optString("Ambient_Temp", "");
                    Balance_State = jsonObject.optString("Balance_State", "");
                    Discharge_mos_State = jsonObject.optString("Discharge_mos_State", "");
                    Charge_mos_State = jsonObject.optString("Charge_mos_State", "");
                    Cycle_Number = jsonObject.optString("Cycle_Number", "");

                    // 处理布尔值字段
                    Mos_Over_Temp = jsonObject.optBoolean("Mos_Over_Temp", false);
                    Charge_Over_Current = jsonObject.optBoolean("Charge_Over_Current", false);
                    Discharge_Over_Current1 = jsonObject.optBoolean("Discharge_Over_Current1", false);
                    Discharge_Over_Current2 = jsonObject.optBoolean("Discharge_Over_Current2", false);
                    Cell_OverVoltage = jsonObject.optBoolean("Cell_OverVoltage", false);
                    Short_Circuit = jsonObject.optBoolean("Short_Circuit", false);
                    Cell_Low_Voltage = jsonObject.optBoolean("Cell_Low_Voltage", false);
                    Discharge_Low_Temp = jsonObject.optBoolean("Discharge_Low_Temp", false);
                    Charge_Over_Temp = jsonObject.optBoolean("Charge_Over_Temp", false);
                    Charge_Low_Temp = jsonObject.optBoolean("Charge_Low_Temp", false);

                    Capacity = jsonObject.getJSONObject("battery_last_data").getJSONObject("last_result").getString("capacity");
                    Mos_Temp = jsonObject.getJSONObject("battery_last_data").getJSONObject("last_result").getDouble("temp_mos");
                    V = jsonObject.getJSONObject("battery_last_data").getJSONObject("last_result").getDouble("V");
                    I = jsonObject.getJSONObject("battery_last_data").getJSONObject("last_result").getDouble("I");
                    soc = jsonObject.getJSONObject("battery_last_data").getJSONObject("last_result").getDouble("soc");

                    Online_State = jsonObject.getJSONObject("battery_last_data").getJSONObject("online_state").getInt("online");
                    Create_At = jsonObject.getJSONObject("battery_last_data").getJSONObject("last_result").getString("create_at");

                    // 获取温度值
                    double temp1 = Double.parseDouble(jsonObject.getJSONObject("battery_last_data").getJSONObject("last_result").getString("temp1"));
                    double temp2 = Double.parseDouble(jsonObject.getJSONObject("battery_last_data").getJSONObject("last_result").getString("temp2"));
                    double temp3 = Double.parseDouble(jsonObject.getJSONObject("battery_last_data").getJSONObject("last_result").getString("temp3"));
                    double temp4 = Double.parseDouble(jsonObject.getJSONObject("battery_last_data").getJSONObject("last_result").getString("temp4"));
                    double temp5 = Double.parseDouble(jsonObject.getJSONObject("battery_last_data").getJSONObject("last_result").getString("temp5"));
                    double temp6 = Double.parseDouble(jsonObject.getJSONObject("battery_last_data").getJSONObject("last_result").getString("temp6"));

                    double[] temps = {temp1, temp2, temp3, temp4, temp5, temp6};

                    List<Double> validTemps = new ArrayList<>();
                    for (double temp : temps) {
                        if (temp != -40) {
                            validTemps.add(temp);
                        }
                    }

                    if (!validTemps.isEmpty()) {
                        MaxTemp = Collections.max(validTemps);  // 获取最大值
                        MinTemp = Collections.min(validTemps);  // 获取最小值
                    } else {
                        // 如果所有温度都等于-40，则可以设置默认值或进行特殊处理
                        MaxTemp = MinTemp = -40;
                    }

                    TextView Latest_Update_Text = findViewById(R.id.laest_update);
                    TextView Battery_State = findViewById(R.id.batteryState);
                    TextView V_Text = findViewById(R.id.V);
                    TextView I_Text = findViewById(R.id.I);
                    TextView soc_Text = findViewById(R.id.soc_2);
                    TextView Capacity_Text = findViewById(R.id.capacity);
                    TextView Temp_mos_Text = findViewById(R.id.temp_mos);
                    TextView Battery_Type_Text = findViewById(R.id.Battery_Type);
                    TextView Cell_Number_Text = findViewById(R.id.Cell_Number);
                    TextView Ambient_Temp_Text = findViewById(R.id.Ambient_Temp);
                    TextView Cycle_Number_Text = findViewById(R.id.Cycle_Number);
                    TextView Online_State_Text = findViewById(R.id.online_state);
                    TextView Max_Temp_Text = findViewById(R.id.maxTemp);
                    TextView Min_Temp_Text = findViewById(R.id.minTemp);
                    ImageView Balance_State_Image = findViewById(R.id.Balance_State);
                    ImageView Discharge_mos_State_Image = findViewById(R.id.Discharge_mos_State);
                    ImageView Charge_mos_State_Image = findViewById(R.id.Charge_mos_State);
                    ImageView Charge_Over_Current_Image = findViewById(R.id.Charge_Over_Current);
                    ImageView Discharge_Over_Current1_Image = findViewById(R.id.Discharge_Over_Current1);
                    ImageView Discharge_Over_Current2_Image = findViewById(R.id.Discharge_Over_Current2);
                    ImageView Cell_OverVoltage_Image = findViewById(R.id.Cell_OverVoltage);
                    ImageView Short_Circuit_Image = findViewById(R.id.Short_Circuit);
                    ImageView Cell_Low_Voltage_Image = findViewById(R.id.Cell_Low_Voltage);
                    ImageView Discharge_Low_Temp_Image = findViewById(R.id.Discharge_Low_Temp);
                    ImageView Charge_Over_Temp_Image = findViewById(R.id.Charge_Over_Temp);

                    if(I > 0){
                        Battery_State.setText("Discharging");
                    } else if(I < 0){
                        Battery_State.setText("Charging");
                    } else{
                        Battery_State.setText("Idle");
                    }
                    if(Online_State == 1){
                        Online_State_Text.setText("Online");
                    } else{
                        Online_State_Text.setText("Offline");
                    }
                    Latest_Update_Text.setText(Create_At);
                    V_Text.setText(String.valueOf(V));
                    I_Text.setText(String.valueOf(I));
                    if (soc >= 100) {
                        // 当 soc 为 100 时，直接显示整数
                        soc_Text.setText(String.valueOf((int) soc) + "%");
                    }
//                    else if(soc > 100 || soc < 0) {
//                        soc_Text.setText("ERR");
//                    }
                    else {
                        // 否则保留一位小数
                        soc_Text.setText(String.format("%.1f", soc) + "%");
                    }
                    Capacity_Text.setText(Capacity+"Ah");
                    Temp_mos_Text.setText(String.valueOf(Mos_Temp)+"℃");
                    Battery_Type_Text.setText(Battery_Type);
                    Cell_Number_Text.setText(Cell_Number);
                    Ambient_Temp_Text.setText(String.valueOf(Ambient_Temp)+"℃");
                    Max_Temp_Text.setText(String.valueOf(MaxTemp)+"℃");
                    Min_Temp_Text.setText(String.valueOf(MinTemp)+"℃");
                    Cycle_Number_Text.setText(Cycle_Number);

                    if (Charge_Over_Current) {
                        Charge_Over_Current_Image.setImageResource(R.drawable.square_green);  // 显示图片1
                    } else {
                        Charge_Over_Current_Image.setImageResource(R.drawable.square_gray);  // 显示图片2
                    }
                    if (Discharge_Over_Current1) {
                        Discharge_Over_Current1_Image.setImageResource(R.drawable.square_green);  // 显示图片1
                    } else {
                        Discharge_Over_Current1_Image.setImageResource(R.drawable.square_gray);  // 显示图片2
                    }
                    if (Discharge_Over_Current2) {
                        Discharge_Over_Current2_Image.setImageResource(R.drawable.square_green);  // 显示图片1
                    } else {
                        Discharge_Over_Current2_Image.setImageResource(R.drawable.square_gray);  // 显示图片2
                    }
                    if (Cell_OverVoltage) {
                        Cell_OverVoltage_Image.setImageResource(R.drawable.square_green);  // 显示图片1
                    } else {
                        Cell_OverVoltage_Image.setImageResource(R.drawable.square_gray);  // 显示图片2
                    }
                    if (Short_Circuit) {
                        Short_Circuit_Image.setImageResource(R.drawable.square_green);  // 显示图片1
                    } else {
                        Short_Circuit_Image.setImageResource(R.drawable.square_gray);  // 显示图片2
                    }
                    if (Cell_Low_Voltage) {
                        Cell_Low_Voltage_Image.setImageResource(R.drawable.square_green);  // 显示图片1
                    } else {
                        Cell_Low_Voltage_Image.setImageResource(R.drawable.square_gray);  // 显示图片2
                    }
                    if (Discharge_Low_Temp) {
                        Discharge_Low_Temp_Image.setImageResource(R.drawable.square_green);  // 显示图片1
                    } else {
                        Discharge_Low_Temp_Image.setImageResource(R.drawable.square_gray);  // 显示图片2
                    }
                    if (Charge_Over_Temp) {
                        Charge_Over_Temp_Image.setImageResource(R.drawable.square_green);  // 显示图片1
                    } else {
                        Charge_Over_Temp_Image.setImageResource(R.drawable.square_gray);  // 显示图片2
                    }
                    if ("开".equals(Balance_State)) {
                        Balance_State_Image.setImageResource(R.drawable.square_green);  // 显示图片1
                    } else if ("关".equals(Balance_State)) {
                        Balance_State_Image.setImageResource(R.drawable.square_gray);  // 显示图片2
                    }
                    if ("开".equals(Discharge_mos_State)) {
                        Discharge_mos_State_Image.setImageResource(R.drawable.square_green);  // 显示图片1
                    } else if ("关".equals(Discharge_mos_State)) {
                        Discharge_mos_State_Image.setImageResource(R.drawable.square_gray);  // 显示图片2
                    }
                    if ("开".equals(Charge_mos_State)) {
                        Charge_mos_State_Image.setImageResource(R.drawable.square_green);  // 显示图片1
                    } else if ("关".equals(Charge_mos_State)) {
                        Charge_mos_State_Image.setImageResource(R.drawable.square_gray);  // 显示图片2
                    }
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
