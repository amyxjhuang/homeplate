#include <opencv2/opencv.hpp>
#include <iostream>

using namespace cv;
using namespace std;

int main() {
    // Load the image
    Mat image = imread("test1.jpg");
    if (image.empty()) {
        cout << "Could not open or find the image" << endl;
        return -1;
    }

    // Convert to grayscale
    Mat gray;
    cvtColor(image, gray, COLOR_BGR2GRAY);

    // Apply Gaussian blur
    Mat blurred;
    GaussianBlur(gray, blurred, Size(5, 5), 0);

    // Edge detection
    Mat edges;
    Canny(blurred, edges, 50, 150);

    // Find contours
    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;
    findContours(edges, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

    for (size_t i = 0; i < contours.size(); i++) {
        vector<Point> approx;
        double peri = arcLength(contours[i], true);
        approxPolyDP(contours[i], approx, 0.02 * peri, true);

        // A home plate is a 5-sided polygon
        if (approx.size() == 5) {
            drawContours(image, vector<vector<Point>>{approx}, -1, Scalar(0, 255, 0), 3);
        }
    }

    // Display the result
    imshow("Home Plate Detection", image);
    waitKey(0);
    return 0;
}
