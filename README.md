# Energy API

Backend service for a clean energy dashboard.

The application fetches Great Britain generation mix data from the Carbon Intensity API and exposes endpoints for:

* daily energy mix summary for today, tomorrow and the day after tomorrow,
* optimal electric vehicle charging window based on the highest clean energy share.

Clean energy sources used in calculations:

* biomass
* nuclear
* hydro
* wind
* solar

## Tech stack

* Java 25
* Spring Boot
* Maven
* Spring Web
* Spring Validation
* Spring Cache
* Caffeine
* Spring Boot Actuator
* JUnit 5
* Mockito

## External API

The application uses the Carbon Intensity API endpoint:

```text
GET https://api.carbonintensity.org.uk/generation/{from}/{to}
```

Datetime parameters are sent in ISO8601 format:

```text
YYYY-MM-DDThh:mmZ
```

Example:

```text
2026-07-01T00:00Z
```

## Running locally

### Requirements

* Java 25
* Maven Wrapper included in the project

### Start application

Windows:

```bash
mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

The application starts on:

```text
http://localhost:8080 
```

Health check:

```text
GET http://localhost:8080/actuator/health
```

## Docker

Build image:

```bash
docker build -t energy-api .
```

Run container:

```bash
docker run --rm -p 8080:8080 energy-api
```

The API will be available at:

```text
http://localhost:8080
```

## API endpoints

### Get daily energy mix

```text
GET /api/energy-mix
```

Returns average generation mix values for today, tomorrow and the day after tomorrow.

Example response:

```json
{
  "days": [
    {
      "date": "2026-07-01",
      "cleanEnergyPercentage": 49.69,
      "sources": [
        {
          "source": "biomass",
          "percentage": 5.92
        },
        {
          "source": "wind",
          "percentage": 18.35
        }
      ]
    }
  ]
}
```

### Get optimal charging window

```text
GET /api/charging-window?durationHours=3
```

The `durationHours` parameter must be a full number between 1 and 6.

Example response:

```json
{
  "startDateTime": "2026-07-01T13:00:00+01:00",
  "endDateTime": "2026-07-01T16:00:00+01:00",
  "cleanEnergyPercentage": 62.41
}
```

## Validation

The charging duration is validated using Spring Validation annotations.

Valid values:

```text
1-6
```

Invalid values return a clear error response.

Example:

```text
GET /api/charging-window?durationHours=7
```

Response:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "durationHours must be between 1 and 6.",
  "path": "/api/charging-window"
}
```

## Caching

Generation data from the external API is cached for 5 minutes using Spring Cache and Caffeine.

This prevents unnecessary calls to the external API during repeated frontend refreshes.

## Tests

Run tests:

Windows:

```bash
mvnw.cmd test
```

Linux/macOS:

```bash
./mvnw test
```

Current test coverage includes:

* daily energy mix calculation,
* grouping half-hour intervals by date,
* clean energy percentage calculation,
* optimal charging window selection,
* insufficient generation data handling.

## Project structure

- `client`
  - `carbonintensity`
- `config`
- `controller`
- `domain`
- `dto`
- `exception`
- `service`

## Notes

The application uses the Europe/London time zone when grouping and calculating energy data for Great Britain.
