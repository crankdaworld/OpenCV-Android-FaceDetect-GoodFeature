//http://gtcmt-android-swarmbot.googlecode.com/svn-history/r61/trunk/Demokit2/src/com/google/android/DemoKit/CtView.java
//imran
package com.google.android.DemoKit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
//import java.util.Map;

import org.opencv.android.*;
import org.opencv.utils.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.video.*;
import org.opencv.imgproc.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnTouchListener;

class CtView extends SampleCvViewBase implements OnTouchListener{
    private static final String TAG = "Sample::CtView";
    private Mat                 mRgba;
    private Mat                 mGray;
    private Mat mHSV;
    private Mat test;
    private Mat hist;
    private Mat dst;

    private Mat mRgbaInnerWindow;
    private Mat mHSVInnerWindow;
    RotatedRect r;
    
    private CascadeClassifier   mCascade;
    Objdetect obj;
    
    public float minFaceSize = 0.2f;
    
    Rect samp=null;
    Rect ret;
    Rect searchWindow,searchWindow2;
    double[] sampVals;
	private boolean drawRect, histstored;
	
	List<Mat>  matlist,matlist2;
	List<Integer> integerlist,integerlist2;
	List<Float> floatlist;
	TermCriteria criteria;
	
	float[] tempdata=null;
    
