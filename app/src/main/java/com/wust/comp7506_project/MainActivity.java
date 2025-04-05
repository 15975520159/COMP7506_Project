package com.wust.comp7506_project;

import static com.wust.comp7506_project.LoginActivity.KEY_IS_LOGGED_IN;
import static com.wust.comp7506_project.LoginActivity.SHARED_PREF_NAME;
import static com.wust.comp7506_project.LoginActivity.KEY_IS_LOGGED_IN;
import static com.wust.comp7506_project.LoginActivity.SHARED_PREF_NAME;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements CardListAdapter.OnItemClickListener {
    protected String User;
    private CardListAdapter cardListAdapter;
    private RecyclerView cardListRecyclerView;
    private View loadingGif; // 添加 loading_gif 组件

    //读取超时为60s
    private static final long READ_TIMEOUT = 60000;
    //写入超时为60s
    private static final long WRITE_TIMEOUT = 60000;
    //连接超时为60s
    private static final long CONNECT_TIMEOUT = 60000;
    List<CardListContactInfo> cardListContactInfoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 获取当前的 Window 对象
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

        // 获取当前窗口的 Insets
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取RecyclerView的引用，并对其进行设置
        cardListRecyclerView = findViewById(R.id.card_list);
        cardListRecyclerView.setHasFixedSize(true);

        // 创建LinearLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // 为RecyclerView对象指定创建的layoutManager
        cardListRecyclerView.setLayoutManager(layoutManager);

        cardListAdapter = new CardListAdapter(cardListContactInfoList);
        // 设置点击事件监听器
        cardListAdapter.setOnItemClickListener(this);

        cardListRecyclerView.setAdapter(cardListAdapter);

        // 获取启动该 Activity 的 Intent
        Intent intent = getIntent();
        int userId = intent.getIntExtra("USER_ID", 0);
        String userName = intent.getStringExtra("USER_NAME");
        String companyName = intent.getStringExtra("COMPANY_NAME");
        TextView userInfo = findViewById(R.id.userInfo);
        userInfo.setText(userName);
        TextView noBatteryInfoText = findViewById(R.id.no_battery_info_text);
        noBatteryInfoText.setVisibility(View.INVISIBLE);
        User = userName;

        loadInfo(userId,noBatteryInfoText);
        saveUserInfo(userId,User);
    }

    // 获取状态栏的高度
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void saveUserInfo(int userId, String userName) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("USER_ID", userId);
        editor.putString("USER_NAME", userName);
        editor.apply();
    }

    // 从SharedPreferences中获取保存的用户信息
    private UserInfo getUserInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("USER_ID", 0);
        String userName = sharedPreferences.getString("USER_NAME", "");
        return new UserInfo(userId, userName);
    }

    private void initInfo(String location, String batteryId, String lastSync, String warning, double soc, String state, double temp, double I, double V, String exception) {
        CardListContactInfo element = new CardListContactInfo(location, batteryId, lastSync, warning,soc, state,temp, Math.abs(I), V, exception);
        cardListContactInfoList.add(element);
    }

    private void loadInfo(int userId,TextView noBatteryInfoText){
        // 获取 loading_gif 的引用
        loadingGif = findViewById(R.id.loading_gif);
        // 设置初始状态为可见
        loadingGif.setVisibility(View.VISIBLE);

        // 构建请求
//        String url = "http://www.iotlab.one/api/get_user_bind_battery_APP_list/" + userId;
        String url = "http://www.dgthreeteam.com/api/get_user_bind_battery_APP_list/" + userId;
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();
// 发起请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                // 请求失败时隐藏 loading_gif，并显示 no_battery_info_text
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingGif.setVisibility(View.INVISIBLE);
                        noBatteryInfoText.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        // 解析数据并更新UI
                        JSONObject jsonResponse = new JSONObject(responseData);
                        if (jsonResponse.length() == 0) {
                            // 如果结果为空，显示 no_battery_info_text
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    noBatteryInfoText.setVisibility(View.VISIBLE);
                                    loadingGif.setVisibility(View.INVISIBLE);
                                }
                            });
                            return;
                        }
                        Iterator<String> keys = jsonResponse.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            JSONObject batteryInfo = jsonResponse.getJSONObject(key);
                            JSONObject battery = batteryInfo.getJSONObject("battery");

                            String batteryId = battery.getJSONObject("original").getJSONObject("battery_pool_info").getString("battery_id");
                            double soc = battery.getJSONObject("original").getJSONObject("last_result").getDouble("soc");
                            String cityName = batteryInfo.getString("city_name");
                            double V = battery.getJSONObject("original").getJSONObject("last_result").getDouble("V");
                            double I = battery.getJSONObject("original").getJSONObject("last_result").getDouble("I");
                            double maxTemp = Math.max(battery.getJSONObject("original").getJSONObject("last_result").getDouble("temp1"),
                                    Math.max(battery.getJSONObject("original").getJSONObject("last_result").getDouble("temp2"),
                                            Math.max(battery.getJSONObject("original").getJSONObject("last_result").getDouble("temp3"),
                                                    Math.max(battery.getJSONObject("original").getJSONObject("last_result").getDouble("temp4"),
                                                            Math.max(battery.getJSONObject("original").getJSONObject("last_result").getDouble("temp5"),
                                                                    Math.max(battery.getJSONObject("original").getJSONObject("last_result").getDouble("temp6"), battery.getJSONObject("original").getJSONObject("last_result").getDouble("temp_mos")))))));
                            String lastSync = battery.getJSONObject("original").getJSONObject("last_result").getString("create_at");
                            String state;
                            String exception = battery.getString("exception");

                            if(I > 0){
                                state = "放电中";
                            } else if(I == 0){
                                state = "静置中";
                            } else{
                                state = "充电中";
                            }

                            String warning = getWarningMeaning(battery); // 获取警告信息
                            initInfo(cityName, batteryId, lastSync, warning, soc, state, maxTemp, I, V, exception);
                        }
                        // 在主线程中设置适配器
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 设置适配器
                                cardListAdapter.notifyDataSetChanged();
                                // 请求完成后隐藏 loading_gif
                                loadingGif.setVisibility(View.INVISIBLE);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // 解析出错时隐藏 loading_gif，并显示 no_battery_info_text
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingGif.setVisibility(View.INVISIBLE);
                                noBatteryInfoText.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } else {
                    // 请求失败时隐藏 loading_gif，并显示 no_battery_info_text
                    Log.e("API Error", "Request failed");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingGif.setVisibility(View.INVISIBLE);
                            noBatteryInfoText.setVisibility(View.VISIBLE);
                        }
                    });
                }

            }
        });

        // 找到logout按钮
        FrameLayout logoutButton = findViewById(R.id.logout);

