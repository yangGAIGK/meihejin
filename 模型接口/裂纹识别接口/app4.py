import os
import io
import time
import numpy as np
import cv2
import torch
import base64
from flask import Blueprint, request, jsonify
from collections import OrderedDict
from flask_cors import CORS

# --- Try importing DeepCrack model ---
try:
    from model.deepcrack import DeepCrack
except ImportError:
    print("错误：无法导入 DeepCrack 模型。")
    print("请确保 'model/deepcrack.py' 文件存在于 'model' 子目录下，")
    print("并且此脚本与 'model' 目录在同一父目录下。")
    exit()

# --- Configuration ---
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_FILE_PATH = os.path.join(SCRIPT_DIR, 'checkpoints', '__epoch(12)_0000279_2024-05-06-10-48-32.pth')  # Ensure this path is correct
BINARY_THRESHOLD = 0.5
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'bmp', 'tif', 'tiff'}
CONTOUR_MERGE_MAX_DISTANCE = 50  # Max distance for merging contours

# --- Global Variables ---
bp4 = Blueprint('app4', __name__)
device = None
model = None
CORS(bp4)  # Enable CORS

# --- Helper Functions ---

def allowed_file(filename):
    """Checks if the file extension is allowed"""
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

def threshold_matrix(matrix, threshold_value):
    """Binarizes the matrix"""
    if isinstance(matrix, torch.Tensor):
        matrix = matrix.cpu().numpy()
    binary_matrix = np.where(matrix > threshold_value, 255, 0).astype(np.uint8)
    return binary_matrix

def preprocess_image_bytes(image_bytes, device):
    """Loads image from bytes and preprocesses"""
    try:
        nparr = np.frombuffer(image_bytes, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)  # Load as color initially
        if img is None:
            print("Warning: Could not decode image from provided bytes.")
            return None
        # --- IMPORTANT: Adjust preprocessing if your model expects different input ---
        # Resize image to 512x512 to match model input requirements
        img = cv2.resize(img, (512, 512))
        # Assuming model expects BGR, float32, normalized [0, 1]
        img = img.astype(np.float32) / 255.0
        img_tensor = torch.from_numpy(img).permute(2, 0, 1)  # HWC to CHW
        img_tensor = img_tensor.unsqueeze(0)  # Add batch dimension
        img_tensor = img_tensor.to(device)
        return img_tensor
    except Exception as e:
        print(f"Error during image preprocessing: {e}")
        return None

def load_model(model_path, device):
    """Loads the DeepCrack model"""
    global model
    print(f"Attempting to load model from: {model_path}")
    try:
        local_model = DeepCrack()
        use_data_parallel = torch.cuda.device_count() > 1 and device.type == 'cuda'
        if use_data_parallel:
            print(f"Using DataParallel for {torch.cuda.device_count()} GPUs.")
            local_model = torch.nn.DataParallel(local_model)
        local_model.to(device)

        if not os.path.isfile(model_path):
            print(f"Error: Model file not found at '{model_path}'")
            return False

        # Try loading with weights_only first for security
        try:
            state_dict = torch.load(model_path, map_location=device, weights_only=True)
            print("Model loaded using weights_only=True.")
        except Exception as e_safe:
            print(f"Warning: weights_only=True loading failed ({e_safe}). Falling back to default torch.load...")
            state_dict = torch.load(model_path, map_location=device)
            print("Warning: Model loaded using default torch.load. Ensure the model source is trusted.")

        # Handle potential 'module.' prefix mismatch
        is_model_parallel = isinstance(local_model, torch.nn.DataParallel)
        state_dict_keys = list(state_dict.keys())
        is_dict_parallel = state_dict_keys and state_dict_keys[0].startswith('module.')

        if is_model_parallel and not is_dict_parallel:
            print("Adjusting non-parallel weights for DataParallel model...")
            local_model.load_state_dict(state_dict, strict=False)  # Less strict loading might work
        elif not is_model_parallel and is_dict_parallel:
            print("Removing 'module.' prefix from weights for single-device model...")
            new_state_dict = OrderedDict()
            for k, v in state_dict.items():
                name = k[7:]  # remove module.
                new_state_dict[name] = v
            local_model.load_state_dict(new_state_dict)
        else:
            print("Model and weights state seem consistent. Loading directly...")
            local_model.load_state_dict(state_dict)

        local_model.eval()
        model = local_model  # Assign to global variable
        print("Model loaded and set to evaluation mode successfully.")
        return True
    except FileNotFoundError:
        print(f"Error: Pre-trained model file '{model_path}' not found.")
        return False
    except Exception as e:
        print(f"Error during model construction or loading: {e}")
        return False

# --- Contour Analysis Functions ---

