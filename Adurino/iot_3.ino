#include <Wire.h>
#include <LiquidCrystal_I2C.h>

#include <WiFi.h>
#include <WebServer.h>

#include <ArduinoJson.h>

#include <TimeAlarms.h>
#include <TimeLib.h>

#include <NTPClient.h>
#include <WiFiUDP.h>

#include <DHT.h>
#include <HTTPClient.h>

#include <PubSubClient.h>

#include "LCDControl.h"
#include "MQTT.h"

#define RAIN_PIN 39

#define SOIL_PIN 36

#define DHT_PIN 25
#define DHT_TYPE DHT11

#define LIGHT_PIN 26

// lcd
#define SDA_PIN 27
#define SCL_PIN 14

// relay
#define PUMP_PIN 32  //IN1
#define LAMP_PIN 33  // IN2

#define TRIGGER_ON_PUMP 5
#define TRIGGER_OFF_PUMP 18
#define TRIGGER_ON_LAMP 16
#define TRIGGER_OFF_LAMP 17

#define URL_UPLOAD "http://192.168.0.103:8080/reading"

#define NUM_ALARMS 5

int rainVal, soilVal, lightVal;
float temperatureVal, humidityVal;
bool isPumpOn = false, isLampOn = false;
int pumpingTime = 10000, curPumpingTime;
int timeCheckLightSeconds = 30;
int temperatureThreshold = 20, soilThreshold = 50;

DHT dht(DHT_PIN, DHT_TYPE);

LiquidCrystal_I2C lcd(0x27, 16, 2);  // 16 cols, 2 rows of lcd
LCDControl myLCD(&lcd);

WebServer server(80);

WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "time.google.com");

const char *ssid = "TP-Link_C6701";
const char *password = "kienb20dccn359";
const char *deviceCode = "device1";
const char *publish = "ptit/iot24/device/subscribe/device1";
const char *subscribe = "ptit/iot24/device/publish/device1";

WiFiClient espClient;
PubSubClient client(espClient);

MQTT mqtt(subscribe);

IPAddress staticIP(192, 168, 0, 150);
IPAddress gateway(192, 168, 0, 1);
IPAddress subnet(255, 255, 255, 0);
IPAddress dns(8, 8, 8, 8);

struct AlarmInfo {
  bool enabled;
  int hour;
  int minute;
  void (*callback)();
  AlarmID_t alarmID;
};

AlarmInfo alarms[NUM_ALARMS + 1];

void setup() {
  Serial.begin(115200);

  // set up pin output
  pinMode(PUMP_PIN, OUTPUT);
  digitalWrite(PUMP_PIN, LOW);
  isPumpOn = false;

  pinMode(LAMP_PIN, OUTPUT);
  digitalWrite(LAMP_PIN, LOW);
  isLampOn = false;

  pinMode(TRIGGER_ON_PUMP, INPUT_PULLUP);
  pinMode(TRIGGER_OFF_PUMP, INPUT_PULLUP);
  pinMode(TRIGGER_ON_LAMP, INPUT_PULLUP);
  pinMode(TRIGGER_OFF_LAMP, INPUT_PULLUP);

  pinMode(RAIN_PIN, INPUT);
  pinMode(LIGHT_PIN, INPUT);

  // set up lcd 1602
  Wire.begin(SDA_PIN, SCL_PIN);
  lcd.init();
  lcd.backlight();
  dht.begin();
  randomSeed(42);

  WiFi.config(staticIP, gateway, subnet, dns);

  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    myLCD.printC(0, ".");
    Serial.print(".");
    delay(500);
  }

  lcd.clear();
  Serial.print(WiFi.localIP());
  Serial.println();

  myLCD.printC(0, "Device Code: ");
  myLCD.printC(1, deviceCode);

  timeClient.begin();
  timeClient.update();
  setTime(timeClient.getHours() + 7, timeClient.getMinutes(), timeClient.getSeconds(), 1, 1, 2023);
  timeClient.end();

  for (int i = 0; i <= NUM_ALARMS; i++) {
    alarms[i] = { false, 9, 0, checkAndPump, -1 };
  }

  Alarm.timerRepeat(timeCheckLightSeconds, checkLight);

  mqtt.init(&client);
  mqtt.setCallback(callback);

  server.enableCORS(true);
  server.onNotFound(notFound);
  server.begin();
  delay(2000);
}

