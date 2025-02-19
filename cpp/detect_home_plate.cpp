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

SimpleBlobDetector::Params getBlobParams() {
    SimpleBlobDetector::Params params;

    params.maxArea = 1300;
    params.minThreshold = 254;
    params.maxThreshold = 255;
    params.minRepeatability = 1;
    
    params.filterByArea = true;
    params.minArea = 700;
    
    params.filterByCircularity = true;
    params.minCircularity = 0.3f;
    params.maxCircularity = 0.6f;
    
    params.filterByConvexity = false;
    params.minConvexity = 0.1f;
    
    params.filterByInertia = false;
    params.minInertiaRatio = 0.11f;
    return params;

}

void CustomHomePlateFind() {
    std::string ImagePath = "../images/test1.jpg";
    
    std::cout << "FileName " << ImagePath << std::endl;
    
    cv::Mat matBGR = cv::imread(ImagePath);
    
    if (matBGR.empty()) {
        std::cout << "NOT OPENED" << std::endl;
        return;
    }
    
    std::cout << "OPENED cols " << matBGR.cols << " rows " << matBGR.rows << std::endl;
    
    cv::Mat matGray, hsvFrame;
    cv::cvtColor(matBGR, matGray, cv::COLOR_BGR2GRAY);
    cv::cvtColor(matBGR, hsvFrame, cv::COLOR_BGR2HSV);
    
    cv::Scalar lowerBound(1, 10, 190);
    cv::Scalar upperBound(50, 160, 255);
    
    cv::inRange(hsvFrame, lowerBound, upperBound, hsvFrame);
    
    cv::morphologyEx(hsvFrame, hsvFrame, cv::MORPH_DILATE, cv::getStructuringElement(cv::MORPH_ELLIPSE, cv::Size(3, 3)));
    
    cv::bitwise_not(hsvFrame, hsvFrame);
    
    // Blob detector parameters
    cv::SimpleBlobDetector::Params params = getBlobParams();
    
    // Create detector
    cv::Ptr<cv::SimpleBlobDetector> detector = cv::SimpleBlobDetector::create(params);
    
    // Detect blobs
    std::vector<cv::KeyPoint> keypoints;
    detector->detect(hsvFrame, keypoints);
    
    std::cout << "Keypoints found " << keypoints.size() << std::endl;
    
    for (const auto& key : keypoints) {
        std::cout << "Keypoint found size " << key.size << " x " << key.pt.x << " y " << key.pt.y << std::endl;
    }
    
    // Draw keypoints
    cv::Mat imageWithKeypoints;
    cv::cvtColor(matGray, imageWithKeypoints, cv::COLOR_GRAY2BGR);
    cv::drawKeypoints(imageWithKeypoints, keypoints, imageWithKeypoints, cv::Scalar(0, 0, 255), cv::DrawMatchesFlags::DRAW_RICH_KEYPOINTS);
    
    // Display the result
    cv::imshow("Found Blobs", imageWithKeypoints);
    cv::waitKey(0);
}

int main() {
    // Mat image = imread("../images/test1.jpg");
    // if (image.empty()) {
    //     cout << "Error reading image" << endl;
    //     return -1;
    // }
    // cout << "Converting to gray & gaussian blur" << endl;

    // Mat gray;
    // cvtColor(image, gray, COLOR_BGR2GRAY);

    // Mat blurred;
    // GaussianBlur(gray, blurred, Size(5, 5), 0);

    // Mat edges;
    // Canny(blurred, edges, 50, 150);

    // SimpleBlobDetector::Params blobParams = getBlobParams(image);
    // Ptr<SimpleBlobDetector> detector = SimpleBlobDetector::create(blobParams);
    
    // // Detect blobs
    // vector<KeyPoint> keypoints;
    // detector->detect(blurred, keypoints);
    
    // // Draw detected blobs in red
    // for (const KeyPoint& kp : keypoints) {
    //     if (isPointWithinBounds(Point(kp.pt.x, kp.pt.y), image)) {
    //         circle(image, Point(kp.pt.x, kp.pt.y), kp.size/2, Scalar(0, 0, 255), 2);
    //     }
    // }

    // vector<vector<Point>> contours;
    // vector<Vec4i> hierarchy;
    // findContours(edges, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

    // // Let's assume the polygon can't be more than 1/5th the width of the image, 
    // // or more than 5% of the area of the image 
    // double maxPerimeter = 0.2 * image.cols;
    // double maxArea = 0.05 * image.cols * image.rows;

    // for (size_t i = 0; i < contours.size(); i++) {
    //     vector<Point> approx;
    //     double perimeter = arcLength(contours[i], true);
    //     double epsilon = 0.018 * perimeter;
    //     bool closed = true;



    //     approxPolyDP(contours[i], approx, epsilon, closed);

    //     double area = contourArea(contours[i]);

    //     // // Assuming we could see 5 points
    //     if (approx.size() > 2 && approx.size() < 7 && perimeter < maxPerimeter && area < maxArea) {
    //         Moments m = moments(contours[i]);
    //         Point2f center(m.m10/m.m00, m.m01/m.m00);

    //         if (isPointWithinBounds(center, image)) {
    //             drawContours(image, vector<vector<Point>>{approx}, -1, Scalar(0, 255, 0), 1);
    //         }
    //     }
    // }

    // imshow("Home Plate Detection", image);
    // waitKey(0);
    CustomHomePlateFind();
    return 0;
}
