
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;


public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	JLabel lbIm2;
	BufferedImage imgOne2;

 
	int width = 512;
	int height = 512;

	private static int pixels[][]= new int[512][512];
	private static final int N = 8;
	private static  int Q = 8;
	private static  int mode = 8;
	private static  int L = 8;
	private static int maxSignificantBit = 16;

	double [][][][] dctBlocks = new double[64][64][8][8];
	
	double [][][] blocksR = new double [4096][8][8];
	double [][][] blocksG = new double [4096][8][8];
	double [][][] blocksB = new double [4096][8][8];


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
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 
				
					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
 
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					pixels[x][y]=pix;

 
					img.setRGB(x,y,pix);
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
 
    public static double[][] DCT(double[][] input) {
        int N = 8;
        double[][] output = new double[N][N];
        double cu, cv, sum;
        
        for (int u = 0; u < N; u++) {
            for (int v = 0; v < N; v++) {
                sum = 0.0;
                cu = (u == 0) ? 1 / Math.sqrt(2) : 1;
                cv = (v == 0) ? 1 / Math.sqrt(2) : 1;
                
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        sum += Math.cos(((2 * i + 1) * u * Math.PI) / (2.0 * N)) *
                               Math.cos(((2 * j + 1) * v * Math.PI) / (2.0 * N)) *
                               input[i][j];
                    }
                }
                output[u][v] = 0.25 * cu * cv * sum;
            }
        }
        return output;
    }
    
    // IDCT Function
    public static double[][] IDCT(double[][] input) {
        int N = 8;
        double[][] output = new double[N][N];
        double cu, cv, sum;
        
        for (int x = 0; x < N; x++) {
            for (int y = 0; y < N; y++) {
                sum = 0.0;
                
                for (int u = 0; u < N; u++) {
                    for (int v = 0; v < N; v++) {
                        cu = (u == 0) ? 1 / Math.sqrt(2) : 1;
                        cv = (v == 0) ? 1 / Math.sqrt(2) : 1;
                        
                        sum += cu * cv * input[u][v] *
                               Math.cos(((2 * x + 1) * u * Math.PI) / (2.0 * N)) *
                               Math.cos(((2 * y + 1) * v * Math.PI) / (2.0 * N));
                    }
                }
                output[x][y] = 0.25 * sum;
            }
        }
        return output;
    }
    

	public  void DCTimp(){
        double[][] inputBlockR =new double[8][8];
        double[][] inputBlockG =new double[8][8];
        double[][] inputBlockB =new double[8][8];

		int count = 0;
 
        for (int k = 0; k < 512; k += 8) {
            for (int l = 0; l < 512; l += 8) {

                for(int i = 0;i<8;i++){
                    for(int j = 0;j<8;j++){
                        int color = pixels[i+k][j+l];

                        int rVal = (color >> 16) & 0xFF;
                        int gVal = (color >> 8) & 0xFF;
                        int bVal = (color) & 0xFF;
                                
                        inputBlockR[i][j] = rVal;
                        inputBlockG[i][j] = gVal;
                        inputBlockB[i][j] = bVal;
                        
        
                    } 
                }
                double [][]dctcoef = DCT(inputBlockR);        
                double [][]dctcoef1 = DCT(inputBlockG);       
                double [][]dctcoef2 = DCT(inputBlockB);

				blocksR[count] = quantizedBlocks(dctcoef);
				blocksG[count] = quantizedBlocks(dctcoef1);
				blocksB[count] = quantizedBlocks(dctcoef2);
				count++;
            }
        }
    }

	public void IDCTimp3()throws InterruptedException{

		imgOne2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		int minValue = 0;  
        int maxValue = 255; 		

		double [][][] idctbit = new double[4096] [8][8];
		double [][][] idctbit1 = new double [4096][8][8];
		double [][][] idctbit2 = new double[4096] [8][8];

		
		int pos = 0;
 
		for(int m = 1;m<maxSignificantBit;m++){
			pos = m;
			for(int i = 0;i<4096;i++){
				double [][]dctcoef = dequantizedBlocks(blocksR[i]);    
				double [][]dctcoef1 = dequantizedBlocks(blocksG[i]);   
				double [][]dctcoef2 = dequantizedBlocks(blocksB[i]);   

				for(int k = 0;k<8;k++){
					for(int l = 0;l<8;l++){
						int coef =(int) dctcoef[k][l];
						int coef1 =(int) dctcoef1[k][l];
						int coef2 =(int) dctcoef2[k][l];

						if(coef < 0 ){
							int coefP = -coef;
							idctbit[i][k][l] = (-1)*applyMask(coefP, pos); 
						}else{
							idctbit[i][k][l] = applyMask(coef, pos);
						}

						if(coef1 < 0 ){
							int coefP1 = -coef1;
							idctbit1[i][k][l] = (-1)*applyMask(coefP1, pos); 
						}else{
							idctbit1[i][k][l] = applyMask(coef1, pos);
						}

						if(coef2 < 0 ){
							int coefP2 = -coef2;
							idctbit2[i][k][l] = (-1)*applyMask(coefP2, pos); 
						}else{
							idctbit2[i][k][l] = applyMask(coef2, pos);
						}

					}
				}

				double [][]idctcoef = IDCT(idctbit[i]);        
				double [][]idctcoef1 = IDCT(idctbit1[i]);       
				double [][]idctcoef2 = IDCT(idctbit2[i]);

				for (int x = 0; x < idctcoef.length; x++) {
					for (int y = 0; y < idctcoef[x].length; y++) {

						int rV =  ((int)Math.round(idctcoef[x][y]));
						int gV = ((int)Math.round(idctcoef1[x][y] ));
						int bV =  ((int)Math.round(idctcoef2[x][y]));

						int r = Math.max(minValue, Math.min(maxValue,rV));
						int g = Math.max(minValue, Math.min(maxValue,gV));
						int b = Math.max(minValue, Math.min(maxValue,bV));

						int pix = 0xff000000 | ( (   r  & 0xff) << 16) | (( g& 0xff) << 8) | (  b& 0xff);
						int k = i /64;
						int l = i %64;
	
						imgOne2.setRGB(k*8+x, l*8+y,pix);
						 
					}
				}

			}
			createFrame();
			Thread.sleep(L);  
			 
		}
		
	} 
	 
	public static int applyMask(int elem, int pos) {
        int mask = ((1 << pos) - 1) << (maxSignificantBit - pos);
        return (elem & mask);
    
	}

	public void IDCTimp2()throws InterruptedException{
		imgOne2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 
		int minValue = 0;  
        int maxValue = 255;  
		int row = 0;
		int col = 0;
		
		boolean goingUp = true;
		double [][][] idcttemp = new double[4096] [8][8];
		double [][][] idcttemp1 = new double [4096][8][8];
		double [][][] idcttemp2 = new double[4096] [8][8];
		for(int m = 0;m<64;m++){

			for(int i = 0;i<4096;i++){
				double [][]dctcoef = dequantizedBlocks(blocksR[i]);    
				double [][]dctcoef1 = dequantizedBlocks(blocksG[i]);   
				double [][]dctcoef2 = dequantizedBlocks(blocksB[i]);  
				
				idcttemp[i][row][col]= dctcoef[row][col];
				
				idcttemp1[i][row][col]= dctcoef1[row][col];
				
				idcttemp2[i][row][col]= dctcoef2[row][col];

				double [][]idctcoef = IDCT(idcttemp[i]);        
				double [][]idctcoef1 = IDCT(idcttemp1[i]);       
				double [][]idctcoef2 = IDCT(idcttemp2[i]);



				for (int x = 0; x < idctcoef.length; x++) {
					for (int y = 0; y < idctcoef[x].length; y++) {

						int rV =  ((int)Math.round(idctcoef[x][y]));
						int gV = ((int)Math.round(idctcoef1[x][y] ));
						int bV =  ((int)Math.round(idctcoef2[x][y]));

						int r = Math.max(minValue, Math.min(maxValue,rV));
						int g = Math.max(minValue, Math.min(maxValue,gV));
						int b = Math.max(minValue, Math.min(maxValue,bV));

						int pix = 0xff000000 | ( (   r  & 0xff) << 16) | (( g& 0xff) << 8) | (  b& 0xff);
						int k = i /64;
						int l = i %64;
	
						imgOne2.setRGB(k*8+x, l*8+y,pix);

					}
				}
			}
			 
			if (goingUp) {
				if (col == 7) {
					row++;
					goingUp = false;
				} else if (row == 0) {
					col++;
					goingUp = false;
				} else {
					row--;
					col++;
				}
			} else {
				if (row == 7) {
					col++;
					goingUp = true;
				} else if (col == 0) {
					row++;
					goingUp = true;
				} else {
					row++;
					col--;
				}
			}
			createFrame();
			Thread.sleep(L);

		}

	}
 

	public void createFrame(){
		if (frame == null) {
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
	
			frame.getContentPane().add(lbIm1, c);
			lbIm2 = new JLabel(new ImageIcon(imgOne2));
		
			c.gridx = 1; 
			c.gridy = 0;  
	
			frame.getContentPane().add(lbIm2, c);
	
			frame.pack();
			frame.setVisible(true);
		} else {
			 
			lbIm1.setIcon(new ImageIcon(imgOne));
			lbIm2.setIcon(new ImageIcon(imgOne2));
			 
		}

	}

	public void IDCTimp()throws InterruptedException{
		imgOne2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		int minValue = 0;  
        int maxValue = 255;  
 

		for(int i = 0;i<4096;i++){
			double [][]dctcoef = dequantizedBlocks(blocksR[i]);    
			double [][]dctcoef1 = dequantizedBlocks(blocksG[i]);   
			double [][]dctcoef2 = dequantizedBlocks(blocksB[i]);   

			double [][]idctcoef = IDCT(dctcoef);        
			double [][]idctcoef1 = IDCT(dctcoef1);       
			double [][]idctcoef2 = IDCT(dctcoef2);

			for (int x = 0; x < idctcoef.length; x++) {
				for (int y = 0; y < idctcoef[x].length; y++) {

					int rV =  ((int)Math.round(idctcoef[x][y]));
					int gV = ((int)Math.round(idctcoef1[x][y] ));
					int bV =  ((int)Math.round(idctcoef2[x][y]));

					int r = Math.max(minValue, Math.min(maxValue,rV));
					int g = Math.max(minValue, Math.min(maxValue,gV));
					int b = Math.max(minValue, Math.min(maxValue,bV));

					int pix = 0xff000000 | ( (   r  & 0xff) << 16) | (( g& 0xff) << 8) | (  b& 0xff);
					int k = i /64;
					int l = i %64;
 
					imgOne2.setRGB(k*8+x, l*8+y,pix);

				}
			}
			createFrame();
			Thread.sleep(L);  
		}

	}

	public double[][] quantizedBlocks(double input[][]){
		double ret[][]=new double[8][8];
		for(int i = 0;i<8;i++){
			for(int j = 0;j<8;j++){
				ret[i][j]= Math.round( input[i][j]*(1.0/(Math.pow(2, Q))));
				 
			}
		}
		return ret;
	}

	public double[][] dequantizedBlocks(double input[][]){
		double ret[][]=new double[8][8];
		for(int i = 0;i<8;i++){
			for(int j = 0;j<8;j++){ 
					ret[i][j]= input[i][j]*(Math.pow(2, Q));
			}
		}
		return ret;
	}

	public void showIms(String[] args){

		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[0], imgOne);

		DCTimp();

		try {
			if(mode == 1){
				IDCTimp( );
			}else if(mode == 2){
				IDCTimp2( );
			}else{
				IDCTimp3( );
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
 
	}
 

	public static void main(String[] args) {
		ImageDisplay ren = new ImageDisplay();
		Q = Integer.parseInt(args[1]);
		mode = Integer.parseInt(args[2]);
		L = Integer.parseInt(args[3]);
		ren.showIms(args);
	}

}
