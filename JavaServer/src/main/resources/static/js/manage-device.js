const ipServer = "10.208.208.210";
const prefixPublish = "ptit/iot24/device/publish/";
const prefixSubscribe = "ptit/iot24/device/subscribe/";
const clientId = "mqttjs_" + Math.random().toString(16).substr(2, 8);
const host = "ws://broker.hivemq.com:8000/mqtt";
const options = {
  keepalive: 60,
  clientId: clientId,
  protocolId: "MQTT",
  protocolVersion: 4,
  clean: true,
  reconnectPeriod: 1000,
  connectTimeout: 30 * 1000,
  // will: {
  //   topic: "iot_24_mqtt_sub",
  //   payload: "Connection Closed abnormally..!",
  //   qos: 0,
  //   retain: false,
  // },
};
console.log("Connecting mqtt client");
const client = mqtt.connect(host, options);
client.on("error", (err) => {
  console.log("Connection error: ", err);
  client.end();
});
client.on("reconnect", () => {
  console.log("Reconnecting...");
});
client.on("connect", () => {
  console.log(`Client connected: ${clientId}`);
  // Subscribe
  let deviceCode = document.getElementById("deviceCode").value;
  client.subscribe(`${prefixSubscribe}${deviceCode}`, { qos: 0 }); // ptit/iot24/device/subscribe/device1
});
client.on("message", (topic, message, packet) => {
  let json_string = message.toString();
  let json_data = JSON.parse(json_string);
  let type = json_data.type;
  if (type == "state") {
    let content = json_data.content;
    updateState(content);
  } else {
    console.log(`Received Message: ${message.toString()} On topic: ${topic}`);
  }
});

function updateState(content) {
  document.getElementById("rain").innerText =
    content.rain == 1 ? "Không mưa" : "Có mưa";
  document.getElementById("light").innerText =
    content.light == 1 ? "Yếu" : "Mạnh";
  document.getElementById("soil").innerText = content.soil + "%";
  document.getElementById("humidity").innerText =
    parseFloat(content.humidity).toFixed(2) + "%";
  document.getElementById("temperature").innerText =
    parseFloat(content.temperature).toFixed(2) + "°C";
  document.getElementById("btn-pump").checked = content.isPumpOn;
  document.getElementById("btn-lamp").checked = content.isLampOn;

  // update chart
  updateDataChart(
    parseFloat(content.temperature).toFixed(0),
    content.soil,
    parseFloat(content.humidity).toFixed(0)
  );
}

function mqtt_publish_message(type, content) {
  let deviceCode = document.getElementById("deviceCode").value;
  let topic_publish = `${prefixPublish}${deviceCode}`;
  let doc = {};
  doc["type"] = type;
  if (type == "control") {
    // content: pump-off, pump-on, lamp-off, lamp-on
    doc["content"] = {
      command: content,
    };
  } else if (type == "update" || type == "update-time") {
    // content: {id, ip, thresh...}
    doc["content"] = content;
  }
  client.publish(topic_publish, JSON.stringify(doc), {
    qos: 0,
    retain: false,
  });
}

document.getElementById("btn-pump").addEventListener("click", () => {
  isPumpOn = document.getElementById("btn-pump").checked;
  if (!isPumpOn) {
    mqtt_publish_message("control", "pump-off");
  } else {
    mqtt_publish_message("control", "pump-on");
  }
});

document.getElementById("btn-lamp").addEventListener("click", () => {
  isLampOn = document.getElementById("btn-lamp").checked;
  if (!isLampOn) {
    mqtt_publish_message("control", "lamp-off");
  } else {
    mqtt_publish_message("control", "lamp-on");
  }
});

