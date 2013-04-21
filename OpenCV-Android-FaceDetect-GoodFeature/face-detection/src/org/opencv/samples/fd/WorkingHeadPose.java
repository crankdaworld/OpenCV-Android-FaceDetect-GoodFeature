package org.opencv.samples.fd;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.video.Video;

import android.util.Log;

//http://stackoverflow.com/questions/9321307/image-perspective-transform-using-android-opencv
//http://stackoverflow.com/questions/8197107/opencv-camera-pose-estimation

public class WorkingHeadPose 
{
enum HeadPoseStatus {NONE, KEYFRAME, TRACKING};
//private List<MatOfPoint> previousCorners;
//private List<MatOfPoint> corners;
public MatOfPoint2f previousCorners;
public MatOfPoint corners,tempcorners;

public int cornerCount,maxCorners,focalLength;
public MatOfPoint3f modelPoints;
//private solvePnP positObject;
//private float translationVector,rotationMatrix;
public Mat Rvec;
public Mat Tvec;
public CascadeClassifier haarCascade;
public Mat previousFrame;
public float PI=3.141592f;
HeadPoseOptions hpo;
HeadPoseStatus hpstatus;
Rect[] TempFace;



public WorkingHeadPose()
{
	hpo=new HeadPoseOptions();
	hpstatus=HeadPoseStatus.NONE;
	cornerCount=0;
	maxCorners=100;
	//previousCorners= new ArrayList<MatOfPoint>();
	//corners= new ArrayList<MatOfPoint>();
	previousCorners= new MatOfPoint2f();
	corners= new MatOfPoint();
	tempcorners=new MatOfPoint();
	Rvec=new Mat(3,1,CvType.CV_64FC1);
	Tvec=new Mat(3,1,CvType.CV_64FC1);
	//previousFrame=new Mat();
	modelPoints = new MatOfPoint3f();
	focalLength=hpo.focalLength;
	
}

public void hpFind(Mat mRgba,Mat mGray,WorkingHeadPose hp,Rect[] facesArray)
//public void hpFind(Mat mRgba,Mat mGray,HeadPose hp)
{
	
	int i;
	Log.i("HeadPose","hpFind:Total Faces Found"+facesArray.length);
	if(hp.hpstatus==HeadPoseStatus.NONE)
	{
		if(facesArray.length<1)
		return;
		TempFace=facesArray.clone();
		hp.cornerCount=hp.maxCorners;
		Rect roi = new Rect((int)facesArray[0].tl().x,(int)(facesArray[0].tl().y),facesArray[0].width,(int)(facesArray[0].height));//imran
		Mat cropped = new Mat();
		Mat GrayClone=new Mat();
		GrayClone=mGray.clone();
		cropped = GrayClone.submat(roi);
		hpFindCorners(cropped,hp);
		//mGray is untouched
		//******** working fine upto here
		
		 // Map face points to model
		
		if(hp.cornerCount<4)
		return;
		
		Vector<Point3> points = new Vector<Point3>();
		if(hp.corners.total()>0)//adding to make sure, copying is done perfectly.imran,was getting exception
		{
		Log.i("hpFind+","hp.corners.total()"+hp.corners.total());
		Log.i("hpFind+","hp.cornerCount"+hp.cornerCount);
		
		//Point3 temp1;//=new Point3();
		
		for(i=0;i<hp.cornerCount;i++)
		{
		if(i==hp.corners.total())
			break;
		//Log.i("hpFind+","Itertion"+i);
		points.add(new Point3((hp.corners.toList().get(i).x/facesArray[0].width) - 0.5,-(hp.corners.toList().get(i).y/facesArray[0].height) + 0.5, 0.5 * Math.sin(PI *(hp.corners.toList().get(i).x/facesArray[0].width))));
		//modelPoints.toList().set(i, hpmodel(hp.corners.toList().get(i).x/facesArray[0].width,hp.corners.toList().get(i).x/facesArray[0].height));
		//temp1=new Point3((hp.corners.toList().get(i).x/facesArray[0].width) - 0.5,-(hp.corners.toList().get(i).y/facesArray[0].height) + 0.5, 0.5 * Math.sin(PI *(hp.corners.toList().get(i).x/facesArray[0].width)));
		//modelPoints. .p  .toList().set .set(i,temp1);
		
		}
		modelPoints.fromList(points);
		
		}
	
		//imran example from marker.java , search for Point3f	
			
		// Traslate corners from face coordinated to image coordinates
		for(i=0;i <hp.cornerCount;i++)
		{
			if(i==hp.corners.total())
				break;
			hp.corners.toList().get(i).x+=facesArray[0].tl().x;
			hp.corners.toList().get(i).y+=facesArray[0].br().y;
			//hp.corners.toList().set(i, hp.corners.toList().get(i)+facesArray[0].tl().x);
			
		}
		hp.corners.copyTo(hp.tempcorners);// .clone();
		 // Change status
		hp.hpstatus=HeadPoseStatus.KEYFRAME;
	}
	else
	{
		if(facesArray.length>1)
		TempFace=facesArray.clone();//imran assigning here also,to better measure 
		
		MatOfPoint2f corners2f=new MatOfPoint2f();
		hp.corners.convertTo(corners2f, CvType.CV_32FC2);
		hp.previousCorners=corners2f;
		corners2f.convertTo(hp.corners,CvType.CV_32S );	
		
		hpTrack(mRgba,hp,facesArray);
		Point center = new Point();
		
		if (hp.cornerCount < 4) 
		{
			hp.hpstatus=HeadPoseStatus.NONE;
		    return;
		}
		
		
		hp.hpstatus=HeadPoseStatus.TRACKING;
		
	}
	if (hp.previousFrame==null)//imran
	{
		//hp.previousFrame =new Mat(mRgba.width(),mRgba.height(),CvType);
		hp.previousFrame=new Mat(mRgba.size(),CvType.CV_8UC4);
		
	 }
	mRgba.copyTo(hp.previousFrame);
	//cvCopy(frame, headPose->previousFrame, NULL);
	 
	 
}
//http://stackoverflow.com/questions/13234968/android-opticalflow-calcopticalflowpyrlk-goodfeaturestotrack-return-same-po
//http://www.jayrambhia.com/blog/2012/08/08/lucas-kanade-tracker/
//http://docs.opencv.org/java/org/opencv/video/Video.html#calcOpticalFlowPyrLK%28org.opencv.core.Mat,%20org.opencv.core.Mat,%20org.opencv.core.MatOfPoint2f,%20org.opencv.core.MatOfPoint2f,%20org.opencv.core.MatOfByte,%20org.opencv.core.MatOfFloat%29
//http://opencv.willowgarage.com/documentation/c/video_motion_analysis_and_object_tracking.html
//imran compare c and android def , as they are different

void hpTrack(Mat mRgba,WorkingHeadPose hp,Rect[] facesArray)
{
	MatOfByte status = new MatOfByte();
	//Mat prev=new Mat(mRgba.width(),mRgba.height(),CvType.CV_8UC1);
	//Mat curr=new Mat(mRgba.width(),mRgba.height(),CvType.CV_8UC1);
	Mat prev=new Mat(mRgba.size(),CvType.CV_8UC1);
	Mat curr=new Mat(mRgba.size(),CvType.CV_8UC1);
	MatOfPoint2f tmpCorners =new MatOfPoint2f();
	MatOfFloat err=new MatOfFloat();
	int i,j,count;
	TermCriteria optical_flow_termination_criteria=new TermCriteria();//=(TermCriteria.MAX_ITER|TermCriteria.EPS,20,.3);//  ( CV_TERMCRIT_ITER | CV_TERMCRIT_EPS, 20, .3 );
	optical_flow_termination_criteria.epsilon=.3;
	optical_flow_termination_criteria.maxCount =20;
	// Good features to track
	Imgproc.cvtColor(hp.previousFrame, prev, Imgproc.COLOR_RGBA2GRAY,0);
	Imgproc.cvtColor(mRgba, curr, Imgproc.COLOR_RGBA2GRAY,0);
	//Video.calcOpticalFlowPyrLK(prev, curr,hp.previousCorners,tmpCorners,status,new MatOfFloat(), new Size(10,10), 3,optical_flow_termination_criteria, 0, 1);
	//http://stackoverflow.com/questions/12561292/android-using-calcopticalflowpyrlk-with-matofpoint2f
	if(hp.previousCorners.total()>0 )
	//Video.calcOpticalFlowPyrLK(prev, curr,hp.previousCorners,tmpCorners,status,new MatOfFloat(), new Size(11,11),5,optical_flow_termination_criteria, 0, 1);
	Video.calcOpticalFlowPyrLK(prev, curr, hp.previousCorners, tmpCorners, status, err);
	
	
	/*Point[] pointp = hp.previousCorners.toArray();
	Point[] pointn = tmpCorners.toArray();
	for (Point px : pointp) 
	{ Core.circle(mRgba, px, 15, new Scalar(255,0,0)); }
	for (Point py : pointn) 
	{ Core.circle(mRgba, py, 5, new Scalar(0,0,255)); }
	*/
	
	
	
	
	//Point a,b;
	//a=new Point();
	//b=new Point();
	/*if(TempFace.length>0)
	{
	
	for( i = 0; i < hp.tempcorners.total(); i++ )
	{ 
		//center.x=facearray1[0].x + hp.corners.toList().get(i).x;
		//center.y=facearray1[0].y + hp.corners.toList().get(i).y;
		Point a = new Point(TempFace[0].tl().x+hp.tempcorners.toList().get(i).x,TempFace[0].tl().x+hp.tempcorners.toList().get(i).y);		
		Point b= new Point(TempFace[0].tl().y+tmpCorners.toList().get(i).x,TempFace[0].tl().y+tmpCorners.toList().get(i).y);
		Core.line(mRgba, a,b, new Scalar(255,0,0),2);
	}
	}
*/
	count = 0;
	  for (i = 0; i < hp.cornerCount; i += 1) 
	  {
		  if(i==hp.corners.total())
				break;
		  if (status.toList().get(i) == 1) 
	    {
	      count += 1;
	    }
	  }
	  
	// Replace headPose->corners and headPose->modelPoints
	  //imran
	  //http://stackoverflow.com/questions/11273588/how-to-convert-matofpoint-to-matofpoint2f-in-opencv-java-api
	  
	  MatOfPoint2f corners2f=new MatOfPoint2f();
	  hp.corners.convertTo(corners2f, CvType.CV_32FC2);
	  List<Point> plist = new ArrayList<Point>();
	  
	  for (i = 0,j=0; i < hp.cornerCount; i += 1) 
	  {
		  if(i==hp.corners.total())
				break;
		 if (status.toList().get(i) == 1) 
	    {
	      
	    	//plist.add(tmpCorners.toList().get(i));
	    	corners2f.toList().set(j, tmpCorners.toList().get(i));// .get(i)=tmpCorners.toList().get(i);
	    	hp.modelPoints.toList().set(j, hp.modelPoints.toList().get(i));
	    	// =hp.modelPoints.toList().get(i);
	    	
	    	//hp.corners[j] = tmpCorners[i];
	      //headPose->modelPoints[j] = headPose->modelPoints[i];
	      j += 1;
	    }
	  }
	  //corners2f.fromList(plist);
	  corners2f.convertTo(hp.corners,CvType.CV_32S );
	  Log.i("CournerCount","Reassigning"+count);
	  hp.cornerCount = count;

}





//http://dasl.mem.drexel.edu/~noahKuntz/openCVTut9.html
//http://www.pages.drexel.edu/~nk752/tutorials.html

public void hpFindCorners(Mat cropped,WorkingHeadPose hp)
{
	/// Parameters for Shi-Tomasi algorithm
	double qualityLevel = 0.01;
	double minDistance = 10;
	int blockSize = 3;
	boolean useHarrisDetector = false;
	double k = 0.04; 
	//Mat copy;
	//copy=cropped.clone();
	
	//Mat faceG=new Mat(cropped.width(),cropped.height(),CvType.CV_8UC1);
	//Mat eigImage=new Mat(cropped.width(),cropped.height(),CvType.CV_32FC1);
	//Mat tmpImage=new Mat(cropped.width(),cropped.height(),CvType.CV_32FC1);
	
	Imgproc.goodFeaturesToTrack(cropped, hp.corners,hp.cornerCount, qualityLevel, minDistance, new Mat(), blockSize, useHarrisDetector, k);
	//int r = 4;
	//https://groups.google.com/forum/?fromgroups=#!topic/android-opencv/7oYUPUin6bE
	
	//Mat grayRnd = new Mat(10,10, CvType.CV_8U);
    //Core.randu(grayRnd, 0, 256);
	Log.i("HeadPose","FindCorner:Total Corners Found"+hp.corners.total());
	//displaying good features in FdView
	//imran
	//for( int i = 0; i < hp.corners.total(); i++ )
	//{ Core.circle( copy, hp.corners.toList().get(i), r, new Scalar(255,0,0), -1, 8, 0 ); }

}

public Point3 hpmodel(double x,double y)
{
	
	return new Point3(x - 0.5, -y + 0.5, 0.5 * Math.sin(PI *x));

}

}
