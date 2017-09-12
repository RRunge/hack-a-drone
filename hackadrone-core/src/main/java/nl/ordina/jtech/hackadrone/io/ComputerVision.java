package nl.ordina.jtech.hackadrone.io;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.awt.image.BufferedImage;

class ComputerVision {

    private CascadeClassifier faceCascade;
    private CascadeClassifier eyeCascade;

    ComputerVision() {
        faceCascade = new CascadeClassifier("resources/haarcascade_frontalface_default.xml");
        eyeCascade = new CascadeClassifier("resources/haarcascade_eye.xml");
    }

    public BufferedImage DetectFaces(String imagePath) {
        System.out.println("ImagePath: " + imagePath);
        Mat frame = Imgcodecs.imread(imagePath, 1);
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(grayFrame, grayFrame);
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(20,20), new Size());

        Rect[] facesArray = faces.toArray();

        for (int i = 0; i < facesArray.length; i++)
            Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);

        return mat2Img(frame);
    }

    public static BufferedImage mat2Img(Mat in)
    {
        BufferedImage out;
        byte[] data = new byte[720 * 576 * (int)in.elemSize()];
        int type;
        in.get(0, 0, data);

        if(in.channels() == 1)
            type = BufferedImage.TYPE_BYTE_GRAY;
        else {
            type = BufferedImage.TYPE_3BYTE_BGR;
            // bgr to rgb
            byte b;
            for(int i=0; i<data.length; i=i+3) {
                b = data[i];
                data[i] = data[i+2];
                data[i+2] = b;
            }
        }

        out = new BufferedImage(720, 576, type);

        out.getRaster().setDataElements(0, 0, 720, 576, data);
        return out;
    }
}
