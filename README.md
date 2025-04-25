# TED Talks API 🎤

A RESTful API for managing and analyzing TED Talk data with CSV import functionality.

## 📂 Project Structure
```
src/
├── main/
│ ├── java/com/io/tedtalk/
│ │ ├── controller/ # API controllers
│ │ ├── model/ # Entity classes
│ │ ├── repository/ # Data repositories
│ │ ├── service/ # Business logic
│ │ ├── exception/ # Custom exceptions
│ │ └── TedTalkApiApplication.java
│ └── resources/
│ ├── application.properties
│
└── test/ # Unit and integration tests
└── java/com/io/tedtalk/
├── controller/
├── repository/
└── service/
```


## 🚀 Features

### Core Functionality
- **CRUD Operations**:
  - Get all TED Talks
  - Create new TED Talk
  - Update TED Talk by ID
  - Delete TED Talk by ID
- **Search Capabilities**:
  - Search by title (contains, case-insensitive)
  - Search by author (contains, case-insensitive)
- **Analytics**:
  - Get most influential speakers (ranked internally using views and likes)
  - Get most influential talks per year
- **Statistics Update**:
  - Patch operation to update views/likes

### CSV Import
- Bulk import TED Talks from CSV
- Comprehensive validation:
  - Only accepts csv files
  - Date format validation (MMMM yyyy)
  - URL format validation
  - Duplicate prevention (both in DB and within CSV)
  - Data type validation (views/likes must be integers)
  - Columns cannot be empty

## 🔧 Technical Decisions

1. **Unique Constraints**:
   - `link` field is unique to prevent duplicate TED Talks

2. **Date Handling**:
   - Stored as separate `month` (0-11) and `year` fields for easier operations
   - Exposed as combined "Month Year" string in API (e.g., "June 2023")

3. **Validation**:
   - Comprehensive validation at both controller and service layers
   - Custom error responses for invalid data
   - Assuming link will be unique for a ted talk

4. **CSV Import**:
   - Atomic operation - either all valid rows are imported or none
   - Detailed import report with success/failure counts

5. **Test Coverage**:
   - Enough test coverage added to validate different uses cases and units
  
## 💻 API Documentation

### Swagger
You can use below swagger URL to access the endpoints

- http://localhost:8080/swagger-ui/index.html

### Base URL
`https://your-api-url.com/api/tedtalks`

### Endpoints

#### TED Talks Management
| Method | Endpoint                | Description                          |
|--------|-------------------------|--------------------------------------|
| GET    | `/`                     | Get all TED Talks                    |
| GET    | `/{id}`                 | Get TED Talk by ID                   |
| POST   | `/`                     | Create new TED Talk                  |
| PUT    | `/{id}`                 | Update TED Talk by ID                |
| DELETE | `/{id}`                 | Delete TED Talk by ID                |
| PATCH  | `/{id}/stats`           | Update views/likes                   |

#### Search Endpoints
| Method | Endpoint                | Description                          |
|--------|-------------------------|--------------------------------------|
| GET    | `/search/title?q={query}` | Search by title                     |
| GET    | `/search/author?q={query}`| Search by author                    |

#### Analytics Endpoints
| Method | Endpoint                | Description                          |
|--------|-------------------------|--------------------------------------|
| GET    | `/influencers`          | Top influential speakers             |
| GET    | `/influencers/per-year` | Most influential talks per year      |

#### CSV Import
| Method | Endpoint          | Description                     |
|--------|-------------------|---------------------------------|
| POST   | `/api/import/csv` | Import TED Talks from CSV file  |

## 📝 CSV Format

Required CSV header (exact match): title,author,date,views,likes,link
Number of columns should match the header


## 🛠️ Setup & Installation

1. **Development Environment**:
   - Java 21
   - Maven 3.8+
   - Spring Boot 3.4.5

2. **Configuration**: Database Configuration

The application uses an embedded H2 database with the following configuration:

- **Database Type**: H2 (Embedded)
- **Driver Class**: `org.h2.Driver`
- **JDBC URL**: `jdbc:h2:file:./data/tedtalkdb`
- **Username**: `sa`
- **Password**: `sa`
- **Schema Initialization**: Automatic (schema.sql/data.sql)
- **Hibernate DDL Mode**: `update`

### Accessing H2 Console

During development, you can access the H2 database console:

1. Start the application
2. Navigate to: `http://localhost:8080/h2-console`
3. Use the following connection details:
   - JDBC URL: `jdbc:h2:file:./data/tedtalkdb`
   - Username: `sa`
   - Password: `sa`

### Important Notes

- The database file is stored in the `./data/` directory relative to your application
- The database persists between application restarts (`DB_CLOSE_DELAY=-1`)
- Schema is automatically initialized on application startup
- File uploads are limited to 25MB maximum

For production environments, consider switching to a more robust database like PostgreSQL or MySQL by updating the datasource configuration in `application.properties`.

Build, Run and Test

'''
mvn clean install
mvn spring-boot:run
mvn test
'''

Sample Requests

Create Ted Talk
curl -X POST "http://localhost:8080/api/tedtalks" \
-H "Content-Type: application/json" \
-d '{
    "title": "Ted Talk by John M on API development",
    "author": "John Mike",
    "date": "October 2012",
    "views": 45000000,
    "likes": 2200000,
    "link": "https://example.com/johnmike-article-1"
}'

Import CSV

curl -X POST "http://localhost:8080/api/import/csv" \
-H "Content-Type: multipart/form-data" \
-F "file=@talks.csv"


## Assumptions
- Security/authentication not in scope
- CSV must be properly formatted
- Performance optimizations can be addressed later
- Better database can be chosen later

