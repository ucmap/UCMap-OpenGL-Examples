package com.example.demo1004;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Vector;

import org.gdal.gdal.gdal;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.Geometry;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.jeo.vector.Feature;
import org.jeo.vector.Field;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
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
	UCFeatureLayer layer;
	String shpPathname;
	
	private EditFeatureAttTool efaTool;
	private String[] fields;
	private String[] values;
	private AddFeatureTool addTool;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//UCMapView.setTileScale(0.5f);
		UCFeatureLayer.setSimplifyTolerance(0.1f);
		setContentView(R.layout.activity_main);
		
		mView=(UCMapView)this.findViewById(R.id.mapView);
		mView.setBackgroundColor(Color.WHITE);
		String dir=Environment.getExternalStorageDirectory().getPath();
		shpPathname=dir+"/polygon.shp";
		
		if (new File(shpPathname).exists())
		{
			layer=mView.addFeatureLayer(null);
			layer.loadShapefile(shpPathname,30, 2, "#FFCCCCFF", "#FF00FF00",true);
			mView.moveTo(116.383333,39.9, 512);
			//mView.moveTo(116.5832,25.9321, 1<<15);
			mView.postDelayed(new Runnable() {
				@Override
				public void run() {
					mView.refresh();
				}
			}, 0);
		}
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
			try {
				createShp(shpPathname);
				if (layer==null) layer=mView.addFeatureLayer(null);
				layer.loadShapefile(shpPathname,30, 2, "#FFCCCCFF", "#FF00FF00",true);//最后参数是true表示需要进入编辑模式
				mView.moveTo(116.383333,39.9, 512);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		else if (id==R.id.menu_2) {
			if (addTool!=null) addTool.stop();
			BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.marker_poi);
			BitmapDrawable bd2=(BitmapDrawable) getResources().getDrawable(R.drawable.cross);
			addTool=new AddFeatureTool(mView,layer,bd.getBitmap(),bd2.getBitmap());
			addTool.start();
			return true;
		}
		else if (id==R.id.menu_3) {
			if (addTool!=null) addTool.stop();
			EditFeatureTool tool=new EditFeatureTool(mView,layer);
			return true;
		}
		else if (id==R.id.menu_4) {
			if (addTool!=null) addTool.stop();
			EditFeatureAttTool tool=new EditFeatureAttTool(mView,new Handler() {
				public void handleMessage(Message msg) 
				{
					if (msg.what==1)
					{
						efaTool=(EditFeatureAttTool)msg.obj;
						int fieldCount=efaTool.layer.getFieldCount()-1;
						fields=new String[fieldCount];
						values=new String[fieldCount];
						for (int i=0;i<fieldCount;++i)
						{
							Field f=efaTool.layer.getField(i+1);
							fields[i]=f.name();
							Object value=efaTool.feature.get(f.name());
							if (value!=null)
							{
								if (f.type()==Byte.class)
									values[i]=Byte.toString((Byte)value);
								else if (f.type()==Short.class)
									values[i]=Short.toString((Short)value);
								else if (f.type()==Integer.class)
									values[i]=Integer.toString((Integer)value);
								else if (f.type()==Long.class)
									values[i]=Long.toString((Long)value);
								else if (f.type()==Float.class)
									values[i]=Float.toString((Float)value);
								else if (f.type()==Double.class)
									values[i]=Double.toString((Double)value);
								else if (f.type()==java.sql.Date.class)
								{
									SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									values[i]=format.format((java.sql.Date)value);
								}
								else if (f.type()==java.sql.Time.class)
								{
									SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									values[i]=format.format((java.sql.Time)value);
								}
								else if (f.type()==String.class)
									values[i]=(String)value;
							}
							else
							{
								if (f.type()==String.class)
									values[i]="";
								else if (f.type()==java.sql.Date.class)
								{
									values[i]="";
								}
								else if (f.type()==java.sql.Time.class)
								{
									values[i]="";
								}
								else
									values[i]="0";
							}
						}
						showModifyDialog();
					}
				}
			});
			layer.setListener(tool);
			return true;
		}
		else if (id==R.id.menu_5) {
			if (addTool!=null) addTool.stop();
			DeleteFeatureTool tool=new DeleteFeatureTool(mView,layer);
			return true;
		}
		else if (id==R.id.menu_6) {
			layer.saveShapefile(shpPathname);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
private Vector<EditText> ets=new Vector<EditText>();
    
    private LinearLayout.LayoutParams LP_FF = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);     
    private LinearLayout.LayoutParams LP_FW = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);     
    private LinearLayout.LayoutParams LP_WW = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
    
    private void showModifyDialog()
    {
    	ScrollView sv   = new ScrollView(this);     
    	sv.setLayoutParams( LP_FF );
    	
    	LinearLayout layout = new LinearLayout(this);
    	layout.setOrientation( LinearLayout.VERTICAL );
    	sv.addView( layout ); 
    	
    	TextView tv;
    	EditText et;
    	RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(400,LayoutParams.WRAP_CONTENT);
    	//lp.setMargins(70, 0, 0, 0);
    	
    	int count=fields.length;
    	ets.clear();
    	for (int i=0;i<count;++i)
    	{
	    	tv = new TextView(MainActivity.this);
	    	tv.setText(fields[i]);
	    	tv.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Medium);
	    	layout.addView( tv );
	    	et=new EditText(MainActivity.this);
	    	et.setLayoutParams(lp);
	    	et.setSingleLine(true);
	    	et.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Medium);
	    	et.setTextColor(0xFF000000);
	    	if (values!=null) et.setText(values[i]);
	    	layout.addView(et);
	    	ets.addElement(et);
    	}

    	AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    	builder.setView(sv).setTitle("修改属性").setIcon(R.drawable.marker_poi)
    	.setPositiveButton("确定", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				int count=ets.size();
				String[] values=new String[count];
				for (int i=0;i<count;++i)
				{
					values[i]=ets.elementAt(i).getText().toString();
				}
				Hashtable<String,Object> newFeature=new Hashtable<String,Object>();
				newFeature.put("geometry", efaTool.feature.geometry());
				try {
					for (int i=0;i<fields.length;++i)
					{
						Field f=efaTool.layer.getField(fields[i]);
						if (f.type()==Byte.class)
							newFeature.put(fields[i], Byte.parseByte(values[i]));
						else if (f.type()==Short.class)
							newFeature.put(fields[i], Short.parseShort(values[i]));
						else if (f.type()==Integer.class)
							newFeature.put(fields[i], Integer.parseInt(values[i]));
						else if (f.type()==Long.class)
							newFeature.put(fields[i], Long.parseLong(values[i]));
						else if (f.type()==Float.class)
							newFeature.put(fields[i], Float.parseFloat(values[i]));
						else if (f.type()==Double.class)
							newFeature.put(fields[i], Double.parseDouble(values[i]));
						else if (f.type()==java.sql.Date.class)
						{
							SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							newFeature.put(fields[i], format.parse(values[i]));
						}
						else if (f.type()==java.sql.Time.class)
						{
							SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							newFeature.put(fields[i], new java.sql.Time(format.parse(values[i]).getTime()));
						}
						else if (f.type()==String.class)
							newFeature.put(fields[i], values[i]);
					}
					efaTool.layer.updateFeature(efaTool.feature, newFeature);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
    		
    	})
    	.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
    	builder.create().show();
    }
	
	private void createShp(String shpPath) throws UnsupportedEncodingException {

        ogr.RegisterAll();
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "NO");
        gdal.SetConfigOption("SHAPE_ENCODING", "UTF-8");

        String strDriverName = "ESRI Shapefile";
        org.gdal.ogr.Driver oDriver = ogr.GetDriverByName(strDriverName);
        if (oDriver == null) {
            System.out.println(strDriverName + " 驱动不可用！\n");
            return;
        }
        //String shpPath=Environment.getExternalStorageDirectory().getPath() + "/geodata/utf8.shp";
        DataSource oDS = oDriver.CreateDataSource(shpPath, null);
        if (oDS == null) {
            System.out.println("创建矢量文件【" + shpPath + "】失败！\n");
            return;
        }

        Layer oLayer = oDS.CreateLayer("TestPolygon", null, ogr.wkbPolygon, null);
        if (oLayer == null) {
            System.out.println("图层创建失败！\n");
            return;
        }

        // 下面创建属性表
        // 先创建一个叫FieldID的整型属性
        FieldDefn oFieldID = new FieldDefn("FieldID", ogr.OFTInteger);
        int ret=oLayer.CreateField(oFieldID);

        // 再创建一个叫FeatureName的字符型属性，字符长度为50
        FieldDefn oFieldName = new FieldDefn("FieldName", ogr.OFTString);
        oFieldName.SetWidth(100);
        ret=oLayer.CreateField(oFieldName);

        FeatureDefn oDefn = oLayer.GetLayerDefn();

        // 创建三角形要素
        org.gdal.ogr.Feature oFeatureTriangle = new org.gdal.ogr.Feature(oDefn);
        oFeatureTriangle.SetField(0, 0);
