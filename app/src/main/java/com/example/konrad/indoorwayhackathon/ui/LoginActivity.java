package com.example.konrad.indoorwayhackathon.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.konrad.indoorwayhackathon.R;
import com.example.konrad.indoorwayhackathon.Utils;
import com.example.konrad.indoorwayhackathon.net.Api;
import com.example.konrad.indoorwayhackathon.net.ApiService;
import com.example.konrad.indoorwayhackathon.model.LoginResponse;
import com.indoorway.android.common.sdk.IndoorwaySdk;
import com.indoorway.android.common.sdk.model.Sex;
import com.indoorway.android.common.sdk.model.Visitor;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity
{
    public static final String TAG = LoginActivity.class.getSimpleName();

    @BindView(R.id.loginField)
    TextView loginField;
    @BindView(R.id.passwordField)
    TextView passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        setTitle("Login");
    }

    public void doLogin(final View view) throws IOException
    {
        final String loginName = loginField.getText().toString();
        String password = passwordField.getText().toString();

        ApiService service = Api.getApi();

        Call<LoginResponse> response = service.doLogin("password", loginName, password);
        response.enqueue(new Callback<LoginResponse>()
        {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response)
            {
                Visitor visitor = new Visitor();
                visitor.setName(loginName);
                visitor.setAge(60);
                visitor.setSex(Sex.MALE);
                visitor.setShareLocation(true);
                IndoorwaySdk.instance().visitor().setup(visitor);
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                Utils.setToken(response.body().getAccessToken());
                startActivity(intent);
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t)
            {
            }
        });
    }
}
