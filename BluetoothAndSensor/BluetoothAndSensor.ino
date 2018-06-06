#define ledPin13 13
#define ledPin10 10
int state = 0;
String input;
char charInput;

void setup() {
  // put your setup code here, to run once:
  pinMode(ledPin13, OUTPUT);
  pinMode(ledPin10, OUTPUT);
  digitalWrite(ledPin13, LOW);
  digitalWrite(ledPin10, LOW);
  Serial.begin(9600);
}

void loop() {
  // put your main code here, to run repeatedly:
  if(Serial.available() > 0){ // Checks whether data is comming from the serial port
    input = Serial.readString();
    state = input.charAt(0);
  } 
  if (state == '0') {
    digitalWrite(ledPin13, LOW);
    digitalWrite(ledPin10, LOW);
  } else if (state == '1'){
    digitalWrite(ledPin13, HIGH);
  }  else if (state == '2'){
    digitalWrite(ledPin10, HIGH);
  } 
  Serial.println(1);
  delay(100);
//    int rawvoltage= analogRead(A0);
//    float millivolts= (rawvoltage/1024.0) * 5000;
//    //This needs to be divided by 10, debugging using 1
//    float kelvin= (millivolts / 1);
//    float celsius= kelvin - 273.15;
//    Serial.println(celsius);
//    delay(100);
  
}
