package com.example.demo1000;

import java.io.IOException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import cn.creable.ucmap.openGIS.UCMapView;
import cn.creable.ucmap.openGIS.UCMarkerLayer;
import cn.creable.ucmap.openGIS.UCMarkerLayerListener;
import cn.creable.ucmap.openGIS.UCVectorLayer;

public class MainActivity extends Activity implements LocationListener{
	
	UCMapView mView;
	UCVectorLayer vlayer;
	UCMarkerLayer mlayer;
	Polygon pg;
	
	boolean hasGeojsonLayer=false;
	
	int type=0;
	
	GeometryFactory gf=new GeometryFactory();
	
    private LocationManager locationManager;
    private boolean hasLocation=false;
    private double mylon,mylat;
    
    private Car[] mCars=new Car[4];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		UCMapView.setTileScale(0.5f);
		setContentView(R.layout.activity_main);
		
		mView=(UCMapView)this.findViewById(R.id.mapView);
		mView.setBackgroundColor(0xFFFFFFFF);
		String dir=Environment.getExternalStorageDirectory().getPath();
		mView.addTDMapLayer("http://t0.tianditu.cn/vec_c/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=vec&STYLE=default&TILEMATRIXSET=c&FORMAT=tiles", 1, 18,dir+"/cacheVec.db");
		mView.addTDMapLayer("http://t1.tianditu.cn/cva_c/wmts?service=wmts&request=GetTile&version=1.0.0&LAYER=cva&tileMatrixSet=c&format=tiles", 1, 18,dir+"/cacheCva.db");
		mView.addTDMapLayer("http://t0.tianditu.cn/img_c/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=img&STYLE=default&TILEMATRIXSET=c&FORMAT=tiles", 1, 18,dir+"/cacheImg.db");
		mView.addTDMapLayer("http://t1.tianditu.cn/cia_c/wmts?service=wmts&request=GetTile&version=1.0.0&LAYER=cia&tileMatrixSet=c&format=tiles", 1, 18,dir+"/cacheCia.db");
		mView.setLayerVisible(2, false);
		mView.setLayerVisible(3, false);
		if (vlayer==null) vlayer=mView.addVectorLayer();
		mView.moveTo(118.778771, 32.043880, 512);
		
		//mView.addGeoserverLayer("http://192.168.1.10:8080/geoserver/gwc/service/wmts?service=WMTS&request=GetTile&version=1.0.0&layer=tiger-ny&style=default&format=image/png&TileMatrixSet=EPSG:900913", 0, 21,dir+"/cacheTiger.db");
		
