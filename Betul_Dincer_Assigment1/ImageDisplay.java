import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.math.BigDecimal;

public class ImageDisplay {

    JFrame frame;
    JLabel lbIm1;
    BufferedImage imgOne;
    BufferedImage transformedImage;
    int width = 512;
    int height = 512;
    double zoomFactorRounded=0.0;

    static double zoomFactor = 0.0f;
    static double zoomFactorChange = 0.0f;
    static double rotationAngle = 0.0f;
    static double rotationAngleChange = 0.0f;
    static int frameVal = 24; 

    private void readImageRGB(int width, int height, String imgPath, BufferedImage img) {
        try {
            int frameLength = width * height * 3;

            File file = new File(imgPath);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(0);

            long len = frameLength;
            byte[] bytes = new byte[(int) len];

            raf.read(bytes);

            int ind = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind + height * width];
                    byte b = bytes[ind + height * width * 2];

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    img.setRGB(x, y, pix);
                    ind++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage transformImage(BufferedImage originalImage, double zoomFactor, double rotationAngle) {

        BufferedImage transformedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
   
        double translateX = width / 2.0;
        double translateY = height / 2.0;
    
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                double centerX = x - translateX;
                double centerY = y - translateY;

                double rotatedX = (Math.cos(Math.toRadians(-rotationAngle)) * centerX -
                                  Math.sin(Math.toRadians(-rotationAngle)) * centerY)/zoomFactor;
    
                double rotatedY = (Math.sin(Math.toRadians(-rotationAngle)) * centerX +
                                  Math.cos(Math.toRadians(-rotationAngle)) * centerY)/zoomFactor;

                rotatedX += translateX;
                rotatedY += translateY;

                if (rotatedX >= 0 && rotatedX < width && rotatedY >= 0 && rotatedY < height) {                           
                    if(zoomFactor < 1.0){
                        int rVal = 0;
                        int gVal = 0;
                        int bVal = 0;
                        int windowSize = 0;
                        for(int i = (int)rotatedX -1; i<= (int)rotatedX + 1; i++ ){
                            for(int j = (int)rotatedY - 1; j<= (int)rotatedY + 1; j++){
                                if (i >= 0 && i < width && j >= 0 && j < height) {
                                    int color = originalImage.getRGB(i, j);
                                    rVal += (color >> 16) & 0xFF;
                                    gVal += (color >> 8) & 0xFF;
                                    bVal += (color) & 0xFF;
                                    windowSize ++;
                                }
                            }             
                        }
                        rVal = rVal/windowSize;
                        gVal = gVal/windowSize;
                        bVal = bVal/windowSize;                         
                        int newColor = 0xff000000 | ((rVal & 0xff) << 16) | ((gVal & 0xff) << 8) | (bVal & 0xff);
                        transformedImage.setRGB(x, y, newColor); 
                    }else{
                        int color = originalImage.getRGB((int) rotatedX, (int) rotatedY);
                        transformedImage.setRGB(x, y, color);
                    }

                } else {
                     
                    transformedImage.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        }

        return transformedImage;
    }
    
    void showVideo(String[] args) throws InterruptedException{

        while(true){
            showIms(args);
            Thread.sleep(1000/frameVal);            
            zoomFactor = zoomFactor + zoomFactorChange; 
            BigDecimal bd = new BigDecimal(zoomFactor);
            BigDecimal roundedNumber = bd.round(new java.math.MathContext(3));
            zoomFactorRounded = roundedNumber.doubleValue();
            if(zoomFactorRounded>0){
                zoomFactor = zoomFactorRounded;
            }else{
                showIms(args);
                break;
                 
            }
            rotationAngle += rotationAngleChange;
            rotationAngle = rotationAngle % 360;   
        }
         
    }
    

    public void showIms(String[] args) {
       
        imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        readImageRGB(width, height, args[0], imgOne);
        if(zoomFactorRounded>0){
            transformedImage = transformImage(imgOne, zoomFactor, rotationAngle);
        }else{
            transformedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }

        if (frame == null) {
            // Create the frame only once
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
        } else {
             
            lbIm1.setIcon(new ImageIcon(transformedImage));
             
        }
    }

    public static void main(String[] args) {
        zoomFactor = 1.0;  
        rotationAngle = 0.0; 
        frameVal =  Integer.parseInt(args[3]); 
        rotationAngleChange = Double.parseDouble(args[2])/frameVal; 
        zoomFactorChange = (Double.parseDouble(args[1])-1.0)/frameVal;
        ImageDisplay ren = new ImageDisplay();
        
        try {
            ren.showVideo(args);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
         
        
    }
}