    public CtView(Context context) {
        super(context);
        
        this.setOnTouchListener(this);
        sampVals = new double[4];      
        initialiseLists();
        
        try {
            InputStream is = context.getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(cascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            mCascade = new CascadeClassifier(cascadeFile.getAbsolutePath());
            if (mCascade.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                mCascade = null;
            } else
                Log.i(TAG, "Loaded cascade classifier from " + cascadeFile.getAbsolutePath());

            cascadeFile.delete();
            cascadeDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
    }
    
    public CtView(Context context, AttributeSet attrs, int defStyle)
    {
    	super(context,attrs, defStyle);
    	this.setOnTouchListener(this);
        sampVals = new double[4];    
        initialiseLists();
        /*java.util.List<Mat> images = null;
        java.util.List<Integer> channels = null;
        Mat hist = null;
        Mat dst = null;
        java.util.List<Float> ranges = null;
        double scale = 0;
        Imgproc.calcBackProject( images,  channels,  hist,  dst, ranges,  scale);        
        Mat probImage = null;
        Rect window = null;
        TermCriteria criteria = null;
        RotatedRect r = Video.CamShift(probImage, window,  criteria);
        */
        try {
            InputStream is = context.getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(cascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            mCascade = new CascadeClassifier(cascadeFile.getAbsolutePath());
            if (mCascade.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                mCascade = null;
            } else
                Log.i(TAG, "Loaded cascade classifier from " + cascadeFile.getAbsolutePath());

            cascadeFile.delete();
            cascadeDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
    }
    
    public CtView(Context context, AttributeSet attrs)
    {
    	super(context,attrs);
    	this.setOnTouchListener(this);
        sampVals = new double[4];       
        initialiseLists();
        /*java.util.List<Mat> images = null;
        java.util.List<Integer> channels = null;
        Mat hist = null;
        Mat dst = null;
        java.util.List<Float> ranges = null;
        double scale = 0;
        Imgproc.calcBackProject( images,  channels,  hist,  dst, ranges,  scale);        
        Mat probImage = null;
        Rect window = null;
        TermCriteria criteria = null;
        RotatedRect r = Video.CamShift(probImage, window,  criteria);
        */
        try {
            InputStream is = context.getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(cascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            mCascade = new CascadeClassifier(cascadeFile.getAbsolutePath());
            if (mCascade.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                mCascade = null;
            } else
                Log.i(TAG, "Loaded cascade classifier from " + cascadeFile.getAbsolutePath());

            cascadeFile.delete();
            cascadeDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        super.surfaceChanged(_holder, format, width, height);

        synchronized (this) {
            // initialize Mats before usage
            mGray = new Mat();
            mRgba = new Mat();
            mHSV = new Mat();
            hist = new Mat();
            dst = new Mat();
            
            criteria = new TermCriteria(1,10,1);
        }
    }

    @Override
    protected Bitmap processFrame(VideoCapture capture) {
        capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
        capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);        
        Imgproc.cvtColor(mRgba,mHSV,Imgproc.COLOR_RGB2HSV);
        
        if(drawRect)
        {
        	Core.rectangle(mRgba, samp.tl(), samp.br(), new Scalar(0, 255, 0, 255), 3);
        	//Imgproc.calcBackProject(mRgba, 1, hist, dst, ranges, scale);
        	if(mRgbaInnerWindow!=null)
        	{
        		//Imgproc.blur(mRgbaInnerWindow, mRgbaInnerWindow, new Size(15, 15));
        	}
        	
        	if(histstored)
        	{
        		for(int i=0; i < hist.size().height;i++)
        		{
        			
        			//Log.i(TAG,"i: "+i);
        			hist.get(1, i, tempdata);
        			Rect rr = new Rect();
        			double[] vals = new double[4];
        			vals[0]=2*i;
        			vals[1]= 1;
        			vals[2]= .5;
        			vals[3]= tempdata[1];
        			
        			rr.set(vals);
        			Core.rectangle(mRgba, rr.tl(), rr.br(), new Scalar(i, 255, 0, 255), 1);
        			Log.i(TAG,"tempdata: " + tempdata[1]);
        		}
        		matlist2.clear();
        		matlist2.add(mHSV);
        		Imgproc.calcBackProject(matlist2, integerlist, hist, dst, floatlist,1.0 );
        		//Log.i(TAG,"dst: "+dst.size().height + "," + dst.size().width);
        		Video.meanShift(dst, searchWindow, criteria);
        		Core.rectangle(mRgba, searchWindow.tl(), searchWindow.br(), new Scalar(0, 255, 0, 0), 1);
 
        		r=Video.CamShift(dst, searchWindow2, criteria);
        		Core.rectangle(mRgba, searchWindow2.tl(), searchWindow2.br(), new Scalar(0, 0, 255, 0), 1);
        		
        		//samp=r.boundingRect();
        	}
        }
        
        if (mCascade != null) {
        	
            /*
            int height = mGray.rows();
            //int faceSize = Math.round(height * FdActivity.minFaceSize);
            int faceSize = Math.round(height * minFaceSize);
            List<Rect> faces = new LinkedList<Rect>();
            mCascade.detectMultiScale(mGray, faces, 1.1, 2, 2 // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    , new Size(faceSize, faceSize));

            for (Rect r : faces)
                Core.rectangle(mRgba, r.tl(), r.br(), new Scalar(0, 255, 0, 255), 3);
                */
        }

        Bitmap bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);

        if (Utils.matToBitmap(mRgba, bmp))
            return bmp;

        bmp.recycle();
        return null;
    }

    @Override
    public void run() {
        super.run();

        synchronized (this) {
            // Explicitly deallocate Mats
            if (mRgba != null)
                mRgba.release();
            if (mGray != null)
                mGray.release();
            if (mHSV != null)
            	mHSV.release();

            mRgba = null;
            mGray = null;
            mHSV = null;
        }
    }

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		// TODO Auto-generated method stub
		
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			//start rect point
			Rect r;
			//double[] vals = {event.getX(),event.getY(),20,20};
			sampVals[0]=event.getX();
			sampVals[1]= event.getY();
			sampVals[2]= 20;
			sampVals[3]= 20;
			samp =new Rect();
			samp.set(sampVals);
			drawRect=false;
			
		}
		
		if(event.getAction() == MotionEvent.ACTION_UP)
		{
			sampVals[2]= event.getX()-sampVals[0];
			sampVals[3]= event.getY()-sampVals[1];			
			samp.set(sampVals);
			
			//stop rect point , draw rect and use image
			
			Core.rectangle(mRgba, samp.tl(), samp.br(), new Scalar(0, 255, 0, 255), 3);
			
			
			int rowStart = (int)sampVals[1];
			int rowEnd = (int) event.getY();
			int colStart= (int)sampVals[0];
			int colEnd=(int) event.getX();
			
			if(rowEnd<rowStart)
			{
				int temp=rowEnd;
				rowEnd=rowStart;
				rowStart=temp;
			}
			if(colEnd<colStart)
			{
				int temp=colEnd;
				colEnd=colStart;
				colStart=temp;
			}			
			if(rowEnd==rowStart)
			{
				rowEnd+=1;
			}
			if(colEnd==colStart)
			{
				colEnd+=1;
			}
			
			mRgbaInnerWindow = mRgba.submat(rowStart, rowEnd, colStart, colEnd); 	
			mHSVInnerWindow  = mHSV.submat(rowStart, rowEnd, colStart, colEnd);
					
			clearLists();
			//matlist.add(mRgbaInnerWindow);
			matlist.add(mHSVInnerWindow);
			integerlist.add(new Integer(1));
			integerlist2.add(new Integer(256));
			floatlist.add(new Float(0.0f));
			floatlist.add(new Float(255.0f));
			
			
			Imgproc.calcHist(matlist, integerlist, new Mat(), hist, integerlist2, floatlist);
			histstored=true;
			drawRect=true;
			
			double[] nn= new double[4];
			nn[0]=rowStart;
			nn[1]=colStart;
			nn[3]=rowEnd-rowStart;
			nn[2]=colEnd-colStart;
			searchWindow = new Rect(nn);
			searchWindow2 = new Rect(nn);
			//double[] nn2 = new double[5];
			//nn2[0]=
			r = new RotatedRect(new Point((rowEnd-rowStart)/2,(colEnd-colStart)/2), new Size(nn[3],nn[2]), 0.0);
			
			Log.i(TAG,"hst "+hist.size().height + "," + hist.size().width);
			
			
			
		}
		
		if(event.getAction() == MotionEvent.ACTION_MOVE)
		{
			sampVals[2]= event.getX()-sampVals[0];
			sampVals[3]= event.getY()-sampVals[1];			
			samp.set(sampVals);
			//expand rect point
			drawRect=true;
			
			
		}
		
		
		return true;
	}
	
	void initialiseLists()
	{
		Log.i(TAG, "lists initialized");
		 matlist = new Vector<Mat>();
		 matlist2= new Vector<Mat>();
		 integerlist = new Vector<Integer>();
		 integerlist2 = new Vector<Integer>();
		 floatlist = new Vector<Float>();
		 
		 r= new RotatedRect();
		 
		 tempdata = new float[3];
		 
		 searchWindow = new Rect();
	}
	void clearLists()
	{
		Log.i(TAG, "lists cleared");
		matlist.clear();
		integerlist.clear();
		integerlist2.clear();
		floatlist.clear();
		matlist2.clear();
	}
	
	class CTThread extends Thread
	{
		
		
	}
}