void loop() {
  Alarm.delay(500);
  mqtt.loop();

  publishState();
  server.handleClient();

  readSensor();

  checkAlarm();
  int nxtA = getNextAlarmID();
  Serial.print("NExt alarm: ");
  Serial.println(nxtA);
  Serial.println();
  if (nxtA != -1) {
    myLCD.loop(hour(), alarms[nxtA].hour, minute(), alarms[nxtA].minute, int(temperatureVal), temperatureThreshold, soilVal, soilThreshold);
  } else {
    myLCD.loop(hour(), -1, minute(), -1, int(temperatureVal), temperatureThreshold, soilVal, soilThreshold);
  }

  if ((rainVal == 0) && (isPumpOn == true)) {
    handleTurnOffPump();
  }
  if ((millis() - curPumpingTime > pumpingTime) && (isPumpOn == true)) {
    handleTurnOffPump();
  }
  if ((digitalRead(TRIGGER_ON_PUMP) == 0) && (isPumpOn == false)) {
    handleTurnOnPump();
  }
  if ((digitalRead(TRIGGER_OFF_PUMP) == 0) && (isPumpOn == true)) {
    handleTurnOffPump();
  }
  if ((digitalRead(TRIGGER_ON_LAMP) == 0) && (isLampOn == false)) {
    handleTurnOnLamp();
  }
  if ((digitalRead(TRIGGER_OFF_LAMP) == 0) && (isLampOn == true)) {
    handleTurnOffLamp();
  }
  digitalClockDisplay();
}

void checkAlarm() {
  int h = hour();
  int m = minute();
  for (int i = 1; i <= NUM_ALARMS; ++i) {
    if (alarms[i].enabled) {
      if (alarms[i].hour == h && alarms[i].minute == m) {
        checkAndPump();
        alarms[i].enabled = !alarms[i].enabled;
      }
    }
  }
}

void callback(char *topic, byte *payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }

  DynamicJsonDocument doc(2048);
  deserializeJson(doc, payload, length);
  const char *type = doc["type"];
  if (strcmp(type, "control") == 0) {
    JsonVariant content = doc["content"].as<JsonVariant>();
    const char *command = content["command"].as<const char *>();
    handleCommand(command);
  } else if (strcmp(type, "update") == 0) {
    JsonVariant content = doc["content"].as<JsonVariant>();
    handleUpdate(content);
  } else if (strcmp(type, "update-time") == 0) {
    JsonVariant content = doc["content"].as<JsonVariant>();
    handleUpdateTime(content);
  }
  Serial.println();
}

void handleCommand(const char *command) {
  if (strcmp(command, "pump-on") == 0) handleTurnOnPump();
  else if (strcmp(command, "pump-off") == 0) handleTurnOffPump();
  else if (strcmp(command, "lamp-on") == 0) handleTurnOnLamp();
  else if (strcmp(command, "lamp-off") == 0) handleTurnOffLamp();
}

void handleUpdate(JsonVariant content) {
  soilThreshold = content["soilThreshold"].as<int>();
  temperatureThreshold = content["temperatureThreshold"].as<int>();
  pumpingTime = content["pumpTimeMinute"].as<int>();
  pumpingTime *= 1000;
}

void handleUpdateTime(JsonVariant content) {
  int idx = content["idx"].as<int>();
  const char *on = content["on"].as<const char *>();
  const char *timer = content["timer"].as<const char *>();

  Serial.println(idx);
  if (on[0] == 't') {
    int h, m;
    sscanf(timer, "%d:%d", &h, &m);
    alarms[idx] = { true, h, m, checkAndPump };
  } else {
    alarms[idx] = { false, 9, 0, checkAndPump };
  }
}

