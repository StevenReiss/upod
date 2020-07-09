#include <SPI.h>
#include "Adafruit_WS2801.h"
#include "WiFly.h"
#include "Credentials.h"
#include <util.h>"
#include <Ethernet.h>
WiFlyClient  client(_hostname, 8080);
union serial_super_data {
   unsigned long int_4_bytes;
   unsigned char read_byte[4];
} super_data;

// DEVICE-SPECIFIC
int lampState = 0;
const int lampPin = 8;
int dataPin  = 2;    // Yellow wire on Adafruit Pixels
int clockPin = 3;    // Green wire on Adafruit Pixels
Adafruit_WS2801 strip = Adafruit_WS2801(25, dataPin, clockPin);
// END DEVICE-SPECIFIC

void setup() 
{
  Serial.begin(9600);
  devicespecificsetup();
  while(!connectandauthenticate())
  {
    Serial.println("Retrying connect and auth process");
  }
  devicespecifichappydance(); 
}

boolean connectandauthenticate()
{
  WiFly.begin();  
  if(!WiFly.join(ssid,passphrase))
//  if(!WiFly.join(ssid)) 
  {
    for (int conattempt=1;conattempt<=10;conattempt++)
    {
      delay(500);
      Serial.print("Connecting, Try: ");   // Hang on failure.
      Serial.println(conattempt);
      if(!WiFly.join(ssid,passphrase))
//      if(!WiFly.join(ssid)) 
      {
        break;
      }
    }
  }
  Serial.print("IP: ");
  Serial.println(WiFly.ip());
  if(client.connect()) 
  {
   Serial.println("Connected to UpodInside Port 8080");
   String authmessage = "self:auth:mac|";
   authmessage+=mac;   
   if (client.connected())
   { 
      Serial.print("Sending Auth Message: ");
      Serial.println(authmessage);
      Serial.println("=====BEG HANDSHAKE=====");
      sendr(authmessage);
      int maxAttempts = 300, attempts = 0;
      while(client.available() <= 4)
      {
          delay(10);
          attempts++;
          if(attempts>=maxAttempts)
          {
            return false;
          }
      }
      char incomingByte;
      Serial.print("Incoming handshake bytes: ");
      for(int j=0;j <4; j++)
      {
        super_data.read_byte[3-j]=client.read();
        Serial.print(super_data.read_byte[3-j]);
        Serial.print(" ");
      }
      Serial.println(" "); // finish last
      Serial.print("Handshake message Length: ");
      Serial.println(super_data.int_4_bytes);
      if(super_data.int_4_bytes>100)
      {
          while(client.available())
          {
            client.read();
          }
          client.stop();
          return false;
      }
      String hsline = ""; 
      for (int i = 0; i < super_data.int_4_bytes;i++)
      {
         hsline+=char(client.read());
         delay(10);
      }
      Serial.print("Handshake message Received: ");
      Serial.println(hsline);
      boolean handshakeok = false;
      String mtype = messagetype(&hsline);
      String contents = message(&hsline);
      Serial.print("Handshake message type: ");
      Serial.println(mtype);
      Serial.print("Handshake message contents: ");
      Serial.println(contents);
      delay(10);
      if(contents=="ok")
      { 
        Serial.println("Handshake accepted");
        Serial.println("=====END HANDSHAKE=====");
        return true;
      }
      else
      {
        Serial.println("Handshake NOT accepted");
        Serial.println("=====END HANDSHAKE=====");
      }
   }
  }
  return false;  
}

void loop()
{
  if (!(client.connected())) // reconnect
  { 
      connectandauthenticate();
  }
  if (client.available() >= 4) 
  {
        Serial.println("+++++NEW MESSAGE+++++");
        Serial.print("Incoming bytes: ");
        for(int j=0;j<4; j++)
        {  
          super_data.read_byte[3-j]=client.read();
          Serial.print(super_data.read_byte[3-j]);
          Serial.print(" ");
        }
        Serial.println(" "); // this ends the last group
        Serial.print("Message Length: ");
        Serial.println(super_data.int_4_bytes);
        String msgline = "";
        if(super_data.int_4_bytes<1000)
        {
          for (int i = 0; i < super_data.int_4_bytes;i++)
          {
             msgline+=char(client.read());
             delay(10);
          }
          Serial.print("Message Received: ");
          Serial.println(msgline);
          if(messagetype(&msgline)=="cmd")
          {
            devicespecificaction(message(&msgline), messagename(&msgline));
          }
          //ZZZ DO STH FOR STATUS
        }
        else
        {
            while(client.available())
            { // something is wrong. empty buffer and reconnect in next iteration
              client.read();
            }
            client.stop();
        }
    }
}

