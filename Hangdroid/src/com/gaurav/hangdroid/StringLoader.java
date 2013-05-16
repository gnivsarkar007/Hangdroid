package com.gaurav.hangdroid;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.anddev.andengine.util.Debug;
import android.util.SparseArray;

public class StringLoader {
	SparseArray<String> mStringHolder;
	
	BufferedReader readFromFile;
	String str;
	public StringLoader(InputStream is){
		int i=0;
		mStringHolder=new SparseArray<String>();

		try {
			readFromFile= new BufferedReader(new InputStreamReader(is));
			while((str=readFromFile.readLine())!=null){
				mStringHolder.append(++i, str);
				Debug.d("String "+str+" "+i);
			}
			is.close();
			readFromFile.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Debug.d("String "+str+" "+i+" "+e.toString()+" "+System.getProperty("home.dir"));
		} 
		
		
	}
	public SparseArray<String> getmStringHolder() {
		return mStringHolder;
	}

	public void setmStringHolder(SparseArray<String> mStringHolder) {
		this.mStringHolder = mStringHolder;
	}

	
}
