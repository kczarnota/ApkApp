package com.example.konrad.indoorwayhackathon.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.konrad.indoorwayhackathon.Utils;
import com.example.konrad.indoorwayhackathon.net.login.Api;
import com.example.konrad.indoorwayhackathon.net.login.ApiService;
import com.example.konrad.indoorwayhackathon.net.login.LoginResponse;
import com.example.konrad.indoorwayhackathon.R;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    public static final String TAG = LoginActivity.class.getSimpleName();

    @BindView(R.id.loginField)
    TextView loginField;
    @BindView(R.id.passwordField)
    TextView passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    public void doLogin(View view) throws IOException {
        String loginName = loginField.getText().toString();
        String password = passwordField.getText().toString();

        ApiService service = Api.getApi();

        Call<LoginResponse> response = service.doLogin("password", loginName, password);
        response.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                Log.d(Utils.getTag(TAG), "onResponse: " + response.body().getAccessToken());
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra(Utils.TOKEN_KEY, response.body().getAccessToken());
                startActivity(intent);
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
            }
        });
    }
}
