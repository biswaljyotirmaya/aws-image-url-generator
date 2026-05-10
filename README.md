# AWS Image URL Generator

A Spring Boot REST API that uploads image files to an AWS S3 bucket, stores the generated image URL in MySQL, and exposes endpoints to upload and list saved images.

The project is useful when an application needs a simple backend service for image hosting, profile photo uploads, blog/media uploads, or any workflow where uploaded files should be stored in S3 and referenced later by URL.

## Features

- Upload image files using `multipart/form-data`
- Store uploaded files in AWS S3 with a unique UUID-based file name
- Save image metadata in MySQL using Spring Data JPA
- Retrieve all uploaded image records
- Validate upload confirmation before writing to S3
- Configure AWS credentials through environment variables
- Swagger/OpenAPI UI support through Springdoc
- 10 MB default multipart upload limit

## Tech Stack

- Java 17
- Spring Boot 3.5.14
- Spring Web
- Spring Data JPA
- Spring Validation
- MySQL
- AWS SDK for Java v2
- Lombok
- Springdoc OpenAPI
- Maven

## Project Structure

```text
5-aws-image-url-generator/
|-- pom.xml
|-- mvnw
|-- mvnw.cmd
`-- src/
    |-- main/
    |   |-- java/com/jb/
    |   |   |-- Application.java
    |   |   |-- congfig/AwsConfig.java
    |   |   |-- controller/ImageController.java
    |   |   |-- entity/ImageEntity.java
    |   |   |-- repo/ImageRepo.java
    |   |   `-- service/S3Service.java
    |   `-- resources/application.properties
    `-- test/java/com/jb/ApplicationTests.java
```

## How It Works

1. The client sends an image file to the upload endpoint.
2. The API checks whether `confirm=true` is present.
3. The file is uploaded to the configured S3 bucket.
4. A public-style S3 URL is generated for the object.
5. The image name and URL are saved in the MySQL database.
6. The generated image URL is returned to the client.

## Prerequisites

Install and configure the following before running the application:

- Java 17 or later
- Maven, or use the included Maven Wrapper
- MySQL Server
- AWS account with an S3 bucket
- AWS IAM user or role with permission to upload objects to the target bucket

Minimum IAM permission for uploading objects:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject"
      ],
      "Resource": "arn:aws:s3:::YOUR_BUCKET_NAME/*"
    }
  ]
}
```

## Database Setup

Create the MySQL database used by the application:

```sql
CREATE DATABASE s3_image;
```

The table for `ImageEntity` is created or updated automatically because the application uses:

```properties
spring.jpa.hibernate.ddl-auto=update
```

## Configuration

Main configuration file:

```text
src/main/resources/application.properties
```

Current important properties:

```properties
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/s3_image
spring.datasource.username=root
spring.datasource.password=your_mysql_password

cloud.aws.access-key=${AWS_ACCESS_KEY_ID:}
cloud.aws.secret-key=${AWS_SECRET_ACCESS_KEY:}
cloud.aws.region=${AWS_IMAGE_S3_REGION:eu-north-1}
cloud.aws.s3.bucket=your-s3-bucket-name

spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

spring.jpa.hibernate.ddl-auto=update
```

Recommended: keep secrets out of source code. Store AWS credentials and database passwords in environment variables, a local profile, or a secret manager.

## Environment Variables

Set AWS credentials before starting the app.

PowerShell:

```powershell
$env:AWS_ACCESS_KEY_ID="your_access_key"
$env:AWS_SECRET_ACCESS_KEY="your_secret_key"
$env:AWS_IMAGE_S3_REGION="eu-north-1"
```

Command Prompt:

```bat
set AWS_ACCESS_KEY_ID=your_access_key
set AWS_SECRET_ACCESS_KEY=your_secret_key
set AWS_IMAGE_S3_REGION=eu-north-1
```

Linux/macOS:

```bash
export AWS_ACCESS_KEY_ID="your_access_key"
export AWS_SECRET_ACCESS_KEY="your_secret_key"
export AWS_IMAGE_S3_REGION="eu-north-1"
```

## Running the Application

From the project root:

```powershell
.\mvnw.cmd spring-boot:run
```

Or, if Maven is installed globally:

```powershell
mvn spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