String messagetype(String* givenmessage)
{
  String answer = "junk";
  // see if we find exactly two colons
  int index1 = (*givenmessage).indexOf(':');
  if(index1>0)
  {
    int index2 = (*givenmessage).indexOf(':',index1+1);
    if(index2>index1)
    {
       int index3 = (*givenmessage).indexOf(':',index2+1);
       if(index3==-1)
       {
         answer = (*givenmessage).substring(index1+1,index2);
       }
    }
  }
  return answer;
}

String messagename(String* givenmessage)
{
  String answer = "junk";
  // see if we find exactly two colons
  int index1 = (*givenmessage).indexOf(':');
  if(index1>0)
  {
    int index2 = (*givenmessage).indexOf(':',index1+1);
    if(index2>index1)
    {
       int index3 = (*givenmessage).indexOf(':',index2+1);
       if(index3==-1)
       {
         answer = (*givenmessage).substring(0,index1);
       }
    }
  }
  return answer;
}

String message(String* givenmessage)
{  // see if we find exactly two colons
  String answer = "junk";
  int index1 = (*givenmessage).indexOf(':');
  if(index1>0)
  {
    int index2 = (*givenmessage).indexOf(':',index1+1);
    if(index2>index1)
    {
       int index3 = (*givenmessage).indexOf(':',index2+1);
       if(index3==-1)
       {
         answer = (*givenmessage).substring(index2+1);
       }
    }
  }
  return answer;
}

void sendr (String data) 
{
    unsigned long len = data.length();
    sendBinary(htonl(len));
    client.print(data);
    delay(100); // Give some time for response
}
void sendBinary(unsigned int value)
{  // send the two bytes that comprise a two byte (16 bit) integer (Big Endian)
  client.write(lowByte(value));  // send the low byte
  client.write(highByte(value)); // send the high byte
}
void sendBinary (unsigned long value)
{  // first send the low 16 bit integer value, then send the higher 16 bit integer value:
  unsigned int temp = value & 0xFFFF;  // get the value of the lower 16 bits
  sendBinary(temp);
  temp = value >> 16;  // get the value of the higher 16 bits
  sendBinary(temp);
}


// DEVICE-SPECIFIC FUNCTIONS
void rainbow(uint8_t wait) 
{
  int i, j;
  for (j=0; j < 256; j++) 
  {     // 3 cycles of all 256 colors in the wheel
    for (i=0; i < strip.numPixels(); i++) {
      strip.setPixelColor(i, Wheel( (i + j) % 255));
    }  
    strip.show();   // write all the pixels out
    delay(wait);
  }
}

void rainbowCycle(uint8_t wait) 
{
  int i, j;  
  for (j=0; j < 256 * 5; j++) {     // 5 cycles of all 25 colors in the wheel
    for (i=0; i < strip.numPixels(); i++) 
    {
      strip.setPixelColor(i, Wheel( ((i * 256 / strip.numPixels()) + j) % 256) );
    }  
    strip.show();   // write all the pixels out
    delay(wait);
  }
}

void colorWipe(uint32_t c, uint8_t wait) 
{
  int i;  
  for (i=0; i < strip.numPixels(); i++) 
  {
      strip.setPixelColor(i, c);
      strip.show();
      delay(wait);
  }
}

uint32_t Color(byte r, byte g, byte b)
{
  uint32_t c;
  c = r;
  c <<= 8;
  c |= g;
  c <<= 8;
  c |= b;
  return c;
}

uint32_t Wheel(byte WheelPos)
{
  if (WheelPos < 85) {
   return Color(WheelPos * 3, 255 - WheelPos * 3, 0);
  } else if (WheelPos < 170) {
   WheelPos -= 85;
   return Color(255 - WheelPos * 3, 0, WheelPos * 3);
  } else {
   WheelPos -= 170; 
   return Color(0, WheelPos * 3, 255 - WheelPos * 3);
  }
}

void devicespecificsetup()
{
  strip.begin();
  for(int by=0; by<strip.numPixels(); by++)
  {
    strip.setPixelColor(by, Color(0,0,0));
  }
  strip.show();
  pinMode(lampPin, OUTPUT);  
}

