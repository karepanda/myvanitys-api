# Development Environment Setup

## Run the Application from IntelliJ IDEA

### Maven Run Configuration

1. Go to **Run > Edit Configurations...**
2. Click the **+** button and select **Maven**
3. Configure the following values:
    - **Name**: `MyVanitys Run`
    - **Command line**: `spring-boot:run`
    - **Properties**: `spring-boot.run.profiles=local`

   ![Configuration Example](https://i.ibb.co/... "Upload a screenshot if possible")

4. Click **Apply** and then **OK**
5. Now you can run the application by selecting this configuration and clicking the run button

### Alternative Configuration as Java Application

If the Maven configuration does not work properly, you can use this alternative:

1. Go to **Run > Edit Configurations...**
2. Click the **+** button and select **Application**
3. Configure the following values:
    - **Name**: `MyVanitys Application`
    - **Main class**: `com.myvanitys.api.YourMainClass` (adjust according to your main class)
    - **VM options**: `-Dspring.profiles.active=local`
4. Click **Apply** and then **OK**
