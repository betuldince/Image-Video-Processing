# Image-Video-Processing Projects

This repository contains various projects related to image and video processing. Each folder represents a different project, focusing on specific techniques and methodologies.

## Project Descriptions

### 1. **Bitwise Image Transformation**
Implements basic image processing operations such as:
- **Scaling**: Resizing images to different dimensions.
- **Rotation**: Rotating images at specified angles.
- **Other Transformations**: Includes grayscale conversions and basic manipulations.

**Technologies**:
- Developed in Java.
- Processes raw RGB image files.

**Purpose**: Demonstrates foundational image transformation techniques using pixel-wise operations.

---

### 2. **Image Compression with DCT and IDCT**
This project uses Discrete Cosine Transform (DCT) and Inverse DCT (IDCT) to compress and decompress images with different modes of operation.

- **Compression Modes**:
  - **Sequential Mode**: Decodes and displays image blocks one at a time.
  - **Spectral Selection**:
    - Decodes DC coefficients first.
    - Gradually reconstructs the image by adding AC coefficients in zigzag order.
  - **Successive Bit Approximation**:
    - Transmits the most significant bits first.
    - Progressively reconstructs the image in fewer iterations.

- **DCT-Based Compression**:
  - Transforms image blocks into the frequency domain.
  - Reduces file size by quantizing DCT coefficients.
- **IDCT-Based Decompression**:
  - Reconstructs the original image from compressed DCT coefficients.
  - Multiple decoding modes are available for specific use cases.

**Purpose**: Explores trade-offs between compression ratios and image quality under bandwidth constraints.

---

### 3. **Image Compression with Quantization**
This project focuses on compressing images using quantization techniques.

- **Compression Methods**:
  - **Uniform Quantization**:
    - Divides the color space into fixed-size buckets.
    - Calculates bucket averages and compresses pixel values.
  - **Non-Uniform Quantization**:
    - Dynamically creates buckets based on pixel value distributions.
    - Provides more efficient compression for varying data ranges.

**Technologies**:
- Developed in Java.
- Works with raw RGB images.

**Purpose**: Implements practical quantization techniques to reduce image sizes while maintaining acceptable quality.

---

### 4. **Video Similarity Detection**
This project identifies the most similar video to a given query video by comparing frame-level hashes.

**Steps**:
- Extracts perceptual hashes from video frames.
- Computes frame-wise similarity between the query video and a dataset of preprocessed video hashes.
- Uses the Hamming distance for similarity calculations.

**Key Features**:
- Efficient hashing of frames using grayscale conversion and downscaling.
- Supports comparison against large datasets using precomputed JSON hash files.

**Technologies**:
- Implemented in Python.
- Utilizes OpenCV for video processing.

**Purpose**: Facilitates video content analysis and duplicate detection.

 
