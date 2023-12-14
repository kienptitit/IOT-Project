import torch
import torch.nn as nn
from transformers import SwinForImageClassification, AutoFeatureExtractor


# from PIL import Image
# import numpy as np
# import matplotlib.pyplot as plt
class Decoder(nn.Module):
    def __init__(self):
        super(Decoder, self).__init__()

        self.conv1 = nn.ConvTranspose2d(16, 32, 2, 2, 1)
        self.conv2 = nn.ConvTranspose2d(32, 64, 4, 2, 1)
        self.conv3 = nn.ConvTranspose2d(64, 128, 4, 2, 1)
        self.conv4 = nn.ConvTranspose2d(128, 64, 4, 2, 1)
        self.conv5 = nn.ConvTranspose2d(64, 3, 4, 2, 1)
        self.relu = nn.ReLU()
        self.tanh = nn.Tanh()

    def forward(self, x):
        x = self.relu(self.conv1(x))
        x = self.relu(self.conv2(x))
        x = self.relu(self.conv3(x))
        x = self.relu(self.conv4(x))
        x = self.relu(self.conv5(x))
        return self.tanh(x)


class Identity(nn.Module):
    def __init__(self):
        super().__init__()

    def forward(self, x):
        return x


class MyModel(nn.Module):
    def __init__(self, in_feature, pretrain_swin):
        super().__init__()
        self.swin_model = SwinForImageClassification.from_pretrained(pretrain_swin)
        self.swin_model.classifier = Identity()
        self.swin_model.eval()
        self.lstm_model = nn.LSTM(in_feature, in_feature, batch_first=True)
        self.deconv2D = Decoder()
        self.map_model = nn.Sequential(
            nn.Linear(1024, 256),
            nn.ReLU(),
            nn.Linear(256, 1024)
        )

    def forward(self, x):
        """
        x : [B,N,C,224,224]
        """
        b, n, c, h, w = x.shape
        x = x.reshape(-1, c, h, w)
        with torch.no_grad():
            x_ = self.swin_model(x).logits  # B*N,1024

        x_ = x_.reshape(b, n, -1)
        x_lstm, _ = self.lstm_model(x_)  # B,N,1024
        x_lstm = x_lstm[:, -1]  # B,1024
        x_encoded = self.map_model(x_lstm).reshape(b, 16, 8, 8)
        output = self.deconv2D(x_encoded)  # B,3,224,224
        return output


class Simase_network(nn.Module):
    def __init__(self, in_features, pretrained_swin, pretrained_AE):
        super().__init__()
        self.in_features = in_features
        self.swin_model = SwinForImageClassification.from_pretrained(pretrained_swin)
        self.swin_model.classifier = Identity()
        self.swin_model.eval()
        self.AE_model = MyModel(1024, pretrained_swin)
        self.AE_model.load_state_dict(torch.load(pretrained_AE, map_location="cpu"))
        self.AE_model.eval()
        self.simase_netword = nn.Sequential(
            nn.Linear(self.in_features * 2, 64),
            nn.ReLU(),
            nn.Linear(64, 1),
            nn.Sigmoid()
        )

    def forward(self, x, x_positive):
        b, n, c, h, w = x.shape

        with torch.no_grad():
            output = self.AE_model(x)  # B,C,H,W

        x = x.reshape(-1, c, h, w)  # B*N,C,H,W

        x = torch.concat([x, x_positive, output], dim=0)
        with torch.no_grad():
            x_ = self.swin_model(x).logits  # B*N + B + B,1024

        anchor = x_[-b:]  # B,1024
        x_positive = x_[-2 * b:-b]  # B,1024
        x_negative = x_[:-2 * b].reshape(b, -1, self.in_features)  # B,N,1024
        gap_positive = torch.concat([anchor, x_positive], dim=1)
        gap_negative = torch.concat([anchor.unsqueeze(1).repeat(1, n, 1), x_negative], dim=-1).reshape(-1,
                                                                                                       self.in_features * 2)

        labels = torch.hstack([torch.tensor([0] * len(gap_positive)), torch.tensor([1] * len(gap_negative))])
        gap = torch.concat([gap_positive, gap_negative], dim=0)
        output_simase = self.simase_netword(gap)
        return output_simase, labels

    def inference(self, X, y):
        """
        :param X: [1,3,3,224,224]
        :param y: [1,3,224,224]
        :return:
        """
        with torch.no_grad():
            output = self.AE_model(X)  # 1,3,224,224
            input_ = torch.concat([output, y], dim=0)
            output_swin = self.swin_model(input_).logits  # 2,1024
            input_sim = torch.concat([output_swin[0:1], output_swin[1:]], dim=1)
            output_sim = self.simase_netword(input_sim).squeeze()
        return torch.round(output_sim)

# if __name__ == '__main__':
#     plants_model = MyModel(1024, r"E:\2023\PTIT\IOT\Checkpoint-Swin")
#     plants_model.load_state_dict(
#         torch.load(r"E:\2023\PTIT\IOT\Process_data\new_model_plants_grow.pt", map_location=torch.device("cpu")))
#     image_paths = [r"E:\2023\PTIT\IOT\New_Plants_Grow_No_Duplicated\5\1.jpg",
#                    r"E:\2023\PTIT\IOT\New_Plants_Grow_No_Duplicated\5\7.jpg",
#                    r"E:\2023\PTIT\IOT\New_Plants_Grow_No_Duplicated\5\9.jpg"]
#     images = [Image.open(image_path) for image_path in image_paths]
#     model_name = 'microsoft/swin-base-patch4-window7-224'
#     feature_extractor = AutoFeatureExtractor.from_pretrained(model_name)
#     image_extracted = torch.from_numpy(
#         np.stack(
#             feature_extractor([image.convert('RGB') for image in images], return_tensor="pt").pixel_values)).unsqueeze(
#         0)
#
#     mean = [0.485, 0.456, 0.406]
#     std = [0.229, 0.224, 0.225]
#     output = plants_model(image_extracted).detach().squeeze()
#
#     image_extracted = torch.concat([image_extracted.squeeze(), output.unsqueeze(0)], dim=0)
#     for i in range(3):
#         image_extracted[:, i, :, :] *= std[i]
#         image_extracted[:, i, :, :] += mean[i]
#     titles = ["Tuần 1", "Tuần 2", "Tuần 3", "Predicted Image"]
#     plt.figure(figsize=(15, 6))
#     for i in range(4):
#         plt.subplot(1, 4, i + 1)
#         plt.imshow(image_extracted[i].permute(1, 2, 0))
#         plt.title(titles[i])
#         plt.axis("off")
#     plt.show()
