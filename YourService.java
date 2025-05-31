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

        // Coordinates for four areas (example)
        Point[] areaPoints = new Point[] {
            new Point(10.95, -10.0, 5.2),
            new Point(10.95, -9.0, 3.8),
            new Point(10.95, -8.0, 3.8),
            new Point(9.87, -6.9, 5.0)
        };

        Quaternion forwardFacing = new Quaternion(0f, 0f, -0.707f, 0.707f);

        // Visit each area and recognize items
        for (int i = 0; i < areaPoints.length; i++) {
            moveToWithRetry(areaPoints[i], forwardFacing);

            // Capture image from NavCam
            Mat image = api.getMatNavCam();

            // Image recognition logic goes here
            // For now, simulate recognition with dummy data
            api.setAreaInfo(i + 1, "coral", 1);
        }

        // Move to astronaut and report rounding completion
        Point astronaut = new Point(11.143, -6.7607, 4.9654);
        Quaternion astronautOri = new Quaternion(0f, 0f, 0.707f, 0.707f);
        moveToWithRetry(astronaut, astronautOri);
        api.reportRoundingCompletion();

        // Notify recognized item (simulated)
        api.notifyRecognitionItem();

        // Move to target item (example coordinates)
        Point targetItem = new Point(10.95, -7.0, 5.0);
        moveToWithRetry(targetItem, forwardFacing);

        // Take snapshot
        api.takeTargetItemSnapshot();

        // Flash signal light
        api.flashlightControlFront(0.05f);
        sleep(2000);
        api.flashlightControlFront(0.0f);
    }

    private void moveToWithRetry(Point point, Quaternion quaternion) {
        final int LOOP_MAX = 5;
        int count = 0;
        boolean success = false;

        while (count < LOOP_MAX && !success) {
            success = api.moveTo(point, quaternion, true).hasSucceeded();
            count++;
        }

        if (!success) {
            api.sendDiscoveredError("Failed to move to point after retries.");
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Handle exception if needed
        }
    }
}
