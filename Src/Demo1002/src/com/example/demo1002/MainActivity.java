package com.example.demo1002;

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import org.jeo.data.Cursor;
import org.jeo.vector.BasicFeature;
import org.jeo.vector.Feature;
import org.jeo.vector.Schema;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCFeatureLayerListener;
import cn.creable.ucmap.openGIS.UCLayer;
import cn.creable.ucmap.openGIS.UCMapView;
import cn.creable.ucmap.openGIS.UCMarkerLayer;
import cn.creable.ucmap.openGIS.UCMarkerLayerListener;
import cn.creable.ucmap.openGIS.UCStyle;
import cn.creable.ucmap.openGIS.UCVectorLayer;

public class MainActivity extends Activity implements UCFeatureLayerListener{
	
	UCMapView mView;
	int mLayerCount=11;
	UCLayer mLayers[]=new UCLayer[mLayerCount];
	String mLayerNames[]=new String[mLayerCount];
	boolean visible[]=new boolean[mLayerCount];
	
	private MeasureTool mTool=null;
	private AnalysisTool aTool=null;
	
	private Handler mHandler=new Handler()
	{
		public void handleMessage(Message msg) 
		{
			Toast.makeText(MainActivity.this, (String)msg.obj, Toast.LENGTH_LONG).show();
		}
	};
	
