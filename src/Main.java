
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import pkg1.*;
import qrStorage.QRHashStorage;
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
				"testPhotos/1.jpg"
				//"testPhotos/2.jpg",
				//"testPhotos/3.jpg",
				//"testPhotos/fake/1.jpg",
				//"testPhotos/fake/2.jpg"
				//"testPhotos/4.jpg",
				//"testPhotos/5.jpg",
				//"testPhotos/6.jpg"
		);
		
		List<Mat> infoParts = new ArrayList<>();
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
				ex.printStackTrace();
				System.err.println(ex.getMessage());
				continue;
			}
			
			ImageFuzzyHash hasher = new ImageFuzzyHash(infoPart);
			
			ImageFuzzyHashSum hash = hasher.getImageFuzzyHash();
			infoParts.add(infoPart);
			hashes.add(hash);
			hash.verbose();
			
			

			QRHashStorage qrHashStorage = new QRHashStorage(hash.toString(), "");
			List<BufferedImage> images = qrHashStorage.generateQRCodes();
			int i = 0;
			for(final BufferedImage bufferedImage : images){
				ImageIO.write(bufferedImage, "png", new File("./qrcode" + i++ + ".png"));
			}
		}
		
		assert hashes.size() == infoParts.size();
		
		for(int first = 0; first < hashes.size() - 1; ++first){
			for(int second = first + 1; second < hashes.size(); ++second){
				ImageFuzzyHashSum hash1 = hashes.get(first);
				ImageFuzzyHashSum hash2 = hashes.get(second);
				
				ImageFuzzyHashSum hash2_restored = ImageFuzzyHashSum.fromString(hash2.toString());
				
				HashCompareResult res = hash1.compare(hash2_restored);
				System.out.println(hash2_restored.toString());
				System.out.printf("%d <=> %d : %s\n", first, second, res.isGenuine());
				
				Mat withErrors = res.drawIncorrect(infoParts.get(first));
				Mat withAll = res.drawCorrect(withErrors);
				Imgcodecs.imwrite("withErrors" + first + "-" + second + ".png", withAll);
			}
		}
		
		
		System.out.println("done.");
	}
		
	
	public static void subMain() throws Exception{
		Main.mainTest();
		
		
		
		
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