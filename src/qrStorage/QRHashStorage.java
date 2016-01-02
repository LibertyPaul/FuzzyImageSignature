package qrStorage;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


import org.opencv.core.Mat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.google.zxing.qrcode.QRCodeWriter;

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
	
	public String getHashValue(){
		return this.hashValue;
	}
	
	public String getSignatureValue(){
		return this.digitalSignature;
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
		return MatrixToImageWriter.toBufferedImage(matrix);
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
	
	private static String addId(final String value, final int id, final int maxId){
		String result = "";
		result += String.format("%0" + QRHashStorage.decimalsForId + "d", id);
		result += String.format("%0" + QRHashStorage.decimalsForId + "d", maxId);
		result += value;
		return result;
	}
	
	private static String removeId(final String value){
		assert value.length() >= QRHashStorage.decimalsForId * 2;
		return value.substring(QRHashStorage.decimalsForId * 2);
	}
	
	private static int getId(final String hashPart){
		assert hashPart.length() >= QRHashStorage.decimalsForId;
		String id_s = hashPart.substring(0, QRHashStorage.decimalsForId);
		return Integer.parseUnsignedInt(id_s);
	}
	
	private static int getMaxId(final String hashPart){
		assert hashPart.length() >= QRHashStorage.decimalsForId * 2;
		String maxId_s = hashPart.substring(QRHashStorage.decimalsForId, QRHashStorage.decimalsForId * 2);
		return Integer.parseUnsignedInt(maxId_s);
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
		
		return result;
	}
	
	public static QRHashStorage fromQRCodes(final BufferedImage codePart) throws Exception{
		LuminanceSource luminanceSource = new BufferedImageLuminanceSource(codePart);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));
		QRCodeMultiReader qrMultiReader = new QRCodeMultiReader();
		
		Map<DecodeHintType, Void> hints = new TreeMap<>();
		hints.put(DecodeHintType.TRY_HARDER, null);

		Result[] results = qrMultiReader.decodeMultiple(bitmap, hints);
		if(results.length < 2){
			throw new Exception("Recognition failed");
		}
		
		List<String> values = new LinkedList<>();
		for(int i = 0; i < results.length; ++i){
			values.add(results[i].getText());
		}
		
		final int maxId = QRHashStorage.getMaxId(values.get(0));
		if(maxId - 1 != values.size()){
			throw new Exception("One or more QR-codes wasn't recognized");
		}
		
		List<String> parts = new ArrayList<>(values.size());
		
		for(int id = 0; id <= maxId; ++id){
			for(int i = 0; i < values.size(); ++i){
				final int currentId = QRHashStorage.getId(values.get(i));
				if(currentId == id){
					parts.add(QRHashStorage.removeId(values.get(i)));
					break;
				}
			}
			if(parts.size() != id + 1){
				throw new Exception("Incorrect QR-code");
			}
		}
		
		
		
		return null;
		
	}
	
	public static BufferedImage matToBufferedImage(final Mat srcMat){
		byte[] data = new byte[srcMat.rows() * srcMat.cols() * (int)srcMat.elemSize()];
		srcMat.get(0, 0, data);
		
		assert srcMat.channels() == 3;
		
		for(int i = 0; i < data.length; i += 3){
			byte temp = data[i];
			data[i] = data[i + 2];
			data[i + 2] = temp;
		}
		
		BufferedImage image = new BufferedImage(srcMat.cols(), srcMat.rows(), BufferedImage.TYPE_3BYTE_BGR);
		image.getRaster().setDataElements(0, 0, srcMat.cols(), srcMat.rows(), data);
		
		return image;
	}
	
}







