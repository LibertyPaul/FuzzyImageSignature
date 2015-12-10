
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import pkg1.*;
import ssdeep.InMemorySsdeep;
import ssdeep.ssdeep;

/*
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
*/
public class Main{
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public static void mainTest() throws Exception{
		List<String> testImages = Arrays.asList(
				"testPhotos/1.jpg",
				"testPhotos/2.jpg",
				"testPhotos/3.jpg",
				"testPhotos/4.jpg",
				"testPhotos/5.jpg",
				"testPhotos/6.jpg"
		);
		
		List<ImageFuzzyHashSum> hashes = new ArrayList<>();
		
		for(String fName : testImages){
			ImageRecognizer ir = new ImageRecognizer(fName);
			
			Mat infoPart = null;
			Mat codePart = null;
			try{
				infoPart = ir.getInfoPart();
				Imgcodecs.imwrite("9.infoPart.bmp", infoPart);
				codePart = ir.getCodePart();
				Imgcodecs.imwrite("10.codePart.bmp", codePart);
			}
			catch(Exception ex){
				System.err.println(ex.getMessage());
				continue;
			}
			
			ImageFuzzyHash hasher = new ImageFuzzyHash(infoPart);
			
			ImageFuzzyHashSum hash = hasher.getImageFuzzyHash();
			hashes.add(hash);
			System.out.println(hash.toString());
			
		}
		
		
		
		
		System.out.println("done.");
	}
	

	
	public static void getCharImages(){
		ImageRecognizer ir = new ImageRecognizer("testPhotos/1.jpg");
		Mat infoPart = null;
		Mat codePart = null;
		
		try{
			infoPart = ir.getInfoPart();
			Imgcodecs.imwrite("9.infoPart.jpg", infoPart);
			codePart = ir.getCodePart();
			Imgcodecs.imwrite("10.codePart.jpg", codePart);
		}
		catch(Exception ex){
			System.err.println(ex.getMessage());
			return;
		}
		
		ImageFuzzyHash hasher = new ImageFuzzyHash(infoPart);
		hasher.dumpChars(new File("chars/"));
		hasher.dumpUnique(new File("uniqueChars/"));
		
	}
	
	public static void testLinePage(){
		ImageRecognizer ir = new ImageRecognizer("testPhotos/1.jpg");
		Mat infoPart = null;
		Mat codePart = null;
		
		try{
			infoPart = ir.getInfoPart();
			Imgcodecs.imwrite("9.infoPart.jpg", infoPart);
			codePart = ir.getCodePart();
			Imgcodecs.imwrite("10.codePart.jpg", codePart);
		}
		catch(Exception ex){
			System.err.println(ex.getMessage());
			return;
		}
		
		ImageFuzzyHash hasher = new ImageFuzzyHash(infoPart);
		CharactersStatistics cs = hasher.getCharactersStatistics();
		LinePage lp = cs.createLinePage();
		Mat withLines = lp.drawLines(infoPart);
		Imgcodecs.imwrite("15.WithLines.png", withLines);
	}
	
	public static ImageFuzzyHashSum getHash(String path) throws Exception{
		ImageRecognizer ir = new ImageRecognizer(path);
		
		Mat infoPart = ir.getInfoPart();
		Imgcodecs.imwrite("9.infoPart.jpg", infoPart);
		Mat codePart = ir.getCodePart();
		Imgcodecs.imwrite("10.codePart.jpg", codePart);
		
		ImageFuzzyHash hasher = new ImageFuzzyHash(infoPart);
		return hasher.getImageFuzzyHash();
	}
	
	public static void testHash() throws Exception{
		ImageFuzzyHashSum sum1 = getHash("testPhotos/3.jpg");
		ImageFuzzyHashSum sum2 = getHash("testPhotos/2.jpg");
		
		System.out.println(sum1.toString());
		System.out.println(sum2.toString());
	}
	
	public static void subMain() throws Exception{
		//EuclidianDistance.test();
		//Main.mainTest();
		//Main.getCharImages();
		//Main.testLinePage();
		Main.testHash();
		
	}
	
	
	
	
	
	
	
	
	
	public static void main(String[] args){
		long startTime = System.currentTimeMillis();
		
		try{
			Main.subMain();
		}
		catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long stopTime = System.currentTimeMillis();
		double timeMiliSec = stopTime - startTime;
		System.out.println("Time elapsed: " + timeMiliSec / 1000);
	}

}