# Video Similarity Detection Project

This project implements a Python-based solution for detecting the most similar video to a given query video using frame hashing. The project processes video frames to generate perceptual hashes, stores the hashes in a JSON file, and compares them with the query video's hashes to determine the closest match.

# Features

- Frame Hashing: Converts video frames to perceptual hashes using grayscale conversion and resizing.
- Efficient Similarity Search: Compares preprocessed frame hashes to identify the most similar video.
- Customizable Frame Sampling: Allows adjustable frame sampling intervals for faster processing.
