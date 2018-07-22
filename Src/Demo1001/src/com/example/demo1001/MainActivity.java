package com.example.demo1001;

import java.util.Vector;

import org.jeo.vector.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import cn.creable.ucmap.openGIS.ComparisonOperator;
import cn.creable.ucmap.openGIS.QueryLayerParam;
import cn.creable.ucmap.openGIS.QueryParam;
import cn.creable.ucmap.openGIS.SpatialOperator;
import cn.creable.ucmap.openGIS.UCMapView;
import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCFeatureLayerListener;

public class MainActivity extends Activity {
	
	UCMapView mView;
	UCFeatureLayer olayer;
	
	String server="http://192.168.1.3:8080/geoserver/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UCMapView.setTileScale(0.5f);
		setContentView(R.layout.activity_main);
		
		mView=(UCMapView)this.findViewById(R.id.mapView);
		String dir=Environment.getExternalStorageDirectory().getPath();
		mView.addGeoserverLayer(server+"gwc/service/wmts?service=WMTS&request=GetTile&version=1.0.0&layer=tiger-ny&style=default&format=image/png&TileMatrixSet=EPSG:900913", 0, 21, dir+"/cacheTiger.db");
		olayer=mView.addFeatureLayer(new UCFeatureLayerListener() {

			@Override
			public boolean onItemSingleTapUp(UCFeatureLayer layer, Feature feature,double distance) {
				if (distance>30) return false;
				Toast.makeText(getBaseContext(), "点击了\n" + feature, Toast.LENGTH_SHORT).show();
				Vector<Feature> features=new Vector<Feature>();
				features.add(feature);
				mView.getMaskLayer().setData(features, 30, 8, "#8800FF00", "#8800FF00");
				mView.refresh();
				return true;
			}

			@Override
			public boolean onItemLongPress(UCFeatureLayer layer, Feature feature,double distance) {
				if (distance>30) return false;
				Toast.makeText(getBaseContext(), "长按了\n" + feature, Toast.LENGTH_SHORT).show();
				Vector<Feature> features=new Vector<Feature>();
				features.add(feature);
				mView.getMaskLayer().setData(features, 30, 8, "#8800FF00", "#8800FF00");
				mView.refresh();
				return false;
			}
			
		});
		mView.moveTo(-74.0085734,40.7119456, 65536);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.menu_1) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					QueryParam param=new QueryParam();
					param.setCount(100000);
					param.setNamespace("tiger");
					param.setNamespaceURL("http://www.census.gov");
					QueryLayerParam lp=param.createAndAddQueryLayerParam("tiger_roads");
//					lp.addReturnField("CFCC");
//					lp.addReturnField("the_geom");
//					lp.addOrderbyFieldASC("CFCC");
					
					//lp.AttFilterInsert("tiger","CFCC", ComparisonOperator.Like, "63", null);
					
					GeometryFactory gf=new GeometryFactory();
					Coordinate[] coords2=new Coordinate[5];
					coords2[0]=new Coordinate(-75,40);
					coords2[1]=new Coordinate(-75,41);
					coords2[2]=new Coordinate(-73,41);
					coords2[3]=new Coordinate(-73,40);
					coords2[4]=new Coordinate(-75,40);
					Polygon pg=gf.createPolygon(gf.createLinearRing(coords2));
					lp.SpatialFilterInsert(SpatialOperator.Within, pg, null, 0, "tiger","the_geom", "EPSG:4326", false);
					
					//lp.SpatialFilterInsert(SpatialOperator.BBOX, null, new Envelope(-75, -73, 40, 41), 0, "tiger", "the_geom", "EPSG:4326", false);
					Vector<Feature> features=olayer.getFeature(server+"wfs",param);
					olayer.setData(features, 30, 2, "#FFFF0000", "#8800FF00");
					mView.refresh();
					
//					Envelope env=new Envelope(-75, -73, 40, 41);
//					Vector<Feature> features=olayer.getFeatureByEnvelope("http://192.168.1.6:8080/geoserver/wfs", "tiger_roads", env, "EPSG:4326", "tiger", 100000, 0);
//					olayer.setData(features, 30, 2, "#FFFF0000", "#8800FF00");
//					mView.refresh();
				}
				
			}).start();
			return true;
		}
		else if (id==R.id.menu_2) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					QueryParam param=new QueryParam();
					param.setCount(100000);
					param.setNamespace("tiger");
					param.setNamespaceURL("http://www.census.gov");
					QueryLayerParam lp=param.createAndAddQueryLayerParam("tiger_roads");
					//lp.addReturnField("CFCC");
					//lp.addReturnField("the_geom");//这个字段很重要，必须添加进返回字段列表
					lp.addOrderbyFieldASC("CFCC");
					
					lp.AttFilterInsert("tiger","CFCC", ComparisonOperator.Like, "*63*", null);
					Vector<Feature> features=olayer.getFeature(server+"wfs",param);
					olayer.setData(features, 30, 2, "#FFFF0000", "#8800FF00");
					mView.refresh();
				}
				
			}).start();
			mView.moveTo(-74.007252, 40.721735, 65536);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