// 设置点击事件监听器
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 清除登录状态
//                clearLoginState();

                // 返回到LoginActivity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(R.xml.slide_in_left, R.xml.slide_out_right);
                finish();
            }
        });

        // 找到 refresh 按钮
        FrameLayout refreshButton = findViewById(R.id.refresh);

        // 设置点击事件监听器
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 重新加载 MainActivity
                reloadMainActivity();
            }
        });
    }
    // 实现点击事件接口
    @Override
    public void onItemClick(int position) {
        // 获取点击的 CardListContactInfo 对象
        CardListContactInfo clickedItem = cardListContactInfoList.get(position);

        // 创建 Intent 并启动 BatteryInfoActivity
        Intent intent = new Intent(MainActivity.this, BatteryInfoActivity.class);
        // 创建一个 Bundle 对象，并将所需的数据放入其中
        Bundle extras = new Bundle();
        extras.putString("user",User);
        extras.putString("exception", clickedItem.exception);
        extras.putString("battery_location", clickedItem.battery_location);
        extras.putString("battery_id", clickedItem.battery_id);
        extras.putString("last_sync", clickedItem.last_sync);
        extras.putString("warning", clickedItem.warning);
        extras.putDouble("soc", clickedItem.soc);
        extras.putString("state", clickedItem.state);
        extras.putDouble("temperature", clickedItem.temperature);
        extras.putDouble("electricity", clickedItem.electricity);
        extras.putDouble("voltage", clickedItem.voltage);
        // 将 Bundle 放入 Intent 中
        intent.putExtras(extras);

        // 启动 BatteryInfoActivity
        startActivity(intent);
        overridePendingTransition(R.xml.slide_in_right, R.xml.slide_out_left);
    }

    private String getWarningMeaning(JSONObject battery) {
        Map<String, String> warnings = new LinkedHashMap<>();
        warnings.put("wCellOverVoltage", "单体过压预警");
        warnings.put("wCellLowVoltage", "单体欠压预警");
        warnings.put("wChargeOverCurrent", "充电过流预警");
        warnings.put("wDischargeOverCurrent", "放电过流预警");
        warnings.put("wChargeOverTemp", "充电过温预警");
        warnings.put("wChargeLowTemp", "充电欠温预警");
        warnings.put("wDischargeOverTemp", "放电过温预警");
        warnings.put("wDischargeLowTemp", "放电欠温预警");
        warnings.put("wOverMOSTemp", "MOS过温预警");
        warnings.put("wLowSOC", "SOC过低预警");
        warnings.put("wOverVoltageDiff", "压差过大预警");
        warnings.put("wOverAmbienceTemp", "环境高温预警");
        warnings.put("wLowAmbienceTemp", "环境低温预警");
        warnings.put("wReserve1", "预留告警1");
        warnings.put("wReserve2", "预留告警2");
        warnings.put("wReserve3", "预留告警3");
        warnings.put("wReserve4", "预留告警4");
        warnings.put("wReserve5", "预留告警5");
        warnings.put("wReserve6", "预留告警6");
        warnings.put("wReserve7", "预留告警7");
        warnings.put("wReserve8", "预留告警8");
        warnings.put("wReserve9", "预留告警9");
        warnings.put("wReserve10", "预留告警10");
        warnings.put("wReserve11", "预留告警11");
        warnings.put("pCellOverVoltage", "单体过压保护");
        warnings.put("pSecondOverVoltage", "二次过压保护");
        warnings.put("pCellLowVoltage", "单体欠压保护");
        warnings.put("pChargeOverCurrent", "充电过流保护");
        warnings.put("pSecondOverCurrent", "二次过流保护");
        warnings.put("pDischargeOverCurrent1", "放电过流保护1");
        warnings.put("pDischargeOverCurrent2", "放电过流保护2");
        warnings.put("pChargeOverTemp", "充电过温保护");
        warnings.put("pChargeLowTemp", "充电欠温保护");
        warnings.put("pDischargeOverTemp", "放电过温保护");
        warnings.put("pDischargeLowTemp", "放电欠温保护");
        warnings.put("pOverMOSTemp", "过温保护");
        warnings.put("pOverVoltageDiff", "压差过大保护");
        warnings.put("pShortCircuit", "短路保护");
        warnings.put("pReserve1", "预留保护1");
        warnings.put("pReserve2", "预留保护2");
        warnings.put("pReserve3", "预留保护3");
        warnings.put("pReserve4", "预留保护4");
        warnings.put("pReserve5", "预留保护5");
        warnings.put("pReserve6", "预留保护6");
        warnings.put("pReserve7", "预留保护7");
        warnings.put("pReserve8", "预留保护8");
        warnings.put("pReserve9", "预留保护9");
        warnings.put("pReserve10", "预留保护10");

        Iterator<String> keys = warnings.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                int value = battery.getJSONObject("original").getJSONObject("last_result").optInt(key, 0);
                if (value == 1) {
                    return warnings.get(key);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "暂无警告";
    }

    // 清除登录状态
    private void clearLoginState() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove("USER_EMAIL");
        editor.remove("USER_PASSWORD");
        editor.apply();
    }

    // 重新加载 MainActivity
    private void reloadMainActivity() {
        // 清除 RecyclerView 中的内容
        cardListContactInfoList.clear();
        cardListAdapter.notifyDataSetChanged();
        TextView noBatteryInfoText = findViewById(R.id.no_battery_info_text);
        noBatteryInfoText.setVisibility(View.INVISIBLE);
        // 获取用户信息
        UserInfo userInfo = getUserInfo();

        // 如果 userId 不为 0，则重新加载数据
        if (userInfo.getUserId() != 0) {
            loadInfo(userInfo.getUserId(),noBatteryInfoText);
        }
    }

}