void devicespecifichappydance()
{
    colorWipe(Color(255, 0, 0), 50);
    delay(1000);
    for(int bz=0; bz<strip.numPixels(); bz++)
    {
      strip.setPixelColor(bz, Color(0,0,0));
    }
    strip.show();  
}

void devicespecificaction(String currmsg, String requester)
{
       String newstate = requester; // for sending state to requester
       newstate+=":state:";
       newstate+=device_name;
       int mono=currmsg.indexOf("mono");
       Serial.print("Location of mono");
       Serial.println(mono);
       int pixe=currmsg.indexOf("pixe");       
       Serial.print("Location of pixe");
       Serial.println(pixe);
       if(currmsg=="on")
       {
           for(int i=0; i < strip.numPixels(); i++) 
           {
             strip.setPixelColor(i, Color(255,255,255));
           }  
           strip.show();
           delay(50);
           lampState = 1;
           newstate+="|on";
           sendr(newstate);
       }
       else if(currmsg=="off")
       {
           for (int i=0; i < strip.numPixels(); i++) 
           {
             strip.setPixelColor(i, Color(0,0,0));
           }  
           strip.show();
           delay(50);
           lampState = 0;
           newstate+="|off";
           sendr(newstate);
       }
       else if (mono==0) 
       {
           String remaining = currmsg.substring(5); // cut off "mono "
           Serial.print("Mono: ");
           Serial.println(remaining);
           int index1 = remaining.indexOf(' ');
           if(index1>0)
           {
              int index2 = remaining.indexOf(' ',index1+1);
              if(index2>index1)
              {
                 int index3 = remaining.indexOf(' ',index2+1);
                 if(index3<0)
                 {
                   String mono1=remaining.substring(0,index1);
                   String mono2=remaining.substring(index1+1,index2);
                   String mono3=remaining.substring(index2+1);
                   int m1 = mono1.toInt();
                   if(m1>255)
                   {
                     m1=255;
                   }
                   else if(m1<0)
                   {
                     m1=0;
                   }
                   int m2 = mono2.toInt();
                   if(m2>255)
                   {
                     m2=255;
                   }
                   else if(m2<0)
                   {
                     m2=0;
                   }
                   int m3 = mono3.toInt();
                   if(m3>255)
                   {
                     m3=255;
                   }
                   else if(m3<0)
                   {
                     m3=0;
                   }
                   for (int i=0; i < strip.numPixels(); i++) 
                   {
                     strip.setPixelColor(i, Color(m1,m2,m3));
                   }
                   strip.show();   // write all the pixels out
                   delay(50);
                   lampState=1;
                   newstate+="|on";
                   sendr(newstate);
                 }
              }
           }
       }
       else if (pixe==0) 
       {
           String remaining = currmsg.substring(5); // cut off "pixe "
           Serial.print("Pixe: ");
           Serial.println(remaining);
           int index1 = remaining.indexOf(' ');
           if(index1>0)
           {
              int index2 = remaining.indexOf(' ',index1+1);
              if(index2>index1)
              {
                 int index3 = remaining.indexOf(' ',index2+1);
                 if(index3>0)
                 {
                   int index4 = remaining.indexOf(' ',index3+1);
                   if(index4<0)
                   {
                     String mono0=remaining.substring(0,index1);
                     String mono1=remaining.substring(index1+1,index2);
                     String mono2=remaining.substring(index2+1,index3);
                     String mono3=remaining.substring(index3+1);
                     int m0 = mono0.toInt();
                     if(m0>=strip.numPixels())
                     {
                       m0=strip.numPixels()-1;
                     }
                     else if(m0<0)
                     {
                       m0=0;
                     }
                     int m1 = mono1.toInt();
                     if(m1>255)
                     {
                       m1=255;
                     }
                     else if(m1<0)
                     {
                       m1=0;
                     }
                     int m2 = mono2.toInt();
                     if(m2>255)
                     {
                       m2=255;
                     }
                     else if(m2<0)
                     {
                       m2=0;
                     }
                     int m3 = mono3.toInt();
                     if(m3>255)
                     {
                       m3=255;
                     }
                     else if(m3<0)
                     {
                       m3=0;
                     }
                     strip.setPixelColor(m0, Color(m1,m2,m3));
                     strip.show();   // write all the pixels out
                     delay(50);
                     lampState=1;
                     newstate+="|on";
                     sendr(newstate);
                   }
                 }
              }
           }
       }
}
