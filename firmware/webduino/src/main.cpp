#include <Arduino.h>
#include <Ultrasonic.h>

Ultrasonic ultrasonic(4, 5);

#define LED 3
bool initialized = false;
unsigned int prevTime = 0;
bool ledOn = false;

void blinkLed();

void blinkLed()
{
  unsigned int actualTime = millis();

  if (ledOn && actualTime - prevTime >= 100)
  {
    digitalWrite(LED, LOW);
    ledOn = false;
    prevTime = actualTime;
  }
  else if (!ledOn && actualTime - prevTime >= 2000)
  {
    digitalWrite(LED, HIGH);
    ledOn = true;
    prevTime = actualTime;
  }
}

void setup()
{
  Serial.begin(9600);
  pinMode(LED, OUTPUT);
  digitalWrite(LED, LOW);
}

void loop()
{
  if (Serial.available() > 0)
  {
    String cmd = Serial.readStringUntil('\n');
    cmd.trim();

    if (cmd == "INIT")
    {
      initialized = true;
      digitalWrite(LED, HIGH);
      Serial.println(ultrasonic.read());
      // Serial.print("Initialized reading");
    }

    if (cmd == "STANDBY")
    {
      Serial.print("Exiting...");
      initialized = false;
    }
  }
  else if (!initialized)
  {
    blinkLed();
  }
  else
  {
    long distance = ultrasonic.read();
    Serial.print("Distancia: ");
    Serial.print(distance);
    Serial.println(" cm");
    delay(200);
  }
}
