# E-Volunteer

E-Volunteer is a Jakarta EE volunteer management system implemented as separate pages with a dashboard style inspired by a public volunteer portal.

## Functions

- Student and NGO admin registration and login
- Role-specific dashboard and navigation
- Opportunity creation, editing, deletion, filtering, and search
- Student opportunity applications with seat tracking
- Volunteer-hour submission linked to an application
- NGO approval or rejection of submitted hours
- Verified-hours dashboard and leaderboard
- CSV opportunity report export
- API-first frontend with local demo fallback
- Java REST endpoints for authentication, opportunities, applications, hours, and dashboard state
- Database-ready schema for users, opportunities, applications, and volunteer hours
- Responsive desktop and mobile layout
- JSP views for login, registration, dashboard, opportunities, volunteer hours, leaderboard, feedback, and profile

## MVC mapping

- Servlet controller: `src/main/java/com/mycompany/e/sukarelawan/resources/ESukarelawanApiServlet.java`
- JSP views: `src/main/webapp/*.jsp`
- JavaBean/model classes: `ApiModels.java`, `EVolunteerStore.java`, `JdbcEVolunteerStore.java`, and `InMemoryEVolunteerStore.java`

## Demo accounts

- NGO Admin: `admin@demo.my` / `12345678`
- Student: `student@demo.my` / `12345`

## Build

Run the Maven `clean package` goal. The deployable file is generated at:

`target/E-sukarelawan-1.0-SNAPSHOT.war`

Deploy the WAR to a Jakarta EE 10-compatible server such as GlassFish or Payara.

## Data storage

When opened directly as static HTML or through a simple preview server, the system uses local demo storage so the pages remain functional for coursework demonstration.

When deployed to a Jakarta EE server, the frontend first tries the REST API under:

`/resources/api`

The backend uses MySQL database storage by default:

- The default connection is `jdbc:mysql://localhost:3307/esukarelawan`.
- If MySQL is not running or the database is missing, the app will show the database error instead of silently using demo data.
- Set `ESUKARELAWAN_REQUIRE_DB=false` only when you intentionally want temporary demo mode.

Set these environment variables or Java system properties before deploying:

```text
ESUKARELAWAN_DB_URL=jdbc:mysql://localhost:3307/esukarelawan
ESUKARELAWAN_DB_USER=root
ESUKARELAWAN_DB_PASSWORD=
ESUKARELAWAN_REQUIRE_DB=true
```

Create the database tables with:

`src/main/resources/database-schema.sql`

The schema file also inserts starter demo records, so these accounts work in database mode too:

- NGO Admin: `admin@demo.my` / `12345678`
- Student: `student@demo.my` / `12345`

Example MySQL setup:

```sql
CREATE DATABASE esukarelawan CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Then run `src/main/resources/database-schema.sql` against the `esukarelawan` database.

On Windows, you can also run the included setup helper:

`scripts/setup-mysql-database.bat`

It connects to the NetBeans MySQL server on port `3307`, creates the database if needed, and imports the schema plus starter data.

Database mode now persists:

- accounts and login
- opportunities
- student applications and application review
- volunteer hours and hour review
- profile details, including uploaded profile photo data

This keeps the page logic the same while moving persistence from memory/local demo data into MySQL.
