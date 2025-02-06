# Keep Spring Boot classes
-keep class org.springframework.** { *; }
-keep class org.springframework.boot.** { *; }
-keep class org.springframework.context.** { *; }

# Keep Jackson (for JSON serialization/deserialization)
-keep class com.fasterxml.jackson.** { *; }

# Keep main class (Spring Boot entry point)
-keep class com.yourpackage.Application { public static void main(java.lang.String[]); }
