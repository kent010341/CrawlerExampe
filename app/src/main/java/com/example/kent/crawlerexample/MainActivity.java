package com.example.kent.crawlerexample;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private EditText ed_year, ed_month;
    private Button btn_submit;
    private TextView tv_show;

    private getWeb getW;
    private Handler handler;
    private String sourse_code;
    private String specialPrize;
    private String grandPrize;
    private String firstPrize;
    private String addSixPrize;

    private int year, month, date, hour;
    private int chosen_year, chosen_month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ed_year = (EditText)findViewById(R.id.ed_year);
        ed_month = (EditText)findViewById(R.id.ed_month);
        btn_submit = (Button)findViewById(R.id.btn_submit);
        tv_show = (TextView)findViewById(R.id.tv_show);

        getW = new getWeb();

        Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        date = c.get(Calendar.DAY_OF_MONTH);
        hour = c.get(Calendar.HOUR);
        if(c.get(Calendar.AM_PM) == 1)
            hour += 12;

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    chosen_year = Integer.parseInt(ed_year.getText().toString());
                    chosen_month = Integer.parseInt(ed_month.getText().toString());

                    if(isReleased() && chosen_month <= 12){
                        String web = "http://service.etax.nat.gov.tw/etw-main/web/ETW183W2_";
                        web += chosen_year;
                        if(chosen_month != 11)
                            web += "0" + chosen_month;
                        else
                            web += chosen_month;

                        handler = new Handler(){
                            @Override
                            public void handleMessage(Message msg){
                                switch (msg.what){
                                    case 1:
                                        sourse_code = getW.getWebData();
                                        parser();
                                        tv_show.setText(specialPrize + "\n" + grandPrize + "\n" + firstPrize + "\n" + addSixPrize);
                                    break;
                                }
                                super.handleMessage(msg);
                            }
                        };
                        getW.handleURLdata(web, handler);
                    }

                }catch (NumberFormatException e){}
            }
        });
    }

    private boolean isReleased(){
        boolean condition = false;
        int odded_month = month - (month+1)%2;
        int temp_month = odded_month + (year - chosen_year) * 12;

        if(chosen_year > 101){
            if(chosen_month < temp_month - 2){
                condition = true;
            }
            else if(chosen_month == temp_month - 2){
                if(month % 2 == 1){
                    if(date > 25){
                        condition = true;
                    }
                    else if(date == 25 && hour >= 14){
                        condition = true;
                    }
                }
                else{
                    condition = true;
                }
            }
        }
        else if(chosen_year == 101){
            if(chosen_month >= 5){
                condition = true;
            }
            else{
                System.out.println("under 101/5");
            }
        }
        else{
            System.out.println("under 101/5");
        }

        return condition;
    }

    private void parser(){
        String temp = null;

        try {
            temp = sourse_code.substring(
                    sourse_code.indexOf("<th id=\"specialPrize\" rowspan=\"2\">特別獎</th> ")
            );
            Log.i("Parser", temp);
            specialPrize = temp.substring(
                    temp.indexOf("<td headers=\"specialPrize\" class=\"number\"> ") + "<td headers=\"specialPrize\" class=\"number\"> ".length(),
                    temp.indexOf(" </td>")
            );

            temp = temp.substring(
                    temp.indexOf("<th id=\"grandPrize\" rowspan=\"2\">特獎</th> ")
            );
            Log.i("Parser", temp);
            grandPrize = temp.substring(
                    temp.indexOf("<td headers=\"grandPrize\" class=\"number\"> ") + "<td headers=\"grandPrize\" class=\"number\"> ".length(),
                    temp.indexOf(" </td>")
            );

            temp = temp.substring(
                    temp.indexOf("<th id=\"firstPrize\" rowspan=\"2\">頭獎</th> ")
            );
            Log.i("Parser", temp);
            firstPrize = temp.substring(
                    temp.indexOf("<td headers=\"firstPrize\" class=\"number\"> ") + "<td headers=\"firstPrize\" class=\"number\"> ".length(),
                    temp.indexOf(" </td>")
            );

            temp = temp.substring(
                    temp.indexOf("<th id=\"addSixPrize\">增開六獎</th>  ")
            );
            Log.i("Parser", temp);
            addSixPrize = temp.substring(
                    temp.indexOf("<td headers=\"addSixPrize\" class=\"number\"> ") + "<td headers=\"addSixPrize\" class=\"number\"> ".length(),
                    temp.indexOf(" </td>")
            );
        }catch (StringIndexOutOfBoundsException e){
            Log.e("Prize ERROR)", e.toString());
        }
    }
}

class getWeb{
    private String urlData = new String("");
    public String getWebData(){
        return  urlData;
    }

    public void handleURLdata(final String url, final Handler handler){
        new Thread(new Runnable() {
            @Override
            public void run() {
                urlData = getUrlData(url);
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }).start();
    }
    public synchronized String getUrlData(String url){
        String data = "";
        String decodedString;
        try{
            URL webUrl = new URL(url);
            HttpURLConnection httpURLConnection
                    = (HttpURLConnection)webUrl.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.connect();

            BufferedReader in = new BufferedReader(new
                    InputStreamReader(
                            httpURLConnection.getInputStream()
            ));
            while((decodedString = in.readLine()) != null){
                data += decodedString;
            }
            in.close();
        }catch (IOException e) {
            Log.e("IOException", e.toString());
        }

        return data;
    }
}
