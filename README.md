Smart Campus Sensor and Room Management API

Module: 5COSC022W - Client Server Architectures
Student Name: D.S.Raphael.A.Wijesekera
Student ID: 20232861/ w2120763

API Overview and Design
	This API follows REST architectural princples to provide a campus-wide interface that manages physical rooms and the sensors deployed within them. The system built entirely using JAX-RS with embeded Grizzly HTTP server without any external server or database as mentioned in the coursework.

	Design Principles
		01) Resource based URLs - every entity (Room, Sensor, Sensor Reading) has it's own addressable URL
		02) Correct HTTP methods - GET for retreival, POST for creation and DELETE for deletion/ removal
		03) Meaningful HTTP status codes - 200, 201, 404,403, 409, 422, 500
		04) JSON throughout - all requests and responses use 'application/json'
		05) HATEOAS - every response includes '_links' to guide clients to related resources
		06) In memory storage - data is stored using ConcurrentHashMap for the safety of the threads
		07) No database - only Java data strcutures as required by the specification

	Tech Stack
		Language - Java
		FrameWork - JAX-RS via Jersey
		HTTP Server - Grizzly2
		JSON - Jackson
		Build Tool - Apache Maven
		Storage - ConcurrentHashMap

Step-by-step build and run instructions
	Requirements
		Java JDK 11 or higher - download from https://adoptium.net
		Apache Maven 3.6+ — download from https://maven.apache.org/download.cgi
		
	Verify Both are installed
		java -version
		mvn -version
		
	Step 1: Clone Repository
		git clone https://github.com/shyanraphael/smartcampus-api.git
		cd smartcampus-api
		
	Step 2: Build the project
		mvn clean package
		
	WAIT FOR BUILD SUCCESS. This creates "target/smartcampus-api-1.0-SNAPSHOT.jar".
	
	Step 3: Run the server
		Windows: java -jar target\smartcampus-api-1.0-SNAPSHOT.jar
		Mac/ Linux: java -jar target/smartcampus-api-1.0-SNAPSHOT.jar
		
	YOU WILL SEE:
		---Smart Campus API Server Started!---

		01) Base URL  : http://localhost:8080/api/v1
		02) Discovery : http://localhost:8080/api/v1/
		03) Rooms     : http://localhost:8080/api/v1/rooms
		04)Sensors   : http://localhost:8080/api/v1/sensors

		Press CTRL+C to stop the server.
		
	Step 4: Verify it works
		Open your designated browser and go to: http://localhost:8080/api/v1/rooms
		
	YOU SHOULD BE ABLE TO SEE A JSON LIST OF 3 PRE-LOADED ROOMS. 
	THE API IS READY.
	
Sample curl commands
	To make sure the server is running execute the following commands below.
	
	1. Get the Discovery endpoint
		curl -X GET http://localhost:8080/api/v1/
		
	2. Get all rooms
		curl -X GET http://localhost:8080/api/v1/rooms
		
	3. Create a new room 
		curl -X POST http://localhost:8080/api/v1/rooms \
		  -H "Content-Type: application/json" \
		  -d '{"id": "CONF-101", "name": "Conference Room A", "capacity": 25}'
		  
	4. Get a specific room by ID
		curl -X GET http://localhost:8080/api/v1/rooms/LAB-101
		
	5. Create a new sensor linked to an existing room
		curl -X POST http://localhost:8080/api/v1/sensors \
		  -H "Content-Type: application/json" \
		  -d '{"id": "TEMP-NEW", "type": "Temperature", "status": "ACTIVE", "currentValue": 20.0, "roomId": "LAB-101"}'
		 
	6. Get all sensors
		curl -X GET http://localhost:8080/api/v1/sensors
	
	7. Filter sensors by type
		curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
		
	8. Add a reading to an active sensor
		curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
		  -H "Content-Type: application/json" \
		  -d '{"value": 24.5}'
		  
	9. Get all readings for a sensor
		curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings
		
	10. Try to delete a room that still has no sensors - Orphan Sensors (409 error)
		curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
		
	11. Try to post a reading to a MAINTENANCE sensor (403 error)
		curl -X POST http://localhost:8080/api/v1/sensors/OCC-001/readings \
		  -H "Content-Type: application/json" \
		  -d '{"value": 10.0}'
		  
	12. Try to create a sensor with non-existent roomID (422 error)
		curl -X POST http://localhost:8080/api/v1/sensors \
		  -H "Content-Type: application/json" \
		  -d '{"id": "FAKE-001", "type": "CO2", "status": "ACTIVE", "currentValue": 0, "roomId": "FAKE-999"}'
		  
