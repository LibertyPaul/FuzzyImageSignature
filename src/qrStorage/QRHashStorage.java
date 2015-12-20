package qrStorage;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.decoder.ec.ErrorCorrection;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.QRCode;

public class QRHashStorage{
	private final static int maxHashDecimalsInCode = 3000;
	private final static int maxHashQRCodes = 2;
	private final static int decimalsForId = 3;
	private final String hashValue;
	private final String digitalSignature;
	
	public QRHashStorage(final String hashValue, final String digitalSignature){
		for(final char d : hashValue.toCharArray()){
			if(d < '0' || d > '9'){
				throw new IllegalArgumentException("Only decimal characters are allowed");
			}
		}
		this.hashValue = hashValue;
		this.digitalSignature = digitalSignature;
	}
	
	private int getRequiredQRCodeCount(final int strLen){
		int count = strLen / QRHashStorage.maxHashDecimalsInCode;
		if(strLen % QRHashStorage.maxHashDecimalsInCode != 0){
			++count;
		}
		return count;
	}
	
	private static BufferedImage generateQRCode(final String value) throws WriterException, IOException{
		QRCodeWriter qrWriter = new QRCodeWriter();
		BitMatrix matrix = qrWriter.encode(value, BarcodeFormat.QR_CODE, 500, 500);
		
		BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(matrix);
		//MatrixToImageWriter.writeToPath(matrix, "png", new File("./qr.png").toPath());
		
		return qrImage;
	}
	
	private List<String> splitHashValue() throws Exception{
		final int requiredQRCodeCount = this.getRequiredQRCodeCount(this.hashValue.length());
		if(requiredQRCodeCount > QRHashStorage.maxHashQRCodes){
			throw new Exception("Can't fit hash into " + QRHashStorage.maxHashQRCodes + "QR-codes");
		}
		
		List<String> result = new ArrayList<>(requiredQRCodeCount);
		
		int maxPartSize = this.hashValue.length() / requiredQRCodeCount;
		if(this.hashValue.length() % requiredQRCodeCount != 0){
			++maxPartSize;
		}
		
		assert(maxPartSize <= QRHashStorage.maxHashDecimalsInCode);
		
		for(int i = 0; i < requiredQRCodeCount; ++i){
			final int startPos = i * maxPartSize;
			final int endPos = Math.min(startPos + maxPartSize, this.hashValue.length());
			String current = this.hashValue.substring(startPos, endPos);
			result.add(current);
		}
		
		return result;
	}
	
	private static String addId(final String hashPart, final int id, final int maxId){
		String result = "";
		result += String.format("%0" + QRHashStorage.decimalsForId + "d", id);
		result += String.format("%0" + QRHashStorage.decimalsForId + "d", maxId);
		result += hashPart;
		return result;
	}
	
	public List<BufferedImage> generateQRCodes() throws Exception{
		List<String> hashParts = this.splitHashValue();
		final int qrCodeMaxId = hashParts.size();
		
		List<BufferedImage> result = new ArrayList<BufferedImage>(qrCodeMaxId + 1);
		
		final String numberedDigitalSignature = QRHashStorage.addId(this.digitalSignature, 0, qrCodeMaxId);
		BufferedImage bufferedSignatureQR = QRHashStorage.generateQRCode(numberedDigitalSignature);
		result.add(bufferedSignatureQR);
		
		int i = 1;
		for(final String hashPart : hashParts){
			final String numberedHashPart =  QRHashStorage.addId(hashPart, i, hashParts.size() + 1);
			BufferedImage bufferedImage = QRHashStorage.generateQRCode(numberedHashPart);
			result.add(bufferedImage);
		}
		
		i = 0;
		for(final BufferedImage bufferedImage : result){
			ImageIO.write(bufferedImage, "png", new File("./qrcode" + i++ + ".png"));
		}
		
		return result;
	}
	
}







