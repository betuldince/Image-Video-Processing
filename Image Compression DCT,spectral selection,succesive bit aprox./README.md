# Image Compression Using Discrete Cosine Transform (DCT)

This project implements an **image compression tool** using the Discrete Cosine Transform (DCT) and Inverse Discrete Cosine Transform (IDCT). The implementation supports multiple encoding and decoding modes, including **sequential, spectral selection, and successive bit approximation**. It is designed to demonstrate the trade-offs between compression efficiency and image quality under limited bandwidth constraints.

## Features

- **DCT-Based Compression**:
  - Transforms image blocks into the frequency domain using DCT.
  - Reduces image size by quantizing DCT coefficients.

- **IDCT-Based Decompression**:
  - Reconstructs the image from compressed DCT coefficients.
  - Supports decoding in multiple modes for different use cases.

- **Compression Modes**:
  - **Sequential Mode**: Processes image blocks sequentially in top-to-bottom, left-to-right order.
  - **Progressive Mode (Spectral Selection)**: Decodes low-frequency coefficients first for faster initial previews.
  - **Successive Bit Approximation**: Transmits and decodes the most significant bits first for faster reconstruction.

- **Quantization Control**:
  - Adjustable quantization level (`Q`) to control the trade-off between compression ratio and image quality.

- **Latency Simulation**:
  - Simulates bandwidth constraints by introducing a user-defined delay (`L`) between decoding iterations.

## How It Works

1. **Image Partitioning**:
   - The image is read from an `.rgb` file and divided into 8x8 blocks.
   - Each block undergoes DCT to convert pixel values into frequency coefficients.

2. **Compression**:
   - **Quantization**: Each DCT coefficient is divided by `2^Q` (where `Q` is user-defined) and rounded to the nearest integer.
   - Quantization reduces the number of bits needed to store DCT coefficients.

3. **Decompression**:
   - Dequantized coefficients are multiplied by `2^Q` to approximate their original values.
   - IDCT is applied to reconstruct pixel values.

4. **Modes of Decoding**:
   - **Sequential Mode**: Decodes and displays image blocks one at a time.
   - **Spectral Selection**: Decodes DC coefficients first, followed by AC coefficients using zigzag ordering.
   - **Successive Bit Approximation**: Transmits the most significant bits first, reconstructing the image progressively.

## Usage

### Input

- **Image File**: Must be in `.rgb` format with dimensions 512x512.
- **Parameters**:
  - `Q` (Quantization Level): Integer value to control the degree of compression.
  - `Mode`: 
    - `1` for Sequential Mode.
    - `2` for Spectral Selection Mode.
    - `3` for Successive Bit Approximation Mode.
  - `L` (Latency): Integer value to simulate time delay between iterations.

### Running the Program

1. **Compile the Code**:
   ```bash
   javac ImageDisplay.java