def distance_between_contours(cnt1, cnt2):
    """Calculates the minimum distance between two contours."""
    # Ensure contours are numpy arrays of points
    points1 = np.squeeze(cnt1)
    points2 = np.squeeze(cnt2)

    # Handle cases where squeeze might result in a single point (not an array)
    if points1.ndim == 1: points1 = np.array([points1])
    if points2.ndim == 1: points2 = np.array([points2])
    if points1.size == 0 or points2.size == 0: return float('inf')  # Handle empty contours

    min_dist = float('inf')
    for p1 in points1:
        for p2 in points2:
            try:
                dist = np.linalg.norm(p1 - p2)
                if dist < min_dist:
                    min_dist = dist
            except Exception as e:
                print(f"Error calculating distance between points {p1} and {p2}: {e}")
                continue  # Skip problematic points if necessary
    return min_dist

def merge_close_contours(contours, max_distance):
    """Merges contours that are closer than max_distance."""
    if not contours:
        return []

    merged_contours = []
    # Convert tuple to list to allow modification
    contours = list(contours)

    while contours:
        current_contour = contours.pop(0)
        if current_contour is None or len(current_contour) == 0:  # Skip empty contours
            continue

        i = 0
        while i < len(contours):
            if contours[i] is None or len(contours[i]) == 0:  # Skip empty contours in the inner loop
                i += 1
                continue

            dist = distance_between_contours(current_contour, contours[i])
            if dist <= max_distance:
                # Merge contours: Use vstack for robust concatenation
                try:
                    merged = np.vstack((current_contour, contours.pop(i)))
                    current_contour = merged  # Keep merged points for now
                except Exception as e:
                    print(f"Error merging contours: {e}")
                    i += 1  # Move to next contour if merge fails
            else:
                i += 1
        merged_contours.append(current_contour)
    return merged_contours

