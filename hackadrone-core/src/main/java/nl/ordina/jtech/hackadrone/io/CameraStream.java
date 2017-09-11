/*
 * Copyright (C) 2017 Ordina
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.ordina.jtech.hackadrone.io;

import nl.ordina.jtech.hackadrone.net.Decoder;
import nl.ordina.jtech.hackadrone.net.DroneDecoder;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Class representing the camera for a drone.
 *
 * @author Nils Berlijn
 * @version 1.0
 * @since 1.0
 */
public final class CameraStream implements Handler {

    /**
     * The host of the drone.
     */
    private final String droneHost;

    /**
     * The port of the drone.
     */
    private final int dronePort;

    /**
     * The host of the camera.
     */
    private final String cameraHost;

    /**
     * The port of the camera.
     */
    private final int cameraPort;

    /**
     * The video player.
     */
    private Process videoPlayer;

    /**
     * The video socket.
     */
    private Socket videoSocket;

    /**
     * The video output stream.
     */
    private OutputStream videoOutputStream;

    /**
     * The video decoder.
     */
    private Decoder decoder;

    /**
     * OpenCV VideoCapture.
     */
    private VideoCapture vc;

    /**
     * A command constructor.
     *
     * @param droneHost the host of the drone
     * @param dronePort the port of the drone
     * @param cameraHost the host of the camera
     * @param cameraPort the port of the camera
     */
    public CameraStream(String droneHost, int dronePort, String cameraHost, int cameraPort) {
        this.droneHost = droneHost;
        this.dronePort = dronePort;
        this.cameraHost = cameraHost;
        this.cameraPort = cameraPort;
    }

    /**
     * Starts the video camera.
     */
    @Override
    public void start() {
        try {
            if (videoPlayer != null) {
                stop();
            }
            startVideoStream();

            videoSocket = new Socket(InetAddress.getByName(cameraHost), cameraPort);
            videoOutputStream = new BufferedOutputStream(videoSocket.getOutputStream());

            startVideo();
        } catch (IOException e) {
            System.err.println("Unable to start the video player:");
            e.printStackTrace();
        }
    }

    /**
     * Stops the video camera.
     */
    @Override
    public void stop() {
        if (videoPlayer != null) {
            videoPlayer.destroy();
            videoPlayer = null;
        }

        if (videoOutputStream != null && videoSocket != null) {
            try {
                videoOutputStream.close();
                videoSocket.close();
            } catch (IOException e) {
                System.err.println("Unable to stop the video player");
            }

            videoOutputStream = null;
            videoSocket = null;
        }
    }

    /**
     * Starts the video camera.
     *
     * @throws IOException if starting the video camera failed
     */
    private void startVideoStream() throws IOException {
        String output = "rtsp://" + cameraHost + ":" + cameraPort + "?listen";

        vc = new VideoCapture();
        vc.open(output);
    }

    /**
     * Starts the video.
     *
     * @throws IOException if starting the video failed
     */
    private void startVideo() throws IOException {
        if (decoder != null) {
            throw new IOException("Starting the video stream failed!");
        }

        decoder = new DroneDecoder(droneHost, dronePort);
        decoder.connect();

        final Thread thread = new Thread(() -> {
            byte[] data = null;

            JFrame frame=new JFrame();
            frame.setLayout(new FlowLayout());
            JLabel lbl=new JLabel();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            do {
                try {
                    data = decoder.read();

                    if (videoOutputStream != null) {
                        videoOutputStream.write(data);
                    }

                    if (videoOutputStream == null) {
                        decoder.disconnect();
                        break;
                    }

                    Mat videoFrame = new Mat();
                    vc.read(videoFrame);
                    lbl.setIcon(new ImageIcon(mat2Img(videoFrame)));
                } catch (IOException e) {
                    System.err.println("Unable to read video output stream");
                }
            } while (data != null);

            decoder = null;
        });

        thread.start();
    }


    /**
     * Converts OpenCV Mat object to BufferedImage
     * src: https://stackoverflow.com/questions/27770745/java-opencv-how-to-make-bufferedimage-from-mat-directly
     * @param in Mat to be converted to BufferedImage
     * @return BufferedImage object
     */
    private static BufferedImage mat2Img(Mat in)
    {
        BufferedImage out;
        byte[] data = new byte[320 * 240 * (int)in.elemSize()];
        int type;
        in.get(0, 0, data);

        if(in.channels() == 1)
            type = BufferedImage.TYPE_BYTE_GRAY;
        else
            type = BufferedImage.TYPE_3BYTE_BGR;

        out = new BufferedImage(320, 240, type);

        out.getRaster().setDataElements(0, 0, 320, 240, data);
        return out;
    }
}
