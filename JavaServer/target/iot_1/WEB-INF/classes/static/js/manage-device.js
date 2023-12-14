function fetchData() {
  let ip = document.querySelector("#ip").value;
  fetch(`http://${ip}/state`, {
    method: "GET",
    mode: "cors",
  })
    .then((response) => {
      return response.json();
    })
    .then((data) => {
      // console.log(data);
      document.getElementById("rain").innerText = data.rain;
      document.getElementById("light").innerText = data.light;
      document.getElementById("humidity").innerText = data.humidity;
      document.getElementById("isPumpOn").innerText = data.isPumpOn;
    })
    .catch((error) => {
      console.log(error);
    });
}

// fetchData();
setInterval(fetchData, 2000);

function turnOnPump() {
  let ip = document.querySelector("#ip").value;
  fetch(`http://${ip}/pump-on`, {
    method: "GET",
    mode:'cors',
  })
    .then(() => {
      console.log("Get api - turn on Pump - success");
    })
    .then((error) => {
      console.log("Get api - turn on Pump - error: ", error);
    });
}
function turnOffPump() {
  let ip = document.querySelector("#ip").value;
  fetch(`http://${ip}/pump-off`, {
    method: "GET",
    mode:'cors',
  })
    .then(() => {
      console.log("Get api - turn off Pump - success");
    })
    .then((error) => {
      console.log("Get api - turn off Pump - error: ", error);
    });
}

let updateDeviceForm = document.querySelector("#update-device-form");
if (updateDeviceForm) {
  updateDeviceForm.addEventListener("submit", (e) => {
    e.preventDefault();
    let id = document.getElementById("id").value;
    let ip = document.getElementById("ip").value;
    let name = document.getElementById("name").value;
    let humidityThreshold = document.getElementById("humidityThreshold").value;
    let temperatureThreshold = document.getElementById(
      "temperatureThreshold"
    ).value;
    let pumpTimeMinute = document.getElementById("pumpTimeMinute").value;

    let timeCheckDocs = document.getElementsByName("time_check");
    let timeChecks = [];
    timeCheckDocs.forEach((e) => {
      inputId = e.querySelector('input[type="number"]').value;
      inputTimer = e.querySelector('input[type="time"]').value;
      inputIsOn = e.querySelector('input[type="checkbox"]').checked;
      timeChecks.push({ id: inputId, timer: inputTimer, on: inputIsOn+""});
    });

    let dataToUpdate = {
      id,
      name,
      ip,
      humidityThreshold,
      temperatureThreshold,
      pumpTimeMinute,
      timeChecks,
    };
    let jsonString = JSON.stringify(dataToUpdate);
    let url = `http://localhost:8081/device`;
    let options = {
      method: "PUT",
      mode: "cors",
      headers: {
        "Content-Type": "application/json",
      },
      body: jsonString,
    };

    fetch(url, options)
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error ${response.status}`);
        }
        return response.json();
      })
      .then((updatedData) => {
        console.log("Update data successfully", updatedData);
        alert("Update successfully");
      })
      .catch((error) => {
        console.error("Error updating data:", error);
      });
  });
}