	void init()
	{
		mLayerNames[0]="北京影像";
		mLayerNames[1]="县界";
		mLayerNames[2]="双河线";
		mLayerNames[3]="绿地";
		mLayerNames[4]="湖沼";
		mLayerNames[5]="县道";
		mLayerNames[6]="铁路";
		mLayerNames[7]="省道";
		mLayerNames[8]="国道高速路";
		mLayerNames[9]="国道";
		mLayerNames[10]="北京";
		String dir=Environment.getExternalStorageDirectory().getPath();
		mLayers[0]=mView.addMbtiesLayer(dir + "/bjshp/"+mLayerNames[0]+".mbtiles", 2, 15);
		UCFeatureLayer layer=mView.addFeatureLayer(this);//layer.setNameField("ADMINNAME");
		layer.loadShapefile(dir + "/bjshp/"+mLayerNames[1]+"_region.shp", 30, 2, "#FFCCCCFF", "#00000000");
		mLayers[1]=layer;
		layer=mView.addFeatureLayer(this);
		layer.loadShapefile(dir + "/bjshp/"+mLayerNames[2]+"_region.shp", 30, 0, "#00000000", "#FFB9CAFF");
		mLayers[2]=layer;
		layer=mView.addFeatureLayer(this);
		layer.loadShapefile(dir + "/bjshp/"+mLayerNames[3]+"_region.shp", 30, 0, "#00000000", "#FFC7E5B2");
		mLayers[3]=layer;
		layer=mView.addFeatureLayer(this);
		layer.loadShapefile(dir + "/bjshp/"+mLayerNames[4]+"_region.shp", 30, 0, "#00000000", "#FFB9CAFF");
		mLayers[4]=layer;
		layer=mView.addFeatureLayer(this);//layer.setNameField("RDNAME");
		layer.loadShapefile(dir + "/bjshp/"+mLayerNames[5]+"_polyline.shp", 30, 2, "#FFF1EAB5", "#00000000");
		mLayers[5]=layer;
		layer=mView.addFeatureLayer(this);
		layer.loadShapefile(dir + "/bjshp/"+mLayerNames[6]+"_polyline.shp");//, 30, 2, "#FFF1EAB5", "#00000000");
		layer.setStyle(new UCStyle(null,null,0,0,3,4, "#FF000000",8, "#FFFFFFFF"));
		mLayers[6]=layer;
		layer=mView.addFeatureLayer(this);//layer.setNameField("RDNAME");
		layer.loadShapefile(dir + "/bjshp/"+mLayerNames[7]+"_polyline.shp", 30, 2, "#FFF1EAB5", "#00000000");
		mLayers[7]=layer;
		layer=mView.addFeatureLayer(this);//layer.setNameField("RDNAME");
		layer.loadShapefile(dir + "/bjshp/"+mLayerNames[8]+"_polyline.shp", 30, 2, "#FFF1EAB5", "#00000000");
		mLayers[8]=layer;
		layer=mView.addFeatureLayer(this);//layer.setNameField("RDNAME");
		layer.loadShapefile(dir + "/bjshp/"+mLayerNames[9]+"_polyline.shp", 20, 2, "#FFF1EAB5", "#00000000");
		mLayers[9]=layer;
		layer=mView.addFeatureLayer(this);layer.setNameField("NAME");
		layer.loadShapefile(dir + "/bjshp/"+mLayerNames[10]+"_point.shp");//, 20, 2, "#FFFF0000", "#FF00FF00");
		layer.setStyle(new UCStyle(null,"bjshp/star_red.svg",0,0));
		mLayers[10]=layer;
		
		
		mView.moveTo(116.383333,39.9, 512);
		mView.postDelayed(new Runnable() {
			@Override
			public void run() {
				mView.refresh();
			}
		}, 0);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		UCMapView.setTileScale(0.5f);
		setContentView(R.layout.activity_main);
		
		mView=(UCMapView)this.findViewById(R.id.mapView);
		mView.setBackgroundColor(0xFFFFFFFF);
		String dir=Environment.getExternalStorageDirectory().getPath();
		init();
		
//		mView.addArcgisServerLayer("http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/wmts?service=WMTS&request=GetTile&version=1.0.0&layer=World_Imagery&style=default&format=image/png&TileMatrixSet=GoogleMapsCompatible", 0, 21, dir+"/cacheImgArcgis.db");
//		mView.addArcgisServerLayer("http://services.arcgisonline.com/ArcGIS/rest/services/Reference/World_Boundaries_and_Places/MapServer/wmts?service=WMTS&request=GetTile&version=1.0.0&layer=World_Boundaries_and_Places&style=default&format=image/png&TileMatrixSet=GoogleMapsCompatible", 0, 21, dir+"/cachelblArcgis.db");
//		
		
		Button btn1=(Button)findViewById(R.id.button1);
        if (btn1!=null)
        btn1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mTool==null)
				{
					BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.marker_poi);
					mTool=new MeasureTool(mView,bd.getBitmap(),0);
					mTool.start();
				}
			}
        });
        
        Button btn2=(Button)findViewById(R.id.button2);
        if (btn2!=null)
        btn2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mTool==null)
				{
					BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.marker_poi);
					mTool=new MeasureTool(mView,bd.getBitmap(),1);
					mTool.start();
				}
			}
        });
        
        Button btn3=(Button)findViewById(R.id.button3);
        if (btn3!=null)
        btn3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mTool!=null) mTool.stop();
				mTool=null;
				mView.refresh();
			}
        });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.menu_1) {
			mView.addScaleBar();
			mView.refresh();
		}
		else if (id==R.id.menu_2){
	        for (int i=0;i<mLayerCount;++i)
	        {
	        	visible[i]=mView.getLayerVisible(mLayers[i]);
	        }
	        //显示一个对话框，供用户控制图层
	        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
	        builder.setIcon(R.drawable.marker_poi);
	        builder.setTitle("图层控制");
	        builder.setMultiChoiceItems(mLayerNames, visible,new DialogInterface.OnMultiChoiceClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					visible[which]=isChecked;//保存用户的选择
				}
			});
	        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					int size=visible.length;
					for (int i=0;i<size;++i)
					{//重新设置每个图层的可见性
						mView.setLayerVisible(mLayers[i], visible[i]);
					}
					mView.refresh();//刷新地图立即看到效果
				}
			});
	        builder.setNegativeButton("取消", null);//取消按钮什么事都不做
			builder.create().show();
		}
		else if (id==R.id.menu_3){
			LayoutInflater factory = LayoutInflater.from(this);
	        View view = factory.inflate(R.layout.keyword_query_dlg, null);
	        final EditText text=(EditText)view.findViewById(R.id.keyword_edit);
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setIcon(R.drawable.marker_poi);
	        builder.setTitle("查询");
	        builder.setView(view);
	        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					String keyword=text.getText().toString();
					if (keyword.equalsIgnoreCase(""))
                	{
                		AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
    					builder1.setTitle("信息");
    					builder1.setMessage("关键字不能为空");
    					builder1.setCancelable(true);
    					builder1.setPositiveButton("确定", null);
    					builder1.create().show();
    					return;
                	}
					//TODO:这里查询序号为1的FeatureLayer
					Cursor<Feature> cursor=((UCFeatureLayer)mLayers[1]).searchFeature("ADMINNAME like '%"+keyword+"%'", 0, 0, 0, 0, 0, 0);
					try {
						Vector<Feature> features=new Vector<Feature>();
						Envelope env=null;
						while (cursor.hasNext()) {
							Feature ft = cursor.next();
							features.add(ft);
							if (env==null) env=ft.geometry().getEnvelopeInternal();
							else env.expandToInclude(ft.geometry().getEnvelopeInternal());
						}
						cursor.close();
						
						if (features.size()>0){
							mView.getMaskLayer().setData(features, 30, 2, "#88FF0000", "#88FF0000");
							mView.refresh(1000, env);
						}
						else
						{
							AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
	    					builder1.setTitle("信息");
	    					builder1.setMessage("没有符合要求的目标");
	    					builder1.setCancelable(true);
	    					builder1.setPositiveButton("确定", null);
	    					builder1.create().show();
	    					return;
						}
						
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					
				}
			});
	        builder.setNegativeButton("取消", null);//取消按钮什么事都不做
			builder.create().show();
		}
		else if (id==R.id.menu_4){
			Envelope env=null;
			UCFeatureLayer flayer=null;
			for (int i=0;i<mLayerCount;++i)
			{
				if (mLayers[i] instanceof UCFeatureLayer)
				{
					flayer=(UCFeatureLayer)mLayers[i];
					if (env==null) env=flayer.getFullExtent();
					else env.expandToInclude(flayer.getFullExtent());
				}
			}
			if (env!=null) mView.refresh(1000, env);
		}
		else if (id==R.id.menu_5){
			CRSFactory crsf=new CRSFactory();
			CoordinateTransformFactory ctf=new CoordinateTransformFactory();
			//String proj1="EPSG:4326";
			String param1="+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs";
			//String proj2="EPSG:2362";
			String param2="+proj=tmerc +lat_0=0 +lon_0=114 +k=1.000000 +x_0=500000 +y_0=0 +a=6378140 +b=6356755.288157528 +units=m +no_defs";
			CoordinateTransform ct=ctf.createTransform(crsf.createFromParameters("",param1),crsf.createFromParameters("",param2));
			ProjCoordinate out=new ProjCoordinate();
			ct.transform(new ProjCoordinate(113.82133,23.27463),out);
			String msg=String.format("经纬度点(113.82133,23.27463)转换到3度带高斯80坐标系之后的坐标为(%f,%f)", out.x,out.y);
			Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
		}
		else if (id==R.id.menu_6){
			if (aTool==null)
			{
				BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.marker_poi);
				
				aTool=new AnalysisTool(mView,((UCFeatureLayer)mLayers[1]),bd.getBitmap(),mHandler);
				aTool.start();
			}
			
		}
		else if (id==R.id.menu_7){
			if (aTool!=null) aTool.stop();
			aTool=null;
		}

		return super.onOptionsItemSelected(item);
	}
	
	static Feature feature(String id, Object... values) {
        Feature current=new BasicFeature(id,Arrays.asList(values));
        return current;
    }
	
	@Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

	@Override
	public boolean onItemLongPress(UCFeatureLayer layer, Feature feature, double distance) {
		if (mTool!=null || aTool!=null) return true;
		if (distance>30) return false;
		Toast.makeText(getBaseContext(), "长按了\n" + feature + " distance="+distance, Toast.LENGTH_SHORT).show();
		Vector<Feature> features=new Vector<Feature>();
		features.add(feature);
		mView.getMaskLayer().setData(features, 30, 2, "#88FF0000", "#88FF0000");
		mView.refresh();
		return true;
	}

	@Override
	public boolean onItemSingleTapUp(UCFeatureLayer layer, Feature feature, double distance) {
		if (mTool!=null || aTool!=null) return true;
		if (distance>30) return false;
		Toast.makeText(getBaseContext(), "点击了\n" + feature + " distance="+distance, Toast.LENGTH_SHORT).show();
		Vector<Feature> features=new Vector<Feature>();
		features.add(feature);
		mView.getMaskLayer().setData(features, 30, 2, "#88FF0000", "#88FF0000");
		mView.refresh();
		return true;
	}
}
