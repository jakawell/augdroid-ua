package com.example.augdroid_ua;

import android.location.Location;

public class Tag {

	/**
	 * The id of a tag (this should be unique).
	 */
	public int id;
	/**
	 * The text to display with the tag.
	 */
	public String text;
	/**
	 * The location of the tag.
	 */
	public Location location;
	/**
	 * The height of the tag (in meters from the ground).
	 */
	public float height;
	
	public Tag(int id, String text, Location location, float height) {
		this.id = id;
		this.text = text;
		this.location = location;
		this.height = height;
	}
}
