#include <Wire.h> //I2C Arduino Library
#include <SoftwareSerial.h> 
#include "U8glib.h"

int bluetoothTx = 5;
int bluetoothRx = 6;

#define HMC5883_address 0x0D 
U8GLIB_SSD1306_128X64 u8g(U8G_I2C_OPT_NONE);  // I2C / TWI 
SoftwareSerial bluetooth(bluetoothTx, bluetoothRx);
const int oled_rst = 8;
int angle;

void setup() {

pinMode(oled_rst, OUTPUT); 
digitalWrite(oled_rst, LOW);
delay(100);
digitalWrite(oled_rst, HIGH);

drawIntro();
delay(2000);

//Initialize Serial and I2C communications
Serial.begin(9600);
bluetooth.begin(9600);
Wire.begin();


Wire.beginTransmission(HMC5883_address); //open communication with HMC5883
Wire.write(0x0B); //select mode register
Wire.write(0x01); //continuous measurement mode
Wire.endTransmission();
}

void loop() {

int x, y, z; //triple axis data

//Tell the HMC5883L where to begin reading data
Wire.beginTransmission(HMC5883_address);
Wire.write(0x00); //select register 3, X MSB register
Wire.endTransmission();
//Read data from each axis, 2 registers per axis
Wire.requestFrom(HMC5883_address, 6);
if (6 <= Wire.available()) {
x = Wire.read() << 8; //X msb
x |= Wire.read(); //X lsb
//z = Wire.read() << 8; //Z msb
//z |= Wire.read(); //Z lsb
y = Wire.read() << 8; //Y msb
y |= Wire.read(); //Y lsb
}



angle = atan2(-(double) y, -(double) x) * (180 / 3.14159265) + 180;
angle -= (8 + 4 / 60); // magnetic declination in HK= -2 44′
if (angle < 0) angle += 360;
int angle_freq = angle;
if (angle > 180) angle_freq = 360 - angle;
int freq = map(angle_freq, 180, 0, 1000, 2000);
if (angle_freq > 2)
tone(3, freq);
else{
noTone(3);
}
drawCompass();
//if(bluetooth.available())  drawCompass();
//else{ 
//      u8g.setFont(u8g_font_fub14);
//      u8g.setPrintPos(10, 25);
//      u8g.print("Waiting");}
//  }
  //Read from usb serial to bluetooth
  if(Serial.available())
  {
    String inString = bluetooth.readStringUntil('.');
    bluetooth.print(inString);
  }
delay(200);
}

void drawCompass() {
String inString = bluetooth.readStringUntil('/');
Serial.print(inString);

float angle_rad = 3.14159265 * (float) angle / 180.0;
//char angle_array [] = "    "; // better to start with an empty string to avoid garbage

int x0 = 40 + cos(angle_rad) * 25; // tip of the arrow on circle
int y0 = 30 - sin(angle_rad) * 25;
int x1 = 40 + cos(angle_rad + 0.2) * 15; // triangle point
int y1 = 30 - sin(angle_rad + 0.2) * 15;
int x2 = 40 + cos(angle_rad - 0.2) * 15; // triangle point
int y2 = 30 - sin(angle_rad - 0.2) * 15;
u8g.firstPage(); // draw compass
do {
if(bluetooth.available()){
u8g.setFont(u8g_font_fub14);
u8g.drawDisc(40, 30, 2);
u8g.drawLine(40, 30, x0, y0);
u8g.drawTriangle(x0, y0, x1, y1, x2, y2);
u8g.setFont(u8g_font_fub14);
u8g.setPrintPos(70,40);
u8g.println(inString);
}
else{
u8g.setPrintPos(20, 40);
u8g.print("Waiting");}
} while ( u8g.nextPage() );
}


void drawIntro() {
u8g.firstPage(); // draw intro
do {
u8g.setFont(u8g_font_fub14);
u8g.setPrintPos(10, 25);
u8g.print("AP-MAN-BA");
u8g.setPrintPos(2, 60);
u8g.print("PROJECT");
} while ( u8g.nextPage() );
}

