package com.phicomm.hu;




import com.googlecode.tesseract.android.TessBaseAPI;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FxService extends Service
{


    LinearLayout mFloatLayout;
    WindowManager.LayoutParams wmParams;
    WindowManager.LayoutParams params;
	WindowManager mWindowManager;

	Button mFloatView;
	Handler handler;
	Bitmap bp;
	int width,length;
	ImageView background;
	TessBaseAPI baseApi;
    Boolean flag;//判断显示窗是否生成
    int Bwidth=200;
    int Bheigth=80;
	private static final String TAG = "FxService";


	dbHelper db;
	@Override
	public void onCreate()
	{
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i(TAG, "oncreat");
	    createButton();
		createFloatView();
		createShowView();


		db=new dbHelper(FxService.this);



		handler=new Handler();
		 Display display = mWindowManager.getDefaultDisplay();
         width = display.getWidth();
         length= display.getHeight();
        //Toast.makeText(FxService.this, "create FxService", Toast.LENGTH_LONG);
         background=new ImageView(getApplicationContext());

         baseApi=new TessBaseAPI();
         //(注意)前面的地址是语言包的父级。eng表示解析的是英文
         baseApi.init("/mnt/sdcard/tesseract/", "chi_sim");
         flag=false;
     mFloatView.setOnTouchListener(new OnTouchListener()
        {
            float baseValue;
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {

                switch(event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if (event.getPointerCount() == 2){
                       // Toast.makeText(FxService.this, "haha", Toast.LENGTH_SHORT).show();

                        float x = event.getX(0) - event.getX(1);
                        float y = event.getY(0) - event.getY(1);
                        float value = (float) Math.sqrt(x * x + y * y);// 计算两点的距离
                        if (baseValue == 0) {
                            baseValue = value;
                        }
                        else {
                            if (value - baseValue >= 10) {
                                Bwidth=Bwidth+3;
                                Bheigth=Bheigth+3;
                            }
                            if (value - baseValue <= -10) {
                                Bwidth=Bwidth-3;
                                Bheigth=Bheigth-3;
                          }
                        }
                        mFloatView.setWidth(Bwidth);
                        mFloatView.setHeight(Bheigth);

                    }
                    wmParams.x = (int) event.getRawX() - mFloatView.getMeasuredWidth()/2;

                    Log.i(TAG, "RawX" + event.getRawX());
                    Log.i(TAG, "X" + event.getX());

                    wmParams.y = (int) event.getRawY() - mFloatView.getMeasuredHeight()/2 - 25;

                    Log.i(TAG, "RawY" + event.getRawY());
                    Log.i(TAG, "Y" + event.getY());
                    wmParams.alpha=0.1f;
                    mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                    break;
                case MotionEvent.ACTION_DOWN:

                   handler.postDelayed(runnable, 1000);

                   break;
                case MotionEvent.ACTION_UP:
                   wmParams.alpha=1;
                   handler.removeCallbacks(runnable);
                   if(flag==true){
                       mWindowManager.removeView(background);
                       flag=false;

                   }

                   mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                  break;

            }
                return false;
        }});
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return null;
	}

	private void createButton(){
        mFloatView = new Button(this);
        mFloatLayout=new LinearLayout(this);
        mFloatView.setWidth(Bwidth);
        mFloatView.setHeight(Bheigth);
        mFloatView.layout(10, 10, 40, 40);
        mFloatView.setText("取词");
    }


	private void createShowView(){
	    params = new WindowManager.LayoutParams();
        params.gravity = Gravity.CENTER;
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        params.width = 200;
        params.height = 200;
	}



	private void createFloatView()
    {
        wmParams = new WindowManager.LayoutParams();

        mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);

        wmParams.type = LayoutParams.TYPE_PHONE;

        wmParams.format = PixelFormat.RGBA_8888;

        wmParams.flags =LayoutParams.FLAG_NOT_FOCUSABLE;


        wmParams.gravity = Gravity.LEFT | Gravity.TOP;


        wmParams.x = 0;
        wmParams.y = 0;

        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());

        mFloatLayout.addView(mFloatView);

        mWindowManager.addView(mFloatLayout, wmParams);

        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

    }

	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mFloatLayout != null)
		{
			mWindowManager.removeView(mFloatLayout);
		}
	}
	Runnable runnable=new Runnable(){

        @Override
        public void run() {
            // TODO Auto-generated method stub

            try {
               bp=SurfaceControl.screenshot(width,length);
               saveBitmap(bp);
               flag=true;
               Bitmap saveBitmap = Bitmap.createBitmap( getDiskBitmap("/sdcard/Pictures/ScreenShot.png"),wmParams.x-15, wmParams.y-10 , 90, 50 );
               background.setImageBitmap( saveBitmap );
               mWindowManager.addView(background, params);
               mWindowManager.updateViewLayout(mFloatLayout, wmParams);
               baseApi.setImage(saveBitmap);
               String text1= baseApi.getUTF8Text();
               Toast.makeText(FxService.this, text1, Toast.LENGTH_SHORT).show();
               db.insert(text1);
               baseApi.clear();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

        }

	};

    public void saveBitmap(Bitmap bitmap) throws IOException {
//        String imageDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
//                .format(new Date(System.currentTimeMillis()));
        File file = new File("/mnt/sdcard/Pictures/ScreenShot.png");
        if(!file.exists()){
            file.createNewFile();
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 70, out)) {
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap getDiskBitmap(String pathString)
    {
        Bitmap bitmap = null;
        try
        {
            File file = new File(pathString);
            if(file.exists())
            {
                bitmap = BitmapFactory.decodeFile(pathString);

            }
        } catch (Exception e)
        {
            // TODO: handle exception
        }


        return bitmap;
    }
}
