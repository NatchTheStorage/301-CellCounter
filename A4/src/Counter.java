import javax.imageio.*;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.*;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.awt.image.DataBufferByte;

// Natch Sadindum - NS174 - 1269188
// COMPX-301 A4

public class Counter {
    public static void main(String[] args) throws IOException {
        try {
            if (args.length == 1) {
                System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

                BufferedImage bi = ImageIO.read(new File(args[0]));
                int w = bi.getWidth(), h = bi.getHeight();
                System.out.println("Width/Height = " + w + ", " + h);

                bi = GreyScaled(bi);
                Mat m = bufferedImageToMat(bi);

                m = Thresh(m, 20);
                saveImg(m, "1");

                m = Erode(m, 3);
                m = Dilate(m, 2);
                m = Sharp(m);
                saveImg(m, "2");

                m = Sharp(m);
                m = Dilate(m, 3);
                saveImg(m, "3");

                m = Erode(m, 4);
                saveImg(m, "4");

                m = Sharp(m);
                m = Thresh(m, 30);
                saveImg(m, "5");

                m = Erode(m, 6);
                m = Dilate(m, 7);
                saveImg(m, "6");

                m = Erode(m, 6);
                m = Dilate(m, 4);
                saveImg(m, "7");

                m = Erode(m,2);
                m = Dilate(m, 4);
                m = Sharp(m);
                m = Thresh(m, 60);
                saveImg(m, "8");


                m = BinaryImage(m);
                saveImg(m, "9");

                ProcessedPixel[][] binaryArray = CreateBinaryArray(m);

                System.out.println(CheckForRegions(binaryArray) + " cells detected on this image!");
            }
            else
                System.out.println("Usage - java -cp \".;./opencv-430.jar\" Counter <imagefile>");
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    // greyscales the image
    private static BufferedImage GreyScaled(BufferedImage bi) {
        int w = bi.getWidth(), h = bi.getHeight();
        for(int i=0; i<h; i++) {
            for(int j=0; j<w; j++) {
                Color c = new Color(bi.getRGB(j, i));
                int red = (int)(c.getRed() * 0.299);
                int green = (int)(c.getGreen() * 0.587);
                int blue = (int)(c.getBlue() *0.114);
                Color newColor = new Color(red+green+blue,

                        red+green+blue,red+green+blue);

                bi.setRGB(j,i,newColor.getRGB());
            }
        }

        return bi;
    }

    private static Mat Sharp(Mat m) {
        Mat destination = new Mat(m.rows(),m.cols(),m.type());
        Imgproc.GaussianBlur(m, destination, new Size(0,0), 40);
        Core.addWeighted(m, 1.5, destination, -0.5, 0, destination);
        return destination;
    }

    private static Mat Blur(Mat m) {
        Mat destination = new Mat(m.rows(),m.cols(),m.type());
        Imgproc.GaussianBlur(m, destination, new Size(0,0), 1);
        return destination;
    }

    private static Mat Thresh(Mat m, int min) throws Exception {
        Mat destination = new Mat(m.rows(),m.cols(),m.type());

        Imgproc.threshold(m,destination,min,255,Imgproc.THRESH_TOZERO);
        return destination;

    }

    private static Mat Dilate(Mat m, int factor) throws Exception {
        Mat destination = new Mat(m.rows(),m.cols(),m.type());

        Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(factor, factor));
        Imgproc.dilate(m, destination, element1);
        return destination;
    }

    private static Mat Erode(Mat m, int factor) throws Exception {
        Mat destination = new Mat(m.rows(),m.cols(),m.type());

        Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(factor, factor));
        Imgproc.erode(m, destination, element1);

        return destination;
    }

    private static Mat Contrast(Mat m) {
        Mat destination = new Mat(m.rows(),m.cols(),m.type());
        Imgproc.equalizeHist(m, destination);

        return destination;
    }

