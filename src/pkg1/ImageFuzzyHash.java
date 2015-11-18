package pkg1;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageFuzzyHash{
	protected Mat infoPart;
	protected String fuzzyHash;
	
	public ImageFuzzyHash(Mat srcImage) throws Exception{
		if(srcImage == null)
			throw new Exception("srcIamge should not be null");
		
		Mat bnw = new Mat();
		Imgproc.cvtColor(srcImage, bnw, Imgproc.COLOR_BGR2GRAY);
		
		Mat infoResized = new Mat();
		Imgproc.resize(bnw, infoResized, new Size(1000, 1000));
		
		Mat thresholded = new Mat();
		Imgproc.threshold(infoResized, thresholded, 110, 255, Imgproc.THRESH_BINARY_INV);
		
		this.infoPart = thresholded;
	}
	
	public void generateLevels(){
		Mat previous = this.infoPart.clone();
		int level = 0;
		while(true){
			Mat bg = this.infoPart.clone();
			Mat current = new Mat();
			Imgproc.dilate(previous, current, new Mat());
			
			List<MatOfPoint> contours = new ArrayList<>();
			Imgproc.findContours(current, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
			if(contours.size() <= 1)
				break;
			
			for(int i = 0; i < contours.size(); ++i){
				MatOfPoint currentContour = contours.get(i);
				MatOfPoint2f current2f = new MatOfPoint2f(currentContour.toArray());
				MatOfPoint2f approxCurve2f = new MatOfPoint2f();
				
				Imgproc.approxPolyDP(current2f, approxCurve2f, Imgproc.arcLength(current2f, true) * 0.02, true);
				Rect rect = Imgproc.boundingRect(new MatOfPoint(approxCurve2f.toArray()));
				Imgproc.rectangle(current, rect.tl(), rect.br(), new Scalar(128, 128, 0));
				Imgproc.rectangle(bg, rect.tl(), rect.br(), new Scalar(128, 128, 0));
			}
			
			previous = current;
			Imgcodecs.imwrite("levels/" + (level++) + ".png", current);
			Imgcodecs.imwrite("levels/bg" + (level++) + ".png", bg);
		}
	}
	
}
