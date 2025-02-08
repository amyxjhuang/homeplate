# homeplate
Detecting home plate with OpenCV

```
g++ -o detect_home_plate detect_home_plate.cpp `pkg-config --cflags --libs opencv4`


./detect_home_plate
```

Constraints:
- added bounds (assuming near the center of the image)
- limit the height and width of the polygon
- polygon has approx between 3 and 6 sides 

![Screenshot 2025-02-07 at 8 40 46 PM](https://github.com/user-attachments/assets/e2aee40d-f704-489c-b25e-33b89b8cc913)
