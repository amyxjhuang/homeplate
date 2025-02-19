#include <opencv2/opencv.hpp>
#include <iostream>

using namespace cv;
using namespace std;

cv::Rect getBoundsRect(const cv::Mat& image) {

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
    return cv::Rect(
        leftBound,                    
        topBound,                     
        rightBound - leftBound,    
        bottomBound - topBound      
    );

}

bool isPointWithinBounds(const cv::KeyPoint& keypoint, const cv::Mat& image) {
    // Assume that the home plate is within these bounds
    double topMargin = 0.4; 
    double bottomMargin = 0.1;
    double leftMargin = 0.1;
    double rightMargin = 0.1;

    // Calculate the center of the image
    int imageWidth = image.cols;
    int imageHeight = image.rows;
        
    int leftBound = imageWidth * leftMargin;
    int rightBound = imageWidth * (1 - leftMargin);
    int topBound = imageHeight * topMargin;
    int bottomBound = imageHeight * (1 - bottomMargin);

    return (keypoint.pt.x > leftBound && keypoint.pt.x < rightBound && 
            keypoint.pt.y > topBound && keypoint.pt.y < bottomBound);
}

SimpleBlobDetector::Params getBlobParams() {
    SimpleBlobDetector::Params params;

    params.minThreshold = 200;
    params.maxThreshold = 255;
    params.minRepeatability = 1;
    
    // params.filterByArea = true;
    // params.minArea = 500;
    // params.maxArea = 1300;

    params.filterByCircularity = true;
    params.minCircularity = 0.1f;
    params.maxCircularity = 0.9f;
    
    params.filterByConvexity = false;
    params.minConvexity = 0.1f;
    
    params.filterByInertia = true;
    params.minInertiaRatio = 0.11f;
    return params;

}

void CustomHomePlateFind(const std::string& ImagePath) {    
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
    
    std::vector<cv::KeyPoint> filteredKeypoints;
    for (const auto& key : keypoints) {
        if (isPointWithinBounds(key, matGray)) {
            std::cout << "Keypoint found size " << key.size << " x " << key.pt.x << " y " << key.pt.y << std::endl;
            filteredKeypoints.push_back(key);
            
        }
    
    }
    
    // Draw keypoints
    cv::Mat imageWithKeypoints;
    cv::cvtColor(matGray, imageWithKeypoints, cv::COLOR_GRAY2BGR);
    cv::drawKeypoints(imageWithKeypoints, filteredKeypoints, imageWithKeypoints, cv::Scalar(0, 0, 255), cv::DrawMatchesFlags::DRAW_RICH_KEYPOINTS);
    cv::rectangle(imageWithKeypoints, getBoundsRect(imageWithKeypoints), cv::Scalar(0,255,0), 2);
    // Display the result
    cv::imshow("Found Blobs", imageWithKeypoints);
    cv::waitKey(0);
}

int main(int argc, char** argv) {
    if (argc != 2) {
        std::cout << "No path to image specified, using default" << std::endl;
        CustomHomePlateFind("../images/test1.jpg");

        return -1;
    }
    
    CustomHomePlateFind(argv[1]);
    return 0;
}