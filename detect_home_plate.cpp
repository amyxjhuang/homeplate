#include <opencv2/opencv.hpp>
#include <iostream>

using namespace cv;
using namespace std;

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

    vector<vector<Point>> contours;
    vector<Vec4i> hierarchy;
    findContours(edges, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

    for (size_t i = 0; i < contours.size(); i++) {
        vector<Point> approx;
        double peri = arcLength(contours[i], true);
        approxPolyDP(contours[i], approx, 0.02 * peri, true);

        // Assuming we could see 5 points
        // if (approx.size() == 5) {
            drawContours(image, vector<vector<Point>>{approx}, -1, Scalar(0, 255, 0), 3);
        // }
    }

    imshow("Home Plate Detection", image);
    waitKey(0);
    return 0;
}
