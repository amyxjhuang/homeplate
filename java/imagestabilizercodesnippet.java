   static public void TrackImage(Mat matOrginal, int Index, Rect TrackingRectangle) {
        /// Parameters for Shi-Tomasi algorithm
        maxCorners = Math.max(maxCorners, 10);
        MatOfPoint corners = new MatOfPoint();
        double qualityLevel = .101; // 0.01; // 0.01;
        double minDistance = 10;
        int blockSize = 3, gradientSize = 3;
        boolean useHarrisDetector = false; // false finds circles/balls   //true;  // false;
        double k = 0.04;
        int SizeTarget = 20;  // size of tracking sub image * 2
        int SizeSource = 100; // size of the larger region to check
        int CroppedEdgesSize = 0;


         gTrackingCount++;

        if (LOG) System.out.println("TRACKINGMANAGER frame number " + Index + " rect " + TrackingRectangle);


        // reset the tracking after one second, maybe increase to a few seconds, TBD max time for a golf ball to go 40 feet

        try {


            Mat matGray = new Mat();

            Mat matCropped = new Mat(matOrginal, TrackingRectangle);

            if (LOG) System.out.println("TRACKINGMANAGER matcropped " + matCropped);
            if (LOG) System.out.println("TRACKINGMANAGER matoriginal " + matOrginal);

            Imgproc.cvtColor(matCropped, matGray, Imgproc.COLOR_RGB2GRAY);

            if (arrayTrackingLocations.size() == 0) {
                // TODO test downsizing the src image, see how it affects the corners.

                //               System.out.println("Movement XTrack 20");

                // TODO resize image to not include 10% around borders, add back in offset.
                /// Apply corner detection
                // only look in the zoom window area.
                Imgproc.goodFeaturesToTrack(matGray, corners, maxCorners, qualityLevel, minDistance, new Mat(),
                        blockSize, gradientSize, useHarrisDetector, k);

                /// Draw corners detected
                if(LOG) System.out.println("** Number of corners detected: " + corners.rows() + "?????????????????????????????????????");
                int[] cornersData = new int[(int) (corners.total() * corners.channels())];
                corners.get(0, 0, cornersData);
                int radius = 4;
                for (int i = 0; i < corners.rows(); i++) {
                    ImageProcessing.BallLocations ballLocations = new ImageProcessing.BallLocations();
                    int xPos = cornersData[i * 2];
                    int yPos = cornersData[i * 2 + 1];
                    int nSize = radius; //ballLocations.Size;

                    if (LOG) System.out.println("TRACKINGMANAGER xpos " + xPos + " ypos " + yPos + " sizesource " + SizeSource);

                    if(xPos - SizeTarget < 0 || yPos - SizeTarget < 0 || xPos + SizeTarget > matCropped.rows() || yPos + SizeTarget > matCropped.cols())
                    {
                        if (LOG) System.out.println("TRACKINGMANAGER too close to edge, xpos " + xPos + " ypos " + yPos);
                        continue;
                    }

                    int xPosAbsolute = xPos;
                    int yPosAbsolute = yPos;
                    xPosAbsolute += TrackingRectangle.x;
                    yPosAbsolute += TrackingRectangle.y;

                    ballLocations.Image = matCropped.colRange(xPos - SizeTarget, xPos + SizeTarget).rowRange(yPos - SizeTarget, yPos + SizeTarget).clone();

                    ballLocations.X = xPosAbsolute; // + CroppedEdgesSize;
                    ballLocations.Y = yPosAbsolute; // + CroppedEdgesSize;
                    ballLocations.Size = radius;
                    ballLocations.Index = Index;


                    //                   System.out.println("Movement XTrack 21");
                    // get sub image from frame
                    arrayTrackingLocations.add(ballLocations);

                    if (LOG) System.out.println("TRACKINGMANAGER adding location to track X " + ballLocations.X + " Y " + ballLocations.Y);
                    if (LOG) System.out.println("TRACKINGMANAGER absolute X " + (xPos + TrackingRectangle.x) + " Y " + (yPos + TrackingRectangle.y));
                    gbImageStabilizationFirstFrame = true;
                    if (LOG) System.out.println("TRACKINGMANAGER XPos " + xPos + " tr x " + TrackingRectangle.x + " YPos " + yPos + " tr y " + TrackingRectangle.y);


                    //                  System.out.println("Movement XTrack 22");


                    break;  // only use one for now, or use the corner closest to the center of the sub image.

                }
            } else {
                // Go thru arraytrackinglocations, for each location x,y and timestamp, find object that is nearby.
                // find DeltaX and DeltaY, save info in adjustments list for later useage when drawing OR save with each ball location
                // maybe update X and Y based on current value (unlikely this will work, but worth considering.
                for (int x = 0; x < arrayTrackingLocations.size(); x++) {
                    int xPos;
                    int yPos;

                    ImageProcessing.BallLocations TrackLocation = arrayTrackingLocations.get(x);

                    xPos = TrackLocation.X; //arrayTrackingLocations.get(x).X;
                    yPos = TrackLocation.Y; //arrayTrackingLocations.get(x).Y;

                    Mat mSubImage = arrayTrackingLocations.get(x).Image.clone();

                    if (LOG) System.out.println("TRACKINGMANAGER Getting tracking location xpos " + xPos + " ypos " + yPos + " sizesource " + SizeSource + " subimage " + mSubImage);


                   //////////////////////////////////

                    // for testing set mSubImage to all white or something, see what matchtemplate returns.
                    // need to know if resetting anchor is needed.
                    Mat outputImage = new Mat();
                    int machMethod = Imgproc.TM_CCORR_NORMED;  //.TM_CCOEFF;

                    //Template matching method
                    Imgproc.matchTemplate(Image, mSubImage, outputImage, machMethod); //, mSubImage);  // TODO make src image smaller

//                    Core.normalize(outputImage, outputImage, 0, 1, Core.NORM_MINMAX, -1, new Mat()); //  ????

                    Core.MinMaxLocResult mmr = Core.minMaxLoc(outputImage);
                    org.opencv.core.Point matchLoc = mmr.maxLoc;

                    if(mmr.maxVal < .96f || mmr.maxLoc.x == 0 || mmr.maxLoc.y == 0)
                    {
                        // frame didn't match, continue till one does
                        System.out.println("TRACKINGMANAGER Movement XTrack First and second frame didn't match +++++++++++++++++++ maxval " + mmr.maxVal + " xloc " + mmr.maxLoc.x + " yloc " + mmr.maxLoc.y);
                        gbImageStabilizationFirstFrame = true;
                        arrayTrackingLocations.clear();
                        gbCameraMoving = true;
                        continue;
                    }


                    if (LOG) System.out.println("TRACKINGMANAGER maxVal " + mmr.maxVal + " match x is " + matchLoc.x + " y is " + matchLoc.y);

                    int MovementX = (int) SizeSource - (int) matchLoc.x; // - SizeTarget;
                    int MovementY = SizeSource - (int) matchLoc.y; // - SizeTarget;

                    MovementX = xPos - (xPos - SizeSource + (int) matchLoc.x) - SizeTarget;
                    MovementY = yPos - (yPos - SizeSource + (int) matchLoc.y) - SizeTarget;

                    {
                        Point pzoomwindow = ZoomWindowGetCenter();

                        ZoomWindow.ZoomWindowSetCenter(pzoomwindow);

                    }

                    if (LOG) System.out.println("TRACKINGMANAGER movementX " + MovementX + " movementy " + MovementY);
                    if (LOG) System.out.println("TRACKINGMANAGER absolute movementX " + (matchLoc.x + TrackingRectangle.x) + " movementy " + (matchLoc.y + TrackingRectangle.y));
                    gbCameraMoving = false;
                    gbImageStabilizationFirstFrame = false;
 
                    if(!(abs(MovementX) == 0 && abs(MovementY) == 0))
                    {

                    }
                    else
                    {
                        // if first match isn't 0,0 then camera still moving, ignore the frame

                   }

                    outputImage.release();
                    Image.release();
                    mSubImage.release();

 
                    break;  // only one object to track, for now
                }
            }
            matGray.release();
            matCropped.release();

            //

        }
        catch (Exception ex)
        {
            System.out.println("Sport Trackimage Exception 22 " + ex.getMessage().toString());
            //
        }
    }
