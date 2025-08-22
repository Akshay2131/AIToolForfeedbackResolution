# AI Tool for Feedback Resolution - Frontend Chat System

This project provides a modern web-based chat interface that allows users to ask questions about LOG_ERROR data and get AI-powered insights through SQL queries.

## Features

- **Modern Chat Interface**: Clean, responsive design with real-time messaging
- **AI Integration**: Connects to Gemini AI for intelligent query generation
- **SQL Execution**: Automatically executes generated SQL queries against your database
- **Data Visualization**: Displays query results in formatted tables
- **Error Handling**: Comprehensive error handling for both API and database operations

## Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- MySQL database with LOG_ERROR table
- Google Gemini API key

## Setup

1. **Configure Database**: Ensure your MySQL database is running and the LOG_ERROR table exists
2. **Set API Key**: Add your Google Gemini API key to `application.properties`:
   ```properties
   GOOGLE_API_KEY=your_api_key_here
   ```
3. **Database Configuration**: Update `application.properties` with your database connection details

## Running the Application

### Option 1: Using Maven
```bash
cd AIToolForfeedbackResolution
mvn spring-boot:run
```

### Option 2: Using Maven Wrapper
```bash
cd AIToolForfeedbackResolution
./mvnw spring-boot:run
```

### Option 3: Build and Run JAR
```bash
cd AIToolForfeedbackResolution
mvn clean package
java -jar target/AIToolForfeedbackResolution-0.0.1-SNAPSHOT.jar
```

## Accessing the Frontend

Once the application is running:

1. Open your web browser
2. Navigate to: `http://localhost:8080`
3. The chat interface will load automatically

## How to Use

1. **Ask Questions**: Type natural language questions about your data
   - Example: "Show me all errors for profile 47719642"
   - Example: "What are the recent sync failures?"
   - Example: "Find transactions with response code 400"

2. **AI Processing**: The system will:
   - Send your question to Gemini AI
   - Generate appropriate SQL queries
   - Execute the queries against your LOG_ERROR table
   - Display results in formatted tables

3. **View Results**: Query results are automatically formatted and displayed in the chat

## API Endpoints

- `POST /ask-gemini-flash` - Main chat endpoint that processes user questions
- `GET /test` - Test endpoint for SQL execution

## File Structure

```
src/main/resources/static/
├── index.html          # Main chat interface
├── styles.css          # Styling and responsive design
└── script.js           # Chat functionality and API integration
```

## Troubleshooting

### Common Issues

1. **API Key Not Configured**: Ensure `GOOGLE_API_KEY` is set in `application.properties`
2. **Database Connection**: Check database credentials and ensure MySQL is running
3. **Port Conflicts**: If port 8080 is busy, change it in `application.properties`

### Logs

Check the console output for detailed error messages and debugging information.

## Security Notes

- The system only allows SELECT queries for security
- All queries are validated before execution
- API keys should be kept secure and not committed to version control

## Browser Support

- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+

## Contributing

Feel free to submit issues and enhancement requests! 