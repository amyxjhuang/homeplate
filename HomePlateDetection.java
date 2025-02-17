import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.features2d.SimpleBlobDetector_Params;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import java.util.ArrayList;
import java.util.List;

public class HomePlateDetection {
    
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static boolean isPointWithinBounds(Point point, Mat image) {
        double topMargin = 0.4;
        double bottomMargin = 0.1;
        double leftMargin = 0.1;
        double rightMargin = 0.1;

        int imageWidth = image.cols();
        int imageHeight = image.rows();

        int leftBound = (int) (imageWidth * leftMargin);
        int rightBound = (int) (imageWidth * (1 - rightMargin));
        int topBound = (int) (imageHeight * topMargin);
        int bottomBound = (int) (imageHeight * (1 - bottomMargin));

        return (point.x > leftBound && point.x < rightBound && point.y > topBound && point.y < bottomBound);
    }

    public static SimpleBlobDetector createBlobDetector(Mat image) {
        SimpleBlobDetector_Params params = new SimpleBlobDetector_Params();

        params.set_filterByArea(true);
        params.set_minArea(100);
        params.set_maxArea(0.05 * image.cols() * image.rows());

        params.set_filterByConvexity(true);
        params.set_minConvexity(0.5);

        params.set_filterByInertia(true);
        params.set_minInertiaRatio(0.01);

        return SimpleBlobDetector.create(params);
    }

    public static void main(String[] args) {
        String filePath = "images/test1.jpg"; // Update with your actual image path
        Mat image = Imgcodecs.imread(filePath);

        if (image.empty()) {
            System.out.println("Error reading image");
            return;
        }
        System.out.println("Converting to gray & applying Gaussian blur");

        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        Mat blurred = new Mat();
        Imgproc.GaussianBlur(gray, blurred, new Size(5, 5), 0);

        Mat edges = new Mat();
        Imgproc.Canny(blurred, edges, 50, 150);

        SimpleBlobDetector detector = createBlobDetector(image);
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        detector.detect(blurred, keypoints);

        for (KeyPoint kp : keypoints.toArray()) {
            Point point = new Point(kp.pt.x, kp.pt.y);
            if (isPointWithinBounds(point, image)) {
                Imgproc.circle(image, point, (int) (kp.size / 2), new Scalar(0, 0, 255), 2);
            }
        }

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxPerimeter = 0.2 * image.cols();
        double maxArea = 0.05 * image.cols() * image.rows();

        for (MatOfPoint contour : contours) {
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            double perimeter = Imgproc.arcLength(contour2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(contour2f, approx, 0.018 * perimeter, true);

            double area = Imgproc.contourArea(contour);
            if (approx.total() > 2 && approx.total() < 7 && perimeter < maxPerimeter && area < maxArea) {
                Moments m = Imgproc.moments(contour);
                Point center = new Point(m.m10 / m.m00, m.m01 / m.m00);

                if (isPointWithinBounds(center, image)) {
                    Imgproc.drawContours(image, List.of(new MatOfPoint(approx.toArray())), -1, new Scalar(0, 255, 0), 1);
                }
            }
        }

        Imgcodecs.imwrite("output.jpg", image);
        System.out.println("Processing complete. Output saved as output.jpg.");
    }
}
