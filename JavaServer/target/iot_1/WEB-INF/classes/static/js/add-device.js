let addDeviceForm = document.querySelector("#add-device-form");
if (addDeviceForm) {
  addDeviceForm.addEventListener("submit", (e) => {
    e.preventDefault();
    const ip = document.getElementById("ip").value;
    const name = document.getElementById("name").value;
    console.log(name);
    let url = "http://localhost:8081/device";
    let options = {
      method: "POST",
      mode:'cors',
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        ip: ip,
        name: name,
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
        alert("Add new device successfully")
        window.location.href=`http://localhost:8081/home`
      })
      .catch((error) => {
        console.error("Error updating data:", error);
      });
  });
}

function handleDeleteDevice(id) {
  if (confirm("Are you sure?")) {
    document.getElementById(id).parentElement.remove();
    fetch(`http://localhost:8081/device/${id}`, {
      method: "delete",
    });
  }
}
