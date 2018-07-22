package com.example.demo1003;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeo.data.Cursor;
import org.jeo.json.parser.JSONParser;
import org.jeo.vector.Feature;

import com.vividsolutions.jts.geom.Envelope;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCMapView;
import cn.creable.ucmap.openGIS.UCMarker;
import cn.creable.ucmap.openGIS.UCMarkerLayer;
import cn.creable.ucmap.openGIS.UCMarkerLayerListener;
import cn.creable.ucmap.openGIS.UCVectorTileLayer;

public class MainActivity extends Activity implements LocationListener,UCMarkerLayerListener {
	
	UCMapView mView;
	UCVectorTileLayer vtlayer;
	UCMarkerLayer mlayer;
	Hashtable<String,Hashtable<String,Object>> poiTable;
	UCMarker currentMarker;
	Vector<UCMarker> markers;
	
	PathAnalysisTool paTool;
	
	private LocationManager locationManager;
    private boolean hasLocation=false;
    private double mylon,mylat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mView=(UCMapView)this.findViewById(R.id.mapView);
		String dir=Environment.getExternalStorageDirectory().getPath();
		vtlayer=mView.addVectorTileLayer(dir+"/vtiles/bj.vtiles", 2, true);
		//mView.moveTo(112,37, 512);
		mView.moveTo(116.383333,39.9, 512);
		
		mlayer=mView.addMarkerLayer(this);
		
		mView.addLocationLayer();
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private boolean isContainChinese(String str) {

        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
	
	private String toURLString(String str)
	{
		if (isContainChinese(str)) {
			try {
				return java.net.URLEncoder.encode(str, "utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return str;
	}
	
	private int getAdminCode(String str)
	{
		if (str.equals("北京市"))
			return 156110000;
		else if (str.equals("重庆市"))
			return 156500000;
		else if (str.equals("成都市"))
			return 156510100;
		else if (str.equals("武汉市"))
			return 156420100;
		return -1;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.menu_1) {
			if (paTool==null)
			{
				BitmapDrawable start=(BitmapDrawable) getResources().getDrawable(R.drawable.start);
				BitmapDrawable end=(BitmapDrawable) getResources().getDrawable(R.drawable.end);
				paTool=new PathAnalysisTool(mView,start.getBitmap(),end.getBitmap());
				paTool.start();
			}
			return true;
		}
		else if (id==R.id.menu_2) {
			if (paTool!=null)
			{
				paTool.end();
				paTool=null;
			}
			return true;
		}
		else if (id==R.id.menu_3) {
			if (hasLocation)
			{
				mView.moveTo(mylon, mylat, (1<<mView.getZoomLevel())*mView.getScale());
			}
		}
		else if (id==R.id.menu_4) {
			currentMarker=null;
			LayoutInflater factory = LayoutInflater.from(this);
	        View view = factory.inflate(R.layout.keyword_query_dlg, null);
	        final EditText text=(EditText)view.findViewById(R.id.keyword_edit);
	        final Spinner spin=(Spinner)view.findViewById(R.id.keyword_area);
	        String[] m={"北京市","重庆市","成都市","武汉市"};
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,m);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spin.setAdapter(adapter);

	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setIcon(R.drawable.poiresult);
	        builder.setTitle("POI查询");
	        builder.setView(view);
	        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					final String keyword=text.getText().toString();
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
					mlayer.removeAllItems();
				    poiTable=new Hashtable<String,Hashtable<String,Object>>();
				    markers=new Vector<UCMarker>();
					
					new Thread(new Runnable() {
					
						@Override
						public void run() {
							String page=String.format("http://www.tianditu.com/query.shtml?postStr={\"keyWord\":\"%s\",\"level\":\"11\",\"specifyAdminCode\":\"%d\",\"mapBound\":\"-180,-90,180,90\",\"queryType\":\"1\",\"count\":\"20\",\"start\":\"0\"}&type=query",
									toURLString(keyword),
									getAdminCode((String)spin.getSelectedItem()));
							URL urlString;
							try {
								urlString = new URL(page);
								URLConnection conn = urlString.openConnection();
							    InputStream is = conn.getInputStream();
							    JSONParser p = new JSONParser();
							    POIJSONHandler h=new POIJSONHandler();
							    p.parse(new InputStreamReader(is,"utf-8"),h);
							    is.close();
							    
							    final Envelope env=new Envelope();
							    BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.poiresult);
							    for (Hashtable<String,Object> poi:h.pois)
							    {
							    	String[] list=((String)poi.get("lonlat")).split(" ");
							    	double lon=Double.parseDouble(list[0]);
							    	double lat=Double.parseDouble(list[1]);
							    	env.expandToInclude(lon, lat);
							    	String id=(String)poi.get("hotPointID");
							    	poiTable.put(id, poi);
							    	markers.add(mlayer.addBitmapItem(bd.getBitmap(), lon,lat,"",id));
							    }
							    mView.postDelayed(new Runnable() {
									@Override
									public void run() {
										if (markers.size()>0)
											mView.refresh(1000,env);
										else
										{
											AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
					    					builder1.setTitle("信息");
					    					builder1.setMessage("没找到任何东西");
					    					builder1.setCancelable(true);
					    					builder1.setPositiveButton("确定", null);
					    					builder1.create().show();
										}
									}
								}, 0);
							    
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						    
						}
						
					}).start();
				}
			});
	        builder.setNegativeButton("取消", null);//取消按钮什么事都不做
			builder.create().show();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
    protected void onResume() {
        super.onResume();

        enableAvailableProviders();
    }

    @Override
    protected void onPause() {
        super.onPause();

        locationManager.removeUpdates(this);
    }
    
    private void enableAvailableProviders() {
        locationManager.removeUpdates(this);

        for (String provider : locationManager.getProviders(true)) {
            if (LocationManager.GPS_PROVIDER.equals(provider)
                    || LocationManager.NETWORK_PROVIDER.equals(provider)) {
                locationManager.requestLocationUpdates(provider, 0, 0, this);
            }
        }
    }

	@Override
	public void onLocationChanged(Location location) {
		mView.setLocationPosition(location.getLongitude(), location.getLatitude(),location.getAccuracy());
		//mView.refresh();
		hasLocation=true;
		mylon=location.getLongitude();
		mylat=location.getLatitude();
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onItemLongPress(int index,String title,String description,double x,double y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onItemSingleTapUp(int index,String title,String description,double x,double y) {
		Hashtable<String,Object> poi=poiTable.get(description);
		if (currentMarker!=null)
		{//把原来被点中的marker还原
			BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.poiresult);
			currentMarker.setBitmap(bd.getBitmap());
		}
		//将这次被点中的marker改变样式
		BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.poiresult_sel);
		currentMarker=markers.get(index);
		currentMarker.setBitmap(bd.getBitmap());
		Toast.makeText(getBaseContext(), poi.toString(), Toast.LENGTH_LONG).show();
		return true;
	}
}
