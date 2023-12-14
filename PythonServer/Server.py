import numpy as np
import torch
from flask import Flask, request, jsonify
import pickle
import io
from PIL import Image
import base64
from Plants_Grow import Simase_network
from transformers import SwinForImageClassification
from transformers import AutoFeatureExtractor
from flask_cors import CORS
from io import BytesIO
from ultralytics import YOLO

app = Flask(__name__)

CORS(app)


class Config:
    def __init__(self):
        self.simase_model = "Weights/model_simaese_59.pt"
        self.plants_grow_model = 'Weights/new_model_plants_grow.pt'
        self.Swin_Model = "Weights/Checkpoint-Swin"
        self.pets_detection = "Weights/best_kaggle.pt"


def swin_pred(img):
    model_name = 'microsoft/swin-base-patch4-window7-224'
    feature_extractor = AutoFeatureExtractor.from_pretrained(model_name)
    model_ = SwinForImageClassification.from_pretrained(
        cfg.Swin_Model)
    img = feature_extractor(img.convert('RGB'), return_tensors="pt")
    model_.eval()
    with torch.no_grad():
        logits = model_(**img).logits.squeeze().softmax(dim=-1)
        pred = logits.argmax(-1).item()

    if logits.max(dim=-1).values.item() > 0.3:
        return int(model_.config.id2label[pred]), logits
    else:
        return -1, None


@app.route("/plants-classification", methods=["POST"])
def predict():
    with open("Weights/class_name.pkl", "rb") as f:
        class_ = pickle.load(f)
    id_to_class = {idx: c for c, idx in class_.items()}

    image_data = request.get_data()

    image_data_decoded = base64.b64decode(image_data)
    img = Image.open(io.BytesIO(image_data_decoded))
    out, _ = swin_pred(img)

    data = {
        'idx': out,
        'prediction': id_to_class[out] if out != -1 else "không tìm thấy trong CSDL"
    }
    return jsonify(data)


@app.route("/pets-detection", methods=["POST"])
def pets_detection():
    model = YOLO(cfg.pets_detection)
    image_data = request.get_data()

    image_data_decoded = base64.b64decode(image_data)
    img = Image.open(io.BytesIO(image_data_decoded))
    preds = model(img, conf=0.5)
    bbox = []
    for pred in preds:
        boxes = pred.boxes.cpu().numpy()
        xywhs = boxes.xywh
        for xywh in xywhs:
            xywh = [int(x) for x in xywh]
            bbox.append(xywh)
    data = {
        'bbox': bbox
    }
    return jsonify(data)


@app.route('/plants-grow', methods=['POST'])
def process_images():
    model_name = 'microsoft/swin-base-patch4-window7-224'
    feature_extractor = AutoFeatureExtractor.from_pretrained(model_name)
    simamese = Simase_network(1024, cfg.Swin_Model, cfg.plants_grow_model)
    simamese.load_state_dict(
        torch.load(cfg.simase_model, map_location=torch.device("cpu")))
    simamese.eval()
    if 'image1' in request.files and 'image2' in request.files and 'image3' in request.files and 'image4' in request.files:
        images = [request.files[f'image{i}'] for i in range(1, 5)]
        pil_images = [Image.open(BytesIO(image.read())) for image in images]
        img_extracted = feature_extractor([img.convert('RGB') for img in pil_images],
                                          return_tensor='pt').pixel_values
        img_extracted = torch.from_numpy(np.stack(img_extracted))
        X = img_extracted[:3, :, :, :].unsqueeze(0)
        y = img_extracted[-1, :, :, :].unsqueeze(0)
        output = simamese.inference(X, y).item()
        if output == 0:
            message = "Cây của bạn lớn đúng định kỳ"
        else:
            message = "Cây của bạn đang bị chậm phát triển"
        return jsonify({'message': message})
    else:
        return jsonify({'error': 'Missing one or more images'})


if __name__ == "__main__":
    cfg = Config()
    app.run(host="0.0.0.0", port=5000)
