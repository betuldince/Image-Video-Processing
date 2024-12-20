
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

public class ImageDisplay2 {

	static int mode = 0;
	static double bucket=0;
	static int numBucket=0;
	static int error1=0;
	static int error2=0;
	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;

	// Modify the height and width values here to read and display an image with
  	// different dimensions. 
	int width = 512;
	int height = 512;
	static int [] freqMapR = new int[256];
	static int [] freqMapG = new int[256];
	static int [] freqMapB = new int[256];

	static List<Buckets> bucketListR;
	static List<Buckets> bucketListG;
	static List<Buckets> bucketListB;

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private void readImageRGB(int width, int height, String imgPath, BufferedImage img)
	{
		try
		{
			int frameLength = width*height*3;

			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			long len = frameLength;
			byte[] bytes = new byte[(int) len];

			raf.read(bytes);

			int ind = 0;

			if(mode==2){
				for(int y = 0; y < height; y++)
				{
					for(int x = 0; x < width; x++)
					{
						byte a = 0;
						byte r = bytes[ind];
						byte g = bytes[ind+height*width];
						byte b = bytes[ind+height*width*2]; 
	
						int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
						int rVal = (pix >> 16) & 0xFF;
						int gVal = (pix >> 8) & 0xFF;
						int bVal = (pix) & 0xFF;
						freqMapR[rVal]++;
						freqMapG[gVal]++;
						freqMapB[bVal]++;
						ind++;
					}
				}
				setRangesR();
				setRangesG();
				setRangesB();

				ind = 0;

			}


			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					int newPix = 0;
					if(mode == 1){
						newPix = UniformQuantization(pix);
					}else if(mode == 2){
						newPix = NonUniformQuantization(pix);
					}

					img.setRGB(x,y,newPix); 
				 
					ind++;
				}
			}
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	public int UniformQuantization(int pix){
		 
		int rVal = (pix >> 16) & 0xFF;
		int gVal = (pix >> 8) & 0xFF;
		int bVal = (pix) & 0xFF;
		int sizeBucket = 256/numBucket;
		int bucketPosR = rVal / (sizeBucket);
		int bucketPosG = gVal / (sizeBucket);
		int bucketPosB = bVal / (sizeBucket);

		int midPointR = Math.min(255, ((bucketPosR*sizeBucket) + (((bucketPosR+1)*sizeBucket)))/2) ;
		int midPointG = Math.min(255, ((bucketPosG*sizeBucket) + (((bucketPosG+1)*sizeBucket)))/2) ;
		int midPointB = Math.min(255, ((bucketPosB*sizeBucket) + (((bucketPosB+1)*sizeBucket)))/2) ;
 
		int errorR = Math.abs(rVal-midPointR) ;
		int errorG = Math.abs(gVal-midPointG);
		int errorB = Math.abs(bVal-midPointB);
		int sumError = errorR + errorG + errorB;
		
		error1 += (sumError);
		int newPix = 0xff000000 | ((midPointR & 0xff) << 16) | ((midPointG & 0xff) << 8) | (midPointB & 0xff);
		return newPix;

	}

	public int NonUniformQuantization(int pix){

		int rVal = (pix >> 16) & 0xFF;
		int gVal = (pix >> 8) & 0xFF;
		int bVal = (pix) & 0xFF;

		int newR = newValR(rVal);
		int newG = newValG(gVal);
		int newB = newValB(bVal);

		int errorR = Math.abs(rVal-newR) ;
		int errorG = Math.abs(gVal-newG);
		int errorB = Math.abs(bVal-newB);
 
		int sumError = errorR + errorG + errorB;
		error2 += (errorB);

		int newPix = 0xff000000 | ((newR & 0xff) << 16) | ((newG & 0xff) << 8) | (newB & 0xff);

		return newPix;
	}

	int newValR(int rVal){
		int pixel = rVal;
		for (Buckets bucket : bucketListR) {
			int avgV = bucket.returnAvg(pixel);
			if(avgV !=-1){
				pixel = avgV;
			}
			 
		}
		return pixel;

	}
	int newValG(int gVal){
		int pixel = gVal;
		for (Buckets bucket : bucketListG) {
			int avgV = bucket.returnAvg(pixel);
			if(avgV !=-1){
				pixel = avgV;
			}
			 
		}
		return pixel;

	}
	int newValB(int bVal){
		int pixel = bVal;
		for (Buckets bucket : bucketListB) {
			int avgV = bucket.returnAvg(pixel);
			if(avgV !=-1){
				pixel = avgV;
			}
			 
		}
		return pixel;

	}

	private static int midPointR(int start, int end) {
		int totalPixel =  numPixelInRangeR(start, end);
		if(totalPixel>1){
			totalPixel = totalPixel / 2;
			int counter = 0;
			int mid = start;
			for (int i = start; i < end; i++) {
				counter += freqMapR[i];
				mid++;
				if (counter >= totalPixel) {
					break;
				}
			}
			 
			return mid-1;
		}else{
			return (start+end)/2;
		}

    }

