package com.example.demo1003;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.jeo.json.parser.ContentHandler;
import org.jeo.json.parser.ParseException;

public class POIJSONHandler implements ContentHandler {
	
	public Vector<Hashtable<String,Object>> pois;
	public Hashtable<String,Object> cur;
	public String key;
	public boolean isInPOIs=false;

	@Override
	public void startJSON() throws ParseException, IOException {
		pois=new Vector<Hashtable<String,Object>>();
	}

	@Override
	public void endJSON() throws ParseException, IOException {
		
	}

	@Override
	public boolean startObject() throws ParseException, IOException {
		if (isInPOIs) cur=new Hashtable<String,Object>();
		return true;
	}

	@Override
	public boolean endObject() throws ParseException, IOException {
		if (isInPOIs) {
			pois.add(cur);
			cur=null;
		}
		return true;
	}

	@Override
	public boolean startObjectEntry(String key) throws ParseException, IOException {
		if (key.equals("pois"))
			isInPOIs=true;
		if (cur!=null) 
			this.key=key;
		return true;
	}

	@Override
	public boolean endObjectEntry() throws ParseException, IOException {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean startArray() throws ParseException, IOException {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean endArray() throws ParseException, IOException {
		isInPOIs=false;
		return true;
	}

	@Override
	public boolean primitive(Object value) throws ParseException, IOException {
		if (cur!=null)
		{
			cur.put(key, value);
		}
		return true;
	}

}