def analyze_binary_image(binary_image_np, max_distance=50):
    """
    Analyzes contours in a binary NumPy image.
    Input: binary_image_np (NumPy array, 8-bit single channel, 0 or 255)
           max_distance (for merging contours)
    Output: Dictionary with crack analysis results.
    """
    if binary_image_np is None or binary_image_np.size == 0:
        return {"error": "Input binary image is empty or invalid"}
    if binary_image_np.ndim != 2:
        # If it's not 2D, try converting common cases (e.g., grayscale with extra dim)
        if binary_image_np.ndim == 3 and binary_image_np.shape[2] == 1:
            binary_image_np = np.squeeze(binary_image_np, axis=2)
        else:
            return {"error": f"Input image must be 2D grayscale, but got shape {binary_image_np.shape}"}
    if binary_image_np.dtype != np.uint8:
        # Ensure it's uint8 as expected by findContours
        binary_image_np = binary_image_np.astype(np.uint8)

    print(f"Analyzing binary image with shape: {binary_image_np.shape}, dtype: {binary_image_np.dtype}")
    # Find contours
    try:
        # For OpenCV 4.x+
        contours, hierarchy = cv2.findContours(binary_image_np, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        print(f"Found {len(contours)} initial contours.")
    except ValueError:
        # For older OpenCV versions
        _, contours, hierarchy = cv2.findContours(binary_image_np, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        print(f"Found {len(contours)} initial contours (older OpenCV).")
    except Exception as e:
        print(f"Error during findContours: {e}")
        return {"error": f"Failed during contour detection: {e}"}

    if not contours:
        print("No contours found in the binary image.")
        return {
            "crack_count": 0,
            "crack_info": []
        }

    # Merge close contours
    try:
        merged_contours = merge_close_contours(contours, max_distance)
        print(f"Merged into {len(merged_contours)} contours.")
    except Exception as e:
        print(f"Error during contour merging: {e}")
        return {"error": f"Failed during contour merging: {e}"}

    # Calculate properties for merged contours
    result = {
        "crack_count": len(merged_contours),
        "crack_info": []
    }
    for i, cnt in enumerate(merged_contours):
        if cnt is None or len(cnt) < 3:  # Need at least 3 points for most calculations
            print(f"Skipping invalid merged contour {i+1}")
            continue
        try:
            x, y, w, h = cv2.boundingRect(cnt)
            area = cv2.contourArea(cnt)
            perimeter = cv2.arcLength(cnt, True)  # True indicates closed contour
            # Calculate approximate length
            length = max(w, h)

            # Convert numpy types to standard Python types for JSON serialization
            crack_info = {
                "id": i + 1,
                "position": (int(x), int(y)),  # (x, y) top-left corner
                "width": int(w),
                "height": int(h),
                "area": float(area),
                "perimeter": float(perimeter),
                "length": int(length) #Add crack length
            }
            result["crack_info"].append(crack_info)
        except Exception as e:
            print(f"Error calculating properties for contour {i+1}: {e}")
            continue

    return result

# --- Flask API Endpoint (Combined) ---

@bp4.route('/imagepredict', methods=['POST'])
def predict_and_analyze_api():
    """
    API endpoint: receives an image, predicts cracks using DeepCrack,
    analyzes the binary prediction for contours, and returns JSON
    containing both analysis results and the Base64 encoded binary image.
    """
    start_time = time.time()

    if model is None:
        return jsonify({"error": "Model service internal error: Model not loaded."}), 500

    if 'image' not in request.files:
        return jsonify({"error": "Request must contain 'image' file part."}), 400

    file = request.files['image']

    if file.filename == '':
        return jsonify({"error": "No file selected."}), 400
    if not allowed_file(file.filename):
        return jsonify({"error": f"File type not allowed. Allowed: {', '.join(ALLOWED_EXTENSIONS)}"}), 400

    try:
        img_bytes = file.read()

        # === Step 1: DeepCrack Prediction ===
        input_tensor = preprocess_image_bytes(img_bytes, device)
        if input_tensor is None:
            return jsonify({"error": "Could not process uploaded image file."}), 400

        with torch.no_grad():
            outputs = model(input_tensor)

        # Process prediction output to get probability map
        if isinstance(outputs, (list, tuple)):
            pred_tensor = outputs[0]  # Assuming first output is main prediction
        else:
            pred_tensor = outputs
        pred_prob = torch.sigmoid(pred_tensor)

        # Adjust dimensions and move to CPU
        pred_prob_single = pred_tensor[0].cpu()  # Get first item in batch, move to CPU
        if pred_prob_single.dim() == 3:  # Handle CHW format (C should be 1)
            pred_prob_final = pred_prob_single.squeeze(0)  # Remove channel dim -> HW
        elif pred_prob_single.dim() == 2:  # Handle HW format directly
            pred_prob_final = pred_prob_single
        else:
            raise ValueError(f"Unexpected prediction probability map dimensions: {pred_prob_single.shape}")

        # Binarize the probability map
        binary_image_np = threshold_matrix(pred_prob_final, BINARY_THRESHOLD)
        print(f"Generated binary prediction map with shape: {binary_image_np.shape}")

        # === Step 2: Contour Analysis ===
        # Analyze the generated binary image
        crack_analysis_result = analyze_binary_image(binary_image_np, CONTOUR_MERGE_MAX_DISTANCE)
        if "error" in crack_analysis_result:
            print(f"Contour analysis failed: {crack_analysis_result['error']}")
            return jsonify({"error": f"Contour analysis failed: {crack_analysis_result['error']}"}), 500

        # === Step 3: Encode Binary Image for Response ===
        # Encode the binary NumPy array to PNG format in memory
        is_success, buffer = cv2.imencode(".png", binary_image_np)
        if not is_success:
            raise ValueError("Failed to encode binary image to PNG format")

        # Encode the PNG bytes to Base64 string
        png_as_base64 = base64.b64encode(buffer).decode('utf-8')
        # Add data URI scheme prefix for direct use in <img> src
        binary_image_data_uri = f"data:image/png;base64,{png_as_base64}"

        # === Step 4: Construct Final JSON Response ===
        final_response = {
            "analysis": crack_analysis_result,  # Contains 'crack_count' and 'crack_info' list
            "binary_image_b64": binary_image_data_uri  # Base64 encoded PNG image
        }

        end_time = time.time()
        processing_time = end_time - start_time
        print(f"Request processed successfully: File '{file.filename}', Time: {processing_time:.3f}s")

        # Return the combined JSON response
        return jsonify(final_response)

    except RuntimeError as e:
        if "CUDA out of memory" in str(e):
            print(f"Runtime Error (CUDA OOM): {e}")
            return jsonify({"error": "Server processing resources exhausted (CUDA OOM). Please try again later."}), 503
        else:
            print(f"Runtime Error: {e}")
            return jsonify({"error": f"Runtime error during image processing: {e}"}), 500
    except Exception as e:
        print(f"Unknown error processing request for '{file.filename}': {e}")
        # Log the full traceback in production environments
        import traceback
        traceback.print_exc()
        return jsonify({"error": "An unexpected internal server error occurred."}), 500

# --- Main Execution ---
def init_bp4():
    """
    初始化 bp4，加载模型并返回 Blueprint 实例。
    """
    global device, model

    # Set device (CUDA or CPU)
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"API will use device: {device}")

    # Pre-load the model
    if not load_model(MODEL_FILE_PATH, device):
        print("\n!!! CRITICAL WARNING: Model loading failed. API endpoint /imagepredict will not function correctly! !!!\n")
        # Decide if the app should exit or run with a non-functional endpoint
        # exit(1)

    return bp4
