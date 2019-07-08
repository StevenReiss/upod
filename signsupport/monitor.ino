const int size = 300;
const int delta = 10;
byte move[size];
byte phone[size];
int ctr;
int turn;
int moctr;
int phctr;
const int MOTION_PIN = 3;
const int PHONE_PIN = 0;
const int PHONE_HIGH_THRESHOLD = 400;
const int PHONE_LOW_THRESHOLD = 240;
int outs [] = { 
  1,3,6,12,24,30,60,size };

void setup()
{
  for (int i = 0; i < size; ++i) {
    move[i] = 0;
    phone[i] = 0;
  }
  ctr = 0;
  turn = 0;
  moctr = 0;
  phctr = 0;
  Serial.begin(9600);
  pinMode(13,OUTPUT);
  pinMode(12,OUTPUT);
  pinMode(MOTION_PIN,INPUT);
}


void loop()
{
  unsigned long now = millis();

  // every second
  int sts = digitalRead(MOTION_PIN);
  digitalWrite(12,sts);
  if (sts == HIGH) ++moctr;
  int psts = analogRead(PHONE_PIN);
  // Serial.println(psts);
  if (psts > PHONE_HIGH_THRESHOLD || psts < PHONE_LOW_THRESHOLD) { 
    digitalWrite(13,HIGH);
    ++phctr;
  }
  else digitalWrite(13,LOW);

  // every delta seconds
  if ((turn % delta) == delta-1) {
    if (moctr > 0) move[ctr] = 1;
    else move[ctr] = 0;
    if (phctr > 5) phone[ctr] = 1;
    else phone[ctr] = 0;
    ctr = (ctr + 1) % size;
    moctr = 0;
    phctr = 0;
  }

  // every 2 seconds
  if ((turn % 2) == 0) {
    int report = 0;
    if (Serial.available() > 0) {
      char buf[128];
      int n = Serial.readBytesUntil('\n',buf,127);
      // Serial.print("READ "); Serial.print(Serial.available()); 
      // Serial.print(" "); Serial.println(n);
      if (n > 0) report = 1;
    }
    if (report > 0) {
      int totm = 0;
      int totp = 0;
      int outctr = 0;
      Serial.print("GO ");
      Serial.print(delta);
      Serial.print(" "); 
      Serial.print(turn);
      Serial.print("\t");
      for (int i = 0; i < size; ++i) {
        int j = (ctr + size - i - 1) % size;
        totm += move[j];
        totp += phone[j];
        if (i+1 == outs[outctr]) {
          Serial.print((i+1));
          Serial.print(" ");
          Serial.print(totm);
          Serial.print(" ");
          Serial.print(totp);
          Serial.print("\t");
          ++outctr;
        } 
      }
      Serial.println();
    }
  }

  unsigned long end = millis();
  long delta = (end - now);
  if (delta < 0) delta = 0;
  delta = 1000 - delta;
  if (delta > 2) delay(delta-1);

  turn = (turn + 1) % 120;
}