Report Answers to Coursework Questions
	Part 1: SETUP AND DISCOVERY
		1.1 Architecture and Config: JAX-RS Resource Lifecycle and In-Memory Data Synchronization
			
			Default Lifecycle: Request Scoped
				JAX-RS operates in a request-based lifecycle, typically. This implies that it opens a new resource instance with each inbound HTTP request, and 
				closes it when it sends the response. In this design, resource objects are by default stateless and thread-safe. The requests are allocated their 
				own objects, preventing requests to leak data between themselves via common fields.
				
			Why This Creates a Problem for In-Memory Storage
				This has a very important implication on the per-request lifecycle: any data stored as an instance field in a resource class will be lost as soon 
				as the request is finished. To show this, in the case where RoomResource had the following instance field:private Map<String, Room> rooms = new 
				HashMap<>()the POST requests would create a new empty map every time, add a room to the map, and completely overwrite the map every time the 
				response was sent. The following GET request would get another new, empty map - entirely ignorant of any rooms which had been created.
				
			Solution: Singleton Data Server with ConcurrentHashMap
				In order to separate data persistence and resource lifecycle, the DataStore class is implemented with the Singleton design pattern. The singleton 
				is created once only when the server is started and is then alive throughout the entire life of the application. All instances of resource
				classes no matter the number of instances created in parallel requests - invoke DataStore.getInstance, and works on the same communal data.
				
			Synchronization Stratergy
				As several HTTP requests may come in at the same time, and be served by different threads, a plain HashMap would create race conditions - two 
				threads that write to the map simultaneously may corrupt the internal structure of the map, leading to data loss or 
				ConcurrentModificationException. To avoid this, data collections have been done in DataStore using ConcurrentHashMap, which splits the map into 
				segments and uses fine-grained locking. It enables reads to be made without blocking, and serialises writes to the same key. The outcome is that 
				a thread-safe data store can be made, which guarantees integrity in high-concurrency conditions without any explicit synchronized blocks on all 
				resource methods.
				
		1.2 Discovery Endpoint: HATEOAS and the Value of Self Documention APIs
		
			What HATEOAS is?
				HATEOAS Hypermedia as the Engine of Application State is a constraint of the REST architectural design whereby each response of an API contains 
				hyperlinks about what actions can be done next and the resources available. Instead of making the clients build URLs in their memory or 
				documentation, the server includes navigation with each response.
				
			Why It Is a Trademark of Advanced RESTful Design
				HATEOAS transforms an API into a collection of endpoints into a truly self-describing system. As a client makes a request GET /api/v1/ the reply 
				does not simply give the data but a map of all the collections of resources and the way to access them. When a sensor is formed, the response 
				contains links to the detail page and readings sub-resource of the sensor. Client does not have to guess, hardcode, or refer to external 
				documentation to understand where to proceed.
				
			In this API every resposne body includes a '_links' object with keys such as "self", "readings", "room" and "allRooms" giving the client a complete 
			navugation map at runtime.
			
			
	Part 2: ROOM MANAGEMENT
		2.1 Room Implementation: ID Only vs Full Object Returns in a Collection
		
			When designing a GET/rooms endpoint that returns multiple rooms two approaches will exist:
				01) Returning only IDs
					The response data is limited - maybe simply a list of strings like: [ "LIB-301" and "LAB-101" and "HALL-A"]. This reduces the bandwidth used, 
					which is important when something is at scale; thousands of rooms. It however, pushes the client to the N+1 problem: to make any meaningful 
					display the client will need to make a separate GET /rooms/{id} request on each of the returned IDs. A list of 500 rooms is 501 total HTTP 
					requests each a network latency, connection overhead, and server processing cost. The cost of the follow-up requests would be vastly 
					greater than the bandwidth saved on the list response.
					
				02) Returning Full Objects
					The answer contains the full room object - ID, name, capacity and sensor IDs - of all rooms in one request. It delivers all the information 
					required to render a dashboard, do client-side filtering, or fill a table in a UI in a single round trip. This is larger with a larger 
					payload, which is insignificant on reasonably sized datasets, given the modern network infrastructure and squashing of the HTTP payload.
					
				Decision and Justification
					This API provides the complete room objects in the list response. The number of network round trips is the main factor to be considered; it 
					affects the perceived performance much more than raw payload size. Repatriation of whole objects does away with the N+1 problem. When the 
					size of the dataset is large enough that the size of the payload is a real issue, the right answer is to add pagination, where you have query 
					parameters like ?page= and ?limit=, not to reduce the response to IDs only.
					
		2.2 Deletion and Logic: Indempotency of the DELETE Operation
			An HTTP operation is idempotent when repeatedly making an operation causes the same server state as making the operation. Idempotency is regarding 
			the impact on the state of the server, not regarding getting the same answer every time.
			
			Analysis of DELETE in This Implementation
				Consider a client that sends DELETE /api/v1/rooms/CONF-101 three times in succession:
					01) First call: Room CONF-101 exists. The business logic checks it has no sensors then removes it all from the ConcurrentHashMap and returns 
					200 OK with a confirmation body.
					02) Second call: Room CONF-101 no longer exists. The lookup returns null and a ResourceNotFoundException is thrown and the mapper returns
					404 Not Found.
					03) Third call: Identical to the second call (404 Not Found).
					
			Is This Idempotent?
				Yes. There is no room after the initial call. After the second call, the room still does not exist. There is still no third, after the third. 
				After the second call, the state of the server, which is the contents of the rooms map, is the same. There are no other side effects. The 
				repeated calls do not modify, create, or corrupt any data.
				
				The first deletion alters the HTTP status code of 200 to 404, which is not to the detriment of idempotency. In the REST specification idempotency 
				is defined solely by state of the server, and not by response codes. RFC 7231 specifically mentions that DELETE is idempotent, and the 404 
				response to further calls is the desired and expected behaviour - it is the true reflection of absence of the resource which is the desired final 
				state. A client that repeats a DELETE because of a network failure can safely do so, knowing it will either succeed (when the first attempt 
				failed) or will get a 404 (when the first attempt succeeded) - in either case the server is in the right state.
				
	Part 3: SENSORS AND FILTERING
		3.1 Sensor Integrity: Technical Consequences of @Consumes Media Type Mismatches
			How @Consumes Works
				The @Consumes(MediaType.An annotation on a JAX-RS method using APPLICATION_JSON) indicates a contract: only request bodies with Content-Type of 
				application/json are accepted by this endpoint. Upon receiving a request, Jersey checks the Content-Type header and then calls any application 
				code.
				
			What Happens When a Client Sends the Wrong Format?
				If a client sends a POST request with 'Content-Type: text/plain' or 'Content-Type: application/xml' Jersey evaluates all the registered resource 
				emthods and finds that none of them declare @Consumes values matching the incoming content type. Jersey will reject the request completely before 
				the body of the request method is invoked and will return:
					HTTP 415 Unsupported Media Type
				No code is needed for you to generate this: jersey does it internally․ The client application is informed of the problem and can adjust the 
				Content-Type header of its requests․
				
		3.2 Filtered Retrieval: @QueryParam vs Path Segment for Collection Filtering 
			The Two Approaches
				01) Query paramater: GET /api/v1/sensors?type=C02
				02) Path segment: GET /api/v1/sensors/type/C02
				
			Why Query Parameters are Architecturally Superior for Filtering	
				REST assumes a semantic difference between a path segment that identifies a resource‚ and a query parameter that modifies a resource's response
				when retrieved․ For example‚ the sensor type "CO2" does not in itself identify a specific resource but narrows down the resource collection to 
				CO2 sensors․ This would mean that /sensors/type/CO2 is a separate resource‚ not just part of a resource identifier․ Thus‚ the data model would be 
				misinterpreted and this would confuse client developers․
				
			The @QueryParam approach cleanly seperates each resource identity from resource refinement, produces composable and optional filters and aligns with
			REST conventions and industry standard.
			
	Part 4: SUB-RESOURCES
		4.1 Sub-Resource Locator: Managing Complexity and Allocation
		
			What the Pattern Does?
				A sub-resource locator is a JAX-RS method annotated with the @Path annotation‚ but not with an HTTP method annotation‚ such as @GET or @POST․
				Instead of handling the request the sub-resource locator returns an object․ JAX-RS looks for methods that return an HTTP handler by inspecting
				the object's methods for annotations․ In this API‚ the SensorResource locator for the path {sensorId}/readings returns a SensorReadingResource‚
				instantiated with the sensorId provided to the locator within the constructors․
				
			How It Manages Complexity in Large APIs
				In a naive implementation‚ every couple of nested paths will redirect to a different method of the parent resource class․ In a larger API
				implementation the controller class grows to handle the methods of sensors‚ readings‚ alert messages‚ calibration records‚ maintenance logs‚ etc‚
				accompanied by the GET‚ POST‚ and DELETE methods․ This results in a class with dozens of methods and hundreds of lines of code‚ but no clean
				divisions to follow‚ test‚ and change any feature‚ the entire file must be understood․
				
				An alternative approach is to use sub-resource locators․ This allows the Single Responsibility Principle to be applied at the class level․
				SensorResource uses sensor-level operations only‚ while SensorReadingResource is read-only․ Because the structural classes are small‚ cohesive
				and testable‚ if a developer changes the logic for working with reading history‚ they only need to focus on SensorReadingResource and can be
				confident they are not affecting sensor registration or filtering․
			
	Part 5: ERROR HANDLING
		5.1 Specific Exceptions: Why HTTP 422 Is Significantly Superior to 404
				Lets take an example scenario to showcase why 404 is wrong and why 422 is correct.
				
			The Scenario
				A valid HTTP POST request is sent to /api/v1/sensors with a JSON object as the body․ The JSON is syntactically correct‚ and all required fields
				are present․ However‚ the roomId field has a value (for example "FAKE-999") that does not correspond to an existing room in the system․
				
			Why Is 404 Wrong?
				HTTP 404 Not Found means that the requested URL does not exist․ However‚ the URL /api/v1/sensors does exist and works absolutely perfectly․
				Returning a 404 response tells the client "this endpoint doesn't exist"․ The client has no idea whether the endpoint is valid‚ so they'll think
				the client has the wrong URL and debug their request path‚ wasting time on a nonexistent problem․
				
			Why Is 422 Correct?
				HTTP 422 Unprocessable Entity means: "The server understands the content type of the request‚ the request is syntactically well-formed‚ but it
				cannot process the contained instructions due to semantic errors․" That's what we have here․ The JSON body is correct․ The endpoint exists․ The
				server understood the request completely․ All of which brings me to the crux of the issue: the resource referred to by the value of roomId does
				not exist‚ and therefore the operation cannot be performed․
				
		5.2 Global Safety Net: CyberSecurity Risks of Exposing Java Stack Traces
				Returning a raw Java stack trace in an API reponse is a critical security vulnerability that violates the principle of minimal information
				disclosure. 
				
				01) Tech Stack and Version Fingerprinting
					The stack trace also contains fully qualified class names such as org․glassfish․jersey․server․ServerRuntime and com․fasterxml․jackson
					databind․ ObjectMapper․ These can reveal which frameworks are used‚ and combined with package version strings allow an attacker to search the
					NVD database or CVE databases for known exploits against those specific versions․ One of the most immediate threats is any server running
					Jersey 3․1․3 (with the deserialization vulnerability)․
					
				02) Internal Code Structure and Architecture
					This can also be seen from the stack trace class names and method signature format (for example‚ com․smartcampus․resource․ SensorResource
					createSensor․ The attacker can learn naming conventions‚ the top few available application layers (resource‚ model‚
					exception)‚ and the line number where execution failed; this makes it considerably easier to create a targeted injection attack‚ or to
					determine which code paths to analyze further․
					
				03) File System Paths
					Most stack traces will return absolute file system paths to the offending source files‚ e․g․ /home/ubuntu/smartcampus/src/main/java/․․․․ The
					stack trace may leak the operating system‚ the username of the account running the server‚ the layout of the directory structure that the
					source was deployed to‚ and sometimes information about the development environment․ This information is useful for path traversal attacks
					and exploit development․
					
				04) Business Logic and Validation Exposure
					Exception stack traces also convey information about what the server was doing when the exception occurred․ For example‚ a
					NullPointerException originating from line 42 of a validateRoomCapacity method will tell the reader that there was validation logic‚ there
					was an input to validate‚ and validation logic could have been circumvented by passing a null reference․ Attackers can leverage this to find
					unvalidated code paths and then create inputs that will pass through security checks․
					
				05) Data Strucutre Disclosure
					Exceptions like ArrayIndexOutOfBoundsException for arrays‚ and ConcurrentModificationException for concurrent collection use‚ are
					particularly informative‚ again much like a ClassCastException‚ providing information on how the server's internal type system is used․ This
					narrows the attacker's model‚ which may ease the finding of exploitable conditions․
					
				Mitigations in this API
					GlobalExceptionMapper implements ExceptionMapper<Throwable>․ It is called on any Throwable‚ subclass of RuntimeException or subclass of Error․
					The full stack trace is logged only in the server console‚ which is accessible only to permitted developers․ The client receives a single
					response with an HTTP status code of 500‚ and a descriptive error message: "An unexpected error occurred on the server"․ No information about
					class names‚ line numbers or path names‚ and the exception type are being sent to the client․ In this way‚ the API surface doesn't expose
					implementation‚ regardless of what systemic error caused it․
