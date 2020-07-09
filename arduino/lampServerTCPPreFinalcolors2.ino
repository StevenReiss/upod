#include <SPI.h>
#include "Adafruit_WS2801.h"
#include "WiFly.h"
#include "Credentials.h"
#include <util.h>"
#include <Ethernet.h>

WiFlyClient  _client(_hostname, 8080);

// Constant used to read first 4 bytes.
union serial_super_data {
   unsigned long int_4_bytes;
   unsigned char read_byte[4];
} super_data;


char charr;
char x;
char character;
String line = ""; 
int lampState = 0;
const int lampPin = 8;

char _type[20];
char _data[40];
char lines[80];
//int _numtypes;
bool debug = false; // Prints to extended Debug  to erial
char* _types[]={"update\0", "state\0", "cmd\0", "sys\0", "client\0", "auth\0"};

String mclient;
String mtype;
String mdata;

int dataPin  = 2;    // Yellow wire on Adafruit Pixels
int clockPin = 3;    // Green wire on Adafruit Pixels
// Don't forget to connect the ground wire to Arduino ground,
// and the +5V wire to a +5V supply
// Set the first variable to the NUMBER of pixels. 25 = 25 pixels in a row
Adafruit_WS2801 strip = Adafruit_WS2801(25, dataPin, clockPin);

void setup() 
{
  strip.begin();
  // Update LED contents, to start they are all 'off'
  strip.show();

  Serial.begin(9600);
  pinMode(lampPin, OUTPUT);
  WiFly.begin();  
  if(!WiFly.join(ssid,passphrase))
//  if(!WiFly.join(ssid)) 
  {
    for (int flag = 1;flag <= 10;flag++)
    {
      delay(500);
      Serial.print("Connecting, Try: ");   // Hang on failure.
      Serial.println(flag);
      if(!WiFly.join(ssid,passphrase))
//      if(!WiFly.join(ssid)) 
      {
        break;
      }
    }
  }
  else
  {
    //Serial.println("Wifly Connected to Network");
  }
  
  Serial.print("IP: ");
  Serial.println(WiFly.ip());

  
  if (_client.connect()) {
    //Serial.println("Connected to UpodInside Port 8080");
  
   String message = auth_request(device_name,mac);

   if (_client.connected())
   { 
     if (debug)
     {
       Serial.print("Sending Auth Message: ");
       Serial.println(message);
     }
     Serial.println("=====BEG HANDSHAKE=====");
     sendr(message);
     //Serial.println("Message Sent");
    // Serial.print("freeMemory()=");
     //Serial.println(freeMemory());
     boolean bleh = readHandshake();
       Serial.println("here");
     Serial.println(bleh);
     if (bleh)
     {
       Serial.println("there");
        colorWipe(Color(255, 0, 0), 50);
        delay(1000);
        for(int bz=0; bz<25; bz++)
        {
          strip.setPixelColor(bz, Color(0,0,0));
        }
        strip.show();
        //Serial.println("Authenticated");
     }    
   }
   
  } 

  
}

void loop() 
{
  //Serial.println("Loop");
  if (_client.available() >= 4) {
        /*Serial.println("+++++BEG PROCESS+++++");
        Serial.println("++++++++++++++++++++++");
        Serial.print("Connection Status: ");
        Serial.println(_client.connected());
         byte incomingByte = _client.read();
         Serial.print("Byte1: ");
         Serial.println(incomingByte);
         incomingByte = _client.read();
         Serial.print("Byte2: ");
         Serial.println(incomingByte);
         incomingByte = _client.read();
         Serial.print("Byte3: ");
         Serial.println(incomingByte);
         incomingByte = _client.read();
         Serial.print("Byte4: ");
         Serial.println(incomingByte);*/
        
         // Big endian
        for(int j=0;j <4; j++) super_data.read_byte[3-j]=_client.read();
         
        Serial.print("Message Length: ");
        Serial.println(super_data.int_4_bytes);
        line = "";
        for (int i = 0; i < super_data.int_4_bytes;i++)
        {
         character = _client.read();
         delay(10);
         line =line+ character;
        }
        Serial.print("Message Received: ");
        Serial.println(line);
        process_message(line);
    }
 
  //monitor();
  //Serial.println(connected());
}

bool connected() {
    return _client.connected();
}

void disconnect() {
    _client.stop();
}

