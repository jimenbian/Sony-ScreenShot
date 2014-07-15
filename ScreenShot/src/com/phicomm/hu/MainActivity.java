package com.phicomm.hu;


import java.lang.reflect.Method;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends Activity
{
	//���帡�����ڲ���
	LinearLayout mFloatLayout;
	//���������������ò��ֲ���Ķ���
	WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
    WindowManager mWindowManager;
    //** Called when the activity is first created.
    ListView lv;
    dbHelper db;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        lv=new ListView(this);
        lv=(ListView)findViewById(R.id.listView1);
        Button start = (Button)findViewById(R.id.start_id);

        Button remove = (Button)findViewById(R.id.remove_id);
        db=new dbHelper(MainActivity.this);
        Cursor myCursor = db.select();
        SimpleCursorAdapter adpater=new SimpleCursorAdapter(this
                , R.layout.test, myCursor,
                new String[]{dbHelper.FIELD_TITLE},
                new int[]{R.id.topTextView});

        lv.setAdapter(adpater);
        start.setOnClickListener(new OnClickListener()
        {

			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, FxService.class);
				startService(intent);
				finish();
			}
		});

        remove.setOnClickListener(new OnClickListener()
        {

			@Override
			public void onClick(View v)
			{
				//uninstallApp("com.phicomm.hu");
				Intent intent = new Intent(MainActivity.this, FxService.class);
				stopService(intent);
			}
		});

    }

    private void uninstallApp(String packageName)
    {
    	Uri packageURI = Uri.parse("package:"+packageName);
    	Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
    	startActivity(uninstallIntent);
        //setIntentAndFinish(true, true);
    }

   /* private void forceStopApp(String packageName)
    {
    	 ActivityManager am = (ActivityManager)getSystemService(
                 Context.ACTIVITY_SERVICE);
    		 am.forceStopPackage(packageName);

    	Class c = Class.forName("com.android.settings.applications.ApplicationsState");
    	Method m = c.getDeclaredMethod("getInstance", Application.class);

    	  //mState = ApplicationsState.getInstance(this.getApplication());
    }*/
}