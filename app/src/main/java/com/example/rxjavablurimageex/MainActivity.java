package com.example.rxjavablurimageex;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    TextView tv1;
    ImageView iv;
    long count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv1 = findViewById(R.id.tv1);
        iv = findViewById(R.id.iv);
        mTimer.sendEmptyMessageDelayed(0, 200);
    }

    Handler mTimer = new Handler() {
        public void handleMessage(Message msg) {
            count ++;
            tv1.setText(Long.toString(count));
            mTimer.sendEmptyMessageDelayed(0, 200);
        }
    };

    public void blur2ImageView(Bitmap image) {
        Bitmap newImg = blur(this, image, 25);
        iv.setImageBitmap(newImg);
    }

    public Bitmap getResourceBitmap(int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap image = BitmapFactory.decodeResource(getResources(), resId, options);
        return image;
    }

    public static Bitmap blur(Context ct, Bitmap sentBitmap, int radius) {
        if (Build.VERSION.SDK_INT > 16) {
            Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
            final RenderScript rs = RenderScript.create(ct);
            final Allocation input = Allocation.createFromBitmap(rs, sentBitmap,
                    Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
            final Allocation output = Allocation.createTyped(rs, input.getType());
            final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            script.setRadius(radius);
            script.setInput(input);
            script.forEach(output);
            output.copyTo(bitmap);
            return bitmap;
        }
        return sentBitmap;
    }

    public void onBtnSync(View v) {
        Bitmap image = getResourceBitmap(R.drawable.flower01);
        blur2ImageView(image);
    }

    public void onBtnAsync(View v) {
        Bitmap image = getResourceBitmap(R.drawable.flower02);
        new ImageBlurTask().execute(image);
    }

    private class ImageBlurTask extends AsyncTask<Bitmap, Bitmap, Bitmap> {
        @Override
        protected Bitmap doInBackground(Bitmap... images) {
            Bitmap newImg = blur(getApplicationContext(), images[0], 25);
            return newImg;
        }

        protected void onPostExecute(Bitmap newImg) {
            iv.setImageBitmap(newImg);
        }
    }

    public void onBtnRxjava(View v) {
        Observable.just(getResourceBitmap(R.drawable.flower03))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(image -> blur(getApplicationContext(), image, 25))
                .subscribe(newImg -> iv.setImageBitmap(newImg));
    }

}
