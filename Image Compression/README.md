# Image Compression with Java

This project implements an image compression tool in Java, providing two compression methods: **Uniform Quantization** and **Non-Uniform Quantization**. It allows users to compress images by reducing the number of colors, effectively lowering file size while maintaining visual quality.

## Features

- **Uniform Quantization**:
  - Divides the color space into equally sized buckets.
  - Replaces each color with the average color of the corresponding bucket.

- **Non-Uniform Quantization**:
  - Dynamically partitions the color space based on color frequency distribution.
  - Uses frequency-based bucket creation for better perceptual quality.

- **Error Metrics**:
  - Calculates the sum of quantization errors for each pixel to evaluate the quality of compression.

- **Visualization**:
  - Displays the compressed image using a graphical user interface (GUI).

## File Structure

- **ImageDisplay.java**:
  - Main source code implementing the compression logic and GUI.

## How It Works

The program reads an RGB image file, processes it using the selected compression method, and displays the compressed result.

### Compression Methods

1. **Uniform Quantization**:
   - Each color component (R, G, B) is divided into a fixed number of intervals (buckets).
   - Pixels are replaced with the average color of their respective bucket.

2. **Non-Uniform Quantization**:
   - Buckets are created dynamically based on the frequency distribution of color values.
   - Pixels are replaced with the average color of their frequency-based bucket.

## Usage

### Input

The program requires the following inputs:
1. Path to the RGB file.
2. Mode of compression:
   - `1` for Uniform Quantization.
   - `2` for Non-Uniform Quantization.
3. Number of buckets (affects the level of compression).

### Running the Program

1. **Compile the Code**:
   ```bash
   javac ImageDisplay.java
