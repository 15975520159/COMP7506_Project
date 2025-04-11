package com.wust.comp7506_project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    // 声明SharedPreferences文件名
    public static final String SHARED_PREF_NAME = "login_preferences";
    // 声明SharedPreferences中保存登录状态的键名
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";

    public String user;
    public String password;

    private EditText userEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private ImageView loadingGif; // 添加 loading_gif 组件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            // 使状态栏透明
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 设置导航栏颜色为指定颜色
            window.setNavigationBarColor(Color.parseColor("#F3F3F3"));
            // 让内容延伸至状态栏下方
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化视图
        userEditText = findViewById(R.id.editTextUser);
        passwordEditText = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);
        loadingGif = findViewById(R.id.loading_gif); // 初始化 loading_gif 组件

        // 初始时隐藏 loading_gif
        loadingGif.setVisibility(View.INVISIBLE);

        // 检查是否已经登录过
//        if (isLoggedIn()) {
//            // 如果已经登录过，则直接跳转到 MainActivity
//            goToMainActivity();
//        }

        // 设置点击事件监听器
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取输入的邮箱和密码
                user = userEditText.getText().toString().trim();
                password = passwordEditText.getText().toString().trim();

                // 保存输入的登录信息到SharedPreferences
                saveLoginInfo(user, password);

                // 在登录按钮点击事件中保存登录状态
                saveLoginState(true);
                goToMainActivity();
            }
        });

        // 填充输入框中的内容
        fillLoginInfo();
    }

    // 保存登录状态到SharedPreferences
    private void saveLoginState(boolean isLoggedIn) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    // 检查登录状态
    private boolean isLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // 保存登录信息到SharedPreferences
    private void saveLoginInfo(String user, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USER_EMAIL", user);
        editor.putString("USER_PASSWORD", password);
        editor.apply();
    }

    // 填充输入框中的内容
    private void fillLoginInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("USER_EMAIL", "");
        String userPassword = sharedPreferences.getString("USER_PASSWORD", "");

        userEditText.setText(userEmail);
        passwordEditText.setText(userPassword);
    }

    // 跳转到MainActivity
    private void goToMainActivity() {
        // 编写跳转到MainActivity的代码，包括传递登录信息
        // 显示 loading_gif
        loadingGif.setVisibility(View.VISIBLE);
        fillLoginInfo();

        // 获取填充后的用户和密码
        String user = userEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        // 构建请求
//        String url = "http://www.iotlab.one/api/verify-login?email=" + user + "&password=" + password + "&id=login";
        String url = "http://www.dgthreeteam.com/api/verify-login?email=" + user + "&password=" + password + "&id=login";
        Request request = new Request.Builder()
                .url(url)
                .build();

        OkHttpClient client = new OkHttpClient();
        // 发起请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 隐藏 loading_gif
                loadingGif.setVisibility(View.INVISIBLE);

                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        boolean status = jsonResponse.getBoolean("status");
                        if (status) {
                            // 解析数据
                            int userId = jsonResponse.getInt("user_id");
                            String userName = jsonResponse.getString("user_name");
                            String companyName = jsonResponse.getString("company_name");

                            // 启动 MainActivity，并传递数据
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("USER_ID", userId);
                            intent.putExtra("USER_NAME", userName);
                            intent.putExtra("COMPANY_NAME", companyName);
                            startActivity(intent);
                            overridePendingTransition(R.xml.slide_in_right, R.xml.slide_out_left);
                        } else {
                            // 弹窗提示用户名或密码错误
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, "Incorrect Usr Name or Pwd", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // 请求失败
                    Log.e("API Error", "Request failed");
                }
            }
        });
    }
}
