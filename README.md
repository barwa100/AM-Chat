1. Wstęp (Kontekst biznesowy)
Cel aplikacji: "Aplikacja do komunikacji w czasie rzeczywistym".

Główne funkcje: "Tworzenie kanałów, wysyłanie wiadomości".

Użytkownicy docelowi: "Osoby chcące rozmawiać".

2. Diagram architektury
TO DO
![Diagram](C:\Users\ccerb\Downloads\diagram.png)
3. Warstwy aplikacji

a) Frontend (Klient)

Technologie: Jetpack Compose (Android)

Stan aplikacji: StateFlow (Android)

Komunikacja z backendem:
SignalR (czas rzeczywisty).

b) Backend

Serwer: 

Autentykacja:

Baza danych: 

Cache: 

4. Wzorce architektoniczne
MVVM (Model-View-ViewModel) – separacja logiki od UI.

5. Kluczowe decyzje techniczne
Dlaczego SignalR? "Uproszczona obsługa WebSocketów i fallback na Long Polling".

Dlaczego StateFlow? "Reaktywność + integracja z Kotlin Coroutines".

6. Przepływ danych (przykład)
Użytkownik tworzy kanał → wysyłane jest żądanie REST API.

Serwer zapisuje kanał w PostgreSQL i emituje zdarzenie SignalR "ChannelCreated".

Wszyscy klienci otrzymują aktualizację przez WebSocket.

Frontend aktualizuje UI (StateFlow/LiveData).
?
7. Biblioteki:

Interfejs Użytkownika i Kompozycja:

- androidx.compose.ui:ui-tooling-android
- androidx.compose.runtime:runtime-android•androidx.compose.ui:ui-android
- androidx.compose.foundation:foundation-android
- androidx.compose.material3:material3-android
- androidx.compose.material:material-icons-core-android 
- androidx.compose.animation:animation-android

Architektura i Cykl Życia:

- androidx.lifecycle:lifecycle-runtime-compose-android
- androidx.lifecycle:lifecycle-viewmodel-compose-android
- androidx.activity:activity-compose

Nawigacja:

- androidx.navigation:navigation-compose

Dodatkowo:

- androidx.room:room-runtime
- androidx.room:room-ktx 
- androidx.room:room-compiler
  
8. Testowanie