void process_message(String message)
{
  
  line.toCharArray(lines,80);

  /*if (debug)
  {
    Serial.println(lines);
    Serial.println("Checking if is message");
  }*/
  if (is_message(lines))
  {
    /*if (debug)
     {
        Serial.println("Is Message");
     }*/
    //String temp;// = String(get_type(lines));
    
    //String _type = "cmd";
    strncpy(_type,get_type(lines),20);
    if (strcmp(_type,"cmd\0")==0)
    {
      //temp = String(get_data(lines));
       //String _on = "on";
       //String _off = "off";
       strncpy(_data,get_data(lines),40);
       String thing = _data;
       if (strcmp(_data,"on\0'")==0)
       {
           for (int i=0; i < strip.numPixels(); i++) 
           {
             strip.setPixelColor(i, Color(255,255,255));
           }  
           strip.show();   // write all the pixels out
           delay(500);
           lampState = 1;
           sendr(state(get_name(lines),"state",device_name,"on"));
       }
       else if (strcmp(_data,"off\0'")==0) 
       {
           for (int i=0; i < strip.numPixels(); i++) 
           {
             strip.setPixelColor(i, Color(0,0,0));
           }  
           strip.show();   // write all the pixels out
         delay(500);
         lampState = 0;
         sendr(state(get_name(lines),"state",device_name,"off"));
       }
       else if (thing.indexOf("mono") >= 0) 
       {
           int ff;
           int fg;
           int fh;
           if(thing.charAt(5)=='1')
           {
              ff = 1;
           }
           else
           {
              ff = 0; 
           }
           if(thing.charAt(7)=='1')
           {
              fg = 1;
           }
           else
           {
              fg = 0; 
           }           
           if(thing.charAt(9)=='1')
           {
              fh = 1;
           }
           else
           {
              fh = 0; 
           }
           for (int i=0; i < strip.numPixels(); i++) 
           {
             strip.setPixelColor(i, Color(255*ff,255*fg,255*fh));
           }  
           strip.show();   // write all the pixels out
          delay(500);
          lampState = 0;
       }
       else if (thing.indexOf("pixe") >= 0) 
       {
           int bb = int(thing.charAt(5))-48;
           int bc = int(thing.charAt(6))-48;
           int bd = 10*bb + bc - 10;
           int be = int(thing.charAt(8))-48;
           if(be==0)
           {
             strip.setPixelColor(bd, Color(0,0,0));             
           }
           else if(be==1)
           {
             strip.setPixelColor(bd, Color(0,0,255));             
           }
           else if(be==2)
           {
             strip.setPixelColor(bd, Color(0,255,0));
           }           
           else if(be==3)
           {
             strip.setPixelColor(bd, Color(0,255,255));
           }
           else if(be==4)
           {
             strip.setPixelColor(bd, Color(255,0,0));
           }               
           else if(be==5)
           {
             strip.setPixelColor(bd, Color(255,0,255));
           }    
           else if(be==6)
           {
             strip.setPixelColor(bd, Color(255,255,0));
           }    
           else if(be==7)
           {
             strip.setPixelColor(bd, Color(255,255,255));
           }    
           strip.show();   // write all the pixels out
          delay(100);
          lampState = 0;
       }
     }
      else if (strcmp(_data,"status\0'")==0)
      {
        if (lampState)
        {
          sendr(state(get_name(lines),"state",device_name,"on"));
        }
        else
        {
          sendr(state(get_name(lines),"state",device_name,"off"));
        }
      }
  }

   //Serial.print("freeMemory()=");
   //Serial.println(freeMemory());
   Serial.println("+++++END PROCESS+++++");
}



