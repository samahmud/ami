package jp.jaxa.iss.kibo.rpc.sampleapk;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;
import geometry_msgs.Point;
import geometry_msgs.Quaternion;
import org.opencv.core.Mat;

public class YourService extends KiboRpcService {

    @Override
    protected void runPlan1() {
        // Start the mission
        api.startMission();

        // Coordinates and orientation
        Point[] areaPoints = {
            new Point(10.95, -10.0, 5.2),
            new Point(10.95, -9.0, 3.8),
            new Point(10.95, -8.0, 3.8),
            new Point(9.87, -6.9, 5.0)
        };

        Quaternion forwardOrientation = new Quaternion(0f, 0f, -0.707f, 0.707f);

        // Visit each area and simulate recognition
        for (int i = 0; i < areaPoints.length; i++) {
            moveToWithRetry(areaPoints[i], forwardOrientation);
            Mat image = api.getMatNavCam();

            // Simulate recognition (you can add actual OpenCV processing here)
            api.setAreaInfo(i + 1, "coral", 1);

            // Optionally save the image for debugging
            api.saveMatImage(image, "area" + (i + 1) + "_debug");
        }

        // Move to astronaut and report completion
        Point astronaut = new Point(11.143, -6.7607, 4.9654);
        Quaternion astronautOrientation = new Quaternion(0f, 0f, 0.707f, 0.707f);
        moveToWithRetry(astronaut, astronautOrientation);
        api.reportRoundingCompletion();

        // Recognize and notify the target item
        api.notifyRecognitionItem();

        // Move to the identified target item
        Point targetItem = new Point(10.95, -7.0, 5.0);
        moveToWithRetry(targetItem, forwardOrientation);

        // Take snapshot of the item
        api.takeTargetItemSnapshot();

        // Flash signal to indicate mission completion
        api.flashlightControlFront(0.05f);
        sleep(2000);
        api.flashlightControlFront(0.0f);
    }

    private void moveToWithRetry(Point point, Quaternion quaternion) {
        final int MAX_RETRIES = 5;
        int attempt = 0;
        boolean success = false;

        while (attempt < MAX_RETRIES && !success) {
            success = api.moveTo(point, quaternion, true).hasSucceeded();
            attempt++;
        }

        if (!success) {
            api.sendDiscoveredError("Failed to reach point after " + MAX_RETRIES + " attempts.");
        }
    }

    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
