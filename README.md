# IOT-Group 38
- Nguyễn Trung Kiên - B20DCCN359 
- Trịnh Tuấn Khanh - B20DCCN371
# Run Project
1. Create a database named 'iot-1' in your MySQL Server. Ensure to update the MySQL username and password in the `application.properties` file accordingly.
2. Modify the `ipServer` variable in the `manage-device.js`, `add-device.js`, and `user.js` files to reflect your IPV4 address.
3. Run `Main.java`
4. Retrieve the [Weight](https://drive.google.com/file/d/1tBKcUIYnt359pCh4K17tSwWfGAVvfeA1/view?usp=drive_link) file from Google Drive, unzip the contents, and deposit them into the PythonServer directory as follows:
    ```bash 
   PythonServer
        Weights
            Checkpoint-Swin
            best_kaggle.pt
            class_name.pkl
            model_simaese_59.pt
            new_model_plants_grow.pt
        Plants_Grow.py
        Server.py
        requirement.txt
   ```
5. Install the required libraries:
   ```commandline
   conda activate <your enviroment>
   pip install requirement.txt
   ```
6. Run Python Server
   ```commandline
   cd PythonServer 
   python Server.py
   ```