bool readHandshake() {
    int maxAttempts = 300, attempts = 0;

    while(_client.available() <= 4 && attempts < maxAttempts)
    {
        delay(100);
        attempts++;
    }

    //for(int j=0;j <4; j++) super_data.read_byte[3-j]=_client.read();
    
    char incomingByte;
  for(int j=0;j <4; j++)
  {
    /*Serial.print("Byte: ");
    Serial.println(incomingByte);
    incomingByte = _client.read();*/
    super_data.read_byte[3-j]=_client.read();
  }

    
    Serial.print("Message Length: ");
    Serial.println(super_data.int_4_bytes);

    line = ""; 
    //for (int i = 0; i < 30;i++)
    for (int i = 0; i < super_data.int_4_bytes;i++)
    {
       character = _client.read();
         delay(10);
         line =line+ character;
    }
    Serial.print("Message Received: ");
    line.toCharArray(lines,80);
    Serial.println(lines);
    bool flag = false;
    
    //Serial.println("Checking if is message");
    if (is_message(lines))
    {
      Serial.println("Is formatted message");
      //String temp; //=get_type(lines);
      //Serial.println(lines);
      //String _type = "auth";
      //if (temp == _type)
      strncpy(_type,get_type(lines),20);

      //String _type = "cmd";
      if (strcmp(_type,"auth\0")==0)
      //if (String_type)==String(auth))
      {
        //temp = String(get_data(lines));
         //String _data = "ok";
         //if (temp == _data)
         strncpy(_data,get_data(lines),20);

          //String _type = "cmd";
          if (strcmp(_data,"ok\0")==0)
         {
               flag = true;
         }
         else
         {
           Serial.print("Not OK, Data: ");
           Serial.println(_data);
         }
       }
       else
       {
         
         Serial.print("Not Auth Type: ");
         Serial.print(_type);
         //Serial.println(String(_type)==String(auth));
       }
    }
    else
    {
       Serial.println("not a message"); 
    }

    delay(10);
    //Serial.print("freeMemory()=");
    //Serial.println(freeMemory());
    Serial.println("=====END HANDSHAKE=====");
    return flag;
    
 
}

void sendr (String data) {
    unsigned long len = data.length();
     //int ilen = atoi(len);
    //_client.print(len);
    sendBinary(htonl(len));
    _client.print(data);
    delay(100); // Give some time for response
    //sendBinary(len);
}

void sendBinary(unsigned int value)
{
  // send the two bytes that comprise a two byte (16 bit) integer
  //Serial.print(lowByte(value));  // send the low byte
  //Serial.println(highByte(value)); // send the high byte
  
  // Big Endian
  
  _client.write(lowByte(value));  // send the low byte
  _client.write(highByte(value)); // send the high byte
 


}
void sendBinary (unsigned long value)
{

  // first send the low 16 bit integer value
  //Serial.println (value,HEX);
  unsigned int temp = value & 0xFFFF;  // get the value of the lower 16 bits
  //Serial.println (temp,HEX);
  sendBinary(temp);

  // then send the higher 16 bit integer value:

  temp = value >> 16;  // get the value of the higher 16 bits
  //Serial.println (temp,HEX);
  sendBinary(temp);

}

char* get_type(const char* msg)
{
    char* message;
    strcpy (message,msg);
    
    char* type;
    char *str;
    int length = 1;
    type = strtok_r(message, ":", &message);
    type = strtok_r(message, ":", &message);
    return type;
}

bool is_message (const char* message)
{
    /*""" 
    Checks if message is a valid message. Returns True if valid, 
    else returns False.
    */
    Serial.println(message);
    bool flag = false;
    int lenParts = num_parts(message);
    Serial.println(lenParts);
    if (lenParts == 3)
    {        
	char* tempType = get_type(message);
    Serial.println(tempType);
	if (is_type(tempType))
	{
	    flag = true;
	}
	else
	{
	    flag = false;
	}
    }
    else
    {
	flag = false;
    }
    return flag;
}

/*char* get_type(String msg)
{
    char message[40];
    msg.toCharArray(message,40);
    
    /*char* type;
    char *str;
    int length = 1;
    type = strtok_r(message, ":", &message);
    type = strtok_r(message, ":", &message);*/
    /*return get_type(message);
}*/

char* get_data(const char* msg)
{
    char* message;
    strcpy (message,msg);
    
    char* data;
    char *str;
    int length = 1;
    data = strtok_r(message, ":", &message);
    data = strtok_r(message, ":", &message);
    //data = strtok_r(message, ":", &message);
    return message;
}

/*char* get_data(String msg)
{
    char message[40];
    msg.toCharArray(message,40);
    
   /*char* type;
    char *str;
    int length = 1;
    type = strtok_r(message, ":", &message);
    type = strtok_r(message, ":", &message);*/
    /*return get_data(message);
}*/

char* get_name(const char* msg)
{
  char* message;
  strcpy (message,msg);

  char* name;
  char *str;
  int length = 1;
  name = strtok_r(message, ":", &message);
  return name;
}

bool is_type(const char* type)
{
  /*String stringOne, stringTwo;
  stringOne = String(type);*/
  bool flag = false;
  char* _types[]={"update", "state", "cmd", "sys", "auth", "client"};
  for (int i=0;i < 6;i++)
  {

    //stringTwo = String(_types[i]);
      Serial.print(i);
      Serial.print(_types[i]);
      Serial.print(" ");
      Serial.println(type);
    if (strcmp(_types[i],type)  == 0)
    {
      flag = true;
    }
  }
  return flag;
}

