package com.gaurav.hangdroid;

import java.util.ArrayList;
/*
The original idea was to have floating bubbles.But when they start floating it becomes very difficult to register the 
correct touch area.This class was designed to handle all the floating bodies on screen.Since the bodies do not float
 anymore, this class merely serves as an arraylist to store all drops into.
*/
public class DropsManager extends ArrayList<Drop>{

public Drop getDropbyName(String _name){
	Drop temp=null;

	// loop through all drops present and return the drop that has
	// given name
	for (Drop e : this) {
		if (e.getmDropName() != null) {
			if (e.getmDropName().equalsIgnoreCase(_name)) {
				temp=e;
			}
		}
	}
	return temp;
}


}


