package com.example.demo1004;

import org.jeo.vector.Feature;

import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCFeatureLayerListener;
import cn.creable.ucmap.openGIS.UCMapView;

public class DeleteFeatureTool implements UCFeatureLayerListener{
	
	private UCMapView mMapView;
	
	public DeleteFeatureTool(UCMapView mapView,UCFeatureLayer layer)
	{
		mMapView=mapView;
		layer.setListener(this);
	}

	@Override
	public boolean onItemSingleTapUp(UCFeatureLayer layer, Feature feature, double distance) {
		if (distance>30) return false;
		layer.deleteFeature(feature);
		mMapView.refresh();
		return false;
	}

	@Override
	public boolean onItemLongPress(UCFeatureLayer layer, Feature feature, double distance) {
		// TODO Auto-generated method stub
		return false;
	}

}