		mView.addLocationLayer();
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
		if (id == R.id.menu_100) {
			if (type!=0)
			{
				mView.setLayerVisible(0, true);
				mView.setLayerVisible(1, true);
				mView.setLayerVisible(2, false);
				mView.setLayerVisible(3, false);
				mView.refresh();
				type=0;
			}
		}
		else if (id == R.id.menu_101) {
			if (type!=1)
			{
				mView.setLayerVisible(0, false);
				mView.setLayerVisible(1, false);
				mView.setLayerVisible(2, true);
				mView.setLayerVisible(3, true);
				mView.refresh();
				type=1;
			}
		}
		else if (id == R.id.menu_1) {
			mView.addScaleBar();
			mView.refresh();
		}
		else if (id==R.id.menu_2){
			vlayer.addPoint(gf.createPoint(new Coordinate(118.778771,32.04388)), 0.0001, 0xFFFF0000,1);
			mView.moveTo(118.778771, 32.043880, 32768*4);
		}
		else if (id==R.id.menu_3){
			Coordinate[] coords=new Coordinate[2];
			coords[0]=new Coordinate(118.778771,32.04388);
			coords[1]=new Coordinate(118.778771,32.14388);
			vlayer.addLine(gf.createLineString(coords), 5, 0xFF00FF00);
			mView.moveTo(118.778771, 32.043880, 32768);
		}
		else if (id==R.id.menu_4){
			Coordinate[] coords2=new Coordinate[4];
			coords2[0]=new Coordinate(118.779771,32.04388);
			coords2[1]=new Coordinate(118.779771,32.14388);
			coords2[2]=new Coordinate(118.879771,32.14388);
			coords2[3]=new Coordinate(118.779771,32.04388);
			pg=gf.createPolygon(gf.createLinearRing(coords2));
			vlayer.addPolygon(pg, 10, 0xFF00FF00,0xFF0000FF,1);
			mView.moveTo(118.778771, 32.043880, 32768/4);
		}
		else if (id==R.id.menu_5){
			if (pg==null) return super.onOptionsItemSelected(item);
			Coordinate[] coords2=new Coordinate[4];
			coords2[0]=new Coordinate(118.779771,32.04388);
			coords2[1]=new Coordinate(118.779771,32.14388);
			coords2[2]=new Coordinate(118.979771,32.14388);
			coords2[3]=new Coordinate(118.779771,32.04388);
			Polygon pg2=gf.createPolygon(gf.createLinearRing(coords2));
			vlayer.updatePolygonStyle(pg, 0, 0, 0xFFFFFF00, 1);
			vlayer.updateGeometry(pg, pg2);
			pg=pg2;
			mView.moveTo(118.778771, 32.043880, 32768/8);
		}
		else if (id==R.id.menu_6){
			if (pg==null) return super.onOptionsItemSelected(item);
			vlayer.remove(pg);
			mView.refresh();
		}
		else if (id==R.id.menu_7){
			if (hasGeojsonLayer==false)
			{
				try {
					mView.addGeojsonLayer(this.getAssets().open("bj.json"), "utf-8",30, 2, "#FFFF0000", "#8800FF00");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mView.moveTo(116.383333,39.9, 512);//,60,0);
				hasGeojsonLayer=true;
			}
		}
		else if (id==R.id.menu_8){
			if (mlayer==null)
			{
				mlayer=mView.addMarkerLayer(new UCMarkerLayerListener() {

					@Override
					public boolean onItemSingleTapUp(int index, String title, String description, double x, double y) {
						Toast.makeText(getBaseContext(), "点击了\n" + title, Toast.LENGTH_SHORT).show();
						return true;
					}

					@Override
					public boolean onItemLongPress(int index, String title, String description, double x, double y) {
						Toast.makeText(getBaseContext(), "长按了\n" + title, Toast.LENGTH_SHORT).show();
						return false;
					}
					
				});
			}
			if (mCars[0]==null)
			{
				BitmapDrawable bd=(BitmapDrawable) this.getResources().getDrawable(R.drawable.marker_poi);
				float fontsize=getResources().getDisplayMetrics().density*20;
				mCars[0]=new Car(118.778771, 32.043881,bd.getBitmap(),"苏A11111",fontsize);
				mCars[1]=new Car(118.828771, 32.093881,bd.getBitmap(),"苏A22222",fontsize);
				mCars[2]=new Car(118.798771, 32.033881,bd.getBitmap(),"苏A33333",fontsize);
				mCars[3]=new Car(118.808771, 32.063881,bd.getBitmap(),"苏A44444",fontsize);
				for (int i=0;i<mCars.length;++i)
					mCars[i].init(mlayer);
			}
			mView.refresh(new Envelope(118.775771,118.831771,32.030881,32.096881));
		}
		else if (id==R.id.menu_9){
			if (mCars[0]!=null)
			{
				new Thread(new Runnable() {
				@Override
				public void run() {
					while (true)
					{
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						mCars[0].move(mlayer, mCars[0].x()+0.0004, mCars[0].y());
						mView.refresh();
					}
				}
				}).start();
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (true)
						{
							try {
								Thread.sleep(1200);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							mCars[1].move(mlayer, mCars[1].x()-0.0004, mCars[1].y());
							mView.refresh();
						}
					}
					}).start();
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (true)
						{
							try {
								Thread.sleep(800);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							mCars[2].move(mlayer, mCars[2].x(), mCars[2].y()+0.0004);
							mView.refresh();
						}
					}
					}).start();
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (true)
						{
							try {
								Thread.sleep(1400);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							mCars[3].move(mlayer, mCars[3].x(), mCars[3].y()-0.0004);
							mView.refresh();
						}
					}
					}).start();
			}
		}
		else if (id==R.id.menu_10) {
			if (hasLocation)
			{
				mView.moveTo(mylon, mylat, (1<<mView.getZoomLevel())*mView.getScale());
			}
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
}
