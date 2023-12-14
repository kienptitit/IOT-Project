const ipServer = "10.208.208.210";

let addDeviceForm = document.querySelector("#add-device-form");
if (addDeviceForm) {
  addDeviceForm.addEventListener("submit", (e) => {
    e.preventDefault();
    const deviceCode = document.getElementById("deviceCode").value;
    const id = document.getElementById("id").value;
    const ip = document.getElementById("ip").value;
    const name = document.getElementById("name").value;
    const description = document.getElementById("description").value;
    const soilThreshold = 50;
    const temperatureThreshold = 20;
    const pumpTimeMinute = 1;
    let url = `http://${ipServer}:8080/device`;
    let options = {
      method: "POST",
      mode: "cors",
      headers: {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*",
      },
      body: JSON.stringify({
        id,
        ip,
        deviceCode,
        name,
        description,
        soilThreshold,
        temperatureThreshold,
        pumpTimeMinute,
      }),
    };
    fetch(url, options)
      .then((response) => {
        console.log("response from server: ", response);
        if (!response.ok) {
          throw new Error(`HTTP error ${response.status}`);
        }
        return response.json();
      })
      .then((newData) => {
        console.log("Data new:", newData);
        alert("Thêm mới thành công");
        window.location.href = `http://${ipServer}:8080/home`;
      })
      .catch((error) => {
        console.error("Lỗi thêm mới:", error);
      });
  });
}

function handleDeleteDevice(id) {
  if (confirm("Bạn chắc chắn muốn xóa thiết bị này?")) {
    document.getElementById(id).parentElement.remove();
    fetch(`http://${ipServer}:8080/device/${id}`, {
      method: "delete",
    });
  }
}
