package com.kaist.ninjas.cs408ninjas;

import android.util.Log;
import android.util.Pair;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FrameProcessor {

    public static String TAG = "FRAME PROCESSOR";
    public static void test(String inDir, String outDir){
        String inputFileName="image";
        String inputExtension = "jpg";
        String outputExtension = "png";
        String inputFilePath = inDir + File.separator + inputFileName + "." + inputExtension;

        Log.d (TAG, "loading " + inputFilePath + "...");
        Mat image = Imgcodecs.imread(inputFilePath);
        Log.d (TAG, "width of " + inputFileName + ": " + image.width());
        // if width is 0 then it did not read your image.


        // for the canny edge detection algorithm, play with these to see different results
        int threshold1 = 70;
        int threshold2 = 100;

        Mat im_canny = new Mat();  // you have to initialize output image before giving it to the Canny method
        Imgproc.Canny(image, im_canny, threshold1, threshold2);
        String cannyFilename = outDir + File.separator + inputFileName + "_canny-" + threshold1 + "-" + threshold2 + "." + outputExtension;
        Log.d (TAG, "Writing " + cannyFilename);
        Imgcodecs.imwrite(cannyFilename, im_canny);
    }

    public static void convertColor(Mat src, Mat dst){
        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_BGR2GRAY);
    }


    public static void flip(Mat src, Mat dst){
        flip(src, dst);
    }

    public static MatOfInt getHull(MatOfPoint contour){
        MatOfInt hull = new MatOfInt();
        Imgproc.convexHull(contour, hull);
        return hull;
    }

    public static MatOfPoint getMaxContour(Mat src){
        Mat dst = new Mat(src.size(), CvType.CV_8UC3);
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(dst, dst, 0, 255, 0);

        // Get contours
        Mat _ = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(dst, contours, _,Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        _.release();

        int maxIndex = 0;
        double maxArea = 0;
        for(int i=0;i<contours.size();i++){
            MatOfPoint cnt = contours.get(i);
            double area = Imgproc.contourArea(cnt);
            if(area>maxArea){
                maxIndex = i;
                maxArea = area;
            }
        }
        return contours.get(maxIndex);
    }

    public MatOfInt4 getDefects(MatOfPoint contour){
        MatOfInt hull = new MatOfInt();
        MatOfInt4 defect = new MatOfInt4();
        Imgproc.convexHull(contour, hull);
        if (hull != null /*and len(hull > 3) and len(contour) > 3*/){
             Imgproc.convexityDefects(contour, hull, defect);
             return defect;
        }
        return null;
    }

    public Pair getCentroid(MatOfPoint contour){
        Moments moments = Imgproc.moments(contour);
        if(moments.m00 != 0){
            int cx = (int) (moments.m10/moments.m00);
            int cy = (int) (moments.m01/moments.m00);
            return new Pair(cx, cy);
        }
        return null;
    }

    public void getContourInterior(Mat src, MatOfPoint contour){
        RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
        Mat box = new Mat();
        Imgproc.boxPoints(rect, box);
        int rows = src.width();
        int cols = src.height();

    }


    public static Mat getHandHist (Mat frame){
        Size size = frame.size();
        Mat handHistImage = new Mat(30, 30, frame.channels());

        for(int i = 0; i<9; i++){
            int x = (9+(i%3))*(int) size.width/20;
            int y = (6+4*(i/3))*(int) size.height/20;
            Rect roi = new Rect(x, y, 10, 10);
            Rect dstRange = new Rect((i%3)*10,(i/3)*10, 10, 10);
            frame.submat(roi).copyTo(handHistImage.submat(dstRange));
        }

        Mat hist = new Mat();
        Imgproc.cvtColor(handHistImage, handHistImage, Imgproc.COLOR_BGR2HSV);
        Imgproc.calcHist(Arrays.asList(handHistImage), new MatOfInt(0,1),null, hist, new MatOfInt(180,256), new MatOfFloat(0, 180, 0, 256)  );
        Core.normalize(hist, hist, 0, 255, Core.NORM_MINMAX);

        return hist;
    }

}