let updateDeviceForm = document.querySelector("#update-device-form");
if (updateDeviceForm) {
  updateDeviceForm.addEventListener("submit", (e) => {
    e.preventDefault();
    let id = document.getElementById("id").value;
    let deviceCode = document.getElementById("deviceCode").value;
    let ip = document.getElementById("ip").value;
    let name = document.getElementById("name").value;
    let soilThreshold = document.getElementById("soilThreshold").value;
    let temperatureThreshold = document.getElementById(
      "temperatureThreshold"
    ).value;
    let pumpTimeMinute = document.getElementById("pumpTimeMinute").value;

    let timeCheckDocs = document.getElementsByName("time_check");
    let timeChecks = [];
    let i = 1;
    timeCheckDocs.forEach((e) => {
      inputId = e.querySelector('input[type="number"]').value;
      inputTimer = e.querySelector('input[type="time"]').value;
      inputIsOn = e.querySelector('input[type="checkbox"]').checked;
      timeChecks.push({
        id: inputId + "",
        timer: inputTimer,
        on: inputIsOn + "",
      });
      mqtt_publish_message("update-time", {
        idx: i,
        timer: inputTimer,
        on: inputIsOn + "",
      });
      i += 1;
    });

    mqtt_publish_message("update", {
      soilThreshold,
      temperatureThreshold,
      pumpTimeMinute,
    });

    let dataToUpdate = {
      id,
      deviceCode,
      name,
      ip,
      soilThreshold,
      temperatureThreshold,
      pumpTimeMinute,
      timeChecks,
    };
    let jsonString = JSON.stringify(dataToUpdate);

    let url = `http://${ipServer}:8080/device`;
    let options = {
      method: "PUT",
      mode: "cors",
      headers: {
        "Content-Type": "application/json",
        Connection: "keep-alive",
      },
      body: jsonString,
    };

    fetch(url, options)
      .then((response) => {
        console.log(response);
        if (response.status == 400) {
          return response.text().then((text) => {
            throw new Error(text);
          });
        } else if (response.status == 408) {
          console.log("Cập nhật thất bại");
        } else if (response.status == 200) {
          return response.json();
        }
      })
      .then((data) => {
        if (data != null) {
          // Xử lý dữ liệu khi không có lỗi
          document.getElementById("error-msg-soil-threshold").innerText = "";
          document.getElementById("error-msg-temperature-threshold").innerText =
            "";
          document.getElementById("error-msg-pump-time-minute").innerText = "";
          Swal.fire("Cập nhật thành công!");
        }
      })
      .catch((error) => {
        errorData = JSON.parse(error.message);
        document.getElementById("error-msg-soil-threshold").innerText =
          "soilThreshold" in errorData ? errorData.soilThreshold : "";
        document.getElementById("error-msg-temperature-threshold").innerText =
          "temperatureThreshold" in errorData
            ? errorData.temperatureThreshold
            : "";
        document.getElementById("error-msg-pump-time-minute").innerText =
          "pumpTimeMinute" in errorData ? errorData.pumpTimeMinute : "";
      });
  });
}

let selectplantForm = document.getElementById("select-plant-form");
if (selectplantForm) {
  selectplantForm.addEventListener("submit", (e) => {
    e.preventDefault();
    spinner.classList.toggle("show");
    let deviceId = document.getElementById("id").value;
    let selected = document.getElementById("plant-id");
    selected = selected.options[selected.selectedIndex];
    let plantId = selected.value;
    let plantName = selected.text;
    let data = {
      deviceId,
      plantId,
    };
    let url = `http://${ipServer}:8080/device/change-plant`;
    let options = {
      method: "PUT",
      mode: "cors",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    };

    fetch(url, options)
      .then((response) => {
        console.log(response);
        spinner.classList.remove("show");
        if (response.ok) {
          console.log("Thay cây thành công");
          getInfoPlant(plantName);
        }
      })
      .catch((error) => console.error(error));
  });
}

let imageplantForm = document.getElementById("image-plant-form");
if (imageplantForm) {
  imageplantForm.addEventListener("submit", (e) => {
    e.preventDefault();
    let input = document.getElementById("image-plant-file");
    let data = new FormData();
    data.append("file", input.files[0]);
    let url = "";
    let options = {
      method: "POST",
      body: data,
    };
  });
}

function chartOnOrOff() {
  let tempChart = document.getElementById("temperature-chart");
  let humiChart = document.getElementById("humidity-chart");
  let soilChart = document.getElementById("soil-chart");
  tempChart.style.display =
    tempChart.style.display === "none" ? "block" : "none";
  humiChart.style.display =
    humiChart.style.display === "none" ? "block" : "none";
  soilChart.style.display =
    soilChart.style.display === "none" ? "block" : "none";
}