int num_parts(const char* msg)
{
    char* message;
    strcpy (message,msg);
    
    int length = 0;
    char *str;
    while ((str = strtok_r(message, ":", &message)) != NULL)
    {
        //s.println(str);
	length++;
    }
    
    return length;
}

/*bool is_type(const char* type)
{
  //String stringOne, stringTwo;
  //stringOne = String(type);
  bool flag = false;
  
  Serial.print("freeMemory()=");
  Serial.println(freeMemory());
  int k= 9;
  for (int i=0;i < 6;i++)
  {
    //stringTwo = String(_types[i]);
    k = strcmp(_types[i],type);
    Serial.print("Type check: ");
    Serial.print(k);
    Serial.print(" for: ");
    Serial.println(_types[i]);
    if (k  == 0)
    {
      flag = true;
    }

  }
  return flag;
}*/

/*bool is_type(String type)
{
    String stringTwo;
    bool flag = false;
    char* _types[]={"update", "state", "cmd", "sys", "client", "auth"};
    Serial.print("freeMemory()=");
  Serial.println(freeMemory());
    for (int i=0;i < 6;i++)
    {
        
	stringTwo = String(_types[i]);

	if (type == stringTwo)
	{
	    flag = true;
	}
	
    }
    return flag;
}*/


String pack(String client,String type,String data)
{
                
        //Packs a message according to data and returns a single string representing
        //that message        
        
        String final;
        final = client +  ":" +  type + ":" + data; 
        return final;
}

char* pack(const char* client,const char* type,const char* data)
{

  //Packs a message according to data and returns a single string representing
  //that message        

  //String final;
  //final = String(client) +  ":" +   String(type) + ":" + String(data);
  sprintf(lines,"%s:%s:%s",client,type,data); 
  return lines;
}


String state(const char* client, const char* type, const char* device, const char* state)
{
        /*        
        Creates a state message according to spec. client is the sender or receiver of 
        the message (depending on the kind of message), and device is the client who's 
        state is being updated. Returns the message as a string.       
        */
        String dev = String(device);
        dev = dev + "|" + String (state);
        return pack(String(client), String(type), dev);
}

String state(String client, String type, String device, String state)
{
        /*        
        Creates a state message according to spec. client is the sender or receiver of 
        the message (depending on the kind of message), and device is the client who's 
        state is being updated. Returns the message as a string.       
        */
        String dev = String(device);
        dev = dev + "|" + String (state);
        return pack(client, type, dev);
}

String  auth_request(const char* name,const char* mac_ad)
{
        
        //Creates and returns an auth request message as a string. name is the client name
        //that the remote client would like to register with. 
        
        //String macc = "mac|";
   
        //macc = macc+ ;
       
        mclient = "self";
        mtype = "auth";
        mdata ="";
        mdata = "mac|";
        mdata = mdata+ mac_ad;
        return pack(mclient,mtype, mdata);
}

void rainbow(uint8_t wait) {
  int i, j;
   
  for (j=0; j < 256; j++) {     // 3 cycles of all 256 colors in the wheel
    for (i=0; i < strip.numPixels(); i++) {
      strip.setPixelColor(i, Wheel( (i + j) % 255));
    }  
    strip.show();   // write all the pixels out
    delay(wait);
  }
}

// Slightly different, this one makes the rainbow wheel equally distributed 
// along the chain
void rainbowCycle(uint8_t wait) {
  int i, j;
  
  for (j=0; j < 256 * 5; j++) {     // 5 cycles of all 25 colors in the wheel
    for (i=0; i < strip.numPixels(); i++) {
      // tricky math! we use each pixel as a fraction of the full 96-color wheel
      // (thats the i / strip.numPixels() part)
      // Then add in j which makes the colors go around per pixel
      // the % 96 is to make the wheel cycle around
      strip.setPixelColor(i, Wheel( ((i * 256 / strip.numPixels()) + j) % 256) );
    }  
    strip.show();   // write all the pixels out
    delay(wait);
  }
}

// fill the dots one after the other with said color
// good for testing purposes
void colorWipe(uint32_t c, uint8_t wait) {
  int i;
  
  for (i=0; i < strip.numPixels(); i++) {
      strip.setPixelColor(i, c);
      strip.show();
      delay(wait);
  }
}

/* Helper functions */

// Create a 24 bit color value from R,G,B
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

//Input a value 0 to 255 to get a color value.
//The colours are a transition r - g -b - back to r
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
