#include <opencv2/opencv.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/features2d.hpp>
#include <iostream>

using namespace cv;
using namespace std;

void FindHomePlate(Mat &matBGR);
bool VerifyHomePlate(Mat &image, KeyPoint key);

void CustomHomePlateFind() {
    string ImagePath = "C:/Steve/TestData/HomeBase/Amy/CDD4A4B3-9326-4DAF-BF47-7647606B314D.jpg";
    
    cout << "FileName: " << ImagePath << endl;
    
    Mat matBGR = imread(ImagePath);
    
    if (matBGR.empty()) {
        cout << "NOT OPENED" << endl;
        return;
    }
    
    cout << "OPENED cols: " << matBGR.cols << " rows: " << matBGR.rows << endl;
    
    FindHomePlate(matBGR);
}

void FindHomePlate(Mat &matBGR) {
    bool b2kImage = false;
    int MinArea = 300;
    Mat hsvFrame;
    
    cout << "Image Width: " << matBGR.cols << " Height: " << matBGR.rows << endl;
    
    if (matBGR.cols < 2600) {
        resize(matBGR, matBGR, Size(), 2, 2);
    }
    
    if (matBGR.cols < 2600) {
        b2kImage = true;
        MinArea /= 2;
    }
    
    cvtColor(matBGR, hsvFrame, COLOR_BGR2HSV);
    
    Scalar lowerBound(0, 25, 180);
    Scalar upperBound(30, 80, 255);
    
    inRange(hsvFrame, lowerBound, upperBound, hsvFrame);
    
    morphologyEx(hsvFrame, hsvFrame, MORPH_OPEN, getStructuringElement(MORPH_RECT, Size(2, 2)));
    morphologyEx(hsvFrame, hsvFrame, MORPH_DILATE, getStructuringElement(MORPH_RECT, Size(2, 2)));
    
    bitwise_not(hsvFrame, hsvFrame);
    
    Mat resizeImage;
    resize(hsvFrame, resizeImage, Size(), 0.5, 0.5);
    
    imshow("Thresholded", resizeImage);
    waitKey(0);
    
    resizeImage.release();
    
    Ptr<SimpleBlobDetector> detector = SimpleBlobDetector::create();
    vector<KeyPoint> keypoints;
    detector->detect(hsvFrame, keypoints);
    
    cout << "Keypoints found: " << keypoints.size() << endl;
    
    vector<KeyPoint> keyPointList;
    
    for (const auto &key : keypoints) {
        if (VerifyHomePlate(hsvFrame, key)) {
            keyPointList.push_back(key);
            cout << "Keypoint found size " << key.size << " x " << key.pt.x << " y " << key.pt.y << endl;
        }
    }
    
    Mat imageWithKeypoints;
    drawKeypoints(matBGR, keypoints, imageWithKeypoints, Scalar(0, 0, 255), DrawMatchesFlags::DRAW_RICH_KEYPOINTS);
    
    resize(imageWithKeypoints, imageWithKeypoints, Size(), 0.5, 0.5);
    
    imshow("Found Blobs", imageWithKeypoints);
    waitKey(0);
}

bool VerifyHomePlate(Mat &image, KeyPoint key) {
    Mat gray, blurred, edges;
    gray = image;
    
    GaussianBlur(gray, blurred, Size(5, 5), 0);
    Canny(blurred, edges, 50, 150);
    
    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;
    findContours(edges, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
    
    for (const auto &contour : contours) {
        vector<Point> approxCurve;
        double approxDistance = arcLength(contour, true) * 0.02;
        approxPolyDP(contour, approxCurve, approxDistance, true);
        
        if (approxCurve.size() == 4) {
            double area = contourArea(contour);
            if (area > 300) {
                Rect rect = boundingRect(approxCurve);
                rectangle(image, rect.tl(), rect.br(), Scalar(0, 255, 0), 4);
                cout << "Rectangle found, area: " << area << endl;
                cout << "Rectangle topleft: " << rect.tl() << " bottomright: " << rect.br() << endl;
                if (rect.width > rect.height) {
                    cout << "*** Rectangle width > height" << endl;
                }
            }
        }
    }
    return true;
}
