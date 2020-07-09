#include <SPI.h>
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

void devicespecificsetup()
{
  pinMode(lampPin, OUTPUT);  
}

void devicespecifichappydance()
{
    digitalWrite(lampPin,HIGH);
    delay(1000);
    digitalWrite(lampPin,LOW);
}

void devicespecificaction(String currmsg, String requester)
{
       String newstate = requester; // for sending state to requester
       newstate+=":state:";
       newstate+=device_name;
       if(currmsg=="on")
       {
           digitalWrite(lampPin,HIGH);
           lampState=1;
           newstate+="|on";
           sendr(newstate);
           delay(50);
       }
       else if(currmsg=="off")
       {
           digitalWrite(lampPin,LOW);
           lampState=0;
           newstate+="|off";
           sendr(newstate);
           delay(50);
       }
}
