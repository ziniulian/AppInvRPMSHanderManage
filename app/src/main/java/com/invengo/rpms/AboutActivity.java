package com.invengo.rpms;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

import com.invengo.rpms.R;

public class AboutActivity extends BaseActivity {

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		final Button btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setOnTouchListener( new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {

				case MotionEvent.ACTION_DOWN: {
					// 按住事件发生后执行代码的区域
					btnBack.setBackgroundResource(R.color.lightwhite);
					break;
				}
				case MotionEvent.ACTION_MOVE: {
					// 移动事件发生后执行代码的区域
					btnBack.setBackgroundResource(R.color.lightwhite);
					break;
				}
				case MotionEvent.ACTION_UP: {
					// 松开事件发生后执行代码的区域
					btnBack.setBackgroundResource(R.color.yellow);
					break;
				}
				default:

					break;
				}
				return false;
			}
		});
		btnBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
					finish();
			}
		});

		
	}
}
