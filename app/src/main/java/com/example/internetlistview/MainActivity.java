package com.example.internetlistview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    //用来控制列表的长度
    private final int anInt=10;
    //考虑到自行写的图片访问程序不够完善会导致图片不能显示
    //这里测试就用一张网络图
    private Bitmap bitmap;
    private ListView lv;
    List<HashMap<String,Object>> data = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv=findViewById(R.id.lv);
        getP();
        getDataAsync();
    }

    Handler handler=new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what==1){
                Bundle data1=msg.getData();
                String responseBody=data1.getString("responseBody");
                setData(responseBody);
                SimpleAdapter mSimpleAdapter = new SimpleAdapter(
                        getApplicationContext(),
                        data,
                        R.layout.listitem,
                        new String[]{"image","text"},
                        new int[]{R.id.image,R.id.text});
//                        new int[]{R.id.text1,R.id.text});//测试textview显示
                //需要对Bitmap对象进行类型判断
                mSimpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                    public boolean setViewValue(View view, Object data,
                                                String textRepresentation) {
                        if (view instanceof ImageView && data instanceof Bitmap) {
                            ImageView iv = (ImageView) view;
                            iv.setImageBitmap((Bitmap) data);
                            return true;
                        } else
                            return false;
                    }
                });
                //将数据通过适配器显示到lv
                lv.setAdapter(mSimpleAdapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                });

            }
        }
    };

    public void getDataAsync(){
        //创建client
        final OkHttpClient client=new OkHttpClient();
        //创建request
        final Request request=new Request.Builder().url("https://cnodejs.org/api/v1/topics").build();
        //发起请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //打印失败日志
                Log.d("failed","错误1");
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //获取responseBody
                String responseBody;
                if(response.isSuccessful()){
                    responseBody=response.body().string();
                    //创建message
                    Message msg=Message.obtain();
                    //创建bundle
                    Bundle data=new Bundle();
                    //将responseBody放进data中
                    data.putString("responseBody",responseBody);
                    //将data放进msg中
                    msg.setData(data);
                    //给msg设定一个id
                    msg.what=1;
                    //将msg传递给handler
                    handler.sendMessage(msg);
                }else {
                    Log.d("failed","错误2");
                }
            }
        });
    }

    public void setData(String responseBody){
        try {
            JSONObject jo=new JSONObject(responseBody);
            //jo调用getJSONArray()得到一个数组
            JSONArray jsonArray=jo.getJSONArray("data");
            JSONObject[] jsonObjects = new JSONObject[anInt];
            JSONObject[] jsonObjects_authers=new JSONObject[anInt];
            String[] s=new String[anInt];
            for(int i = 0; i <anInt; i++) {
//              获取数组中的JSONObject
                jsonObjects[i]=jsonArray.getJSONObject(i);
                jsonObjects_authers[i]=jsonObjects[i].getJSONObject("author");
//              s[i]=jsonObjects_authers[i].getString("title");
                 // 这里我们获取的是id，也可以获取其他数据，注意getString()中的字符串不要打错了
                s[i]=jsonObjects[i].getString("id");

            //创建HashMap 对象,添加键值数据
                HashMap<String,Object> map = new HashMap<String,Object>();
                //向map 对象添加两组键值对数据
//                map.put("image","dataOne_" + i);
                map.put("image",bitmap);
                map.put("text","title:" + s[i]);
                //将 map 对象添加到data  集合
                data.add(map);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //获取图片的方法
    public void getP(){
        //创建client
        final OkHttpClient client=new OkHttpClient();
        //创建request
        final Request request=new Request.Builder().url("https://cn.bing.com/sa/simg/hpb/LaDigue_EN-CA1115245085_1920x1080.jpg").build();
        //发起请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //打印失败日志
                Log.d("failed","请求网络错误");
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //获取responseBody
                String responseBody;
                if(response.isSuccessful()){
                    byte[] bytes=response.body().bytes();
                    bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                }else {
                    Log.d("failed","处理图片错误");
                }
            }
        });
    }

    //另一种获取图片的方法
    public Bitmap getBitmap(){
        Bitmap mBitmap = null;
        try {
            URL url = new URL("https://cn.bing.com/sa/simg/hpb/LaDigue_EN-CA1115245085_1920x1080.jpg");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            InputStream is = conn.getInputStream();
            mBitmap = BitmapFactory.decodeStream(is);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mBitmap;
    }
}
