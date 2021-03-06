package nl.ordina.jtech.hackadrone.io;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import org.apache.commons.io.FileUtils;

class FrameWatcher {
    private JLabel label;
    private String latestFrame;
    private ComputerVision cv;
    private static String pathToFrameDir = "frames";

    FrameWatcher() {
        cv = new ComputerVision();
        JFrame frame = new JFrame("Frame Watcher");
        frame.setSize(720,576);
        label = new JLabel();
        frame.add(label);
        frame.setVisible(true);
        latestFrame = "";
        clearFrameDirectory();
        startUpdatingFrames();
    }

    private void startUpdatingFrames() {
        final Path path = Paths.get(pathToFrameDir);
        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
            final WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true) {
                final WatchKey wk = watchService.take();
                for (WatchEvent<?> event : wk.pollEvents()) {
                    final Path changed = (Path) event.context();
                    System.out.println(changed.toString());
                    if (changed.toString().endsWith(".jpg")) {
                        if(!latestFrame.isEmpty()) {
                            File previousFrame = new File(latestFrame);
                            previousFrame.delete();
                        }
                        latestFrame = "frames/" + changed.toString();
                        File f = new File(latestFrame);
                        if (f.exists()) {
                            updateFrame();
                        }
                    }
                }
                // reset the key
                boolean valid = wk.reset();
                if (!valid) {
                    System.out.println("Key has been unregistered");
                }
            }
    } catch(IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updateFrame() {
        label.setIcon(new ImageIcon(cv.DetectFaces(latestFrame)));
    }

    private void clearFrameDirectory() {
        try {
            FileUtils.cleanDirectory(new File(pathToFrameDir));
        } catch (IOException e) {
            System.out.println("IOException while clearing frame directory: " + e.getMessage());
        }
    }
}
