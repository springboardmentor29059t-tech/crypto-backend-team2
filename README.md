# 🚀 Crypto Portfolio Tracker (Backend)

## 📌 Project Overview
A robust Spring Boot backend for a **Crypto Portfolio Tracker with Risk Analysis**. This application allows users to aggregate their crypto holdings from exchanges (like Binance), track real-time portfolio value, view historical performance, and analyze risk.

**Current Progress:** ✅**Project Complete (Weeks 1-8 Finished)**

---

## 🛠️ Tech Stack
* **Language:** Java 17+
* **Framework:** Spring Boot 3.x
* **Database:** MySQL
* **Security:** AES-256 Encryption (for API Keys)
* **External APIs:** Binance API (Testnet), CoinGecko API, CryptoScamDB (simulated)
* **Tools:** Maven, Lombok, Postman

---

## 📅 Features Implemented (Week 1 - Week 7)

### ✅ Milestone 1: Auth & Security (Weeks 1-2)
* **User Authentication:** Secure Signup and Login functionality.
* **Exchange Management:** Users can connect multiple exchanges (currently Binance).
* **Security:** API Keys and Secrets are encrypted using **AES-256** before storage in the database. They are only decrypted momentarily during API calls.

### ✅ Milestone 2: Portfolio & Trade Sync (Weeks 3-4)
* **Balance Aggregation:** Automatically fetches "Free" and "Locked" asset balances from the exchange.
* **Trade History Sync:** Downloads full trade history to a local database.
* **Cost Basis Calculation:** Smart algorithm to calculate the "Average Buy Price" for accurate P&L, handling symbol matching (e.g., `BTCUSDT` trades -> `BTC` holdings).
* **Testnet Trading Engine:** Built a custom "Trade Execution" module to place Buy/Sell orders on the Binance Testnet directly via API.

### ✅ Milestone 3: Pricing, Automation & Risk (Weeks 5-6)
* **Live Pricing:** Integrated **CoinGecko API** to fetch real-time USD prices.
* **Automated Scheduler:** A Cron Job runs every **5 minutes** to capture price snapshots automatically.
* **Portfolio Dashboard:** Specific endpoint that combines Holdings + Live Prices to calculate Total Value and P&L.
* **Risk Analysis Engine:** Automatically checks the user's portfolio against a blacklist of known scam tokens (Rug Pulls, Honeypots).
* **Scam Data Sync:** Service to download known malicious contract addresses from external sources.

### ✅ Milestone 4: P&L, Reporting & Optimization (Weeks 7-8)
* **P&L Engine:** Calculates **Realized** (sold) and **Unrealized** (held) profits using FIFO logic.
* **Tax Hints (Week 8):** Auto-categorizes profits as **Short-Term** (<365 days) or **Long-Term** (>365 days).
* **CSV Export:** Generates a downloadable "Tax-Ready" spreadsheet with detailed tax columns.
* **Optimization:** "N+1" query optimization increased sync speed by ~50x.
* **Quality Assurance:** Full unit testing suite for P&L logic.
---

## 🔗 API Documentation (For Frontend Team)

### 1️⃣ Authentication
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/signup` | Register a new user. |
| `POST` | `/api/auth/login` | Login and receive User ID. |

### 2️⃣ Exchange Management
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/exchanges/connect` | Connect a new exchange (e.g., Binance). Body requires `apiKey` and `apiSecret`. |
| `GET` | `/api/exchanges/my-keys` | Retrieve a list of all connected exchange keys for the user. |
| `DELETE` | `/api/exchanges/{id}` | Delete/Disconnect a specific exchange key by its ID. |

### 3️⃣ Portfolio & Dashboard
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/portfolio/refresh` | **Sync Trigger.** Fetches latest balances and trades from Binance for all assets. |
| `GET` | `/api/portfolio/dashboard` | **Main Dashboard Data.** Returns list of assets with Quantity, Current Price, Total Value, and P&L. |
| `POST` | `/api/portfolio/manual` | **Add Manual Holding.** Manually add an asset (e.g., Hardware Wallet funds). |
| `PUT` | `/api/portfolio/manual/{id}` | **Update Manual Holding.** Edit quantity or details of a manually added asset. |
| `DELETE` | `/api/portfolio/manual/{id}` | **Remove Manual Holding.** Delete a manually added asset from the portfolio. |

### 4️⃣ Reports (New!)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/reports/pnl` | **Full P&L Report.** Returns Realized and Unrealized gains for all assets. |
| `GET` | `/api/reports/export` | **Download CSV.** Generates a downloadable file for Excel/Tax purposes. |
| `GET` | `/api/reports/total-balance` | Returns the aggregated total USD value of the portfolio. |

### 5️⃣ Market Data & Charts
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/prices/refresh` | Manually trigger a price update from CoinGecko. |
| `GET` | `/api/prices/history/{symbol}` | Returns historical price data for charts (e.g., `/api/prices/history/BTC`). |

### 6️⃣ Risk & Security
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/risk/analyze` | **Trigger Scan.** Checks the authenticated user's portfolio against the blacklist. |
| `GET` | `/api/risk/alerts` | **View Alerts.** Returns a list of active security warnings (Rug Pulls, Honeypots). |
| `POST` | `/api/risk/blacklist/add` | **Dev Tool.** Manually add a token to the global blacklist. |
| `POST` | `/api/risk/blacklist/sync` | **Dev Tool.** Force sync scam tokens from external GitHub source. |

### 7️⃣ Developer Utilities (Testnet Only)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/portfolio/trade/execute` | Executes a **Real Order** on Binance Testnet. Params: `symbol`, `side`, `quantity`. |

---

## 🗄️ Database Schema
The application uses a relational MySQL database with the following key tables:
* `users`: Stores user credentials.
* `api_keys`: Stores encrypted exchange credentials.
* `holdings`: Stores the current quantity of each asset.
* `trades`: Stores the full history of buy/sell orders.
* `price_snapshots`: Stores historical price data for charting.
* `scam_tokens`: Stores a blacklist of known malicious contract addresses.
* `risk_alerts`: Stores specific warnings generated for the user's portfolio.

---

## 🚀 How to Run locally
1.  **Clone the repository.**
2.  **Configure Database:** Update `src/main/resources/application.properties`:
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/crypto_tracker
    spring.datasource.username=YOUR_USER
    spring.datasource.password=YOUR_PASSWORD
    app.encryption.key=Your16CharKey123
    ```
3.  **Run the Application:**
    * Use VS Code "Run Java" or `mvn spring-boot:run`.
4.  **Access:** Server runs at `http://localhost:8080`.
