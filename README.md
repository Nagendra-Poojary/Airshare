# AirShare — Enterprise Dual Interface Platform ✈️🚗

**Package**: `com.ewit.airshare`  
**Affiliation**: East West Institute of Technology (EWIT)  
**Core Domain**: Multi-Constraint Spatio-Temporal Dispatch & Dual-Capacity Optimization for Airport Mobility

AirShare is a real-time luggage-aware corridor clustering engine backed by **Java Spring Boot 3** and **PostgreSQL 16 with PostGIS 3.4**. It couples a **2D 0/1 Knapsack Matcher** (Seats $\le 6$, Usable Boot Units $\le 6$) with **PostGIS Linear Referencing** (`ST_LineLocatePoint`) to eliminate CNG boot overflow, carousel delay lag, and suburban zigzagging.

---

## 🌟 Architecture & Key Features

### 1. Passenger Booking & Curbside Check-In Portal
- **Curbside Readiness Epochs**: Decouples cab matching from scheduled flight landing times; flyers enter the pool only when verified at the terminal curb.
- **Luggage Unit Calculator**: Computes volume units ($1\text{u}$ per cabin bag, $2\text{u}$ per check-in trolley) against CNG MPV boot constraints.
- **Proportional Distance-Split Pricing**: Fixed ₹120 toll is divided equally while running distance costs (₹22/km) are split strictly in proportion to distance.
- **Interactive GIS Map**: Leaflet.js with OpenStreetMap tiles displaying KIA terminal origin, 3 highway corridors, and destination drop pins.

### 2. Driver In-Cab HUD Console & Dispatch Engine
- **Dual Knapsack Monitoring**: Real-time progress gauges tracking Seat Occupancy (e.g. 5/6 seats) and Trunk Boot Volume (e.g. 6/6 units).
- **PostGIS Spatial Pruning**: `ST_DWithin` restricts matches within 3.5 km of arterial highway corridors; `ST_LineLocatePoint` enforces monotonic forward progression ($0.0 \to 1.0$) with zero backtracking.
- **Curbside Onboarding Workflow**: State transitions from `OFFERED` ➔ `ACCEPTED` ➔ `ONBOARDED` ➔ `DISPATCHED`.

---

## 📁 Monorepo Structure

```
Airshare/
├── frontend/
│   ├── interface.html              # Standalone dual-portal interface (Passenger & Driver HUD)
│   └── login.html                  # Enterprise authentication gateway (Passenger & Driver login)
├── backend/                        # Java 17+ / Spring Boot 3 PostGIS Engine
│   ├── pom.xml                     # Maven project configuration
│   └── src/main/
│       ├── resources/
│       │   └── application.properties # PostgreSQL / PostGIS configuration
│       └── java/com/ewit/airshare/
│           ├── AirShareApplication.java
│           ├── model/              # JPA Entities (RideRequest, HighwayCorridor)
│           ├── repository/         # Spatial PostGIS Queries (RideRepository)
│           ├── service/            # 2D Knapsack Matcher & Flight Simulator
│           ├── controller/         # REST Controllers (/api/match, /api/simulate)
│           └── dto/                # Request & Response models
├── .gitignore                      # Git exclusion rules
└── README.md                       # Comprehensive documentation
```

---

## 🚀 Quick Start Guide

### Step 1: Database Setup (PostgreSQL + PostGIS)
Ensure your `airshare_db` database is running on port 5432 with PostGIS enabled and the 3 Bengaluru corridors seeded.
*(See `application.properties` to customize password if needed).*

### Step 2: Run the Spring Boot Backend

#### Option A: Run inside IntelliJ IDEA (Recommended)
1. Open IntelliJ IDEA.
2. Click **Open** and select the `Airshare/backend` folder (or `Airshare/backend/pom.xml`).
3. Allow IntelliJ to sync Maven dependencies.
4. Open [AirShareApplication.java](src/main/java/com/ewit/airshare/AirShareApplication.java) and click the green **▶️ Run** button.
5. The backend will start on **`http://localhost:8080`**.

#### Option B: Run via Maven in Terminal
```powershell
cd backend
mvn spring-boot:run
```

---

### Step 3: Launch the Frontend
Double-click `frontend/interface.html` in your file manager, or run:
```powershell
start frontend\interface.html
```

The frontend will automatically connect to `http://localhost:8080/api`:
- **Backend Status Badge**: Shows `🟢 Backend: Connected (PostGIS)`.
- **⚡ Simulate 25 Arrivals Button**: Injects 25 curbside arrivals into PostGIS with 1 click.
- **🚀 Execute Clustering Button**: Runs the 2D Knapsack algorithm and dispatches MPVs on the live map!

---

## 📡 REST API Reference

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/status` | Engine health check & knapsack capacity bounds |
| `GET` | `/api/corridors` | Returns 3 highway corridors with PostGIS GeoJSON geometries |
| `POST` | `/api/simulate?count=25` | Injects synthetic curbside flyer events into PostgreSQL |
| `POST` | `/api/add-rider` | Ingests a single curbside passenger request |
| `POST` | `/api/match` | Runs the 2D Knapsack algorithm and returns dispatched MPV fleet |
| `DELETE` | `/api/reset` | Clears waiting passenger pool for clean demo runs |

---

## 🏆 Exhibition "Judge Wow Moment" Test
To prove that AirShare prevents physical trunk overflow:
1. In the Passenger Portal, set **Check-in Bags = 3** (consuming all 6 boot units).
2. Set drop location to **Whitefield IT Zone** and click **Confirm Curbside Arrival**.
3. Click **Execute Clustering**: AirShare immediately isolates this rider or pairs them strictly with a zero-luggage passenger, showing that the MPV boot is 100% full (6/6 units) and preventing physical luggage overflow!