function readings() {
  let id = document.getElementById("id").value; // deviceID
  let url = `http://${ipServer}:8080/reading/all/${id}`;
  let options = {
    method: "GET",
    mode: "cors",
  };
  fetch(url, options)
    .then((response) => {
      return response.json();
    })
    .then((data) => {
      console.log(data);
      let content = `<a class='btn btn-danger mb-2' href='${window.location.href}'>Trở về</a>`;
      content += `<div class="bg-light rounded h-100 p-4">
      <h6 class="mb-4">Lịch sử kiểm tra</h6>
      <div class="accordion" id="accordionExample">`;
      data.forEach((item, index) => {
        content += `<div class="accordion-item">
                      <h2 class="accordion-header" id="heading${index}">
                          <button class="accordion-button collapsed" type="button"
                              data-bs-toggle="collapse" data-bs-target="#collapse${index}"
                              aria-expanded="false" aria-controls="collapse${index}">
                              Thời gian: ${item.time} - ${
          item.pumpOn == 1 ? "Tưới cây" : "Không tưới"
        }
                          </button>
                      </h2>
                      <div id="collapse${index}" class="accordion-collapse collapse"
                          aria-labelledby="heading${index}" data-bs-parent="#accordionExample">
                          <div class="accordion-body">
                              <p>Độ ẩm đất thực tế: ${item.actualSoil}%</p>
                              <p>Ngưỡng độ ẩm đất: ${item.soilThreshold}%</p>
                              <p>Nhiệt độ thực tế: ${
                                item.actualTemperature
                              }°C</p>
                              <p>Ngưỡng nhiệt độ: ${
                                item.temperatureThreshold
                              }°C</p>
                              <p>Thời gian bơm: ${item.pumpTime} phút</p>
                          </div>
                      </div>
                  </div>`;
      });
      content += `</div></div>`;
      document.getElementById("main-content").innerHTML = content;
    });
}

function updateDataChart(data1, data2, data3) {
  var percentageText = document.querySelector("#temperature-chart .percentage");
  if (percentageText) {
    percentageText.textContent = `${data1}°C`;
  }
  var circlePath = document.querySelector("#temperature-chart .circle");
  if (circlePath) {
    circlePath.setAttribute("stroke-dasharray", `${data1}, 100`);
  }

  var percentageText = document.querySelector("#soil-chart .percentage");
  if (percentageText) {
    percentageText.textContent = `${data2}%`;
  }
  var circlePath = document.querySelector("#soil-chart .circle");
  if (circlePath) {
    circlePath.setAttribute("stroke-dasharray", `${data2}, 100`);
  }

  var percentageText = document.querySelector("#humidity-chart .percentage");
  if (percentageText) {
    percentageText.textContent = `${data3}%`;
  }
  var circlePath = document.querySelector("#humidity-chart .circle");
  if (circlePath) {
    circlePath.setAttribute("stroke-dasharray", `${data3}, 100`);
  }
}

function checkNorm() {
  const fileInput = document.getElementById("image-norm-file");
  const file = fileInput.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onloadend = () => {
      const image = reader.result.split(",")[1];
      const url = `http://${ipServer}:5000/pets-detection`;
      const options = {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: image,
      };

      const spinner = document.getElementById("spinner");
      spinner.classList.toggle("show");

      fetch(url, options)
        .then((response) => {
          return response.json();
        })
        .then((data) => {
          console.log(data);
          spinner.classList.remove("show");
          if (data.bbox.length > 0) {
            bbox = data.bbox;
            const img = new Image();
            img.onload = function () {
              drawImageWithFrame(img, bbox, "Có con gì đó!");
            };
            img.src = reader.result;
          } else {
            Swal.fire("Không phát hiện sâu!");
          }
        });
    };
    reader.readAsDataURL(file);
  } else {
    Swal.fire("Bạn chưa chọn ảnh!");
  }
}