void publishState() {
  const size_t capacity = JSON_OBJECT_SIZE(2) + JSON_OBJECT_SIZE(7) + 60;
  DynamicJsonDocument doc(capacity);
  JsonObject outerObject = doc.to<JsonObject>();
  outerObject["type"] = "state";
  JsonObject content = doc.createNestedObject("content");
  content["rain"] = rainVal;
  content["light"] = lightVal;
  content["soil"] = soilVal;
  content["humidity"] = humidityVal;
  content["temperature"] = temperatureVal;
  content["isPumpOn"] = isPumpOn;
  content["isLampOn"] = isLampOn;
  String json_string;
  serializeJson(doc, json_string);
  mqtt.publish(publish, json_string.c_str());
}

void checkLight() {
  if ((lightVal == 1) && (isLampOn == false)) {
    handleTurnOnLamp();
  } else if ((lightVal == 0) && (isLampOn == true)) {
    handleTurnOffLamp();
  }
}

void digitalClockDisplay() {
  // digital clock display of the time
  Serial.print(hour());
  printDigits(minute());
  printDigits(second());
  Serial.println();
}

void printDigits(int digits) {
  Serial.print(":");
  if (digits < 10)
    Serial.print('0');
  Serial.print(digits);
}

int getNextAlarmID() {
  int nearestIndex = -1;
  int nearestDiff = INT_MAX;

  for (int i = 1; i <= NUM_ALARMS; ++i) {
    if (alarms[i].enabled) {
      int diff = (alarms[i].hour * 60 + alarms[i].minute) - (hour() * 60 + minute());
      if (0 < diff && diff < nearestDiff) {
        nearestDiff = diff;
        nearestIndex = i;
      }
    }
  }
  return nearestIndex;
}

void readSensor() {
  rainVal = digitalRead(RAIN_PIN);
  lightVal = digitalRead(LIGHT_PIN);
  soilVal = map(analogRead(SOIL_PIN), 0, 4095, 100, 0);
  temperatureVal = dht.readTemperature();
  humidityVal = dht.readHumidity();
}

void handleTurnOnPump() {
  isPumpOn = true;
  digitalWrite(PUMP_PIN, HIGH);
  Serial.println("Da bat bom");
  curPumpingTime = millis();
}

void handleTurnOffPump() {
  isPumpOn = false;
  digitalWrite(PUMP_PIN, LOW);
  Serial.println("Da tat bom");
}

void handleTurnOnLamp() {
  isLampOn = true;
  digitalWrite(LAMP_PIN, HIGH);
  Serial.println("Da bat den");
}

void handleTurnOffLamp() {
  isLampOn = false;
  digitalWrite(LAMP_PIN, LOW);
  Serial.println("Da tat den");
}

void checkAndPump() {
  Serial.println("check");
  if ((soilThreshold > soilVal) || (temperatureThreshold < temperatureVal)) {
    handleTurnOnPump();
  }
  String url = URL_UPLOAD;
  WiFiClient client;
  HTTPClient http;
  http.begin(client, url);
  http.addHeader("Content-Type", "application/json");

  DynamicJsonDocument jsonDocument(1024);
  jsonDocument["time"] = formatTime(hour(), minute());
  jsonDocument["actualSoil"] = soilVal;
  jsonDocument["actualTemperature"] = temperatureVal;
  jsonDocument["soilThreshold"] = soilThreshold;
  jsonDocument["temperatureThreshold"] = temperatureThreshold;
  jsonDocument["pumpOn"] = isPumpOn ? 1 : 0;
  jsonDocument["pumpTime"] = pumpingTime / 1000;
  String jsonString;
  serializeJson(jsonDocument, jsonString);
  Serial.println(jsonString);
  http.POST(jsonString);
  Serial.println("Da gui reading");
}

String formatTime(int hour, int minute) {
  String ret = "";
  if (hour < 10) {
    ret = ret + "0" + hour;
  } else {
    ret = ret + hour;
  }
  ret = ret + ":";
  if (minute < 10) {
    ret = ret + "0" + minute;
  } else {
    ret = ret + minute;
  }
  return ret;
}

void notFound() {
  server.send(404, "text/plain", "Not found");
}
