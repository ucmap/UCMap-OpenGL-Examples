package com.example.demo1000;

import java.util.concurrent.locks.ReentrantLock;

import android.graphics.Bitmap;
import android.graphics.Paint;
import cn.creable.ucmap.openGIS.UCMarker;
import cn.creable.ucmap.openGIS.UCMarkerLayer;

public class Car {
	private UCMarker mBitmap,mText;
	
	private double x,y;
	private Bitmap bitmap;
	private String text;
	private float fontsize;
	
	private static ReentrantLock lock = new ReentrantLock();
	
	public Car(double x,double y,Bitmap bitmap,String text,float fontsize)
	{
		this.x=x;
		this.y=y;
		this.bitmap=bitmap;
		this.text=text;
		this.fontsize=fontsize;
	}
	
	public void init(UCMarkerLayer mlayer)
	{
		lock.lock();
		mBitmap=mlayer.addBitmapItem(bitmap, x,y,text,"");
		mText=mlayer.addTextItem(text,Paint.Align.LEFT,fontsize,0,x,y,text,bitmap.getWidth()-5,10);
		lock.unlock();
	}
	
	public void move(UCMarkerLayer mlayer,double x,double y)
	{
		lock.lock();
		mBitmap.setXY(x, y);
		mText.setXY(x, y);
		this.x=x;
		this.y=y;
		mlayer.refresh();
		lock.unlock();
	}
	
	public double x()
	{
		return x;
	}
	
	public double y()
	{
		return y;
	}
}
