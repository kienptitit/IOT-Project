#include "esp32-hal.h"
#include "LCDControl.h"


LCDControl::LCDControl(LiquidCrystal_I2C *lcd) {
  this->lcd = lcd;
}

void LCDControl::printC(int line, String message) {
  if (line == 0) {
    this->lcd->clear();
  }
  this->lcd->setCursor(0, line);
  this->lcd->print(message);
}

void LCDControl::loop(int h, int setH, int s, int setS, int temp, int setTemp, int soil, int setSoil) {
  if (millis() - lastTimeShow >= timeShow) {
    this->indexShow = this->indexShow + 1;
    this->indexShow = this->indexShow % 3;
    if (this->indexShow == 0) {
      this->printC(0, "Dong ho: " + String(h) + ":" + String(s));
      if (setH == -1 && setS == -1) {
        this->printC(1, "Hen gio: Ko co");
      } else {
        this->printC(1, "Hen gio: " + String(setH) + ":" + String(setS));
      }
    } else if (this->indexShow == 1) {
      this->printC(0, "Nhiet do: " + String(temp) + "*C");
      this->printC(1, "Nguong: " + String(setTemp) + "*C");
    } else if (this->indexShow == 2) {
      this->printC(0, "Do am dat: " + String(soil) + "%");
      this->printC(1, "Nguong: " + String(setSoil) + "%");
    }
    lastTimeShow = millis();
  }
}