//        oFeatureTriangle.SetField(1, Base64Utils.encodeStr("三角形11"));
        oFeatureTriangle.SetField(1, new String("三角形11".getBytes(), "UTF-8"));
        Geometry geomTriangle = Geometry.CreateFromWkt("POLYGON ((0 0,20 0,10 15,0 0))");
        oFeatureTriangle.SetGeometry(geomTriangle);
        ret=oLayer.CreateFeature(oFeatureTriangle);
        oLayer.DeleteFeature(ret);

//        // 创建矩形要素
//        org.gdal.ogr.Feature oFeatureRectangle = new org.gdal.ogr.Feature(oDefn);
//        oFeatureRectangle.SetField(0, 1);
//        oFeatureRectangle.SetField(1, "矩形222");
//        Geometry geomRectangle = Geometry.CreateFromWkt("POLYGON ((30 0,60 0,60 30,30 30,30 0))");
//        oFeatureRectangle.SetGeometry(geomRectangle);
//        oLayer.CreateFeature(oFeatureRectangle);
//
//        // 创建五角形要素
//        org.gdal.ogr.Feature oFeaturePentagon = new org.gdal.ogr.Feature(oDefn);
//        oFeaturePentagon.SetField(0, 2);
//        oFeaturePentagon.SetField(1, "五角形33");
//        Geometry geomPentagon = Geometry.CreateFromWkt("POLYGON ((70 0,85 0,90 15,80 30,65 15,70 0))");
//        oFeaturePentagon.SetGeometry(geomPentagon);
//        oLayer.CreateFeature(oFeaturePentagon);

        try {
            oLayer.SyncToDisk();
            oDS.SyncToDisk();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n数据集创建完成！\n");
    }
}