    public static Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    // Converts a MAT object back into a buffered image
    public static BufferedImage mat2Img(Mat in)
    {
        BufferedImage out;
        byte[] data = new byte[512 * 382 * (int)in.elemSize()];
        int type;
        in.get(0, 0, data);

        if(in.channels() == 1)
            type = BufferedImage.TYPE_BYTE_GRAY;
        else
            type = BufferedImage.TYPE_3BYTE_BGR;

        out = new BufferedImage(512, 382, type);

        out.getRaster().setDataElements(0, 0, 512, 382, data);
        return out;
    }

    // Save the image from MAT format into a png with name output+num
    private static void saveImg(Mat m, String num) throws Exception {
        BufferedImage bi = mat2Img(m);
        String name = "out" + num + ".png";
        File output = new File("./output", name);
        ImageIO.write(bi, "png", output);
    }

    // Converts the pixels in the matrix into either black or white only
    private static Mat BinaryImage(Mat m) {
        Mat destination = new Mat(m.rows(),m.cols(),m.type());
        Imgproc.threshold(m,destination, 25,255, Imgproc.THRESH_BINARY);

        return destination;
    }
    // Creates a 2d array of processed pixels converted from raw pixel data
    private static ProcessedPixel[][] CreateBinaryArray(Mat m) {
        ProcessedPixel[][] pixelArray = new ProcessedPixel[m.width()][m.height()];
        for (int y = 0; y < m.height(); y++) {
            for (int x = 0; x < m.width(); x++) {
                double[] pix = m.get(y, x);

                //System.out.println("Location: " + x + ", " + y + " -- value: " + (pix[0] + pix[1] + pix[2]));
                // if any of the pix values are not 0, then it is a cell pixel, else its background
                if (pix[0] > 0) {
                    pixelArray[x][y] = new ProcessedPixel(true,false);
                }
                // if the pixel is black
                else {
                    pixelArray[x][y] = new ProcessedPixel(false,false);
                }
            }
        }
        return pixelArray;
    }

    // Check all the processed pixels in the arrays with FloodFill
    private static int CheckForRegions(ProcessedPixel[][] p) {
        int regions = 0;
        int width = p.length;
        int height = p[0].length;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // If we are looking at a white pixel and it has not been checked before
                if (p[x][y].CheckType() && !p[x][y].IsChecked()) {
                    // gets the size of a cell
                    int cellSize = RecursiveCheckPixel(p,x,y, 1);
                    // If the cell is bigger than this then count it as a cell (if it is smaller it is probably an artifact)
                    if (cellSize > 9)
                        regions++;
                }
                // move to the next pixel if this one is black
            }
        }
        return regions;
    }

    // Recursive function that
    private static int RecursiveCheckPixel(ProcessedPixel[][] p, int x, int y, int pixelCount) {

        // Base cases, do not check off the edge of the image, do not check already checked pixels, do not check black pixels
        if (x < 0 || x >= p.length || y < 0 || y >= p[0].length) {
            return 0;
        }
        if (p[x][y].IsChecked()) {
            return 0;
        }
        if (!p[x][y].CheckType()) {
            return 0;
        }
        p[x][y].SetCheckedTrue();

        // Check all pixels around us, add the checked white pixels to our pixel count/cell siz
        pixelCount += RecursiveCheckPixel(p,x+1,y,pixelCount);
        pixelCount += RecursiveCheckPixel(p,x-1,y,pixelCount);
        pixelCount += RecursiveCheckPixel(p,x,y+1,pixelCount);
        pixelCount += RecursiveCheckPixel(p,x,y-1,pixelCount);

        return pixelCount;
    }
}

class ProcessedPixel {

    private boolean type;   // is the pixel black or white (0 for black, 1 for white)
    private boolean checked;    // has this pixel been checked before?
    public void SetCheckedTrue() { checked = true; }
    public boolean CheckType() { return type; }
    public boolean IsChecked() { return checked; }


    public ProcessedPixel(boolean t, boolean c) {
        type = t;
        checked = c;
    }
}
