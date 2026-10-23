# AirShare — Enterprise Dual Interface Platform ✈️🚗

**AirShare** is a next-generation airport ride-pooling and curbside dispatch platform. It bridges passenger booking with driver in-cab heads-up navigation, featuring real-time state synchronization, corridor-based batch matching, and luggage/seat knapsack capacity optimization.

---

## 🌟 Key Features

### 1. Passenger Booking & Curbside Check-In
- **Multi-Hub Airport Support:** Origin selection for KIA Bengaluru, Mumbai (BOM), Delhi (DEL), Hyderabad (HYD), and Chennai (MAA).
- **Curbside Verification:** Integration for flight tracking, terminal curb gate selection, and seat reservations.
- **Luggage Capacity Calculator:** Volume unit engine calculating cabin bags (1u) and check-in luggage (2u) against vehicle trunk constraints.
- **Dynamic Fare Transparency:** Real-time proportional distance fare splitting vs. solo cab baseline pricing.
- **Interactive GIS Map:** Built with Leaflet & OpenStreetMap, displaying airport hubs, destination drops, and corridor paths.

### 2. Driver In-Cab HUD Console
- **Dual Knapsack Monitoring:** Real-time visual tracking of passenger seating (up to 6 seats) and CNG trunk boot load (up to 6u).
- **Corridor Batch Matching:** Automated grouping along high-frequency airport transit spines.
- **Curbside Onboarding Workflow:** Interactive state machine (`OFFERED` ➔ `ACCEPTED` ➔ `ONBOARDED` / `DISPATCHED`).
- **Sequential Drop Route Engine:** Monotonic waypoint sorting along destination corridors.

---

## 📁 Project Structure

```
Airshare/
├── frontend/
│   └── interface.html    # Standalone dual-portal interface (Passenger & Driver HUD)
├── backend/              # Backend services & APIs (FastAPI / Express ready)
├── .gitignore            # Git exclusion rules
└── README.md             # Project documentation
```

---

## 🚀 Quick Start

### Option A: Direct Launch in Browser
Double-click `frontend/interface.html` in your file explorer, or run in PowerShell:
```powershell
start frontend\interface.html
```

### Option B: Local Web Server (Recommended)
Using Python:
```powershell
python -m http.server 8000 --directory frontend
```
Then visit **[http://localhost:8000/interface.html](http://localhost:8000/interface.html)** in your browser.

---

## 🛠️ Tech Stack

- **Frontend:** HTML5, Vanilla JavaScript, Tailwind CSS (CDN), FontAwesome Icons
- **GIS & Mapping:** Leaflet.js with OpenStreetMap tiles
- **State Management:** Reactive in-memory synchronization between Passenger & Driver portals
