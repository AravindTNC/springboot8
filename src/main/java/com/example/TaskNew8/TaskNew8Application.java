    package com.example.TaskNew8;

    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.scheduling.annotation.EnableScheduling;

    @SpringBootApplication
    @EnableScheduling  // Add this annotation
    public class TaskNew8Application {

        public static void main(String[] args) {
            SpringApplication.run(TaskNew8Application.class, args);
        }
    }