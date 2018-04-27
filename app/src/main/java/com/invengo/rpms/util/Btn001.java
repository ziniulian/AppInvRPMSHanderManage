package com.invengo.rpms.util;

import android.view.MotionEvent;
import android.view.View;

import com.invengo.rpms.R;

/**
 * 右上角按钮样式
 * Created by Administrator on 2018/3/27.
 */

public class Btn001 implements View.OnTouchListener {
	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		switch (motionEvent.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				// 按住事件发生后执行代码的区域
				view.setBackgroundResource(R.drawable.backhoverbtn);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				// 移动事件发生后执行代码的区域
				view.setBackgroundResource(R.drawable.backhoverbtn);
				break;
			}
			case MotionEvent.ACTION_UP: {
				// 松开事件发生后执行代码的区域
				view.setBackgroundResource(R.drawable.backbtn);
				break;
			}
		}
		return false;
	}
}
