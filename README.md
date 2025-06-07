Для того что бы запустить приложение:
- Скомпилируйте проект: mvn clean package
- Для консольного режима: java -jar target/user-service-1.0.0.jar --spring.profiles.active=console
- Для API режима: java -jar target/user-service-1.0.0.jar

Получение пользователя по ID curl.exe http://localhost:8080/api/users/1 
Получение пользователя по Email curl.exe http://localhost:8080/api/users/email/ivan@example.com
Получение всех пользователей curl.exe http://localhost:8080/api/users
Удаление пользователя curl.exe -X DELETE http://localhost:8080/api/users/1
Создание пользователя
$body = @{
    name = "Иван Иванов"
    email = "ivan@example.com"
    age = 25
} | ConvertTo-Json -Compress

Invoke-WebRequest -Uri "http://localhost:8080/api/users" -Method POST -Body $body -ContentType "application/json"
