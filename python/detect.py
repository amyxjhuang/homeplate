import cv2
import numpy as np
from ultralytics import YOLO  # pip install ultralytics

def main():
    # Load the YOLOv11-Seg model.
    # Replace "yolov11_seg.pt" with the correct model identifier or local path.
    model = YOLO("yolo11n-seg.pt")  # Assuming this model is available via Hugging Face/Ultralytics hub

    # Path to your input image
    image_path = "/Users/varundeliwala/NYU/Projects/visaball/images/test0.jpg"
    image = cv2.imread(image_path)
    if image is None:
        print("Error: Could not load image.")
        return
    img_h, img_w, _ = image.shape

    # Run prediction (the model will automatically download if not already present)
    # Adjust the confidence threshold if needed.
    results = model.predict(source=image_path, conf=0.5, verbose=False)

    # We expect segmentation-enabled outputs.
    # For the first image in the batch:
    result = results[0]
    
    # Get bounding boxes and (if available) segmentation masks.
    breakpoint()
    # The structure of result.boxes and result.masks follows the ultralytics convention.
    boxes = result.boxes.xyxy.cpu().numpy() if hasattr(result, "boxes") else None
    if hasattr(result, "masks") and result.masks is not None:
        masks = result.masks.data.cpu().numpy()
    else:
        masks = None


    # Select a candidate detection for home plate:
    # For example, choose the detection with the largest area located in the lower half of the image.
    candidate_box = None
    candidate_index = -1
    max_area = 0
    if boxes is not None:
        for i, box in enumerate(boxes):
            x1, y1, x2, y2 = box.astype(int)
            area = (x2 - x1) * (y2 - y1)
            # Heuristic: Only consider detections whose top edge is in the lower half.
            if y1 > img_h * 0.5 and area > max_area:
                candidate_box = (x1, y1, x2, y2)
                candidate_index = i
                max_area = area

    if candidate_box is not None:
        (x1, y1, x2, y2) = candidate_box
        # Draw the bounding box (green) for the candidate home plate.
        cv2.rectangle(image, (x1, y1), (x2, y2), (0, 255, 0), 2)
        cv2.putText(image, "Home Plate Candidate", (x1, y1 - 10),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)
        
        # If segmentation masks are provided, overlay the mask.
        if masks is not None and candidate_index < len(masks):
            mask = masks[candidate_index]
            # Assume mask values are in [0,1] (or logits). Threshold to create a binary mask.
            binary_mask = (mask > 0.5).astype(np.uint8) * 255
            # Resize mask to original image dimensions (if the model input differs).
            binary_mask = cv2.resize(binary_mask, (img_w, img_h), interpolation=cv2.INTER_NEAREST)
            # Create a colored mask (here in blue) and overlay it.
            color_mask = np.zeros_like(image)
            color_mask[:, :, 0] = binary_mask  # Blue channel
            alpha = 0.5
            image = cv2.addWeighted(image, 1.0, color_mask, alpha, 0)
        
        # Overlay a virtual strike zone above the detected plate.
        # Here we define the strike zone as a rectangle with the same width as the detection
        # and twice the detection’s height, positioned directly above the candidate box.
        box_width = x2 - x1
        box_height = y2 - y1
        zone_height = box_height * 2
        zone_top = y1 - zone_height
        # Clamp the strike zone to the image boundary.
        if zone_top < 0:
            zone_top = 0
        cv2.rectangle(image, (x1, zone_top), (x2, y1), (255, 0, 0), 2)
    else:
        print("No suitable candidate for home plate found.")

    # Display the result.
    cv2.imshow("Detection and Virtual Strike Zone", image)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

    # Optionally, save the output image.
    output_path = "yolo11seg_plate_result.jpg"
    cv2.imwrite(output_path, image)
    print(f"Result saved to {output_path}")

if __name__ == "__main__":
    main()
