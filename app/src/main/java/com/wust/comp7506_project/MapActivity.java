package com.wust.comp7506_project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
//import com.google.android.gms.maps.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MapActivity extends AppCompatActivity {
    public static String exception;
    private MapView mapView;
    private BaiduMap baiduMap;
    private View loadingGif;
    private Polyline currentPolyline;

    private TextView nearestMileageTextView;
    private TextView past7DaysMileageTextView;
    private TextView totalMileageTextView;

    public static String Create_At;

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
        SDKInitializer.setAgreePrivacy(getApplicationContext(), true);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.map);
        baiduMap = mapView.getMap();

        // 获取 loading_gif 的引用
        loadingGif = findViewById(R.id.loading_gif);
        // 设置初始状态为可见
        loadingGif.setVisibility(View.VISIBLE);

        Bundle extras = getIntent().getExtras();
        exception = extras.getString("exception");
        if (exception.equals("no battery table")) {
            loadingGif.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "There is no data available for the current battery", Toast.LENGTH_LONG).show();
        }

        // 获取传递的 batteryId
        String batteryId = getIntent().getStringExtra("battery_id");
        TextView mapBatteryId = findViewById(R.id.map_battery_id);
        mapBatteryId.setText(batteryId);
        // 获取当前时间
        Calendar calendar = Calendar.getInstance();
        // 获取当前小时（24小时制）
        int numberOfData = calendar.get(Calendar.HOUR_OF_DAY);
        // 调用方法获取 GPS 数据
        getGPSData(batteryId, numberOfData);

        ConstraintLayout constraintLayout12 = findViewById(R.id.constraintLayout12);
        LinearLayout expandableContent = findViewById(R.id.expandableContent);

        constraintLayout12.setOnClickListener(new View.OnClickListener() {
            private boolean isExpanded = false;
            ImageView flodState = findViewById(R.id.flodState);

            @Override
            public void onClick(View v) {
                if (isExpanded) {
                    expandableContent.setVisibility(View.GONE);
                    flodState.setImageResource(R.drawable.up_arrow);
                } else {
                    expandableContent.setVisibility(View.VISIBLE);
                    flodState.setImageResource(R.drawable.down_arrow);
                }
                isExpanded = !isExpanded;
            }
        });

        Button togglePolylineButton = findViewById(R.id.toggle_polyline_button); // 替换为你的按钮 ID
        togglePolylineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPolyline != null) {
                    boolean isVisible = currentPolyline.isVisible(); // 获取当前状态
                    currentPolyline.setVisible(!isVisible); // 切换显示/隐藏状态
                    if (!isVisible) {
                        // 显示轨迹时放大地图
                        baiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(20));
                        nearestMileageTextView.setVisibility(View.VISIBLE);
                        past7DaysMileageTextView.setVisibility(View.VISIBLE);
                        totalMileageTextView.setVisibility(View.VISIBLE);
                    } else {
                        // 隐藏轨迹时缩小地图
                        baiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(10)); // 恢复到默认缩放级别
                        nearestMileageTextView.setVisibility(View.GONE);
                        past7DaysMileageTextView.setVisibility(View.GONE);
                        totalMileageTextView.setVisibility(View.GONE);
                    }
                }
            }
        });

        // 初始化返回按钮
        FrameLayout backButtonLayout = findViewById(R.id.backButtonLayout);
        backButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回到上一个页面
                Intent intent = new Intent(MapActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(R.xml.slide_in_left, R.xml.slide_out_right);
                finish();
            }
        });
        FrameLayout cellBatteryInfo = findViewById(R.id.cellBatteryButtonLayout);
        cellBatteryInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, CellBatteryInfoActivity.class);
                String batteryId = getIntent().getStringExtra("battery_id");
                intent.putExtra("battery_id", batteryId);
                intent.putExtra("exception", exception);
                startActivity(intent);
                overridePendingTransition(R.xml.slide_in_left, R.xml.slide_out_right);
            }
        });
        FrameLayout wholeBatteryInfo = findViewById(R.id.wholeBatteryButtonLayout);
        wholeBatteryInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, BatteryInfoActivity.class);
                String batteryId = getIntent().getStringExtra("battery_id");
                intent.putExtra("battery_id", batteryId);
                intent.putExtra("exception", exception);
                startActivity(intent);
                overridePendingTransition(R.xml.slide_in_left, R.xml.slide_out_right);
                finish();
            }
        });
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private Bitmap resizeBitmap(int resourceId, int width, int height) {
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false);
    }

    // 创建自定义视图方法
    private View createCustomView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setBackgroundColor(Color.WHITE); // 设置背景颜色
        textView.setPadding(10, 10, 10, 10); // 设置内边距
        textView.setTextColor(Color.BLACK); // 设置文本颜色
        return textView;
    }

    private void getGPSData(String batteryId, int numberOfData) {
        String url = "http://112.74.94.253/api/last_n_row_gps_location_data/" + batteryId + "/" + 35000;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        List<LatLng> points = new ArrayList<>();
                        LatLng endPoint = null; // 用于存储终点
                        LatLng startPointNearest = null; // 用于存储起点
                        LatLng endPointNearest = null; // 用于存储终点
                        String currentDate = null;
                        String startOfDay = null;

                        try {
                            // 获取数据的第一个记录
                            JSONObject record = response.getJSONObject(0);
                            Create_At = record.getString("create_at");
                            TextView Latest_Update_Text = findViewById(R.id.Latest_Update);
                            Latest_Update_Text.setText(Create_At);

                            // 处理每一条记录
                            for (int i = 0; i < response.length(); i++) {
                                record = response.getJSONObject(i);
                                String recordTime = record.getString("create_at");

                                // 提取日期部分和小时部分
                                String datePart = recordTime.split(" ")[0];

                                // 初始化日期和起始时间
                                if (currentDate == null) {
                                    currentDate = datePart;
                                    startOfDay = currentDate + " 00:00:00"; // 每天的起始时间
                                }

                                if (isSameDay(recordTime, startOfDay, recordTime)) {
                                    double latitude = record.getDouble("Latitude");
                                    double longitude = record.getDouble("Longitude");

                                    LatLng point = new LatLng(latitude, longitude);
                                    points.add(point);

                                    if (endPointNearest == null) {
                                        endPointNearest = point;
                                    }

                                    startPointNearest = point;

                                    // 通过高德经纬度获取地址
                                    if (i == 0) {
                                        // 获取高德经纬度
                                        double gaodeLatitude = record.getJSONObject("gaode").getDouble("Latitude");
                                        double gaodeLongitude = record.getJSONObject("gaode").getDouble("Longitude");
                                        fetchAddressFromGaode(gaodeLatitude, gaodeLongitude);
                                    }
                                }
                            }

                            // 绘制起点和终点图标，并设置不同颜色和说明
//                            if (startPointNearest != null) {
//                                Bitmap startBitmap = resizeBitmap(R.drawable.map_marker_green, 150, 150); // 调整为合适的宽高
//                                MarkerOptions startMarker = new MarkerOptions()
//                                        .position(startPointNearest)
//                                        .icon(BitmapDescriptorFactory.fromBitmap(startBitmap)); // 使用缩放后的图标
//                                baiduMap.addOverlay(startMarker);
//
//                                // 创建并显示 InfoWindow
//                                View startInfoWindowView = createCustomView("起始位置");
//                                InfoWindow startInfoWindow = new InfoWindow(startInfoWindowView, startPointNearest, -50); // Y轴偏移量
//                                baiduMap.showInfoWindow(startInfoWindow);
//                            }

                            // 绘制轨迹
                            if (!points.isEmpty()) {
                                // 加载箭头纹理
                                BitmapDescriptor arrowTexture = BitmapDescriptorFactory.fromResource(R.drawable.next);

                                // 创建折线选项，设置自定义纹理
                                PolylineOptions polylineOptions = new PolylineOptions()
                                        .points(points)
                                        .width(20) // 线宽
                                        .customTexture(arrowTexture) // 设置箭头纹理
                                        .dottedLine(false); // 关闭虚线模式，确保箭头清晰

                                List<BitmapDescriptor> textureList = new ArrayList<>();
                                textureList.add(arrowTexture); // 多次添加，形成均匀的箭头效果
                                polylineOptions.customTextureList(textureList);

                                // 添加到地图
                                currentPolyline = (Polyline) baiduMap.addOverlay(polylineOptions);
                                currentPolyline.setVisible(false); // 初始状态隐藏

                                // 设置合适的缩放
                                baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(
                                        endPoint != null ? endPointNearest : startPointNearest, 10));
                            } else {
                                Toast.makeText(MapActivity.this, "No data found for today", Toast.LENGTH_SHORT).show();
                            }

                            if (endPointNearest != null) {
                                Bitmap endBitmap = resizeBitmap(R.drawable.map_marker, 150, 150); // 调整为合适的宽高
                                MarkerOptions endMarker = new MarkerOptions()
                                        .position(endPointNearest)
                                        .icon(BitmapDescriptorFactory.fromBitmap(endBitmap)); // 使用缩放后的图标
                                baiduMap.addOverlay(endMarker);

                                // 创建并显示 InfoWindow
//                                View endInfoWindowView = createCustomView("最终位置");
//                                InfoWindow endInfoWindow = new InfoWindow(endInfoWindowView, endPointNearest, -0); // Y轴偏移量
//                                baiduMap.showInfoWindow(endInfoWindow);
                            }

                        } catch (JSONException e) {
//                            Log.e("MapActivity", "JSON parsing error: " + e.getMessage());
                        }

                        calculateLast7DaysDistance(batteryId);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        Toast.makeText(MapActivity.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//                        Log.e("MapActivity", "Volley error: " + error.getMessage());
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(MapActivity.this);
        requestQueue.add(jsonArrayRequest);
    }

    public void calculateLast7DaysDistance(String batteryId) {
        String url = "http://112.74.94.253/api/get_battery_travel_distance/" + batteryId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.has("error")) {
                                // 处理 API 返回的错误信息
                                Toast.makeText(MapActivity.this, "Error: " + response.getString("error"), Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // 解析 JSON 数据
                            String lastDate = response.getString("last_date_data").split(" ")[0]; // 提取日期部分
                            double lastMileage = response.getDouble("last_day_distance");
                            double totalMileage = response.getDouble("total_distance");

                            StringBuilder past7DaysMileage = new StringBuilder();
                            for (int i = 7; i >= 1; i--) {
                                String dateKey = i + "_days_date";
                                String distanceKey = i + "_days_ago";

                                if (response.has(dateKey) && response.has(distanceKey)) {
                                    String date = response.getString(dateKey).split(" ")[0];
                                    double distance = response.getDouble(distanceKey);
                                    // 格式化 distance 为保留两位小数
                                    past7DaysMileage.append(date).append("：").append(String.format("%.2f", distance)).append(" km\n");
                                }
                            }

                            // 更新 UI
                            nearestMileageTextView = findViewById(R.id.nearest_mileage);
                            past7DaysMileageTextView = findViewById(R.id.past_7_days_mileage);
                            totalMileageTextView = findViewById(R.id.total_mileage);

                            nearestMileageTextView.setText("Last day's mileage: " + String.format("%.2f", lastMileage) + " km");
                            past7DaysMileageTextView.setText(past7DaysMileage.toString());
                            totalMileageTextView.setText("Total mileage: " + String.format("%.2f", totalMileage) + " km");

                            nearestMileageTextView.setVisibility(View.GONE);
                            past7DaysMileageTextView.setVisibility(View.GONE);
                            totalMileageTextView.setVisibility(View.GONE);

                        } catch (JSONException e) {
//                            Toast.makeText(MapActivity.this, "JSON 解析错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
//                            Log.e("MapActivity", "JSON parsing error: ", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                // 解析错误响应
                                String errorResponse = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                JSONObject errorJson = new JSONObject(errorResponse);

                                if (errorJson.has("error")) {
                                    Toast.makeText(MapActivity.this, "Error: " + errorJson.getString("error"), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } catch (JSONException e) {
//                                Log.e("MapActivity", "Error parsing error response", e);
                            }
                        }

//                        Toast.makeText(MapActivity.this, "请求失败：" + error.toString(), Toast.LENGTH_SHORT).show();
//                        Log.e("MapActivity", "Volley error: ", error);
                    }
                });

        loadingGif.setVisibility(View.INVISIBLE);
        // 添加请求到 Volley 请求队列
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    // 获取高德地图的地址信息
    private void fetchAddressFromGaode(double latitude, double longitude) {
        String gaodeUrl = "https://restapi.amap.com/v3/geocode/regeo?output=json&location=" + longitude + "," + latitude + "&key=4fd4f1c770ac13b65daf92c695b14542";

        JsonObjectRequest gaodeRequest = new JsonObjectRequest(Request.Method.GET, gaodeUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // 获取返回的地址信息
                    if (response != null && response.has("regeocode")) {
                        JSONObject regeocode = response.getJSONObject("regeocode");
                        if (regeocode != null && regeocode.has("formatted_address")) {
                            String formattedAddress = regeocode.getString("formatted_address");
                            // 显示或处理地址信息
                            Log.d("Gaode", "Formatted Address: " + formattedAddress);

                            TextView address = findViewById(R.id.address);
                            address.setText("Location: " + formattedAddress);
//                            // 在地图上显示地址信息
//                            View addressInfoWindowView = createCustomView("最后位置: " + formattedAddress);
//                            InfoWindow addressInfoWindow = new InfoWindow(addressInfoWindowView, new LatLng(latitude, longitude), -50);
//                            baiduMap.showInfoWindow(addressInfoWindow);
                        }
                    }
                } catch (JSONException e) {
//                    Log.e("Gaode", "Error parsing address response: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.e("Gaode", "Error fetching address: " + error.getMessage());
            }
        });

        // 请求队列添加
        RequestQueue requestQueue = Volley.newRequestQueue(MapActivity.this);
        requestQueue.add(gaodeRequest);
    }

    private boolean isSameDay(String recordTime, String startOfDay, String datetimeStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date recordDate = sdf.parse(recordTime);
            Date startDate = sdf.parse(startOfDay);
            Date currentDate = sdf.parse(datetimeStr);

            return !recordDate.before(startDate) && !recordDate.after(currentDate);
        } catch (ParseException e) {
//            Log.e("MapActivity", "Date parsing error: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放地图资源
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 恢复地图状态
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 暂停地图状态
        mapView.onPause();
    }
}
