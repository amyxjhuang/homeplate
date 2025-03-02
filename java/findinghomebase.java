	
	public static void CustomHomePlateFind()
	{
		
// 		String ImagePath = "C:/Steve/TestData/HomeBase/homebasefrompitchersmound 45feet.jpg";
// 		String ImagePath = "C:/Steve/TestData/HomeBase/20250211_120314.jpg";  // works
// 		String ImagePath = "C:/Steve/TestData/HomeBase/20250211_120325.jpg";  // works 
// 		String ImagePath = "C:/Steve/TestData/HomeBase/20250211_120442.jpg";  // 
// 		String ImagePath = "C:/Steve/TestData/HomeBase/20250211_120448.jpg";  //  
// 		String ImagePath = "C:/Steve/TestData/HomeBase/20250211_120455.jpg";  // 
// 		String ImagePath = "C:/Steve/TestData/HomeBase/homebasefrompitchersmound 45feet.jpg";
//		String ImagePath = "C:/Steve/TestData/HomeBase/New Folder/20250225_142346.jpg";
//		String ImagePath = "C:/Steve/TestData/HomeBase/New Folder/20250225_142608.jpg";
//		String ImagePath = "C:/Steve/TestData/HomeBase/New Folder/20250225_142355.jpg";
//		String ImagePath = "C:/Steve/TestData/HomeBase/New Folder/20250225_142403.jpg";
//		String ImagePath = "C:/Steve/TestData/HomeBase/New Folder/20250225_142418.jpg";
//		String ImagePath = "C:/Steve/TestData/HomeBase/New Folder/20250225_142503.jpg";
		
		// second batch test data
// 0BDDD287-5B92-4DFE-9DCC-2F87AE27C4BB	61FB4488-B39E-4F5B-9795-ECFAFD68D468
//		String ImagePath = "C:/Steve/TestData/HomeBase/Amy/8DCE0991-0B8F-464F-935C-42C37E618F55.jpg";  
//		String ImagePath = "C:/Steve/TestData/HomeBase/Amy/61FB4488-B39E-4F5B-9795-ECFAFD68D468.jpg";
//		String ImagePath = "C:/Steve/TestData/HomeBase/Amy/2932D5D9-D15B-4856-982F-3BF6A6A5D36F.jpg";  
		String ImagePath = "C:/Steve/TestData/HomeBase/Amy/CDD4A4B3-9326-4DAF-BF47-7647606B314D.jpg";  
//		String ImagePath = "C:/Steve/TestData/HomeBase/Amy/CDD4A4B3-9326-4DAF-BF47-7647606B314D.jpg";  

//  IMG_0927		
		
		
 		System.out.println("FileName " + ImagePath);
		
 		Mat matBGR = new Mat();
	
 		try
 		{
 			matBGR = Imgcodecs.imread(ImagePath); //, Imgcodecs.IMREAD_GRAYSCALE);
 		}
 		catch(Exception ex)
 		{
 			System.out.println("Exception " + ex.getMessage());
 		}
		
 		if(matBGR == null || matBGR.empty())
 		{
 			System.out.println("NOT OPENED");

 			return;
 		}
		
		
		System.out.println("OPENED cols " + matBGR.cols() + " rows " + matBGR.rows() );

		FindHomePlate(matBGR);
		
	}
	
	public static void FindHomePlate(Mat matBGR)
	{
		boolean b2kImage = false;
		int MinArea = 300;
//        Mat matGray = new Mat();
        Mat hsvFrame = new Mat();
        
        System.out.println("Image Width " + matBGR.cols() + " Height " + matBGR.rows());
        
        if(matBGR.cols() < 2600)
        {
//            Imgproc.resize();
            
            Imgproc.resize(matBGR, matBGR, new org.opencv.core.Size(0, 0), 2, 2); //,   INTER_LINEAR_EXACT); //, INTER_CUBIC);  // INTER_LINEAR_EXACT );
            
        }
        
        if(matBGR.cols() < 2600)  // to include 2560x144
        {
        	b2kImage = true;
        	MinArea = MinArea / 2;
        }
        
//        Imgproc.cvtColor(matBGR, matGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(matBGR, hsvFrame, Imgproc.COLOR_BGR2HSV);
        
        Scalar lowerBound = null; // new Scalar(0,0,0);
        Scalar upperBound = null; //new Scalar(0,0,0);
        
        // H  20-60  .711 degrees to percentage  (256/360)  
        // S  10-35
        // V  80-95
        lowerBound = new Scalar(0 ,25 , 180 ); // new Scalar(30, 255, 255);  // cv::Scalar(30, 100, 100);
        upperBound = new Scalar(30 ,80 , 255);

        Core.inRange(hsvFrame, lowerBound, upperBound, hsvFrame);
        
    	Imgproc.morphologyEx(hsvFrame, hsvFrame, Imgproc.MORPH_OPEN, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));  // TODO te
    	Imgproc.morphologyEx(hsvFrame, hsvFrame, Imgproc.MORPH_DILATE, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));  // TODO te

        Core.bitwise_not(hsvFrame, hsvFrame);

        Mat resizeImage = new Mat();
        Imgproc.resize(hsvFrame, resizeImage, new Size(0, 0), .5, .5);

        HighGui.imshow("Thresholded", resizeImage);
        HighGui.waitKey(0);

        resizeImage.release();
        
        ///////////////
        // Setup SimpleBlobDetector parameters.
        SimpleBlobDetector_Params params = new SimpleBlobDetector_Params();

        // Change thresholds
        params.set_maxArea(1200);
        params.set_minThreshold(254);
        params.set_maxThreshold(255);
