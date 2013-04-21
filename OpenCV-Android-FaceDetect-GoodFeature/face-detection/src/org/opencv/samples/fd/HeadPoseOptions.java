package org.opencv.samples.fd;



public class HeadPoseOptions 
{

public String haarCascadeSrc; //path for haarcascadefile
public int focalLength;

public HeadPoseOptions()
{
	focalLength=600;
	haarCascadeSrc="haarcascade_frontalface_alt2.xml";
}
}
