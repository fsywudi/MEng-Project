/*
  WiFi Web Server

 A simple web server that shows the value of the analog input pins.

 This example is written for a network using WPA encryption. For
 WEP or WPA, change the WiFi.begin() call accordingly.

 Circuit:
 * Analog inputs attached to pins A0 through A5 (optional)

 created 13 July 2010
 by dlf (Metodo2 srl)
 modified 31 May 2012
 by Tom Igoe

 */
#define numInterrupts 3

#include <SPI.h>
#include <WiFiNINA.h>
#include <Servo.h>
#include <iostream>
#include <string>

#include "arduino_secrets.h" 
//please enter your sensitive data in the Secret tab/arduino_secrets.h
char ssid[] = SECRET_SSID;        // your network SSID (name)
char pass[] = SECRET_PASS;    // your network password (use for WPA, or use as key for WEP)
int keyIndex = 0;                 // your network key index number (needed only for WEP)
int i = 0;
int pos = 0; 
int distance = 0;
Servo myservo_r;  // create servo object to control a servo
Servo myservo_l;

String command;
char distanceReturn[100];

int leftDistance = 0;
int rightDistance = 0;
int frontDistance = 0;
float soundSpeed = 343.0;

// used to calculate and store distanceToObjects to objects, in cm, for each sensor
float distanceToObject[numInterrupts];

//
// Pin definitions
//
// Trigger pin, common to all 3 US
int triggerPin = 3;
// Echo pins
int leftEchoPin = 2;
int forwardEchoPin = 9;
int rightEchoPin = 10;

//
// Delay times between making measurements, and printing to Serial Monitor
//
// ms to wait between calculations of distances, so as not to constantly calculate things!
int msWaitBetCalculations = 10;
// ms to wait between each output to Serial Monitor, so as not to have tons of data!
int msWaitBetSerialMonitorOutputs = 5;

//
// Volatiles are used in Interrupt routines (more on that later)
//
// Store the travel times of the pulses
volatile unsigned long pulseTravelTime[numInterrupts];
// Variable to keep track of when each Echo pin changes to HIGH
volatile unsigned long timeEchostateOfPinChangedToHigh[numInterrupts];


// Variable to keep track of last time distances calculations were performed
unsigned long lastTimeCalculatedDistances;
// Variable to keep track of last time displayed data to Serial Monitor, so as not to display too much!
unsigned long lastTimeDisplayedToSerialMonitor;
int status = WL_IDLE_STATUS;

WiFiServer server(80);

void setup() {
  myservo_r.attach(5);  // attaches the servo on pin 9 to the servo object
  myservo_l.attach(6);
  
  Serial.begin(9600); // Serial Monitor

  // Trigger pin is an OUTPUT
  pinMode(triggerPin, OUTPUT);

  // Define input for US # 1, which is Echo pin from left US
  pinMode(leftEchoPin, INPUT);
  // Define input for US # 2, which is Echo pin from front US
  pinMode(forwardEchoPin, INPUT);
  // Define input for US # 1, which is Echo pin from right US
  pinMode(rightEchoPin, INPUT);

  // Define interrupt routines to call for each echo pin
  attachInterrupt(digitalPinToInterrupt(leftEchoPin), ISR_Sensor1_onPin2_Interrupt0, CHANGE );   // ISR for INT0
  attachInterrupt(digitalPinToInterrupt(forwardEchoPin), ISR_Sensor2_onPin7_Interrupt1, CHANGE );   // ISR for INT1
  attachInterrupt(digitalPinToInterrupt(rightEchoPin), ISR_Sensor3_onPin8_Interrupt2, CHANGE );   // ISR for INT1

  // Keep track of last time calculated distances (gotta start somewhere!)
  lastTimeCalculatedDistances = millis();
  // Keep track of last time displayed data to Serial Monitor (gotta start somewhere!)
  lastTimeDisplayedToSerialMonitor = millis();
  //Initialize serial and wait for port to open:

  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }

  // check for the WiFi module:
  if (WiFi.status() == WL_NO_MODULE) {
    Serial.println("Communication with WiFi module failed!");
    // don't continue
    while (true);
  }

  String fv = WiFi.firmwareVersion();
  if (fv < WIFI_FIRMWARE_LATEST_VERSION) {
    Serial.println("Please upgrade the firmware");
  }

  // attempt to connect to WiFi network:
  while (status != WL_CONNECTED) {
    Serial.print("Attempting to connect to SSID: ");
    Serial.println(ssid);
    // Connect to WPA/WPA2 network. Change this line if using open or WEP network:
    status = WiFi.begin(ssid, pass);

    // wait 10 seconds for connection:
    delay(10000);
  }
  server.begin();
  // you're connected now, so print out the status:
  printWifiStatus();

  pinMode(LED_BUILTIN, OUTPUT);
}

void updateDistance() {
  // Calculate distances from sensors if enough time has passed, i.e., at least msWaitBetCalculations
  if (millis() - lastTimeCalculatedDistances >= msWaitBetCalculations)
  {
    calculateDistances(); // Call function to take calculate distances
    // Keep track of last time took measurement, which is now
    
    lastTimeCalculatedDistances = millis();
  }

  // Display data to Serial Monitor only if enough time has passed, i.e., at least msWaitBetSerialMonitorOutputs
  /*
  if (millis() - lastTimeDisplayedToSerialMonitor >= msWaitBetSerialMonitorOutputs)
  {
    for (int k = 0; k < numInterrupts; k++) {
      Serial.print(distanceToObject[k]);
      Serial.print(" ::: ");
    }
    Serial.println();
    // Keep track of last time displayed data to Serial Monitor, which is now!
    lastTimeDisplayedToSerialMonitor = millis();
  }
  */
}

