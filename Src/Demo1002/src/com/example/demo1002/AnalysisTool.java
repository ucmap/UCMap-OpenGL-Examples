package com.example.demo1002;

import android.view.GestureDetector.OnGestureListener;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import org.jeo.data.Cursor;
import org.jeo.vector.BasicFeature;
import org.jeo.vector.Feature;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import cn.creable.ucmap.openGIS.GeometryType;
import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCMapView;
import cn.creable.ucmap.openGIS.UCMarkerLayer;
import cn.creable.ucmap.openGIS.UCStyle;
import cn.creable.ucmap.openGIS.UCVectorLayer;

public class AnalysisTool implements OnGestureListener {
	
	private UCMapView mMapView;
	private UCVectorLayer vlayer;
	private UCMarkerLayer mlayer;
	private Vector<Point> points=new Vector<Point>();
	private Geometry geo;
	private Bitmap pointImage;
	
	private UCFeatureLayer flayer;
	private Handler handler;
	
	private Random rnd=new Random(System.currentTimeMillis());
	
	String randomColor()
	{
		return String.format("#88%02X%02X%02X", rnd.nextInt(256),rnd.nextInt(256),rnd.nextInt(256));
	}
	
	public AnalysisTool(UCMapView mapView,UCFeatureLayer flayer,Bitmap pointImage,Handler handler)
	{
		this.mMapView=mapView;
		this.flayer=flayer;
		this.pointImage=pointImage;
		this.handler=handler;
	}
	
	public void start()
	{
		mlayer=mMapView.addMarkerLayer(null);
		vlayer=mMapView.addVectorLayer();
		mMapView.setListener(this, null);
		points.clear();
	}
	
	Coordinate[] transform(Coordinate[] coords)
	{
		if (coords.length<=0) return coords;
		Coordinate pt1=coords[0];
		if (-180<=pt1.x && pt1.x<=180 && -90<=pt1.y && pt1.y<=90)
		{//将经纬度坐标做个投影变换，用于计算面积
			CRSFactory crsf=new CRSFactory();
			CoordinateTransformFactory ctf=new CoordinateTransformFactory();
			String param1="+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs";
			String param2="+proj=tmerc +lat_0=0 +lon_0=114 +k=1.000000 +x_0=500000 +y_0=0 +a=6378140 +b=6356755.288157528 +units=m +no_defs";
			CoordinateTransform ct=ctf.createTransform(crsf.createFromParameters("",param1),crsf.createFromParameters("",param2));
			ProjCoordinate in=new ProjCoordinate();
			ProjCoordinate out=new ProjCoordinate();
			Coordinate[] result=new Coordinate[coords.length];
			for (int i=0;i<coords.length;++i)
			{
				in.setValue(coords[i].x, coords[i].y);
				ct.transform(in, out);
				result[i]=new Coordinate(out.x,out.y);
			}
			return result;
		}
		return coords;
	}
	
	double getArea(Geometry geo)
	{
		if (geo.getGeometryType().equals(GeometryType.MultiPolygon))
		{
			double area=0.0;
			MultiPolygon mp=(MultiPolygon) geo;
			for (int i=0;i<mp.getNumGeometries();++i)
				area+=getArea(mp.getGeometryN(i));
			return area;
		}
		else if (geo.getGeometryType().equals(GeometryType.Polygon))
		{
			return getArea((Polygon)geo);
		}
		return 0;
	}
	
	double getArea(Polygon pg)
	{
		double area = 0.0;
	    area += Math.abs(CGAlgorithms.signedArea(transform(pg.getExteriorRing().getCoordinates())));
	    for (int i = 0; i < pg.getNumInteriorRing(); i++) {
	      area -= Math.abs(CGAlgorithms.signedArea(transform(pg.getInteriorRingN(i).getCoordinates())));
	    }
	    return area;
	}
	
	Feature feature(String id,com.vividsolutions.jts.geom.Geometry geo)
	{
		Hashtable<String,Object> values=new Hashtable<String,Object>();
		values.put("uid", id);
		values.put("geometry", geo);
		Feature current=new BasicFeature(id,values);
		return current;
	}
	
	public void stop()
	{
		Polygon pg=(Polygon)geo;
		if (pg.isSimple())//排除用户画出自相交的polygon
		{
			Envelope env=pg.getEnvelopeInternal();
			Vector<Feature> features = new Vector<Feature>();
			//根据pg的最小外包矩形，查询有可能与pg相交的feature
			Cursor<Feature> cursor=flayer.searchFeature(null, 0, 0, env.getMinX(),env.getMaxX(),env.getMinY(),env.getMaxY());
			try {
				Vector<UCStyle> styles=new Vector<UCStyle>();
				StringBuilder sb=new StringBuilder();
				sb.append("面积统计如下\n");
				while (cursor.hasNext()) {
					Feature ft = cursor.next();
					if (pg.intersects(ft.geometry()))//pg和ft.geometry是否相交
					{
						Geometry geo=pg.intersection(ft.geometry());//求pg和ft.geometry的交集
						features.add(feature(ft.id(),geo));
						styles.add(new UCStyle("uid='"+ft.id()+"'",30,0,"#880000FF", randomColor()));
						sb.append(String.format("%s=%.6f平方米\n", ft.get("ADMINNAME"),getArea(ft.geometry())));
					}
				}
				cursor.close();
				mMapView.getMaskLayer().setData(features);
				mMapView.getMaskLayer().setStyles(styles);
				mMapView.refresh();
				if (handler!=null)
				{
					Message msg=new Message();
					msg.obj=sb.toString();
					handler.sendMessage(msg);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		mMapView.deleteLayer(mlayer);
		mMapView.deleteLayer(vlayer);
		mMapView.setListener(null, null);
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		Point pt=mMapView.toMapPoint(e.getX(), e.getY());
		mlayer.addBitmapItem(pointImage, pt.getX(),pt.getY(),"","");
		points.add(pt);
		
		if (points.size()==3)
		{
			GeometryFactory gf=new GeometryFactory();
			Coordinate[] coords=new Coordinate[points.size()+1];
			int i;
			for (i=0;i<points.size();++i)
			{
				coords[i]=new Coordinate(points.get(i).getX(),points.get(i).getY());
			}
			coords[i]=new Coordinate(points.get(0).getX(),points.get(0).getY());
			geo=gf.createPolygon(gf.createLinearRing(coords));
			vlayer.addPolygon(geo, 5, 0xFF991111,0xFF222299,0.5f);
		}
		else if (points.size()>3)
		{
			GeometryFactory gf=new GeometryFactory();
			Coordinate[] coords=new Coordinate[points.size()+1];
			int i;
			for (i=0;i<points.size();++i)
			{
				coords[i]=new Coordinate(points.get(i).getX(),points.get(i).getY());
			}
			coords[i]=new Coordinate(points.get(0).getX(),points.get(0).getY());
			Geometry newgeo=gf.createPolygon(gf.createLinearRing(coords));
			vlayer.updateGeometry(geo, newgeo);
			geo=newgeo;
		}
		mMapView.refresh();
		return true;
	}

}
