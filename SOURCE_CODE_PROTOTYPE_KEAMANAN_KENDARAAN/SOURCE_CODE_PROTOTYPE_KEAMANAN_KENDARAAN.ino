// #include <TinyGPSPlus.h>
// #include <HardwareSerial.h>

// TinyGPSPlus gps;
// HardwareSerial neogps(1);  // Gunakan UART1

// #define BUZZER_PIN 2

// // Buffer posisi
// const int POS_BUFFER_SIZE = 5;
// double latBuffer[POS_BUFFER_SIZE];
// double lngBuffer[POS_BUFFER_SIZE];
// int bufferIndex = 0;
// bool bufferFilled = false;

// double prevLat = 0;
// double prevLng = 0;
// bool firstFix = false;

// unsigned long lastTrigger = 0;
// const unsigned long triggerInterval = 15000;  // 15 detik

// void setup() {
//   Serial.begin(115200);
//   neogps.begin(9600, SERIAL_8N1, 4, 17);

//   pinMode(BUZZER_PIN, OUTPUT);
//   digitalWrite(BUZZER_PIN, LOW);
// }

// void loop() {
//   while (neogps.available()) {
//     gps.encode(neogps.read());

//     if (gps.location.isUpdated()) {
//       double lat = gps.location.lat();
//       double lng = gps.location.lng();

//       Serial.print("Raw  Lat: ");
//       Serial.print(lat, 6);
//       Serial.print(" | Raw Lng: ");
//       Serial.println(lng, 6);

//       // Simpan ke buffer
//       latBuffer[bufferIndex] = lat;
//       lngBuffer[bufferIndex] = lng;
//       bufferIndex++;

//       if (bufferIndex >= POS_BUFFER_SIZE) {
//         bufferIndex = 0;
//         bufferFilled = true;
//       }

//       if (bufferFilled) {
//         // Hitung rata-rata posisi
//         double avgLat = 0;
//         double avgLng = 0;
//         for (int i = 0; i < POS_BUFFER_SIZE; i++) {
//           avgLat += latBuffer[i];
//           avgLng += lngBuffer[i];
//         }
//         avgLat /= POS_BUFFER_SIZE;
//         avgLng /= POS_BUFFER_SIZE;

//         Serial.print("Avg  Lat: ");
//         Serial.print(avgLat, 6);
//         Serial.print(" | Avg Lng: ");
//         Serial.println(avgLng, 6);

//         if (!firstFix) {
//           prevLat = avgLat;
//           prevLng = avgLng;
//           firstFix = true;
//         } else {
//           double moved = distanceBetween(prevLat, prevLng, avgLat, avgLng);
//           Serial.print("Jarak bergerak (m): ");
//           Serial.println(moved);

//           if (moved > 10.0 && millis() - lastTrigger > triggerInterval) {
//             Serial.println("🚨 GPS Bergerak > 20m! Buzzer ON");

//             for (int i = 0; i < 20; i++) {
//               tone(BUZZER_PIN, 1000);
//               delay(200);
//               tone(BUZZER_PIN, 1500);
//               delay(200);
//             }
//             noTone(BUZZER_PIN);

//             lastTrigger = millis();
//             prevLat = avgLat;
//             prevLng = avgLng;
//           }
//         }
//       }
//     }
//   }
// }

// double distanceBetween(double lat1, double lng1, double lat2, double lng2) {
//   const double R = 6371000;
//   double dLat = radians(lat2 - lat1);
//   double dLng = radians(lng2 - lng1);
//   double a = sin(dLat / 2) * sin(dLat / 2) + cos(radians(lat1)) * cos(radians(lat2)) * sin(dLng / 2) * sin(dLng / 2);
//   double c = 2 * atan2(sqrt(a), sqrt(1 - a));
//   return R * c;
// }



#include <TinyGPSPlus.h>
#include <HardwareSerial.h>
#include <WiFi.h>
#include <Firebase_ESP_Client.h>
#include <ArduinoJson.h>

TinyGPSPlus gps;
HardwareSerial neogps(1);  // UART1 TX=17, RX=4

#define BUZZER_PIN 2

// Buffer posisi
const int POS_BUFFER_SIZE = 5;
double latBuffer[POS_BUFFER_SIZE];
double lngBuffer[POS_BUFFER_SIZE];
int bufferIndex = 0;
bool bufferFilled = false;

double prevLat = 0;
double prevLng = 0;
bool firstFix = false;

