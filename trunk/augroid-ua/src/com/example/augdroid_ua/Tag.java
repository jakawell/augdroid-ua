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
	/**
	 * Whether or not to force the location on the screen based on screen location variable in stead of lat-long location variables
	 */
	public boolean forceScreenLocation;
	/**
	 * Whether the tag is visible on the screen or not.
	 */
	public boolean screenVisible;
	/**
	 * The x coordinate of the center of the tag on the screen, in pixels.
	 */
	public int screenLocationX = 0;
	/**
	 * The y coordinate of the center of the tag on the screen, in pixels.
	 */
	public int screenLocationY = 0;
	/**
	 * The radius of the tag on the screen, in pixels.
	 */
	public int screenRadius = 0;
	/**
	 * The last distance recorded before the screen location was forced, in meters.
	 */
	public float screenOldDistance = 0;
	/**
	 * Whether or not the tag should be highlighted (i.e., when selected).
	 */
	public boolean highlight = false;
	
	public Tag(int id, String text, Location location, float height) {
		this.id = id;
		this.text = text;
		this.location = location;
		this.height = height;
		this.forceScreenLocation = false;
		this.screenVisible = false;
	}
	
	public void setOnScreen(int screenLocationX, int screenLocationY, int screenRadius) {
		this.screenVisible = true;
		this.screenLocationX = screenLocationX;
		this.screenLocationY = screenLocationY;
		this.screenRadius = screenRadius;
	}
	
	public void setOffScreen() {
		this.screenVisible = false;
	}
	
	public void forceLocation(int screenLocationX, int screenLocationY, float oldDistance) {
		this.forceScreenLocation = true;
		this.screenLocationX = screenLocationX;
		this.screenLocationY = screenLocationY;
		this.screenOldDistance = oldDistance;
		this.highlight = true;
	}
	
	public void releaseForceLocation() {
		this.forceScreenLocation = false;
		this.highlight = false;
	}
	
	public void highlight(boolean highlight) {
		this.highlight = highlight;
	}
}