void loop() {
  // listen for incoming clients
  updateDistance();
  WiFiClient client = server.available();
  
  if (client) {
    
    //Serial.println("new client");
    while (client.available()) {
      char c = client.read();
      while (isalnum(c)){
          command += c;
          c = client.read();
        }
      //Serial.println(i);
      Serial.println("Command received: " + command);
      // Serial.println(command[4]);
      // Serial.println(command.indexOf("left"));
      if (command.equals("1")){
        Serial.println("Forward");
        moveForward();
        }
      else if (command.equals("2")){
        Serial.println("Left");
        turnLeft();
        }
      else if (command.equals("3")){
        Serial.println("Right");
        turnRight();
        }
      else if (command.equals("4")){
        Serial.println("Back");
        moveBackward();
        }
      else if (command.equals("0")){
        Serial.println("Stop");
        stopMove();
        }
      else if (command.equals("5")){
        Serial.println("Distance query");
        //distance = rand() % 100;
        
        String s = String(distanceReturn);
        Serial.println(s);
        client.println(s);
        //Serial.println("Distance: " + distance);
        }
      else {
        Serial.println("Unknown command: " + command);
        client.println(i);
        i++;
        }
      command = "";
    }
    
    client.stop();
    //Serial.println("client disconnected");
  }
}

void printWifiStatus() {
  // print the SSID of the network you're attached to:
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  // print your board's IP address:
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

  // print the received signal strength:
  long rssi = WiFi.RSSI();
  Serial.print("signal strength (RSSI):");
  Serial.print(rssi);
  Serial.println(" dBm");
}

void turnLeft(){
  /*
    for (pos = 60; pos <= 90; pos += 1) { // goes from 0 degrees to 180 degrees
      myservo_r.write(pos);
      myservo_l.write(pos);// tell servo to go to position in variable 'pos'
      delay(15);                       // waits 15 ms for the servo to reach the position
  }
  */

  myservo_l.write(80);
  myservo_r.write(80);

}

void turnRight(){
  /*
  for (pos = 120; pos >= 90; pos -= 1) { // goes from 0 degrees to 180 degrees
    myservo_r.write(pos);
    myservo_l.write(pos);// tell servo to go to position in variable 'pos'
    delay(15);                       // waits 15 ms for the servo to reach the position
  }*/
  
  myservo_l.write(97);
  myservo_r.write(97);
  
}
void moveForward(){
  /*
    for (pos = 180; pos >= 90; pos -= 1) { // goes from 0 degrees to 180 degrees
      myservo_r.write(pos);
      myservo_l.write(180 - pos);// tell servo to go to position in variable 'pos'
      delay(15);                       // waits 15 ms for the servo to reach the position
  }*/
  
  myservo_l.write(70);
  myservo_r.write(100);
}
void moveBackward(){
  /*
    for (pos = 0; pos <= 90; pos += 1) { // goes from 0 degrees to 180 degrees
      myservo_r.write(pos);
      myservo_l.write(180 - pos);// tell servo to go to position in variable 'pos'
      delay(15);                       // waits 15 ms for the servo to reach the position
  }*/
  
  myservo_l.write(99);
  myservo_r.write(80);

}
void stopMove(){
  myservo_l.write(90);
  myservo_r.write(90);

}
void calculateDistances()
{
  // Disable interrupts while calculating so as not to mess things up
  //cli();
  noInterrupts();  for (int k = 0; k < numInterrupts; k++)
  {
    distanceToObject[k] = (pulseTravelTime[k] / 2.0) * (float)soundSpeed / 10000.0; // Calculate distances
  }
  sprintf(distanceReturn, "%f;%f;%f", distanceToObject[0], distanceToObject[1], distanceToObject[2]);
  //sei();// Enable interrupts again, we're all done with calculations
  interrupts();
  // Trigger Sensors (no need to have the initial LOW because this function already takes up time
  digitalWrite(triggerPin, HIGH);
  delayMicroseconds(10); // 10 us is enough!
  digitalWrite(triggerPin, LOW);    // End of pulse
}

//
// Interrupt routines, one for each sensor echo pin
//
void ISR_Sensor1_onPin2_Interrupt0()
{
  byte readEchoPin = digitalRead(leftEchoPin); //  leftEchoPin = 2;
  mainInterruptFunction(readEchoPin, 0);
}

void ISR_Sensor2_onPin7_Interrupt1()
{
  byte readEchoPin = digitalRead(forwardEchoPin); // forwardEchoPin = 7;
  mainInterruptFunction(readEchoPin, 1);
}

void ISR_Sensor3_onPin8_Interrupt2()
{
  byte readEchoPin = digitalRead(rightEchoPin); //rightEchoPin = 8;
  mainInterruptFunction(readEchoPin, 2);
}



// Main Interrupt function
void mainInterruptFunction(bool stateOfPin, int IRQ_Number)
{
  unsigned long currentTime = micros(); 
  if (stateOfPin)
  {
    // If the pin measured had its state change to HIGH, then save that time (us)!
    timeEchostateOfPinChangedToHigh[IRQ_Number] = currentTime;
  }
  else
  {
    // If the pin measured had its state change to LOW, then calculate how much time has passed (µs)
    pulseTravelTime[IRQ_Number] = currentTime - timeEchostateOfPinChangedToHigh[IRQ_Number];
  }
}
