import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.features2d.SimpleBlobDetector_Params;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class HomePlateApp {
    
    // Load the native OpenCV library.
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    
    public static void CustomHomePlateFind() {
        // Set your image file path.
        String imagePath = "/Users/varundeliwala/NYU/Projects/visaball/images/test0.jpg";
        System.out.println("FileName: " + imagePath);
        
        // Read the image.
        Mat matBGR = Imgcodecs.imread(imagePath);
        if (matBGR == null || matBGR.empty()) {
            System.out.println("NOT OPENED");
            return;
        }
        System.out.println("OPENED cols: " + matBGR.cols() + " rows: " + matBGR.rows());
        
        // Convert the image to grayscale and HSV.
        Mat matGray = new Mat();
        Mat hsvFrame = new Mat();
        Imgproc.cvtColor(matBGR, matGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(matBGR, hsvFrame, Imgproc.COLOR_BGR2HSV);
        
        // Threshold the HSV image.
        Scalar lowerBound = new Scalar(1, 10, 190);
        Scalar upperBound = new Scalar(50, 160, 255);
        Core.inRange(hsvFrame, lowerBound, upperBound, hsvFrame);
        
        // Apply a dilation followed by a bitwise inversion.
        Imgproc.morphologyEx(hsvFrame, hsvFrame, Imgproc.MORPH_DILATE,
                Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3)));
        Core.bitwise_not(hsvFrame, hsvFrame);
        
        // Set up the blob detector parameters.
        SimpleBlobDetector_Params params = new SimpleBlobDetector_Params();
        params.set_maxArea(1300);
        params.set_minThreshold(254);
        params.set_maxThreshold(255);
        params.set_minRepeatability(1);
        params.set_filterByArea(true);
        params.set_minArea(700);
        params.set_filterByCircularity(true);
        params.set_minCircularity(0.3f);
        params.set_maxCircularity(0.6f);
        params.set_filterByConvexity(false);
        params.set_minConvexity(0.1f);
        params.set_filterByInertia(false);
        params.set_minInertiaRatio(0.11f);
        
        // Create the blob detector and detect keypoints.
        SimpleBlobDetector detector = SimpleBlobDetector.create(params);
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        detector.detect(hsvFrame, keypoints);
        
        System.out.println("Keypoints found: " + keypoints.size());
        for (KeyPoint key : keypoints.toList()) {
            System.out.println("Keypoint found, size: " + key.size + ", x: " + key.pt.x + ", y: " + key.pt.y);
        }
        
        // Convert the grayscale image to BGR for visualization and draw keypoints.
        Mat imageWithKeypoints = new Mat();
        Imgproc.cvtColor(matGray, imageWithKeypoints, Imgproc.COLOR_GRAY2BGR);
        Features2d.drawKeypoints(imageWithKeypoints, keypoints, imageWithKeypoints,
                new Scalar(0, 0, 255), Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS);
        
        // Display the result.
        HighGui.imshow("Found Blobs", imageWithKeypoints);
        HighGui.waitKey(0);
        HighGui.destroyAllWindows();
    }
    
    public static void main(String[] args) {
        CustomHomePlateFind();
    }
}