## API Documentation

Swagger UI is available after the application starts:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON is available at:

```text
http://localhost:8080/v3/api-docs
```

## REST API

Base URL:

```text
http://localhost:8080/api/images
```

### Upload Image

Uploads an image file to S3 and saves the generated URL in MySQL.

```http
POST /api/images/upload?confirm=true
Content-Type: multipart/form-data
```

Form field:

| Field | Type | Required | Description |
|---|---|---:|---|
| `file` | File | Yes | Image file to upload |
| `confirm` | Boolean | No | Must be `true` to complete upload |

Example using curl:

```bash
curl -X POST "http://localhost:8080/api/images/upload?confirm=true" \
  -F "file=@sample-image.jpg"
```

Successful response:

```text
https://your-s3-bucket-name.s3.amazonaws.com/generated-file-name.jpg
```

If `confirm=true` is not sent:

```text
Please confirm image upload by sending confirm=true.
```

### Get All Images

Returns all image records saved in the database.

```http
GET /api/images
```

Example using curl:

```bash
curl http://localhost:8080/api/images
```

Example response:

```json
[
  {
    "id": 1,
    "imageName": "8a6c8c5e-5b1b-4d6a-95d3_sample-image.jpg",
    "imageUrl": "https://your-s3-bucket-name.s3.amazonaws.com/8a6c8c5e-5b1b-4d6a-95d3_sample-image.jpg"
  }
]
```

## S3 Bucket Notes

The service generates URLs in this format:

```text
https://{bucket-name}.s3.amazonaws.com/{file-name}
```

Uploading to S3 does not automatically make an object publicly readable. If the returned URL must be directly accessible in a browser, configure bucket policy, object permissions, CloudFront, or presigned URLs according to your security requirements.

For production systems, prefer private buckets with presigned URLs or CloudFront signed URLs instead of making uploaded files public by default.

## Build and Test

Build the project:

```powershell
.\mvnw.cmd clean package
```

Run tests:

```powershell
.\mvnw.cmd test
```

Because the application context creates an `S3Client` and connects to MySQL, tests may require valid environment variables and a reachable database unless test-specific configuration is added.

## Troubleshooting

### AWS credentials are not loaded

The application throws this error when `AWS_ACCESS_KEY_ID` or `AWS_SECRET_ACCESS_KEY` is missing:

```text
AWS credentials are not loaded. Set AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY in the system environment.
```

Fix: set both environment variables before starting the application.

### Cannot connect to MySQL

Check that:

- MySQL is running
- Database `s3_image` exists
- Username and password are correct
- Port `3306` is available

### Upload succeeds but image URL does not open

The object may be private. Check the S3 bucket access policy or use presigned URLs for controlled access.

### File is too large

The current upload limit is 10 MB. Increase these values if larger uploads are required:

```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

## Security Recommendations

- Do not commit real AWS keys or database passwords.
- Use environment variables, AWS profiles, IAM roles, or a secrets manager.
- Grant only the S3 permissions the application needs.
- Keep S3 buckets private unless public access is intentionally required.
- Validate allowed file types before accepting uploads in production.
- Consider scanning uploaded files if the service accepts user-generated content.
- Use presigned URLs for private image access.

## Future Improvements

- Add file type validation for images only
- Add file size validation with clearer API errors
- Add delete image endpoint
- Add presigned URL support
- Add pagination for image listing
- Add test profile with H2 or Testcontainers
- Add structured exception handling using `@ControllerAdvice`
- Add audit fields such as upload time and original file name

## Author

Developed as a Spring Boot AWS S3 image URL generator project.
