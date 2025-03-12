

// in the calling program

		int HSVValue = CustomProcessLightIndexHSVValueBaseball(matBGR);
		
		// cant have a low HSV for value.
		if(HSVValue < 160)
			HSVValue = 160;
		
		FindHomePlate(matBGR, HSVValue);


// In the function FindHomePlate use the value calculated in CustomProcessLightIndexHSVValueBaseball()
        lowerBound = new Scalar(0 ,25 , HSVValue ); // new Scalar(30, 255, 255);  // cv::Scalar(30, 100, 100);
        upperBound = new Scalar(30 ,70 , 255);



// The new function

	public static int CustomProcessLightIndexHSVValueBaseball(Mat matBGR)
	{		
	

		Mat matHSV = new Mat();
				
        if(matBGR.cols() < 2600)
        {
//            Imgproc.resize();
            
            Imgproc.resize(matBGR, matBGR, new org.opencv.core.Size(0, 0), 2, 2); //,   INTER_LINEAR_EXACT); //, INTER_CUBIC);  // INTER_LINEAR_EXACT );
            
        }
		
//	      Mat matBGRROI = matBGR.colRange(0, matBGR.cols()).rowRange(0, matBGR.rows() / 4);
//	      Mat matBGRROI = matBGR.colRange(0, matBGR.cols()).rowRange(matBGR.rows() / 2, matBGR.rows() / 2 + matBGR.rows() / 4);
        Mat matBGRROI = matBGR.colRange(800, 1600).rowRange(1800, matBGR.rows());
		
		org.opencv.imgproc.Imgproc.cvtColor(matBGRROI, matHSV, Imgproc.COLOR_BGR2HSV);
		
		
		//Calculate histogram
		java.util.List<Mat> matList = new LinkedList<Mat>();
		matList.add(matHSV);
		Mat histogram = new Mat();
		MatOfFloat ranges=new MatOfFloat(0,256);
		MatOfInt histSize = new MatOfInt(256);
		
		//////////
		

		Imgproc.calcHist(
		                matList, 
		                new MatOfInt(2), // 0 for hue, 1 for sat, 2 for value
		                new Mat(), 
		                histogram , 
		                histSize , 
		                ranges);
		///////////

		// Create space for histogram image
		Mat histImage = Mat.zeros( 100, (int)histSize.get(0, 0)[0], CvType.CV_8UC1);
		// Normalize histogram                          
		Core.normalize(histogram, histogram, 1, histImage.rows() , Core.NORM_MINMAX, -1, new Mat() );   
		// Draw lines for histogram points
		int ValueIndex = 200;
		for( int i = 0; i < (int)histSize.get(0, 0)[0]; i++ )
		{
			if(Math.round( histogram.get(i,0)[0] ) >= 98 )
			{
				ValueIndex = i;
				break;

//		        System.out.println("I " + i + " get " + Math.round( histogram.get(i,0)[0] ));
			}
		}
		
        System.out.println("Index where value is 98 " + ValueIndex);
		
        return (ValueIndex + 10);
        

	}