	private static int midPointG(int start, int end) {
		int totalPixel =  numPixelInRangeG(start, end);
		
		if(totalPixel>1){
			totalPixel = totalPixel / 2;
			System.out.println(totalPixel);
			int counter = 0;
			int mid = start;
			for (int i = start; i <= end; i++) {
				counter += freqMapG[i];
				mid++;
				if (counter >= totalPixel) {
					System.out.println(counter+ " " + totalPixel);
					break;
				}
			}
			//System.out.println(mid);
			return mid-1;
		}else{
			return (start+end)/2;
		}
    }

	private static int midPointB(int start, int end) {
		int totalPixel =  numPixelInRangeB(start, end);
		if(totalPixel>1){
			totalPixel = totalPixel / 2;
			int counter = 0;
			int mid = start;
			for (int i = start; i < end; i++) {
				counter += freqMapB[i];
				mid++;
				if (counter >= totalPixel) {
					break;
				}
			}
			 
			return mid-1;
		}else{
			return (start+end)/2;
		}
    }


	private static int numPixelInRangeG(int s, int e){
		int retVal = 0;
		if(s==e){
			return freqMapG[s];
		}
		for(int i=s;i<=e;i++){
			retVal += freqMapG[i];
		}
		return retVal;
	}
	private static int numPixelInRangeB(int s, int e){
		int retVal = 0;
		if(s==e){
			return freqMapB[s];
		}
		for(int i=s;i<=e;i++){
			retVal += freqMapB[i];
		}
		return retVal;
	}


	private static int numPixelInRangeR(int s, int e){
		int retVal = 0;
		if(s==e){
			return freqMapR[s];
		}
		for(int i=s;i<=e;i++){
			retVal += freqMapR[i];
		}
		return retVal;
	}

	private static void createTwoBucketsG(List<Buckets> bucketList, Buckets bucket) {
        int numPix = bucket.getNumPixels();
        int midP = midPointG(bucket.getStart(), bucket.getEnd());
 		 
		if((midP == bucket.getStart() || midP == bucket.getEnd()) && bucket.getNumPixels()!= 0 ){
			bucketList.remove(bucket);
			Buckets bucket1 = new Buckets(midP, midP,freqMapG[midP],freqMapG );
			bucketList.add(bucket1);
			 

		}else {
			Buckets bucket1 = new Buckets(bucket.getStart(), midP, numPixelInRangeG(bucket.getStart(),midP),freqMapG);
			Buckets bucket2 = new Buckets(midP, bucket.getEnd(),numPixelInRangeG(midP,bucket.getEnd()),freqMapG);
	 
			bucketList.remove(bucket);
			bucketList.add(bucket1);
			bucketList.add(bucket2);

		}

    }

	private static void createTwoBucketsR(List<Buckets> bucketList, Buckets bucket) {
        int numPix = bucket.getNumPixels();
        int midP = midPointR(bucket.getStart(), bucket.getEnd());
 		 
		if((midP == bucket.getStart() || midP == bucket.getEnd()) && bucket.getNumPixels()!= 0 ){
			bucketList.remove(bucket);
			Buckets bucket1 = new Buckets(midP, midP,freqMapR[midP],freqMapR );
			bucketList.add(bucket1);
			 

		}else {
			Buckets bucket1 = new Buckets(bucket.getStart(), midP, numPixelInRangeR(bucket.getStart(),midP),freqMapR);
			Buckets bucket2 = new Buckets(midP, bucket.getEnd(),numPixelInRangeR(midP,bucket.getEnd()),freqMapR);
	 
			bucketList.remove(bucket);
			bucketList.add(bucket1);
			bucketList.add(bucket2);

		}

    }

	private static void createTwoBucketsB(List<Buckets> bucketList, Buckets bucket) {
        int numPix = bucket.getNumPixels();
        int midP = midPointB(bucket.getStart(), bucket.getEnd());
 		 
		if((midP == bucket.getStart() || midP == bucket.getEnd()) && bucket.getNumPixels()!= 0 ){
			bucketList.remove(bucket);
			Buckets bucket1 = new Buckets(midP, midP,freqMapB[midP],freqMapB );
			bucketList.add(bucket1);
			 

		}else {
			Buckets bucket1 = new Buckets(bucket.getStart(), midP, numPixelInRangeB(bucket.getStart(),midP),freqMapB);
			Buckets bucket2 = new Buckets(midP, bucket.getEnd(),numPixelInRangeB(midP,bucket.getEnd()),freqMapB);
	 
			bucketList.remove(bucket);
			bucketList.add(bucket1);
			bucketList.add(bucket2);

		}

    }

	

	private static int findFirstBucket(List<Buckets> buckets){
		
		for(int i = 0 ;i<buckets.size();i++){
			if (buckets.get(i).getStart() != buckets.get(i).getEnd()) {
				
				return i;
			}
		}
		return -1;
	}