//        params.set_minThreshold(0);
//        params.set_maxThreshold(1);
        params.set_minRepeatability(1);

        // Filter by Area.
        params.set_filterByArea(true);
        params.set_minArea(MinArea);

        // Filter by Circularity
        params.set_filterByCircularity(true);
        params.set_minCircularity(0.15f);
        params.set_maxCircularity(.40f);

        // Filter by Convexity
        params.set_filterByConvexity(true);
        params.set_minConvexity(0.85f);
        params.set_maxConvexity(1f);

        // Filter by Inertia
        params.set_filterByInertia(false);
        params.set_minInertiaRatio(0.11f);

       // Create a detector with the parameters
        SimpleBlobDetector detector = SimpleBlobDetector.create(params);

        // Detect blobs
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        detector.detect(hsvFrame, keypoints);
        
        System.out.println("Keypoints found " + keypoints.size());
        
        ArrayList<KeyPoint> keyPointList;
        
        List<KeyPoint> kpl = keypoints.toList();
        keyPointList = new ArrayList<>();
        for (KeyPoint key: kpl)
        {
            // TODO if key is contained in another key, use only largest one, meaning findblobs found 2
            // blobs within the same area.
            // this will eliminate the smaller blobs from the list.
        	
//        	if(key.size < 30 || key.size > 50)
//        		continue;
        	if(VerifyHomePlate(hsvFrame, key)) 
        	{
        		keyPointList.add(key);
        		System.out.println("Keypoint found size " + key.size + " x " + key.pt.x + " y "+ key.pt.y + " angle " + key.angle + " " + key.response + " " + key.octave);         
        	}
            
            
        }
        
        //  Location of homebase approximately is 1980 x 1510

              
        // Draw keypoints on the image
        Mat imageWithKeypoints = new Mat();
        Features2d.drawKeypoints(matBGR, keypoints, imageWithKeypoints, new Scalar(0,0, 255), Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS);

        
        // Save or display the result
//        Imgcodecs.imwrite("C:/Steve/TestData/homebaseprocessed.jpg", imageWithKeypoints); // Save the result
        // (Optional) Display the image (requires a UI framework like Swing or JavaFX)
 

        Imgproc.resize(imageWithKeypoints, imageWithKeypoints, new Size(0, 0), .5, .5);
        
        HighGui.imshow("Found Blobs ", imageWithKeypoints);
        HighGui.waitKey(0);
        
        // Extract rectangle for dewarping
        
        
        // dewarp!
/*        
        Mat perspectiveTransformMatrix = Imgproc.getPerspectiveTransform(sourcePoints, destinationPoints);
        
        Size outputSize = new Size(width, height); // Desired dimensions of the dewarped image
        Mat dewarpedImage = new Mat();
        Imgproc.warpPerspective(distortedImage, dewarpedImage, perspectiveTransformMatrix, outputSize);
  */      
        
		
	}
	
	public static boolean VerifyHomePlate(Mat image, KeyPoint key)
	{
		boolean bRV = false;
		
		// extract out around the keypoint
		
		// find rectangles in image
		// Convert the image to grayscale
        Mat gray = new Mat();
//        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
        gray = image;

        // Apply Gaussian blur to reduce noise
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(gray, blurred, new Size(5, 5), 0);

        // Detect edges using Canny edge detector
        Mat edges = new Mat();
        Imgproc.Canny(blurred, edges, 50, 150);

        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Iterate through contours and approximate them to polygons
        for (MatOfPoint contour : contours) {
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

            // Convert the approximated contour to a MatOfPoint
            MatOfPoint points = new MatOfPoint(approxCurve.toArray());

            // Check if the polygon has 4 vertices (a quadrilateral)
            if (points.toArray().length == 4) {
                // Calculate the area of the contour
                double area = Imgproc.contourArea(contour);

                // Set a minimum area threshold to filter out small contours
                if (area > 300) {
                    // Draw the rectangle
                    Rect rect = Imgproc.boundingRect(points);
                    Imgproc.rectangle(image, rect.tl(), rect.br(), new Scalar(0, 255, 0), 4);
                    System.out.println("Rectangle found, area " + area);
                    System.out.println("Rectangle topleft " + rect.tl() + " bottomright " + rect.br());
                    System.out.println("Rectangle width " + rect.width + " height " + rect.height);
                    
                    // TODO make sure rectangle has aspect ration at least 4 to 1 long not tall.
                    
                    if(rect.width > rect.height)
                    {
                        System.out.println("*** Rectangle width > height");
                   	
                    }

                }
            }
        }
		
		// determine angle of lines, should be 0 or 180, not 90 or down to 30 degrees.
		
		//
		
		bRV = true;
		
		return bRV;
	}
