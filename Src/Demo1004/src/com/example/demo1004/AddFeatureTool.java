package com.example.demo1004;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import org.gdal.ogr.ogr;
import org.jeo.vector.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCMapView;
import cn.creable.ucmap.openGIS.UCMarkerLayer;
import cn.creable.ucmap.openGIS.UCScreenLayer;
import cn.creable.ucmap.openGIS.UCScreenLayerListener;

public class AddFeatureTool {
	
	private UCMapView mapView;
	private UCFeatureLayer layer;
	private UCScreenLayer slayer;
	private UCMarkerLayer mlayer;
	private Bitmap pointImage;
	private Bitmap crossImage;
	
	private GeometryFactory gf=new GeometryFactory();
	private Vector<Coordinate> coords;
	private Feature feature;
	
	public AddFeatureTool(UCMapView mapView,UCFeatureLayer layer,Bitmap pointImage,Bitmap crossImage)
	{
		this.mapView=mapView;
		this.layer=layer;
		this.pointImage=pointImage;
		this.crossImage=crossImage;
	}
	
	public void start()
	{
		mlayer=mapView.addMarkerLayer(null);
		String dir=Environment.getExternalStorageDirectory().getPath();
		slayer=mapView.addScreenLayer(crossImage,0,0, new UCScreenLayerListener() {

			@Override
			public boolean onItemSingleTapUp(UCScreenLayer lyr) {
				Point pt=mapView.toMapPoint(mapView.getWidth()/2, mapView.getHeight()/2);
				if (layer.getGeometryType()==ogr.wkbPoint || layer.getGeometryType()==ogr.wkbMultiPoint)
				{
					Hashtable<String,Object> values=new Hashtable<String,Object>();
					values.put("geometry", pt);
					layer.addFeature(values);
				}
				else if (layer.getGeometryType()==ogr.wkbLineString || layer.getGeometryType()==ogr.wkbMultiLineString)
				{
					mlayer.addBitmapItem(pointImage, pt.getX(),pt.getY(),"","");
					if (coords==null)
					{
						coords=new Vector<Coordinate>();
						coords.add(new Coordinate(pt.getX(),pt.getY()));
					}
					else
					{
						coords.add(new Coordinate(pt.getX(),pt.getY()));
						if (feature==null) {
							Coordinate[] cds=new Coordinate[coords.size()];
							coords.copyInto(cds);
							Hashtable<String,Object> values=new Hashtable<String,Object>();
							values.put("geometry",gf.createLineString(cds));
							feature=layer.addFeature(values);
						}
						else
						{
							Coordinate[] cds=new Coordinate[coords.size()];
							coords.copyInto(cds);
							Hashtable<String,Object> values=new Hashtable<String,Object>();
							values.put("geometry",gf.createLineString(cds));
							layer.updateFeature(feature, values);
						}
					}
				}
				else if (layer.getGeometryType()==ogr.wkbPolygon || layer.getGeometryType()==ogr.wkbMultiPolygon)
				{
					mlayer.addBitmapItem(pointImage, pt.getX(),pt.getY(),"","");
					if (coords==null)
						coords=new Vector<Coordinate>();
					coords.add(new Coordinate(pt.getX(),pt.getY()));
					if (coords.size()>2)
					{
						if (feature==null) {
							Coordinate[] cds=new Coordinate[coords.size()+1];
							coords.copyInto(cds);
							cds[coords.size()]=cds[0];
							Hashtable<String,Object> values=new Hashtable<String,Object>();
							values.put("geometry",gf.createPolygon(gf.createLinearRing(cds)));
							feature=layer.addFeature(values);
						}
						else
						{
							Coordinate[] cds=new Coordinate[coords.size()+1];
							coords.copyInto(cds);
							cds[coords.size()]=cds[0];
							Hashtable<String,Object> values=new Hashtable<String,Object>();
							values.put("geometry",gf.createPolygon(gf.createLinearRing(cds)));
							layer.updateFeature(feature, values);
						}
					}
				}
				mapView.refresh();
				return true;
			}

			@Override
			public boolean onItemLongPress(UCScreenLayer lyr) {
				mlayer.removeAllItems();
				coords.clear();
				coords=null;
				feature=null;
				mapView.refresh();
				return true;
			}
			
		});
		
		mapView.refresh();
	}
	
	public void stop()
	{
		if (slayer!=null) mapView.deleteLayer(slayer);
		if (mlayer!=null) mapView.deleteLayer(mlayer);
	}

}
