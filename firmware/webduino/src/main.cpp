#include <Arduino.h>

#define LED 3
bool initialized = false;

void blinkLed();

void setup()
{
  Serial.begin(9600);
  pinMode(LED, OUTPUT);
}

void blinkLed()
{
  digitalWrite(LED, HIGH);
  delay(50);
  digitalWrite(LED, LOW);
  delay(2000);
}

void loop()
{
  if (Serial.available() > 0)
  {
    String cmd = Serial.readStringUntil('\n');
    cmd.trim();

    if (cmd == "INIT")
    {
      digitalWrite(LED, HIGH);
      Serial.print("Initialized reading");
      initialized = true;
    }

    if (cmd == "STOP")
    {
      Serial.print("Exiting...");
      initialized = false;
    }
  }
  if (!initialized)
  {
    blinkLed();
  }
  else
  {
  }
}
