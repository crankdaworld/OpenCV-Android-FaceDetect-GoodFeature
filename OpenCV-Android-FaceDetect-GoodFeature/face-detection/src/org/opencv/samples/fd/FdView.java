/**
* Copyright (C) 2013 Imran Akthar (www.imranakthar.com)
* imran@imranakthar.com
*/

package org.opencv.samples.fd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.samples.fd.HeadPose.HeadPoseStatus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

class FdView extends SampleCvViewBase {
    private static final String   TAG = "Sample::FdView";
    private Mat                   mRgba,mHsv;
    private Mat                   mGray;
    private File                  mCascadeFile;
    private CascadeClassifier     mJavaDetector;
    private DetectionBasedTracker mNativeDetector;

    private static final Scalar   FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    
    public static final int       JAVA_DETECTOR     = 0;
    public static final int       NATIVE_DETECTOR   = 1;
    
    private int                   mDetectorType     = NATIVE_DETECTOR;

    private float                 mRelativeFaceSize = 0;
    private int					  mAbsoluteFaceSize = 0;
    public int isHeadPoseOn=0;
    
    HeadPose hp;//imran
    Rect[] facearray1;
    
    
    public void setMinFaceSize(float faceSize)
    {
		mRelativeFaceSize = faceSize;
		mAbsoluteFaceSize = 0;
    }
    
    public void setDetectorType(int type)
    {
    	if (mDetectorType != type)
    	{
    		mDetectorType = type;
    		
    		if (type == NATIVE_DETECTOR)
    		{
    			Log.i(TAG, "Detection Based Tracker enabled");
    			mNativeDetector.start();
    		}
    		else
    		{
    			Log.i(TAG, "Cascade detector enabled");
    			mNativeDetector.stop();
    		}
    	}
    }

    public FdView(Context context) {
        super(context);

        try {
            hp=new HeadPose();//imran
        	InputStream is = context.getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            if (mJavaDetector.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                mJavaDetector = null;
            } else
                Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

            mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);
            
            cascadeDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
    }

    @Override
	public void surfaceCreated(SurfaceHolder holder) {
        synchronized (this) {
            // initialize Mats before usage
            mGray = new Mat();
            mRgba = new Mat();
            mHsv = new Mat();
        }

        super.surfaceCreated(holder);
	}

    
    public Rect[] FaceDetect(Mat mRgba,Mat mGray )
    {
    	if (mAbsoluteFaceSize == 0)
        {
        	int height = mGray.rows();
        	if (Math.round(height * mRelativeFaceSize) > 0);
        	{
        		mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
        	}
        	mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }
        
        MatOfRect faces = new MatOfRect();
        
        if (mDetectorType == JAVA_DETECTOR)
        {
        	if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2 // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        , new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        else if (mDetectorType == NATIVE_DETECTOR)
        {
        	if (mNativeDetector != null)
        		mNativeDetector.detect(mGray, faces);
        }
        else
        {
        	Log.e(TAG, "Detection method is not selected!");
        }
        
        Rect[] facesArray = faces.toArray();
        /*for (int i = 0; i < facesArray.length; i++)
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);*/
        
       
    	return facesArray;
    	
    }
    @Override
    public String getstatus()
    {
    	
    	return hp.hpstatus.toString();
    	
    }
    
    
	@Override
    protected Bitmap processFrame(VideoCapture capture) {
        capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
        capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);
        //Imgproc.cvtColor(mRgba,mHsv,Imgproc.COLOR_RGB2HSV);//imran adding for camshift
       // facearray1=FaceDetect(mRgba,mGray);
       // if(facearray1.length>1)
       // hp.CamShift(mHsv,facearray1);
        
    	Log.i("Headposestatus","HP STatus"+hp.hpstatus);
       if(isHeadPoseOn==1)
        {
    	
    	if(hp.hpstatus==HeadPoseStatus.NONE)
    	{
        facearray1=FaceDetect(mRgba,mGray);
    	}
       // for (int i = 0; i < facearray1.length; i++)
          //  Core.rectangle(mRgba, facearray1[i].tl(), facearray1[i].br(), FACE_RECT_COLOR, 3);
        hp.hpFind(mRgba,mGray,hp,facearray1);
        if(hp.hpstatus==HeadPoseStatus.TRACKING)
        {
        Point center = new Point();
        int r = 4;
        Log.i("HeadPose","FindCorner:Total Corners Found"+hp.features_next.total());
        for( int i = 0; i < hp.features_next.total()-1; i++ )
    	{ 
    		//center.x=facearray1[0].x + hp.corners.toList().get(i).x;
    		//center.y=facearray1[0].y + hp.corners.toList().get(i).y;
    		//center.x=hp.TempFace[0].x+hp.features_next.toList().get(i).x;
    		//center.y=hp.TempFace[0].y+hp.features_next.toList().get(i).y;
    		//Core.circle( mRgba, center, r, new Scalar(255,0,0), -1, 8, 0 );
    	}
    	
        }
        }
        
        
        Bitmap bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);

        try 
        {
        	Utils.matToBitmap(mRgba, bmp);
        } catch(Exception e) {
        	Log.e(TAG, "Utils.matToBitmap() throws an exception: " + e.getMessage());
            bmp.recycle();
            bmp = null;
        }
        
        return bmp;
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
            if (mCascadeFile != null)
            	mCascadeFile.delete();
            if (mNativeDetector != null)
            	mNativeDetector.release();

            mRgba = null;
            mGray = null;
            mCascadeFile = null;
        }
    }
}