function checkNorm2() {
  const fileInputs = [
    document.getElementById("image-norm-file2"),
    document.getElementById("image-norm-file3"),
    document.getElementById("image-norm-file4"),
    document.getElementById("image-norm-file5"),
  ];

  // Kiểm tra xem tất cả các ô input có chọn đủ ảnh không
  const isAllFilesSelected = fileInputs.every((fileInput) => fileInput.files.length === 1);

  if (isAllFilesSelected) {
    const formData = new FormData();

    fileInputs.forEach((fileInput, index) => {
      const file = fileInput.files[0];
      console.log(file)
      formData.append(`image${index + 1}`, file);
    });

    const url = `http://${ipServer}:5000/plants-grow`;
    const options = {
      method: "POST",
      body: formData,
    };

    const spinner = document.getElementById("spinner");
    spinner.classList.toggle("show");

    fetch(url, options)
        .then((response) => response.json())
        .then((data) => {
          console.log(data)
          spinner.classList.remove("show");
          Swal.fire(data);
        })
        .catch((error) => {
          console.error(error);
          spinner.classList.remove("show");
          Swal.fire("Error occurred while processing images");
        });
  } else {
    Swal.fire("Bạn cần chọn đúng 1 ảnh cho mỗi tuần!");
  }
}









function checkPlant() {
  const fileInput = document.getElementById("image-plant-file");
  const file = fileInput.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onloadend = () => {
      const image = reader.result.split(",")[1];
      const url = `http://${ipServer}:5000/plants-classification`;
      const options = {
        method: "POST",
        mode: "cors",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: image,
      };

      const spinner = document.getElementById("spinner");
      spinner.classList.toggle("show");

      fetch(url, options)
        .then((response) => {
          console.log(response);
          return response.json();
        })
        .then((data) => {
          spinner.classList.remove("show");
          console.log(data);
          const img = new Image();
          let pred = data.prediction.split(/[-–]/);

          let plant_name = "Cây " + pred[0].trim();
          img.onload = function () {
            drawImageWithFrame(img, [], plant_name);
          };
          img.src = reader.result;
          if (pred[0].trim() != "không tìm thấy trong CSDL") {
            console.log("NHẬN RA CÂY: " + pred[0].trim());
            getInfoPlant(pred[0].trim());
          } else {
            document.getElementById("error-msg-soil-threshold").innerText = "";
            document.getElementById(
              "error-msg-temperature-threshold"
            ).innerText = "";
          }
        });
    };
    reader.readAsDataURL(file);
  } else {
    Swal.fire("Bạn chưa chọn ảnh!");
  }
}

function getInfoPlant(plantName) {
  let url = `http://${ipServer}:8080/plant/get-info/${plantName}`;
  let options = {
    method: "get",
    mode: "cors",
  };
  fetch(url, options)
    .then((response) => {
      return response.json();
    })
    .then((data) => {
      console.log(data);
      let txtSoil = `Gợi ý: ${data.minSoil}%-${data.maxSoil}%`;
      let txtTemp = `Gợi ý: ${data.minTemperature}°C-${data.maxTemperature}°C`;
      document.getElementById("error-msg-soil-threshold").innerText = txtSoil;
      document.getElementById("error-msg-temperature-threshold").innerText =
        txtTemp;
    })
    .catch((err) => {
      console.log(err);
    });
}

function drawImageWithFrame(img, bbox, content) {
  const canvas = document.getElementById("popupCanvas");
  const ctx = canvas.getContext("2d");

  // Set canvas size to match image size
  canvas.width = img.width;
  canvas.height = img.height;

  // Draw the image on the canvas
  ctx.drawImage(img, 0, 0);

  if (bbox.length == 0) {
    // plant
  } else {
    // norm
    bbox.forEach((row, i) => {
      ctx.strokeStyle = "red";
      ctx.lineWidth = 10;
      ctx.strokeRect(row[0] - row[2] / 2, row[1] - row[3] / 2, row[2], row[3]);
    });
  }

  Swal.fire({
    title: "Kết quả!",
    text: content,
    imageUrl: canvas.toDataURL(),
    imageWidth: 640,
    imageHeight: 480,
    imageAlt: "Custom image",
  });
}

function closeImagePopup() {
  const imagePopup = document.getElementById("imagePopup");
  imagePopup.style.display = "none";
}



