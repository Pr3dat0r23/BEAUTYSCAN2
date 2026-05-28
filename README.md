# BEAUTYSCAN2

Aplikacja mobilna na Androida (Kotlin) do skanowania kodów kreskowych kosmetyków i analizy ich składu.

## Zrealizowane zadania

* **Szkielet interfejsu (UI):** Utworzono główne fragmenty nawigacyjne (skaner, historia, ręczne wyszukiwanie).
* **Baza danych:** Wdrożono lokalną bazę Room (scan_history) do zapisu historii skanów (kod EAN, nazwa, marka, ocena, data).
* **Integracja API:** Skonfigurowano pobieranie danych o produkcie z polskiego API Open Beauty Facts (Retrofit).
* **Modele danych:** Przygotowano struktury (Gson) do odczytu nazwy, marki, składu (INCI/PL) oraz tagów (np. wegański, mikroplastik).
* **Asynchroniczność:** Zaimplementowano asynchroniczne zapisywanie i pobieranie historii w czasie rzeczywistym przy użyciu Kotlin Coroutines i Flow.

## Technologie

* Kotlin
* Android Jetpack (Room)
* Retrofit & Gson
* Coroutines & Flow