unsigned long lastTrigger = 0;
const unsigned long triggerInterval = 15000;  // 15 detik

// WiFi & Firebase config
#define WIFI_SSID "Reiky"
#define WIFI_PASSWORD "09876543"
#define API_KEY "AIzaSyAdoA70QgO1wgNXfp9gHv1Tckq8slSi74k"  // API Key dari Firebase Project
#define DATABASE_URL "https://keamanansepedamotor-default-rtdb.asia-southeast1.firebasedatabase.app/"
#define USER_EMAIL "221111033@mhs.stiki.ac.id"
#define USER_PASSWORD "Stikimalang100"

FirebaseData fbdo;  
FirebaseAuth auth;
FirebaseConfig config;

void setup() {
  Serial.begin(115200);
  neogps.begin(9600, SERIAL_8N1, 4, 17);

  pinMode(BUZZER_PIN, OUTPUT);
  digitalWrite(BUZZER_PIN, LOW);

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi Connected");

  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;
  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;

  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);
}

// ... [semua bagian atas tetap sama seperti kode kamu] ...

void loop() {
  while (neogps.available()) {
    gps.encode(neogps.read());

    if (gps.location.isUpdated()) {
      double lat = gps.location.lat();
      double lng = gps.location.lng();

      Serial.print("Raw  Lat: ");
      Serial.print(lat, 6);
      Serial.print(" | Raw Lng: ");
      Serial.println(lng, 6);

      // Simpan ke buffer
      latBuffer[bufferIndex] = lat;
      lngBuffer[bufferIndex] = lng;
      bufferIndex++;

      if (bufferIndex >= POS_BUFFER_SIZE) {
        bufferIndex = 0;
        bufferFilled = true;
      }

      if (bufferFilled) {
        // Hitung rata-rata posisi
        double avgLat = 0;
        double avgLng = 0;
        for (int i = 0; i < POS_BUFFER_SIZE; i++) {
          avgLat += latBuffer[i];
          avgLng += lngBuffer[i];
        }
        avgLat /= POS_BUFFER_SIZE;
        avgLng /= POS_BUFFER_SIZE;

        Serial.print("Avg  Lat: ");
        Serial.print(avgLat, 6);
        Serial.print(" | Avg Lng: ");
        Serial.println(avgLng, 6);

        // Kirim ke Firebase
        sendToFirebase(avgLat, avgLng);

        if (!firstFix) {
          prevLat = avgLat;
          prevLng = avgLng;
          firstFix = true;
        } else {
          double moved = distanceBetween(prevLat, prevLng, avgLat, avgLng);
          Serial.print("Jarak bergerak (m): ");
          Serial.println(moved);

          if (moved > 10.0 && millis() - lastTrigger > triggerInterval) {
            Serial.println("🚨 GPS Bergerak > 10m! Buzzer ON");

            // Kirim status is_gerak = true
            Firebase.RTDB.setDouble(&fbdo, "motor001/sensor/lokasi/is_gerak", 1);

            for (int i = 0; i < 20; i++) {
              tone(BUZZER_PIN, 1000);
              delay(200);
              tone(BUZZER_PIN, 1500);
              delay(200);
            }
            noTone(BUZZER_PIN);

            // Setelah alarm mati, kirim is_gerak = false
            Firebase.RTDB.setDouble(&fbdo, "motor001/sensor/lokasi/is_gerak", 0);

            lastTrigger = millis();
            prevLat = avgLat;
            prevLng = avgLng;
          }
            Firebase.RTDB.setDouble(&fbdo, "motor001/sensor/lokasi/is_gerak", 0);
        }
      }
    }
  }
}

void sendToFirebase(double lat, double lng) {
  String basePath = "motor001/sensor/lokasi";

  Firebase.RTDB.setDouble(&fbdo, basePath + "/lat", lat);
  Firebase.RTDB.setDouble(&fbdo, basePath + "/lng", lng);
}

double distanceBetween(double lat1, double lng1, double lat2, double lng2) {
  const double R = 6371000;
  double dLat = radians(lat2 - lat1);
  double dLng = radians(lng2 - lng1);
  double a = sin(dLat / 2) * sin(dLat / 2) + cos(radians(lat1)) * cos(radians(lat2)) * sin(dLng / 2) * sin(dLng / 2);
  double c = 2 * atan2(sqrt(a), sqrt(1 - a));
  return R * c;
}
