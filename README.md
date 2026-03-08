# RMS KNX Integration

## Overview

RMS KNX Integration is a Java-based server application designed to integrate and control KNX devices such as lights and air conditioners through a TCP-based communication layer.

The system acts as a bridge between external client applications and a KNX network by translating TCP commands into KNX group communication messages and synchronizing device states in real time.

This project uses the **Calimero KNX library** to establish a tunneling connection with a KNX/IP gateway and enables asynchronous communication with multiple clients.

---

## Features

* KNX/IP tunneling connection using Calimero
* TCP server for external client communication
* Multi-client support
* Real-time device state synchronization
* Asynchronous device status querying
* Thread-safe KNX connection management
* JSON-based command and response communication
* Device state caching using in-memory data structures
* Automatic initialization and status sync on server startup
* Scalable concurrent task execution using ExecutorService

---

## Architecture

```
+-------------+       TCP        +--------------------+      KNX/IP      +----------------+
|   Clients   |  <------------>  |   RMS TCP Server   |  <------------>  | KNX/IP Gateway |
+-------------+                  +--------------------+                  +----------------+
                                         |
                                         |
                                         v
                                  +-------------+
                                  | KNX Devices |
                                  | (Lights/AC) |
                                  +-------------+
```

### Components

**TCP Server**

* Accepts connections from multiple clients
* Receives JSON commands
* Dispatches commands to appropriate device services

**KNX Connection Layer**

* Establishes KNX/IP tunneling connection
* Handles group read/write communication
* Attaches listeners for device state updates

**Device Services**

* LightService
* ACService

These services:

* Send commands to KNX devices
* Query device status
* Maintain device state in memory

**State Management**

* Maintains device states using:

  * `Map<String, Boolean>` for light state
  * `Map<String, Integer>` for dim levels
* Provides Optional-based safe access methods

---

## Technologies Used

| Technology      | Purpose                         |
| --------------- | ------------------------------- |
| Java            | Core application development    |
| Maven           | Dependency and build management |
| Calimero        | KNX/IP communication            |
| Jackson         | JSON parsing and serialization  |
| ExecutorService | Concurrent task execution       |
| TCP Sockets     | Client-server communication     |

---

## Key Java Concepts Used

* ExecutorService thread pools
* Java Streams API
* Optional for null-safe handling
* Synchronized methods for thread safety
* Exception handling
* TCP socket programming
* Concurrent device state management

---

## Dependencies

Main dependencies used in the project:

* Calimero Core
* Calimero Device
* Jackson Databind
* Apache Commons Lang
* Apache Commons Collections
* JAXB Runtime

---

## Build Instructions

Clone the repository:

```
git clone https://github.com/your-username/RMSIntegration.git
cd RMSIntegration
```

Build the project:

```
mvn clean package
```

This will generate a **fat jar** containing all dependencies.

---

## Run the Application

```
java -jar target/RMSIntegration-1.0-SNAPSHOT.jar
```

The TCP server will start and wait for client connections.

---

## Communication Format

Clients communicate with the server using JSON messages.

Example command:

```
{
  "deviceType": "LIGHT",
  "deviceId": "A",
  "command": "ON"
}
```

Example response:

```
{
  "status": "success",
  "message": "Light A turned ON"
}
```

---

## Device Initialization

When the server starts:

1. KNX connection is established
2. Device services are initialized
3. Initial device status is queried asynchronously
4. Device states are cached in memory

---

## Concurrency Model

The system uses an **ExecutorService thread pool** to handle:

* KNX reader thread
* KNX writer thread
* Device status synchronization
* TCP client request processing

This ensures efficient non-blocking operation and scalability.

---

## Error Handling

The application includes:

* KNX connection error handling
* Device query exception handling
* Safe Optional-based state retrieval

---

## Future Improvements

* Proper logging
* Support for additional KNX device types
* REST API integration
* Monitoring and metrics support

---

## Author

Prachi Singh

---

## License

This project is intended for internal development and learning purposes.
