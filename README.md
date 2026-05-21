# data-aggregator
## API Data Aggregator with JSON/CSV export

Java service for collecting, normalizing, and saving data from various APIs. Supports export to **JSON** and **CSV**. 

### Features

- **Two run modes:**
  - Automatic
    Accepts the following input parameters:
    - List of APIs
    - Output file format(JSON/CSV)
    - Max number of threads
    - Poll interval
  - Interactive
    User can choose API, output file format, file write mode and read the output file.
    User can also choose how to run the application: in single-thread mode or  multithread mode. User chooses max number of threads and poll         interval in multithread mode.
- **Two output file formats:**
  - JSON
  - CSV
- **Three public APIs:**
  - [The Metropolitan Museum of Art Collection API](https://metmuseum.github.io/)
  - [REST Countries API](https://restcountries.com/)
  - [Open Meteo API](https://api.met.no/weatherapi)

 ### Technologies
 - Java 24 (OpenJDK)
 - Maven
 - Jackson
 - OpenCSV
 - IntelliJ IDEA

### Build & Run

The project uses Maven for building. Compile and package the application with:
```bash
mvn clean package
```

To start the application:
```bash
java -jar ./target/data-aggregator.jar
```
In automatic mode, pass command-line arguments at startup using the following format:
```bash
java -jar ./target/data-aggregator.jar <api_list> <file_format> <thread_count> <poll_interval> <poll_duration>
```


