#include <opencv2/opencv.hpp>
#include <iostream>

using namespace cv;
using namespace std;

bool isPointWithinBounds(Point point, Mat image) {
    // Assume that the home plate is within these bounds
    double topMargin = 0.4; 
    double bottomMargin = 0.1;
    double leftMargin = 0.1;
    double rightMargin = 0.1;

    // Calculate the center of the image
    int imageWidth = image.cols;
    int imageHeight = image.rows;

        
    int leftBound = imageWidth * leftMargin;
    int rightBound = imageWidth * (1 - rightMargin);
    int topBound = imageHeight * topMargin;
    int bottomBound = imageHeight * (1 - bottomMargin);
    return (point.x > leftBound && point.x < rightBound && point.y > topBound && point.y < bottomBound);
}

SimpleBlobDetector::Params getBlobParams(Mat image) {
    SimpleBlobDetector::Params params;

    // params.filterByArea = true;
    // params.minArea = 100;
    // params.maxArea = 0.05 * image.cols * image.rows;  // Same max area as before
    
    // Filter by circularity
    params.filterByCircularity = true;
    params.minCircularity = 0.1;
    
    // Filter by convexity
    params.filterByConvexity = true;
    params.minConvexity = 0.5;
    
    // Filter by inertia (elongation)
    params.filterByInertia = true;
    params.maxInertiaRatio = 0.80;
    return params;

}

int main() {
    Mat image = imread("images/test1.jpg");
    if (image.empty()) {
        cout << "Error reading image" << endl;
        return -1;
    }
    cout << "Converting to gray & gaussian blur" << endl;

    Mat gray;
    cvtColor(image, gray, COLOR_BGR2GRAY);

    Mat blurred;
    GaussianBlur(gray, blurred, Size(5, 5), 0);

    Mat edges;
    Canny(blurred, edges, 50, 150);

    cout << "Getting blob params for blob detection" << endl;
    SimpleBlobDetector::Params blobParams = getBlobParams(image);
    Ptr<SimpleBlobDetector> detector = SimpleBlobDetector::create(blobParams);
    
    // Detect blobs
    vector<KeyPoint> keypoints;
    // detector->detect(blurred, keypoints);

    Mat blobMask;
    detector->detect(gray, keypoints, blobMask);
    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;
    cout << "Finding blob contours" << endl;
    findContours(blobMask, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

    // Draw detected blob contours in red
    for (const auto& contour : contours) {
        cout << "Drawing blob contours" << endl;
        drawContours(image, vector<vector<Point>>{contour}, 0, Scalar(0, 0, 255), 2);
    }
    
    // Draw detected blobs in red
    // for (const KeyPoint& kp : keypoints) {
    //     if (isPointWithinBounds(Point(kp.pt.x, kp.pt.y), image)) {
    //         circle(image, Point(kp.pt.x, kp.pt.y), kp.size/2, Scalar(0, 0, 255), 2);
    //     }
    // }
    // }


    // vector<vector<Point>> contours;
    // vector<Vec4i> hierarchy;
    // findContours(edges, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

    // Let's assume the polygon can't be more than 1/5th the width of the image, 
    // or more than 5% of the area of the image 
    double maxPerimeter = 0.2 * image.cols;
    double maxArea = 0.05 * image.cols * image.rows;

    for (size_t i = 0; i < contours.size(); i++) {
        vector<Point> approx;
        double perimeter = arcLength(contours[i], true);
        double epsilon = 0.018 * perimeter;
        bool closed = true;



        approxPolyDP(contours[i], approx, epsilon, closed);

        double area = contourArea(contours[i]);

        // // Assuming we could see 5 points
        if (approx.size() > 2 && approx.size() < 7 && perimeter < maxPerimeter && area < maxArea) {
            Moments m = moments(contours[i]);
            Point2f center(m.m10/m.m00, m.m01/m.m00);

            if (isPointWithinBounds(center, image)) {
                drawContours(image, vector<vector<Point>>{approx}, -1, Scalar(0, 255, 0), 1);
            }
        }
    }

    imshow("Home Plate Detection", image);
    waitKey(0);
    return 0;
}