	private static void setRangesR(){
		bucketListR = new ArrayList<>();
		int totalRed = 512*512;
		Buckets initialBucket = new Buckets(0, 255, totalRed,freqMapR);
		bucketListR.add(initialBucket);
		while (bucketListR.size() <numBucket) {
			int ind = 0;
			Collections.sort(bucketListR, Collections.reverseOrder());

            Buckets mostPixelsBucket = bucketListR.get(ind);
			if(mostPixelsBucket.getStart() != mostPixelsBucket.getEnd()){
				createTwoBucketsR(bucketListR, mostPixelsBucket);
				 
			}else {
				Collections.sort(bucketListR, Collections.reverseOrder());
				int index = findFirstBucket(bucketListR);
				Buckets mostPixelsBucket1 = bucketListR.get(index);
				createTwoBucketsR(bucketListR, mostPixelsBucket1);
			} 
			
		}
	}

	private static void setRangesG(){
		bucketListG= new ArrayList<>();
		int totalGreen = 512*512;
		Buckets initialBucket = new Buckets(0, 255, totalGreen,freqMapG);
		bucketListG.add(initialBucket);
		while (bucketListG.size() <numBucket) {
			int ind = 0;
			Collections.sort(bucketListG, Collections.reverseOrder());

            Buckets mostPixelsBucket = bucketListG.get(ind);
			if(mostPixelsBucket.getStart() != mostPixelsBucket.getEnd()){
				createTwoBucketsG(bucketListG, mostPixelsBucket);
				 
			}else {
				Collections.sort(bucketListG, Collections.reverseOrder());
				int index = findFirstBucket(bucketListG);
				Buckets mostPixelsBucket1 = bucketListG.get(index);
				createTwoBucketsG(bucketListG, mostPixelsBucket1);
			} 
			
		}
	}
	private static void setRangesB(){
		bucketListB= new ArrayList<>();
		int totalB = 512*512;
		Buckets initialBucket = new Buckets(0, 255, totalB,freqMapB);
		bucketListB.add(initialBucket);
		while (bucketListB.size() <numBucket) {
			int ind = 0;
			Collections.sort(bucketListB, Collections.reverseOrder());

            Buckets mostPixelsBucket = bucketListB.get(ind);
			if(mostPixelsBucket.getStart() != mostPixelsBucket.getEnd()){
				createTwoBucketsB(bucketListB, mostPixelsBucket);
				 
			}else {
				Collections.sort(bucketListB, Collections.reverseOrder());
				int index = findFirstBucket(bucketListB);
				Buckets mostPixelsBucket1 = bucketListB.get(index);
				createTwoBucketsB(bucketListB, mostPixelsBucket1);
			} 
			
		}
	}






	public void showIms(String[] args){

		// Read in the specified image
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[0], imgOne);

		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		lbIm1 = new JLabel(new ImageIcon(imgOne));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		ImageDisplay2 ren = new ImageDisplay2();
		mode = Integer.parseInt(args[1]);
		bucket = Double.parseDouble(args[2]);

		//numBucket =(int) Math.cbrt(bucket);


		numBucket = 50;
		ren.showIms(args);
		Collections.sort(bucketListG, Comparator.comparingInt(Buckets::getStart));
		for(int i =0;i<bucketListG.size();i++){
			System.out.println(i+" "+ bucketListG.get(i));
		}

		for(int i = 0;i<freqMapG.length;i++){
			System.out.println(i+": "+ freqMapG[i]);
		}
		
		
		/*
		for(int i = 0;i<freqMapR.length;i++){
			System.out.println(i+": "+freqMapR[i]);
		}
		 
		System.out.println(numBucket);
		ren.showIms(args);
		System.out.println(error2);
		*/
	}

}


class Buckets implements Comparable<Buckets> {
    private int start;
    private int end;
    private int numOfPixels;
	private int[] freqMap;
	private int avg;
	 
    public Buckets(int start, int end, int numOfPixels, int [] freqMap) {
        this.start = start;
        this.end = end;
        this.numOfPixels = numOfPixels;
		this.freqMap = freqMap;
		avgOfBucket();
    }

	public void avgOfBucket(){
		int total = 0;
		int totalFreq = 0;
		for(int i = start ; i<end;i++){
			total += i*freqMap[i];
			totalFreq += freqMap[i];
		}
		if(totalFreq>0){
			avg =  total/totalFreq;
		}else{
			avg = start; // ASK THIS!
		}
		
	}

	public int returnAvg(int pixVal){
		if(pixVal>= start && pixVal <= end){
			return avg;
		}
		return -1;
	}

	public int getAvg(){
		return avg;
	}

 
    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getNumPixels() {
        return numOfPixels;
    }

    public void setStart(int startN) {
        this.start = startN;
    }

    public void setEnd(int endN) {
        this.end = endN;
    }

    public void setNumPixels(int numOfPixelsN) {
        this.numOfPixels = numOfPixelsN;
    }

    @Override
    public int compareTo(Buckets other) {
         
        return Integer.compare(this.numOfPixels, other.numOfPixels);
    }

    @Override
    public String toString() {
        return "[" + start + "-" + end + "-" + numOfPixels + "-"+ avg+ "]";
    }
}
