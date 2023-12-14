#ifndef MQTT_H
#define MQTT_H

#include <functional>
#include <PubSubClient.h>

using namespace std;

class MQTT {
public:
  MQTT(const char* topic_subscribe);
  void init(PubSubClient *client);
  void publish(const char* topic, const char* message);
  void setCallback(function<void(char*, uint8_t*, unsigned int)> callback);
  void loop();
  void reconnect();


private:
  PubSubClient *client;
  const char* mqtt_server = "broker.hivemq.com";
  int port = 1883;
  const char* topic_subscribe;
};

#endif