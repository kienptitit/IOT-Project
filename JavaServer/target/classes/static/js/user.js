const ipServer = "10.208.208.210";
let registerForm = document.querySelector("#register-form");
if (registerForm) {
  registerForm.addEventListener("submit", (e) => {
    e.preventDefault();
    let fullName = document.getElementById("full-name").value;
    let username = document.getElementById("username").value;
    let password = document.getElementById("password").value;
    let repeatPassword = document.getElementById("repeat-password").value;
    if (fullName == "") {
      alert("Vui lòng điền đầy đủ họ tên");
      return;
    } else if (username == "") {
      alert("Vui lòng tài khoản bạn muốn đăng ký");
      return;
    } else if (password == "") {
      alert("Vui lòng nhập mật khẩu");
      return;
    } else if (password != repeatPassword) {
      alert("Mật khẩu không khớp");
      return;
    }
    let data = {
      fullName,
      username,
      password,
    };
    let url = `http://${ipServer}:8080/register`;
    let options = {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    };
    fetch(url, options)
      .then((response) => {
        console.log(response);
        if (response.ok) {
          return response.json();
        } else {
          return response.json().then((data) => {
            console.log("loi ne: ",data.message);
            throw new Error(data.message || "Something went wrong");
          });
        }
      })
      .then((data) => {
        console.log(data);
        alert("Đăng ký tài khoản thành công");
        
      })
      .catch((error) => {
        console.error(error.message);
      });
  });
}
