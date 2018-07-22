package com.example.demo1004;

import org.jeo.vector.Feature;

import android.os.Handler;
import android.os.Message;
import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCFeatureLayerListener;
import cn.creable.ucmap.openGIS.UCMapView;

public class EditFeatureAttTool implements UCFeatureLayerListener {
	
	private UCMapView mapView;
	private Handler handler;
	
	public UCFeatureLayer layer;
	public Feature feature;
	
	public EditFeatureAttTool(UCMapView mapView,Handler handler)
	{
		this.mapView=mapView;
		this.handler=handler;
	}

	@Override
	public boolean onItemSingleTapUp(UCFeatureLayer layer, Feature feature, double distance) {
		if (distance>30) return false;
		this.layer=layer;
		this.feature=feature;
		if (handler!=null)
		{
			Message msg=new Message();
			msg.what=1;
			msg.obj=this;
			handler.sendMessage(msg);
		}
		return true;
	}

	@Override
	public boolean onItemLongPress(UCFeatureLayer layer, Feature feature, double distance) {
		// TODO Auto-generated method stub
		return false;
	}

}
