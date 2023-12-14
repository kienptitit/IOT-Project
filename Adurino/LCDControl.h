#ifndef LCDControl_H
#define LCDControl_H

#include <LiquidCrystal_I2C.h>

class LCDControl {
public:
    LCDControl(LiquidCrystal_I2C *lcd); 
    void printC(int line, String message);
    void loop(int h, int setH, int s, int setS, int temp, int setTemp, int soil, int setSoil);

private:
    LiquidCrystal_I2C *lcd;
    int lastTimeShow;
    int timeShow = 5000;
    int indexShow;
};

#endif