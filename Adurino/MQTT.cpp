#include "MQTT.h"
#include <WiFi.h>
#include <PubSubClient.h>

MQTT::MQTT(const char* topic_subscribe) {
  this->topic_subscribe = topic_subscribe;
}

void MQTT::init(PubSubClient *client) {
  this->client = client;
  this->client->setServer(this->mqtt_server, this->port);
}

void MQTT::reconnect() {
  while (!this->client->connected()) {
    Serial.print("Attempting MQTT connection...");
    String clientId = "IOT24-";
    clientId += String(random(0xffff), HEX);
    if (this->client->connect(clientId.c_str())) {
      Serial.println("connected");
      this->client->subscribe(this->topic_subscribe);
    } else {
      Serial.print("failed, rc=");
      Serial.println(" try again in 5 seconds");
      delay(5000);
    }
  }
}

void MQTT::loop() {
  if (!this->client->connected()) {
    this->reconnect();
  }
  this->client->loop();
}

void MQTT::setCallback(function<void(char*, uint8_t*, unsigned int)> callback) {
  this->client->setCallback(callback);
}

void MQTT::publish(const char* topic, const char* message){
  this->client->publish(topic, message);
}