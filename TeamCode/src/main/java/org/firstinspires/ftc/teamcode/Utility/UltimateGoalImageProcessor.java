package org.firstinspires.ftc.teamcode.Utility;

import android.os.Environment;

import com.acmerobotics.dashboard.config.Config;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import static java.lang.Thread.sleep;

//import org.opencv.core.*;
//import org.opencv.imgproc.*;

/**
 * GripPipeline class.
 *
 * <p>An OpenCV pipeline generated by GRIP.
 *
 * @author GRIP
 */

@Config
public class UltimateGoalImageProcessor {

    private static UltimateGoalImageProcessor processor = new UltimateGoalImageProcessor();
    public static UltimateGoalImageProcessor getInstance(){
        return processor;
    }

    private int sizeThresh = 70000;
    private int lrTolerance = 10;
    public static double blurAmount = 10;


    static {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public UltimateGoalImageProcessor() {
    }

    public ImageResult process(Mat source0) {

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String filename = "Orig.jpg";
        File file = new File(path, filename);
        Imgcodecs.imwrite(file.toString(), source0);

        Mat gray = new Mat();
        Mat canny = new Mat();
        Imgproc.cvtColor(source0, gray, Imgproc.COLOR_BGR2GRAY); //convert roi into gray
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        filename = "gray.jpg";
        file = new File(path, filename);
        Imgcodecs.imwrite(file.toString(), gray);

        //Crop the image
        Mat cropInput = gray;
        Mat cropOutput = new Mat();
        int widthLeft = (int) Math.round(source0.width() * 0.59);
        int widthRight = (int) Math.round(source0.width() * 0.78);
        int heightDown = (int) Math.round(source0.height() * 0.23);
        int heightUp = (int) Math.round(source0.height() * 0.64);


        cropOutput = cropInput.submat(heightDown,heightUp, widthLeft, widthRight);


        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        filename = "Crop.jpg";
        file = new File(path, filename);
        Imgcodecs.imwrite(file.toString(), cropOutput);

        //Rotate the Image
        Mat rotateInput = cropOutput;
        Mat rotateOutput = new Mat();

        Core.rotate(rotateInput, rotateOutput, Core.ROTATE_90_CLOCKWISE);

        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        filename = "Rotate.jpg";
        file = new File(path, filename);
        Imgcodecs.imwrite(file.toString(), rotateOutput);

        // Step Blur0:®
       /* Mat blurInput = rotateOutput;
        BlurType blurType = BlurType.get("Box Blur");
        double blurRadius = blurAmount;
        Mat blurOutput = new Mat();
        blur(blurInput, blurType, blurRadius, blurOutput);
*/
/*        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        filename = "Blur.jpg";
        file = new File(path, filename);
        Imgcodecs.imwrite(file.toString(), blurOutput);*/


        //CONVERT TO HSV


/*
        // Step Blur0:®
        Mat blurInput = rotateOutput;
        BlurType blurType = BlurType.get("Box Blur");
        double blurRadius = blurAmount;
        Mat blurOutput = new Mat();
        blur(blurInput, blurType, blurRadius, blurOutput);

        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        filename = "Blur.jpg";
        file = new File(path, filename);
        Imgcodecs.imwrite(file.toString(), blurOutput);


        Imgproc.Canny(blurOutput, canny,10,50);//apply canny to roi
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        filename = "canny.jpg";
        file = new File(path, filename);
        Imgcodecs.imwrite(file.toString(), canny);
*/

        //Convert to black and white
        Mat bwInput = rotateOutput;
        Mat bwOutput = new Mat();

        desaturate(bwInput, bwOutput);

        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        filename = "Bw.jpg";
        file = new File(path, filename);
        Imgcodecs.imwrite(file.toString(), bwOutput);

        ImageResult imageResult = new ImageResult();

        return imageResult;

    }

    public int getValueBySide(boolean left, Mat inputMat){
        int imgWidth = inputMat.width();
        int imgHeight = inputMat.height();
        double sum = 0;
        int offset = left ? 0: imgWidth/2;
        int count = 0;


        for(int rows = 0; rows < imgHeight; rows += 5){
            for(int cols = offset; cols < ((imgWidth/2) + offset); cols += 5){
                sum += inputMat.get(rows,cols)[0];
                count ++;
            }
        }

        return (int)(sum/count);


    }

    /**
     * An indication of which type of filter to use for a blur.
     * Choices are BOX, GAUSSIAN, MEDIAN, and BILATERAL
     */
    enum BlurType{
        BOX("Box Blur"), GAUSSIAN("Gaussian Blur"), MEDIAN("Median Filter"),
        BILATERAL("Bilateral Filter");

        private final String label;

        BlurType(String label) {
            this.label = label;
        }

        public static BlurType get(String type) {
            if (BILATERAL.label.equals(type)) {
                return BILATERAL;
            }
            else if (GAUSSIAN.label.equals(type)) {
                return GAUSSIAN;
            }
            else if (MEDIAN.label.equals(type)) {
                return MEDIAN;
            }
            else {
                return BOX;
            }
        }

        @Override
        public String toString() {
            return this.label;
        }
    }

    /**
     * Softens an image using one of several filters.
     * @param input The image on which to perform the blur.
     * @param type The blurType to perform.
     * @param doubleRadius The radius for the blur.
     * @param output The image in which to store the output.
     */
    private void blur(Mat input, BlurType type, double doubleRadius,
                      Mat output) {
        int radius = (int)(doubleRadius + 0.5);
        int kernelSize;
        switch(type){
            case BOX:
                kernelSize = 2 * radius + 1;
                Imgproc.blur(input, output, new Size(kernelSize, kernelSize));
                break;
            case GAUSSIAN:
                kernelSize = 6 * radius + 1;
                Imgproc.GaussianBlur(input,output, new Size(kernelSize, kernelSize), radius);
                break;
            case MEDIAN:
                kernelSize = 2 * radius + 1;
                Imgproc.medianBlur(input, output, kernelSize);
                break;
            case BILATERAL:
                Imgproc.bilateralFilter(input, output, -1, radius, radius);
                break;
        }
    }

    private void desaturate(Mat input, Mat output) {
        switch (input.channels()) {
            case 1:
                // If the input is already one channel, it's already desaturated
                input.copyTo(output);
                break;
            case 3:
                Imgproc.cvtColor(input, output, Imgproc.COLOR_BGR2GRAY);
                break;
            case 4:
                Imgproc.cvtColor(input, output, Imgproc.COLOR_BGRA2GRAY);
                break;
            default:
                throw new IllegalArgumentException("Input to desaturate must have 1, 3, or 4 channels");
        }
    }

}
